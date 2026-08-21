# M359 behavior map

Official server symbols:

- `net.minecraft.src.WorldProviderHell.canSleepHere` returns false. Overworld
  `WorldProvider.canSleepHere` returns true. That is the dimension gate.
- `net.minecraft.src.ItemBed.onItemUse` still places block `26` on face `1`
  (UP) in the Nether. Yaw `0` writes foot metadata `0` and head metadata `8`
  one cell south, the same halves M330 occupies in the Overworld.
- `net.minecraft.src.BlockBed.blockActivated` walks to the head, then explodes
  when `canSleepHere` is false: the head is cleared, the cursor walks one more
  cell, and `World.newExplosion(null, x+0.5, y+0.5, z+0.5, 5F, true)` emits
  protocol-14 Packet60 at strength `5` with flaming. Packet17 sleep is absent.
- TNT (M137) is Packet60 at strength `4` from primed entity `46`. This SET is
  bed-caused strength `5` in dimension `-1`.

The frozen signal names both poles: Overworld `0` sleeps (M330 Packet17),
Nether `-1` explodes (this Packet60). This map does not re-run M330 sleep,
does not claim TNT blast rays, fire persistence, player death, or End beds.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|profile=allow-nether-true|entry=prelogin-player-nbt-dimension-minus-one+item355|fixture=nether-netherrack87+item355-block26|cause=packet15-item355-place+empty-hand-use|wire=packet60-strength5-flaming+packet17-absent|oracle=nether-explode-not-overworld-sleep|overworld=0:sleep,nether=-1:explode,dimension=-1,support=8:8:1,foot=8:9:1:26:0->gone,head=8:9:2:26:8->gone,packet17=absent,packet60=strength5,destroyed=positive+bed,persisted=bed-absent,clients=3,disconnect=clean
```

Frozen semantic SHA-256:
`be77b379de881712f9089340681a1a0779977df7934e51508858f83c97a9a7a6`.
