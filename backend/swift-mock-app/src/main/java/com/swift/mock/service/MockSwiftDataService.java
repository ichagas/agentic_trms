package com.swift.mock.service;

import com.swift.mock.config.SwiftProperties;
import com.swift.mock.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mock data service for SWIFT messages and operations
 * Simulates in-memory storage of SWIFT messages, settlements, and payments
 */
@Service
public class MockSwiftDataService {

    private static final Logger logger = LoggerFactory.getLogger(MockSwiftDataService.class);

    private final SwiftProperties swiftProperties;
    private final Map<String, SwiftMessage> messages = new ConcurrentHashMap<>();
    private final Map<String, Settlement> settlements = new ConcurrentHashMap<>();
    private final Map<String, Payment> payments = new ConcurrentHashMap<>();
    private final Map<String, Confirmation> confirmations = new ConcurrentHashMap<>();
    private final AtomicInteger messageCounter = new AtomicInteger(1);
    private final AtomicInteger settlementCounter = new AtomicInteger(1);
    private final AtomicInteger paymentCounter = new AtomicInteger(1);

    public MockSwiftDataService(SwiftProperties swiftProperties) {
        this.swiftProperties = swiftProperties;
        initializeMockData();
    }

    private void initializeMockData() {
        logger.info("Initializing mock SWIFT data...");

        // Create some sample SWIFT messages
        createSampleMessage("ACC-001-USD", "TRX-001", new BigDecimal("10000.00"), "USD", "TESTGB2LXXX");
        createSampleMessage("ACC-002-EUR", "TRX-002", new BigDecimal("50000.00"), "EUR", "TESTDE5FXXX");
        createSampleMessage("ACC-003-GBP", null, new BigDecimal("25000.00"), "GBP", "TESTFR21XXX");

        logger.info("Initialized {} SWIFT messages", messages.size());
    }

    private void createSampleMessage(String accountId, String transactionId, BigDecimal amount, String currency, String receiverBIC) {
        String messageId = generateMessageId();
        SwiftMessage message = SwiftMessage.builder()
                .id(messageId)
                .messageType("MT103")
                .senderBIC(swiftProperties.getOurBic())
                .receiverBIC(receiverBIC)
                .amount(amount)
                .currency(currency)
                .accountId(accountId)
                .transactionId(transactionId)
                .status(transactionId != null ? MessageStatus.RECONCILED : MessageStatus.SENT)
                .reference("REF-" + messageId)
                .valueDate(LocalDate.now())
                .sentTimestamp(LocalDateTime.now().minusHours(2))
                .confirmedTimestamp(LocalDateTime.now().minusHours(1))
                .beneficiaryName("Sample Beneficiary")
                .beneficiaryAccount("12345678")
                .orderingCustomer("Sample Customer")
                .rawMessageContent(generateMockMT103Content(messageId, amount, currency))
                .build();

        messages.put(messageId, message);
    }

    public String generateMessageId() {
        return String.format("SWIFT-MSG-%05d", messageCounter.getAndIncrement());
    }

    public String generateSettlementId() {
        return String.format("SETTLEMENT-%05d", settlementCounter.getAndIncrement());
    }

    public String generatePaymentId() {
        return String.format("PAYMENT-%05d", paymentCounter.getAndIncrement());
    }

    public SwiftMessage saveMessage(SwiftMessage message) {
        if (message.getId() == null || message.getId().isEmpty()) {
            message.setId(generateMessageId());
        }
        messages.put(message.getId(), message);
        logger.info("Saved SWIFT message: {}", message.getId());
        return message;
    }

    public Optional<SwiftMessage> getMessage(String messageId) {
        return Optional.ofNullable(messages.get(messageId));
    }

    public List<SwiftMessage> getAllMessages() {
        return new ArrayList<>(messages.values());
    }

    public List<SwiftMessage> getMessagesByAccount(String accountId) {
        return messages.values().stream()
                .filter(msg -> accountId.equals(msg.getAccountId()))
                .sorted(Comparator.comparing(SwiftMessage::getSentTimestamp).reversed())
                .toList();
    }

    public List<SwiftMessage> getMessagesByTransaction(String transactionId) {
        return messages.values().stream()
                .filter(msg -> transactionId.equals(msg.getTransactionId()))
                .toList();
    }

    public List<SwiftMessage> getUnreconciledMessages() {
        return messages.values().stream()
                .filter(msg -> msg.getStatus() == MessageStatus.UNRECONCILED ||
                              msg.getStatus() == MessageStatus.SENT)
                .sorted(Comparator.comparing(SwiftMessage::getSentTimestamp).reversed())
                .toList();
    }

    public List<SwiftMessage> getMessagesByStatus(MessageStatus status) {
        return messages.values().stream()
                .filter(msg -> msg.getStatus() == status)
                .toList();
    }

    public Settlement saveSettlement(Settlement settlement) {
        if (settlement.getId() == null || settlement.getId().isEmpty()) {
            settlement.setId(generateSettlementId());
        }
        settlements.put(settlement.getId(), settlement);
        logger.info("Saved settlement: {}", settlement.getId());
        return settlement;
    }

    public List<Settlement> getSettlementsByAccount(String accountId) {
        return settlements.values().stream()
                .filter(s -> accountId.equals(s.getAccountId()))
                .sorted(Comparator.comparing(Settlement::getSettlementDate).reversed())
                .toList();
    }

    public Payment savePayment(Payment payment) {
        if (payment.getId() == null || payment.getId().isEmpty()) {
            payment.setId(generatePaymentId());
        }
        payments.put(payment.getId(), payment);
        logger.info("Saved payment: {}", payment.getId());
        return payment;
    }

    public Optional<Payment> getPayment(String paymentId) {
        return Optional.ofNullable(payments.get(paymentId));
    }

    public Confirmation saveConfirmation(Confirmation confirmation) {
        confirmations.put(confirmation.getId(), confirmation);
        return confirmation;
    }

    private String generateMockMT103Content(String reference, BigDecimal amount, String currency) {
        return String.format("""
                {1:F01%s0000000000}
                {2:I103%sN}
                {4:
                :20:%s
                :23B:CRED
                :32A:%s%s%s
                :50K:ORDERING CUSTOMER
                :59:BENEFICIARY NAME
                /ACCOUNT NUMBER
                :71A:SHA
                -}
                """,
                swiftProperties.getOurBic(),
                swiftProperties.getDefaultReceiverBic(),
                reference,
                LocalDate.now().toString().replace("-", ""),
                currency,
                amount.toString()
        );
    }

    public void clearAllData() {
        messages.clear();
        settlements.clear();
        payments.clear();
        confirmations.clear();
        messageCounter.set(1);
        logger.info("Cleared all mock data");
    }
}
