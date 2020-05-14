package demo.failsafe.mockservice;

import java.util.UUID;

public class MockService {
    private int ctr = 0;

    public int getCounterValueForRetry() throws IllegalAccessException {
        ctr++;
        if (ctr != 3) {
            throw new IllegalAccessException("Counter is not 3");
        }
        if (ctr == 3) {
            ctr = 0;
        }
        return 3;
    }

    public int getCounterValueForFallback() throws IllegalAccessException {
        ctr++;
        if(ctr < 3) {
            throw new RuntimeException("Counter is not 3");
        }
        ctr = 0;
        throw new IllegalAccessException("Counter is not returned");
    }

    public int getCounterValueForTimeOut() throws InterruptedException {
        ctr++;
        if(ctr < 3) {
            Thread.sleep(10000);
        }
        if (ctr == 3) {
            ctr = 0;
        }
        return 3;
    }

    public String getCounterValueForCircuitBreaker() {
        ctr++;
        if(ctr < 3) {
            throw new RuntimeException("Counter is not 3");
        }
        return UUID.randomUUID().toString();
    }
}