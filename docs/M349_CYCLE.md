# M349 qualification cycle

`DoubleChestSetCycle` rebuilds the raised two-block stone pad in two
fresh official server JVMs. Each run places two adjacent chest items
`54`, opens Packet100 with 54 owned slots titled `Large chest`, and
reloads both chest cells plus the 54-slot window. The frozen signal
includes the 54-slot window and both block cells. Headless
`B173WireClient` is the only client. There is no GUI and no Aero path.
One official EOF is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/DoubleChestSetCycle.java m349-double-chest-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`ec079803ad133072d794b370d1dd5988e5931287cded14a33e3abd7702c0fd26`.
