package ru.rsu.core.service.impl;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ru.rsu.core.model.entity.Receipt;
import ru.rsu.core.model.enums.ReceiptStatuses;
import ru.rsu.core.repository.ReceiptRepository;
import ru.rsu.core.service.ReceiptService;
import ru.rsuog.model.PaymentInfo;

@Service
@Transactional(readOnly = true)
public class ReceiptServiceImpl implements ReceiptService {
  private final ReceiptRepository receiptRepository;
  private static final Logger logger = LoggerFactory.getLogger(ReceiptServiceImpl.class);

  public ReceiptServiceImpl(ReceiptRepository receiptRepository) {
    this.receiptRepository = receiptRepository;
  }

  @Override
  public List<Receipt> getAllReceipt() {
    return receiptRepository.findAll();
  }

  @Override
  public Optional<Receipt> getReceiptById(long id) {
    return receiptRepository.findById(id);
  }

  @Override
  @Transactional
  public Receipt addReceipt(Receipt receipt) {
    return receiptRepository.save(receipt);
  }

  @Override
  @Transactional
  public void removeReceiptById(long id) {
    receiptRepository.deleteById(id);
  }

  @Override
  @Transactional
  public PaymentInfo updateReceipt(PaymentInfo paymentInfo) throws Exception {
    Optional<Receipt> receiptOptional = receiptRepository.findById(paymentInfo.getReceiptId());
    Receipt receiptForUpdate;
    try {
      if (receiptOptional.isEmpty()) {
        String errorMsg = "Receipt with id: " + paymentInfo.getReceiptId()
            + " doesn't exist";
        logger.error(HttpStatus.BAD_REQUEST.toString() + " " + errorMsg);

        paymentInfo.setStatusCode(400);
        paymentInfo.setErrorMsg(errorMsg);
        return paymentInfo;
      } else {
        receiptForUpdate = receiptOptional.get();
      }
    } catch (Exception ex) {
      logger.error(ex.getMessage());
      paymentInfo.setStatusCode(500);
      paymentInfo.setErrorMsg(ex.getMessage());
      return paymentInfo;
    }

    if (!paymentInfo.isDeleted()) {
      if (paymentInfo.getAmount() > 0) {
        if (receiptForUpdate.getActiveAmount() > 0) {
          receiptForUpdate.setActiveAmount(
              receiptForUpdate.getActiveAmount() - paymentInfo.getAmount());
        }
      }
    } else {
      receiptForUpdate.setActiveAmount(
          receiptForUpdate.getActiveAmount() + paymentInfo.getAmount());
    }

    checkAndUpdateReceipt(receiptForUpdate);
    logger.info("Receipt with id: " + receiptForUpdate.getId() + "was updated. New data: "
                    + receiptForUpdate.toString());
    addReceipt(receiptForUpdate);

    return paymentInfo;
  }

  @Override
  public void checkAndUpdateReceipt(Receipt receipt) {
    if (receipt.getActiveAmount() < receipt.getDebtAmount() && receipt.getActiveAmount() > 0) {
      receipt.setReceiptStatus(ReceiptStatuses.partPaid.getReceiptStatusValue());
    } else if (receipt.getActiveAmount() <= 0) {
      receipt.setReceiptStatus(ReceiptStatuses.fullPaid.getReceiptStatusValue());
    } else if (receipt.getActiveAmount() >= receipt.getDebtAmount()) {
      receipt.setReceiptStatus(ReceiptStatuses.notPaid.getReceiptStatusValue());
    }
  }
}
