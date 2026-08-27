<!-- worldline-map-schema=1 -->
<!-- boundary=b173-wall-attachment-state-domain-cycle -->
<!-- nonclaims=activation,support-loss,sign-text,redstone,render -->
<!-- frozen-trace=678f9ba8811950597452d5249f27ad36e5684e77d1544e47e1efc1801162be1e -->

# Beta 1.7.3 wall-attachment state domains

Three public TestKit rows exercise torch `50`, ladder `65`, and wall sign `68` against the
unmodified official server. Torch covers all four wall faces plus the floor; ladder and wall sign
cover all four wall faces. Each row owns a fresh world and proves its final attachment grid again
after a fresh login.

The package closes 13 reachable metadata states through 13 causal Packet15 placements. Sign item
`323` producing wall-sign block `68` is part of the public scenario loadout contract. This is one
support-face subsystem, not one milestone per item or face.

This map does not claim activation, support-loss behavior, sign text, redstone transitions,
collision, lighting propagation, or client rendering.

Frozen signal:
`provider=b1.7.3-server-state-domain,family=wall-attachment,rows=3,passed=3,states=13,reload=FRESH_LOGINx3,evidence=3374ce92b504c7287ff648f62a5b2fa6d0b20f85869578a6e3fd1807b7c688fa,isolation=3-fresh-worlds`.
