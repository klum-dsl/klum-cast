# Use 0.4 as the migration line before 1.0

KlumCast uses `0.4.x` as a transitional, explicitly breaking migration line. `0.4.0` introduces the three-artifact layout,
stable module names, new SPI, dual bindings, and structured diagnostics while retaining the deprecated base-class adapter
and raw typed-member bridge. Release-facing documentation must enumerate dependency, import, binding, and diagnostic
changes. `0.3.x` receives only necessary fixes. `1.0.0` removes the temporary bridges and begins the durable compatibility
promise on the clean architecture.
