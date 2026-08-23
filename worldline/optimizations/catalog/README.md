# Optimization Catalog

Each optimization is one UTF-8 `.properties` file named after its stable ID.
The canonical schema and field meanings are documented in
`docs/OPTIMIZATION_SDK.md`.

The harness fails when a reference has no record, an annotation-tracked record
has no site, a symbol-tracked source member cannot be resolved uniquely, or a
decided record has no evidence.

This directory contains only records for implementations owned by Worldline.
External projects keep catalogs under `worldline/optimizations/catalog/` in
their own repositories. Experiments may cite those external stable IDs in
evidence without copying their records here.
