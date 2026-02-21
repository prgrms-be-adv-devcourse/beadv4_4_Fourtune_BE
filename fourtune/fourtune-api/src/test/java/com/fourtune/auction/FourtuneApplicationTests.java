package com.fourtune.auction;

import com.fourtune.api.infrastructure.kafka.notification.NotificationKafkaProducer;
import com.fourtune.api.infrastructure.kafka.search.SearchKafkaProducer;
import com.fourtune.api.infrastructure.kafka.watchList.WatchListKafkaProducer;
import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class FourtuneApplicationTests {

	@MockitoBean
	private FirebaseMessaging firebaseMessaging;

	@MockitoBean
	private WatchListKafkaProducer watchListKafkaProducer;

	@MockitoBean
	private NotificationKafkaProducer notificationKafkaProducer;

	@MockitoBean
	private SearchKafkaProducer searchKafkaProducer;

	@Test
	void contextLoads() {
	}

}
