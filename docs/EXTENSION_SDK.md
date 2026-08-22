# Extension SDK

Worldline distinguishes runtime **drivers** from mod **extensions**. A driver
implements the kernel backend for a game runtime. An extension binds overlay
sites to catalog roles. Mods adapt to Worldline; Worldline does not grow an
in-tree adapter per mod.

## Ownership

Worldline owns drivers. The closed driver set is `b173-client` and
`b173-server`. StationAPI is the next driver slot: the same game on the Yarn
and Fabric Loader classpath. It is not implemented yet and is not an
extension.

A mod owns its extension manifest in its own repository under the Worldline
namespace, not a generic `extensions/` folder:

```text
worldline/extensions/<id>/manifest.properties
```

Worldline may pin one overlay extension (`aero-model-lib`) under `adapters/`
to run oracled smokes. That pin is not a template for other mods. Worldline
experiments may cite an external extension; they must not copy the mod's types
into `SemanticCatalog.standard()`.

## Manifest schema

```properties
schema=worldline.adapter.semantics.v1
adapter=example-mod
kind=extension
owner.prefix=worldline/example/
site.1=worldline/example/Probe#onTick
role.1=CLIENT_TICK_ROOT
subject.1=net/minecraft/client/Minecraft.runTick
```

`kind` is `driver` or `extension`. Only the closed driver names may use
`kind=driver`. `owner.prefix` must be a `worldline/` package. Subjects must
already exist in the catalog. `aero/` and `/aero/modellib` fail closed.
Unknown roles fail closed.

A ready-to-copy record is in `worldline/extensions/TEMPLATE.properties`.

## Validation

The canonical Worldline gate checks the in-tree allowlists in
`harness.properties` and binds every site to the catalog.

The same kind checker can validate another repository without importing that
project into Worldline:

```text
java path/to/worldline/tools/harness/AdapterKindCheck.java path/to/project
```

Catalog binding uses the Worldline CLI from the repository that owns the
manifests, or against an explicit repository root:

```text
worldline semantics adapter
worldline semantics adapter check path/to/project
```

The checker rejects missing kinds, driver names outside the closed set, extra
in-tree manifests, and allowlist drift. An empty `worldline/extensions`
directory is valid in a project that has no Worldline extension.
