package parallelSpeed.server;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class HelmetQueueManager {
    private final LinkedBlockingQueue<Helmet> helmetQueue;
    private int usage = 0;

    public HelmetQueueManager(int capacity) {
        this.helmetQueue = new LinkedBlockingQueue<>(capacity);
        initResources(capacity);
    }

    public Helmet acquireHelmet(int timeOut) throws InterruptedException {
        return helmetQueue.poll(timeOut, TimeUnit.MILLISECONDS); // Try to get a helmet
    }

    public void offerHelmet(Helmet helmet) {
        helmetQueue.offer(helmet);
        usage++;
    }

    private void initResources(int capacity) {
        for (int i = 0; i < capacity; i++) {
            helmetQueue.offer(new Helmet());
        }
    }

    public int getUsage() {
        return usage;
    }

}
