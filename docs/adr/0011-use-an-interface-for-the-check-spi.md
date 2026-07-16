# Use an interface for the check SPI

The durable check SPI is a minimal Java interface whose operation accepts the immutable check context and returns
diagnostics. Check authors do not extend a required KlumCast base class; reusable behavior belongs in separate utilities
or optional convenience types. The current `KlumCastCheck` abstract class is migration surface rather than the permanent
contract, avoiding inherited mutable state and preserving consumers' freedom to choose their own implementation
inheritance.
