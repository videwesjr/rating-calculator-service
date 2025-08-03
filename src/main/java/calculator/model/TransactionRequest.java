package calculator.model;

public record TransactionRequest(double total_amount, String cpf, CreditCard cc) {}
