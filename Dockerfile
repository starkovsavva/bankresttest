FROM openjdk:17-jdk-slim

# Установка рабочей директории
WORKDIR /app

# Копирование конкретного JAR файла
COPY target/bank-card-management-1.0.0.jar app.jar

# Создание не-root пользователя для безопасности
RUN groupadd -r spring && useradd -r -g spring spring
USER spring

# Открытие порта
EXPOSE 8080

# Запуск приложения
ENTRYPOINT ["java", "-jar", "/app/app.jar"]