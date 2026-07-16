# Activate validation from the compiler artifact

KlumCast uses a service-loaded global AST transformation as its default supported activation mechanism. Adding the
compiler artifact to a Groovy compilation classpath explicitly activates validation; the metadata and check-SPI artifacts
remain inert. This keeps activation independent of any particular build tool and avoids requiring per-annotation
registration. The compiler artifact's documentation must make its ambient effect and scanning scope explicit, while
transformation phase, ordering guarantees, opt-out behavior, and any future manual activation hook remain separate
decisions.
