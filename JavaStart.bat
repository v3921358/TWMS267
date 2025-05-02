@echo off
title [ StartServer ] Login / Channel / Cash 
chcp 65001
java ^
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
