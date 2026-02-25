package com.fourtune.fcm.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
@Slf4j
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = true)
public class FirebaseConfig {

    @Value("${FIREBASE_SERVICE_ACCOUNT_JSON:}")
    private String serviceAccountJson;

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        FirebaseApp firebaseApp = null;

        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                firebaseApp = FirebaseApp.getInstance();
            }
        } catch (Exception e) {
        }

        if (firebaseApp == null) {
            try {
                InputStream serviceAccount = resolveServiceAccount();
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                firebaseApp = FirebaseApp.initializeApp(options);
                log.info("Firebase(FCM) 연결 성공!");

            } catch (IllegalStateException e) {
                firebaseApp = FirebaseApp.getInstance();
            } catch (IOException e) {
                log.error("Firebase 초기화 실패: FIREBASE_SERVICE_ACCOUNT_JSON 환경 변수 또는 firebase-service-account.json 파일을 확인하세요.", e);
                throw new RuntimeException(e);
            }
        }

        return FirebaseMessaging.getInstance(firebaseApp);
    }

    private InputStream resolveServiceAccount() throws IOException {
        if (serviceAccountJson != null && !serviceAccountJson.isBlank()) {
            log.info("Firebase 서비스 계정을 환경 변수에서 로드합니다.");
            return new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8));
        }
        log.info("Firebase 서비스 계정을 파일(firebase-service-account.json)에서 로드합니다.");
        return new ClassPathResource("firebase-service-account.json").getInputStream();
    }
}
