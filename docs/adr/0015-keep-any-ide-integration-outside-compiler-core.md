# Keep any IDE integration outside compiler core

IDE integration is speculative and is not a roadmap commitment. If pursued, it lives in a separate integration layer that
consumes KlumCast's structured diagnostics; IntelliJ or other IDE APIs do not enter the compiler core. Editor completion
may eventually justify a read-only metadata or query API, but that API must be driven by concrete completion scenarios
rather than anticipated now. No GitHub issue or core API requirement is created by this conditional boundary.
