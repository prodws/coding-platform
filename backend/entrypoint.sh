#!/bin/sh
javac -cp /opt/junit/junit-platform-console-standalone.jar *.java && {
  TMPFILE=$(mktemp)
  java -jar /opt/junit/junit-platform-console-standalone.jar \
    execute \
    --class-path . \
    --scan-class-path \
    --disable-banner \
    --details=none > "$TMPFILE" 2>&1
  EXIT_CODE=$?
  grep -v '^\s*\[.*\]\s*$' "$TMPFILE"
  rm "$TMPFILE"
  exit $EXIT_CODE
}
