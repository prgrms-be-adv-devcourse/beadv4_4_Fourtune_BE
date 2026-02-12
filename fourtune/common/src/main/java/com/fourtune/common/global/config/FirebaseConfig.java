package com.fourtune.common.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseConfig {
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
                InputStream serviceAccount = new ClassPathResource("firebase-service-account.json").getInputStream();
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                firebaseApp = FirebaseApp.initializeApp(options);
                log.info("🔥 Firebase(FCM) 연결 성공!");

            } catch (IllegalStateException e) {
                firebaseApp = FirebaseApp.getInstance();
            } catch (IOException e) {
                log.error("🚫 Firebase 초기화 실패: json 파일 위치나 내용을 확인하세요.", e);
                throw new RuntimeException(e);
            }
        }

        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
