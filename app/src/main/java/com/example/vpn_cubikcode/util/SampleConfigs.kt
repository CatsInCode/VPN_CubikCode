package com.example.vpn_cubikcode.util

object SampleConfigs {
    val XRAY_VLESS_REALITY = """
{
  "dns": {
    "hosts": {
      "domain:googleapis.cn": "googleapis.com"
    },
    "queryStrategy": "UseIPv4",
    "servers": [
      "1.1.1.1",
      {
        "address": "1.1.1.1",
        "domains": [],
        "port": 53
      },
      {
        "address": "8.8.8.8",
        "domains": [],
        "port": 53
      }
    ]
  },
  "inbounds": [
    {
      "port": 10808,
      "protocol": "socks",
      "settings": {
        "auth": "noauth",
        "udp": true,
        "userLevel": 8
      },
      "sniffing": {
        "destOverride": ["http", "tls", "quic"],
        "enabled": true
      },
      "tag": "socks"
    },
    {
      "port": 10809,
      "protocol": "http",
      "settings": {
        "userLevel": 8
      },
      "sniffing": {
        "destOverride": ["http", "tls", "quic"],
        "enabled": true
      },
      "tag": "http"
    },
    {
      "listen": "127.0.0.1",
      "port": 11111,
      "protocol": "dokodemo-door",
      "settings": {
        "address": "127.0.0.1"
      },
      "tag": "metrics_in"
    }
  ],
  "log": {
    "loglevel": "warning"
  },
  "metrics": {
    "tag": "metrics_out"
  },
  "outbounds": [
    {
      "mux": {
        "concurrency": -1,
        "enabled": false,
        "xudpConcurrency": 8,
        "xudpProxyUDP443": ""
      },
      "protocol": "vless",
      "settings": {
        "vnext": [
          {
            "address": "node.africaparty.host",
            "port": 443,
            "users": [
              {
                "encryption": "none",
                "flow": "xtls-rprx-vision",
                "id": "99ce47dd-fc6b-4cd4-a5ce-9f2ac5000fc2",
                "level": 8,
                "security": "auto"
              }
            ]
          }
        ]
      },
      "streamSettings": {
        "network": "tcp",
        "realitySettings": {
          "allowInsecure": false,
          "fingerprint": "chrome",
          "publicKey": "zme14cCdvdafer0AkwOmtBE_yYaiS0-Cj4vKtijkuGo",
          "serverName": "node.africaparty.host",
          "shortId": "5f42dfa0b327cccd",
          "show": false,
          "spiderX": "/"
        },
        "security": "reality",
        "tcpSettings": {
          "header": {
            "type": "none"
          }
        }
      },
      "tag": "proxy"
    },
    {
      "protocol": "freedom",
      "settings": {
        "domainStrategy": "UseIP"
      },
      "tag": "direct"
    },
    {
      "protocol": "blackhole",
      "settings": {
        "response": {
          "type": "http"
        }
      },
      "tag": "block"
    }
  ],
  "policy": {
    "levels": {
      "0": {
        "statsUserDownlink": true,
        "statsUserUplink": true
      },
      "8": {
        "connIdle": 300,
        "downlinkOnly": 1,
        "handshake": 4,
        "uplinkOnly": 1
      }
    },
    "system": {
      "statsInboundDownlink": true,
      "statsInboundUplink": true,
      "statsOutboundDownlink": true,
      "statsOutboundUplink": true
    }
  },
  "remarks": "Steal",
  "routing": {
    "domainStrategy": "IPIfNonMatch",
    "rules": [
      {
        "inboundTag": ["metrics_in"],
        "outboundTag": "metrics_out"
      },
      {
        "inboundTag": ["socks"],
        "outboundTag": "direct",
        "port": "53"
      },
      {
        "ip": ["1.1.1.1"],
        "outboundTag": "direct",
        "port": "53"
      },
      {
        "ip": ["8.8.8.8"],
        "outboundTag": "direct",
        "port": "53"
      }
    ]
  },
  "stats": {}
}
""".trimIndent()
}
