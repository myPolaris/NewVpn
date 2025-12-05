package com.swift.newvpn.vpn;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.ToNumberPolicy;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.swift.newvpn.utils.Utils;

public class SingBoxOptions {

    // base
    private static final Gson gsonSingbox = new GsonBuilder()
            .registerTypeHierarchyAdapter(SingBoxOption.class, new SingBoxOptionSerializer())
            .setPrettyPrinting()
            .setNumberToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .setLenient()
            .disableHtmlEscaping()
            .create();

    public static class SingBoxOption {

        public transient Map<String, Object> _hack_config_map; // 仍然用普通json方式合并，所以Object内不要使用 _hack

        public transient String _hack_custom_config;

        public SingBoxOption() {
            _hack_config_map = new HashMap<>();
        }

        public Map<String, Object> asMap() {
            return gsonSingbox.fromJson(gsonSingbox.toJson(this), Map.class);
        }

    }

    // 自定义序列化器
    public static class SingBoxOptionSerializer implements JsonSerializer<SingBoxOption> {
        @Override
        public JsonElement serialize(SingBoxOption src, Type typeOfSrc, JsonSerializationContext context) {
            // 拿到原始的 delegate（默认序列化器）
            TypeAdapter<?> delegate = gsonSingbox.getDelegateAdapter(
                    new TypeAdapterFactory() {
                        @Override
                        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                            return null; // 返回 null，表示只作为“跳过当前自定义”的 marker
                        }
                    },
                    TypeToken.get(src.getClass())
            );
            Map<String, Object> map;
            map = gsonSingbox.fromJson(((TypeAdapter<SingBoxOption>) delegate).toJson(src), Map.class);
            if (src._hack_config_map != null && !src._hack_config_map.isEmpty()) {
                Utils.INSTANCE.mergeMap(map, src._hack_config_map);
            }
            if (src._hack_custom_config != null && !src._hack_custom_config.isBlank()) {
                Utils.INSTANCE.mergeJSON(map, src._hack_custom_config);
            }
            return gsonSingbox.toJsonTree(map);
        }
    }

    // custom classes

    public static class User {
        public String username;
        public String password;
    }

    public static class ScapeVpnOptions extends SingBoxOption {
        public LogOptions log;

        public DNSOptions dns;

        public NTPOptions ntp;

        public List<Inbound> inbounds;

        public List<SingBoxOption> outbounds;

        public RouteOptions route;

        public ExperimentalOptions experimental;

    }

    // paste generate output here

    public static class ClashAPIOptions extends SingBoxOption {

        public String external_controller;

        public String external_ui;

        public String external_ui_download_url;

        public String external_ui_download_detour;

        public String secret;

        public String default_mode;

        // Generate note: option type:  public List<String> ModeList;

    }

    public static class LogOptions extends SingBoxOption {

        public Boolean disabled;

        public String level;

        public String output;

        public Boolean timestamp;

        // Generate note: option type:  public Boolean DisableColor;

    }

    public static class DebugOptions extends SingBoxOption {

        public String listen;

        public Integer gc_percent;

        public Integer max_stack;

        public Integer max_threads;

        public Boolean panic_on_fault;

        public String trace_back;

        public Long memory_limit;

        public Boolean oom_killer;

    }

    public static class DNSOptions extends SingBoxOption {

        public List<DNSServerOptions> servers;

        public List<DNSRule> rules;

        @SerializedName("final")
        public String final_;

        public Boolean reverse_mapping;

        public DNSFakeIPOptions fakeip;

        // Generate note: nested type DNSClientOptions
        public String strategy;

        public Boolean disable_cache;

        public Boolean disable_expire;

        public Boolean independent_cache;

        // End of public DNSClientOptions ;

    }

    public static class DNSServerOptions extends SingBoxOption {

        public String tag;

        public String address;

        public String address_resolver;

        public String address_strategy;

        public Long address_fallback_delay;

        public String strategy;

        public String detour;

    }

    public static class DNSFakeIPOptions extends SingBoxOption {

        public Boolean enabled;

        public String inet4_range;

        public String inet6_range;

    }

    public static class ExperimentalOptions extends SingBoxOption {

        public ClashAPIOptions clash_api;

        public V2RayAPIOptions v2ray_api;

        public CacheFile cache_file;

        public DebugOptions debug;

    }

    public static class CacheFile extends SingBoxOption {

        public Boolean enabled;

        public Boolean store_fakeip;

        public String path;

        public String cache_id;

    }

    public static class Inbound extends SingBoxOption {

        public String type;

        public String tag;

    }

    public static class NTPOptions extends SingBoxOption {

        public Boolean enabled;

        public Long interval;

        public Boolean write_to_system;

        // Generate note: nested type ServerOptions
        public String server;

        public Integer server_port;

        // End of public ServerOptions ;

        // Generate note: nested type DialerOptions
        public String detour;

        public String bind_interface;

        public String inet4_bind_address;

        public String inet6_bind_address;

        public String protect_path;

        public Integer routing_mark;

        public Boolean reuse_addr;

        public Long connect_timeout;

        public Boolean tcp_fast_open;

        public Boolean tcp_multi_path;

        public Boolean udp_fragment;

        // Generate note: option type:  public Boolean UDPFragmentDefault;

        public String domain_strategy;

        public Long fallback_delay;

        // End of public DialerOptions ;

    }

    public static class Outbound extends SingBoxOption {

        public String type;

        public String tag;
    }

    public static class RouteOptions extends SingBoxOption {

        public List<Rule> rules;

        public List<RuleSet> rule_set;

        @SerializedName("final")
        public String final_;

        public Boolean find_process;

        public Boolean auto_detect_interface;

        public Boolean override_android_vpn;

        public String default_interface;

        public Integer default_mark;

    }

    public static class Rule extends SingBoxOption {

        public String type;

    }

    public static class RuleSet extends SingBoxOption {

        public String type;

        public String tag;

        public String format;

        public String path;

        public String url;

    }

    public static class DNSRule extends SingBoxOption {

        public String type;
    }

    public static class InboundTLSOptions extends SingBoxOption {

        public Boolean enabled;

        public String server_name;

        public Boolean insecure;

        // Generate note: Listable
        public List<String> alpn;

        public String min_version;

        public String max_version;

        // Generate note: Listable
        public List<String> cipher_suites;

        // Generate note: Listable
        public List<String> certificate;

        public String certificate_path;

        // Generate note: Listable
        public List<String> key;

        public String key_path;

        public InboundACMEOptions acme;

        public InboundECHOptions ech;

        public InboundRealityOptions reality;

    }

    public static class InboundRealityOptions extends SingBoxOption {

        public Boolean enabled;

        public InboundRealityHandshakeOptions handshake;

        public String private_key;

        // Generate note: Listable
        public List<String> short_id;

        public Long max_time_difference;

    }

    public static class InboundRealityHandshakeOptions extends SingBoxOption {

        // Generate note: nested type ServerOptions
        public String server;

        public Integer server_port;

        public String detour;

        public String bind_interface;

        public String inet4_bind_address;

        public String inet6_bind_address;

        public String protect_path;

        public Integer routing_mark;

        public Boolean reuse_addr;

        public Long connect_timeout;

        public Boolean tcp_fast_open;

        public Boolean tcp_multi_path;

        public Boolean udp_fragment;

        // Generate note: option type:  public Boolean UDPFragmentDefault;

        public String domain_strategy;

        public Long fallback_delay;

    }

    public static class InboundECHOptions extends SingBoxOption {

        public Boolean enabled;

        // Generate note: Listable
        public List<String> key;

        public String key_path;

    }

    public static class InboundACMEOptions extends SingBoxOption {

        // Generate note: Listable
        public List<String> domain;

        public String data_directory;

        public String default_server_name;

        public String email;

        public String provider;

        public Boolean disable_http_challenge;

        public Boolean disable_tls_alpn_challenge;

        public Integer alternative_http_port;

        public Integer alternative_tls_port;

        public ACMEExternalAccountOptions external_account;

    }

    public static class ACMEExternalAccountOptions extends SingBoxOption {

        public String key_id;

        public String mac_key;

    }

    public static class TunPlatformOptions extends SingBoxOption {

        public HTTPProxyOptions http_proxy;

    }

    public static class HTTPProxyOptions extends SingBoxOption {

        public Boolean enabled;

        public String server;

        public Integer server_port;

    }

    public static class UDPOverTCPOptions extends SingBoxOption {

        public Boolean enabled;

        public Integer version;

    }

    public static class V2RayAPIOptions extends SingBoxOption {

        public String listen;

        public V2RayStatsServiceOptions stats;

    }

    public static class V2RayStatsServiceOptions extends SingBoxOption {

        public Boolean enabled;

        public List<String> inbounds;

        public List<String> outbounds;

        public List<String> users;

    }

    public static class Inbound_TunOptions extends Inbound {

        public String interface_name;

        public Integer mtu;

        // Generate note: Listable
        public List<String> inet4_address;

        // Generate note: Listable
        public List<String> inet6_address;

        public Boolean auto_route;

        public Boolean strict_route;

        // Generate note: Listable
        public List<String> inet4_route_address;

        // Generate note: Listable
        public List<String> inet6_route_address;

        // Generate note: Listable
        public List<String> include_interface;

        // Generate note: Listable
        public List<String> exclude_interface;

        // Generate note: Listable
        public List<Integer> include_uid;

        // Generate note: Listable
        public List<String> include_uid_range;

        // Generate note: Listable
        public List<Integer> exclude_uid;

        // Generate note: Listable
        public List<String> exclude_uid_range;

        // Generate note: Listable
        public List<Integer> include_android_user;

        // Generate note: Listable
        public List<String> include_package;

        // Generate note: Listable
        public List<String> exclude_package;

        public Boolean endpoint_independent_nat;

        public Long udp_timeout;

        public String stack;

        public TunPlatformOptions platform;

        // Generate note: nested type InboundOptions
        public Boolean sniff;

        public Boolean sniff_override_destination;

        public Long sniff_timeout;

        public String domain_strategy;

        // End of public InboundOptions ;

    }

    public static class Inbound_MixedOptions extends Inbound {

        // Generate note: nested type ListenOptions
        public String listen;

        public Integer listen_port;

        public Boolean tcp_fast_open;

        public Boolean tcp_multi_path;

        public Boolean udp_fragment;


        public Long udp_timeout;

        public Boolean proxy_protocol;

        public Boolean proxy_protocol_accept_no_header;

        public String detour;

        // Generate note: nested type InboundOptions
        public Boolean sniff;

        public Boolean sniff_override_destination;

        public Long sniff_timeout;

        public String domain_strategy;
        public List<User> users;

        public Boolean set_system_proxy;

        public InboundTLSOptions tls;

    }

    public static class Outbound_SocksOptions extends Outbound {

        // Generate note: nested type DialerOptions
        public String detour;

        public String bind_interface;

        public String inet4_bind_address;

        public String inet6_bind_address;

        public String protect_path;

        public Integer routing_mark;

        public Boolean reuse_addr;

        public Long connect_timeout;

        public Boolean tcp_fast_open;

        public Boolean tcp_multi_path;

        public Boolean udp_fragment;


        public String domain_strategy;

        public Long fallback_delay;

        // End of public DialerOptions ;

        // Generate note: nested type ServerOptions
        public String server;

        public Integer server_port;

        // End of public ServerOptions ;

        public String version;

        public String username;

        public String password;

        public String network;

        public UDPOverTCPOptions udp_over_tcp;

    }

    public static class Outbound_SelectorOptions extends Outbound {

        public List<String> outbounds;

        @SerializedName("default")
        public String default_;

    }

    public static class Rule_DefaultOptions extends Rule {

        // Generate note: Listable
        public List<String> inbound;

        public Integer ip_version;

        // Generate note: Listable
        public List<String> network;

        // Generate note: Listable
        public List<String> auth_user;

        // Generate note: Listable
        public List<String> protocol;

        // Generate note: Listable
        public List<String> domain;

        // Generate note: Listable
        public List<String> domain_suffix;

        // Generate note: Listable
        public List<String> domain_keyword;

        // Generate note: Listable
        public List<String> domain_regex;

        public List<String> rule_set;

        public Boolean source_ip_is_private;

        public Boolean ip_is_private;

        // Generate note: Listable
        public List<String> source_ip_cidr;

        // Generate note: Listable
        public List<String> ip_cidr;

        // Generate note: Listable
        public List<Integer> source_port;

        // Generate note: Listable
        public List<String> source_port_range;

        // Generate note: Listable
        public List<Integer> port;

        // Generate note: Listable
        public List<String> port_range;

        // Generate note: Listable
        public List<String> process_name;

        // Generate note: Listable
        public List<String> process_path;

        // Generate note: Listable
        public List<String> package_name;

        // Generate note: Listable
        public List<String> user;

        // Generate note: Listable
        public List<Integer> user_id;

        public String clash_mode;

        public Boolean invert;

        public String action;

        public String outbound;

    }

    public static class DNSRule_DefaultOptions extends DNSRule {

        // Generate note: Listable
        public List<String> inbound;

        public Integer ip_version;

        // Generate note: Listable
        public List<String> query_type;

        // Generate note: Listable
        public List<String> network;

        // Generate note: Listable
        public List<String> auth_user;

        // Generate note: Listable
        public List<String> protocol;

        // Generate note: Listable
        public List<String> domain;

        // Generate note: Listable
        public List<String> domain_suffix;

        // Generate note: Listable
        public List<String> domain_keyword;

        // Generate note: Listable
        public List<String> domain_regex;

        public List<String> rule_set;

        // Generate note: Listable
        public List<String> source_ip_cidr;

        // Generate note: Listable
        public List<Integer> source_port;

        // Generate note: Listable
        public List<String> source_port_range;

        // Generate note: Listable
        public List<Integer> port;

        // Generate note: Listable
        public List<String> port_range;

        // Generate note: Listable
        public List<String> process_name;

        // Generate note: Listable
        public List<String> process_path;

        // Generate note: Listable
        public List<String> package_name;

        // Generate note: Listable
        public List<String> user;

        // Generate note: Listable
        public List<Integer> user_id;

        // Generate note: Listable
        public List<String> outbound;

        public String clash_mode;

        public Boolean invert;

        public String server;

        public Boolean disable_cache;

        public Integer rewrite_ttl;

    }
}
