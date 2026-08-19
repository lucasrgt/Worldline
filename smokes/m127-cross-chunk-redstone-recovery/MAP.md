# M127 behavior map

M127 rebuilds M126's seam fixture and qualifies the powered state through a
fresh client. A second Packet15 activation switches lever `69:9` back to
`69:1`; the attached wire across the seam falls from `55:15` to `55:0`.

Fresh complete Packet51 snapshots have exactly one inverse delta per chunk and
zero residual states against the original off snapshots.

Frozen semantic SHA-256:
`269f3a7043dc7c483f160233c36890ef075faf03e36300801aa5779f06b05aa2`.
