package demo.failsafe.timeoutdemo;

import java.time.Duration;

import demo.failsafe.mockservice.MockService;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import net.jodah.failsafe.Timeout;
import net.jodah.failsafe.TimeoutExceededException;

public class App {

    MockService service = new MockService();

    public static void main(String[] args) {
        new App().demoTimeOut();    
    }

    private void demoTimeOut() {
        RetryPolicy<Integer> retryPolicy = new RetryPolicy<>();
        retryPolicy.withDelay(Duration.ofMillis(100));
        retryPolicy.withMaxAttempts(5);
        retryPolicy.handle(TimeoutExceededException.class);
        retryPolicy.onRetry(e -> System.out.println("Retry : " + e.getAttemptCount()));

        Timeout<Integer> timeout = Timeout.of(Duration.ofSeconds(1));
        timeout.withCancel(true);
        timeout.onFailure(e -> System.err.println("Backing out after 1 second"));

        Integer val = Failsafe.with(retryPolicy, timeout).get(service::getCounterValueForTimeOut);
        System.out.println("Value : " + val);
    }
}