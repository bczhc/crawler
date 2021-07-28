package pers.zhc.crawler;

/**
 * @author bczhc
 */
public class LockCountDownLatch {
    private volatile int count;

    public LockCountDownLatch(int count) {
        this.count = count;
    }

    public void countDown() {
        synchronized (this) {
            if (--count == 0) {
                this.notify();
            }
        }
    }

    public void await() throws InterruptedException {
        synchronized (this) {
            this.wait();
        }
    }

    public synchronized void resetCount(int count) {
        this.count = count;
    }
}
