package ai.javaclaw.channels.wechat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.client.RestClient;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * HTTP client for Tencent iLink Bot API (WeChat enterprise bot).
 * All requests are POST JSON with Bearer token auth.
 */
public class ILinkClient {

    private final RestClient restClient;
    private final String botToken;

    public ILinkClient(String baseUrl, String botToken) {
        this.botToken = botToken;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("AuthorizationType", "ilink_bot_token")
                .build();
    }

    // ---- Auth headers ----

    private String freshWechatUin() {
        byte[] bytes = ByteBuffer.allocate(4).putInt(ThreadLocalRandom.current().nextInt()).array();
        return Base64.getEncoder().encodeToString(bytes);
    }

    // ---- QR Code login (one-time setup) ----

    public QrCodeResponse getQrCode() {
        return restClient.get()
                .uri("/ilink/bot/get_bot_qrcode?bot_type=3")
                .header("Authorization", "Bearer " + botToken)
                .header("X-WECHAT-UIN", freshWechatUin())
                .retrieve()
                .body(QrCodeResponse.class);
    }

    public QrCodeStatusResponse getQrCodeStatus(String qrcode) {
        return restClient.get()
                .uri("/ilink/bot/get_qrcode_status?qrcode={qr}", qrcode)
                .header("Authorization", "Bearer " + botToken)
                .header("X-WECHAT-UIN", freshWechatUin())
                .retrieve()
                .body(QrCodeStatusResponse.class);
    }

    // ---- Long-poll receive ----

    public GetUpdatesResponse getUpdates(String cursor) {
        Map<String, Object> body = Map.of(
                "get_updates_buf", cursor == null ? "" : cursor,
                "base_info", Map.of("channel_version", "1.0.2")
        );
        return restClient.post()
                .uri("/ilink/bot/getupdates")
                .header("Authorization", "Bearer " + botToken)
                .header("X-WECHAT-UIN", freshWechatUin())
                .body(body)
                .retrieve()
                .body(GetUpdatesResponse.class);
    }

    // ---- Send message ----

    public void sendText(String toUserId, String contextToken, String text) {
        Map<String, Object> body = Map.of(
                "msg", Map.of(
                        "to_user_id", toUserId,
                        "context_token", contextToken,
                        "message_type", 2,
                        "message_state", 2,
                        "item_list", List.of(Map.of(
                                "type", 1,
                                "text_item", Map.of("text", text)
                        ))
                )
        );
        restClient.post()
                .uri("/ilink/bot/sendmessage")
                .header("Authorization", "Bearer " + botToken)
                .header("X-WECHAT-UIN", freshWechatUin())
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    // ---- Response records ----

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record QrCodeResponse(
            @JsonProperty("qrcode") String qrcode,
            @JsonProperty("expired_time") int expiredTime
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record QrCodeStatusResponse(
            @JsonProperty("status") int status,
            @JsonProperty("bot_token") String botToken,
            @JsonProperty("base_url") String baseUrl
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GetUpdatesResponse(
            @JsonProperty("ret") int ret,
            @JsonProperty("msgs") List<InboundMessage> msgs,
            @JsonProperty("get_updates_buf") String getUpdatesBuf,
            @JsonProperty("longpolling_timeout_ms") long longpollingTimeoutMs
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InboundMessage(
            @JsonProperty("from_user_id") String fromUserId,
            @JsonProperty("to_user_id") String toUserId,
            @JsonProperty("context_token") String contextToken,
            @JsonProperty("item_list") List<MessageItem> itemList
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MessageItem(
            @JsonProperty("type") int type,
            @JsonProperty("text_item") TextItem textItem
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TextItem(
            @JsonProperty("text") String text
    ) {}
}
