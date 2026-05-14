package ru.codecrafters.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ru.codecrafters.ai.web.dto.GenerateTaskRequest;
import ru.codecrafters.ai.web.dto.GeneratedTaskResponse;

@Component
public class GigachatTaskGenerationProvider implements TaskGenerationProvider {
    private static final Logger log = LoggerFactory.getLogger(GigachatTaskGenerationProvider.class);
    private static final long TOKEN_REFRESH_SKEW_MILLIS = 60_000L;
    private static final String SYSTEM_PROMPT = "Ты помощник преподавателя детской онлайн-школы программирования. Генерируй учебные задания строго в JSON.";

    private final String authKey;
    private final String scope;
    private final String authUrl;
    private final String apiUrl;
    private final String model;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final AssignmentPromptFactory promptFactory;

    private String cachedAccessToken;
    private long cachedExpiresAtMillis;

    public GigachatTaskGenerationProvider(
            @Value("${app.ai.gigachat.auth-key}") String authKey,
            @Value("${app.ai.gigachat.scope}") String scope,
            @Value("${app.ai.gigachat.auth-url}") String authUrl,
            @Value("${app.ai.gigachat.api-url}") String apiUrl,
            @Value("${app.ai.gigachat.model}") String model,
            @Value("${app.ai.gigachat.verify-ssl}") boolean verifySsl,
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            AssignmentPromptFactory promptFactory
    ) {
        this.authKey = authKey;
        this.scope = scope;
        this.authUrl = authUrl;
        this.apiUrl = apiUrl;
        this.model = model;
        this.restClient = buildRestClient(restClientBuilder, verifySsl);
        this.objectMapper = objectMapper;
        this.promptFactory = promptFactory;
    }

    public boolean isConfigured() {
        return authKey != null && !authKey.isBlank();
    }

    @Override
    public GeneratedTaskResponse generate(GenerateTaskRequest request) {
        if (!isConfigured()) {
            throw new IllegalStateException("GigaChat auth key is not configured");
        }

        try {
            String accessToken = accessToken();
            String content = requestCompletion(accessToken, promptFactory.build(request));
            return parseGeneratedTask(content, request);
        } catch (RestClientException exception) {
            throw new IllegalStateException("GigaChat HTTP request failed", exception);
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("GigaChat response processing failed", exception);
        }
    }

    private synchronized String accessToken() {
        long now = System.currentTimeMillis();
        if (cachedAccessToken != null && now < cachedExpiresAtMillis - TOKEN_REFRESH_SKEW_MILLIS) {
            return cachedAccessToken;
        }

        String body = "scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8);
        JsonNode response = restClient.post()
                .uri(authUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .header("RqUID", UUID.randomUUID().toString())
                .header(HttpHeaders.AUTHORIZATION, "Basic " + normalizedAuthKey())
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        if (response == null || response.path("access_token").asText().isBlank() || !response.has("expires_at")) {
            throw new IllegalStateException("GigaChat OAuth response is invalid");
        }

        cachedAccessToken = response.path("access_token").asText();
        cachedExpiresAtMillis = response.path("expires_at").asLong();
        log.info("GigaChat access token was refreshed; expiresAt={}", cachedExpiresAtMillis);
        return cachedAccessToken;
    }

    private String requestCompletion(String accessToken, String prompt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", model);
        payload.put("messages", List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user", "content", prompt)
        ));
        payload.put("stream", false);
        payload.put("repetition_penalty", 1);

        JsonNode response = restClient.post()
                .uri(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .body(payload)
                .retrieve()
                .body(JsonNode.class);

        String content = response == null
                ? null
                : response.path("choices").path(0).path("message").path("content").asText(null);
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("GigaChat chat completion response is empty");
        }
        return content;
    }

    private GeneratedTaskResponse parseGeneratedTask(String rawContent, GenerateTaskRequest request) throws Exception {
        JsonNode root = objectMapper.readTree(extractJson(rawContent));
        String title = requiredText(root, "title");
        String taskType = optionalText(root, "taskType", request.assignmentType().name());
        if (!request.assignmentType().name().equals(taskType)) {
            throw new IllegalStateException("GigaChat JSON taskType does not match request taskType");
        }
        String description = requiredText(root, "description");
        String instructions = requiredText(root, "instructions");
        String inputData = optionalText(root, "inputData", blankToDefault(request.inputData(), "Не требуется"));
        String expectedResult = requiredText(root, "expectedResult");
        String difficulty = requiredText(root, "difficulty");
        String topic = requiredText(root, "topic");
        List<String> hints = hints(root.path("hints"));

        if (!List.of("BEGINNER", "INTERMEDIATE", "ADVANCED").contains(difficulty)) {
            difficulty = request.difficulty().name();
        }

        Map<String, Object> additionalFields = additionalFields(root);
        validateTypeSpecificFields(root, request);

        return new GeneratedTaskResponse(
                title,
                taskType,
                description,
                instructions,
                inputData,
                expectedResult,
                hints,
                difficulty,
                topic,
                "GIGACHAT",
                additionalFields
        );
    }

    private String extractJson(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int firstNewLine = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewLine >= 0 && lastFence > firstNewLine) {
                return trimmed.substring(firstNewLine + 1, lastFence).trim();
            }
        }

        int objectStart = trimmed.indexOf('{');
        int objectEnd = trimmed.lastIndexOf('}');
        if (objectStart >= 0 && objectEnd > objectStart) {
            return trimmed.substring(objectStart, objectEnd + 1);
        }
        return trimmed;
    }

    private String requiredText(JsonNode root, String fieldName) {
        String value = root.path(fieldName).asText(null);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("GigaChat JSON is missing required field: " + fieldName);
        }
        return value.trim();
    }

    private String optionalText(JsonNode root, String fieldName, String defaultValue) {
        String value = root.path(fieldName).asText(null);
        if (value == null || value.isBlank()) {
            log.warn("GigaChat JSON is missing optional field {}, using default value", fieldName);
            return defaultValue;
        }
        return value.trim();
    }

    private List<String> hints(JsonNode hintsNode) {
        List<String> hints = new ArrayList<>();
        if (hintsNode.isArray()) {
            hintsNode.forEach(item -> {
                if (item.isTextual() && !item.asText().isBlank()) {
                    hints.add(item.asText().trim());
                }
            });
        } else if (hintsNode.isTextual() && !hintsNode.asText().isBlank()) {
            for (String hint : hintsNode.asText().split("\\n|;")) {
                if (!hint.isBlank()) {
                    hints.add(hint.trim());
                }
            }
        }

        if (hints.isEmpty()) {
            throw new IllegalStateException("GigaChat JSON is missing required field: hints");
        }
        return hints;
    }

    private Map<String, Object> additionalFields(JsonNode root) {
        Set<String> baseFields = Set.of(
                "title",
                "taskType",
                "description",
                "instructions",
                "inputData",
                "expectedResult",
                "hints",
                "difficulty",
                "topic"
        );
        Map<String, Object> fields = new LinkedHashMap<>();
        root.fields().forEachRemaining(entry -> {
            if (!baseFields.contains(entry.getKey())) {
                fields.put(entry.getKey(), objectMapper.convertValue(entry.getValue(), Object.class));
            }
        });
        return fields;
    }

    private void validateTypeSpecificFields(JsonNode root, GenerateTaskRequest request) {
        switch (request.assignmentType()) {
            case PRACTICE -> {
                requiredText(root, "starterCode");
                requiredArray(root, "requirements");
                requiredObject(root, "example");
                requiredText(root.path("example"), "input");
                requiredText(root.path("example"), "output");
                requiredText(root, "teacherSolution");
            }
            case TEST -> validateTest(root, request);
            case BUG_FIX -> {
                requiredText(root, "buggyCode");
                requiredText(root, "bugDescription");
                requiredText(root, "expectedFixedBehavior");
                requiredText(root, "teacherSolution");
                requiredArray(root, "commonMistakes");
            }
            case MINI_PROJECT -> {
                requiredText(root, "projectGoal");
                requiredArray(root, "functionalRequirements");
                requiredArray(root, "steps");
                requiredArray(root, "acceptanceCriteria");
                requiredArray(root, "extensionIdeas");
            }
            case HOMEWORK_WITH_CRITERIA -> {
                requiredArray(root, "homeworkTasks");
                requiredArray(root, "evaluationCriteria");
                if (!root.path("maxScore").canConvertToInt()) {
                    throw new IllegalStateException("GigaChat JSON is missing required numeric field: maxScore");
                }
                requiredText(root, "teacherNotes");
            }
        }
    }

    private void validateTest(JsonNode root, GenerateTaskRequest request) {
        JsonNode questions = requiredArray(root, "questions");
        int expectedQuestionCount = requestedQuestionCount(request.additionalRequirements());
        if (questions.size() != expectedQuestionCount) {
            throw new IllegalStateException("GigaChat TEST questions count does not match requested count");
        }
        for (JsonNode question : questions) {
            requiredText(question, "question");
            JsonNode options = requiredArray(question, "options");
            if (options.size() != 4) {
                throw new IllegalStateException("GigaChat TEST question must contain exactly 4 options");
            }
            String correctAnswer = requiredText(question, "correctAnswer");
            boolean answerInOptions = false;
            for (JsonNode option : options) {
                if (correctAnswer.equals(option.asText())) {
                    answerInOptions = true;
                    break;
                }
            }
            if (!answerInOptions) {
                throw new IllegalStateException("GigaChat TEST correctAnswer must match one of options");
            }
            requiredText(question, "explanation");
        }
        requiredText(root, "passingScore");
    }

    private int requestedQuestionCount(String additionalRequirements) {
        if (additionalRequirements == null || additionalRequirements.isBlank()) {
            return 5;
        }
        Matcher matcher = Pattern.compile("(\\d+)\\s*(вопрос|вопроса|вопросов)", Pattern.CASE_INSENSITIVE)
                .matcher(additionalRequirements);
        if (!matcher.find()) {
            return 5;
        }
        int count = Integer.parseInt(matcher.group(1));
        return Math.max(1, Math.min(count, 30));
    }

    private JsonNode requiredArray(JsonNode root, String fieldName) {
        JsonNode value = root.path(fieldName);
        if (!value.isArray() || value.isEmpty()) {
            throw new IllegalStateException("GigaChat JSON is missing required array field: " + fieldName);
        }
        return value;
    }

    private JsonNode requiredObject(JsonNode root, String fieldName) {
        JsonNode value = root.path(fieldName);
        if (!value.isObject()) {
            throw new IllegalStateException("GigaChat JSON is missing required object field: " + fieldName);
        }
        return value;
    }

    private String normalizedAuthKey() {
        String trimmed = authKey.trim();
        if (trimmed.regionMatches(true, 0, "Basic ", 0, "Basic ".length())) {
            return trimmed.substring("Basic ".length()).trim();
        }
        return trimmed;
    }

    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private RestClient buildRestClient(RestClient.Builder builder, boolean verifySsl) {
        if (verifySsl) {
            return builder.build();
        }

        log.warn("GigaChat SSL verification is disabled. Use this only for local development.");
        try {
            TrustManager[] trustAllManagers = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllManagers, new SecureRandom());
            HttpClient httpClient = HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .build();
            return builder.requestFactory(new JdkClientHttpRequestFactory(httpClient)).build();
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to configure GigaChat HTTP client", exception);
        }
    }
}
