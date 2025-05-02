@echo off
chcp 65001
color 0B

set "BUILD_NAME=twms"
set "PROJECT_BUILD_DIRECTORY=target"

native-image ^
  -H:+UnlockExperimentalVMOptions ^
  -J-Xms32g ^
  -J-Xmx48g ^
  -H:Class=Net.NetRun ^
  -H:Name=%BUILD_NAME% ^
  -H:+AddAllCharsets ^
  -march=x86-64-v3 ^
  -Ob ^
  --no-fallback ^
  -Ob ^
  --initialize-at-build-time=org.slf4j,ch.qos.logback ^
  --initialize-at-run-time=io.netty ^
  -H:Path=%PROJECT_BUILD_DIRECTORY%/native ^
  -jar %PROJECT_BUILD_DIRECTORY%\%BUILD_NAME%.jar

pause
