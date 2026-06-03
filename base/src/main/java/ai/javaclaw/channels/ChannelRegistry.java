package ai.javaclaw.channels;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ChannelRegistry {

    private final Map<String, Channel> channels;
    private final AtomicReference<ChannelMessageReceivedEvent> lastChannelMessage;
    private final Map<String, String> pendingTasks;
    private String defaultChannelName;

    public ChannelRegistry() {
        this.channels = new HashMap<>();
        this.lastChannelMessage = new AtomicReference<>();
        this.pendingTasks = new ConcurrentHashMap<>();
    }

    public void registerChannel(Channel channel) {
        channels.put(channel.getName(), channel);
        if (channels.size() == 1) {
            this.defaultChannelName = channel.getName();
        }
    }

    public void unregisterChannel(Channel channel) {
        channels.remove(channel.getName());
    }

    public Channel getLatestChannel() {
        if (lastChannelMessage.get() != null) {
            return channels.get(lastChannelMessage.get().getChannel());
        }
        return channels.get(defaultChannelName);
    }

    public Channel getChannel(String name) {
        return channels.get(name);
    }

    public void publishMessageReceivedEvent(ChannelMessageReceivedEvent event) {
        lastChannelMessage.set(event);
    }

    public void setPendingTask(String channelName, String taskId) {
        pendingTasks.put(channelName, taskId);
    }

    public String getPendingTaskId(String channelName) {
        return pendingTasks.get(channelName);
    }

    public void clearPendingTask(String channelName) {
        pendingTasks.remove(channelName);
    }
}
