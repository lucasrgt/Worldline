# M37 Route Correction Policy

Two fresh protocol-14 sessions submit a three-step route with explicit
`STOP_ON_CORRECTION`. The first `+0.125 X` step is unchallenged. The second
targets a cache-selected solid terrain cube and must be corrected. The route
must return immediately with two outcomes, one correction, and no retry; the
third `+0.125 X` step is never sent.

The original cached chunk remains loaded. Official player NBT after clean
disconnect/save must equal the first step's pose, proving the absent third step
did not change server state.
