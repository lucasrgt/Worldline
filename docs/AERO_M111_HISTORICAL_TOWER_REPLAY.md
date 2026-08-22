# AERO-M111 Historical Tower Hitch Replay

Status: planned; runtime evidence not yet acquired.

## Purpose

AERO-M111 turns the historical stacked-machine tower into a canonical,
complete-census hitch replay. The reported issue occurred across worlds while
testing AeroModelLib near vertically stacked machine towers separated by solid
cobblestone floors. The milestone therefore does not require one private save
to define the scene.

## Canonical scene

The replay uses AeroModelLib's existing MEGA fixture with all scene-defining
properties explicit:

- `aero.mega=true`;
- `aero.mega.solidTerrain=true`;
- sixteen floors;
- four machine families per floor;
- 3 by 3 vertical-stack groups;
- 576 Aero block entities per chunk;
- native non-forced world saves enabled;
- frame pacing disabled and the vanilla FPS limit set to zero;
- keyframe side effects disabled in the first attribution pass;
- deterministic per-position animation phase spreading enabled.

The sparse default MEGA terrain is not an acceptable substitute. The solid
cobblestone floors are part of the historical trigger because they exercise
terrain compilation, terrain rendering, driver submission, and Aero block
entities together.

## Acquisition protocol

Each arm starts in a fresh client process and a restored copy of the same
generated world. It records every complete frame and divides the run into:

1. world entry and initial chunk compilation;
2. a post-load stationary drain window;
3. a bounded look, jump, and spin route beside the towers;
4. a post-route stationary window with native 40-tick autosaves still active.

The retained post-load observation must last at least ten minutes. Initial
chunk work must be reported separately and may not be used to explain later
periodic hitches.

The primary arm uses Aero source defaults and the default supported JVM. Save
suppression, alternate collectors, C1-only compilation, chunk budgets, frame
pacing, render governors, and high-memory presets are isolation treatments,
not baseline settings. They must be tested one at a time in later arms.

## Required timeline

Every retained frame carries one frame sequence and monotonic time shared by:

- complete frame duration;
- client tick and world-save duration;
- dirty and written chunk counts;
- chunk compile calls, maximum compile duration, and remaining backlog;
- terrain render duration;
- entity render and Aero preparation, enqueue, flush, and rebuild durations;
- `Display.update` duration;
- allocation, heap, GC count, and GC duration deltas;
- Aero page, display-list, batch, animation, and visibility counters.

The artifact stores the complete census. Hitch thresholds are applied only
after sealing so selective logging cannot hide the event distribution.

## Controls

The first differential set is:

1. solid cobblestone tower with Aero machines and native saves;
2. the same solid tower without Aero machine placement;
3. the same Aero tower with non-forced saves suppressed for isolation only;
4. the same Aero tower with sparse terrain.

Order is counterbalanced across fresh processes. No optimization treatments
are combined in this milestone.

## Completion gates

AERO-M111 is complete only when:

- scene identity and exact configuration are machine-checked;
- two fresh counterbalanced sets complete normally;
- each arm retains at least ten post-load minutes without selective rows;
- the camera/input route and restored starting world match within each set;
- initial backlog drains before the post-load classification window;
- every worst post-load frame is classified as save, GC/runtime, chunk
  compile/render, Aero logical work, display/present wait, mixed, or unknown;
- cleanup leaves no client, server, Gradle daemon, registered worktree, or
  official-runtime lock owner behind.

## Non-claims

This milestone reproduces and classifies the historical scene. It does not
claim that every hitch is caused by Aero rendering, that one optimization is
effective, that absolute timings generalize across machines, or that an
opt-in runtime policy is ready for promotion.
