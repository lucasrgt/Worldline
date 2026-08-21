# M71 Paired Aero Window

M71 adds a matched descriptive acquisition layer above M70. It changes no
public API or protocol adapter. Each fresh arm reconstructs the same official
server, two-wire-player combat fixture, and real graphical StationAPI/Aero
observer.

## Contract

Four matched pairs execute in balanced `control/event` and `event/control`
order. Both arms wait for at least 300 renderer completions and five seconds of
warmup, then the attacker sends one exact chat message. The real client applies
the resulting Packet3 broadcast at handler TAIL; this common network event is
the measurement anchor.

The control arm sends no combat packets. The event arm immediately sends the
published M69 Packet18 request followed by the published M66 Packet7 attack.
The real client requires Packet18 attacker/animation1 before Packet38
victim/status2 and before the first measured renderer completion. The control
arm fails if either controlled observation occurs.

Each arm then completes at least 480 renderer frames and eight seconds. Aero is
pinned to threshold 25 ms, heartbeat 200 ms, asynchronous file writes, and a
fixed two-gigabyte heap. The runner parses only FrameSpike, GC, and Pulse lines
printed between the Packet3 anchor and completion marker, then requires those
same lines to occur in order in the file flushed during normal shutdown.
WorldFlush is excluded because its `frameMs` is flush duration rather than an
inter-frame interval.

## Descriptive output and non-claims

Each arm reports selected-row median, nearest-rank p95 and maximum frame time,
compile-chunk timing and calls, GC-bearing rows, and visible-chunk range. Each
pair reports the observed event-minus-control deltas. Numeric observations,
their signs, and row counts above the minimum remain dynamic and do not affect
promotion.

The four pairs are repetition and order balancing, not an inferential sample.
M71 publishes no p-value, confidence interval, regression threshold, causal
effect, spike attribution, performance improvement, FPS claim, complete frame
census, pixel oracle, or cross-machine generalization. Logger stdout and file
I/O perturb both arms. The Aero test content remains disabled, so this is not a
reproduction of server-synchronized Aero model content or the historical lag
mechanism.
