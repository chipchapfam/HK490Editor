for %%I in ("%cd%\..") do set "root=%%~fI"
ren %root%\lib\*.jar *.ar
ren %root%\*.jar *.ar