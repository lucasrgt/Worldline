# M511-SW redstone ore trigger

M511-SW freezes redstone ore's official Beta 1.7.3 activation and random-tick
reversion. A mapped click changes block 73 to glowing block 74 while an adjacent
untouched ore remains 73. With a seeded active-chunk player fixture, vanilla
random ticking eventually changes the triggered block back to 73.

The cycle runs twice through Worldline's controlled runtime and twice against
the official obfuscated server JAR. Canonical traces must match exactly. This
corrects the discarded scheduled-update premise: the trigger schedules nothing;
the glowing block is marked for random ticks.
