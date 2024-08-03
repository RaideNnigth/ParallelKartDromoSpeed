package parallelSpeed.server;

import parallelSpeed.client.Pilot;

import java.util.Comparator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Kartodromo {
    private static final int TIME_OUT = 1000;

    private static final int NUM_KARTS = 10;
    private static final int NUM_HELMETS = 10;

    private static final Comparator<Pilot> comparator = Comparator.comparing((Pilot p) -> p.getAge() <= 14 ? 0 : 1);
    private static final PriorityBlockingQueue<Pilot> helmetWaitList = new PriorityBlockingQueue<>(11, comparator);
    private static final LinkedBlockingQueue<Pilot> kartWaitList = new LinkedBlockingQueue<>();

    private static final Semaphore kartsSemaphore = new Semaphore(NUM_KARTS);
    private static final Lock kartLock = new ReentrantLock();
    private static final Condition kartCondition = kartLock.newCondition();

    private static final Semaphore helmetSemaphore = new Semaphore(NUM_HELMETS);
    private static final Lock helmetLock = new ReentrantLock();
    private static final Condition helmetCondition = helmetLock.newCondition();

    private static int KARTS_USAGE = 0;
    private static int HELMETS_USAGE = 0;


    public static void acquireResourcesAdult(Pilot pilot ) throws InterruptedException {

    }

    public static void acquireResourcesChild(Pilot pilot ) throws InterruptedException {

    }

    public static void acquireHelmet(Pilot pilot) throws InterruptedException {
        assert pilot != null;
        helmetLock.lock();
        try {
            // Add pilot to queue
            helmetWaitList.offer(pilot);

            while (helmetWaitList.peek() != pilot) {
                helmetCondition.await();
            }
            helmetWaitList.poll();
            helmetSemaphore.acquire();
            HELMETS_USAGE++;
            pilot.setHelmet();
            helmetCondition.signalAll();

        } finally {
            helmetLock.unlock();
        }
    }

    public synchronized static void acquireKart(Pilot pilot) throws InterruptedException {
        assert pilot != null;
        kartLock.lock();
        try {
            // Add pilot to queue
            kartWaitList.offer(pilot);

            while (kartWaitList.peek() != pilot) {
                kartCondition.await();
            }
            kartWaitList.poll();
            kartsSemaphore.acquire();
            KARTS_USAGE++;
            pilot.setKart();
            kartCondition.signalAll();

        } finally {
            kartLock.unlock();
        }
    }

    public static void releaseHelmet() {
        helmetSemaphore.release();
        helmetLock.lock();
        try {
            helmetCondition.signalAll(); // Signal next waiting thread
        } finally {
            helmetLock.unlock();
        }
    }

    public static void releaseKart() {
        kartsSemaphore.release();
        kartLock.lock();
        try {
            kartCondition.signalAll(); // Signal next waiting thread
        } finally {
            kartLock.unlock();
        }
    }

    public static void releaseResources() {
        releaseKart();
        releaseHelmet();
    }

    public static int getKartsUsage() {
        return KARTS_USAGE;
    }

    public static int getHelmetsUsage() {
        return HELMETS_USAGE;
    }

}
