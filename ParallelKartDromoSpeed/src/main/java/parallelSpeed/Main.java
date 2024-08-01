package parallelSpeed;

import parallelSpeed.client.Person;
import parallelSpeed.server.PriorityQueueManager;
import parallelSpeed.server.Stock;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

//TODO: Implement group or people getting at the same time
//TODO: Report detailed

public class Main {

    private static int HOW_MANY_PEOPLE_RAN = 0;
    private static int HOW_MANY_PEOPLE = 0;
    private static LinkedBlockingQueue<Person> WHO_RAN = new LinkedBlockingQueue<>();

    private static final ArrayList<Thread> threads = new ArrayList<>();

    public static void main(String[] args) {
        PriorityQueueManager priorityQueueManager = PriorityQueueManager.getInstance();
        Stock stock = Stock.getInstance(10, 10);

        int minimalAge = 8;
        int maxAge = 20;

        int minutesOffDay = 8 * 60; // 8 hours or 480 minutes
        int changeOfGettingIn = 10; // % chance of getting in per minute
        int eachMinuteIs = 1000; // 1 second in real time for simulation

        while (minutesOffDay-- > 0) {
            if (new Random().nextInt(100) < changeOfGettingIn) {
                createRandomNumberOfThreads(priorityQueueManager, stock, minutesOffDay, minimalAge, maxAge);
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
        } catch (InterruptedException _) {}

        System.out.println("How many people get to line: " + HOW_MANY_PEOPLE);
        System.out.println("How many people get to Run: " + HOW_MANY_PEOPLE_RAN);
        System.out.println("In % would be: " + (HOW_MANY_PEOPLE_RAN * 100) / HOW_MANY_PEOPLE + "%" );

        float averageWaitTime = 0;
        float averageWaitTimeForRan = 0;
        for (Person person : WHO_RAN) {
            averageWaitTimeForRan += person.getWaitTime();
        }
        averageWaitTime += averageWaitTimeForRan;
        averageWaitTimeForRan /= HOW_MANY_PEOPLE_RAN;
        System.out.println("Average Wait Time for people that Ran: " + averageWaitTimeForRan + "ms.");

        float averageWaitTimeForNotRan = 0;
        for (Person person : priorityQueueManager.getPriorityQueue()) {
            averageWaitTimeForNotRan += person.getWaitTime();
        }
        averageWaitTime += averageWaitTimeForNotRan;
        averageWaitTimeForNotRan /= priorityQueueManager.getPriorityQueue().size();
        System.out.println("Average Wait Time for people that have not Ran: " + averageWaitTimeForNotRan + "ms.");


        averageWaitTime /= HOW_MANY_PEOPLE + priorityQueueManager.getPriorityQueue().size();
        System.out.println("Average Wait Time for all people: " + averageWaitTime + "ms.");

    }

    private static void createRandomNumberOfThreads(
            PriorityQueueManager priorityQueueManager,
            Stock stock,
            int minutesOffDay,
            int minimalAge,
            int maxAge
    ) {
        Random random = new Random();
        int numOfThreads = random.nextInt(5, 10);
        System.out.println("Group of " + numOfThreads + " has arrived!");

        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < numOfThreads; i++) {
            Person person = new Person("Person " + minutesOffDay + "-" + i, new Random().nextInt(minimalAge, maxAge + 1));
            priorityQueueManager.offerPerson(person);
            createNewThread(priorityQueueManager, stock);
        }
    }

    private static void createNewThread(PriorityQueueManager priorityQueueManager, Stock stock) {
        // Create a new thread
        Thread thread = new Thread(() -> {
            try {
                Person couldRun = priorityQueueManager.runNextInQueue(stock);
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