<!-- worldline-map-schema=1 -->
<!-- boundary=b173-ground-flora-conformance-cycle -->
<!-- nonclaims=tall-grass-full-state-domain,fern-metadata,shears-drops,growth,bonemeal,mushrooms,saplings,sugar-cane,cactus,native-render -->
<!-- frozen-trace=30842230f6bf54ee567f079462ebc2833718be060a57571b4f22aa14c7026420 -->

# Beta 1.7.3 ground-flora conformance

Four caller-owned rows cover dandelion, rose, tall-grass, and dead-bush. Flowers and tall-grass
use dirt support; dead-bush uses sand. The package composes four maintained public TestKit
families rather than embedding an oracle in smoke code.

The state-domain layer intentionally covers only dandelion, rose, and dead-bush metadata zero.
Tall-grass is stateful, so observing one placement cannot close its full reachable domain.
All four subjects receive gameplay collision and light probes, a 240-tick stability window,
causal removal of their actual support, target-air observation, and final fresh-login persistence.

The package contains nineteen Functional Census atoms in one coherent flora subsystem across
fifteen isolated official-server worlds. It does not claim fern metadata, shears drops, growth,
bonemeal, mushrooms, saplings, sugar-cane, cactus, or native rendering.

Frozen aggregate signal:
`family=ground-flora-conformance,subjects=4,claims=19,state-subjects=3,collision=4,light=4,tick=4,neighbor=4,tick-window=240,reload=FRESH_LOGINx15,state=4c5474b8302148f24c66eb117dee1e34723bee34560a1a7640ae7913f117d57f,collision-signature=1ed214c5a9814940e9159faecdd4fb46c203549f06821e2673f82e2e6917e386,light-signature=c33ecd57cb22064f008e31485c66596b339a6ccd17183a5dfe1d647987baa31a,support=7d5cfbdca87c219229e5f394ba53e41eeafbf68789cbc5fc90c115336334beac`.

The aggregate semantic signature is
`30842230f6bf54ee567f079462ebc2833718be060a57571b4f22aa14c7026420`.

v1|server=official-b1.7.3|seed=17320110707|family=ground-flora-conformance|subjects=37,38,31,32|claims=19|layers=state-domain,collision-shape,light-behavior,tick-policy,neighbor-response|state=4c5474b8302148f24c66eb117dee1e34723bee34560a1a7640ae7913f117d57f|collision=1ed214c5a9814940e9159faecdd4fb46c203549f06821e2673f82e2e6917e386|light=c33ecd57cb22064f008e31485c66596b339a6ccd17183a5dfe1d647987baa31a|support=7d5cfbdca87c219229e5f394ba53e41eeafbf68789cbc5fc90c115336334beac|oracle=four-public-family-signatures
