# M304 behavior map

A wooden hoe tills official dirt into farmland `60:0`. The actor then
jumps and falls onto that cell until the official server converts it
back to dirt `3:0`. The frozen signal includes both `3->60` and `60->3`.
That exact dirt cell survives a clean save plus fresh login. This is
the M307 till plus M308 trample compound, distinct from M156 hydration.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-dirt3|cause=packet15-wooden-hoe290+packet13-jump-fall|wire=packet53-farmland60+dirt3|oracle=live-till-3->60+live-trample-60->3+fresh-login-dirt3:0|column=17,support=4:71:4:1:0,cell=4:72:4:3:0,hoe=290,till=3->60,trample=60->3,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`ce698c2302ea621590b03877774a82c7ea0a5b085bf5536d28093462ed8c121c`.
