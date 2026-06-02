package ai.javaclaw.channels.wechat;

import ai.javaclaw.configuration.ConfigurationManager;
import ai.javaclaw.onboarding.OnboardingProvider;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@Order(55)
public class WeChatOnboardingProvider implements OnboardingProvider {

    public static final String SESSION_BOT_TOKEN = "onboarding.wechat.bot-token";
    public static final String SESSION_BASE_URL = "onboarding.wechat.base-url";

    private final Environment env;

    public WeChatOnboardingProvider(Environment env) {
        this.env = env;
    }

    @Override
    public boolean isOptional() {
        return true;
    }

    @Override
    public String getStepId() {
        return "wechat";
    }

    @Override
    public String getStepTitle() {
        return "WeChat";
    }

    @Override
    public String getTemplatePath() {
        return "onboarding/steps/wechat";
    }

    @Override
    public void prepareModel(Map<String, Object> session, Map<String, Object> model) {
        String existingToken = (String) session.getOrDefault(SESSION_BOT_TOKEN,
                env.getProperty("agent.channels.wechat.bot-token", ""));
        model.put("wechatBotToken", existingToken);
        model.put("wechatConfigured", existingToken != null && !existingToken.isBlank());
    }

    @Override
    public String processStep(Map<String, String> formParams, Map<String, Object> session) {
        String token = formParams.getOrDefault("wechatBotToken", "").trim();
        String baseUrl = formParams.getOrDefault("wechatBaseUrl", "").trim();

        if (token.isBlank()) {
            // Allow skipping if no token (optional step)
            return null;
        }

        session.put(SESSION_BOT_TOKEN, token);
        if (!baseUrl.isBlank()) {
            session.put(SESSION_BASE_URL, baseUrl);
        }

        return null;
    }

    @Override
    public void saveConfiguration(Map<String, Object> session, ConfigurationManager configurationManager) throws IOException {
        String token = (String) session.get(SESSION_BOT_TOKEN);
        String baseUrl = (String) session.get(SESSION_BASE_URL);
        if (token != null && !token.isBlank()) {
            Map<String, Object> props = new java.util.LinkedHashMap<>();
            props.put("agent.channels.wechat.bot-token", token);
            if (baseUrl != null && !baseUrl.isBlank()) {
                props.put("agent.channels.wechat.base-url", baseUrl);
            }
            configurationManager.updateProperties(props);
        }
    }
}
