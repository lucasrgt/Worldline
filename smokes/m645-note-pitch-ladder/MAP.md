<!-- worldline-map-schema=1 -->
<!-- boundary=note-pitch-ladder -->
<!-- nonclaims=audio-playback,client-rendering,dig-start-pitch-advance,instruments-beyond-stone,seeds-beyond-17320110707 -->
<!-- frozen-trace=f6dcb484fa9a8f017e4a462f0eaea706a5542a601d4536ea481da72f7e2e862d -->

# M645 semantic map

Evidence: official-server dual replica (two fresh workspaces).

The public boundary is `worldline.testkit.NotePitchFixture#cycle`. It validates one
complete official tuning cycle on one note block: Packet54 pitches one through
twenty-four in ascending order, the wrap back to pitch zero, and one confirmation
click replaying pitch one. The instrument stays constant across every event, and
the single click observed after a save-stop-restart reload must replay pitch two,
which distinguishes a persisted pitch-one tile from a fresh pitch-zero note.
Evidence is equatable over clicks, wrap pitch, retained pitch, and instrument;
coordinates are deliberately excluded.

The official tuning action is empty-hand block activation (Packet15 use) against
note block 25: the server increments the tile pitch modulo twenty-five and then
plays the new pitch. Whether dig-start input advances pitch is explicitly outside
this milestone's bounded claims. Pinned executed evidence from m166
(`click=packet54:1:1` on a fresh stone-supported note) matches increment-then-play
activation semantics; this milestone generalizes that boundary across the whole
ladder plus wrap.

Each replica seeds a player at 4.5:60:4.5 with stone and one note block, raises a
stone column above any water from the chunk-0,0 dirt foundation used by m313 and
m166 on seed 17320110707, places the note block, performs all 26 activations
awaiting each Packet54, verifies the cell remains `25:0`, saves, stops the server
JVM, restarts a fresh server on the same world, logs in again, and activates once
more. TileEntityNote persistence is the only mechanism that can explain the
retained pitch-two observation.

Frozen signal:
`seed=17320110707,clicks=26,ladder=pitches1-24-wrap0,instrument=1,retained=2,persisted=true,replicas=2,disconnect=clean`.

Frozen semantic SHA-256: `f6dcb484fa9a8f017e4a462f0eaea706a5542a601d4536ea481da72f7e2e862d`.
