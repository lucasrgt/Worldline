<!-- worldline-map-schema=1 -->
<!-- boundary=player-pose -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=e43923f84231be276ae24a78a94f1d50aef3d5229dc59f10bcc5fd83c7cbc0db -->

# M24 Multiplayer Play Pose

Two fresh protocol-14 clients consume the official server's spawn/time
prelude, acknowledge its initial position packet, and send a deliberate look
packet. After clean disconnect and save, original Worldline NBT code observes
the requested yaw and pitch in each official player file.

The gate freezes the packet path and requested rotation, while world-dependent
spawn coordinates remain observational. The official server JAR and generated
world/player data remain ignored.

Frozen expected signature SHA-256: `e43923f84231be276ae24a78a94f1d50aef3d5229dc59f10bcc5fd83c7cbc0db`
