# M72 qualification cycle

`AeroServerContentCycle` verifies the pinned Aero origin/revision and clean
checkout, rebuilds the mapped Aero JAR, and statically rejects client/Aero/LWJGL
imports from the common/server content closure.

For each of two scenarios it creates separate detached server and client
worktrees plus fresh external game directories. The server loader must list 40
mods including Worldline content and excluding Aero. The real client loader must
list 46 mods including the same content and Aero 3.0.0.

The cycle then requires, in one scenario:

1. named StationAPI login and play readiness;
2. one server placement marker with identifier, raw ID, coordinates, and nonce;
3. one matching client message, application, and Aero renderer-return marker;
4. twenty subsequent frames and a file-backed Aero row with positive visible
   chunks, `atRestRenders`, and `atRestListCalls`;
5. normal client exit, lost-connection observation, `save-all`, server stop, and
   successful Gradle exits;
6. clean worktrees and an unchanged pinned checkout.

Run two uses a different server-only nonce. Both runs reproduce semantic SHA-256
`6dff186ed904bdce57466038dd32a9824888d6de7ddb1a20041663cb8cec0501`.

Generated JARs, worktrees, worlds, options, logs, and evidence stay under ignored
local paths. No official or derived Minecraft runtime artifact enters the public
tree.
