@echo off
chcp 65001 >nul
title 合同管理系统
echo 正在启动合同管理系统...
echo.

start http://localhost:8080
"D:\desktop\文件夹\Tools\apache-maven-3.9.11\bin\mvn.cmd" spring-boot:run
pause
