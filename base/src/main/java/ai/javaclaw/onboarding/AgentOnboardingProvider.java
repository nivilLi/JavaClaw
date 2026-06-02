package ai.javaclaw.onboarding;

import java.util.Map;
import java.util.Optional;

public interface AgentOnboardingProvider {

    String getId();

    String getLabel();

    String slogan();

    boolean requiresApiKey();

    String defaultModel();

    default Optional<SystemWideToken> systemWideToken() {
        return Optional.empty();
    }

    default String createPropertyKey(String propertySuffix) {
        return "spring.ai." + getId() + "." + propertySuffix;
    }

    default void saveProperty(Map<String, Object> properties, String propertySuffix, String value) {
        if (value == null || value.isBlank()) return;
        properties.put(createPropertyKey(propertySuffix), value);
    }

    /**
     * Returns the value to write to {@code spring.ai.model.chat} when this provider is selected.
     * Defaults to {@link #getId()}. Override when the Spring AI starter uses a different model ID
     * than this provider's unique identifier (e.g. Qwen reuses the OpenAI starter).
     */
    default String getChatModelId() {
        return getId();
    }

    /**
     * Returns additional properties to persist when this provider is selected during onboarding.
     * Keys are full property names (e.g. "spring.ai.openai.base-url").
     */
    default Map<String, String> additionalProperties() {
        return Map.of();
    }

    record SystemWideToken(String name, String token) {}
}
