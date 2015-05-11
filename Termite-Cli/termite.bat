@echo off

if "%TERMITE_CLI_PATH%"=="" (
    echo "Error: environment variable TERMITE_CLI_PATH undefined."
	goto end
)

set jline=%TERMITE_CLI_PATH%\libs\jline-2.13.jar
set groovy=%TERMITE_CLI_PATH%\libs\groovy-all-2.3.6.jar
set termite=%TERMITE_CLI_PATH%\libs\Termite-Cli.jar
set deps="%jline%;%groovy%;%termite%"
java -cp %deps% pt.inesc.termite.cli.Main %*

:end

