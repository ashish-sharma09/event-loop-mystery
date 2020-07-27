package micronaut.http;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class NonBlockingController {

    @Get("/nonBlocking")
    Single<String> nonBlocking() {
        return Single.just(Thread.currentThread().getName());
    }
}
