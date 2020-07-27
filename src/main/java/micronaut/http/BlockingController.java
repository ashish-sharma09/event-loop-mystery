package micronaut.http;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class BlockingController {

    @Get("/blocking")
    String blocking() throws InterruptedException {
        Thread.sleep(1000);
        return "OK -> " + Thread.currentThread().getName();
    }
}
