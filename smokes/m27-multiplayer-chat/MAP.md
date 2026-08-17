# M27 Two-Client Multiplayer Chat

Each of two fresh scenarios boots one official server and connects two
protocol-14 clients simultaneously. Both synchronize their play poses, the
server lists exactly both usernames, `WorldlineA` sends `worldline-m27`, and
`WorldlineB` must receive exactly `<WorldlineA> worldline-m27`.

The receiver uses the bounded inbound packet reader, so queued time, chunk,
inventory, entity, and metadata payloads are consumed by their official
lengths before chat. Unknown packet IDs and invalid payload sizes fail closed.
