<!-- worldline-map-schema=1 -->
<!-- boundary=sky-brightness-cycle -->
<!-- nonclaims=renderer-color,weather-attenuation,nether-lighting,terrain-skylight -->
<!-- frozen-trace=a9f7037ea337b6a85f2564061420b3295ef1b8b3cd1c07978f460663cd037fb0 -->

# M654 sky brightness cycle behavior map

## Boundary

The fixture creates a clear Overworld with a non-null in-memory `WorldInfo` and a chunk loader
that rejects all terrain access. At thirteen canonical world times it recomputes and reads the
server's skylight-subtraction field. The equatable evidence retains every time/value pair, so a
brightness-only or time-only observation cannot satisfy the contract.

The frozen sequence is:

```text
time                 0  6000  12000  12500  13000  13500  14000  18000  22000  22500  23000  23500  23999
skylight-subtracted  0  0     0      3      6      9      11     11     11     9      6      3      0
```

## Mapping anchors

- `World` maps to client `fd` and server `dj`.
- `func_32005_b(long)` maps to `b(long)` and shifts world time without loading terrain.
- `calculateSkylightSubtracted(float)` maps to `a(float)`; `calculateInitialSkylight()` maps to
  server `g()`, and `skylightSubtracted` is field `f`.
- `getWorldTime()` maps to server `m()`.

## Oracle independence

The mapped subject compiles against RetroMCP server classes and publishes immutable API/TestKit
evidence. The official oracle uses only obfuscated symbols and compiles directly against the
hash-verified Beta 1.7.3 server JAR. They share literals and `CanonicalTrace`, not implementations.

Two mapped processes and two official processes must be deterministic and byte-identical. This
boundary does not claim client renderer color, rain or thunder attenuation, Nether lighting, or
terrain skylight propagation.

Frozen signal:

```text
weather=clear,times=0:6000:12000:12500:13000:13500:14000:18000:22000:22500:23000:23500:23999,skylight-subtracted=0:0:0:3:6:9:11:11:11:9:6:3:0,replicas=4,oracle=mapped-official-match
```

Frozen semantic SHA-256:
`a9f7037ea337b6a85f2564061420b3295ef1b8b3cd1c07978f460663cd037fb0`.
