package ru.shutoff.messenger.domain.file_handling.configuration;

import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.mock.mockito.MockBean;

@org.springframework.boot.test.context.TestConfiguration
public class TestConfiguration {
    @MockBean
    private RabbitAdmin rabbitAdmin;
    @MockBean
    private RabbitTemplate rabbitTemplate;
}
