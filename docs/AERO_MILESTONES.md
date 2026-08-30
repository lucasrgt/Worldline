# Aero Milestone Namespace

Worldline uses the canonical `AERO-M<number>` identifier for milestones whose
primary subject is AeroModelLib behavior, performance, rendering, or runtime
qualification. General Worldline milestones retain the unprefixed `M<number>`
namespace.

## Existing milestone aliases

The following completed milestones are canonically classified as Aero
milestones:

- `AERO-M10` through `AERO-M19` alias legacy Worldline milestones `M10`
  through `M19`.
- `AERO-M68` aliases legacy Worldline milestone `M68`.
- `AERO-M70` through `AERO-M110` alias legacy Worldline milestones `M70`
  through `M110`.

This inventory contains 52 Aero milestones. `M69` is deliberately excluded:
it qualifies the vanilla held-item swing and Packet18 observation boundary and
does not depend on Aero behavior.

Legacy filenames, smoke directory names, release-property keys, version tags,
and frozen evidence retain their original `M<number>` spelling. They are
immutable compatibility identifiers, not a competing namespace. New prose and
new planning references should use `AERO-M<number>` and may add the legacy ID
in parentheses when necessary.

## Ownership boundary

AeroModelLib owns production optimization implementations and canonical
`aero.*` optimization records. Worldline owns the differential experiments,
behavioral oracles, runtime evidence, and promotion gates that qualify them.
An Aero milestone passing in Worldline does not by itself enable an opt-in
optimization or promote an Aero release.

## Current investigation wave

The causal-performance wave began at `AERO-M111`:

- `AERO-M111` (`M768`) is complete: the canonical historical stacked-machine
  tower replay is specified in
  [AERO_M111_HISTORICAL_TOWER_REPLAY.md](AERO_M111_HISTORICAL_TOWER_REPLAY.md)
  and qualified by Worldline.
- `AERO-M112` (`M769`) is complete: it qualified the unified frame, JFR, GC,
  safepoint, allocation, and I/O timeline.
- `AERO-M113` (`M770`) is the current GPU, driver, `Display.update`, and
  present-attribution qualification.
- `AERO-M114`: counterbalanced paired statistics and hitch-rate gate.
- `AERO-M115`: incremental non-forced autosave candidate and drain invariant.
- `AERO-M116`: visible, age, and debt-aware chunk-work scheduling candidate.
- `AERO-M117`: real-consumer Aero CPU-path adoption and differential.
- `AERO-M118`: page rebuild, invalidation, allocation, and cache A/B matrix.
- `AERO-M119`: GL state, geometry, display-list, and LOD A/B matrix.
- `AERO-M120`: long-soak, reload, world-transition, leak, and promotion gate.

Entries after `AERO-M113` remain planned milestones, not completed evidence or
performance claims.
