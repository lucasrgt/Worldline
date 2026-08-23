# M595 powered rail brake set behavior map

One official session builds a raised north-south track: a stone wall north of
launch powered-rail `27`, a second powered-rail `27` one cell south, detector
`28` beyond that, and a stone bumper. Packet15 of minecart item `328` on the
still unpowered launch rail emits Packet23 type `10` at the launch-rail
center. A bounded live hold keeps beyond detector `28:0`. Packet15 of floor
torch item `76` writes both rails `27:8` and starts the cart. Packet14 then
breaks the torch; both rails return to `27:0` and the unpowered powered-rails
brake the moving cart. Beyond detector `28:0` stays idle. Those exact cells
survive a clean save plus fresh login.

This map does not re-qualify M184 unpowered powered-rail place-only or M377
powered-rail acceleration onto a detector. Headless `B173WireClient`
protocol-14 only. No GUI. No Aero.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+wall+launch27+mid27+beyond28+bumper+torch76+minecart328|cause=packet15-item27+packet15-item28+packet15-minecart328+packet15-item76+break-torch76|wire=packet23-type10+thrower0+packet53-rail27:0->8->0+packet53-beyond28:0+packet53-torch76:5->0|oracle=powered-launch-then-unpowered-brake-stop+fresh-login|column=17,support=4:71:4:1:0,wall=4:72:3:1:0,bumper=4:72:7:1:0,launch=4:72:4:27:0->8->0,mid=4:72:5:27:0->8->0,beyond=4:72:6:28:0,cart=type10+thrower0+fixed144:2331:144,moved=1,braked=1,torch=5:72:4:76:5->0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`9af61c7c0e4b8e165e1a7d94f70410c5bff7e66102a757de60a02f08077ffa38`.
