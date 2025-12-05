package com.swift.newvpn.vpn


import android.os.Binder
import com.swift.newvpn.BuildConfig
import com.swift.newvpn.base.CacheKey
import com.swift.newvpn.base.IPv6Mode
import com.swift.newvpn.base.KvCache
import com.swift.newvpn.model.SocksBean
import com.swift.newvpn.vpn.SingBoxOptions.DNSFakeIPOptions
import com.swift.newvpn.vpn.SingBoxOptions.DNSOptions
import com.swift.newvpn.vpn.SingBoxOptions.DNSRule_DefaultOptions
import com.swift.newvpn.vpn.SingBoxOptions.DNSServerOptions
import com.swift.newvpn.vpn.SingBoxOptions.Inbound_MixedOptions
import com.swift.newvpn.vpn.SingBoxOptions.Inbound_TunOptions
import com.swift.newvpn.vpn.SingBoxOptions.LogOptions
import com.swift.newvpn.vpn.SingBoxOptions.Outbound
import com.swift.newvpn.vpn.SingBoxOptions.Outbound_SelectorOptions
import com.swift.newvpn.vpn.SingBoxOptions.RouteOptions
import com.swift.newvpn.vpn.SingBoxOptions.Rule_DefaultOptions
import com.swift.newvpn.vpn.SingBoxOptions.SingBoxOption
import com.swift.newvpn.vpn.SingBoxOptions.ScapeVpnOptions
import com.swift.newvpn.vpn.services.ScapeVpnService
import com.swift.newvpn.utils.ProxyManager
import com.swift.newvpn.utils.Utils
import com.swift.newvpn.utils.isIpAddress
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

const val TAG_MIXED = "mixed-in"

const val TAG_PROXY = "proxy"
const val TAG_DIRECT = "direct"
const val TAG_BYPASS = "bypass"

const val LOCALHOST = "127.0.0.1"

const val DEF_REMOTE_DNS = "https://dns.google/dns-query"
const val DEF_DIRECT_DNS = "https://223.5.5.5/dns-query"

object Config{
    const val ipv6Mode = IPv6Mode.DISABLE
    val logLevel = if (BuildConfig.DEBUG) 4 else 0
    const val mtu = 9000
    const val resolveDestination = false
    private val userIndex by lazy { Binder.getCallingUserHandle().hashCode() }

    val mixedPort: Int
        get() =  parsePort(KvCache.kv.getString(CacheKey.MIXED_PORT,""), 2080 + userIndex)

    fun parsePort(str: String?, default: Int, min: Int = 1025): Int {
        val value = str?.toIntOrNull() ?: default
        return if (value !in min..65535) default else value
    }
}

class ConfigBuildResult(
    var config: String,
    var trafficMap: Map<String, List<SocksBean>>,
    var profileTagMap: Map<String, String>
) {
    data class IndexEntity(var chain: LinkedHashMap<Int, SocksBean>)
}

fun buildConfig(
    proxy: SocksBean
): ConfigBuildResult {

    val trafficMap = HashMap<String, List<SocksBean>>()
    val tagMap = HashMap<String, String>()
    val globalOutbounds = HashMap<String, String>()
    val selectorNames = ArrayList<String>()

    fun selectorName(name_: String): String {
        var name = name_
        var count = 0
        while (selectorNames.contains(name)) {
            count++
            name = "$name_-$count"
        }
        selectorNames.add(name)
        return name
    }

    fun SocksBean.resolveChain(): MutableList<SocksBean> = mutableListOf(this)
    val userDNSRuleList = mutableListOf<DNSRule_DefaultOptions>()
    val domainListDNSDirectForce = mutableListOf<String>()
    val bypassDNSBeans = hashSetOf<SocksBean>()
    val isVPN = true
    val bind = LOCALHOST
    val remoteDns = DEF_REMOTE_DNS.split("\n")
        .mapNotNull { dns -> dns.trim().takeIf { it.isNotBlank() && !it.startsWith("#") } }
    val directDNS = DEF_DIRECT_DNS.split("\n")
        .mapNotNull { dns -> dns.trim().takeIf { it.isNotBlank() && !it.startsWith("#") } }
    val needSniff = true
    val needSniffOverride = false
    val externalIndexMap = ArrayList<ConfigBuildResult.IndexEntity>()
    val ipv6Mode = Config.ipv6Mode

    fun genDomainStrategy(noAsIs: Boolean): String {
        return when {
            !noAsIs -> ""
            ipv6Mode == IPv6Mode.DISABLE -> "ipv4_only"
            ipv6Mode == IPv6Mode.PREFER -> "prefer_ipv6"
            ipv6Mode == IPv6Mode.ONLY -> "ipv6_only"
            else -> "prefer_ipv4"
        }
    }

    return ScapeVpnOptions().apply {
        log = LogOptions().apply {
            level = when (Config.logLevel) {
                0 -> "panic"
                1 -> "warn"
                2 -> "info"
                3 -> "debug"
                4 -> "trace"
                else -> "info"
            }
        }

        dns = DNSOptions().apply {
            servers = mutableListOf()
            rules = mutableListOf()
            independent_cache = true
        }

        fun autoDnsDomainStrategy(s: String): String? {
            if (s.isNotEmpty()) {
                return s
            }
            return when (ipv6Mode) {
                IPv6Mode.DISABLE -> "ipv4_only"
                IPv6Mode.ENABLE -> "prefer_ipv4"
                IPv6Mode.PREFER -> "prefer_ipv6"
                IPv6Mode.ONLY -> "ipv6_only"
                else -> null
            }
        }

        inbounds = mutableListOf()

        if (isVPN) inbounds.add(Inbound_TunOptions().apply {
            type = "tun"
            tag = "tun-in"
            stack = "gvisor"
            endpoint_independent_nat = true
            mtu = Config.mtu
            domain_strategy = genDomainStrategy(Config.resolveDestination)
            sniff = needSniff
            sniff_override_destination = needSniffOverride
            when (ipv6Mode) {
                IPv6Mode.DISABLE -> {
                    inet4_address = listOf(ScapeVpnService.PRIVATE_VLAN4_CLIENT + "/28")
                }

                IPv6Mode.ONLY -> {
                    inet6_address = listOf(ScapeVpnService.PRIVATE_VLAN6_CLIENT + "/126")
                }

                else -> {
                    inet4_address = listOf(ScapeVpnService.PRIVATE_VLAN4_CLIENT + "/28")
                    inet6_address = listOf(ScapeVpnService.PRIVATE_VLAN6_CLIENT + "/126")
                }
            }
        })
        inbounds.add(Inbound_MixedOptions().apply {
            type = "mixed"
            tag = TAG_MIXED
            listen = bind
            listen_port = Config.mixedPort
            domain_strategy = genDomainStrategy(Config.resolveDestination)
            sniff = needSniff
            sniff_override_destination = needSniffOverride
        })
        outbounds = mutableListOf()

        // init routing object
        route = RouteOptions().apply {
            auto_detect_interface = true
            rules = mutableListOf()
            rule_set = mutableListOf()
        }

        // returns outbound tag
        fun buildChain(
            chainId: String, entity: SocksBean
        ): String {
            val profileList = entity.resolveChain()
            val chainTrafficSet = HashSet<SocksBean>().apply {
                plusAssign(profileList)
                add(entity)
            }

            var currentOutbound: SingBoxOption
            lateinit var pastOutbound: SingBoxOption
            var pastEntity: SocksBean? = null
            val externalChainMap = LinkedHashMap<Int, SocksBean>()
            externalIndexMap.add(ConfigBuildResult.IndexEntity(externalChainMap))
            val chainOutbounds = ArrayList<SingBoxOption>()

            var chainTagOut = ""
            val chainTag = "c-$chainId"

            val defaultServerDomainStrategy = SingBoxOptionsUtil.domainStrategy("server")

            profileList.forEachIndexed { index, bean ->

                var tagOut = "$chainTag-${bean.name}"

                // needGlobal: can only contain one?
                var needGlobal = false

                // first profile set as global
                if (index == profileList.lastIndex) {
                    needGlobal = true
                    tagOut = "g-" + bean.name
                    bypassDNSBeans += bean
                }

                // last profile set as "proxy"
                if (chainId.isEmpty() && index == 0) {
                    tagOut = TAG_PROXY
                }

                // selector human readable name
                if (index == 0) {
                    tagOut = selectorName(bean.displayName())
                }

                // chain rules
                if (index > 0) {
                    pastOutbound._hack_config_map["detour"] = tagOut
                } else {
                    // index == 0 means last profile in chain / not chain
                    chainTagOut = tagOut
                }

                // now tagOut is determined
                if (needGlobal) {
                    globalOutbounds[bean.name]?.let {
                        if (index == 0) chainTagOut = it // single, duplicate chain
                        return@forEachIndexed
                    }
                    globalOutbounds[bean.name] = tagOut
                }

                currentOutbound = SingBoxOptionsUtil.buildSingBoxOutboundSocksBean(bean)

                // internal & external
                currentOutbound.apply {
                    // udp over tcp
                    // domain_strategy
                    pastEntity?.apply {
                        // don't loopback
                        if (defaultServerDomainStrategy != "" && !serverAddress.isIpAddress()) {
                            domainListDNSDirectForce.add("full:${serverAddress}")
                        }
                    }
                    _hack_config_map["domain_strategy"] = defaultServerDomainStrategy

                    _hack_config_map["tag"] = tagOut

                    _hack_custom_config = bean.customOutboundJson
                }

                bean.finalAddress = bean.serverAddress
                bean.finalPort = bean.serverPort

                outbounds.add(currentOutbound)
                chainOutbounds.add(currentOutbound)
                pastOutbound = currentOutbound
                pastEntity = bean
            }

            trafficMap[chainTagOut] = chainTrafficSet.toList()
            return chainTagOut
        }

        // build outbounds
        val list = ProxyManager.getAll()
        list.forEach {
            tagMap[it.name] = buildChain(it.name, it)
        }
        outbounds.add(0, Outbound_SelectorOptions().apply {
            type = "selector"
            tag = TAG_PROXY
            default_ = tagMap[proxy.name]
            outbounds = tagMap.values.toList()
        })
        // 对 rule_set tag 去重
        if (route.rule_set != null) {
            route.rule_set = route.rule_set.distinctBy { it.tag }
        }

        for (freedom in arrayOf(TAG_DIRECT, TAG_BYPASS)) outbounds.add(Outbound().apply {
            tag = freedom
            type = "direct"
        })

        // Bypass Lookup for the first profile
        bypassDNSBeans.forEach {
            val serverAddr = it.serverAddress

            if (!serverAddr.isIpAddress()) {
                domainListDNSDirectForce.add("full:${serverAddr}")
            }
        }

        remoteDns.forEach {
            var address = it
            if (address.contains("://")) {
                address = address.substringAfter("://")
            }
            "https://$address".toHttpUrlOrNull()?.apply {
                if (!host.isIpAddress()) {
                    domainListDNSDirectForce.add("full:$host")
                }
            }
        }

        dns.servers.add(DNSServerOptions().apply {
            address = "rcode://success"
            tag = "dns-block"
        })

        dns.servers.add(DNSServerOptions().apply {
            address = "local"
            tag = "dns-local"
            detour = TAG_DIRECT
        })

        directDNS.firstOrNull().let {
            dns.servers.add(DNSServerOptions().apply {
                address = it ?: throw Exception("No direct DNS, check your settings!")
                tag = "dns-direct"
                detour = TAG_DIRECT
                address_resolver = "dns-local"
                strategy = autoDnsDomainStrategy(SingBoxOptionsUtil.domainStrategy(tag))
            })
        }

        remoteDns.firstOrNull().let {
            // Always use direct DNS for urlTest
            dns.servers.add(DNSServerOptions().apply {
                address = it ?: throw Exception("No remote DNS, check your settings!")
                tag = "dns-remote"
                address_resolver = "dns-direct"
                strategy = autoDnsDomainStrategy(SingBoxOptionsUtil.domainStrategy(tag))
            })
        }

        dns.final_ = "dns-remote"

        // dns object user rules
        userDNSRuleList.forEach {
            if (!it.checkEmpty()) dns.rules.add(it)
        }

        // built-in DNS rules
        route.rules.add(0, Rule_DefaultOptions().apply {
            protocol = listOf("dns")
            action = "hijack-dns"
        })
        route.rules.add(0, Rule_DefaultOptions().apply {
            port = listOf(53)
            action = "hijack-dns"
        })
        // block mcast
        route.rules.add(Rule_DefaultOptions().apply {
            ip_cidr = listOf("224.0.0.0/3", "ff00::/8")
            source_ip_cidr = listOf("224.0.0.0/3", "ff00::/8")
            action = "reject"
        })
        // FakeDNS obj
        dns.fakeip = DNSFakeIPOptions().apply {
            enabled = true
            inet4_range = "198.18.0.0/15"
            inet6_range = "fc00::/18"
        }
        dns.servers.add(DNSServerOptions().apply {
            address = "fakeip"
            tag = "dns-fake"
            strategy = "ipv4_only"
        })
        dns.rules.add(DNSRule_DefaultOptions().apply {
            inbound = listOf("tun-in")
            server = "dns-fake"
            disable_cache = true
        })
        // avoid loopback
        dns.rules.add(0, DNSRule_DefaultOptions().apply {
            outbound = mutableListOf("any")
            server = "dns-direct"
        })
        // force bypass (always top DNS rule)
        if (domainListDNSDirectForce.isNotEmpty()) {
            dns.rules.add(0, DNSRule_DefaultOptions().apply {
                makeSingBoxRule(domainListDNSDirectForce.toHashSet().toList())
                server = "dns-direct"
            })
        }

        _hack_custom_config = ""
    }.let {
        val configMap = it.asMap()
        Utils.mergeJSON(configMap, proxy.customConfigJson)
        ConfigBuildResult(
            Utils.gson.toJson(configMap),
            trafficMap,
            tagMap
        )
    }
}
