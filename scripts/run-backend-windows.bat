@echo off
setlocal EnableExtensions
set "ROOT=%~dp0.."
pushd "%ROOT%\backend"
call mvn spring-boot:run
popd
