package pers.zhc.crawler;

/**
 * @author bczhc
 */
public class Demo {
    public static void main(String[] args) {
        LockCountDownLatch latch = new LockCountDownLatch(1);

        new Thread(() -> {
            System.out.println("1");
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("2");
        }).start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("3");
        latch.countDown();
        System.out.println("ok");
    }

}
