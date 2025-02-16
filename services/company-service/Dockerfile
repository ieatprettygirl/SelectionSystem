# Используем базовый образ OpenJDK
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем JAR файл в контейнер
COPY demo/target/company-service.jar app.jar

# Открываем порт
EXPOSE 8083

# Указываем команду для запуска приложения
ENTRYPOINT ["java", "-jar", "app.jar"]