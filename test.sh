#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p build/tests
java -jar /workspace/tools/ecj.jar -8 -encoding UTF-8 -d build/tests app/src/main/java/ru/mozhno/budget/BudgetMath.java app/src/main/java/ru/mozhno/budget/DateText.java tests/BudgetMathTest.java
java -cp build/tests ru.mozhno.budget.BudgetMathTest
