# M501 qualification cycle

M501 compares the mapped and official Beta 1.7.3 `EntityItem` update paths in
four fresh JVMs. A falling item and a supported control freeze gravity, ground
collision, vertical stopping, and age progression for 30 controlled ticks.

The expected signature is
`d5e39c681248baa95e697c21d1db30d004ed3e6e090fa5dd8feb4fc0b6e34e8c`.
Qualify it with `java tools/harness/Gate.java --milestone
m501-sw-entity-item-grounding`.
