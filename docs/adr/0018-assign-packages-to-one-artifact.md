# Assign each package to one artifact

KlumCast preserves `com.blackbuild.klum.cast` and `com.blackbuild.klum.cast.checks` for declarative metadata and built-in
validation annotations, places durable public extension contracts under `com.blackbuild.klum.cast.spi`, and places the
engine and built-in implementations in compiler-owned packages such as `com.blackbuild.klum.cast.compiler.internal`. No
package is split across artifacts. The old `com.blackbuild.klum.cast.checks.impl.KlumCastCheck` name may live temporarily
and exclusively in the SPI artifact as a deprecated migration adapter, but it is not a permanent public package.
