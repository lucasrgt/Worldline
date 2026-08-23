<!-- worldline-map-schema=1 -->
<!-- boundary=redstone-piston-extend -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=b0b808a6538d69a14208d5a2d18fe38ab5784856f1d0b01b3b541b9a3943eadb -->

# Piston extend smoke map

A piston facing east is powered by a standing torch on its west face. The
backend steps `updateEntities` then `World.tick` so piston tile entities can
finish. After eight ticks the piston is extended and the official server JAR
matches. BUD, sticky retraction, and entity-driven plates remain outside.

Frozen expected signature SHA-256: b0b808a6538d69a14208d5a2d18fe38ab5784856f1d0b01b3b541b9a3943eadb
