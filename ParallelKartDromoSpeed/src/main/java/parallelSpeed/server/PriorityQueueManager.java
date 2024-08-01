package parallelSpeed.server;

import parallelSpeed.client.Person;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

import static java.lang.Math.round;

public class PriorityQueueManager {

    private static volatile PriorityQueueManager instance;

    private final PriorityBlockingQueue<Person> priorityQueue;

    private final int whenToIncreasePriority = 14000; // 14 seconds in real time or 14000ms

    private PriorityQueueManager() {
        //Comparator<Person> comparator = Comparator.comparingInt(Person::getAge);
        Comparator<Person> comparator = Comparator.comparing(Person::getPriority)
                .thenComparing(Person::getAge)
                .thenComparing(Person::hasHelmet)
                .thenComparing(Person::hasKart);
        this.priorityQueue = new PriorityBlockingQueue<>(10, comparator);
    }

    public static PriorityQueueManager getInstance() {
        if (instance == null) {
            synchronized (PriorityQueueManager.class) {
                if (instance == null) {
                    instance = new PriorityQueueManager();
                }
            }
        }
        return instance;
    }

    public Person runNextInQueue(Stock stock) throws InterruptedException {
        if (priorityQueue.isEmpty()) {
            System.out.println("No one in the queue.");
            return null;
        }
        Person person = priorityQueue.poll();

        if (!stock.acquireResources(person)) {
            person.incrementPriority();
            priorityQueue.offer(person);
            return null;
        }
        person.run();
        stock.releaseResources(person);
        return person;
    }

    public void offerPerson(Person person) {

        priorityQueue.forEach(
            p -> {
                long waitTime = System.currentTimeMillis() - p.getArrivalTime();
                if (waitTime > whenToIncreasePriority) {
                    // For each 1000 milliseconds of wait time, increment the priority by one
                    int howMuchToIncrement = round((float) (waitTime - whenToIncreasePriority) / 1000);
                    person.incrementPriority(howMuchToIncrement);
                    System.out.println("Increasing priority by: " + howMuchToIncrement + " for person: " + p.getName());
                }
            }
        );

        priorityQueue.offer(person);
    }

    public PriorityBlockingQueue<Person> getPriorityQueue() {
        return priorityQueue;
    }
}
