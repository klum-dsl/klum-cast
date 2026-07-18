# Align 0.4 with Groovy 3–5 and Java 17

KlumCast aligns its `0.4.0` compatibility baseline with the other Klum libraries on Groovy 3 through 5 and Java 17.
Issue #13 delivers that migration and blocks the `0.4.0` release, consolidating the support change with the breaking 0.4
migration rather than imposing another baseline change after release. The currently documented Groovy 2.4 and Java 11
support remains in effect until issue #13 is delivered.

Boolean-composition development in issue #22, including draft pull request #33, remains independent and may merge before
the baseline migration. Its durable composition contracts do not depend on Groovy 2; issue #13 removes any temporary
Groovy-2-specific test lanes and documentation before `0.4.0` is released.

Issue #13 waits for KlumAST issue #455 to accept the shared Groovy 3–5 build and migration strategy before implementation.
This avoids introducing a temporary KlumCast-only Groovy 5 setup that would immediately need replacement, at the cost of
making the `0.4.0` release depend on that cross-library decision.
