package parallelSpeed;

import parallelSpeed.client.Person;
import parallelSpeed.server.PriorityQueueManager;
import parallelSpeed.server.Stock;

import java.util.ArrayList;
import java.util.Random;

public class Main {

    private static int HOW_MANY_PEOPLE_RAN = 0;

    public static void main(String[] args) {
        PriorityQueueManager priorityQueueManager = PriorityQueueManager.getInstance();
        Stock stock = Stock.getInstance(10, 10, 10);

        int minimalAge = 15;
        int maxAge = 20;

        int minutesOffDay = 8 * 60; // 8 hours or 480 minutes
        int changeOfGettingIn = 10; // % chance of getting in per minute
        int eachMinuteIs = 1000; // 1 second in real time for simulation

        while (minutesOffDay-- > 0) {
            if (new Random().nextInt(100) < changeOfGettingIn) {
                Person person = new Person("Person" + minutesOffDay, new Random().nextInt(minimalAge, maxAge + 1) );
                priorityQueueManager.offerPerson(person);
                createNewThread(priorityQueueManager, stock);
            }
            try {
                Thread.sleep(eachMinuteIs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void createNewThread(PriorityQueueManager priorityQueueManager, Stock stock) {
        // Create a new thread
        Thread thread = new Thread(() -> {
            try {
                boolean couldRun = priorityQueueManager.runNextInQueue(stock);
                if (!couldRun) {
                    System.out.println("Person could not run.");
                } else {
                    HOW_MANY_PEOPLE_RAN++;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println(Thread.currentThread().getName() + " was interrupted.");
            }
        });
        thread.setPriority(Thread.MAX_PRIORITY);
        // Start the thread
        thread.start();
    }
}