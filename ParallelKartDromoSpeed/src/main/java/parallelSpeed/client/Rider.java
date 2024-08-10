package parallelSpeed.client;

import parallelSpeed.server.Kartodromo;

import static parallelSpeed.server.Kartodromo.RIDER_THREADS;

public class Rider implements Comparable<Rider>, Runnable {

    private final RiderType type;
    private final String id;
    private final int lapTime;
    private final long arrivalTime;
    private long waitingTime = -1;
    private long finishTime;

    public boolean helmet = false;
    public boolean kart = false;

    @Override
    public int compareTo(Rider other) {
        // Sort by Helmet first
        if (this.helmet && !other.helmet) {
            return -1;
        } else if (!this.helmet && other.helmet) {
            return 1;
        }

        // Sort by Kart second
        if (this.kart && !other.kart) {
            return -1;
        } else if (!this.kart && other.kart) {
            return 1;
        }

        // Sort by RiderType last
        if (this.type != other.type) {
            if (this.type == RiderType.U14_KID) return -1;
            if (other.type == RiderType.U14_KID) return 1;
            if (this.type == RiderType.KID) return -1;
            if (other.type == RiderType.KID) return 1;
        }

        // If all criteria are equal, return 0
        return 0;
    }

    public Rider(String id, RiderType type) {
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
    @Override
    public void run() {
        try {
            waitingTime = System.currentTimeMillis() - arrivalTime;
            Thread.sleep(lapTime);
            finishTime = System.currentTimeMillis();
            System.out.println("Rider " + id + " ran for " + lapTime + "ms. After waiting for: " + waitingTime + "ms.");
            Kartodromo.releaseKart();
            Kartodromo.releaseHelmet();
        } catch (InterruptedException e) {
            System.out.println("Error while karting");
        }
        finishTime = System.currentTimeMillis();
        waitingTime = finishTime - arrivalTime;
    }

    public void acquireHelmet() {
        this.helmet = true;
        if (kart) {
            Thread running = new Thread(this);
            RIDER_THREADS.add(running);
            running.start();
        } else {
            Kartodromo.getInLineForKart(this);
        }
    }

    public void acquireKart() {
        this.kart = true;
        if (helmet) {
            Thread running = new Thread(this);
            RIDER_THREADS.add(running);
            running.start();
        } else {
            Kartodromo.getInLineForHelmet(this);
        }
    }

    public String getId() {
        return id;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }

    public long getWaitingTime() {
        return waitingTime;
    }

    public long getFinishTime() {
        return finishTime;
    }
}
