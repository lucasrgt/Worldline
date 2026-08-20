# M441 behavior map

Official cookie `357` and mushroom stew `282` are eaten as one Packet15
air-use rest family not covered by M374. Packet15 direction 255 eats each
stack: Packet8 restores `19 -> 20` (cookie heal 1) and `12 -> 20` (stew
heal 8), Packet103 consumes cookie to empty, and stew leaves bowl `281`.
Beta 1.7.3 has no hunger bar; food heals health. Golden apple `322` is
already hashed in M374 and is not repeated here.

This map does not re-qualify M374 remaining-food-eat, M327 food crafts, or
cake eat (M160 / M335 / M369). Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=cookie357+stew282|cause=packet15-dir255-item357+packet15-dir255-item282|wire=packet8-health19->20+12->20+packet103-empty-357+bowl-281|oracle=itemfood-cookie-heal1+stew-heal8+bowl-leftover+fresh-login|cookie=357:1:0->empty,health=19->20,heal=1,stew=282:1:0->281:1:0,health=12->20,heal=8,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`a742d0481ec2e053071b64ffb13a565582bd3dbbc76859b4d650f2a8b74ac5b7`.
