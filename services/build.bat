@echo off

echo Building company-service...
cd company-service\demo
call mvn clean package
if %errorlevel% neq 0 (
    echo Build failed for company-service!
    pause
    exit /b %errorlevel%
)
cd ../..

echo Building vacancy-service...
cd vacancy-service\demo
call mvn clean package
if %errorlevel% neq 0 (
    echo Build failed for vacancy-service!
    pause
    exit /b %errorlevel%
)
cd ../..

echo Building Docker images...
docker-compose build
if %errorlevel% neq 0 (
    echo Docker build failed!
    pause
    exit /b %errorlevel%
)

echo Starting containers...
docker-compose up -d
if %errorlevel% neq 0 (
    echo Failed to start containers!
    pause
    exit /b %errorlevel%
)

echo All services are up and running!
pause