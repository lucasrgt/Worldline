<!-- worldline-map-schema=1 -->
<!-- boundary=b173-static-light-transport-cycle -->
<!-- nonclaims=random-tick-melting,dynamic-redstone-state,global-sky-brightness,native-render -->
<!-- frozen-trace=ddade0dec6c4591634c260476e5a2ca0241685affa5d49fbea85b5ed5a7712c6 -->

# Beta 1.7.3 static light transport

Seven public TestKit rows compare exact light planes before and after official gameplay
placement. Glass, leaves, and ice distinguish skylight transparency and attenuation. Torch,
glowstone, lit redstone torch, and jack-o-lantern distinguish source emission levels and
one-step-per-cell horizontal block-light decay.

Each row owns a fresh world, begins with the same three open-air cells, proves exact inventory
consumption and placed state, saves, disconnects cleanly, then reconnects before reading a full
Packet51 block-light and sky-light plane. The package therefore joins seven Functional Census
atoms into one reusable light-transport mini-subsystem rather than counting one milestone per
block or sample.

This map does not claim random-tick melting, redstone-driven source transitions, the global
time-of-day brightness curve, renderer shading, or modded light engines.

Frozen signal:
`provider=b1.7.3-server-light,family=static-transport,rows=7,passed=7,probes=15,reload=FRESH_LOGINx7,evidence=eaa8a83ff7582430369b4c3e664982c73053e6945d5eb9bb0014487eeefe38a7,isolation=7-fresh-worlds`.
