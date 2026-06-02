package ai.javaclaw.channels.wechat;

import ai.javaclaw.agent.Agent;
import ai.javaclaw.channels.Channel;
import ai.javaclaw.channels.ChannelMessageReceivedEvent;
import ai.javaclaw.channels.ChannelRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * WeChat channel using Tencent's iLink Bot API with long-polling.
 * Activated when {@code agent.channels.wechat.bot-token} is set.
 */
public class WeChatChannel implements Channel {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeChatChannel.class);

    private final ILinkClient iLinkClient;
    private final Agent agent;
    private final ChannelRegistry channelRegistry;

    private volatile String lastFromUserId;
    private volatile String lastContextToken;

    private volatile boolean running = false;
    private Thread pollingThread;

    public WeChatChannel(WeChatProperties properties, Agent agent, ChannelRegistry channelRegistry) {
        this.iLinkClient = new ILinkClient(properties.baseUrl(), properties.botToken());
        this.agent = agent;
        this.channelRegistry = channelRegistry;
        channelRegistry.registerChannel(this);
        LOGGER.info("Started WeChat (iLink) integration");
    }

    @Override
    public String getName() {
        return "WeChat";
    }

    @Override
    public void sendMessage(String message) {
        if (lastFromUserId == null || lastContextToken == null) {
            LOGGER.warn("No known WeChat recipient, cannot send message");
            return;
        }
        try {
            iLinkClient.sendText(lastFromUserId, lastContextToken, message);
        } catch (Exception e) {
            LOGGER.error("Failed to send WeChat message", e);
        }
    }

    @PostConstruct
    public void startPolling() {
        running = true;
        pollingThread = Thread.ofVirtual().name("wechat-polling").start(this::pollLoop);
    }

    @PreDestroy
    public void stopPolling() {
        running = false;
        if (pollingThread != null) {
            pollingThread.interrupt();
        }
    }

    private void pollLoop() {
        String cursor = "";
        LOGGER.info("WeChat long-poll loop started");
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                ILinkClient.GetUpdatesResponse response = iLinkClient.getUpdates(cursor);
                if (response == null) {
                    Thread.sleep(1000);
                    continue;
                }
                if (response.getUpdatesBuf() != null && !response.getUpdatesBuf().isBlank()) {
                    cursor = response.getUpdatesBuf();
                }
                List<ILinkClient.InboundMessage> msgs = response.msgs();
                if (msgs != null) {
                    for (ILinkClient.InboundMessage msg : msgs) {
                        processMessage(msg);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                LOGGER.warn("Error during WeChat poll, retrying in 3s: {}", e.getMessage());
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        LOGGER.info("WeChat long-poll loop stopped");
    }

    private void processMessage(ILinkClient.InboundMessage msg) {
        if (msg.itemList() == null || msg.itemList().isEmpty()) return;

        ILinkClient.MessageItem firstItem = msg.itemList().getFirst();
        if (firstItem.type() != 1 || firstItem.textItem() == null) return;

        String text = firstItem.textItem().text();
        if (text == null || text.isBlank()) return;

        lastFromUserId = msg.fromUserId();
        lastContextToken = msg.contextToken();

        channelRegistry.publishMessageReceivedEvent(new ChannelMessageReceivedEvent(getName(), text));
        String response = agent.respondTo("wechat-" + msg.fromUserId(), text);
        sendMessage(response);
    }
}
