package micronaut.http;

import io.micronaut.core.annotation.Blocking;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class StatusController {

    private Logger logger = LoggerFactory.getLogger(NonBlockingStatusController.class);

    @Get("/status")
    @Blocking
    String status() throws InterruptedException {
        Thread.sleep(1000);
        return "OK -> " + Thread.currentThread().getName();
    }
}
