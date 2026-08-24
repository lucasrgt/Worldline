# M624-WEATHER-STATE-MATRIX Weather state matrix

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

This milestone freezes deterministic Beta 1.7.3 rain and thunder transitions, countdown reseeding, strength ramps, and WorldInfo persistence.

## Qualification cycle

M624 compares five mapped and official weather cases: dry-to-rain, rain-to-dry, calm-to-thunder, thunder-to-calm, and a simultaneous storm transition. Each case advances three weather updates, records countdown and strength state, and round-trips the final WorldInfo through NBT. Two mapped and two official processes must produce the same canonical trace.

Expected signal: `official oracle: MATCH`.

Frozen semantic SHA-256: `b96fb0662d743ed48b06c1346b4f18210823c33353aeed64b310a19db86892ad`.
