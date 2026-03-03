package com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch;

import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

/**
 * Elasticsearch Testcontainer 싱글턴.
 * Nori 플러그인이 포함된 커스텀 ES 이미지를 빌드하여 사용합니다.
 * ES 버전 변경 시 {@link #ES_VERSION} 상수와 {@code Dockerfile.elasticsearch}를 함께
 * 수정하세요.
 */
public final class ElasticsearchTestContainer {

    // Elasticsearch 버전 — Dockerfile.elasticsearch 의 FROM과 반드시 동일하게 유지할 것.
    static final String ES_VERSION = "9.2.3";

    private static final ElasticsearchContainer CONTAINER = createContainer();

    private ElasticsearchTestContainer() {
        // 인스턴스 생성 방지
    }

    @SuppressWarnings("resource") // 싱글턴 — JVM 종료 시까지 유지
    private static ElasticsearchContainer createContainer() {
        ImageFromDockerfile image = new ImageFromDockerfile("fourtune-es-test", false)
                .withDockerfileFromBuilder(
                        builder -> builder.from("docker.elastic.co/elasticsearch/elasticsearch:" + ES_VERSION)
                                .run("bin/elasticsearch-plugin install analysis-nori")
                                .build());

        ElasticsearchContainer container = new ElasticsearchContainer(
                DockerImageName.parse(image.get())
                        .asCompatibleSubstituteFor("docker.elastic.co/elasticsearch/elasticsearch"))
                .withEnv("xpack.security.enabled", "false")
                .withEnv("discovery.type", "single-node")
                .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
                .withStartupTimeout(Duration.ofMinutes(5));

        container.start();
        return container;
    }

    public static ElasticsearchContainer getInstance() {
        return CONTAINER;
    }
}
