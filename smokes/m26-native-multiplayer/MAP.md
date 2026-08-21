# M26 Native Multiplayer Render Bridge

Two fresh client processes each boot a fresh official Beta 1.7.3 server,
complete protocol-14 login and pose synchronization, then draw a fixed frame
through mapped Minecraft `Tessellator`, native LWJGL, and an offscreen OpenGL
Pbuffer. The connected session selects the foreground color.

The gate requires no onscreen `Display`, the renderer provenance under the
mapped client workspace, exact pixel coverage, the M10-qualified frame hash,
and clean client/server shutdown. M10 already proves this mapped Tessellator
path matches the official obfuscated renderer.

Frozen expected signature SHA-256: `c2d85227a2cb542e0c9b21aa77dd71a0bbfaab7162a1db6c0fb0955876dbb2ce`
