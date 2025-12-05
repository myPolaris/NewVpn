package com.swift.newvpn.vpn

import com.swift.newvpn.base.KvCache
import com.swift.newvpn.model.SocksBean

object SingBoxOptionsUtil {

    fun domainStrategy(tag: String): String = when (tag) {
        "dns-remote" -> {
            auto2("domain_strategy_for_remote", "")
        }

        "dns-direct" -> {
            auto2("domain_strategy_for_direct", "")
        }

        // server
        else -> {
            auto2("domain_strategy_for_server", "prefer_ipv4")
        }
    }

    private fun auto2(key: String, newS: String): String {
        return (KvCache.kv.getString(key, "") ?: "").replace("auto", newS)
    }

    fun buildSingBoxOutboundSocksBean(bean: SocksBean): SingBoxOptions.Outbound_SocksOptions {
        return SingBoxOptions.Outbound_SocksOptions().apply {
            type = "socks"
            server = bean.serverAddress
            server_port = bean.serverPort
            username = bean.username
            password = bean.password
            version = bean.protocolVersionName()
        }
    }
}

fun SingBoxOptions.DNSRule_DefaultOptions.makeSingBoxRule(list: List<String>) {
    rule_set = mutableListOf<String>()
    domain = mutableListOf<String>()
    domain_suffix = mutableListOf<String>()
    domain_regex = mutableListOf<String>()
    domain_keyword = mutableListOf<String>()
    list.forEach {
        when {
            it.startsWith("geosite:") -> {
                rule_set.plusAssign(it)
            }
            it.startsWith("full:") -> {
                domain.plusAssign(it.removePrefix("full:").lowercase())
            }
            it.startsWith("domain:") -> {
                domain_suffix.plusAssign(it.removePrefix("domain:").lowercase())
            }
            it.startsWith("regexp:") -> {
                domain_regex.plusAssign(it.removePrefix("regexp:").lowercase())
            }
            it.startsWith("keyword:") -> {
                domain_keyword.plusAssign(it.removePrefix("keyword:").lowercase())
            }
            else -> {
                domain_suffix.plusAssign(it.lowercase())
            }
        }
    }
    rule_set?.removeIf { it.isNullOrBlank() }
    domain?.removeIf { it.isNullOrBlank() }
    domain_suffix?.removeIf { it.isNullOrBlank() }
    domain_regex?.removeIf { it.isNullOrBlank() }
    domain_keyword?.removeIf { it.isNullOrBlank() }
    when {
        rule_set?.isEmpty() == true -> rule_set = null
        domain?.isEmpty() == true -> domain = null
        domain_suffix?.isEmpty() == true -> domain_suffix = null
        domain_regex?.isEmpty() == true -> domain_regex = null
        domain_keyword?.isEmpty() == true -> domain_keyword = null
    }
}

fun SingBoxOptions.DNSRule_DefaultOptions.checkEmpty(): Boolean {
    return when {
        rule_set?.isNotEmpty() == true -> false
        domain?.isNotEmpty() == true -> false
        domain_suffix?.isNotEmpty() == true -> false
        domain_regex?.isNotEmpty() == true -> false
        domain_keyword?.isNotEmpty() == true -> false
        user_id?.isNotEmpty() == true -> false
        else -> true
    }
}