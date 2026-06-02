package ai.javaclaw.channels.wechat;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "agent.channels.wechat")
public record WeChatProperties(
    String botToken,
    String baseUrl
) {
    public WeChatProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://ilinkai.weixin.qq.com";
        }
    }
}
