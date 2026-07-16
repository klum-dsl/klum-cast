# Constraint violations are diagnostics

Expected constraint violations are explicit source-positioned diagnostic data, and one check may produce zero or more of
them. Unexpected failures in a check, binding, or the KlumCast engine remain technical failures with their causes preserved;
legacy `ValidationException` and runtime-exception behavior may be adapted for migration but does not define the new SPI.
