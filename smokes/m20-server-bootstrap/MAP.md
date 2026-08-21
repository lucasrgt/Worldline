# M20 Official Server Boundary

The smoke starts the hash-pinned, unmodified Minecraft Beta 1.7.3 dedicated
server JAR twice in fresh ignored workspaces. Each process binds only to
localhost on a temporary port, disables online authentication and mob spawning,
uses the fixed Worldline seed, reaches the native `Done` marker, accepts the
native `stop` command, saves, and exits cleanly.

The JAR and generated worlds remain under ignored roots. No server class,
resource, generated world, or decompiled source is committed. M20 proves only
artifact identity and lifecycle control; it does not yet connect a client,
instrument a server tick, or claim multiplayer determinism.

Frozen expected signature SHA-256: `7d1edb19b978300465878cfade247ec0db7db37b9a5fbcfd9a595566bfb06b60`
