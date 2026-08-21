# M518-SW primed TNT fuse lifecycle

M518-SW corrects the discarded heartbeat-based proposal by measuring the
official internal `EntityTNTPrimed.fuse` boundary directly. Fuse `80` decreases
once per entity update, remains live at `40`, reaches `0` after update 80, and
terminates at `-1` on update 81.

Two Worldline controlled-runtime processes and two official obfuscated-JAR
processes must emit the same canonical trace. An unprimed TNT block and a
mid-fuse stop are negative and mutation controls. Network heartbeats and
Packet60 arrival are deliberately not treated as entity ticks.

M518-SW does not claim exact blast geometry, chained TNT, entity damage,
persistence, or a Worldline explosion model.

Frozen semantic SHA-256:
`f6a9810837ac1a3622784617b05e6c957344be3be5493d4b261e359f18076f1d`.
