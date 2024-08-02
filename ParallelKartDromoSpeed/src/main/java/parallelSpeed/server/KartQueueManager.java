package parallelSpeed.server;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class KartQueueManager {

    private final LinkedBlockingQueue<Kart> kartQueue;
    private int usage = 0;

    public KartQueueManager(int capacity) {
        this.kartQueue = new LinkedBlockingQueue<>(capacity);
        initResources(capacity);
    }

    public Kart acquireKart(int timeOut) throws InterruptedException {
        return kartQueue.poll(timeOut, TimeUnit.MILLISECONDS); // Try to get a kart
    }

    public void offerKart(Kart kart) {
        kartQueue.offer(kart);
        usage++;
    }

    private void initResources(int capacity) {
        for (int i = 0; i < capacity; i++) {
            kartQueue.offer(new Kart());
        }
    }

    public int getUsage() {
        return usage;
    }

}
