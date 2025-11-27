@echo off
echo.
echo ===============================
echo   Motivise Testing Suite
echo ===============================

if "%1"=="coverage" (
    echo Running all tests with coverage report
    mvnw clean test jacoco:report
    echo.
    echo Coverage report generated!
    echo View report: target\site\jacoco\index.html
    echo.
    pause
    goto :end
)

if "%1"=="unit" (
    echo Running only unit tests ^(DTO + Service^)
    mvnw test -Dtest="**/dto/*Test,**/service/*Test"
    goto :end
)

if "%1"=="integration" (
    echo Running only integration tests
    mvnw test -Dtest="**/integration/*Test"
    goto :end
)

if "%1"=="specific" (
    if "%2"=="" (
        echo Usage: test.cmd specific TestClassName
        echo Example: test.cmd specific PostServiceTest
        goto :end
    )
    echo Running specific test: %2
    mvnw test -Dtest=%2
    goto :end
)

if "%1"=="quick" (
    echo Running quick test suite ^(no coverage^)
    mvnw test -DfailIfNoTests=false
    goto :end
)

if "%1"=="strict" (
    echo Running tests with strict validation
    mvnw test -Pstrict
    goto :end
)

if "%1"=="help" (
    goto :help
)

if "%1"=="" (
    echo Running all tests
    mvnw test
    goto :end
)

:help
echo.
echo Test Commands:
echo.
echo   test.cmd              - Run all tests
echo   test.cmd coverage     - Run tests + generate coverage report
echo   test.cmd unit         - Run only unit tests ^(DTO/Service^)
echo   test.cmd integration  - Run only integration tests
echo   test.cmd quick        - Fast test run ^(no coverage^)
echo   test.cmd strict       - Run with strict validation
echo   test.cmd specific TestClassName - Run specific test
echo   test.cmd help         - Show this help
echo.
echo Coverage report location: target\site\jacoco\index.html
echo Test reports: target\surefire-reports\
echo.

:end