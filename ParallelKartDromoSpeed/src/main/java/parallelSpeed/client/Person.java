package parallelSpeed.client;

import parallelSpeed.server.Helmet;
import parallelSpeed.server.HelmetQueueManager;
import parallelSpeed.server.Kart;
import parallelSpeed.server.KartQueueManager;

import java.util.Random;

public class Person {
    private final String name;
    private final int age;

    // Variables to control the Queue and resources
    private final int timeOut = 200;


    private Kart kart;
    private Helmet helmet;

    // final variables to manipulate how the Person will behave
    private final int minimalLapTime = 5000; // 5000 ms or 5 realTimeMinutes
    private final int maxLapTime = 7000; // 7000 ms or 7 realTimeMinutes

    // Variables to register the info about time and resources for further report
    private final long arrivalTime;
    private final int lapWillBeThisLong;
    private long waitingTime;


    public Person(String name, int age) {
        this.name = name;
        this.age = age;

        this.kart = null;
        this.helmet = null;

        this.arrivalTime = System.currentTimeMillis();
        this.lapWillBeThisLong = new Random().nextInt(this.minimalLapTime, this.maxLapTime);
    }

    public boolean run(HelmetQueueManager helmetQueueManager, KartQueueManager kartQueueManager) throws InterruptedException {
        // Kids under 14 years old must get the helmet first, adults like to get the kart first
        // The person will try to get the helmet and kart
        boolean acquired = acquireResources(helmetQueueManager, kartQueueManager);
        if (!acquired) {
            return false;
        }
        this.waitingTime = System.currentTimeMillis() - this.arrivalTime;

        // The person is now ready to run use the helmet and kart for a while
        // and then return them to the stock (sleep)

        Thread.sleep(this.lapWillBeThisLong);
        System.out.println(this.name + " has finished running after waiting for: " + waitingTime);

        // Finished running
        // Don't fucking forget to return your stuffy
        releaseResources(helmetQueueManager, kartQueueManager);
        return true;
    }

    public boolean acquireResources(HelmetQueueManager helmetQueueManager, KartQueueManager kartQueueManager) {
        if (this.age < 14) {
            return acquireResourcesForKid(helmetQueueManager, kartQueueManager);
        } else {
            return acquireResourcesForAdult(helmetQueueManager, kartQueueManager);
        }
    }

    public void releaseResources(HelmetQueueManager helmetQueueManager, KartQueueManager kartQueueManager) {
        if (this.age < 14) {
            releaseResourcesForKid(helmetQueueManager, kartQueueManager);
        } else {
            releaseResourcesForAdult(helmetQueueManager, kartQueueManager);
        }
    }

    private boolean acquireResourcesForAdult(HelmetQueueManager helmetQueueManager, KartQueueManager kartQueueManager) {
        try {
            this.kart = kartQueueManager.acquireKart(this.timeOut); // Try to get a kart
            if (this.kart == null) {
                System.out.println(this.name + " could not run cause of: No Kart available.");
                return false; // No kart available
            }

            this.helmet = helmetQueueManager.acquireHelmet(this.timeOut); // Try to get a helmet
            if (this.helmet == null) {
                kartQueueManager.offerKart(this.kart); // Return the kart if no helmet
                System.out.println(this.name + " could not run cause of: No Helmet available.");
                return false;
            }

            // Successfully acquired both
            return true;
        } catch (InterruptedException e) {
            // Handle interruption
            System.out.println(e.getMessage() + " |---| " + e.getCause());
            return false;
        } finally {
            // Ensure resources are released if not acquired
            if (this.helmet == null && this.kart != null) kartQueueManager.offerKart(this.kart);
        }
    }

    private boolean acquireResourcesForKid(HelmetQueueManager helmetQueueManager, KartQueueManager kartQueueManager) {
        try {
            this.helmet = helmetQueueManager.acquireHelmet(this.timeOut); // Try to get a helmet
            if (this.helmet == null) {
                System.out.println(this.name + " could not run cause of: No Helmet available.");
                return false; // No helmet available
            }

            this.kart = kartQueueManager.acquireKart(this.timeOut); // Try to get a kart
            if (this.kart == null) {
                helmetQueueManager.offerHelmet(this.helmet); // Return the helmet if no kart
                System.out.println(this.name + " could not run cause of: No Kart available.");
                return false;
            }

            // Successfully acquired both
            return true;
        } catch (InterruptedException e) {
            // Handle interruption
            System.out.println(e.getMessage() + " |---| " + e.getCause());
            return false;
        } finally {
            // Ensure resources are released if not acquired
            if (this.kart == null && this.helmet != null) helmetQueueManager.offerHelmet(this.helmet);
        }
    }

    private void releaseResourcesForAdult(HelmetQueueManager helmetQueueManager, KartQueueManager kartQueueManager) {
        kartQueueManager.offerKart(this.kart);
        helmetQueueManager.offerHelmet(this.helmet);
        kart = null;
        helmet = null;
    }

    private void releaseResourcesForKid(HelmetQueueManager helmetQueueManager, KartQueueManager kartQueueManager) {
        helmetQueueManager.offerHelmet(this.helmet);
        kartQueueManager.offerKart(this.kart);
        kart = null;
        helmet = null;
    }

    public int getAge() {
        return age;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }

    public long getCurrentWaitingTime() {
        return this.arrivalTime != 0 ? System.currentTimeMillis() - this.arrivalTime : 0;
    }

    public long getWaitingTime() {
        return waitingTime;
    }

}
