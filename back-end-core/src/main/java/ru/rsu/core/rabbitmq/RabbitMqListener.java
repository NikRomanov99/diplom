package ru.rsu.core.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ru.rsu.core.service.ReceiptService;
import ru.rsuog.model.PaymentInfo;

@EnableRabbit
@Component
public class RabbitMqListener {
  private final ReceiptService receiptService;
  private static final Logger logger = LoggerFactory.getLogger(ReceiptService.class);

  public RabbitMqListener(ReceiptService receiptService) {
    this.receiptService = receiptService;
  }

  @RabbitListener(queues = "paymentQueue")
  public String processPaymentQueue(String message) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    PaymentInfo paymentInfoFromRabbit;

    logger.info("New massage from RabbitMQ: " + message);

    try {
      paymentInfoFromRabbit = objectMapper.readValue(message, PaymentInfo.class );
    } catch (JsonProcessingException ex){
      PaymentInfo errorResponse = new PaymentInfo();
      errorResponse.setErrorMsg("Message from RabbitMQ does not match PaymentInfo format");
      errorResponse.setStatusCode(500);
      logger.error(errorResponse.getErrorMsg());

      return objectMapper.writeValueAsString(errorResponse);
    }

    return  objectMapper.writeValueAsString(receiptService.updateReceipt(paymentInfoFromRabbit));
  }
}
