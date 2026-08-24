<!-- worldline-map-schema=1 -->
<!-- boundary=protocol14-edge-packets -->
<!-- nonclaims=map-pixels,map-colors,sign-gui,rendering,keepalive-nonce,packet255-timeout,post-beta-packet-formats -->
<!-- frozen-trace=222d8c1c1f4e3a309412c8ee0a503bf578887f121efcad6faac35285c67f48bc -->

# M631 protocol-14 edge packets behavior map

The official Beta 1.7.3 dedicated server receives Packet130 text for a standing sign while the
same player holds first-map item `358:0`. A one-block post-sign move makes the official map update
after that sign. The play stream then yields the sign observation before a bounded Packet131
envelope. The adapter records packet order while preserving the existing sign tracker, proving
that the two variable payloads do not desynchronize the stream.

A second synchronized client deliberately sends no play traffic. Its read-only probe receives no
Packet0 before the server socket's 30-second read timeout. The stream closes at EOF after
`SocketTimeoutException: Read timed out`, and the official log records `disconnect.genericReason`.
Reading server packets never acknowledges them or emits Packet10.

Frozen signal:

```text
order=0x82>0x83,sign=packet130,map=packet131:358:0,payload=bounded,keepalive=not-emitted,timeout=socket-read-timeout,clients=2,disconnect=clean
```

This boundary does not claim Packet131 pixel contents or colors, sign-editor GUI behavior,
rendering, a keep-alive nonce, Packet255 on timeout, or any packet format after protocol 14.
FRONT-07 owns deterministic map contents by seed and position.

Frozen trace SHA-256: `222d8c1c1f4e3a309412c8ee0a503bf578887f121efcad6faac35285c67f48bc`.
