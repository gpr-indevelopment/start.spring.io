FROM openjdk:17-slim

RUN apt-get update -y && apt-get install -y \
  strace \
  htop

# Copia o jar que está no target para a pasta do app e executa
COPY ./target/start-site-exec.jar /usr/app/
COPY ./strace.sh /usr/app/
RUN chmod +x /usr/app/strace.sh
WORKDIR /usr/app
EXPOSE 8081
CMD ["/bin/bash"]
ENTRYPOINT ["java", "-jar", "start-site-exec.jar"]