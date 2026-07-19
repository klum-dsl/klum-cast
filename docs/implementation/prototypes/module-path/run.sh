#!/usr/bin/env bash
set -euo pipefail

required=(ANNOTATIONS_JAR SPI_JAR COMPILER_JAR GROOVY_JAR GROOVY_VERSION FIXTURE_BUILD_DIR JAVA_HOME)
for variable in "${required[@]}"; do
    if [[ -z "${!variable:-}" ]]; then
        echo "Missing required environment variable: $variable" >&2
        exit 2
    fi
done

fixture_dir="$(cd "$(dirname "$0")" && pwd)"
work_dir="$FIXTURE_BUILD_DIR"
classpath="$ANNOTATIONS_JAR:$SPI_JAR:$COMPILER_JAR:$GROOVY_JAR"
module_path="$classpath"
java_bin="$JAVA_HOME/bin"

[[ "$work_dir" == */module-feasibility-groovy[345] ]] || {
    echo "Fixture build directory must identify one Groovy lane: $work_dir" >&2
    exit 2
}
rm -rf -- "$work_dir"
mkdir -p "$work_dir"

expect_manifest_name() {
    local jar="$1"
    local expected="$2"
    local actual
    actual="$(unzip -p "$jar" META-INF/MANIFEST.MF | tr -d '\r' | sed -n 's/^Automatic-Module-Name: //p')"
    [[ "$actual" == "$expected" ]] || {
        echo "Expected $jar to declare $expected but found ${actual:-no Automatic-Module-Name}" >&2
        exit 1
    }
}

expect_manifest_name "$ANNOTATIONS_JAR" com.blackbuild.klum.cast.annotations
expect_manifest_name "$SPI_JAR" com.blackbuild.klum.cast.spi
expect_manifest_name "$COMPILER_JAR" com.blackbuild.klum.cast.compiler

service="$(unzip -p "$COMPILER_JAR" META-INF/services/org.codehaus.groovy.transform.ASTTransformation | tr -d '\r')"
[[ "$service" == 'com.blackbuild.klum.cast.validation.KlumCastTransformation' ]] || {
    echo 'The compiler JAR does not publish the KlumCast global AST transformation service.' >&2
    exit 1
}

package_list="$work_dir/packages.txt"
for jar in "$ANNOTATIONS_JAR" "$SPI_JAR" "$COMPILER_JAR"; do
    "$java_bin/jar" tf "$jar" | sed -n 's#^\(com/.*\)/[^/]*\.class$#\1#p' | tr / . | sort -u
done | tee "$package_list" | sort | uniq -d | grep . && {
    echo 'Published JARs contain a split package.' >&2
    exit 1
} || true

sort -u "$package_list" > "$work_dir/actual-packages.txt"
diff -u <(printf '%s\n' \
    com.blackbuild.klum.cast \
    com.blackbuild.klum.cast.checks \
    com.blackbuild.klum.cast.checks.impl \
    com.blackbuild.klum.cast.compiler.internal.checks \
    com.blackbuild.klum.cast.spi \
    com.blackbuild.klum.cast.validation) "$work_dir/actual-packages.txt"

classes="$work_dir/classpath-classes"
mkdir -p "$classes"
find "$fixture_dir/src" -name '*.java' ! -name 'module-info.java' -print0 | \
    xargs -0 "$java_bin/javac" -cp "$classpath" -d "$classes"

classpath_output="$("$java_bin/java" -cp "$classpath:$classes" org.codehaus.groovy.tools.FileSystemCompiler -d "$classes" "$fixture_dir/src/fixture/use/ConsumerTarget.groovy" 2>&1)"
printf '%s\n' "$classpath_output"
grep -F 'TYPED-CHECK-RAN' <<<"$classpath_output"
grep -F 'NAME-CHECK-RAN' <<<"$classpath_output"

module_description="$work_dir/groovy-module.txt"
"$java_bin/jar" --describe-module --file "$GROOVY_JAR" > "$module_description" 2>&1 || true
if ! grep -Eq '^(org\.codehaus\.groovy|org\.apache\.groovy)@' "$module_description"; then
    echo "Groovy $GROOVY_VERSION is not usable on the module path; classpath behavior above remains supported."
    cat "$module_description"
    exit 0
fi

groovy_module="$(grep -E '^(org\.codehaus\.groovy|org\.apache\.groovy)@' "$module_description" | head -n 1 | sed 's/@.*//')"
case "$GROOVY_VERSION" in
    3.*) [[ "$groovy_module" == org.codehaus.groovy ]] ;;
    4.*|5.*) [[ "$groovy_module" == org.apache.groovy ]] ;;
    *) echo "Unexpected module-path-success Groovy lane: $GROOVY_VERSION" >&2; exit 1 ;;
esac

module_sources="$work_dir/module-src"
modules="$work_dir/modules"
mkdir -p "$module_sources"
cp -R "$fixture_dir/src/consumer.named.implementation" "$fixture_dir/src/consumer.named.metadata" \
      "$fixture_dir/src/consumer.typed" "$module_sources/"
for module_info in "$module_sources"/*/module-info.java; do
    sed "s/@GROOVY_MODULE@/$groovy_module/g" "$module_info" > "$module_info.tmp"
    mv "$module_info.tmp" "$module_info"
done
"$java_bin/javac" --module-path "$module_path" --module-source-path "$module_sources" -d "$modules" \
    --module consumer.typed,consumer.named.metadata,consumer.named.implementation

module_output="$("$java_bin/java" --module-path "$module_path:$modules" --add-modules ALL-MODULE-PATH \
    -m "$groovy_module/org.codehaus.groovy.tools.FileSystemCompiler" -d "$modules" \
    "$fixture_dir/src/fixture/use/ConsumerTarget.groovy" 2>&1)"
printf '%s\n' "$module_output"
grep -F 'TYPED-CHECK-RAN' <<<"$module_output"
grep -F 'NAME-CHECK-RAN' <<<"$module_output"
echo "Module-path feasibility passed for Groovy $GROOVY_VERSION using $groovy_module."
