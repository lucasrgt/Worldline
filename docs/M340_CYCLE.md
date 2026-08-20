# M340 qualification cycle

`RedstoneInputSetCycle` rebuilds the raised-stone fixture in two fresh
official server JVMs. Each run places lever `69` and stone button `77`,
latches the lever on then off, pulses the button through powered
metadata `77:9` and back, and reloads unpowered metadata after save plus
login. The signal must include lever `69:1->9->1` and button `77:1->9->1`.
A lever-only or button-only result matching M115, M165, or M279 fails.
One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`366f2922f527ce87c4818902a75b3e646c8a6e5946e6b84838fe3c9918f0c456`.

Run directly with:

```text
java tools/smoke/RedstoneInputSetCycle.java m340-redstone-input-set
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
