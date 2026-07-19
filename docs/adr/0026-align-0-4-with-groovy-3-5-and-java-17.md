# Align 0.4 with Groovy 3–5 and Java 17

KlumCast aligns its `0.4.0` compatibility baseline with the other Klum libraries on Groovy 3 through 5 and Java 17.
Issue #13 delivers that migration and blocks the `0.4.0` release, consolidating the support change with the breaking 0.4
migration rather than imposing another baseline change after release. The currently documented Groovy 2.4 and Java 11
support ends with the `0.3.x` line.

KlumCast compiles one Java-17 production artifact set against Groovy 3 and verifies that same artifact set with isolated
Groovy 3, 4, and 5 test and consumer lanes. Groovy/Spock tests and fixtures are recompiled in each lane; only explicit
version-neutral Java helpers and resources may be shared.

`klum-cast-annotations` remains Groovy-free. Although `klum-cast-spi` exposes Groovy AST types, its publication metadata
must not force a Groovy version or group. Custom-check and compiler-plugin consumers select their matching compiler
dependency explicitly: `org.codehaus.groovy:groovy` / module `org.codehaus.groovy` for Groovy 3, and
`org.apache.groovy:groovy` / module `org.apache.groovy` for Groovy 4 and 5. Compiler activation remains owned by
`klum-cast-compile`.

Boolean-composition development in issue #22, including draft pull request #33, remains independent and may merge before
the baseline migration. Its durable composition contracts do not depend on Groovy 2; issue #13 removes any temporary
Groovy-2-specific test lanes and documentation before `0.4.0` is released.

KlumAST issue #455, its ADR 0011, and merged pull request #497 accepted this handoff. Issue #13 implements it without
depending on KlumAST issue #496 or selecting a shared convention-plugin extraction design.
