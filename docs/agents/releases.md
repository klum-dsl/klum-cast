# Maintainer release runbook

This is the single, canonical procedure for a KlumCast maintainer publishing a public release candidate (RC) or final
release. Follow the maintainer path below in order; do not infer a release procedure from individual Gradle tasks or CI
workflows. Those implementation details exist to enforce this runbook and provide its evidence.

The shared channel, immutability, authorization, and recovery policy is authoritative in
[KlumAST ADR 0012](https://github.com/klum-dsl/klum-ast/blob/master/docs/adr/0012-shared-prerelease-channel-policy.md).
This document applies that policy to KlumCast's three artifacts and registries; it does not redefine the shared policy.

Implementing or changing this runbook never authorizes a publication. Every RC publication is approved through the
protected `release-candidate` GitHub environment. Every final publication is separately approved through the protected
`final-release` environment. A release operator must stop if either environment lacks required reviewers.

## Maintainer path

Use these as the operational sequence. The following sections give the exact commands, approval points, and recovery
rules for each step.

1. Select the exact `main` commit and intended RC/final version; complete the prerequisites and `clean check` evidence.
2. Dispatch the protected RC or final publication workflow for that exact commit. Approval of its matching environment is
   the required human authorization.
3. Wait for Maven Central to expose all three artifacts, then run the credential-free public resolve-back workflow.
4. Only after that proof passes, create the annotated tag and GitHub Release from the exact published commit.
5. Record the completed evidence, or follow the abort/supersession rules without attempting to recover a public RC.

The release workflow performs staging, signing, and the full repository gate. The public-resolution workflow proves the
published result from clean Gradle and Maven consumers. A maintainer owns the authorization, sequencing, observation,
tagging, GitHub Release, and evidence record; no ordinary CI job can perform those actions.

## What this procedure releases

One KlumCast publication consists of exactly these Maven coordinates at one identical version:

- `com.blackbuild.klum.cast:klum-cast-annotations`
- `com.blackbuild.klum.cast:klum-cast-spi`
- `com.blackbuild.klum.cast:klum-cast-compile`

The protected publication task publishes only each subproject's `mavenJava` publication. It must never publish the
local-only `consumerFixture` publication. All three artifacts, their POMs, Gradle module metadata, sources, Javadocs, and
signatures are uploaded to one Sonatype staging repository, which is closed and released once for the complete product.
An upload of fewer than all three artifacts is not a release.

Release work must preserve the contracts in ADRs 0017–0020, 0022, 0024, and 0026:

- Java 17 is the build and consumer baseline; the same production artifacts are verified with Groovy 3, 4, and 5.
- Annotations remains Groovy-free. SPI publishes no Groovy selector. Compile has compile-scope dependencies on annotations
  and SPI, and owns compiler activation.
- Package ownership has no split packages. Automatic module names remain `com.blackbuild.klum.cast.annotations`,
  `com.blackbuild.klum.cast.spi`, and `com.blackbuild.klum.cast.compiler`.
- Consumers select `org.codehaus.groovy:groovy` / module `org.codehaus.groovy` for Groovy 3 and
  `org.apache.groovy:groovy` / module `org.apache.groovy` for Groovy 4 and 5.

Groovy 2.4 and Java 11 belong only to the 0.3 line. They are not valid 0.4 release evidence.

## Before publishing an RC or final

Before requesting authorization, confirm all of the following:

1. The release issue defines the intended version and scope. Its changes, `CHANGES.md`, migration/support documentation,
   and user documentation agree. Every included pull request is merged to `main`, and unrelated issue/label/milestone state
   has not been changed.
2. The selected full commit SHA is on `origin/main`. The local checkout is on that commit, clean, not a composite build,
   and not behind its tracked remote. Run `git fetch origin` and inspect `git status --short --branch`.
3. JDK 17 is active. Run `./gradlew --version` and verify both the launcher and daemon JVM before continuing.
4. `./gradlew clean check` passes. This is the complete Java 17/Groovy 3–5 gate: isolated tests, packaging, POMs, local
   Gradle/Maven consumers, classpath activation, automatic modules, package ownership, service discovery, and
   generation-matching JPMS behavior.
5. The release notes name compatibility changes and deferred work. In particular, do not present an RC as `0.4.0` and do
   not claim KlumAST's coordinated real-consumer RC train; that later validation belongs to KlumAST issue #512.
6. The appropriate protected GitHub environment exists with required reviewers. Its secrets are Central token username
   `SONATYPE_USERNAME`, token password `SONATYPE_PASSWORD`, ASCII-armored in-memory PGP key `SIGNING_KEY`, and
   `SIGNING_PASSWORD`. Credential values must not be placed in files, command arguments, ordinary CI, pull-request/fork
   jobs, artifacts, or logs.
7. The operator can approve Central staging and create Git tags/GitHub Releases, and `gh auth status` shows only the
   access needed for those later manual record steps.

Ordinary CI has only `contents: read` and no release environment or secrets. The publication workflow also retains
`contents: read`; it cannot create tags or GitHub Releases. The Gradle publication boundary additionally requires
`KLUM_CAST_RELEASE_AUTHORIZED` to match `-Prelease.channel`, so raw Sonatype tasks cannot become an accidental alternate
path.

## Version and tag identities

The only accepted public inputs are:

| Operation | Gradle channel | Version | Annotated tag | GitHub Release |
| --- | --- | --- | --- | --- |
| Release candidate | `rc` | `X.Y.Z-rc.N`, with increasing `N >= 1` | `vX.Y.Z-rc.N` | Prerelease |
| Final | `final` | `X.Y.Z` | `vX.Y.Z` | Non-prerelease |

Use `./gradlew candidate --dry-run` or `./gradlew final --dry-run` on the clean release commit to inspect Nebula's inferred
identity without creating a tag. The protected workflow still receives an explicit exact version, and Gradle rejects a
channel/version mismatch. Nebula creates the annotated `v`-prefixed tag only later, with `candidate` or `final` and the
same explicit `-Prelease.version`.

Snapshots (`snapshot`, `immutableSnapshot`, and `devSnapshot`) are development-only and are not accepted by the public
publication or resolve-back tasks. Sonatype staging is only the gate through which an RC/final passes. Alpha and beta
identities are rejected. An RC is never relabelled or promoted; a final is a distinct publication from the accepted RC
commit.

## Changelog version lifecycle

`CHANGES.md` records one cumulative section for the projected final release rather than one section per RC. Apply these
transitions in order:

1. During development and every RC validation attempt, the maintainer keeps the active heading at the projected final
   version, for example `0.4.0 (unreleased)`. Publishing or superseding an RC does not rename or fragment that section.
2. For each RC, the release operator records its exact immutable `X.Y.Z-rc.N` identity in the annotated tag, GitHub
   prerelease, and release-evidence record at the corresponding RC publication steps below. Those records identify the
   RC; the changelog heading continues to identify the projected final release.
3. After accepting an RC and before requesting final publication, the final-release operator creates a
   documentation-only finalization commit that reconciles the release notes and changes the active heading to the exact
   final version and release date, for example `0.4.0 — YYYY-MM-DD`. Record this commit as the documentation-only
   exception permitted by KlumAST ADR 0012. Any substantive source, dependency, build, signing, publication, or workflow
   change crosses the new-RC boundary: stop finalization and publish and validate the next increasing RC first.
4. Immediately after the final release, the maintainer opens the next projected-version `(unreleased)` section before
   accumulating further user-visible changes. This begins the next normal development cycle; it is not part of the
   completed release's validated content.

Until step 3 is deliberately performed for an actual final release, retain the current projected-version heading.

## Publish an RC

Set the evidence record's exact `<version>`, `<commit>`, and release-notes file before starting. Then perform these steps
in order:

1. Complete the prerequisites and record the successful `./gradlew clean check` command and commit SHA.
2. Request the protected publication without checking credentials into the shell or repository:

   ```text
   gh workflow run publish-release.yml --ref main -f channel=rc -f version=<X.Y.Z-rc.N> -f commit=<full-sha>
   ```

   Locate the run with `gh run list --workflow=publish-release.yml` and watch it with `gh run watch <run-id>`.
   Approval of the `release-candidate` environment is the human RC authorization.
3. The workflow checks out the exact commit with no persisted GitHub credential and verifies that it is on `origin/main`.
   Gradle then reruns `check`, signs the three `mavenJava` publications in memory, initializes one Sonatype staging
   repository, uploads the complete product, closes it, and releases it to Central. No tag has been created yet.
4. Wait until all three exact coordinates are visible from `https://repo.maven.apache.org/maven2`. Then run the
   credential-free public proof:

   ```text
   gh workflow run verify-public-release.yml --ref main -f version=<X.Y.Z-rc.N>
   ```

   The equivalent local command is
   `./gradlew -PpublicVersion=<X.Y.Z-rc.N> verifyPublicReleaseCoordinates --no-build-cache`. The gate deletes its isolated
   Gradle and Maven caches, uses Maven Central only, and proves Gradle and Maven compiler activation on Groovy 3, 4, and 5.
   The Gradle lanes additionally inspect the three downloaded JARs, classpath service activation, package ownership,
   automatic-module names, and the generation-matching Groovy module on the JPMS module path.
5. Only after Central visibility and the clean resolve-back proof succeed, create and push the tag from the exact commit:

   ```text
   ./gradlew candidate -Prelease.version=<X.Y.Z-rc.N>
   ```

   Nebula reruns the release check, creates annotated tag `vX.Y.Z-rc.N`, and pushes it to the tracked remote. Verify the
   remote tag targets `<commit>`.
6. Create the GitHub prerelease from the already pushed tag:

   ```text
   gh release create v<X.Y.Z-rc.N> --verify-tag --target <commit> --prerelease --title <X.Y.Z-rc.N> --notes-file <notes-file>
   ```

7. Complete the evidence record and link it from the release issue. An RC remains a validation artifact, not the formal
   release.

## Publish a final release

A final release needs a new, separate authorization even when it uses the accepted RC commit.

1. Identify the accepted RC and verify that its public consumer run passed. The final source commit must be that accepted
   RC commit unless subsequent changes are documentation-only. Record every such documentation-only change and why it
   does not change the validation claim. Any source, build, signing, publication, dependency, or workflow change requires
   the next RC instead.
2. Perform the changelog finalization transition above: reconcile final release notes, migration/support documentation,
   issues, and pull requests in a documentation-only commit, including the exact final version/date heading. Then run
   `./gradlew clean check` again on that exact final commit.
3. Request the independently protected final publication:

   ```text
   gh workflow run publish-release.yml --ref main -f channel=final -f version=<X.Y.Z> -f commit=<full-sha>
   ```

   Approval of `final-release` is the separate human final authorization. Watch the workflow through staging close and
   Central release.
4. After all three final coordinates are publicly visible, run
   `gh workflow run verify-public-release.yml --ref main -f version=<X.Y.Z>` and retain its evidence.
5. From the exact final commit, run `./gradlew final -Prelease.version=<X.Y.Z>`. Verify annotated tag `vX.Y.Z`, then create
   the GitHub Release with
   `gh release create v<X.Y.Z> --verify-tag --target <commit> --title <X.Y.Z> --notes-file <notes-file>`.
6. Recheck the published GitHub Release, Central coordinates, issue/PR links, release notes, migration guide, and support
   matrix. Update the release issue only with the completed evidence; do not rewrite unrelated tracker state.
7. Immediately after the final release is complete, open the next projected-version `(unreleased)` changelog section
   before recording further user-visible changes.

## Release evidence record

Keep one durable record in the release issue and GitHub Release. It must contain:

- channel, exact version, all three coordinates, source commit, annotated tag, and GitHub Release URL;
- protected publication workflow run, environment approval, Sonatype staging/release outcome, and Central URLs;
- Java 17 version and the complete `clean check` run, including Groovy 3, 4, and 5 results;
- public resolve-back workflow run and its Gradle/Maven, classpath, activation, package, POM/module, signature, and JPMS
  results for every Groovy generation;
- SHA-256 checksums and PGP signature verification for the three primary JARs, plus confirmation that sources, Javadocs,
  POMs, and Gradle module metadata are present;
- release-notes/migration/support-document links, included issues and pull requests, deferred work, and any superseded
  attempts or documentation-only exception.

Never treat an unpublished local consumer fixture, staging URL, successful upload without close/release, or resolution
from a warm/shared cache as public evidence.

## Abort, retry, and supersession

- Before Central accepts any upload and before any tag is pushed, a failed local check or authorization may be corrected
  and rerun with the same identity only when no repository content was accepted. Record the failure if authorization had
  already started.
- If an RC upload is partial, its staging close fails, any coordinate becomes public, or its tag was pushed, stop. Drop an
  open staging repository through the Central administrative interface, record that RC as failed/superseded, and use the
  next `rc.N`. Never overwrite, delete, relabel, or recover the RC.
- If Central released all three RC artifacts but public validation rejects them, retain them and their evidence as a
  rejected RC and use the next `rc.N` after corrective changes.
- If final staging fails and no artifact was ever public, stop and obtain an explicit human recovery decision. A retry may
  only reproduce the same bytes from the same commit; any content change requires renewed final authorization and, when
  substantive, another RC. If any final artifact became public, the version is burned.
- The procedure creates tags after Central publication to avoid dangling release tags. If an RC tag nevertheless exists,
  it is immutable and burns that RC. A final tag may be deleted only by an explicit recorded human decision proving that
  no artifact for the version was ever published. Never delete a public RC tag.
- Credentials are not a recovery tool. Do not reveal, rotate, recreate, or move them into ordinary CI. Credential changes
  require their own authorized administrative work.

After an abort, preserve logs that contain no secrets, record the exact last successful registry state, and reconcile the
release issue before another attempt. Do not describe a partial three-artifact set, open staging repository, or failed
resolve-back run as a successful release.

## External evidence condition

This repository deliberately does not simulate public success with the temporary consumer repository. The first complete
`verifyPublicReleaseCoordinates` evidence remains externally conditioned on a manually authorized RC being present in
Maven Central. This implementation does not publish, tag, sign with live credentials, or create a GitHub Release.
KlumAST issue #512 owns the later coordinated real-consumer RC-train validation after the repository-local RC evidence
exists.
