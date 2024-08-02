package parallelSpeed;

import parallelSpeed.client.Person;
import parallelSpeed.server.HelmetQueueManager;
import parallelSpeed.server.KartQueueManager;
import parallelSpeed.server.PriorityQueueManager;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

    private static String NAME_SYNTAX = "P_%d-%d";
    private static int HOW_MANY_PEOPLE_RAN = 0;
    private static int HOW_MANY_PEOPLE = 0;
    private static long LAST_RUN_WAS_AT = 0;

    private static final int KART_QUANTITY = 10;
    private static final int HELMET_QUANTITY = 10;

    private final static HelmetQueueManager HELMET_QUEUE_MANAGER = new HelmetQueueManager(HELMET_QUANTITY);
    private final static KartQueueManager KART_QUEUE_MANAGER = new KartQueueManager(KART_QUANTITY);
    private final static PriorityQueueManager PRIORITY_QUEUE_MANAGER = new PriorityQueueManager(HELMET_QUEUE_MANAGER, KART_QUEUE_MANAGER);

    private static final LinkedBlockingQueue<Person> WHO_RAN = new LinkedBlockingQueue<>();
    private static final ArrayList<Thread> threads = new ArrayList<>();

    public static void main(String[] args) {
        int minimalAge = 8;
        int maxAge = 20;

        int minutesOffDay = 8 * 60; // 8 hours or 480 minutes
        int changeOfGettingIn = 10; // % chance of getting in per minute
        int eachMinuteIs = 1000; // 1 second in real time for simulation

        while (minutesOffDay-- > 0) {
            if (new Random().nextInt(100) < changeOfGettingIn) {
                createRandomNumberOfThreads(minutesOffDay, minimalAge, maxAge);
            }
            try {
                Thread.sleep(eachMinuteIs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            for (Thread thread : threads) {
                thread.join();
            }
            LAST_RUN_WAS_AT = System.currentTimeMillis();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("How many people get to line: " + HOW_MANY_PEOPLE);
        System.out.println("How many people get to Run: " + HOW_MANY_PEOPLE_RAN);
        System.out.println("In % would be: " + (HOW_MANY_PEOPLE_RAN * 100) / HOW_MANY_PEOPLE + "%");

        float averageWaitTime = 0;
        float averageWaitTimeForRan = 0;
        for (Person person : WHO_RAN) {
            averageWaitTimeForRan += person.getWaitingTime();
        }
        averageWaitTime += averageWaitTimeForRan;
        averageWaitTimeForRan /= HOW_MANY_PEOPLE_RAN;
        System.out.println("Average Wait Time for people that Ran: " + averageWaitTimeForRan + "ms.");

        float averageWaitTimeForNotRan = 0;
        float biggestWaitTimeForNotRan = 0;
        for (Person person : PRIORITY_QUEUE_MANAGER.getPriorityQueue()) {
            averageWaitTimeForNotRan += LAST_RUN_WAS_AT - person.getArrivalTime();
            if (biggestWaitTimeForNotRan < LAST_RUN_WAS_AT - person.getArrivalTime()) {
                biggestWaitTimeForNotRan = LAST_RUN_WAS_AT - person.getArrivalTime();
            }
        }
        averageWaitTime += averageWaitTimeForNotRan;
        averageWaitTimeForNotRan /= PRIORITY_QUEUE_MANAGER.getPriorityQueue().size();
        System.out.println("Average Wait Time for people that have not Ran: " + averageWaitTimeForNotRan + "ms.");
        System.out.println("Biggest Wait Time for people that have not Ran: " + biggestWaitTimeForNotRan + "ms.");

        averageWaitTime /= HOW_MANY_PEOPLE + PRIORITY_QUEUE_MANAGER.getPriorityQueue().size();
        System.out.println("Average Wait Time for all people: " + averageWaitTime + "ms.");

        System.out.println("Total time: " + (System.currentTimeMillis() - LAST_RUN_WAS_AT) + "ms.");
        System.out.println("Helmet Usage: " + HELMET_QUEUE_MANAGER.getUsage());
        System.out.println("Kart Usage: " + KART_QUEUE_MANAGER.getUsage());

    }

    private static void createRandomNumberOfThreads(
            int minutesOffDay,
            int minimalAge,
            int maxAge
    ) {
        Random random = new Random();
        int numOfThreads = random.nextInt(5, 10);
        System.out.println("Group of " + numOfThreads + " has arrived!");

        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < numOfThreads; i++) {
            String name = String.format(NAME_SYNTAX, minutesOffDay, i);
            int age = new Random().nextInt(minimalAge, maxAge + 1);
            Person person = new Person(name, age);
            PRIORITY_QUEUE_MANAGER.offerPerson(person);
            createNewThread();
        }
    }

    private static void createNewThread() {
        // Create a new thread
        Thread thread = new Thread(() -> {
            try {
                Person couldRun = PRIORITY_QUEUE_MANAGER.runNextInQueue();
                if (couldRun != null) {
                    HOW_MANY_PEOPLE_RAN++;
                    WHO_RAN.offer(couldRun);
                }
                HOW_MANY_PEOPLE++;

            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println(Thread.currentThread().getName() + " was interrupted.");
            }
        });
        thread.setPriority(Thread.MAX_PRIORITY);
        // Start the thread
        thread.start();
        threads.add(thread);
    }
}