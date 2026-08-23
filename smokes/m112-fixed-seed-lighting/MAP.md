<!-- worldline-map-schema=1 -->
<!-- boundary=m112-fixed-seed-lighting -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=f5180dc49e6d6117c501e903ab16b1015a071cedf027e2444168a40109dc0969 -->

# M112 behavior map

M112 reuses M111's seed, absolute chunk `(0,0)`, official server artifact and
two-fresh-world design. A minimal pre-login player NBT anchors the loaded view
at the target without editing blocks. The smoke reads the two
4-bit light planes already present in the full Packet51 payload.

Every block-light and sky-light sample is hashed in X/Z/Y order. Exact
histograms over values 0–15 independently prove the decoded ranges and counts.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|target=absolute-chunk|origin=0,0|samples=32768|block=bc449b312209d70eb9ca4403aea691f640e3d4cccc1211e6dd10d073e393ad76|blockHist=0:32702;1:0;2:0;3:0;4:0;5:0;6:0;7:0;8:0;9:0;10:0;11:0;12:4;13:22;14:16;15:24|sky=ea9305667c4d0bbaf0e94c527d3aee028a42bd7dc5266bfdd744165e85f73663|skyHist=0:15360;1:0;2:0;3:256;4:0;5:0;6:256;7:0;8:0;9:256;10:0;11:0;12:256;13:0;14:0;15:16384|decode=packet51-nibbles-xzy|disconnect=clean
```

SHA-256: `f5180dc49e6d6117c501e903ab16b1015a071cedf027e2444168a40109dc0969`.

M112 does not attribute sources, mutate blocks, step a light update, prove
cross-chunk propagation, day/night brightness, rendering, persistence,
alternate seeds or dimensions.
