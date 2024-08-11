package parallelSpeed.client;

import parallelSpeed.server.Kartodromo;

import static parallelSpeed.server.Kartodromo.RIDER_THREADS;

public class Rider implements Comparable<Rider> {

    private final RiderType type;
    private final String id;
    private final int lapTime;
    private final int arrivalTime;
    private int waitingTime = -1;
    private double helmetPriority;
    private double kartPriority;

    private boolean helmet = false;
    private boolean kart = false;


    @Override
    public int compareTo(Rider other) {

        // Sort by priority
        if (this.helmetPriority < other.helmetPriority) {
            return -1;
        } else if (this.helmetPriority > other.helmetPriority) {
            return 1;
        }
        return 0;
    }

    public Rider(String id, RiderType type, int arrivedAt) {
        this.id = id;
        this.type = type;
        this.arrivalTime = arrivedAt;
        this.lapTime = (int) ((Math.random() * 1000) + 5000);

        // priority is a function of the type of rider and the time they arrived (initially 1)
        setHelmetPriority();
        setKartPriority();

        // Set the initial state (Which queue the rider should be in)
        if (this.type == RiderType.KID || this.type == RiderType.U14_KID) {
            Kartodromo.getInLineForHelmet(this);
        } else {
            Kartodromo.getInLineForKart(this);
        }
    }

    public void goKarting() {
        try {
            Thread.sleep(lapTime);
            System.out.println("Rider " + id + " ran for " + lapTime + "ms. After waiting for: " + waitingTime + " minutes (simulation).");
            Kartodromo.releaseKart();
            Kartodromo.releaseHelmet();
        } catch (InterruptedException e) {
            System.out.println("Error while karting");
        }
    }

    public void acquireHelmet(int at) {
        this.helmet = true;
        if (kart) {
            waitingTime = at - arrivalTime;
            Thread running = new Thread(this::goKarting);
            RIDER_THREADS.offer(running);
            running.start();
        } else {
            Kartodromo.getInLineForKart(this);
        }
    }

    public void acquireKart(int at) {
        this.kart = true;
        if (helmet) {
            waitingTime = at - arrivalTime;
            Thread running = new Thread(this::goKarting);
            RIDER_THREADS.offer(running);
            running.start();
        } else {
            Kartodromo.getInLineForHelmet(this);
        }
    }

    public String getId() {
        return id;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public boolean hasHelmet() {
        return helmet;
    }

    public boolean hasKart() {
        return kart;
    }

    public RiderType getType() {
        return type;
    }

    public void setHelmetPriority() {
        // Logarithmic increase in priority
        int typeIncrease = type == RiderType.ADULT ? 3 : type == RiderType.KID ? 2 : 1;
        double arrivalIncrease = (double) arrivalTime / 10;
        this.helmetPriority = 1 + Math.log(arrivalIncrease + typeIncrease);
    }

    public void setKartPriority() {
        // Logarithmic increase in priority
        double arrivalIncrease = (double) arrivalTime / 10;
        this.kartPriority = 1 + Math.log(arrivalIncrease);
    }

    public double getKartPriority() {
        return kartPriority;
    }
}
