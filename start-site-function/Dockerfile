FROM public.ecr.aws/amazoncorretto/amazoncorretto:17.0.4-al2

ENV LANG=en_US.UTF-8
ENV TZ=:/etc/localtime
ENV PATH=/opt/java/openjdk/bin:/usr/local/bin:/usr/bin:/bin:/opt/bin
ENV LD_LIBRARY_PATH=/lib:/usr/lib:/var/runtime:/var/runtime/lib:/var/task:/var/task/lib:/opt/lib
ENV LAMBDA_TASK_ROOT=/var/task
ENV LAMBDA_RUNTIME_DIR=/var/runtime
COPY target/start-site-function-0.0.1-SNAPSHOT-aws.jar "${LAMBDA_TASK_ROOT}/lib/"

ENTRYPOINT [ "/usr/bin/java", "-Dspring.main.web-application-type=none", "--class-path", "/var/runtime/lib/*:/var/task/lib/*:/var/task/", "--add-opens", "java.base/java.util=ALL-UNNAMED", "io.spring.start.site.FunctionConfiguration" ]

CMD [ "hello.handler" ]