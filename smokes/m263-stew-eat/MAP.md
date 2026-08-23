<!-- worldline-map-schema=1 -->
<!-- boundary=m263-stew-eat -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=94038e1a1f75ad42e97730c63d6089ab182511bd6f5889d8a1610d83e5471bc9 -->

# M263 behavior map

Official mushroom stew item `282` is eaten with Packet15 air-use (direction
`255` at `-1,255,-1`). Vanilla Beta 1.7.3 has no hunger bar; stew is
`ItemFood` that heals eight health points, capped at 20, and leaves bowl
`281` in the held slot. The freeze is Packet8 `12 -> 20` plus held
`282:1 -> 281:1`.

This is not bread, cookie, or cooked pork. Those foods consume the stack
to empty. Stew is `ItemSoup` and always leaves bowl `281`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=stew282|cause=packet15-dir255-item282|wire=packet8-health12->20+packet103-stew-bowl281|oracle=itemfood-stew-heal8+bowl-leftover|health=12->20,heal=8,stew=282:1->281:1,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`94038e1a1f75ad42e97730c63d6089ab182511bd6f5889d8a1610d83e5471bc9`.
