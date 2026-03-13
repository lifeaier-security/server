package com.lifeaier.base.comm.mail.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;
import com.lifeaier.base.comm.constant.ErrorCode;
import com.lifeaier.base.comm.mail.service.MailVerifyService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class MailVerifyController {

    private static final Logger log = LoggerFactory.getLogger(MailVerifyController.class);

    private final MailVerifyService mailVerifyService;

    @Value("${app.url}")
    private String appUrl;

    public MailVerifyController(
            MailVerifyService mailVerifyService
    ) {
        this.mailVerifyService = mailVerifyService;
    }

    @PostMapping("/verify")
    public void verify(
            @RequestBody Map<String, Object> data,
            HttpServletResponse response) throws IOException {

        String token = data.get("token").toString();
        boolean success = mailVerifyService.verify(token, response);

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

        mailVerifyService.resend(username);
    }
}