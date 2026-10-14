# Use effective-declaration contributors for coordinated preprocessing

KlumCast exposes a narrow compiler-artifact service-provider contract for an integration that must add authorized
annotations before validation of one declaration. A service-loaded effective-declaration contributor receives the
declaration during KlumCast's semantic-analysis traversal and returns one complete batch of annotation additions with
source provenance. KlumCast attaches that batch, validates the resulting effective annotation set once, records its
final declaration state, and retains ownership of constraint discovery and execution, applicability filters,
composition, diagnostic deduplication, and compiler rendering.

This avoids relying on order between independent global transformations. It also prevents callers from treating
per-annotation `VALIDATED` metadata or `ValidationHandler` as an integration protocol: when a batch changes the
effective set, KlumCast reconsiders all validated annotations on that declaration; when the resulting state is
unchanged, it does nothing.

The contributor supplies additions and their origin only. It cannot schedule arbitrary work, replace KlumCast
traversal, directly mutate the declaration, or change it after the handoff. The contract is framework-neutral and does
not establish a general compiler-plugin framework, transformation scheduler, or annotation-propagation mechanism.
