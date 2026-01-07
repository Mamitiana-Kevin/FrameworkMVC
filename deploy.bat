@echo off
echo ============================================
echo    BUILDING FRAMEWORK.JAR + COPY TO TEST
echo ============================================

rem CHANGE THESE PATHS TO YOUR REAL ONES
set TOMCAT_LIB=jakarta.servlet-api_5.0.0.jar
set FRAMEWORK_SRC=framework
set BUILD_DIR=build\classes
set JAR_OUTPUT=build\framework.jar
set TEST_WEBAPP=C:\Users\Mamitiana\Documents\GitHub\Test\src\main\webapp\WEB-INF\lib

echo 1. Cleaning old build...
if exist build rmdir /s /q build
if exist %TEST_WEBAPP%\framework.jar del %TEST_WEBAPP%\framework.jar

echo 2. Creating directories...
mkdir %BUILD_DIR% 2>nul

echo 3. Compiling framework sources...
javac -classpath "%TOMCAT_LIB%" -d "%BUILD_DIR%" ^
    %FRAMEWORK_SRC%\annotation\*.java ^
    %FRAMEWORK_SRC%\core\*.java ^
    %FRAMEWORK_SRC%\servlet\*.java ^
    %FRAMEWORK_SRC%\utils\*.java ^
    %FRAMEWORK_SRC%\filter\*.java

if %errorlevel% neq 0 (
    echo.
    echo ERROR: COMPILATION FAILED!
    pause
    exit /b 1
)

echo 4. Creating JAR...
cd build
jar cf framework.jar -C classes .
cd ..

echo 5. Copying JAR to test project...
mkdir %TEST_WEBAPP% 2>nul
copy build\framework.jar %TEST_WEBAPP%\

echo 6. Content of the JAR:
echo -------------------- JAR CONTENT --------------------
jar tf %TEST_WEBAPP%\framework.jar
echo ----------------------------------------------------

echo.
echo SUCCESS! Framework rebuilt and copied!
echo.
pause