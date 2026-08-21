# M147 behavior map

The accepted arm places twelve alternating stone/cobblestone blocks above an
upward piston. Activation changes all thirteen chain cells, extends the piston
and yields fifteen exact raised changes. The rejected arm adds one more payload
block: activation powers the lever but changes zero chain cells and leaves the
piston retracted.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=up-piston33+alternating-stone1-cobble4+lever69|settle=200+10ticks|cause=packet15-lever-activate|effect=official-piston-limit-12-accept-13-reject|observation=fresh-login-packet51|capacity{blocks=12,column=10,lever=1->9,piston=1->9,chain=13:ebeb71fd765c54cb14321320250bb980dc1c8613f63fd5cc69a1bfa0b4eb7860,raised=15:a7436c91bba2fb27cd892bf6d4a4791297adfbeeab2e82810d50dde7e25f3bea},overlimit{blocks=13,column=10,lever=1->9,piston=1->1,chain=0:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855,raised=1:3506bb3866a86782ddacfae92e7468ec72d1874777e768650eb4be95b8810c85}|disconnect=clean
```

Frozen semantic SHA-256:
`6fd354f14bc191c11fd670b0d58e6aa0b86072feec3bb2322261cef951ca1a54`.
