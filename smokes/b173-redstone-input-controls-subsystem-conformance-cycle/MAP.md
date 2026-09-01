<!-- worldline-map-schema=1 -->
<!-- boundary=b173-redstone-input-controls-subsystem-conformance-cycle -->
<!-- nonclaims=registry-presence,gameplay-placement,break-transition,drop-matrix,save-reload,native-render,downstream-wire-propagation -->
<!-- frozen-trace=273182979913a3ae4281a7301f39e63fe7dc822c0444df8be30fa245e34afe7b -->

# Beta 1.7.3 redstone input controls

This package treats lever `69`, stone pressure plate `70`, wooden pressure plate `72`, and stone
button `77` as one input-control subsystem instead of four unrelated counters. Each control proves
the same five Atlas dimensions: reachable metadata state, state-sensitive bounds plus collision,
light-table behavior, native timing policy, and support-loss neighbor response.

The lever toggles floor orientation `5` to powered `13` and remains latched. The wall button moves
from `1` to `9`, changes its depth, and returns to `1` on its native 20-tick update. A player powers
the stone plate while a dropped item does not; the same item powers the wooden plate. Both plates
change from one-sixteenth to one-thirty-second height while pressed and release on their native
20-tick update after the actor is independently removed.

All four native collision boxes are absent, their opacity and emission entries are zero, and
removing the exact floor or wall support transitions the control to air with one native item drop.
Registry, gameplay placement, ordinary break/drop matrices, save/reload, rendering, and downstream
wire propagation remain separate evidence families.

Frozen signal:
`family=redstone-input-controls,subjects=69+70+72+77,claims=20,states=lever-toggle+button-pulse+plate-selectivity,bounds=stateful,collision=none,light=0/0,ticks=latch+20,neighbors=support-drop,oracle=MATCH`.
