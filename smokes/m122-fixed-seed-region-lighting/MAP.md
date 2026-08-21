# M122 behavior map

M122 repeats M121's exact official-server lifecycle for chunks `(-1,-1)`
through `(1,1)`: load all nine chunks, advance 200 player heartbeats, cleanly
save, restart, and receive each complete chunk through a fresh Packet51 reader.

The block-light and sky-light planes are traversed independently in fixed
chunk/x/z/y order. Each plane contributes 294,912 exact nibble samples and a
sixteen-bin histogram. Unlike M121's interior block-state diagnostics, no
normalization is applied to either lighting plane.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|region=3x3-absolute-chunks--1:1:-1:1|settle=200ticks+clean-restart+fresh-packet51|samples=294912|block=a1689237fdf271a9435c247807d60013026adbb0b608818a5084dd7756359da4|blockHist=0:294373;1:0;2:0;3:0;4:0;5:0;6:1;7:5;8:5;9:8;10:7;11:8;12:45;13:121;14:126;15:213|sky=74598f4128934f383ccb34511866e94e96ae2979355fed3b37b34633b3ffa85e|skyHist=0:138719;1:0;2:0;3:2050;4:0;5:0;6:2150;7:0;8:0;9:2233;10:0;11:0;12:2304;13:0;14:0;15:147456|decode=packet51-nibbles-xzy|disconnect=clean
```

SHA-256: `55f946b28a62caf43a7b02b027f13747f5662e315fbf0c8e70f9cca77a189192`.
