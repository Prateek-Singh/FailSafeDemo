package demo.failsafe.retrydemo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;

import java.time.Duration;

import javax.xml.ws.WebServiceException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import demo.failsafe.mockservice.MockService;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import net.jodah.failsafe.event.ExecutionAttemptedEvent;
import net.jodah.failsafe.event.ExecutionCompletedEvent;
import net.jodah.failsafe.function.CheckedConsumer;

@RunWith(MockitoJUnitRunner.class)
public class App {
	
	private static final String EXCEPTION_MSG = "Can't access the Resource at the moment";
	private static final String UNSUPPORTED_EXCEPTION_MSG = "Can't support this operation";

	private static final String EXPECTED_VALUE = "Expected Value";
	
	private final CheckedConsumer<ExecutionAttemptedEvent<String>> printAttemptedCountAndExceptionMsg = e -> {
		System.err.println("Retry Demo Exception : " + e.getLastFailure().getMessage() + ", Attempt : " + e.getAttemptCount());
	}; 
	
	private final CheckedConsumer<ExecutionCompletedEvent<String>> printFailedExceptionMsg = e -> {
		System.err.println("Failed Exception : " + e.getFailure().getMessage());
	}; 
	
	@Mock
	MockService mockService;
	
	WebServiceException webServiceException;
	UnsupportedOperationException unsupportedOperationException;
	
	@Before
	public void setUp() {
		webServiceException = new WebServiceException(EXCEPTION_MSG);
		unsupportedOperationException = new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
		
		doThrow(webServiceException)
		.doThrow(unsupportedOperationException)
//		.doThrow(new RuntimeException(UNSUPPORTED_EXCEPTION_MSG))
		.doReturn(EXPECTED_VALUE).when(mockService).mockBehavior();
	}

	@Test
	public void testRetry() {		
		RetryPolicy<String> retryPolicy = new RetryPolicy<>();
		retryPolicy.withDelay(Duration.ofMillis(100));
		retryPolicy.withMaxAttempts(3);
		retryPolicy.handle(WebServiceException.class, UnsupportedOperationException.class);
		
		retryPolicy.onFailedAttempt(printAttemptedCountAndExceptionMsg);
		retryPolicy.onFailure(printFailedExceptionMsg);

		String actualValue = Failsafe.with(retryPolicy).get(mockService::mockBehavior);
		assertThat(actualValue).isEqualTo(EXPECTED_VALUE);
		
//		assertThatThrownBy(() -> {
//			Failsafe.with(retryPolicy).get(mockService::mockBehavior);
//		}).isInstanceOf(RuntimeException.class)
//		.hasMessageContaining(UNSUPPORTED_EXCEPTION_MSG);
	}
}