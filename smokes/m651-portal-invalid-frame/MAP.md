<!-- worldline-map-schema=1 -->
<!-- boundary=portal-invalid-frame -->
<!-- nonclaims=horizontal-frames,other-frame-sizes,frame-damage,fire-lifetime,portal-traversal -->
<!-- frozen-trace=11d594bcba998d11c06db03da5649a516c04d97349a2c6e38a1fd46e4824a96a -->

# M651 portal invalid frame behavior map

The official Beta 1.7.3 server receives Packet15 placements for thirteen obsidian `49:0`
blocks in an upright four-by-five frame around six interior air cells. The second inner cell of
the top edge, `6:69:4`, deliberately remains air. Packet15 flint and steel `259` then creates
observable fire `51` at the lower interior cell, causally proving that ignition reached this
incomplete fixture.

No portal block `90` exists in any of the six interior cells after a telemetered twenty-tick
observation. After clean disconnect, save, and fresh login, all thirteen obsidian cells persist,
the required top-edge gap remains air, and the interior still contains zero portal cells.
Qualified M132 and M382 cycles separately prove that the complete fourteen-block control frame
does activate, so this boundary isolates the required gap rather than redefining activation.

`worldline.testkit.PortalInvalidFrameFixture#reject` compares the placed frame, missing cell,
causal fire observation, and live and persisted portal counts as equatable evidence. This
boundary does not claim horizontal frames, other sizes, frame damage, fire lifetime, or travel.

Frozen signal:

```text
column=10,frame=4:65:4-7:69:4,obsidian=13,missing=6:69:4:0:0,interior=6,fire=observed,portal=0,persistedPortal=0,flint=259,dimension=0,clients=2,disconnect=clean
```

Frozen trace SHA-256: `11d594bcba998d11c06db03da5649a516c04d97349a2c6e38a1fd46e4824a96a`.
