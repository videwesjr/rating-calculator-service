package calculator.service;

import calculator.model.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RatingCalculatorService {

    private static final String CPF_RATING_API = "http://localhost:3000/cpf_query";

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Connection connection;

    public RatingCalculatorService(Connection connection) {
        this.connection = connection;
    }

    public String calculateRating(String transactionJson) throws Exception {
        var request = mapper.readValue(transactionJson, TransactionRequest.class);

        var cpfRating = fetchCpfRating(request.cpf());
        var ccScore = calculateCcScore(request.cc().fullKey());

        var features = new TransactionFeatures(
                request.total_amount(),
                cpfRating,
                ccScore
        );

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(features);
    }

    private Double fetchCpfRating(String cpf) {
        try {
            var uri = URI.create(CPF_RATING_API.concat("?cpf=").concat(cpf));
            var httpRequest = HttpRequest.newBuilder(uri).GET().build();
            var response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            var result = mapper.readValue(response.body(), CpfRatingResult.class);

            return switch (result.rating()) {
                case "good" -> 0.0;
                case "bad" -> 1.0;
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

    private double calculateCcScore(String ccKey) throws Exception {
        var sql = """
            SELECT SUM(CASE status
                       WHEN 'approved' THEN -1.0
                       WHEN 'pending' THEN  0.0
                       WHEN 'declined' THEN 5.0
                       ELSE 0.0 END) AS score
              FROM credit_card_transactions t
              JOIN transaction_status s ON t.transaction_id = s.transaction_id
             WHERE t.credit_card = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, ccKey);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getDouble("score") : 0.0;
            }
        }
    }
}
