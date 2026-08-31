<!-- worldline-map-schema=1 -->
<!-- boundary=public-extensions-sdk -->
<!-- nonclaims=legacy-loader-runtime-qualification-remains-separate -->
<!-- frozen-trace=e478ceed869683756efdf01295073a02fdc445bf198db636524d8bf2abff2f55 -->

# M785 public Extensions SDK mapping

M785 is a structural TestKit capability. It does not introduce or reinterpret an
official Minecraft mapping. The external fixture compiles with only the public
`worldline.api`, `worldline.extension`, `worldline.test`, and
`worldline.testkit` packages on its authoring classpath.

The manifest binds `example.sdk-fixture` to a public `WorldlineExtension`
entrypoint. Capability negotiation happens before entrypoint construction. The
entrypoint registers four neutral subjects, provider IDs, three executable
contracts, one loader adapter descriptor, and no Minecraft or adapter type.

The exact tooling observation is:

`extensions=1,subjects=4,contracts=3,modes=conformance+differential+custom-contract,tests=5,atlas-pages=8,imports=public-only`

Its SHA-256 is
`e478ceed869683756efdf01295073a02fdc445bf198db636524d8bf2abff2f55`.

The cycle proves public discovery, registration, execution, evidence, and Atlas
projection. It does not qualify a new legacy-loader runtime or claim that every
third-party mod can run without a separately qualified runtime adapter.
