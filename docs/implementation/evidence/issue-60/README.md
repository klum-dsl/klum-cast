# RC-fix evidence: ordinary Gradle contributor loading

Date: 2026-10-14. Related: KlumCast #60 and KlumAST #890.

## Finding and bounded correction

Released `0.5.0-rc.1`, source `a25c6c36d2aa6681735a7871880054ace23417cb`, rejects independently compiled contributors
with `ServiceConfigurationError: ... not a subtype` under Gradle 8.14.4. Source resolution and AST transformations can
load separate copies of the compiler interface. Passing the source loader to `ServiceLoader` crosses those identities.

The new ordinary Gradle regression reproduces that failure with the unchanged compiler. KlumCast now implements
`CompilationUnitAware` and uses the compilation unit's transform loader to discover providers. The compiler supplies
that same loader to named check and applicability-filter resolution during effective validation. An initial
discovery-only correction passed the no-op gate but failed the actual Required probe because its named built-in check
was still loaded through the source loader. A stronger ordinary-Gradle regression also exposed silently skipped checks on an existing annotation: its reflective
metadata came from the source loader, so typed meta-annotation identity tests failed. The final correction reflects
validated-annotation metadata through the transform loader while keeping the original AST nodes for context and
diagnostics. All three reflection/implementation boundaries therefore use the compiler's identity.

The declaration handoff, provenance, duplicate checks, final state, and diagnostic rendering algorithms are unchanged. No consumer
loader adjustment, manual validation, direct local-transform invocation, or production KlumAST patch is involved.
The fallback for internal transformation instances without a compilation unit uses the contributor interface's loader;
ordinary service activation receives the real compilation unit from Groovy.

## Local verification

Java: Zulu 17.0.3+7-LTS. Gradle: 8.14.4. Groovy: 3.0.25 / 4.0.32 / 5.0.6.

- `./gradlew clean check`: passes, including 85 compiler tests in each isolated Groovy lane, no failures or skips,
  packaging, local Gradle/Maven consumers, metadata and generation-matching JPMS checks.
- `verifyGradleContributorGroovy3`, `verifyGradleContributorGroovy4`, `verifyGradleContributorGroovy5`: pass. These are
  now part of root `check`. Each compiles the same Java provider classes with no registration, no-op registration, and
  contributing registration. A separate foreign JAR supplies a typed check, named check, named filter, and local
  transformation. An explicit existing annotation's typed check must observe the newly contributed annotation.
  All checks/filter must run once before normal local discovery invokes the transformation once.
- The supplied KlumAST no-op reproducer and its clean no-service control pass all three ordinary compilation lanes.
- The saved actual-annotation probe passes all three compilation/runtime lanes and prints
  `REQUIRED-AUTOCREATE-QUERY-CLUSTER-FOREIGN-PASSED` in each.

KlumAST was cloned locally into `/tmp/klumcast-issue60-ap1-check` at the supplied commit
`12c7587c35802f3b5cc8f8d7212243a82bc6db0d`. The original consumer worktree was not changed. Only isolated dependency
resolution and opt-in probe source inputs were added. The candidate resolves all three artifacts from KlumCast's
local `consumer-test-repository`, with exact version
`0.5.0-dev.0.uncommitted+codex.issue.60.contributor.classloader.a25c6c3`.
This is a local development artifact identity, not an admitted public replacement. Rebuilding after committing changes
Nebula's development identity; read the new exact version from that repository's Maven metadata when repeating.

The actual probe returns contributions through the neutral seam for real `Required`, `AutoCreate`, `Builder.Query`,
and `Cluster` annotation classes. Its schema uses none of those KlumAST annotations explicitly. An explicit foreign annotation verifies effective-set
validation of annotations already present before contribution. Runtime assertions prove one
resulting `Validate` annotation and the exact missing-required-field failure, child auto-creation, a query callable in
Builder scope, and Cluster selection. The separately compiled foreign annotation checks that typed/named validation
and the named filter ran before its normally discovered transform creates its sentinel method exactly once. This is
bounded integration evidence; it does not claim completion of KlumAST's AP-1 production qualification.

## Repeating the supplied no-op gate

First run KlumCast's `clean check` to build the local artifact repository and foreign JARs. In an isolated KlumAST clone
at the commit above, set `cast_root` to this KlumCast checkout and `cast_version` to its exact local publication version:

```shell
./gradlew -I "$cast_root/docs/implementation/evidence/issue-60/no-op.gradle" \
  -PcastCandidateVersion="$cast_version" \
  -PcastCandidateRepository="$cast_root/build/consumer-test-repository" \
  :klum-ast:clean :klum-ast:verifyAp1GradleResolution \
  :klum-ast:compileTestFixturesGroovy \
  :klum-ast:compileGroovy4TestsGroovy :klum-ast:compileGroovy5TestsGroovy \
  --continue --max-workers=2
```

Repeat with `-Pap1NoService`; each command cleans compiler output before compilation. The script retains the supplied
resolution assertions, including identical artifact versions and rejection of physical Maven Local paths.

## Repeating actual annotation contributions

In the same isolated clone:

```shell
./gradlew -I "$cast_root/docs/implementation/evidence/issue-60/contributing.gradle" \
  -PcastCandidateVersion="$cast_version" \
  -PcastCandidateRepository="$cast_root/build/consumer-test-repository" \
  -PcastProbeRoot="$cast_root/docs/implementation/evidence/issue-60/ap1-probe" \
  -PcastForeignJar="$cast_root/build/contributor-workspaces/contributor-groovy3/build/libs/gradle-contributor-fixture-foreign.jar" \
  :klum-ast:clean :klum-ast:verifyRcFixTestFixtures \
  :klum-ast:verifyRcFixGroovy4Tests :klum-ast:verifyRcFixGroovy5Tests \
  --continue --max-workers=2
```

Only version-neutral Java foreign helper output is shared. Groovy schema and test-fixture sources are compiled separately
with each lane's Groovy generation. The Groovy-3 JavaExec includes the test-fixture compile classpath because that
source set declares Groovy compile-only. This affects runtime assertions only, not compiler loading or transformation
ordering. These init scripts are qualification probes; never apply them to publication or release tasks.

## Release boundary

The correction is substantive and requires a new increasing RC. Proposed complete replacement product:

- `com.blackbuild.klum.cast:klum-cast-annotations:0.5.0-rc.2`
- `com.blackbuild.klum.cast:klum-cast-spi:0.5.0-rc.2`
- `com.blackbuild.klum.cast:klum-cast-compile:0.5.0-rc.2`

These coordinates have not been published or supplied as available artifacts. Preserve the existing `0.5.0-rc.1`
artifacts and tags unchanged. After review/merge and explicit authorization through the protected release runbook,
publish all three artifacts from the exact approved main commit, then perform public resolve-back. KlumAST must rerun
its gate on the exact released replacement and complete AP-1 production qualification. AP-1 remains blocked on that
external artifact handoff; no final-release readiness is claimed.
