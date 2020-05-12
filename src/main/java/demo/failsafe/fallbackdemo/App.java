package demo.failsafe.fallbackdemo;

import java.time.Duration;

import demo.failsafe.mockservice.MockService;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.Fallback;
import net.jodah.failsafe.RetryPolicy;

public class App {

    MockService service = new MockService();

    public static void main(String[] args) {
        new App().demoFallback();    
    }

    private void demoFallback() {
        RetryPolicy<Integer> retryPolicy = new RetryPolicy<>();
        retryPolicy.withDelay(Duration.ofSeconds(1));
        retryPolicy.withMaxAttempts(5);
        retryPolicy.handle(RuntimeException.class);

        Fallback<Integer> fallback = Fallback.of(10);

        Integer val = Failsafe.with(fallback, retryPolicy).get(service::getCounterValueForFallback);
        System.out.println("Value : " + val);
    }
}