# M405 behavior map

The fixture raises an isolated `7×7` grass platform and places one default
mob spawner `52:0`. Packet24 type `90` is a pig. Packet7 button 0 while
holding saddle item `329` consumes that stack. Empty-hand Packet7 button 0
then mounts the saddled pig and Packet39 attach is decoded on the existing
Packet23 object tracker.

This map does not re-qualify M149 pig death or M150 pork `319`. It does not
claim wheat breeding, pork drops, dismount, or persistence of the saddle
across restart.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+default-spawner52|cause=packet7-button0-saddle329+empty-hand-packet7-mount|wire=packet24-type90+packet103-saddle-consume+packet39-attach|oracle=pig-type90-saddle329-mount|column=17,platform=7x7-48grass,spawner=52:0,mob=type90,saddle=329+consumed,mount=packet7-button0+packet39-attach,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`a27d2ce0c705f4fe5af56c8e35b8ec7c212956eaff46a764ce610d54f40c06d9`.
