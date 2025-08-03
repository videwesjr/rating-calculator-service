package calculator.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransactionFeatures(
        double TOTAL_AMOUNT,
        Double CPF_RATING,
        double CC_SCORE
) {}
