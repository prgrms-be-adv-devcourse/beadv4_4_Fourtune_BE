package com.fourtune.outbox.handler;

/**
 * Outbox 이벤트 핸들러 인터페이스
 * 각 도메인에서 이 인터페이스를 구현하여 Kafka 발행 로직을 정의
 */
public interface OutboxEventHandler {

    /**
     * 이 핸들러가 처리할 aggregate type 반환
     */
    String getAggregateType();

    /**
     * 이벤트 발행 처리
     * 
     * @param payload JSON 문자열 형태의 이벤트 페이로드
     */
    void handle(String payload) throws Exception;
}
