@echo off
title fuck
chcp 65001
java ^
 -agentlib:native-image-agent=config-output-dir=reflect,config-write-period-secs=20,config-write-initial-delay-secs=5 ^
 -XX:+UnlockExperimentalVMOptions ^
 -XX:+EnableJVMCI ^
 -Dpolyglotimpl.TraceClassPathIsolation=true ^
 -Dpolyglotimpl.DisableClassPathIsolation=true ^
 -verbose:classloader ^
 -Xms3000m ^
 -Xmx5000m ^
 -server ^
 -jar target\twms.jar --js.nashorn-compat=true ^
pause
