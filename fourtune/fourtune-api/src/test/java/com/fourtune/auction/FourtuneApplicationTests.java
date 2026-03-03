package com.fourtune.auction;

import com.fourtune.api.infrastructure.kafka.notification.NotificationKafkaProducer;
import com.fourtune.api.infrastructure.kafka.search.SearchKafkaProducer;
import com.fourtune.api.infrastructure.kafka.watchList.WatchListKafkaProducer;
import com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch.ElasticsearchTestContainer;
import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

@SpringBootTest
class FourtuneApplicationTests {

	private static final ElasticsearchContainer elasticsearchContainer = ElasticsearchTestContainer.getInstance();

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.elasticsearch.uris", elasticsearchContainer::getHttpHostAddress);
	}

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
