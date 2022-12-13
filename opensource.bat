@echo off

rem *********************************************************************
rem *** A script used for extracting the code for opensourced part of ***
rem *** the Headwind MDM part into separate directory                 ***
rem *** Requires WinBash to be installed                              ***
rem *********************************************************************

set TARGET_DIR=target\opensource

echo Creating target directory...
mkdir %TARGET_DIR%

echo Preparing ROOT module...
awk !/license-server/ pom.xml > %TARGET_DIR%\pom.xml

rem cp --parents -v pom.xml %TARGET_DIR%

echo Preparing COMMON module...
mkdir %TARGET_DIR%\common
cp --parents -v common\pom.xml %TARGET_DIR%
cp --parents -vr common\src\ %TARGET_DIR%

echo Preparing JWT module...
mkdir %TARGET_DIR%\jwt
cp --parents -v jwt\pom.xml %TARGET_DIR%
cp --parents -vr jwt\src\ %TARGET_DIR%

echo Preparing NOTIFICATION module...
mkdir %TARGET_DIR%\notification
cp --parents -v notification\pom.xml %TARGET_DIR%
cp --parents -vr notification\src\ %TARGET_DIR%

echo Preparing PLUGINS module...
mkdir %TARGET_DIR%\plugins
mkdir %TARGET_DIR%\plugins\platform

awk !/photo/ plugins\pom.xml | awk !/licensing/ | awk !/contacts/ | awk !/deviceimport/ | awk !/deviceexport/ | awk !/devicereset/ | awk !/deviceinfo/ | awk !/devicelocations/ | awk !/knox/ > %TARGET_DIR%\plugins\pom.xml

cp --parents -v plugins\platform\pom.xml %TARGET_DIR%
cp --parents -vr plugins\platform\src\ %TARGET_DIR%
cp --parents -vr plugins\platform\local-maven-repo\ %TARGET_DIR%

echo Preparing DEVICE LOG PLUGIN module...
mkdir %TARGET_DIR%\plugins\devicelog
mkdir %TARGET_DIR%\plugins\devicelog\core
mkdir %TARGET_DIR%\plugins\devicelog\postgres

cp --parents -v plugins\devicelog\pom.xml %TARGET_DIR%
cp --parents -vr plugins\devicelog\src\ %TARGET_DIR%
cp --parents -v plugins\devicelog\core\pom.xml %TARGET_DIR%
cp --parents -vr plugins\devicelog\core\src\ %TARGET_DIR%
cp --parents -v plugins\devicelog\postgres\pom.xml %TARGET_DIR%
cp --parents -vr plugins\devicelog\postgres\src\ %TARGET_DIR%

echo Preparing AUDIT PLUGIN module...
mkdir %TARGET_DIR%\plugins\audit

cp --parents -v plugins\audit\pom.xml %TARGET_DIR%
cp --parents -vr plugins\audit\src\ %TARGET_DIR%

echo Preparing MESSAGING PLUGIN module...
mkdir %TARGET_DIR%\plugins\messaging

cp --parents -v plugins\messaging\pom.xml %TARGET_DIR%
cp --parents -vr plugins\messaging\src\ %TARGET_DIR%

echo Preparing SERVER module...
mkdir %TARGET_DIR%\server
mkdir %TARGET_DIR%\server\webtarget

awk !/photo/ server\pom.xml | awk !/licensing/ | awk !/contacts/ | awk !/deviceimport/ | awk !/deviceexport/ | awk !/deviceinfo/ | awk !/devicereset/ | awk !/devicelocations/ | awk !/knox/ > %TARGET_DIR%\server\pom.xml

cp --parents -vr server\src\ %TARGET_DIR%
cp --parents -vr server\conf\ %TARGET_DIR%

awk !/license/ server\conf\context.xml > %TARGET_DIR%\server\conf\context.xml

cp --parents -vr server\webtarget\bower.json %TARGET_DIR%
cp --parents -vr server\webtarget\Gruntfile.js %TARGET_DIR%
cp --parents -vr server\webtarget\package.json %TARGET_DIR%
cp --parents -vr server\webtarget\package-lock.json %TARGET_DIR%

awk !/photo/ server\build.properties | awk !/licensing/ | awk !/license/ | awk !/deviceimport/ | awk !/deviceexport/ | awk !/deviceinfo/ | awk !/devicereset/ | awk !/devicelocations/ | awk !/knox/ > %TARGET_DIR%\server\build.properties

rmdir %TARGET_DIR%\server\src\main\webapp\app\components\plugins\photo /s /q
rmdir %TARGET_DIR%\server\src\main\webapp\app\components\plugins\deviceimport /s /q
rmdir %TARGET_DIR%\server\src\main\webapp\app\components\plugins\deviceexport /s /q
rmdir %TARGET_DIR%\server\src\main\webapp\app\components\plugins\licensing /s /q
rmdir %TARGET_DIR%\server\src\main\webapp\app\components\plugins\deviceinfo /s /q
rmdir %TARGET_DIR%\server\src\main\webapp\app\components\plugins\devicereset /s /q
rmdir %TARGET_DIR%\server\src\main\webapp\app\components\plugins\devicelocations /s /q

echo Preparing SWAGGER module...
mkdir %TARGET_DIR%\swagger
mkdir %TARGET_DIR%\swagger\ui

cp --parents -vr swagger\ui\src\ %TARGET_DIR%
cp --parents -v swagger\ui\favicon-16x16.png %TARGET_DIR%
cp --parents -v swagger\ui\favicon-32x32.png %TARGET_DIR%
cp --parents -v swagger\ui\index.html %TARGET_DIR%
cp --parents -v swagger\ui\oauth2-redirect.html %TARGET_DIR%
cp --parents -v swagger\ui\swagger-ui.js %TARGET_DIR%
cp --parents -v swagger\ui\swagger-ui-bundle.js %TARGET_DIR%
cp --parents -v swagger\ui\swagger-ui-standalone-preset.js %TARGET_DIR%
cp --parents -v swagger\ui\swagger-ui.css %TARGET_DIR%
cp --parents -v swagger\ui\pom.xml %TARGET_DIR%

echo Preparing INSTALLATOR ...
mkdir %TARGET_DIR%\install
mkdir %TARGET_DIR%\install\sql

cp --parents -v BUILD.txt %TARGET_DIR%
cp --parents -v hmdm_install.sh %TARGET_DIR%
cp --parents -v LICENSE %TARGET_DIR%
cp --parents -v README.md %TARGET_DIR%
cp --parents -v TROUBLESHOOTING.txt %TARGET_DIR%
cp --parents -v NOTICE %TARGET_DIR%
cp --parents -vr install %TARGET_DIR%



