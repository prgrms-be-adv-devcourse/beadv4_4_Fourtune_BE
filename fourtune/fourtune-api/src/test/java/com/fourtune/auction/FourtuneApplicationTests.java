package com.fourtune.auction;

import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class FourtuneApplicationTests {

	@MockitoBean
	private FirebaseMessaging firebaseMessaging;

	@MockitoBean
	private com.fourtune.common.shared.watchList.kafka.WatchListKafkaProducer watchListKafkaProducer;

	@MockitoBean
	private com.fourtune.common.shared.notification.kafka.NotificationKafkaProducer notificationKafkaProducer;

	@MockitoBean
	private com.fourtune.common.shared.search.kafka.SearchKafkaProducer searchKafkaProducer;

	@Test
	void contextLoads() {
	}

}
