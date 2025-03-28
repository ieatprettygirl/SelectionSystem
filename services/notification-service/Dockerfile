# Используем базовый образ OpenJDK
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем JAR файл в контейнер
COPY demo/target/notification-service.jar app.jar

# Открываем порт
EXPOSE 8081

# Указываем команду для запуска приложения
ENTRYPOINT ["java", "-jar", "app.jar"]