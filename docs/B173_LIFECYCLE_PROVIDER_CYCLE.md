# Official Beta 1.7.3 lifecycle provider cycle

`b173-lifecycle-provider-cycle` is one subsystem-sized TestKit acceptance package, not three
milestone-count atoms. It publishes an optional provider JAR, a Java SPI entry, a fixed-seed
official-server arena, a public scenario catalog, and canonical evidence for three routing layers.

The provider requires the caller-owned official Beta 1.7.3 server JAR through
`-Dworldline.b173.lifecycle.serverJar=...`. It checks the exact 503100-byte artifact and SHA-256
`033a127e4a25a60b038f15369c89305a3d53752242a1cff11ae964954e79ba4d`; neither the JAR nor a world
save is distributed. Every test creates a new diagnostic workspace under the runner's configured
world directory and closes its protocol client and server even when the test fails.

The cycle runs cobblestone, dirt, and empty chest. For each row it proves the support precondition,
air target, one-item placement consumption, placed state, fresh-login persistence, tool damage
`0 -> 1`, break to air, normalized exact drop, and post-break fresh-login persistence. It then
repeats the complete three-server run and requires identical canonical artifacts and signature.
The frozen official-cycle signature is
`a2541a94cd70363267c953f38abf479551440ddab60bc3e3b33c90cdec828046`.

This is deliberately the first bounded public matrix. The provider does not receive the current
scenario in `TestRuntimeRequest`, so its arena provisions all supported rows in advance. Expanding
to a large registry matrix should first add scenario-aware setup data rather than silently growing
an opaque inventory convention.
