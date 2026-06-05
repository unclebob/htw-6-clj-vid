#!/usr/bin/env bash
set -euo pipefail

parser="${PWD}/tmp/bin/gherkin-parser"
if [[ ! -x "$parser" ]]; then
  parser="$(command -v gherkin-parser)"
fi

rm -rf build/acceptance
mkdir -p build/acceptance/ir build/acceptance/generated

for feature in features/domain/*.feature; do
  name="$(basename "$feature" .feature)"
  ir="build/acceptance/ir/${name}.json"
  "$parser" "$feature" "$ir"
  ./scripts/acceptance-entrypoint-generator "$ir" build/acceptance/generated
done

for generated in build/acceptance/generated/*_acceptance_test.clj; do
  bb "$generated"
done
