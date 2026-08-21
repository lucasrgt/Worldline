# M118 redstone iron door

Status: GO in Worldline v1.106.0.

M118 composes the selected-item and empty-hand interaction boundaries with an
official powered consumer. Item 330 is used on the stabilized column to create
an iron door: bottom block `71:0`, upper block `71:8`. A side lever stabilizes
at `69:1` before treatment.

One Packet15 lever activation produces `69:9`, door bottom `71:4`, and door top
`71:12` after ten bounded signal ticks. Packet53 exposes all three transitions;
after clean disconnect/save, a fresh Packet51 reproduces them. Exactly those
three complete-chunk states differ. The ordered delta SHA-256 is
`25ac35cc392872a6b74071fed09a2a9e647b4c9a7b2896477af9c94719441191`.

M118 proves one lever-powered iron-door opening. It does not claim generic
powered consumers, closing, manual iron-door interaction, wooden doors,
double doors, indirect-power topology, distance attenuation, update order,
tick-exact latency, collision/pathfinding, rendering or a Worldline redstone
implementation.
