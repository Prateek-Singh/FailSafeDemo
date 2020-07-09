package demo.failsafe.fallbackdemo;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import demo.failsafe.mockservice.MockService;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.Fallback;
import net.jodah.failsafe.RetryPolicy;
import net.jodah.failsafe.event.ExecutionAttemptedEvent;
import net.jodah.failsafe.event.ExecutionCompletedEvent;
import net.jodah.failsafe.function.CheckedConsumer;

@RunWith(MockitoJUnitRunner.class)
public class App {
	
	private static final Logger log = LoggerFactory.getLogger(App.class);

	private static final String EXCEPTION_MSG = "Can't access the Resource at the moment";
	private static final String UNSUPPORTED_EXCEPTION_MSG = "Can't support this operation";
	private static final String FALLBACK_EXCEPTION_MSG = "Fallback resource not available";

	private static final String EXPECTED_VALUE = "Expected Value";
	
	private final CheckedConsumer<ExecutionAttemptedEvent<String>> printAttemptedCountAndExceptionMsg = e -> {
		log.info("Fallback Demo Exception : " + e.getLastFailure().getMessage() + ", Attempt : " + e.getAttemptCount());
	}; 
	
	private final CheckedConsumer<ExecutionCompletedEvent<String>> printFailedToFallbackExceptionMsg = e -> {
		log.info("Failed to get Fallback value : " + e.getFailure().getMessage());
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
		.doThrow(unsupportedOperationException).when(mockService).mockBehavior();
	}


	@Test
    public void testFallback() {
        RetryPolicy<String> retryPolicy = new RetryPolicy<>();
        retryPolicy.withDelay(Duration.ofSeconds(1));
        retryPolicy.withMaxAttempts(2);
        retryPolicy.handle(WebServiceException.class, UnsupportedOperationException.class);
        retryPolicy.onRetry(printAttemptedCountAndExceptionMsg);

        Fallback<String> fallback = Fallback.of(EXPECTED_VALUE);
        
//        Fallback<String> fallback = Fallback.of(() -> {
//        	throw new RuntimeException(FALLBACK_EXCEPTION_MSG);
//        });
//        fallback.onFailure(printFailedToFallbackExceptionMsg);

        String actualValue = Failsafe.with(fallback, retryPolicy).get(mockService::mockBehavior);
        assertThat(actualValue).isEqualTo(EXPECTED_VALUE);
        
//        assertThatThrownBy(() -> {
//        	Failsafe.with(fallback, retryPolicy).get(mockService::mockBehavior);
//        }).isInstanceOf(RuntimeException.class)
//        .hasMessage(FALLBACK_EXCEPTION_MSG);
    }
}