package parallelSpeed.server;

import parallelSpeed.client.Rider;
import parallelSpeed.client.RiderType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.*;

public class Kartodromo {
    // State variables
    private final static int NUM_HELMETS = 10;
    private final static int NUM_KARTS = 10;
    private final static int MIN_PILOTS = 4;
    private final static int MAX_PILOTS = 10;
    private final static int MIN_AGES = 12;
    private final static int MAX_AGES = 23;
    private final static int MINUTES_OF_DAY = 8 * 60;

    // Report variables
    private static final ArrayList<Rider> RIDERS = new ArrayList<>();
    public static final LinkedBlockingQueue<Thread> RIDER_THREADS = new LinkedBlockingQueue<>();
    private static int TOTAL_RIDERS = 0;
    private static int TOTAL_U14_KIDS = 0;
    private static int TOTAL_KIDS = 0;
    private static int TOTAL_ADULTS = 0;
    private static int HELMET_USAGE = 0;
    private static int KART_USAGE = 0;
    private static long AVG_WAITING_TIME = 0;

    // Random number generator
    private final static Random RANDOM = new Random();

    // Semaphores
    private static final Semaphore helmets = new Semaphore(NUM_HELMETS);
    private static final Semaphore karts = new Semaphore(NUM_KARTS);

    // Order by having helmet or kart, then by arrival time
    private static final Comparator<Rider> kartComparator = (r1, r2) -> {
        if (r1 == r2) {
            return 0;
        }

        if (r1.hasHelmet() && !r2.hasHelmet()) {
            return -1;
        } else if (!r1.hasHelmet() && r2.hasHelmet()) {
            return 1;
        }

        return r1.getKartPriority() < r2.getKartPriority() ? -1 : 1;
    };
    private static final PriorityBlockingQueue<Rider> waitingForKart = new PriorityBlockingQueue<>(11, kartComparator);
    private static final PriorityBlockingQueue<Rider> waitingForHelmet = new PriorityBlockingQueue<>();

    // Stop managing queues and flux control variables
    private static volatile boolean STOP_MANAGING_HELMETS = false;
    private static volatile boolean STOP_MANAGING_KARTS = false;
    private static int CURRENT_MINUTE = 0;
    private static final int TIMEOUT_WAITING_FOR_PERSON_IN_KART_QUEUE = 3;
    private static final int TIMEOUT_WAITING_FOR_PERSON_IN_HELMET_QUEUE = 1;

    public void working() throws InterruptedException {
        // New thread for getNextInLineForHelmet
        Thread manageLineHelmet = new Thread(this::manageHelmetQueue);
        manageLineHelmet.start();
        Thread manageLineKart = new Thread(this::manageKartQueue);
        manageLineKart.start();

        for (int i = 0; i < MINUTES_OF_DAY; i++) {
            CURRENT_MINUTE = i;
            int shouldCreateGroup = RANDOM.nextInt(0, 10);
            if (shouldCreateGroup <= 2) {
                int numPilots = RANDOM.nextInt(MIN_PILOTS, MAX_PILOTS);
                System.out.println("Creating group of " + numPilots + " pilots at minute " + i);
                createGroup(numPilots, i);
            }
            Thread.sleep(1000);
        }
        STOP_MANAGING_HELMETS = true;
        STOP_MANAGING_KARTS = true;

        for (Thread t : RIDER_THREADS) {
            t.join();
        }

        System.out.println("--------------------End of day---------------------");
        System.out.println("Waiting for threads to finish...");
        System.out.println("All rider threads finished");

        manageLineHelmet.join();
        System.out.println("Helmet manage queue thread finished");
        manageLineKart.join();
        System.out.println("Kart manage queue thread finished");

        report();
    }

    private static void createGroup(int numPilots, int minuteOfTheDay) {
        for (int i = 0; i < numPilots; i++) {
            int age = RANDOM.nextInt(MIN_AGES, MAX_AGES);
            if (age <= 14) {
                RIDERS.add(new Rider(minuteOfTheDay + "-" + i, RiderType.U14_KID, minuteOfTheDay));
                TOTAL_U14_KIDS++;
            } else if (age <= 18) {
                RIDERS.add(new Rider(minuteOfTheDay + "-" + i, RiderType.KID, minuteOfTheDay));
                TOTAL_KIDS++;
            } else {
                RIDERS.add(new Rider(minuteOfTheDay + "-" + i, RiderType.ADULT, minuteOfTheDay));
                TOTAL_ADULTS++;
            }
            TOTAL_RIDERS++;
        }
    }

    public void manageHelmetQueue() {
        while (!STOP_MANAGING_HELMETS) {
            try {
                Rider rider = waitingForHelmet.poll(TIMEOUT_WAITING_FOR_PERSON_IN_HELMET_QUEUE, TimeUnit.SECONDS);
                if (rider != null) {
                    helmets.acquire();
                    HELMET_USAGE++;
                    rider.acquireHelmet(CURRENT_MINUTE);
                }
            } catch (InterruptedException e) {
                System.out.println("Helmet queue interrupted");
            }
        }
    }

    public void manageKartQueue() {
        while (!STOP_MANAGING_KARTS) {
            try {
                Rider rider = waitingForKart.poll(TIMEOUT_WAITING_FOR_PERSON_IN_KART_QUEUE, TimeUnit.SECONDS);
                if (rider != null) {
                    karts.acquire();
                    KART_USAGE++;
                    rider.acquireKart(CURRENT_MINUTE);
                }
            } catch (InterruptedException e) {
                System.out.println("Kart queue interrupted");
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

    public static void report() {
        for (Rider rider : RIDERS) {
            if (rider.getWaitingTime() != -1) {
                AVG_WAITING_TIME += rider.getWaitingTime();
            } else {
                AVG_WAITING_TIME += MINUTES_OF_DAY - rider.getArrivalTime();
            }
        }

        int kartQueueSize = waitingForKart.isEmpty() ? 1 : waitingForKart.size();
        System.out.println("Kart queue size: " + kartQueueSize);
        long avgStillWaitingTimeForKart = 0;
        for (Rider rider : waitingForKart) {
            avgStillWaitingTimeForKart += MINUTES_OF_DAY - rider.getArrivalTime();
            System.out.println("Rider " + rider.getId() + " age: " + rider.getType() + " waiting time: " + (MINUTES_OF_DAY - rider.getArrivalTime() + " minutes"
                    + " has helmet: " + rider.hasHelmet() + " has kart: " + rider.hasKart()));
        }
        avgStillWaitingTimeForKart /= (waitingForKart.size() + 1);

        int helmetQueueSize = waitingForHelmet.isEmpty() ? 1 : waitingForHelmet.size();
        System.out.println("Helmet queue size: " + helmetQueueSize);
        long avgStillWaitingTimeForHelmet = 0;
        for (Rider rider : waitingForHelmet) {
            avgStillWaitingTimeForHelmet += MINUTES_OF_DAY - rider.getArrivalTime();
            System.out.println("Rider " + rider.getId() + " age: " + rider.getType() + " waiting time: " + (MINUTES_OF_DAY - rider.getArrivalTime()) + " minutes"
                    + " has helmet: " + rider.hasHelmet() + " has kart: " + rider.hasKart());
        }
        avgStillWaitingTimeForHelmet /= (waitingForHelmet.size() + 1);

        System.out.println("Total riders: " + TOTAL_RIDERS);
        System.out.println("Total U14 kids: " + TOTAL_U14_KIDS);
        System.out.println("Total kids: " + TOTAL_KIDS);
        System.out.println("Total adults: " + TOTAL_ADULTS);
        System.out.println("Total helmet usage: " + HELMET_USAGE);
        System.out.println("Total kart usage: " + KART_USAGE);
        System.out.println("Average waiting time: " + AVG_WAITING_TIME / TOTAL_RIDERS);
        System.out.println("Still waiting for helmet: " + (helmetQueueSize - 1));
        System.out.println("Still waiting for kart: " + (kartQueueSize - 1));
        System.out.println("Average still waiting time for helmet: " + avgStillWaitingTimeForHelmet);
        System.out.println("Average still waiting time for kart: " + avgStillWaitingTimeForKart);
    }

}
