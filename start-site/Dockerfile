FROM openjdk:17-slim

# Copia o jar que está no target para a pasta do app e executa
COPY ./target/start-site-exec.jar /usr/app/
WORKDIR /usr/app
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "start-site-exec.jar"]