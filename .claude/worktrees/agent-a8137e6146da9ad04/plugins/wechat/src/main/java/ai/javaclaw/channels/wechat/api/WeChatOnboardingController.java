package ai.javaclaw.channels.wechat.api;

import ai.javaclaw.channels.wechat.ILinkClient;
import ai.javaclaw.channels.wechat.WeChatOnboardingProvider;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * REST endpoints used by the WeChat onboarding step to obtain a login QR code
 * and poll for scan status.
 *
 * <p>The login flow works without a pre-existing bot token: iLink's QR code
 * endpoint is called with an empty token so the browser can display the QR,
 * wait for the user to scan, and then receive the real {@code bot_token} in
 * the status response (status == 2).</p>
 */
@Controller
@RequestMapping("/onboarding/wechat")
public class WeChatOnboardingController {

    /**
     * Default iLink API base URL used before a bot_token (and its own base_url) is known.
     */
    private static final String DEFAULT_BASE_URL = "https://ilinkai.weixin.qq.com";

    /**
     * Session attribute key storing the qrcode string returned by the QR endpoint,
     * needed for the follow-up status poll.
     */
    static final String SESSION_QRCODE = "onboarding.wechat.qrcode";

    /**
     * GET /onboarding/wechat/qrcode
     *
     * Calls iLink to obtain a fresh QR code URL. Stores the raw qrcode identifier
     * in the HTTP session so the status endpoint can reference it.
     *
     * @return JSON {@code {"qrcodeUrl": "...", "qrcode": "..."}}
     */
    @GetMapping("/qrcode")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getQrCode(HttpSession session) {
        try {
            // Use empty token — the QR endpoint is the pre-auth entry point.
            ILinkClient loginClient = new ILinkClient(DEFAULT_BASE_URL, "");
            ILinkClient.QrCodeResponse response = loginClient.getQrCode();

            if (response == null || response.qrcode() == null) {
                return ResponseEntity.internalServerError()
                        .body(Map.of("error", "Failed to obtain QR code from iLink API"));
            }

            session.setAttribute(SESSION_QRCODE, response.qrcode());

            return ResponseEntity.ok(Map.of(
                    "qrcode", response.qrcode(),
                    "expiredTime", response.expiredTime()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Could not reach iLink API: " + e.getMessage()));
        }
    }

    /**
     * GET /onboarding/wechat/status
     *
     * Polls iLink for the scan status of the QR code stored in the session.
     *
     * <ul>
     *   <li>status 0 – not yet scanned</li>
     *   <li>status 1 – scanned but not confirmed on the phone</li>
     *   <li>status 2 – confirmed; {@code token} and {@code baseUrl} are returned
     *       and saved into the session so {@link WeChatOnboardingProvider} can
     *       persist them on form submit</li>
     * </ul>
     *
     * @return JSON {@code {"status": 0|1|2 [, "token": "...", "baseUrl": "..."]}}
     */
    @GetMapping("/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStatus(HttpSession session) {
        String qrcode = (String) session.getAttribute(SESSION_QRCODE);
        if (qrcode == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No active QR session. Fetch /qrcode first."));
        }

        try {
            ILinkClient loginClient = new ILinkClient(DEFAULT_BASE_URL, "");
            ILinkClient.QrCodeStatusResponse response = loginClient.getQrCodeStatus(qrcode);

            if (response == null) {
                return ResponseEntity.internalServerError()
                        .body(Map.of("error", "Null response from iLink status endpoint"));
            }

            if (response.status() == 2) {
                // Confirmed — store token and base URL into the onboarding session
                // so WeChatOnboardingProvider.processStep() picks them up on form POST.
                String token = response.botToken() != null ? response.botToken() : "";
                String baseUrl = response.baseUrl() != null ? response.baseUrl() : DEFAULT_BASE_URL;
                session.setAttribute(WeChatOnboardingProvider.SESSION_BOT_TOKEN, token);
                session.setAttribute(WeChatOnboardingProvider.SESSION_BASE_URL, baseUrl);

                return ResponseEntity.ok(Map.of(
                        "status", 2,
                        "token", token,
                        "baseUrl", baseUrl
                ));
            }

            return ResponseEntity.ok(Map.of("status", response.status()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Could not reach iLink API: " + e.getMessage()));
        }
    }
}
