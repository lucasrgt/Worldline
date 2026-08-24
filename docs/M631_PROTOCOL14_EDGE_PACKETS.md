# M631-PROTOCOL14-EDGE-PACKETS protocol14 edge packets

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M631 freezes three protocol-14 edge boundaries from the official Beta 1.7.3 dedicated server. A Packet130 sign update is decoded before a subsequent Packet131 map-data envelope on the same play stream without losing framing. A separate synchronized client that withholds all later play traffic receives no Packet0, then reaches the official 30-second socket read timeout, closes its stream, and is logged as disconnect.genericReason. This does not claim map pixels, map colors, sign GUI behavior, rendering, a keep-alive nonce, Packet255 on timeout, or post-Beta packet formats.

## Qualification cycle

DataDrivenCycle runs two fresh official dedicated-server JVMs. Each replica places and updates a standing sign, selects first-map item 358, then moves one block so the official map updates after the sign; the public B173Protocol14Access boundary preserves Packet130-before-Packet131 ordering. It then opens a second synchronized client, reads server packets without replying, proves that zero Packet0 frames precede EOF, and correlates the official SocketTimeoutException plus disconnect.genericReason record. Protocol14EdgeFixture normalizes payload length and wall-clock variation into equatable framing and timeout evidence. Headless B173WireClient only. No GUI. No Aero.

Expected signal: `order=0x82>0x83,sign=packet130,map=packet131:358:0,payload=bounded,keepalive=not-emitted,timeout=socket-read-timeout,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `222d8c1c1f4e3a309412c8ee0a503bf578887f121efcad6faac35285c67f48bc`.
