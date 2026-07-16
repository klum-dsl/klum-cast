#!/usr/bin/env bash
set -euo pipefail

root="$(cd "$(dirname "$0")" && pwd)"
work="$(mktemp -d "${TMPDIR:-/tmp}/klum-cast-binding-poc.XXXXXX")"

if [[ -z "${GROOVY_JAR:-}" ]]; then
    GROOVY_JAR="$(rg --files "$HOME/.gradle/caches/modules-2/files-2.1/org.apache.groovy/groovy/4.0.12" | rg '/groovy-4\.0\.12\.jar$' | head -1)"
fi

if [[ ! -f "$GROOVY_JAR" ]]; then
    echo "Set GROOVY_JAR to a Groovy compiler jar" >&2
    exit 2
fi

mkdir -p "$work/annotations" "$work/spi" "$work/typed-valid" "$work/typed-invalid" \
    "$work/raw-invalid" "$work/named-nested" "$work/probe" "$work/split-named-meta" \
    "$work/split-named-impl" "$work/split-typed-meta" "$work/split-typed-impl" "$work/split-typed-together" \
    "$work/signature-typed" "$work/signature-raw" "$work/signature-consumer" "$work/signature-probe"

javac -d "$work/annotations" \
    "$root/annotations/poc/metadata/NamedCheckBinding.java" \
    "$root/annotations/poc/metadata/RawCheckBinding.java"
jar --create --file "$work/klum-cast-annotations.jar" -C "$work/annotations" .

javac -cp "$GROOVY_JAR" -d "$work/spi" \
    "$root/spi/poc/spi/Check.java" \
    "$root/spi/poc/spi/CheckContext.java" \
    "$root/spi/poc/spi/TypedCheckBinding.java"
jar --create --file "$work/klum-cast-spi.jar" -C "$work/spi" .

javac -cp "$GROOVY_JAR:$work/klum-cast-spi.jar" -d "$work/typed-valid" \
    "$root/typed-valid/poc/consumer/TypedConstraint.java"

if javac -cp "$GROOVY_JAR:$work/klum-cast-spi.jar" -d "$work/typed-invalid" \
    "$root/typed-invalid/poc/consumer/InvalidTypedConstraint.java" 2>"$work/typed-invalid.err"; then
    echo "Expected the invalid typed binding to fail compilation" >&2
    exit 1
fi

javac -cp "$work/klum-cast-annotations.jar" -d "$work/raw-invalid" \
    "$root/raw-invalid/poc/consumer/InvalidRawConstraint.java"

javac -cp "$GROOVY_JAR:$work/klum-cast-annotations.jar:$work/klum-cast-spi.jar" \
    -d "$work/named-nested" "$root/named-nested/poc/consumer/NamedConstraint.java"

javac -cp "$GROOVY_JAR:$work/klum-cast-annotations.jar:$work/klum-cast-spi.jar:$work/raw-invalid:$work/named-nested" \
    -d "$work/probe" "$root/probe/poc/probe/BindingProbe.java"

java -cp "$GROOVY_JAR:$work/klum-cast-annotations.jar:$work/klum-cast-spi.jar:$work/raw-invalid:$work/named-nested:$work/probe" \
    poc.probe.BindingProbe

javac -cp "$work/klum-cast-annotations.jar" -d "$work/split-named-meta" \
    "$root/split-named-meta/poc/split/SplitNamedConstraint.java"
javac -cp "$GROOVY_JAR:$work/klum-cast-spi.jar:$work/split-named-meta" -d "$work/split-named-impl" \
    "$root/split-named-impl/poc/impl/SplitNamedCheck.java"

if javac -cp "$GROOVY_JAR:$work/klum-cast-spi.jar" -d "$work/split-typed-meta" \
    "$root/split-typed-meta/poc/split/SplitTypedConstraint.java" 2>"$work/split-typed-meta.err"; then
    echo "Expected typed metadata to require its implementation artifact" >&2
    exit 1
fi

if javac -cp "$GROOVY_JAR:$work/klum-cast-spi.jar" -d "$work/split-typed-impl" \
    "$root/split-typed-impl/poc/impl/SplitTypedCheck.java" 2>"$work/split-typed-impl.err"; then
    echo "Expected typed implementation to require its metadata artifact" >&2
    exit 1
fi

javac -cp "$GROOVY_JAR:$work/klum-cast-spi.jar" -d "$work/split-typed-together" \
    "$root/split-typed-meta/poc/split/SplitTypedConstraint.java" \
    "$root/split-typed-impl/poc/impl/SplitTypedCheck.java"

javac -cp "$GROOVY_JAR:$work/klum-cast-spi.jar" -d "$work/signature-typed" \
    "$root/signature-typed/poc/compat/Binding.java"
javac -d "$work/signature-raw" "$root/signature-raw/poc/compat/Binding.java"
javac -cp "$GROOVY_JAR:$work/klum-cast-spi.jar:$work/signature-typed" -d "$work/signature-consumer" \
    "$root/signature-consumer/poc/compat/CompatConstraint.java"
javac -cp "$work/signature-raw:$work/signature-consumer" -d "$work/signature-probe" \
    "$root/signature-probe/poc/probe/SignatureProbe.java"
java -cp "$GROOVY_JAR:$work/klum-cast-spi.jar:$work/signature-raw:$work/signature-consumer:$work/signature-probe" \
    poc.probe.SignatureProbe

typed_descriptor="$(javap -classpath "$work/signature-typed" -s poc.compat.Binding | rg -A1 'value\(\)' | tail -1 | xargs)"
raw_descriptor="$(javap -classpath "$work/signature-raw" -s poc.compat.Binding | rg -A1 'value\(\)' | tail -1 | xargs)"
if [[ "$typed_descriptor" != "$raw_descriptor" ]]; then
    echo "Typed and raw binding descriptors differ" >&2
    exit 1
fi

echo "annotations-module=$(jar --describe-module --file "$work/klum-cast-annotations.jar" 2>/dev/null | rg ' automatic$' | head -1)"
echo "spi-module=$(jar --describe-module --file "$work/klum-cast-spi.jar" 2>/dev/null | rg ' automatic$' | head -1)"
echo "groovy-module=$(jar --describe-module --file "$GROOVY_JAR" 2>/dev/null | rg ' automatic$' | head -1)"
echo "spi-dependencies=$(jdeps --ignore-missing-deps --module-path "$GROOVY_JAR" --print-module-deps "$work/klum-cast-spi.jar")"
echo "typed-valid=compiled"
echo "typed-invalid=rejected-by-javac"
echo "raw-invalid=compiled-and-rejected-only-by-runtime-probe"
echo "split-named=metadata-then-implementation-compiled"
echo "split-typed=only-joint-compilation-succeeded"
echo "typed-to-raw-descriptor=$typed_descriptor"
echo "typed-consumer-with-raw-binding=loaded"
echo "work=$work"
