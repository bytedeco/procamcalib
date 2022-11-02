@echo off

rem PGRFlyCapture and OpenCV, both compiled with Intel's Compiler,
rem do not play nice with each other...
set KMP_DUPLICATE_LIB_OK=TRUE

rem Under Windows, Java uses DirectX by default, but your video driver 
rem might work better with OpenGL than DirectX. Try to use this option...
rem set OPTIONS=-Dsun.java2d.opengl=True

rem Required options for JDK 17+ since not yet supported by NetBeans
rem set "OPTIONS=--add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.desktop/javax.swing.plaf.synth=ALL-UNNAMED"

set JAVA=%SystemRoot%\system32\java
if exist %JAVA%.exe goto CHECKSERVER
set JAVA=java

:CHECKSERVER
%JAVA% -server -version
if errorlevel 1 goto DEFAULT

:SERVER
start %JAVA%w -server %OPTIONS% -jar "%~dp0\modules\procamcalib.jar" --laf javax.swing.plaf.metal.MetalLookAndFeel %*
if errorlevel 1 goto PAUSE
goto END

:DEFAULT
echo WARNING: Server JVM not available. Executing with default JVM...
%JAVA%w %OPTIONS% -jar "%~dp0\modules\procamcalib.jar" --laf javax.swing.plaf.metal.MetalLookAndFeel %*
if errorlevel 1 goto PAUSE
goto END

:PAUSE
pause
:END
