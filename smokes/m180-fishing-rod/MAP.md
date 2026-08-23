<!-- worldline-map-schema=1 -->
<!-- boundary=m180-fishing-rod -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=9eafaf3ce4f443aa63f94304c269f66ab3a4257921e9d595d17a208ef9c8554a -->

# M180 behavior map

The fixture raises an isolated stone column. Using fishing rod item `346`
in-air (Packet15 direction 255) while looking forward emits official
Packet23 type `90`. Two headless peers observe the same entity identity,
type, thrower, and quantized pose. Catch RNG is not hashed.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone|cause=packet15-dir255-rod346|wire=packet23-type90+thrower0|oracle=two-peer-identical-fishhook-object|column=17,support=4:71:4:1:0,hook=type90+shared-id+thrower0+fixed138:2512:144,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`9eafaf3ce4f443aa63f94304c269f66ab3a4257921e9d595d17a208ef9c8554a`.
