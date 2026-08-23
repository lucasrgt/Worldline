<!-- worldline-map-schema=1 -->
<!-- boundary=remote-world-view -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=45179dd32117513e55cbf0698ec09e51440b3e3007188c100bcdd234257f0be4 -->

# M28 Remote Chunk Observation

Two fresh protocol-14 clients synchronize with two fresh official servers and
pump inbound packets until the first native `Packet51MapChunk`. Worldline reads
its origin, encoded dimensions, and bounded compressed payload length, consumes
the payload, and exposes only a neutral immutable observation.

The gate requires full `16 x 128 x 16` chunk dimensions and a positive payload
under four million bytes. Spawn-dependent origin and compressed size remain
observational and are not frozen.

Frozen expected signature SHA-256: `45179dd32117513e55cbf0698ec09e51440b3e3007188c100bcdd234257f0be4`
