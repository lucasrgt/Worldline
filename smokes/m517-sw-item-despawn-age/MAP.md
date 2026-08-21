# M517-SW mapping

The official Beta 1.7.3 server class `ez` maps to `EntityItem`. Its `b` age field is incremented by `onUpdate`; at `age >= 6000` the inherited `Entity.setEntityDead` marks the entity and `World.updateEntities` removes it.

The four-process differential freezes age 5999 as live and present, age 6000 as dead and absent, a young live control, and player collection as the alternative terminal path. Packet29 removal remains the independently frozen wire observation in M52, so M517 makes no new packet-decoder claim.
