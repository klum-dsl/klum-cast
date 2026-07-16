# Use engine-owned, compilation-scoped check instances

Check implementations are concrete classes with an accessible no-argument constructor. The compiler engine owns their
lifecycle and may instantiate per binding or reuse instances within one compilation; checks must not depend on instance
identity, invocation count, or retained state. Instances are not shared across compilation runs, avoiding classloader
retention. Constructor injection, service registries, and custom factories remain out of scope until a concrete dependency
injection requirement emerges.
