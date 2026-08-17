# M26 Native Multiplayer Render Bridge

M26 combines the official-server multiplayer boundary with the M10-qualified
native renderer in one client process. Each fresh scenario boots an unmodified
Beta 1.7.3 server, performs protocol-14 login and pose synchronization, and
uses connected session state to select a native offscreen frame.

The frame travels through mapped Minecraft `Tessellator`, LWJGL, an OpenGL
Pbuffer, and RGBA readback. The gate requires `Display.isCreated() == false`,
mapped-client provenance, exact quad coverage, and the same frame SHA-256 that
M10 independently proved equal between mapped and official obfuscated
Tessellator implementations.

This keeps protocol, process, and rendering adapters separate while proving
they compose without an onscreen window.

## Non-claims

M26 does not instantiate the complete `Minecraft` gameplay loop, interpret
chunk payloads into a world renderer, drive an interactive GUI, claim
graphical multiplayer parity, or externally control server ticks.
