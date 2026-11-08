#!/usr/bin/env bash
# Verified-here build for pwhitelist.
# The canonical build is Gradle (./gradlew shadowJar); this script is the
# fallback that works in this sandbox, where Maven/Gradle downloads get
# connection-reset through the egress relay. It compiles with javac against
# curl-fetched API jars and hand-assembles the jar, shading toml4j in.
set -euo pipefail
cd "$(dirname "$0")"

DEPS="$HOME/workspace/.deps/pwhitelist"
API="$HOME/workspace/.deps/paper-1.20.4"
mkdir -p "$DEPS" build/classes build/jar

fetch() { # fetch <url> <dest>
  if [ ! -s "$2" ]; then
    echo "Fetching $(basename "$2")..."
    curl -sfL --max-time 120 "$1" -o "$2"
  fi
}

fetch "https://repo.maven.apache.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar" "$DEPS/gson-2.10.1.jar"
fetch "https://repo.maven.apache.org/maven2/com/moandjiezana/toml/toml4j/0.7.2/toml4j-0.7.2.jar" "$DEPS/toml4j-0.7.2.jar"
fetch "https://repo.maven.apache.org/maven2/net/kyori/adventure-text-serializer-legacy/4.16.0/adventure-text-serializer-legacy-4.16.0.jar" "$DEPS/adventure-text-serializer-legacy-4.16.0.jar"
fetch "https://repo.maven.apache.org/maven2/net/kyori/examination-api/1.3.0/examination-api-1.3.0.jar" "$DEPS/examination-api-1.3.0.jar"

CP="$API/paper-api-1.20.4.jar:$API/adventure-api-4.16.0.jar:$API/adventure-key-4.16.0.jar:$API/bungeecord-chat-1.20.jar:$DEPS/adventure-text-serializer-legacy-4.16.0.jar:$DEPS/examination-api-1.3.0.jar:$DEPS/gson-2.10.1.jar:$DEPS/toml4j-0.7.2.jar"

find src/main/java -name '*.java' > build/sources.txt
javac --release 17 -cp "$CP" -d build/classes @build/sources.txt

rm -rf build/jar && mkdir -p build/jar
cp -r build/classes/* build/jar/
cp src/main/resources/plugin.yml build/jar/
cp src/main/resources/config.toml build/jar/
# Shade toml4j (not provided by Paper); Gson and Adventure come from the server.
(cd build/jar && unzip -qo "$DEPS/toml4j-0.7.2.jar" -x 'META-INF/*')

OUT="pwhitelist-1.0.0.jar"
(cd build/jar && jar --create --file "../../$OUT" .)
echo "Built $OUT"
sha256sum "$OUT"
