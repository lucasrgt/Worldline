<!-- worldline-map-schema=1 -->
<!-- boundary=server-acl-matrix -->
<!-- nonclaims=ip-bans,whitelist,remote-console,arbitrary-commands,kick-reasons,filesystem-editing,online-mode-authentication -->
<!-- frozen-trace=ba3d94b3028cbf3f33a2bdaf53b0342caa477cc629484fc4527fde8cec15d106 -->

# M630 server ACL matrix behavior map

The official Beta 1.7.3 dedicated server starts with three fixed player identities. The actor
first sends a restricted time command as a regular player and the server records it as a tried
command without changing the distant target time. Console `op` grants the same identity
permission; the server records the next request as an issued command and a save confirms the
time change. Console `deop` then removes permission, and a second distant target is rejected
while the accepted time remains within bounded tick drift.

The other two identities distinguish session controls. Console `kick` emits Packet255 and the
same identity can immediately reconnect. Console `ban` emits Packet255 and a new handshake for
that identity is rejected as banned. Console `pardon` permits a fresh login again. Player-list
observations bound every disconnect and reconnect.

Frozen signal:

```text
regular=time-denied,op=time-allowed,deop=time-denied,kick=disconnect+reconnect,ban=disconnect+relogin-denied,pardon=relogin-allowed,identities=3,disconnect=clean
```

This boundary does not claim IP bans, whitelist behavior, remote console, arbitrary operator
commands, custom kick reasons, direct ACL-file editing, or online-mode authentication.

Frozen trace SHA-256: `ba3d94b3028cbf3f33a2bdaf53b0342caa477cc629484fc4527fde8cec15d106`.
