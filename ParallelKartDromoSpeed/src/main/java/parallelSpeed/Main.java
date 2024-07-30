package parallelSpeed;

import parallelSpeed.client.Person;
import parallelSpeed.server.PriorityQueueManager;
import parallelSpeed.server.Stock;

import java.util.ArrayList;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting to count boy");

        Random random = new Random();
        int count = random.nextInt(1000000);
        ArrayList<Person> people = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            people.add(new Person("Person " + i, random.nextInt(40)));
        }

        PriorityQueueManager priorityQueueManager = PriorityQueueManager.getInstance();
        Stock stock = Stock.getInstance(10000, 10000, 10000);

        while (!people.isEmpty()) {
            Person arrived = people.remove(random.nextInt(people.size()));
            Thread thread = new Thread(() -> {
                try {
                    System.out.println("Person " + arrived.getName() + " has arrived to the queue.");
                    priorityQueueManager.addPerson(arrived);
                    Person nextPerson = priorityQueueManager.getNextPerson();
                    System.out.println("Person " + nextPerson.getName() + " is the next person to run.");
                    nextPerson.run(stock);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.out.println(Thread.currentThread().getName() + " was interrupted.");
                }
            });
            thread.setPriority(Thread.MAX_PRIORITY);
            thread.start();
        }
    }
}