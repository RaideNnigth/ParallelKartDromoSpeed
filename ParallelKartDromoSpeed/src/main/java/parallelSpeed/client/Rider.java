package parallelSpeed.client;

import parallelSpeed.server.Kartodromo;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Rider implements Comparable<Rider>, Runnable {

    private RiderState state;
    private final RiderType type;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition stateChanged = lock.newCondition();

    private final int id;
    private final int lapTime;
    private final long arrivalTime;

    private boolean helmet = false;
    private boolean kart = false;

    @Override
    public int compareTo(Rider other) {
        // Sort by type
        // Has Helmet or Kart -> U14_KID -> KID -> ADULT
        if (this.helmet && !other.helmet) {
            return -1;
        } else if (!this.helmet && other.helmet) {
            return 1;
        } else if (this.kart && !other.kart) {
            return -1;
        } else if (!this.kart && other.kart) {
            return 1;
        } else if (this.type == RiderType.U14_KID && other.type != RiderType.U14_KID) {
            return -1;
        } else if (this.type != RiderType.U14_KID && other.type == RiderType.U14_KID) {
            return 1;
        } else if (this.type == RiderType.KID && other.type == RiderType.ADULT) {
            return -1;
        } else if (this.type == RiderType.ADULT && other.type == RiderType.KID) {
            return 1;
        } else {
            return 0;
        }
    }

    public Rider(int id, RiderType type) {
        this.id = id;
        this.type = type;
        this.arrivalTime = System.currentTimeMillis();
        this.lapTime = (int) ((Math.random() * 1000) + 5000);

        // Set the initial state (Which queue the rider should be in)
        if (this.type == RiderType.KID || this.type == RiderType.U14_KID) {
            Kartodromo.getInLineForHelmet(this);
        } else {
            Kartodromo.getInLineForKart(this);
        }
    }

    /*
    Control the rider's state
     */
    @Override
    public void run() {
        while (true) {
            lock.lock();
            try {
                while (state == RiderState.WAITING_FOR_RESOURCES) {
                    stateChanged.await();
                }
                if (state == RiderState.RUNNING) {
                    System.out.println("Rider " + id + " is running");
                    Thread.sleep(lapTime);
                    Kartodromo.releaseKart();
                    Kartodromo.releaseHelmet();
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }

    public void acquireHelmet() {
        this.helmet = true;
        if (kart) {
            state = RiderState.RUNNING;
            lock.lock();
            stateChanged.signalAll();
            lock.unlock();
        } else {
            Kartodromo.getInLineForKart(this);
        }
    }

    public void acquireKart() {
        this.kart = true;
        if (helmet) {
            state = RiderState.RUNNING;
            lock.lock();
            stateChanged.signalAll();
            lock.unlock();
        } else {
            Kartodromo.getInLineForHelmet(this);
        }
    }

    public int getId() {
        return id;
    }

}
