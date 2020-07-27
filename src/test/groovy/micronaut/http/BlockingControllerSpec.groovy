package micronaut.http

import io.micronaut.context.ApplicationContext
import io.micronaut.runtime.server.EmbeddedServer
import io.restassured.RestAssured
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class BlockingControllerSpec extends Specification {

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

        // better warmup
        get("/blocking", threadPool, 100)*.get()
        get("/nonBlocking", threadPool, 100)*.get()
    }

    @Unroll
    def "#blockingConcurrentCalls blocking calls, #nonBlockingCalls - impact of blocking calls on non blocking calls"() {
        given: "Sequential set of concurrent blocking calls to create noise in background"
        def blockingCaller = Thread.start {
            blockingSequenceCalls.times {
                // concurrent
                get("/blocking", threadPool, blockingConcurrentCalls)*.get()
            }
        }

        when: "requests are made"
        List<Future<Long>> nonBlockingFutures = get("/nonBlocking", threadPool, nonBlockingCalls)

        and: "join the thread blockingCaller so it finish cleanly"
        blockingCaller.join()

        then: "non blocking futures average response time should not be slower than 10ms"
        def average = nonBlockingFutures.stream().mapToLong { it.get() }.average()
        println("Average of Non blocking requests without blocking requests: -----> " + average.getAsDouble())

        average.getAsDouble() <= AVERAGE_EXPECTED_RESPONSE_TIME

        where:
        blockingConcurrentCalls | blockingSequenceCalls | nonBlockingCalls | description
        0                       | 0                     | 20               | "20 concurrent non blocking calls"
        0                       | 0                     | 40               | "40 concurrent non blocking calls"
        0                       | 0                     | 20               | "20 concurrent non blocking calls again"
        0                       | 0                     | 100              | "100 concurrent non blocking calls"
        0                       | 0                     | 100              | "100 concurrent non blocking calls"
        0                       | 0                     | 100              | "100 concurrent non blocking calls"


        2                       | 5                    | 20                | "2 blocking calls in background"
        10                      | 1                    | 20                | "10 blocking calls in background"
        20                      | 1                    | 20                | "20 blocking calls in background"
    }


    private List<Future<Long>> get(String endpoint, ExecutorService threadPool, Integer concurrency) {
        List<Future<Long>> futures = []

        concurrency.times {
            futures.add(threadPool.submit(new Callable<Long>() {
                @Override
                Long call() throws Exception {
                    def time = RestAssured
                            .given()
                            .header("Content-Type", "application/json")
                            .get(endpoint)
                            .time()

                    println "$endpoint - $time"

                    return time
                }
            }))
        }
        futures
    }
}
