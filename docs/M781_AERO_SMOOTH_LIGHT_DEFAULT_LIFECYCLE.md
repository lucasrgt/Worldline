# M781-AERO-SMOOTH-LIGHT-DEFAULT-LIFECYCLE Aero smooth-light default and world lifecycle

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

An independent external Java 8 consumer proves that the absent property enables the cache, explicit false disables it, explicit true enables it, same-world claims reuse the same array, and a world identity switch clears all previous entries.

## Qualification cycle

The cycle pins an exact clean Aero revision and hashes the production cache source and external probe, compiles both with javac release 8, then executes six fresh counterbalanced JVMs across default, false, and true startup policy arms. Every arm checks exact output, same-world array identity, and two-way world-switch eviction. It does not launch Minecraft or claim GPU performance.

Expected signal: `consumer=aero-model-lib,revision=06c0c22ce15454b45b14597332a92241fef0931e,compile=javac-release8,jvms=6-fresh-counterbalanced,default=enabled,explicit-false=disabled,explicit-true=enabled,same-world=array-reuse,world-switch=entries1-to0+old-world-miss,oracle=exact`.

Frozen semantic SHA-256: `8ebf6d22267e4e6fd22f47dc275a5993b15d9b7c7188ea3cf69dd35ec6feef4d`.
