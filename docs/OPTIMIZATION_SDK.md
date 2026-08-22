# Optimization Metadata SDK

Worldline defines a neutral metadata contract for performance changes. The SDK
records what changed, its status, behavioral delta, risks, rollback path,
implementation symbols, and evidence. It does not enable features, inject code,
or make a project-specific optimization part of Worldline.

## Ownership

The repository that owns an implementation owns its optimization record.
Worldline keeps only records for Worldline code under
`worldline/optimizations/catalog/`. A mod keeps its records in the same path
in its own repository. Worldline scenarios and reports may cite an external
stable ID, but they do not copy the record or define the mod's implementation
details.

For example, a renderer library owns records about its culling, batching, and
cache implementations. Worldline owns only the schema, validation mechanism,
and evidence produced while testing those records.

## Optional source references

Worldline-owned code may use the dependency-free metadata module:

```java
@OptimizationRef("worldline.scheduler.visible-first")
```

`OptimizationRef` has `SOURCE` retention. It does not enter class files, alter
bytecode, or create a runtime dependency. Projects may instead use
`tracking=symbol` and keep source untouched. Multiple IDs may be supplied when
one implementation site participates in independent changes.

## Record schema

Records use UTF-8 Java properties and live under
`worldline/optimizations/catalog/<id>.properties` in the owning repository:

```properties
schema=worldline.optimization.v1
id=example.renderer.visible-cull
summary=Skip renderer work outside the visible region.
subsystem=render.visibility
status=candidate
default.enabled=false
behavior.delta=Changes which renderer submissions are skipped.
risks=Incorrect visibility may hide content.
rollback=Disable the owning feature flag.
tracking=symbol
source.symbols=example.Renderer#isVisible
evidence=example:visibility-matrix
```

Valid statuses are `active`, `candidate`, `rejected`, `retired`, and `unknown`.
Only `active` records may default on. Every decided status requires evidence;
`unknown` may temporarily use `evidence=none`. A ready-to-copy record is in
`worldline/optimizations/TEMPLATE.properties`.

## Validation

The canonical Worldline gate validates its own catalog and source references.
The same checker can validate another repository without importing that
project into Worldline:

```text
java path/to/worldline/tools/harness/OptimizationCatalogCheck.java path/to/project
```

The checker rejects incomplete records, unsafe defaults, unknown annotation
IDs, and annotation/symbol tracking drift. An empty catalog is valid only when
the repository contains no `OptimizationRef` sites.
