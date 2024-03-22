FROM rabbitmq:3.12.4-management-alpine
RUN rabbitmq-plugins enable rabbitmq_management
RUN rabbitmq-plugins enable rabbitmq_stomp