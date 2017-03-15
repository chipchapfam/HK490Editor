for %%I in ("%cd%\..") do set "root=%%~fI"
ren %root%\lib\*.ar *.jar
ren %root%\*.ar *.jar
java -jar %root%\hk490editor-0.0.1-SNAPSHOT-jfx.jar