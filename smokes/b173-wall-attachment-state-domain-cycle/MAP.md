<!-- worldline-map-schema=1 -->
<!-- boundary=b173-wall-attachment-state-domain-cycle -->
<!-- nonclaims=activation,support-loss,sign-text,redstone,render -->
<!-- frozen-trace=3bb3be6ba44a1870233a2bc6f381bd7c7c16c2ac557cbcd0984d53497380ccbc -->

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
`provider=b1.7.3-server-state-domain,family=wall-attachment,rows=3,passed=3,states=13,reload=FRESH_LOGINx3,evidence=510f1332238056df7cbf74ded0428cb9789961d82a54bf069b8c1fe9ea7c61c5,isolation=3-fresh-worlds`.
