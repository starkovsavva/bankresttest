# Этап 1: Сборка проекта с использованием Maven
FROM maven:3.8.5-openjdk-17 AS build

# Установка рабочей директории
WORKDIR /app

# Копирование pom.xml для кэширования зависимостей
COPY pom.xml .

# Загрузка зависимостей
RUN mvn dependency:install -DskipTests

# Копирование исходного кода
COPY src ./src

# Сборка проекта
RUN mvn package -DskipTests

# Этап 2: Создание окончательного образа
FROM openjdk:17-jdk-slim

# Установка рабочей директории
WORKDIR /app

# Копирование JAR файла из этапа сборки
COPY --from=build /app/target/bank-card-management-1.0.0.jar app.jar

# Создание не-root пользователя для безопасности
RUN groupadd -r spring && useradd -r -g spring spring
USER spring

# Открытие порта
EXPOSE 8080

# Запуск приложения
ENTRYPOINT ["java", "-jar", "/app/app.jar"]