package parallelSpeed.server;

import parallelSpeed.client.Person;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

public class PriorityQueueManager {

    private final PriorityBlockingQueue<Person> priorityQueue;
    private final HelmetQueueManager helmetQueueManager;
    private final KartQueueManager kartQueueManager;

    public PriorityQueueManager(HelmetQueueManager helmetQueueManager, KartQueueManager kartQueueManager) {
        Comparator<Person> comparator = Comparator
                .comparing((Person p) -> p.getAge() <= 14 ? 0 : 1) // Separate kids from others
                .thenComparing((Person p) -> p.getAge() <= 14 ? -p.getCurrentWaitingTime() : 0) // Kids: Higher waiting time first
                .thenComparing((Person p) -> p.getAge() > 14 ? -p.getCurrentWaitingTime() : 0); // Others: Higher waiting time first

        this.priorityQueue = new PriorityBlockingQueue<>(10, comparator);
        this.helmetQueueManager = helmetQueueManager;
        this.kartQueueManager = kartQueueManager;
    }

    public Person runNextInQueue() throws InterruptedException {
        if (this.priorityQueue.isEmpty()) {
            System.out.println("No one in the queue.");
            return null;
        }
        Person person = priorityQueue.poll();
        if (!person.run(this.helmetQueueManager, this.kartQueueManager)){
            this.priorityQueue.offer(person);
            return null;
        }
        return person;
    }

    public void offerPerson(Person person) {
        priorityQueue.offer(person);
    }

    public PriorityBlockingQueue<Person> getPriorityQueue() {
        return priorityQueue;
    }
}
