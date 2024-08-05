package parallelSpeed.client;

import parallelSpeed.server.Kartodromo;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static parallelSpeed.server.Kartodromo.tryAcquireResourcesForAdult;
import static parallelSpeed.server.Kartodromo.tryAcquireResourcesForKid;

public class Rider implements Comparable<Rider> {

    private RiderState state;
    private final RiderType type;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition stateChanged = lock.newCondition();
    private final StateObserver observer;

    private final int id;
    private final int lapTime;
    private final long arrivalTime;
    private long startTime;
    private long endTime;


    public Rider(int id, RiderType type) {
        this.id = id;
        this.type = type;
        this.arrivalTime = System.currentTimeMillis();
        this.lapTime = (int) ((Math.random() * 1000) + 5000);
        this.state = RiderState.WAITING_FOR_RESOURCES;
        this.observer = new StateObserver(this);
    }

    public void tryAcquireResources() throws InterruptedException {
        if (type == RiderType.KID) {
            tryAcquireResourcesForKid(this);
        } else {
            tryAcquireResourcesForAdult(this);
        }
        setState(RiderState.READY_TO_RUN);
    }

    public void run() {
        lock.lock();
        try {
            setState(RiderState.RUNNING);
            startTime = System.currentTimeMillis();
            System.out.println("Rider " + id + " is running for " + lapTime + "ms");
            try {
                Thread.sleep(lapTime);
            } catch (InterruptedException e) {
                System.out.println("Rider " + id + " was interrupted: " + e.getMessage());
            }
            endTime = System.currentTimeMillis();
            setState(RiderState.FINISHED);
            Kartodromo.releaseKart();
            Kartodromo.releaseHelmet();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int compareTo(Rider other) {
        int value;
        if (this.type == RiderType.KID) {
            value = 0;
        } else {
            value = 1;
        }

        int value2;
        if (other.type == RiderType.KID) {
            value2 = 0;
        } else {
            value2 = 1;
        }
        return Integer.compare(value, value2);
    }

    public RiderType getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public int getLapTime() {
        return lapTime;
    }

    public void waitForStateChange() throws InterruptedException {
        lock.lock();
        try {
            stateChanged.await(); // Wait for the state to change
            if (state == RiderState.FINISHED) {
                System.out.println("Rider " + id + " has finished running after waiting for: " + (System.currentTimeMillis() - arrivalTime) + "ms");
            } else if (state == RiderState.WAITING_FOR_RESOURCES) {
                System.out.println("Rider " + id + " is waiting for Resources");
            } else if (state == RiderState.READY_TO_RUN) {
                System.out.println("Rider " + id + " is ready to run");
                run();
            }
        } finally {
            lock.unlock();
        }
    }

    public RiderState getState() {
        lock.lock();
        try {
            return state;
        } finally {
            lock.unlock();
        }
    }

    public void setState(RiderState newState) {
        lock.lock();
        try {
            state = newState;
            stateChanged.signalAll(); // Notify waiting threads
        } finally {
            lock.unlock();
        }
    }

}
