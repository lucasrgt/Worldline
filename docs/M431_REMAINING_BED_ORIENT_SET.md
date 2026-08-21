# M431 remaining bed orient set

M431 qualifies the official Beta 1.7.3 dedicated-server remaining bed
orientation family as one compound SET. Packet15 of bed item `355` on a
raised stone platform with look yaw `90`, `180`, and `-90` places block
`26` as west foot/head `26:1`/`26:9`, north `26:2`/`26:10`, and east
`26:3`/`26:11`. The frozen signal names Overworld dimension `0` and those
six halves. All six cells survive a clean save plus fresh login.

This family is distinct from shipping M240 (one south `26:0`/`26:8`
place), M330 Overworld sleep occupy (`26:8→26:12` Packet17), and M359
Nether bed explode (Packet60 strength `5`, Packet17 absent). It does not
claim occupied-head metadata `26:12`, spawn-point persistence, rain
Packet70, or Nether ignition. Headless `B173WireClient` protocol-14 only.
No GUI. No Aero.

Frozen semantic SHA-256:
`8aa709e05da8be4a281e9eded3c6297e0f4236a515d73d60178570c69cf303a1`.
