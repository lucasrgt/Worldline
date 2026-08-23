<!-- worldline-map-schema=1 -->
<!-- boundary=m176-sign -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=02572936a90d996c81d67465e5507ff4f5ecb33262d10fd9cdb0e2cbe28489ff -->

# M176 behavior map

Packet15 places sign item `323` on a raised stone column as standing sign
`63:4` from actor look yaw `-90`. Official Packet130 then writes four ASCII
lines. The same tile text is read back after a clean save plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+item323-block63|cause=packet15-item323+packet130-ascii|wire=packet53-sign63:4+packet130-persist|oracle=fresh-login-packet130|column=17,support=4:71:4:1:0,sign=4:72:4:63:4,text=World/line/M176/ok,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`02572936a90d996c81d67465e5507ff4f5ecb33262d10fd9cdb0e2cbe28489ff`.
