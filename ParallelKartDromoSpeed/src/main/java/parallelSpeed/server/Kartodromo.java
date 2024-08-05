package parallelSpeed.server;


import parallelSpeed.client.Rider;
import parallelSpeed.client.RiderState;
import parallelSpeed.client.RiderType;

import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Kartodromo {

    private final static int NUM_HELMETS = 10;
    private final static int NUM_KARTS = 10;
    private final static int MAX_WAITING_TIME = 1000;
    private final static int MINUTES_OF_DAY = 8 * 60;
    private final static int MIN_PILOTS = 4;
    private final static int MAX_PILOTS = 10;
    private final static Random RANDOM = new Random();


    private static final Semaphore helmets = new Semaphore(NUM_HELMETS);
    ;
    private static final Semaphore karts = new Semaphore(NUM_KARTS);

    private static final ReentrantLock helmetsLock = new ReentrantLock();
    private static final Condition helmetsCondition = helmetsLock.newCondition();

    private static final ReentrantLock kartsLock = new ReentrantLock();
    private static final Condition kartsCondition = kartsLock.newCondition();

    private static final PriorityBlockingQueue<Rider> waitingForHelmet = new PriorityBlockingQueue<>();
    private static final PriorityBlockingQueue<Rider> waitingForKart = new PriorityBlockingQueue<>();

    private static final LinkedBlockingQueue<Rider> arrivedRiders = new LinkedBlockingQueue<>();

    public void working() throws InterruptedException {
        for (int i = 0; i < MINUTES_OF_DAY; i++) {
            int numPilots = RANDOM.nextInt(MIN_PILOTS, MAX_PILOTS);
            int shouldCreateGroup = RANDOM.nextInt(0, 10);
            if (shouldCreateGroup <= 1) {
                System.out.println("Creating group of " + numPilots + " pilots at minute " + i);
                createGroup(numPilots, i);
                Thread.sleep(1000);
            }
        }
    }

    private static void createGroup(int numPilots, int minuteOfTheDay) {
        for (int i = 0; i < numPilots; i++) {
            int age = RANDOM.nextInt(8, 20);
            Rider rider;
            if (age <= 14) {
                rider = new Rider(minuteOfTheDay + i, RiderType.KID);
            } else {
                rider = new Rider(minuteOfTheDay + i, RiderType.ADULT);
            }
            arrivedRider(rider);
            Thread thread = new Thread(() -> {
                try {
                    rider.tryAcquireResources();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        }
    }

    public static void arrivedRider(Rider rider) {
        arrivedRiders.offer(rider);
    }

    private static void getInLineForHelmet(Rider rider) {
        waitingForHelmet.offer(rider);
    }

    private static void getInLineForKart(Rider rider) {
        waitingForKart.offer(rider);
    }

    public static void tryAcquireResourcesForKid(Rider rider) throws InterruptedException {
        assert rider != null;

        getInLineForHelmet(rider);
        helmetsLock.lock();
        while (waitingForHelmet.peek() != rider) {
            try {
                helmetsCondition.await();
            } catch (InterruptedException e) {
                System.out.println("Rider " + rider.getId() + " was interrupted while waiting for helmet");
            }
        }
        waitingForHelmet.poll();
        helmets.acquire();
        long helmetAcquiredAt = System.currentTimeMillis();
        helmetsLock.unlock();

        getInLineForKart(rider);
        kartsLock.lock();
        while (waitingForKart.peek() != rider) {
            try {
                if (System.currentTimeMillis() - helmetAcquiredAt > MAX_WAITING_TIME) {
                    releaseHelmet();
                    rider.setState(RiderState.WAITING_FOR_RESOURCES);
                    getInLineForHelmet(rider);
                    kartsLock.unlock();
                    break;
                }
                kartsCondition.await();
            } catch (InterruptedException e) {
                System.out.println("Rider " + rider.getId() + " was interrupted while waiting for kart");
            }
        }
        waitingForKart.poll();
        karts.acquire();
        rider.setState(RiderState.READY_TO_RUN);
        kartsLock.unlock();
    }

    public static void tryAcquireResourcesForAdult(Rider rider) throws InterruptedException {
        assert rider != null;

        getInLineForKart(rider);
        kartsLock.lock();
        while (waitingForKart.peek() != rider) {
            try {
                kartsCondition.await();
            } catch (InterruptedException e) {
                System.out.println("Rider " + rider.getId() + " was interrupted while waiting for kart");
            }
        }
        waitingForKart.poll();
        karts.acquire();
        long kartAcquiredAt = System.currentTimeMillis();
        kartsLock.unlock();

        getInLineForHelmet(rider);
        helmetsLock.lock();
        while (waitingForHelmet.peek() != rider) {
            try {
                if (System.currentTimeMillis() - kartAcquiredAt > MAX_WAITING_TIME) {
                    releaseKart();
                    rider.setState(RiderState.WAITING_FOR_RESOURCES);
                    getInLineForKart(rider);
                    helmetsLock.unlock();
                    break;
                }
                helmetsCondition.await();
            } catch (InterruptedException e) {
                System.out.println("Rider " + rider.getId() + " was interrupted while waiting for helmet");
            }
        }
        waitingForHelmet.poll();
        helmets.acquire();
        rider.setState(RiderState.READY_TO_RUN);
        helmetsLock.unlock();
    }

    public static void releaseHelmet() {
        helmetsLock.lock();
        try {
            helmets.release();
            System.out.println("Helmet released");
            helmetsCondition.signalAll();
        } finally {
            helmetsLock.unlock();
        }
    }

    public static void releaseKart() {
        kartsLock.lock();
        try {
            karts.release();
            System.out.println("Kart released");
            kartsCondition.signalAll();
        } finally {
            kartsLock.unlock();
        }
    }
}
