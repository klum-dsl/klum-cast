# Checks receive an immutable invocation context

Each check invocation receives one immutable check context instead of being configured through mutable protected fields
plus separate method parameters. The context provides the validated annotation, target, typed control annotation when
present, member context, binding metadata, and composition path; the eventual interface-versus-base-class shape and
diagnostic result mechanism remain separate decisions.
