# Используем базовый образ OpenJDK
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем JAR файл в контейнер
COPY demo/target/vacancy-service.jar app.jar

# Открываем порт
EXPOSE 8084

# Указываем команду для запуска приложения
ENTRYPOINT ["java", "-jar", "app.jar"]