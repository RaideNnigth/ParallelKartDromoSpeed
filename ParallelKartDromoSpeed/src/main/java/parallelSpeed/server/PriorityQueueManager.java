package parallelSpeed.server;

import parallelSpeed.client.Person;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

public class PriorityQueueManager {

    private static volatile PriorityQueueManager instance;


    private final PriorityBlockingQueue<Person> priorityQueue;

    private PriorityQueueManager() {
        Comparator<Person> comparator = Comparator.comparingInt(Person::getAge);
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

    public Person getNextPerson() {
        return priorityQueue.poll();
    }

    public void addPerson(Person person) {
        priorityQueue.add(person);
    }

}
