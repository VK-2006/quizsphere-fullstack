@echo off
setlocal EnableExtensions
set "ROOT=%~dp0.."
pushd "%ROOT%\frontend"
if not exist node_modules call npm ci --include=optional
call npm run dev
popd
