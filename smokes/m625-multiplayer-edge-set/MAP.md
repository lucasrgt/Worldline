<!-- worldline-map-schema=1 -->
<!-- boundary=behavior-evidence -->
<!-- nonclaims=duplicate-login,global-packet-order,combat,inventory-mutation -->
<!-- frozen-trace=d63e6e3d624950298e04542285ce3e9639d5d15be3b3d0866b35a9549f98e878 -->

# M625 multiplayer edge set behavior map

## Boundary

This set boots the official Beta 1.7.3 dedicated server twice. On the first
boot, Alpha then Beta connect; Alpha moves and disconnects while Beta remains.
The server must persist Alpha's pose, accept a new Alpha session with a fresh
entity ID, and expose the surviving/reconnected presence order as Beta then
Alpha. Both sessions disconnect before a clean save.

On the second boot, Beta then Alpha connect. The server list must retain that
reverse connection order and Alpha must resume the pose persisted before the
restart. Beta disconnects first without terminating Alpha; Alpha then closes.

## Pass condition

Two fresh official cycles must emit byte-identical semantic traces. Each list
transition is polled within five seconds. The same-user reconnect must receive
a different entity ID, and the persisted X/Y/Z pose must agree within 0.01
blocks after both disconnect and restart.

The set does not claim simultaneous login races, duplicate-name rejection,
global packet order, timeout behavior, combat, or inventory mutation.

## Frozen semantic signal

`orders=Alpha>Beta+Beta>Alpha,disconnect=isolated,reconnect=same-user-new-entity,persistence=disconnect+restart,clients=5,servers=2`
