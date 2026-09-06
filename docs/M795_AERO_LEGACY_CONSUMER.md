# M795-AERO-LEGACY-CONSUMER Aero native ModLoader and Forge consumer

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Compile every Aero core and legacy adapter source against real ModLoader and Forge SDKs as Java 8, then qualify exact rendering, display-list ownership, animation advancement, and native NBT restoration in four fresh official clients.

## Qualification cycle

The existing native-loader preparation verifies the official Beta 1.7.3 client, ModLoader archive, and Forge archive hashes before preparing isolated SDKs. The external Aero checkout must be clean at the exact pin and its public extension must be discovered. Aero's maintained native platform builder compiles all core and ModLoader sources and seals hashes of source files, Java 8 classes, and each native SDK classpath. A small original consumer mod executes twice per loader in fresh Java 8 processes. Forge must report active revision 1.0.6. Each client renders a procedural triangle into an owned 128 by 128 RGBA8/depth24 surface through the real native Tessellator. Direct draw, first display-list compilation, cached draw, post-disposal recompilation, and the zero-time animated pose must match byte for byte. Disposed list IDs must cease to exist in OpenGL. Twelve animation ticks must change the image, and a native NBTTagCompound round trip must restore identical state and rendered pixels. All seven nonempty complete images also match across all four clients, without masks or tolerances. The old generated TestKit probe is removed only from each newly prepared owned workspace before the Aero consumer is installed. Inputs use WORLDLINE_LEGACY_BASE_WORKSPACE, WORLDLINE_LEGACY_LOADER_ARTIFACTS, and WORLDLINE_JAVA8_HOME. Native SDKs, official bytes, generated sources and all raw images remain local. This contract qualifies loader compilation and exercised rendering/lifecycle APIs; it makes no legacy performance, gameplay coverage, or end-to-end FPS claim.

Expected signal: `sdk=discovered,loaders=modloader-forge,sessions=4,java=8,builds=native,rgba=exact28,caches=active,dispose=verified,animation=advanced,nbt=restored`.

Frozen semantic SHA-256: `adf87227dcf2814f4444ff433c2ebc43526f9db631c8c5d583d8ec5fa862e425`.
