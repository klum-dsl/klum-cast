# KlumCast

KlumCast is a compile-time annotation-use validation library for Groovy compilation. Consumers declare reusable
constraints on annotation types and members; KlumCast runs checks against target AST nodes and emits source-positioned
diagnostics during semantic analysis. Consumers that depend on successful validation perform their main transformation
in a later compilation phase. KlumCast is not a runtime model-validation library and does not own consumer-framework
semantics.

## Language

**Validated annotation**:
A consumer-defined annotation whose uses KlumCast validates.
_Avoid_: Annotation under check, target annotation

**Validation annotation**:
A reusable declarative annotation that contributes one or more checks to a validated annotation. Validation annotations may
be composed from other validation annotations.
_Avoid_: Jury annotation

**Target**:
The Groovy AST node on which a validated annotation is used.
_Avoid_: Annotation target when referring to the annotation type's Java `@Target` declaration

**Check**:
Validation logic bound declaratively to a validation annotation and evaluated for a target. Consumers may author checks
as a supported KlumCast extension capability.
_Avoid_: Validator when referring to the executable check implementation

**Check context**:
The immutable description of one check invocation, including the validated annotation, target, applicable validation
annotation, member context, binding metadata, and composition path. It intentionally exposes Groovy compiler AST types.
_Avoid_: Mutable check state, annotation stack

**Filter**:
A condition that determines whether a validation annotation or check applies to a target.

**Diagnostic**:
A source-positioned compiler failure produced when an annotation use violates a declared constraint. Diagnostics are
expected check outcomes; broken checks, bindings, and framework execution are technical failures instead.
_Avoid_: Runtime validation error

**Technical failure**:
An unexpected failure of a check implementation, declarative binding, or the KlumCast engine. A technical failure is not a
diagnostic about consumer annotation usage.
_Avoid_: Constraint violation
