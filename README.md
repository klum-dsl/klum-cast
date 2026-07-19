# KlumCast

KlumCast validates uses of annotations during Groovy compilation. It runs reusable checks before an annotation-driven
AST transformation performs its main work, and reports constraint violations as source-positioned compiler diagnostics.
It is not a runtime model-validation library.

KlumCast 0.4 requires Java 17 and supports Groovy 3, 4, and 5.

## Start with your role

Choose the journey that matches the task you are doing. These are roles, not required people, modules, or artifact names;
one project or person may take more than one role.

| Role | Start here | Outcome |
|---|---|---|
| Check writer | [Write a reusable check](docs/user/check-writer.md) | Bind stateless validation logic to a validation annotation and return actionable diagnostics. |
| Check aggregator | [Assemble domain validations](docs/user/check-aggregator.md) | Combine built-in and custom validation annotations into a reusable domain-oriented annotation. |
| Check user | [Validate a consumer annotation](docs/user/check-user.md) | Mark a consumer-defined annotation as validated and activate KlumCast for the Groovy compilation that uses it. |

Read [dependencies, activation, and shared concepts](docs/user/README.md) for the common compile-time boundary, canonical
language, artifact roles, source scan, and transformation timing. The `docs/user/` location is an interim user-facing
home and may move when the project adopts a final documentation strategy.

## Artifact map

- `klum-cast-annotations` contains Groovy-free metadata and built-in validation annotations.
- `klum-cast-spi` contains the Groovy-facing `Check`, immutable `CheckContext`, diagnostics, filters, and typed binding.
- `klum-cast-compile` contains and activates the service-loaded compiler transformation. Put it only on Groovy
  compilation classpaths that should run validation.

The [shared orientation](docs/user/README.md#dependencies-by-role) gives the exact Gradle and Maven scopes. Check writers
must also select the Groovy compiler dependency matching their Groovy generation; the SPI deliberately does not select
one transitively.

## Further reference

- [Built-in validation annotations](klum-cast-annotations/src/main/java/com/blackbuild/klum/cast/checks)
- [Supported SPI source](klum-cast-spi/src/main/java/com/blackbuild/klum/cast/spi)
- [0.4 migration guide](docs/migration/0.4.md)
- [Release-facing changes](CHANGES.md)
- [Architecture decisions](docs/adr/README.md)

The migration guide is for upgrading existing integrations; the three role journeys above are the single onboarding
route for new use.
