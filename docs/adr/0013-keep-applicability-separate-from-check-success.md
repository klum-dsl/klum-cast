# Keep applicability separate from check success

Applicability is a first-class concept distinct from check success: a filtered-out check is not applicable, which must not
be treated as a successful branch by boolean composition. KlumCast retains a supported applicability-filter SPI, redesigned
as a stateless interface receiving immutable context rather than the mutable `Filter.Function` base class. Declarative
target-kind filters remain built in, custom filters support typed and name-based bindings under the same resolution rules
as checks, and all filters attached to one binding are conjunctive unless an explicit composition strategy defines
otherwise.
