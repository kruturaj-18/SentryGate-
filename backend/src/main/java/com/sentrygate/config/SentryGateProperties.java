package com.sentrygate.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sentrygate")
@Getter @Setter
public class SentryGateProperties {

    private Jwt jwt = new Jwt();
    private GitHub github = new GitHub();
    private Frontend frontend = new Frontend();
    private Demo demo = new Demo();

    @Getter @Setter
    public static class Jwt {
        private String secret;
        private long expiration = 86400000;
    }

    @Getter @Setter
    public static class GitHub {
        private String clientId;
        private String clientSecret;
        private String appId;
        private String appPrivateKey;
        private String webhookSecret;
        private String apiBaseUrl = "https://api.github.com";
        private String oauthBaseUrl = "https://github.com";
    }

    @Getter @Setter
    public static class Frontend {
        private String url = "http://localhost:5173";
    }

    @Getter @Setter
    public static class Demo {
        private boolean mode = true;
    }
}
