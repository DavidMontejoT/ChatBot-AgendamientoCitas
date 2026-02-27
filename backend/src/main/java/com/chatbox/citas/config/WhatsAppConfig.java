package com.chatbox.citas.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class WhatsAppConfig {

    @Value("${whatsapp.api.token}")
    private String apiToken;

    @Value("${whatsapp.api.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.api.base-url}")
    private String baseUrl;

    @Value("${whatsapp.api.version}")
    private String apiVersion;

    @Value("${whatsapp.api.verify-token}")
    private String verifyToken;

    @Value("${whatsapp.conversation.timeout-minutes:30}")
    private int conversationTimeoutMinutes;

    @PostConstruct
    public void validateConfiguration() {
        if (apiToken == null || apiToken.isBlank()) {
            throw new IllegalStateException("WhatsApp API token is not configured. Please set 'whatsapp.api.token' in application.properties");
        }
        if (phoneNumberId == null || phoneNumberId.isBlank()) {
            throw new IllegalStateException("WhatsApp phone number ID is not configured. Please set 'whatsapp.api.phone-number-id' in application.properties");
        }
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("WhatsApp base URL is not configured. Please set 'whatsapp.api.base-url' in application.properties");
        }
        if (apiVersion == null || apiVersion.isBlank()) {
            throw new IllegalStateException("WhatsApp API version is not configured. Please set 'whatsapp.api.version' in application.properties");
        }
        if (verifyToken == null || verifyToken.isBlank()) {
            throw new IllegalStateException("WhatsApp verify token is not configured. Please set 'whatsapp.api.verify-token' in application.properties");
        }
        if (conversationTimeoutMinutes <= 0) {
            throw new IllegalStateException("WhatsApp conversation timeout must be positive. Current value: " + conversationTimeoutMinutes);
        }

        log.info("✅ WhatsApp configuration validated successfully");
        log.debug("WhatsApp Config - Base URL: {}, Version: {}, Phone ID: {}, Timeout: {} min",
            baseUrl, apiVersion, phoneNumberId, conversationTimeoutMinutes);
    }

    public String getApiToken() {
        return apiToken;
    }

    public String getPhoneNumberId() {
        return phoneNumberId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getVerifyToken() {
        return verifyToken;
    }

    public int getConversationTimeoutMinutes() {
        return conversationTimeoutMinutes;
    }

    public String getApiUrl() {
        return baseUrl + "/" + apiVersion + "/" + phoneNumberId + "/messages";
    }
}
