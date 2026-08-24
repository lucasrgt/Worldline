# M630-SERVER-ACL-MATRIX server acl matrix

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M630 freezes the Beta 1.7.3 dedicated-server ACL matrix across a regular player, operator, deoperator, console kick, console ban, and console pardon. A regular player's time command is denied; console op grants that command and deop removes it again. Kick emits an administrative disconnect but permits immediate reconnection. Ban disconnects and rejects the same identity at login until console pardon restores access. This does not claim IP bans, whitelist policy, remote console, arbitrary commands, kick reasons, filesystem editing, or online-mode authentication.

## Qualification cycle

DataDrivenCycle runs two fresh official dedicated-server JVMs with three pre-seeded identities. Each replica synchronizes denied versus issued player commands through the official server log and confirmed save state, synchronizes kick and ban through Packet255 disconnects and the server player list, probes the banned login handshake, then proves pardon by a successful fresh login. ServerAclFixture normalizes log prefixes and tick drift into an equatable role/session matrix. Headless B173WireClient only. No GUI. No Aero.

Expected signal: `regular=time-denied,op=time-allowed,deop=time-denied,kick=disconnect+reconnect,ban=disconnect+relogin-denied,pardon=relogin-allowed,identities=3,disconnect=clean`.

Frozen semantic SHA-256: `ba3d94b3028cbf3f33a2bdaf53b0342caa477cc629484fc4527fde8cec15d106`.
