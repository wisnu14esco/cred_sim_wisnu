package com.wisnu.creditsimulator.client;

import com.wisnu.creditsimulator.json.JsonException;
import com.wisnu.creditsimulator.json.LoanApplicationJson;
import com.wisnu.creditsimulator.model.LoanApplication;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

/**
 * Fetches a loan application from a JSON web service using the JDK.
 */
public class HttpLoanApplicationClient implements LoanApplicationClient {

    public static final URI DEFAULT_URI =
            URI.create("https://run.mocky.io/v3/9108b1da-beec-409e-ae14-e8091955666c");
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient httpClient;
    private final URI uri;
    private final Duration timeout;

    public HttpLoanApplicationClient(URI uri) {
        this(HttpClient.newBuilder()
                        .connectTimeout(DEFAULT_TIMEOUT)
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .build(),
                uri, DEFAULT_TIMEOUT);
    }

    public HttpLoanApplicationClient(HttpClient httpClient, URI uri, Duration timeout) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.uri = Objects.requireNonNull(uri, "uri");
        this.timeout = Objects.requireNonNull(timeout, "timeout");
    }

    public URI uri() {
        return uri;
    }

    @Override
    public LoanApplication fetch() {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(timeout)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (HttpTimeoutException e) {
            throw new LoanServiceException("Web service tidak merespons dalam " + timeout.toSeconds() + " detik", e);
        } catch (IOException e) {
            throw new LoanServiceException("Gagal menghubungi web service " + uri + " (" + describe(e) + ")", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LoanServiceException("Permintaan ke web service dibatalkan", e);
        }

        if (response.statusCode() != 200) {
            throw new LoanServiceException("Web service mengembalikan HTTP " + response.statusCode());
        }
        try {
            return LoanApplicationJson.fromJson(response.body());
        } catch (JsonException | IllegalArgumentException e) {
            throw new LoanServiceException("Respons web service tidak valid: " + e.getMessage(), e);
        }
    }

    private static String describe(IOException e) {
        return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
    }
}
