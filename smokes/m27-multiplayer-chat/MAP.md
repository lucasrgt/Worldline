<!-- worldline-map-schema=1 -->
<!-- boundary=multiplayer-session -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=7d264e3b365a4ab223d45cd95eb17aa90683ef123af51775defc120d7635aa12 -->

# M27 Two-Client Multiplayer Chat

Each of two fresh scenarios boots one official server and connects two
protocol-14 clients simultaneously. Both synchronize their play poses, the
server lists exactly both usernames, `WorldlineA` sends `worldline-m27`, and
`WorldlineB` must receive exactly `<WorldlineA> worldline-m27`.

The receiver uses the bounded inbound packet reader, so queued time, chunk,
inventory, entity, and metadata payloads are consumed by their official
lengths before chat. Unknown packet IDs and invalid payload sizes fail closed.

Frozen expected signature SHA-256: `7d264e3b365a4ab223d45cd95eb17aa90683ef123af51775defc120d7635aa12`
