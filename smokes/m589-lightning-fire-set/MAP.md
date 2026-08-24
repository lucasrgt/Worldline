<!-- worldline-map-schema=1 -->
<!-- boundary=lightning-fire -->
<!-- nonclaims=weather-scheduler,packet71,adjacent-random-fires,entity-damage -->
<!-- frozen-trace=b099beb97a56923bfc2c3f421ad099ad804b35c075c08e0421415f639896b0d1 -->

# M589 lightning fire behavior map

Official server symbols:

- `c` is `EntityLightningBolt`; its constructor accepts `dj` (`World`)
  plus three coordinates.
- With `dj.q >= 2`, loaded chunks, an air strike cell, and valid fire support,
  the constructor writes `na.as` (`Block.fire`) at the strike coordinate.
- The mapped lane performs the same operation through
  `EntityLightningBolt`, `World.difficultySetting`, and `Block.fire`.

The fixture contrasts normal difficulty 2 with easy difficulty 1 on identical
stone floors. It records the center cell before construction, after
construction, and after two entity ticks. Only the center cell, difficulty,
lightning age/death state, and entity count enter the canonical trace.
Randomized neighboring fires are deliberately excluded.

This boundary does not claim natural thunder scheduling, Packet71 transport,
neighbor fire placement, struck-entity damage or conversion, rain, sound, or
client rendering.

Frozen signal:
`oracle=MATCH,fixture=m589-lightning-fire-set,ticks=2,controlled=true`.

Frozen semantic SHA-256:
`b099beb97a56923bfc2c3f421ad099ad804b35c075c08e0421415f639896b0d1`.
