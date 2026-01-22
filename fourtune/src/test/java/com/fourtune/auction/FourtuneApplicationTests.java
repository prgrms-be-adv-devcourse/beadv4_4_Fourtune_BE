package com.fourtune.auction;

import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class FourtuneApplicationTests {

	@MockitoBean
	private FirebaseMessaging firebaseMessaging;

	@Test
	void contextLoads() {
	}

}
