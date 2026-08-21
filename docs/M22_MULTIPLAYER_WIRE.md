# M22 Multiplayer Wire Harness

M22 adds the first real client/server multiplayer path. `MultiplayerSession`
and immutable `MultiplayerState` describe a connection without game-specific
packet types. `MultiplayerServerRuntime` extends the M21 server boundary with
connected-player observation.

The b1.7.3 wire adapter implements only the minimum native protocol-14 flow:

1. open a localhost TCP socket;
2. send packet `0x02` with the username;
3. require the offline `-` handshake response;
4. send packet `0x01` with protocol 14;
5. parse the official login response and entity ID;
6. remain connected while the official server's `list` command reports the
   username;
7. close the socket and require the list to become empty.

Two fresh scenarios repeat this flow against two unmodified official servers.
Entity IDs, ports, timing, and post-login packet streams are observational.

## Non-claims

The wire client is original Worldline code, not the official graphical client.
M22 does not render, move a player, parse the full play protocol, compare packet
streams, or externally step server/client ticks. Those stay separate milestones.
