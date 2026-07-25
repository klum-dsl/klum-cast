# Protect versioned documentation publication and recovery

KlumCast will publish post-4.0 user documentation and isolated artifact API documentation as immutable, versioned static
sites. This consumes the shared route, exact-snapshot, and alias policy from KlumAST ADR 0013, while retaining KlumCast's
own source layout, renderer, Pages writer, credentials, artifact-proof handoff, branding, and recovery operations.

## Decision

`docs/user/` is the canonical source for the role-based onboarding journey, and `README.md` remains the repository
landing page that points to the canonical versioned Pages URLs. A legacy-link inventory, rather than the renderer, decides
which compatibility stubs are needed.

The historic inventory retains `v0.1.0`, `v0.2.0`, `v0.3.0`, and `v0.3.1` from their root `README.md` sources as
archived content. `v0.4.0-rc.2` is retained as an immutable non-stable prerelease snapshot because it contains the
role-based source and 0.4 migration guidance. Earlier RC tags remain unlisted unless the link inventory establishes a
stub need.

The repository-local renderer adopts the KlumAST structural contract without sharing implementation: explicit Git-object
source selection, deterministic static HTML, isolated API trees for `klum-cast-annotations`, `klum-cast-spi`, and
`klum-cast-compile`, exact-tree manifests, and credential-free rendering/link/deep-link verification. The renderer
receives a versioned branding manifest; the KlumCast branding maintainer approves a final manifest. Season 4 assets are
repository-versioned local files under `docs/user/assets/branding/season-4/`, with SHA-256 digests and no remote/CDN
dependency.

The protected documentation writer and GitHub Pages service use the shared library-neutral environment names
`documentation-pages-writer` and `documentation-pages`. The writer environment holds only the ID/key of a dedicated,
repository-scoped Pages-writer GitHub App. It mints a short-lived token only after validating the exact source, version,
stage, absent target path, and manifest. The service environment is credential-free and deploys the read-back ledger.
Neither environment receives artifact-publication credentials.

An active `gh-pages` ruleset prevents creation, updates, deletion, and non-fast-forward changes. The Pages-writer App is
the sole always-bypass actor; no human or `GITHUB_TOKEN` bypass is permitted. Protected writer-environment approval is
the human authorization gate.

Aliases can advance only after a protected, immutable, machine-readable public-artifact-proof handoff. It binds the
release channel, exact version, full source SHA, and successful public-resolve-back run identity. Missing or mismatched
proof cannot advance an alias.

The root-level documentation recovery ledger is append-only. It records failed and superseded attempts with exact
version/SHA, pending path, last verified phase, evidence links, reason, and required next action. Failed paths remain
unlisted; recovery creates a new immutable attempt rather than overwriting or repairing an existing snapshot.

## Consequences

The documentation site becomes a protected release-evidence participant before artifact publication, without gaining
publication, signing, tag, or GitHub Release authority. Existing `release-candidate` and `final-release` environments
remain the artifact-release authorization boundary.

KlumCast adopts no shared renderer extraction now. Cross-library alignment is limited to the documented structural and
security contract; all implementation and operational configuration remain repository-local. Creating environments,
credentials, the GitHub App, Pages configuration, workflows, or a release requires separately authorized work.
