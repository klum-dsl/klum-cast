# Check-binding layout proof of concept

This isolated fixture tests the binding alternatives required by provisional ADR 0010. It does not modify KlumCast's
production API or build.

Run `./run.sh`. By default the script locates the cached Groovy 4.0.12 jar; set `GROOVY_JAR` to exercise another Groovy
generation. The fixture was run successfully with Groovy 3.0.17 and 4.0.12 on JDK 25.

## Proven results

- An SPI-owned `Class<? extends Check>` binding compiles with a nested check class and rejects `String.class` at compile
  time. The check/context API directly exposes Groovy `AnnotationNode` and `AnnotatedNode`; no AST abstraction is needed.
- A metadata-owned `Class<?>` binding keeps metadata independent of SPI and Groovy, but `String.class` compiles. The engine
  can reject it only when resolving the binding.
- A name-bound nested check resolves successfully using its binary name with `$`. This works but is materially less
  ergonomic than a class literal because annotation string values cannot call `Class.getName()`.
- A name-bound metadata artifact compiles before its implementation artifact even when the check implementation refers
  back to the control annotation. The corresponding typed layout has no valid separate compilation order: metadata refers
  to the implementation class and the implementation refers to metadata. Joint compilation works, proving that the
  problem is artifact dependency direction rather than Java type correctness.
- Changing an annotation member from `Class<? extends Check>` to `Class<?>` preserves the JVM method descriptor
  `()Ljava/lang/Class;`. A consumer compiled against the typed form loads with the raw form when its check implementation's
  SPI/Groovy dependencies are present. This is a viable binary migration bridge, but recompiled consumers lose type
  checking on that legacy member.
- Jars named `klum-cast-annotations.jar` and `klum-cast-spi.jar` derive automatic module names `klum.cast.annotations` and
  `klum.cast.spi`. The SPI requires Groovy because its public context uses Groovy AST types. Groovy 3 derives
  `org.codehaus.groovy`; Groovy 4 derives `org.apache.groovy`, so an explicit module descriptor or variant-aware module
  strategy remains coupled to the separate multi-Groovy work in KlumAST #455.
- The experiment needs no SPI-to-annotations edge. The compiler artifact can depend on both siblings and normalize their
  binding metadata into SPI-owned invocation data.

## Supported conclusion

The evidence supports keeping both modes with explicit ownership: name binding in the lightweight metadata artifact and
strong typed binding in the SPI artifact. Co-located or nested check implementations can choose the typed form; cyclic
split-module layouts use names. The existing mixed binding annotation can become a deprecated raw bridge for a documented
migration window. Dropping typed binding is possible but is not required by the dependency constraints.
