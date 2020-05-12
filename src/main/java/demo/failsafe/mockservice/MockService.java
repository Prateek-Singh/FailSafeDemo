package demo.failsafe.mockservice;

public class MockService {
    private int ctr = 0;

    public int getCounterValueForRetry() throws IllegalAccessException {
        ctr++;
        if (ctr != 3) {
            //System.out.println("ctr is " + ctr + " and not equal to 3 hence throwing exception");
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
            //System.out.println("ctr is " + ctr + " and not equal to 3 hence throwing exception");
            throw new RuntimeException("Counter is not 3");
        }
        ctr = 0;
        throw new IllegalAccessException("Counter is not returned");
    }

    public int getCounterValueForTimeOut() throws InterruptedException {
        ctr++;
        if(ctr < 3) {
            //System.out.println("ctr is " + ctr + " and not equal to 3 hence throwing exception");
            Thread.sleep(10000);
        }
        if (ctr == 3) {
            ctr = 0;
        }
        return 3;
    }
}