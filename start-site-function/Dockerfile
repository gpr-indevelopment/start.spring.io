FROM public.ecr.aws/aleph0io/lambda/java:17.0.4-al2

COPY target/start-site-function-0.0.1-SNAPSHOT-aws.jar "${LAMBDA_TASK_ROOT}/lib/"

CMD [ "org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest" ]