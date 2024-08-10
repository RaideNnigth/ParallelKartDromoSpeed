package parallelSpeed.server;

import parallelSpeed.client.Rider;
import parallelSpeed.client.RiderType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;

public class Kartodromo {
    private final static int NUM_HELMETS = 10;
    private final static int NUM_KARTS = 10;
    private final static int MIN_PILOTS = 1;
    private final static int MAX_PILOTS = 5;
    private final static int MINUTES_OF_DAY = 8 * 60;

    private final static Random RANDOM = new Random();
    private static boolean END_OF_DAY = false;

    private static final Semaphore helmets = new Semaphore(NUM_HELMETS);
    private static final Semaphore karts = new Semaphore(NUM_KARTS);
    private static final PriorityBlockingQueue<Rider> waitingForHelmet = new PriorityBlockingQueue<>();
    // Order by having helmet or kart, then by arrival time
    private static final Comparator<Rider> kartComparator = (r1, r2) -> {
        if (r1.helmet && !r2.helmet) {
            return -1;
        } else if (!r1.helmet && r2.helmet) {
            return 1;
        }
        if (r1.kart && !r2.kart) {
            return -1;
        } else if (!r1.kart && r2.kart) {
            return 1;
        }
        return 0;
    };

    private static final PriorityBlockingQueue<Rider> waitingForKart = new PriorityBlockingQueue<>(11, kartComparator);
    private static final Rider POISON_PILL = new Rider("-1", null); // Special rider object acting as a poison pill

    // Report variables
    private static final ArrayList<Rider> RIDERS = new ArrayList<>();
    public static final ArrayList<Thread> RIDER_THREADS = new ArrayList<>();
    private static int TOTAL_RIDERS = 0;
    private static int TOTAL_U14_KIDS = 0;
    private static int TOTAL_KIDS = 0;
    private static int TOTAL_ADULTS = 0;
    private static int HELMET_USAGE = 0;
    private static int KART_USAGE = 0;
    private static long AVG_WAITING_TIME = 0;

    public void working() throws InterruptedException {
        // New thread for getNextInLineForHelmet
        Thread manageLineHelmet = new Thread(this::manageHelmetQueue);
        manageLineHelmet.start();
        Thread manageLineKart = new Thread(this::manageKartQueue);
        manageLineKart.start();

        for (int i = 0; i < MINUTES_OF_DAY; i++) {
            int shouldCreateGroup = RANDOM.nextInt(0, 10);
            if (shouldCreateGroup <= 2) {
                int numPilots = RANDOM.nextInt(MIN_PILOTS, MAX_PILOTS);
                System.out.println("Creating group of " + numPilots + " pilots at minute " + i);
                createGroup(numPilots, i);
                Thread.sleep(1000);
            }
        }
        long lastMinute = System.currentTimeMillis();
        END_OF_DAY = true;
        // Add poison pills to queues to stop the threads
        waitingForHelmet.offer(POISON_PILL);
        waitingForKart.offer(POISON_PILL);
        System.out.println("--------------------End of day---------------------");

        System.out.println("Waiting for threads to finish...");
        manageLineHelmet.join();
        System.out.println("Helmet thread finished");
        manageLineKart.join();
        System.out.println("Kart thread finished");
        for (Thread riderThread : RIDER_THREADS) {
            riderThread.join();
        }
        System.out.println("All rider threads finished");

        report(lastMinute);
    }

    private static void createGroup(int numPilots, int minuteOfTheDay) {
        for (int i = 0; i < numPilots; i++) {
            int age = RANDOM.nextInt(11, 22);
            if (age <= 14) {
                RIDERS.add(new Rider(minuteOfTheDay + "-" + i, RiderType.U14_KID));
                TOTAL_U14_KIDS++;
            } else if (age <= 18) {
                RIDERS.add(new Rider(minuteOfTheDay + "-" + i, RiderType.KID));
                TOTAL_KIDS++;
            } else {
                RIDERS.add(new Rider(minuteOfTheDay + "-" + i, RiderType.ADULT));
                TOTAL_ADULTS++;
            }
            TOTAL_RIDERS++;
        }
    }

    public void manageHelmetQueue() {
        while (true) {
            try {
                if (END_OF_DAY) {
                    break;
                }
                Rider rider = waitingForHelmet.take();
                if (rider == POISON_PILL) {
                    break;
                }
                helmets.acquire();
                rider.acquireHelmet();
                HELMET_USAGE++;
            } catch (InterruptedException e) {
                System.out.println("Error managing helmet queue");
            }
        }
    }

    public void manageKartQueue() {
        while (true) {
            try{
                if (END_OF_DAY) {
                    break;
                }
                Rider rider = waitingForKart.take();
                if (rider == POISON_PILL) {
                    break;
                }
                karts.acquire();
                rider.acquireKart();
                KART_USAGE++;
            } catch (InterruptedException e) {
                System.out.println("Error managing kart queue");
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
        helmets.release();
    }

    public static void releaseKart() {
        karts.release();
    }

    public static void report(long lastMinute) {
        for (Rider rider : RIDERS) {
            if (rider.getWaitingTime() != -1) {
                AVG_WAITING_TIME += rider.getWaitingTime();
                System.out.println("Rider " + rider.getId() + " waited for " + rider.getWaitingTime() + "ms.");
            } else {
                AVG_WAITING_TIME += lastMinute - rider.getArrivalTime();
                System.out.println("Rider " + rider.getId() + " waited for " + (lastMinute - rider.getArrivalTime()) + "ms.");
            }
        }

        System.out.println("Total riders: " + TOTAL_RIDERS);
        System.out.println("Total U14 kids: " + TOTAL_U14_KIDS);
        System.out.println("Total kids: " + TOTAL_KIDS);
        System.out.println("Total adults: " + TOTAL_ADULTS);
        System.out.println("Total helmet usage: " + HELMET_USAGE);
        System.out.println("Total kart usage: " + KART_USAGE);
        System.out.println("Average waiting time: " + AVG_WAITING_TIME / TOTAL_RIDERS);
    }

}
