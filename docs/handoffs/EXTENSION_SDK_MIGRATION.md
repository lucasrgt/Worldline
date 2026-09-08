# Extension SDK migration handoff

## Goal

Refactor the consumer's extension registrations to the current Worldline SDK while
preserving its real-game proof behavior, stable contract IDs, observations, and
evidence attachments. Use the Worldline `main` commit that introduces
`worldline.extension.ExtensionContracts` and the native TestKit progress HUD.

Rebuild or refresh the consumer's Worldline API and runner artifacts from that
checkout. Previously downloaded TestKit artifacts do not gain the new API when
the source checkout is updated. `java tools/testkit/TestKitPackage.java` packages
the compiled checkout after its canonical gate has passed; its ignored output is
`.worldline/dist/testkit`. Check the checkout's `release/testkit-artifacts.lock`
and the package's `checksums.properties` when updating local dependencies.

## Composition

Keep one manifest and one `WorldlineExtension` entrypoint for the mod. The
entrypoint is a composition root, not a Java `main` method. Split registration
groups by machine or subsystem, and keep execution code in the consumer's proof
classes. No annotation scanning or additional SDK interfaces are needed.

```text
proof/IndustrialModExtension.java
proof/SharedFixtures.java
proof/machines/refinery/RefineryContracts.java
proof/machines/refinery/RefineryProof.java
proof/machines/crusher/CrusherContracts.java
proof/machines/crusher/CrusherProof.java
```

```java
public final class IndustrialModExtension implements WorldlineExtension {
    @Override public void register(WorldlineExtensionRegistry registry) {
        SharedFixtures.register(registry);
        RefineryContracts.register(registry);
        CrusherContracts.register(registry);
    }
}
```

Point `worldline/extensions/<existing-extension-id>/manifest.properties` at the
new composition class. Keep the extension ID and capability declarations stable.
Only add the crusher registration when that machine actually has proof code.

## Refinery migration

Register `fresh-game` once in `SharedFixtures`, using the current qualified-runtime
precondition and setup. A fixture name alone does not reset a game. Then move the
refinery declarations into a group:

```java
import worldline.extension.ExtensionContracts;
import worldline.extension.ExtensionSubject;
import worldline.extension.ExtensionSubjectKind;
import worldline.extension.WorldlineExtensionRegistry;

public final class RefineryContracts {
    private RefineryContracts() {}

    public static void register(WorldlineExtensionRegistry registry) {
        registry.subject(ExtensionSubject.of("refinery:machine",
                ExtensionSubjectKind.SUBSYSTEM, "5x3x3 refinery"));
        ExtensionContracts.custom("real-machine-pipeline", "refinery:machine")
                .fixture("fresh-game")
                .action("exercise-machine", RefineryProof::exercise)
                .expect("pipeline", RefineryProof::status, "PASS")
                .registerInto(registry);
    }
}
```

`RefineryProof.exercise(ExtensionContext)` keeps the existing bounded polling,
native-formation assertion, runtime evidence files, input log, screenshots, and
attachments. `RefineryProof.status(ExtensionContext)` returns the existing
`runtime.properties` status. Both callbacks may throw checked exceptions; the
action returns `void`, and the observation returns `String`.

The shortcut selects `CUSTOM_CONTRACT`, builds the expected equatable signature,
and registers an equatable oracle under the contract's own ID. Remove redundant
manual oracle and signature registration for migrated contracts. Other existing
contracts may still use their old shared oracle. Conformance, differential, and
custom-oracle contracts continue to use the explicit builder and registry.

## ID and reuse rules

- Register each subject once; several contracts may refer to it.
- Provider IDs are extension-wide, even when Java classes are split into groups.
- Inline overloads register a provider. ID-only overloads reference one already
  registered: `.fixture(id)`, `.action(id)`, `.expect(observationId, expectedValue)`.
- Do not repeat inline registration for a shared provider. Duplicate IDs remain
  errors; the shortcut does not replace or silently deduplicate registrations.
- Keep existing IDs such as `real-machine-pipeline` and `pipeline` during the
  initial migration. Use descriptive machine-specific IDs for new providers.
- Actions run in declaration order. Registration does not execute callbacks.

## Native progress HUD

Enable `--overlay` on the existing `worldline test run` command, set `overlay=true`
in `worldline-test.properties`, or use `new RunnerOptions().overlay(true)`.
Re-prepare ModLoader/Forge TestKit clients so their manifest advertises
`progress.protocol=worldline.test-progress.v1`; the API/runner update alone does
not install the native probe into an older prepared client.

The HUD is transparent, uses the native F3 font and shadow, and always shows its
diagnostic rows independently of F3. It reports suite, test, fixture/action/
observation/oracle step, status, counts, attempt, elapsed time, timeout, seed,
runtime, and ticks. `PASSED` is green and `FAILED` is red. `Ticks` counts calls
through `TestContext.tick` or `ExtensionContext.tick`, not total world time.

Keep polling through `context.tick()`. Extension fixture, action, observation,
and oracle phases appear automatically. Unsupported graphical sessions retain
the default no-op; this change does not add a StationAPI HUD implementation.

## Consumer acceptance

1. Compile against the updated public SDK and discover the extension by manifest.
2. Load two machine groups together and verify no duplicate subjects or providers.
3. Execute the existing refinery proof on its qualified real runtime, preserving
   native formation, the pipeline result, failure handling, and all attachments.
4. Verify that the observed evidence and declared expected values still match.
   Update any registry/Atlas snapshot that explicitly includes the oracle ID.
5. Run with the overlay enabled and confirm readable phase labels and increasing
   context ticks; retain the consumer's normal reports as the result authority.

See [Extension SDK](../EXTENSION_SDK.md) for the full public API examples and
[legacy driver preparation](../../adapters/modloader-forge/README.md) for the
qualified ModLoader/Forge runtime setup.
