@echo off
setlocal
cd /d "%~dp0\..\.."
where java >nul 2>nul || (
  echo pre-push: java was not found on PATH 1>&2
  exit /b 1
)
if not exist ".worldline\gate\classes\PrePushCheck.class" (
  java tools\harness\Gate.java || exit /b 1
  set WORLDLINE_PREPUSH_BOOTSTRAPPED=1
)
java -cp ".worldline\gate\classes" PrePushCheck
exit /b %ERRORLEVEL%
