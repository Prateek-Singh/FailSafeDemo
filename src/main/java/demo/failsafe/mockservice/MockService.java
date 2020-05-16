package demo.failsafe.mockservice;

import java.util.UUID;

public class MockService {
    private int ctr = 0;

    public int mockBehaviorForRetryPolicy() throws IllegalAccessException {
        ctr++;
        if (ctr != 3) {
            throw new IllegalAccessException("Counter is not 3");
        }
        if (ctr == 3) {
            ctr = 0;
        }
        return 3;
    }

    public int mockBehaviorForFallbackPolicy() throws IllegalAccessException {
        ctr++;
        if(ctr < 3) {
            throw new RuntimeException("Counter is not 3");
        }
        ctr = 0;
        throw new IllegalAccessException("Counter is not returned");
    }

    public int mockBehaviorForTimeoutPolicy() throws InterruptedException {
        ctr++;
        if(ctr < 3) {
            Thread.sleep(10000);
        }
        if (ctr == 3) {
            ctr = 0;
        }
        return 3;
    }

    public String mockBehaviorForCircuitBreakerPolicy() {
        ctr++;
        if(ctr < 3 || ctr == 5) {
            throw new RuntimeException("Counter is not 3");
        }
        return UUID.randomUUID().toString();
    }
}