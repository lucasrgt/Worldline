# M246 qualification cycle

`SpruceLogCycle` rebuilds the raised stone fixture in two fresh official
server JVMs. Each run places wood/log item `17` (damage `1`) on the top
face, freezes live `17:1`, and reloads that cell after save plus a
fresh login. One EOF from the official process sleeps five seconds and
retries once on a sibling workspace.

The frozen semantic SHA-256 is
`da7cf603b820a91005a39a8dcd6ce70f9779f145f26a8ffc835f7ad93a077693`.

Canonical evidence uses two official server JVMs and four client sessions.
