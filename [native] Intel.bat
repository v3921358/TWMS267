@echo off
chcp 65001
color 0B
native-image ^
  -H:+UnlockExperimentalVMOptions ^
  -H:Class=Net.NetRun ^
  -H:Name=twms ^
  -H:+AddAllCharsets ^
  -march=native ^
  --strict-image-heap ^
  -Ob ^
  --no-fallback ^
  --initialize-at-build-time=org.slf4j,ch.qos.logback ^
  --initialize-at-run-time=io.netty ^
-jar target\twms.jar ^
pause
