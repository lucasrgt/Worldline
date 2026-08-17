# M36 Route Recovery

Two fresh protocol-14 sessions execute a three-step relative route. Step one is
the persisted M35 `+0.125 X` movement. Step two targets a nearby solid block
from the decoded cache and must be corrected to step one's pose. Step three
still executes and applies another `+0.125 X` from that corrected pose.

The ordered outcomes must be `UNCHALLENGED`, `CORRECTED`, `UNCHALLENGED` with
exactly one correction. The original cache chunk remains loaded, and official
player NBT after clean disconnect/save must equal the final recovered pose.
