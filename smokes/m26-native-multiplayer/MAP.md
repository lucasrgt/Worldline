# M26 Native Multiplayer Render Bridge

Two fresh client processes each boot a fresh official Beta 1.7.3 server,
complete protocol-14 login and pose synchronization, then draw a fixed frame
through mapped Minecraft `Tessellator`, native LWJGL, and an offscreen OpenGL
Pbuffer. The connected session selects the foreground color.

The gate requires no onscreen `Display`, the renderer provenance under the
mapped client workspace, exact pixel coverage, the M10-qualified frame hash,
and clean client/server shutdown. M10 already proves this mapped Tessellator
path matches the official obfuscated renderer.
