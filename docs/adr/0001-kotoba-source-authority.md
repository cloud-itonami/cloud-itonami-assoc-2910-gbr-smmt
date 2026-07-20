# ADR 0001: Kotoba is the SMMT catalog source authority

- Status: Accepted
- Date: 2026-07-21

## Decision

`src/association_facts.kotoba` is the sole production source. Both citations
retain every scalar field. The founding date remains the verified full date
`1902-07-22`, while the first exhibition remains year-only `1903`. Topic count
plus indexed access preserves the distinct governance and commerce topics.
Unknown values and indexes return zero or typed option-none; no effects are
declared.

CI executes reference semantics, restricted JavaScript, instantiated typed
WebAssembly, and production source-authority checks. Clojure and the JVM are
compiler/test hosts only.

## Consequences

- Source date precision is preserved instead of inventing month/day values.
- The two distinct topics remain queryable without host sets.
