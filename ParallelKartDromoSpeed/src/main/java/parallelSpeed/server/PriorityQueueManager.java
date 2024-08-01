package parallelSpeed.server;

import parallelSpeed.client.Person;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

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

    public boolean runNextInQueue(Stock stock) throws InterruptedException {
        if (priorityQueue.isEmpty()) {
            System.out.println("No one in the queue.");
            return false;
        }
        Person person = priorityQueue.poll();

        if (!stock.acquireResources(person)) {
            person.incrementPriority();
            priorityQueue.offer(person);
            System.out.println("Person " + person.getName() + " could not run. Priority increased to " + person.getPriority());
            return false;
        }
        person.run();
        stock.releaseResources(person);
        return true;
    }

    public void offerPerson(Person person) {

        priorityQueue.forEach(
            p -> {
                if (System.currentTimeMillis() - p.getArrivalTime() > whenToIncreasePriority) {
                    person.incrementPriority();
                }
            }
        );

        priorityQueue.offer(person);
    }

    public int size() {
        return priorityQueue.size();
    }
}
