package demo.failsafe.circuitbreakerdemo;

import java.time.Duration;
import java.time.ZonedDateTime;

import demo.failsafe.mockservice.MockService;
import net.jodah.failsafe.CircuitBreaker;
import net.jodah.failsafe.CircuitBreakerOpenException;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

public class App {
    MockService service = new MockService();

    public static void main(String[] args) throws Exception {
        new App().demoCircuitBreaker();
    }

    private void demoCircuitBreaker() throws Exception {
        CircuitBreaker<String> breaker = new CircuitBreaker<>();
        breaker.withFailureThreshold(2);
        breaker.withSuccessThreshold(5);
        breaker.withDelay(Duration.ofSeconds(1));

        breaker.onClose(() -> System.out.println("Breaker is closed"));
        
        breaker.onHalfOpen(() -> System.out.println("Breaker is HalfOpen"));
        
        breaker.onOpen(() -> System.out.println("Breaker is Open"));
        
        RetryPolicy<String> retryPolicy = new RetryPolicy<>();
        retryPolicy.withDelay(Duration.ofSeconds(2));
        retryPolicy.withMaxDuration(Duration.ofSeconds(60));

        retryPolicy.onRetry(e -> System.out.printf("circuit-breaker state is: %s and retry %d\n", breaker.getState(), e.getAttemptCount()));

        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000);
            try {
                String id = Failsafe.with(retryPolicy, breaker)
                        .get(service::getCounterValueForCircuitBreaker);
                System.out.printf("FailsafeExample: id '%s' received at '%s'\n", id, ZonedDateTime.now());
            } catch (CircuitBreakerOpenException e) {
                System.out.printf("circuit-breaker is open (state %s), time is '%s'\n", breaker.getState(),
                        ZonedDateTime.now());
            }
        }
    }
}