package demo.failsafe.circuitbreakerdemo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;

import java.time.Duration;
import java.util.UUID;

import javax.xml.ws.WebServiceException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import demo.failsafe.mockservice.MockService;
import net.jodah.failsafe.CircuitBreaker;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import net.jodah.failsafe.event.ExecutionAttemptedEvent;
import net.jodah.failsafe.event.ExecutionCompletedEvent;
import net.jodah.failsafe.function.CheckedConsumer;
import net.jodah.failsafe.function.CheckedRunnable;

@RunWith(MockitoJUnitRunner.class)
public class App {
	
	private static final CheckedRunnable printBreakerIsOpen = () -> System.out.println("Breaker is Open now");
	private static final CheckedRunnable printBreakerIsClosed = () -> System.out.println("Breaker is Closed now");
	private static final CheckedRunnable printBreakerIsHalfOpen = () -> System.out.println("Breaker is HalfOpen now");

	private static final String EXCEPTION_MSG = "Can't access the Resource at the moment";
	private static final String EXPECTED_UUID = UUID.randomUUID().toString();
	
	@Mock
    MockService mockService;
	
	WebServiceException webServiceException;
	
	CircuitBreaker<String> breaker;
	
	private final CheckedConsumer<ExecutionAttemptedEvent<String>> printAttemptMsg = e -> {
		System.out.println("Attempt " + e.getAttemptCount() + " failed, Breaker state now : " + breaker.getState());
	};
	
	private final CheckedConsumer<ExecutionCompletedEvent<String>> printSuccesstMsg = e -> {
		System.out.println("Attempt " + e.getAttemptCount() + " Succeeded, Breaker state now : " + breaker.getState());
	};
	
	@Before
	public void setUp() {
		webServiceException = new WebServiceException(EXCEPTION_MSG);
		
		doThrow(webServiceException)
		.doThrow(webServiceException)
		.doReturn(EXPECTED_UUID)
		.doReturn(EXPECTED_UUID)
		.doReturn(EXPECTED_UUID)
		.doThrow(webServiceException)
		.doThrow(webServiceException)
		.doReturn(EXPECTED_UUID)
		.doReturn(EXPECTED_UUID)
		.doReturn(EXPECTED_UUID)
		.when(mockService).mockBehavior();
	}

	@Test
    public void testCircuitBreaker() {
		breaker = new CircuitBreaker<>();
		breaker.withFailureThreshold(2);
		breaker.withSuccessThreshold(3);
		breaker.withDelay(Duration.ofMillis(50));
		breaker.onOpen(printBreakerIsOpen);
		breaker.onClose(printBreakerIsClosed);
		breaker.onHalfOpen(printBreakerIsHalfOpen);

		RetryPolicy<String> retryPolicy = new RetryPolicy<>();
		retryPolicy.withMaxRetries(2);
		retryPolicy.handle(WebServiceException.class);
		retryPolicy.withDelay(Duration.ofMillis(100));
		retryPolicy.onFailedAttempt(printAttemptMsg);
		retryPolicy.onSuccess(printSuccesstMsg);

		for(int idx = 0; idx < 7; idx++) {
			System.out.println("Breaker State before the call : " + breaker.getState() + " for : " + idx);
			String actualUUID = Failsafe.with(retryPolicy, breaker).get(mockService::mockBehavior);
			assertThat(actualUUID).isEqualTo(EXPECTED_UUID);
		}
    }
}