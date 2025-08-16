@echo off
echo Starting Exam Application...
echo.
echo Please make sure MySQL is running and the database 'exam' exists.
echo.
echo Database connection details:
echo - Host: localhost
echo - Port: 3306
echo - Database: exam
echo - Username: root
echo - Password: 123456
echo.
echo Press any key to start the application...
pause > nul

mvn spring-boot:run

pause
