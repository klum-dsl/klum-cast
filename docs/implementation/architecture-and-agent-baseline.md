# Architecture and agent baseline

Date: 2026-07-16

Delivery update: 2026-07-19. Issue #13 implements the accepted KlumAST #455 handoff: Java 17, one Groovy-3-compiled
production artifact set, and isolated Groovy 3/4/5 tests plus Gradle/Maven publication consumers. The evidence baseline
below describes the pre-migration repository that motivated the decisions; the confirmed contract sections and issue
handoff record the delivered 0.4 state.

This is the repository-owned evidence and decision log for the library-native architecture session requested after
KlumAST issue #450. It separates confirmed KlumCast decisions from candidate improvements and from the cross-repository
multi-Groovy investigation in KlumAST issue #455.

## Evidence baseline

- KlumCast publishes `klum-cast-annotations` and `klum-cast-compile`.
- The annotations artifact contains the declarative validation vocabulary, embedded checks, filters, and custom-check base
  API. Its public signatures expose Groovy AST types while its generated Maven POM declares no Groovy dependency.
- The compile artifact depends transitively on annotations, contains external built-in checks and validation orchestration,
  and registers a global semantic-analysis AST transformation through `META-INF/services`.
- Both artifacts contain `com.blackbuild.klum.cast.checks.impl`, creating a split package. Neither artifact declares an
  explicit JPMS descriptor or stable automatic-module name.
- The current JDK 11 build passes 56 tests under Groovy 2.4.21, 3.0.17, and 4.0.12. Compile-module coverage measured under
  Groovy 4 is 83% instructions and 74% branches; the annotations artifact has no test or coverage source set.
- Open repository issues relevant to architecture are #12 (module descriptors), #13 (Groovy 2), #16 (check context), and
  #17 (structured messages).
- KlumAST consumes both artifacts through public `api` edges, defines eight string-bound custom checks, and retains two
  validation-ownership TODOs. Those consumer facts are inputs, not KlumCast requirements.

## Confirmed decisions

### Product boundary

KlumCast is a compile-time annotation-use validation library for Groovy compilation. It owns reusable declarative
constraints, check orchestration, and source-positioned diagnostics during semantic analysis. Consumers that depend on
successful validation perform their main transformation in a later compilation phase. KlumCast does not own runtime
model validation or consumer-framework semantics.

This scope is recorded as product language in root `CONTEXT.md`; the later ADRs in this plan select the artifact,
activation, diagnostic, and extension boundaries that implement it.

### Consumer-authored checks

Consumer-authored checks are a supported core extension capability. KlumCast owns a deliberate public SPI that lets a
consumer bind declarative validation annotations to its own check implementations. ADR 0001 records this commitment while
later ADRs replace the current `checks.impl` package, mutable field injection, and annotation-stack representation with
the package, immutable-context, binding, and migration contracts below.

### Check invocation context

Checks are stateless per invocation and receive one immutable check context. ADR 0002 replaces the current mixture of
protected mutable fields and method parameters at the supported contract boundary. The context carries the validated
annotation, target, typed control annotation when present, member context, binding metadata, and composition path; exact
Java record/class syntax remains an implementation detail, while ADRs 0011 and 0014 define the interface and diagnostic
result boundaries.

### Diagnostics and technical failures

Expected constraint violations are explicit diagnostic data, and one check may emit zero or more source-positioned
diagnostics. Unexpected check, binding, and engine failures retain their causes as technical failures rather than being
converted into invalid-annotation diagnostics. ADRs 0014, 0016, and 0023 define the structured fields, override model, and
compiler presentation.

### Groovy AST API boundary

The supported check SPI intentionally exposes Groovy compiler AST types. ADR 0004 accepts Groovy AST as public API rather
than introducing a limiting wrapper abstraction. ADR 0026 applies the accepted multi-Groovy handoff: SPI metadata does not
force a version selector, and custom-check authors explicitly select the matching Groovy 3, 4, or 5 compiler dependency.

### Artifact roles

ADR 0005 separates lightweight declarative annotations/metadata, the Groovy-dependent public check SPI, and the compiler
engine with built-in implementations and activation. This replaces the current contradiction where the annotations
artifact exposes AST-dependent SPI types but omits Groovy from its published dependency metadata. Coordinates, package
names, dependency edges, and compatibility bridges are resolved by ADRs 0017 through 0021.

### Activation boundary

ADR 0006 keeps service-loaded global AST transformation as the default supported activation mechanism and confines it to
the compiler artifact. Adding that artifact to a Groovy compilation classpath is the explicit activation act; the metadata
and check-SPI artifacts remain inert. The ambient effect and scan scope must be documented. ADRs 0007 through 0009 resolve
ordering, opt-out behavior, and the absence of a public manual hook.

### Transformation timing

ADR 0007 retains `SEMANTIC_ANALYSIS` as KlumCast's validation phase. KlumCast guarantees completion relative to consumer
transformations in later phases, and consumers that depend on validation should perform their main mutation in
`CANONICALIZATION` or later. It provides no ordering guarantee relative to other semantic-analysis transformations;
same-phase annotation synthesis or mutation needs explicit future coordination.

### Activation control

ADR 0008 makes dependency placement the supported activation control. Removing the compiler artifact from a Groovy
compilation classpath disables KlumCast while leaving metadata and check-SPI artifacts usable. There is no runtime flag,
system-property switch, or source-level opt-out without a concrete tooling or embedding requirement that cannot control
its compilation classpath.

### Activation API

ADR 0009 keeps service loading as the only supported activation API. Engine orchestration remains separable enough for
internal tests, but no public manual invocation hook is promised without a concrete embedding use case and a defined
compiler lifecycle contract.

### Declarative check binding

ADR 0010 is accepted after the proof of concept. The metadata artifact owns name-based binding for lightweight and cyclic
split-module designs; the SPI artifact owns strongly typed binding for co-located and nested checks. The compiler
normalizes both, and a declaration selects one form. The existing mixed annotation's typed member may become a deprecated
raw `Class<?>` bridge for a documented migration window. No Groovy AST abstraction is introduced.

### Check SPI shape

ADR 0011 defines the durable check SPI as a minimal Java interface that receives the immutable check context and returns
diagnostics. No KlumCast base class is required. Reusable helpers remain separate, and the current mutable
`KlumCastCheck` abstract class is treated as migration surface rather than the permanent contract.

### Check construction and lifecycle

ADR 0012 requires a concrete check implementation with an accessible no-argument constructor. The engine may construct
per binding or reuse within one compilation, so checks cannot rely on identity, invocation count, or retained state.
Instances do not cross compilation runs; dependency injection and custom factories require a future concrete use case.

### Applicability filters

ADR 0013 keeps applicability distinct from check success so composition can distinguish a filtered-out branch from a
passing branch. Filters form a supported, stateless interface over immutable context; declarative target-kind filters stay
built in, and custom filters have typed and name-based binding modes under the same resolution rules as checks. Filters on
one binding remain conjunctive unless an explicit composition strategy says otherwise.

### Structured diagnostics

ADR 0014 defines an immutable diagnostic with a stable check-scoped code, rendered message, primary AST node, optional
structured arguments and related nodes, and engine-attached binding/composition provenance. Constraint diagnostics remain
compiler errors; technical failures stay outside this payload, and message overrides remain a separate decision.

### Conditional IDE boundary

ADR 0015 records no IDE roadmap commitment. If the speculative integration is pursued, it remains a separate layer over
structured KlumCast diagnostics and introduces no IDE dependencies into compiler core. Completion support may motivate a
read-only query API only after concrete scenarios exist.

### Diagnostic message overrides

ADR 0016 gives checks ownership of default messages and stable check-scoped codes. Validation annotations may declare
optional message templates keyed by code and using named structured arguments. The nearest applicable override in a
composition path wins; invalid codes or templates are technical configuration failures. Exact annotation and template
syntax remains implementation work for issue #17.

### Artifact coordinates

ADR 0017 preserves `klum-cast-annotations` for lightweight metadata and built-in validation annotations, adds
`klum-cast-spi` for Groovy-dependent extension contracts, and retains `klum-cast-compile` for the engine, implementations,
and activation. No aggregator or coordinate rename is added. Custom-check authors add SPI explicitly, with a documented
breaking migration from the current base-class API.

### Published dependency graph

ADR 0019 keeps annotations and SPI as siblings with no edge between them. SPI exposes the Groovy compiler API but publishes
no forced Groovy selector; compile depends on both siblings and normalizes both binding forms. Annotation-only consumers
remain Groovy-free, typed-check authors add SPI and matching Groovy explicitly, and adding compile owns activation. ADR
0026 records the accepted KlumAST #455 version-selection handoff.

### JPMS identity

ADR 0020 assigns stable module names `com.blackbuild.klum.cast.annotations`, `.spi`, and `.compiler`, initially through
`Automatic-Module-Name` with continued classpath support. Explicit descriptors require a Groovy 3/4/5 feasibility proof
that includes service loading and separate classpath coverage. The Groovy module-name transition remains a #455
coordination fact rather than a KlumCast variant decision.

### Release and migration line

ADR 0021 makes `0.4.x` the transitional redesign line. It introduces the new artifact and SPI contracts with deprecated
migration bridges and complete release-facing migration documentation; `0.3.x` is fixes-only. `1.0.0` removes temporary
bridges and starts the durable compatibility promise on the clean architecture.

### Post-1.0 compatibility surface

ADR 0022 makes coordinates, stable module names, exported metadata/built-in/SPI packages, public type shapes, diagnostic
codes and argument names, binding and activation behavior, dependency edges, and the documented Java/Groovy support matrix
compatibility commitments. Compiler implementation identities, default message prose, and rendering details remain
internal. Removing an in-window Groovy generation requires a major release.

### Compiler diagnostic presentation

ADR 0023 maps every structured diagnostic to one native Groovy compiler error with its stable code and primary AST-node
position. Related locations and provenance are supplementary context. Independent diagnostics are emitted in deterministic
source/binding order, while technical failures retain their cause and binding identity in a separate failure path.

### KlumAST `FieldAstValidator` ownership

`FieldAstValidator` and all of its DSL-object, keyed-model, link, default-implementation, and instantiability rules remain
strictly consumer-owned by KlumAST. Its “move logic to KlumCast” TODO refers only to possible reusable node-kind dispatch
before consumer hook methods, not ownership of those rules. This classification is intentionally not an ADR. A generic
helper remains optional until another concrete use case demonstrates the same repetition.

### KlumAST `LinkHelper` ownership

The `LinkHelper` TODO contains two KlumAST-owned changes. Its single-use static methods should move into the
`AutoLinkPhase` case class now that KlumAST has a phase model. Any remaining static `@LinkTo` annotation invariants should
be KlumAST custom checks using KlumCast's SPI; existing `@MutuallyExclusive`, `@NotOn`, and `@AlsoNeeds` declarations have
already moved much of that validation. Runtime provider, owner, selector, and value resolution remains KlumAST lifecycle
behavior. This classification creates no KlumCast API or ADR.

### Test and coverage seams

ADR 0024 assigns tests by artifact contract: metadata and Groovy-free publication in annotations; immutable SPI,
diagnostics, filters, typed binding, and Groovy compatibility in SPI; end-to-end compiler behavior in compile; and separate
packaging plus representative consumer fixtures. Per-artifact coverage remains visible, but release readiness is based on
risk scenarios including service loading, ordering, both bindings, failures, module/class paths, nested checks, and split
consumer modules rather than one aggregate percentage.

### Package ownership

ADR 0018 preserves existing declarative packages, puts durable public extension contracts under
`com.blackbuild.klum.cast.spi`, and assigns engine and implementation packages exclusively to the compiler artifact. This
eliminates the current split package. The old `checks.impl.KlumCastCheck` name may be a temporary deprecated adapter in the
SPI artifact only; it is not a permanent package contract.

## Implementation gates and deferred decisions

- #12 owns the explicit-module-descriptor feasibility POC across the supported Groovy generations; stable automatic-module
  identities are already decided.
- #22 owns detailed boolean composition semantics over the confirmed applicability, diagnostic, and technical-failure
  model.
- #23 is an optional investigation gated on a second concrete node-kind dispatch use case.
- Exact Java type names and diagnostic-template syntax remain implementation design within #16 and #17's confirmed
  boundaries.
- The supported 0.4 matrix is Java 17 with Groovy 3, 4, and 5 under #13 and accepted KlumAST #455 ADR 0011.

No further library-native product decision is required before the focused issues can prepare implementation plans and
proofs. Production API/build changes still require their normal issue workflow and review.

Issue #455 separately owns the common multi-Groovy design. This plan may contribute facts to that issue but must not select
its outcome.

## Tracer bullets

- [#24 — Deliver the 0.4 architecture and migration baseline](https://github.com/klum-dsl/klum-cast/issues/24)
  coordinates the accepted artifact, package, dependency, activation, SPI, diagnostics, JPMS-identity, compatibility,
  migration-documentation, and verification work. It links the focused issues without absorbing #455's multi-Groovy
  decision, the speculative IDE idea, or KlumAST-specific validation semantics.

- [#22 — Define boolean composition strategies over check outcomes](https://github.com/klum-dsl/klum-cast/issues/22)
  follows the incomplete OR/XOR/conditional scope of #2 and #21. It will prove composition against the confirmed immutable
  context and diagnostic/technical-failure contracts while keeping the cross-repository build design out of scope.

- [#23 — Investigate reusable node-kind dispatch for consumer-authored checks](https://github.com/klum-dsl/klum-cast/issues/23)
  compares KlumAST's consumer-owned `FieldAstValidator` pattern with klum-wrap or another independent use case. It gates
  any helper on two concrete uses and requires an interface-compatible, immutable-context design; it does not upstream
  KlumAST validation semantics and is intentionally not an ADR decision.

- Prove the declarative binding layout before finalizing ADR 0010. Compare an SPI-owned strongly typed annotation, a
  metadata-owned `Class<?>` compatibility bridge, and a name-only model. Compile nested check classes and invalid check
  types; inspect published dependencies, class loading, automatic modules, and package ownership. This is an isolated
  design experiment, not authorization to change the production API or build.

  The reproducible fixture under `docs/implementation/prototypes/check-binding/` passed on Groovy 3.0.17 and 4.0.12. It
  proves that SPI-owned typed binding preserves compile-time rejection and nested-check ergonomics; name binding preserves
  cyclic split-module dependency direction; and a metadata-owned raw bridge preserves the JVM `Class` method descriptor
  but not source-level type safety. It also shows that SPI need not depend on annotations: compile can depend on both.
  ADR 0010 now accepts the resulting dual-binding model.

- Speculatively explore an IntelliJ integration that presents KlumCast diagnostics directly in the editor and can
  eventually contribute
  annotation-aware completion. Stable diagnostic codes, structured arguments, precise source locations, and provenance
  are enabling contracts. Keep IntelliJ platform dependencies outside the compiler core; assess a query/metadata API only
  when completion scenarios are concrete. Eclipse integration is lower priority because its incremental compiler already
  reduces the immediate feedback gap. This is neither a roadmap commitment nor a request for a GitHub issue.

## Issue #455 handoff: Groovy 3–5 verification contract

KlumAST already publishes `com.blackbuild.convention.groovy` from its combined `klum-ast-gradle-plugin` artifact. Its wiki
explicitly says the plugin is not Klum-specific and might be extracted. The implementation selects matching Groovy BOM,
Groovy, and Spock coordinates across Groovy 3/4/5, handles the `org.codehaus.groovy` to `org.apache.groovy` boundary,
supports root-project inheritance and skipping Spock, and configures JUnit Platform. Scenario fixtures cover Groovy 3, 4,
and 5 plus explicit-version and no-Spock cases.

KlumAST issue #455, ADR 0011, its executable plan, and merged pull request #497 accepted a repository-local lane handoff:
compile production once against Groovy 3, recompile tests and fixtures separately with matching Groovy/Spock 3, 4, and 5,
and gate publication on the whole matrix. KlumCast implements that contract directly and does not depend on KlumAST issue
#496 or extract KlumAST's convention plugin. Published-consumer and JPMS fixtures additionally prove the
`org.codehaus.groovy` to `org.apache.groovy` group/module-name boundary.
