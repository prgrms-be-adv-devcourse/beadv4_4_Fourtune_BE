package com.fourtune.auction;

import com.fourtune.auction.boundedContext.notification.adapter.in.kafka.NotificationUserKafkaListener;
import com.fourtune.auction.boundedContext.settlement.adapter.in.kafka.SettlementUserKafkaListener;
import com.fourtune.auction.boundedContext.watchList.adapter.in.kafka.WatchListUserKafkaListener;
import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class FourtuneApplicationTests {

	@MockitoBean
	private FirebaseMessaging firebaseMessaging;

	@MockitoBean private WatchListUserKafkaListener watchListUserKafkaListener;
	@MockitoBean private SettlementUserKafkaListener settlementUserKafkaListener;
	@MockitoBean private NotificationUserKafkaListener notificationUserKafkaListener;

	@Test
	void contextLoads() {
	}

}
