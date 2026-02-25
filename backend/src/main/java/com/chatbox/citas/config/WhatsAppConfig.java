package com.chatbox.citas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

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

    public String getApiUrl() {
        return baseUrl + "/" + apiVersion + "/" + phoneNumberId + "/messages";
    }
}
