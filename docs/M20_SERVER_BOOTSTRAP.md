# M20 Official Server Bootstrap

M20 adds the official Minecraft Beta 1.7.3 dedicated server as a second
proprietary runtime input. Mojang's current b1.7.3 version manifest exposes the
client only, so Worldline acquires a community-preserved copy of the unmodified
official server binary and freezes its byte length, SHA-1, and SHA-256.

The public repository contains only `artifacts/minecraft-b1.7.3-server.properties`.
The JAR is downloaded to ignored `local/artifacts/` with:

```text
java tools/artifacts/Acquire.java server
```

The downloader requires HTTPS, writes through a partial file, validates all
frozen identity fields, and only then moves the artifact into place.

## Executable boundary

The M20 smoke starts the unmodified dedicated server twice in separate ignored
directories. Each process binds to localhost on a temporary port, disables
online authentication and mob spawning, uses the fixed Worldline seed, reaches
the native startup marker, accepts `stop`, saves, and exits with code zero.

The normalized lifecycle is:

```text
v1|version=Beta 1.7.3|startup=done|online=false|shutdown=clean
```

## Non-claims

M20 does not decompile or instrument the server, connect a client, control a
server tick, compare world state, or prove multiplayer determinism. Those are
separate milestones built on this artifact and lifecycle boundary.
