# M523-SW behavior map

- `B173LevelDatTime` verifies that `Time` is NBT type 4, patches only that
  value in the semantic NBT tree, and reads it back as a signed `long`.
- `B173DedicatedServer.state()` reads the gzip-NBT `level.dat` `Time` tag through
  `B173LevelDat`; the tag must be NBT type 4 and is decoded as a signed `long`.
- `B173WireClient.sustainTicks` drives protocol-14 heartbeats at 50 ms intervals
  through `B173PlayChannel` and pumps `B173PlayInbound` after every heartbeat.
- A clean `save-all` and shutdown persist the server tick clock. Restarting the
  same world preserves the saved value before the next explicit save.
- The fixture enables the Nether so both official world servers advance their
  clocks. This makes the later secondary-dimension `level.dat` write canonical;
  the disabled-Nether profile's stale-canonical/advanced-`level.dat_old` save
  order remains the separately frozen M500 boundary.

The smoke creates the world normally, patches a value above signed 32-bit
range while offline, then contrasts 80 connected
heartbeats with an immediate reload, and records only bounded categories. It
does not claim cross-dimension clock synchronization, weather, spawn-cycle,
bed-skip, gamerule, or client-clock behavior.

Frozen expected signature SHA-256: 583ff279e5fecfafedd95a704a77525872d14cd939775376b47a4a116d2b30f7

## Frozen semantic signal

`persisted=signed-long,restart=preserved,profile=overworld+nether,heartbeats=80,advance=bounded,no-heartbeat=smaller,save=clean,clients=1`
