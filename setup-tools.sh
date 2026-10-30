#!/usr/bin/env bash
set -euo pipefail
mkdir -p /workspace/tools /workspace/downloads
fetch() {
 local url="$1" file="$2" checksum="$3"
 curl -fL "$url" -o "$file"
 printf '%s  %s\n' "$checksum" "$file" | sha1sum -c -
}
fetch https://dl.google.com/android/repository/platform-35_r02.zip /workspace/downloads/platform.zip 0bb560a90a7a2cbd0dd8348224d518b638fe7949
fetch https://dl.google.com/android/repository/build-tools_r35_linux.zip /workspace/downloads/build-tools.zip 2cfaa0bbb2336e9ec18ed3ecea84fa2e2af607bc
unzip -q -o /workspace/downloads/platform.zip -d /workspace/tools
unzip -q -o /workspace/downloads/build-tools.zip -d /workspace/tools
curl -fL https://repo.maven.apache.org/maven2/org/eclipse/jdt/ecj/3.40.0/ecj-3.40.0.jar -o /workspace/tools/ecj.jar
curl -fL https://repo.maven.apache.org/maven2/org/eclipse/jdt/ecj/3.40.0/ecj-3.40.0.jar.sha1 -o /workspace/downloads/ecj.sha1
python3 - <<'PY'
import hashlib
assert hashlib.sha1(open('/workspace/tools/ecj.jar','rb').read()).hexdigest()==open('/workspace/downloads/ecj.sha1').read().strip()
PY
