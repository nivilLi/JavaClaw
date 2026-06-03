package ai.javaclaw.agent;

import ai.javaclaw.channels.ChannelRegistry;
import ai.javaclaw.tasks.TaskManager;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;

@Component
public class DefaultAgent implements Agent {

    private final ChatClient chatClient;
    private final ChannelRegistry channelRegistry;
    private final TaskManager taskManager;

    public DefaultAgent(ChatClient chatClient, ChannelRegistry channelRegistry, TaskManager taskManager) {
        this.chatClient = chatClient;
        this.channelRegistry = channelRegistry;
        this.taskManager = taskManager;
    }

    @Override
    public String respondTo(String conversationId, String question) {
        // Check if the current channel has a pending task awaiting human input
        var latestChannel = channelRegistry.getLatestChannel();
        if (latestChannel != null) {
            String pendingTaskId = channelRegistry.getPendingTaskId(latestChannel.getName());
            if (pendingTaskId != null) {
                channelRegistry.clearPendingTask(latestChannel.getName());
                try {
                    taskManager.resumeWithUserReply(pendingTaskId, question);
                    return "Got it! I've resumed the task with your input and it will continue shortly.";
                } catch (Exception e) {
                    // If task lookup fails, fall through to normal conversation
                }
            }
        }

        return chatClient
                .prompt(question)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    @Override
    public <T> T prompt(String conversationId, String input, Class<T> result) {
        return chatClient
                .prompt(input)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .entity(result);
    }
}
