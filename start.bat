@echo off
cd /d D:\Java_IDEA\HeTongGuanLiXitong
start "" "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.3.4.1\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run
echo 正在启动应用，请等待...
:wait
timeout /t 2 /nobreak >nul
powershell -Command "try { $r = Invoke-WebRequest -Uri http://localhost:8080/login -UseBasicParsing -TimeoutSec 2; exit 0 } catch { exit 1 }" >nul 2>&1
if errorlevel 1 goto wait
start http://localhost:8080/login
