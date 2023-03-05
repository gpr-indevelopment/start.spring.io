FROM public.ecr.aws/lambda/nodejs:18

RUN yum -y install fio

COPY executor.js  ${LAMBDA_TASK_ROOT}/

CMD [ "executor.handler" ]