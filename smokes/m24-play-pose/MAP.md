# M24 Multiplayer Play Pose

Two fresh protocol-14 clients consume the official server's spawn/time
prelude, acknowledge its initial position packet, and send a deliberate look
packet. After clean disconnect and save, original Worldline NBT code observes
the requested yaw and pitch in each official player file.

The gate freezes the packet path and requested rotation, while world-dependent
spawn coordinates remain observational. The official server JAR and generated
world/player data remain ignored.
