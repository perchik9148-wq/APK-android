#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
TOOLS="${ANDROID_BUILD_TOOLS:-/workspace/tools/android-15}"
PLATFORM="${ANDROID_PLATFORM:-/workspace/tools/android-35/android.jar}"
mkdir -p build/{gen,classes,dex}
"$TOOLS/aapt" package -f -m -M app/src/main/AndroidManifest.xml -S app/src/main/res -I "$PLATFORM" -J build/gen -F build/resources.apk
find app/src/main/java build/gen -name '*.java' > build/sources.txt
java -jar /workspace/tools/ecj.jar -8 -encoding UTF-8 -classpath "$PLATFORM" -d build/classes @build/sources.txt
find build/classes -name '*.class' > build/classes.txt
"$TOOLS/d8" --lib "$PLATFORM" --min-api 26 --output build/dex @build/classes.txt
cp build/resources.apk build/unsigned.apk
(cd build/dex && zip -q -u ../unsigned.apk classes.dex)
"$TOOLS/zipalign" -f 4 build/unsigned.apk build/aligned.apk
# Local development key only. Keep this file to install updates over this build.
if [ ! -f build/debug.keystore ]; then
 keytool -genkeypair -keystore build/debug.keystore -storepass android -keypass android -alias androiddebugkey -dname 'CN=Android Debug' -keyalg RSA -keysize 2048 -validity 10000 >/dev/null 2>&1
fi
"$TOOLS/apksigner" sign --ks build/debug.keystore --ks-pass pass:android --key-pass pass:android --out build/mozhno-potratit.apk build/aligned.apk
"$TOOLS/apksigner" verify --verbose build/mozhno-potratit.apk
printf 'APK: %s/build/mozhno-potratit.apk\n' "$PWD"
