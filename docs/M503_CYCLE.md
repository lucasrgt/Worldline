# M503 qualification cycle

M503 drives the seeded passive-AI path of a target-free pig through mapped and
official Beta 1.7.3 worlds. The 240-tick fixture freezes the same world and pig
random streams and compares the complete canonical movement trace.

The expected signature is
`17747e296bd5c3d985ae22b817598da213acc55c7d078016447b2f0869ed1d28`.
Qualify it with `java tools/harness/Gate.java --milestone m503-sw-pig-wander`.
