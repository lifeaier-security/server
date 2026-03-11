package web.server.api.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;
import web.server.api.common.ErrorCode;
import web.server.api.service.MailVerificationService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class MailVerificationController {

    private static final Logger log = LoggerFactory.getLogger(MailVerificationController.class);

    private final MailVerificationService mailVerificationService;

    @Value("${app.url}")
    private String appUrl;

    public MailVerificationController(
            MailVerificationService mailVerificationService
    ) {
        this.mailVerificationService = mailVerificationService;
    }

    @PostMapping("/verify")
    public void verify(
            @RequestBody Map<String, Object> data,
            HttpServletResponse response) throws IOException {

        String token = data.get("token").toString();
        boolean success = mailVerificationService.verify(token, response);

        if (success) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ErrorCode error = ErrorCode.MAIL_NOT_VERIFIED;

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("error", error.name());

            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getWriter(), responseBody);
        }
    }

    @PostMapping("/resend")
    public void resend(@RequestBody Map<String, Object> data) {

        String username = data.get("username").toString();

        mailVerificationService.resend(username);
    }
}