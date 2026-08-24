# Worldline Roadmap

Worldline advances only through executable, oracle-backed behavior contracts.
The official Minecraft Beta 1.7.3 client and dedicated server remain the
behavioral authorities; their binaries and original assets are never release
artifacts.

## Live status

Do not maintain milestone counts or qualification state by hand here. The
current sources are:

- [Generated repository status](generated/STATUS.md)
- [Generated milestone catalog](generated/MILESTONES.md)
- [Fable 2 sealed program](FABLE2_PROGRAM.md)
- [Engineering workflow](ENGINEERING_WORKFLOW.md)
- [Complete maintained mapping batch](SEM_M13_COMPLETE_MAPPING_BATCH.md)
- [Aero milestone namespace](AERO_MILESTONES.md)

The release line through v1.462.0 and the M570-M625 evidence train are complete
repository history, not future work. Their individual contracts remain in the
generated catalog and stable `M*_*.md` documents.

## Active direction

The next product frontier is deliberately bounded:

1. close uncovered vanilla systems with reusable TestKit behaviors rather than
   milestone-number APIs;
2. preserve portable qualification through content-addressed pins and execute
   only changed behavioral inputs;
3. preserve the complete-game maintained dual-source mapping constitution while
   the bytecode-exhaustive queue remains an optional diagnostic surface;
4. make cache, worktree, and archival operations observable, scheduled, and
   fail-closed;
5. publish a stable TestKit consumer contract after a clean external-clone
   validation.

The current frontier set covers dungeon generation, chunk unload/reload
persistence, classic minecart boosters, sound-effect packets, operator ACLs,
protocol-14 edge packets, map packet content, bed lifecycle depth, a permanent
cross-lane seed matrix, and an explicit render/particle decision.

## Promotion rule

A stage becomes official only when its contract, non-claims, public binding,
executable oracle, frozen evidence, source provenance, and canonical Gate are
committed together. Candidate checks never substitute for exact milestone or
orchestrator qualification.

## Parallel workstreams

The platform/dev-tools line remains milestone-number-free: Mod API, scenario
DSL, differential fuzzing, replay/minimization, time travel, profiling,
coverage, HTML evidence, registry census, seed Atlas, and semantic screen
export. Atlas classifies declared behavior metadata, never numeric ranges.

Aero uses `AERO-M<number>` as its canonical namespace. Legacy M10-M19, M68,
and M70-M110 names remain immutable historical aliases; new Aero work requires
an explicitly qualified performance or rendering boundary.
