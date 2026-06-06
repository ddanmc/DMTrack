@echo off
echo Cerrando cualquier proceso en el puerto 9095...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :9095') do taskkill /F /PID %%a >nul 2>&1

echo Iniciando la aplicación Spring Boot...
start cmd /k ".\mvnw spring-boot:run"

echo Esperando que la aplicación arranque...
timeout /t 30 >nul

start http://localhost:9095/productos
exit



