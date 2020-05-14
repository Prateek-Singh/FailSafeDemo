package demo.failsafe.retrydemo;

import java.time.Duration;

import demo.failsafe.mockservice.MockService;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

public class App {

    MockService service = new MockService();

    public static void main(String[] args) {
        new App().demoRetry();    
    }

    private void demoRetry() {
        RetryPolicy<Integer> retryPolicy = new RetryPolicy<>();
        retryPolicy.withDelay(Duration.ofMillis(100));
        retryPolicy.withMaxAttempts(3);
        retryPolicy.handle(IllegalAccessException.class);
        retryPolicy.onFailedAttempt(e -> System.err.println("Error : " + e.getLastFailure().getMessage()));
        //retryPolicy.onFailure(e -> System.err.println("Failure : " + e.getFailure().getMessage()));

        Integer val = Failsafe.with(retryPolicy).get(service::getCounterValueForRetry);
        System.out.println("Value : " + val);
    }
}