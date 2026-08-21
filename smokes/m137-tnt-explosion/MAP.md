# M137 behavior map

A dry stone column isolates TNT `46` above the generated world. Packet15 with
flint and steel `259` removes the block into a primed entity; after the bounded
fuse the official server emits protocol-14 Packet60.

The adapter parses three doubles, one strength float, a bounded count and
three signed relative bytes per destroyed cell. Beta 1.7.3 Packet60 ends there:
there are no motion floats. Every listed loaded cell becomes air in the remote
cache. The constructed support must occur in the list and remain air after a
clean save and fresh login.

Frozen semantic SHA-256:
`bb96106b407266a1f02f9e9e8097e71f5d11de9337293e8cf063277cd00f07ed`.
