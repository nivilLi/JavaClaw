package ai.javaclaw.providers.qwen;

import ai.javaclaw.onboarding.AgentOnboardingProvider;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class QwenAgentOnboardingProvider implements AgentOnboardingProvider {

    private static final String DASHSCOPE_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    @Override
    public String getId() {
        return "qwen";
    }

    /**
     * Qwen uses DashScope's OpenAI-compatible endpoint, so all Spring AI properties
     * must use the "spring.ai.openai.*" prefix and "spring.ai.model.chat=openai".
     */
    @Override
    public String createPropertyKey(String propertySuffix) {
        return "spring.ai.openai." + propertySuffix;
    }

    @Override
    public String getLabel() {
        return "Qwen (通义千问)";
    }

    @Override
    public String slogan() {
        return "Uses Alibaba DashScope API for Qwen models (qwen-max, qwen-plus, qwen-turbo).";
    }

    @Override
    public boolean requiresApiKey() {
        return true;
    }

    @Override
    public String defaultModel() {
        return "qwen-max";
    }

    @Override
    public String getChatModelId() {
        // Spring AI activates the OpenAI starter when spring.ai.model.chat=openai
        return "openai";
    }

    @Override
    public Map<String, String> additionalProperties() {
        return Map.of("spring.ai.openai.base-url", DASHSCOPE_BASE_URL);
    }
}
