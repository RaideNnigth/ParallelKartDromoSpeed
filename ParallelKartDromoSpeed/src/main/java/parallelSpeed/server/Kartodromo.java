package parallelSpeed.server;

import parallelSpeed.client.Rider;
import parallelSpeed.client.RiderType;

import java.util.Random;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Kartodromo {
    private final static int NUM_HELMETS = 10;
    private final static int NUM_KARTS = 10;
    private final static int MIN_PILOTS = 4;
    private final static int MAX_PILOTS = 10;
    private final static int MINUTES_OF_DAY = 8 * 60;

    private final static Random RANDOM = new Random();

    private static boolean END_OF_DAY = false;

    private static final Semaphore helmets = new Semaphore(NUM_HELMETS);
    private static final Semaphore karts = new Semaphore(NUM_KARTS);

    private static final PriorityBlockingQueue<Rider> waitingForHelmet = new PriorityBlockingQueue<>();

    //TODO: COMPARATOR FOR KART QUEUE
    private static final PriorityBlockingQueue<Rider> waitingForKart = new PriorityBlockingQueue<>();

    private static final ReentrantLock helmetLock = new ReentrantLock();
    private static final Condition helmetCondition = helmetLock.newCondition();
    private static final ReentrantLock kartLock = new ReentrantLock();
    private static final Condition kartCondition = kartLock.newCondition();

    public void working() throws InterruptedException {
        // New thread for getNextInLineForHelmet
        Thread manageLineHelmet = new Thread(this::manageHelmetQueue);
        manageLineHelmet.start();
        Thread manageLineKart = new Thread(this::manageKartQueue);
        manageLineKart.start();

        for (int i = 0; i < MINUTES_OF_DAY; i++) {
            int shouldCreateGroup = RANDOM.nextInt(0, 10);
            if (shouldCreateGroup <= 1) {
                int numPilots = RANDOM.nextInt(MIN_PILOTS, MAX_PILOTS);
                System.out.println("Creating group of " + numPilots + " pilots at minute " + i);
                createGroup(numPilots, i);
                Thread.sleep(1000);
            }
        }
        END_OF_DAY = true;
    }

    private static void createGroup(int numPilots, int minuteOfTheDay) {
        for (int i = 0; i < numPilots; i++) {
            int age = RANDOM.nextInt(8, 40);
            Rider rider;
            if (age <= 14) {
                rider = new Rider(minuteOfTheDay + i, RiderType.U14_KID);
            } else if (age <= 18) {
                rider = new Rider(minuteOfTheDay + i, RiderType.KID);
            } else {
                rider = new Rider(minuteOfTheDay + i, RiderType.ADULT);
            }
            Thread riderThread = new Thread(rider);
            riderThread.start();
        }
    }

    public void manageHelmetQueue() {
        while (!END_OF_DAY || !waitingForHelmet.isEmpty()) {
            helmetLock.lock();
            try {
                while (helmets.availablePermits() < 1 && !END_OF_DAY) {
                    helmetCondition.await();
                }
                if (helmets.availablePermits() > 0 && !waitingForHelmet.isEmpty()) {
                    Rider rider = waitingForHelmet.poll();
                    rider.acquireHelmet();
                }
            } catch (InterruptedException e) {
                System.out.println("Error managing helmet queue");
            } finally {
                helmetLock.unlock();
            }
        }
    }

    public void manageKartQueue() {
        while (!END_OF_DAY) {
            kartLock.lock();
            try {
                while (karts.availablePermits() < 1 && !END_OF_DAY) {
                    kartCondition.await();
                }
                if (karts.availablePermits() > 0 && !waitingForKart.isEmpty()) {
                    Rider rider = waitingForKart.poll();
                    rider.acquireKart();
                }
            } catch (InterruptedException e) {
                System.out.println("Error managing kart queue");
            } finally {
                kartLock.unlock();
            }
        }
    }

    public static void getInLineForHelmet(Rider rider) {
        waitingForHelmet.offer(rider);
    }

    public static void getInLineForKart(Rider rider) {
        waitingForKart.offer(rider);
    }

    public static void releaseHelmet() {
        helmetLock.lock();
        try {
            helmets.release();
            helmetCondition.signal();
        } finally {
            helmetLock.unlock();
        }
    }

    public static void releaseKart() {
        kartLock.lock();
        try {
            karts.release();
            kartCondition.signal();

        } finally {
            kartLock.unlock();
        }
    }

}
