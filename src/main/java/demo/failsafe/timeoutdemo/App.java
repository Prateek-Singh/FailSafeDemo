package demo.failsafe.timeoutdemo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import demo.failsafe.mockservice.MockService;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.Fallback;
import net.jodah.failsafe.RetryPolicy;
import net.jodah.failsafe.Timeout;
import net.jodah.failsafe.TimeoutExceededException;
import net.jodah.failsafe.event.ExecutionAttemptedEvent;
import net.jodah.failsafe.event.ExecutionCompletedEvent;
import net.jodah.failsafe.function.CheckedConsumer;

@RunWith(MockitoJUnitRunner.class)
public class App {

	private static final Logger log = LoggerFactory.getLogger(App.class);
	
	private static final String EXPECTED_VALUE = "Expected Value";
	private static final String FALLBACK_VALUE = "Fallback Value";
	
	private final CheckedConsumer<ExecutionAttemptedEvent<String>> printAttemptedCountAndExceptionMsg = e -> {
		log.info("Timeout Demo Attempt : {}", e.getAttemptCount());
	}; 
	
	private final CheckedConsumer<ExecutionCompletedEvent<String>> printTimeoutExceededExceptionMsg = e -> {
		log.info("Timed out with TimeoutExceededException");
	};
	
	@Mock
	MockService mockService;
	
	@Before
	public void setUp() {
		mockService = () -> {
			try {
				Thread.sleep(200);
//				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
			return EXPECTED_VALUE;
		};
	}

	@Test
    public void testTimeOut() {
        RetryPolicy<String> retryPolicy = new RetryPolicy<>();
        retryPolicy.withDelay(Duration.ofMillis(100));
        retryPolicy.withMaxAttempts(3);
        retryPolicy.handle(TimeoutExceededException.class);
        retryPolicy.onRetry(printAttemptedCountAndExceptionMsg);

        Timeout<String> timeout = Timeout.of(Duration.ofMillis(100));
        timeout.withCancel(true);
        timeout.onFailure(printTimeoutExceededExceptionMsg);
        
        Fallback<String> fallback = Fallback.of(FALLBACK_VALUE);

        String actualValue = Failsafe.with(fallback, retryPolicy, timeout).get(mockService::mockBehavior);
        assertThat(actualValue).isEqualTo(FALLBACK_VALUE);
//        assertThat(actualValue).isEqualTo(EXPECTED_VALUE);
    }
}