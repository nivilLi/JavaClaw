package ai.javaclaw.channels.wechat;

import ai.javaclaw.agent.Agent;
import ai.javaclaw.channels.ChannelRegistry;
import ai.javaclaw.channels.wechat.api.WeChatOnboardingController;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@AutoConfiguration
@EnableConfigurationProperties(WeChatProperties.class)
public class WeChatChannelAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "agent.channels.wechat", name = "bot-token")
    public WeChatChannel weChatChannel(WeChatProperties properties, Agent agent, ChannelRegistry channelRegistry) {
        return new WeChatChannel(properties, agent, channelRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public WeChatOnboardingProvider weChatOnboardingProvider(Environment env) {
        return new WeChatOnboardingProvider(env);
    }

    @Bean
    @ConditionalOnMissingBean
    public WeChatOnboardingController weChatOnboardingController() {
        return new WeChatOnboardingController();
    }
}
