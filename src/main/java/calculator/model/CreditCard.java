package calculator.model;

public record CreditCard(String bin, String last4) {
    public String fullKey() {
        return bin + "-" + last4;
    }
}
