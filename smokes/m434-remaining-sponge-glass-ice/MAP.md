<!-- worldline-map-schema=1 -->
<!-- boundary=m434-remaining-sponge-glass-ice -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=0716150d188414cd60d0bebe7aa70f27ace8a376a47f6e0a912fc026e8ab63b5 -->

# M434 behavior map

One official session places sponge item `19`, glass item `20`, and ice
item `79` on one raised stone fixture. Packet15 writes Overworld sponge
`19:0` on the support top, glass `20:0` on the west pad, and ice `79:0`
on the east pad. No torch is present, so ice stays ice. All three cells
survive a clean save plus fresh login.

This map does not re-qualify the shipping sponge-only (M206), glass-only
(M196), ice-only (M193), fragile ice-and-glass break (M308), or
torch-lit ice-and-snow melt (M386) traces. Headless `B173WireClient`
protocol-14 only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+sponge19+glass20+ice79|cause=packet15-item19+packet15-item20+packet15-item79|wire=packet53-sponge19:0+packet53-glass20:0+packet53-ice79:0|oracle=live-transparent-odd-solid-place19+20+79+fresh-login|column=17,support=4:71:4:1:0,sponge=4:72:4:19:0,west=3:71:4:1:0,glass=3:72:4:20:0,east=5:71:4:1:0,ice=5:72:4:79:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`0716150d188414cd60d0bebe7aa70f27ace8a376a47f6e0a912fc026e8ab63b5`.
