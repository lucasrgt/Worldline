# M361 ladder climb set

M361 composes official ladder placement with official client physics.
`LadderClimbSetCycle` proves two east-facing `65:5` cells are server-authored
and survive a save plus fresh login. The controlled client cycle drives
`B173PhysicsProbe.ladder` through the mapped
`EntityPlayerSP.moveEntityWithHeading` root and compares the result with the
exact official obfuscated root in two additional JVMs.

No Packet13 climb is manually injected. The server scenario does not calculate
vertical motion, and the client probe does not substitute a Worldline physics
equation. Qualification requires both frozen traces and an exact
mapped/official client match.

The replacement server signature and shared client-physics signature remain
pending until the exclusive official-runtime cycle completes. No GUI. No Aero.
