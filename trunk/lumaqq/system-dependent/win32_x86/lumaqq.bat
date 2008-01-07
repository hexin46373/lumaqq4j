@echo off
set cp=
for %%i in (".\lib\*.jar") do call setenv.bat %%i

start javaw -Djava.library.path=swt -classpath %cp% edu.tsinghua.lumaqq.LumaQQ
