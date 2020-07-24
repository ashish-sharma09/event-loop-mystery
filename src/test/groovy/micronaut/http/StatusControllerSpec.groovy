package micronaut.http

import io.micronaut.context.ApplicationContext
import io.micronaut.runtime.server.EmbeddedServer
import io.restassured.RestAssured
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class StatusControllerSpec extends Specification {

    // This may vary for different machines
    public static final BigDecimal AVERAGE_EXPECTED_RESPONSE_TIME = 20.0

    @Shared
    def context

    @Shared
    ExecutorService threadPool = Executors.newFixedThreadPool(100)

    void setupSpec() {
        context = ApplicationContext
                .build()
                .mainClass(EmbeddedServer)
                .build()
        context.start()

        def server = context.getBean(EmbeddedServer)
        server.start()

        RestAssured.port = server.port

        // application warmup
        get("/status", threadPool, 5).every{it.get() > 0.0}
        get("/_status", threadPool, 5).every{it.get() > 0.0}
    }

    def "Non blocking controller should not be too affected with requests to a blocking controller"() {
        given:

        when: "Multiple concurrent requests are made to BlockingController"
        def blockingFutures = get("/status", threadPool, 20)

        and: "At the same time requests are made to non-blocking controller"
        List<Future<Long>> nonBlockingFutures = get("/_status", threadPool, 20)

        then: "non blocking futures average response time should not be slower than 10ms"
        def average = nonBlockingFutures.stream().mapToLong { it.get() }.average()
        println ("Average of Non blocking requests with blocking requests: -----> " + average.getAsDouble())
        average.getAsDouble() <= AVERAGE_EXPECTED_RESPONSE_TIME

        cleanup: // free up all threads
        blockingFutures.every{it.get() > 0.0}
    }

    def "Non blocking controller should be fast when there are no requests to a blocking controller"() {
        given:

        when: "requests are made"
        List<Future<Long>> nonBlockingFutures = get("/_status", threadPool, 20)

        then: "non blocking futures average response time should not be slower than 10ms"
        def average = nonBlockingFutures.stream().mapToLong { it.get() }.average()
        println ("Average of Non blocking requests without blocking requests: -----> " + average.getAsDouble())
        average.getAsDouble() <= AVERAGE_EXPECTED_RESPONSE_TIME
    }

    private List<Future<Long>> get(String endpoint, ExecutorService threadPool, Integer concurrency) {
        List<Future<Long>> futures = []

        concurrency.times {
            futures.add(threadPool.submit(new Callable<Long>() {
                @Override
                Long call() throws Exception {
                    return RestAssured
                            .given()
                            .header("Content-Type", "application/json")
                            .get(endpoint)
                            .time()
                }
            }))
        }
        futures
    }
}
