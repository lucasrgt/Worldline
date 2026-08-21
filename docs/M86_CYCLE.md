# M86 qualification cycle

`RepeatedMembershipRecoveryCycle` builds pinned Aero in a disposable worktree,
verifies the common/server closure contains no Aero or client imports, and
runs two fresh graphical-client/modded-server replicas with one shared plan,
seed, and root nonce.

After retained record 300, each client requests generation-one removal. Every
following operation waits at least thirty retained records after the previous
observed transition: restore generation one, remove generation two, and
restore generation two. The server and client both reject duplicate, skipped,
reordered, cross-generation, wrong-coordinate, and wrong-nonce messages.

The 60-byte sidecar stores schema, nonce, plan, and four request/event pairs.
The runner reparses it together with every M74 and M78 record. It requires two
complete `16 -> 15 -> 16` cycles, removed topology `3 pages + 1 fallback`,
restored topology `4 pages + 1 rebuild`, cache count four, and no rebuild in
any other record. Artifact sizes, EOF, hashes, camera, marker order, clean
disconnect/stop, Aero provenance, and worktrees are fail-closed.

Diagnostic mode runs one replica but explicitly cannot qualify the milestone
or write release evidence. The canonical two-replica run freezes semantic
SHA-256 `841b311c16d11cbbe669756fd0fc020c4371b650ad9c185d8ab717c7217abc44`.
