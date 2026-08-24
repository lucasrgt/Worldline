# M623-LIGHTING-ENGINE-MATRIX Lighting engine matrix

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

This milestone freezes Beta 1.7.3 generated skylight and queued block/sky-light updates across a deterministic matrix.

## Qualification cycle

M623 compares mapped and official server lighting over open, roofed, and aperture terrain. It then replaces one source through glowstone, active furnace, glowing redstone ore, active redstone torch, and air while recording attenuation. Finally it closes and reopens a skylight aperture and requires exact recovery. Two mapped and two official processes must produce the same canonical trace.

Expected signal: `official oracle: MATCH`.

Frozen semantic SHA-256: `b1bf8088d22536e61795483c655bc8e1d82eed18d0e6298e0859aa40525435fb`.
