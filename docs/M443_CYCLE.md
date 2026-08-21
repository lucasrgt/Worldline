# M443 qualification cycle

`RemainingBucketRestSetCycle` rebuilds the paired raised stone trenches in
two fresh official server JVMs. Each run seeds still water `9` and still
lava `11`, opens the adjacent dirt gates onto air, waits for official
horizontal flow, then uses empty bucket `325` Packet15 direction-255 on
each flowing cell and each source. Flowing water `8/9` and flowing lava
`10/11` must reject; source `9:0` and `11:0` must fill to `326` and
`327`. The empty sources plus both filled buckets reload after save plus
fresh login. One official EOF is retried after a 5 second sleep. Headless
`B173WireClient` is the only client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/RemainingBucketRestSetCycle.java m443-remaining-bucket-rest-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`b556b71fd57896aa06fbb39f5088d8f96e6c8a64076014c7d7391b961c669eb7`.
