package parallelSpeed.server;

import parallelSpeed.client.Person;

import java.util.concurrent.*;

public class Stock {

    private static volatile Stock instance;

    private final int helmetQuantity;
    private final int kartQuantity;

    private final LinkedBlockingQueue<Helmet> helmetQueue;
    private final LinkedBlockingQueue<Kart> kartQueue;

    private int helmetUsage = 0;
    private int kartUsage = 0;

    private final int timeOut = 100;

    private Stock(int helmetQuantity, int kartQuantity) {
        this.helmetQuantity = helmetQuantity;
        this.kartQuantity = kartQuantity;

        this.helmetQueue = new LinkedBlockingQueue<>(helmetQuantity);
        this.kartQueue = new LinkedBlockingQueue<>(kartQuantity);

        initResources();
    }

    public static Stock getInstance(int helmetQuantity, int kartQuantity) {
        if (instance == null) {
            synchronized (Stock.class) {
                if (instance == null) {
                    instance = new Stock(helmetQuantity, kartQuantity);
                }
            }
        }
        return instance;
    }

    public boolean acquireResources(Person person) {
        if (person.getAge() < 14) {
            return acquireResourcesForKid(person);
        } else {
            return acquireResourcesForAdult(person);
        }
    }

    public void releaseResources(Person person) {
        if (person.getAge() < 14) {
            releaseResourcesForKid(person);
        } else {
            releaseResourcesForAdult(person);
        }
    }

    private boolean acquireResourcesForAdult(Person person) {
        Kart kart = null;
        Helmet helmet = null;
        try {
            kart = person.setKart(kartQueue.poll(timeOut, TimeUnit.MILLISECONDS)); // Try to get a kart
            if (kart == null) {
                System.out.println("Person " + person.getName() + " could not run cause of: No Kart available\nPriority will be Increased");
                return false; // No kart available
            }

            helmet = person.setHelmet(helmetQueue.poll(timeOut, TimeUnit.MILLISECONDS)); // Try to get a helmet
            if (helmet == null) {
                kartQueue.offer(person.offerKart()); // Return the kart if no helmet
                System.out.println("Person " + person.getName() + " could not run cause of: No Helmet available.\nPriority will be Increased");
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
            if (helmet == null && kart != null) kartQueue.offer(person.offerKart());
        }
    }

    private boolean acquireResourcesForKid(Person person) {
        Helmet helmet = null;
        Kart kart = null;
        try {
            helmet = person.setHelmet(helmetQueue.poll(timeOut, TimeUnit.MILLISECONDS)); // Try to get a helmet
            if (helmet == null) return false; // No helmet available

            kart = person.setKart(kartQueue.poll(timeOut, TimeUnit.MILLISECONDS)); // Try to get a kart
            if (kart == null) {
                helmetQueue.offer(person.offerHelmet()); // Return the helmet if no kart
                return false;
            }

            // Successfully acquired both
            return true;
        } catch (InterruptedException e) {
            // Handle interruption
            return false;
        } finally {
            // Ensure resources are released if not acquired
            if (kart == null && helmet != null) helmetQueue.offer(person.offerHelmet());
        }
    }

    private void releaseResourcesForAdult(Person person) {
        kartQueue.offer(person.offerKart());
        helmetQueue.offer(person.offerHelmet());
    }

    private void releaseResourcesForKid(Person person) {
        helmetQueue.offer(person.offerHelmet());
        kartQueue.offer(person.offerKart());
    }

    // Utils, helpers, getters and setters
    private void initResources() {
        for (int i = 0; i < helmetQuantity; i++) {
            helmetQueue.add(new Helmet());
        }

        for (int i = 0; i < kartQuantity; i++) {
            kartQueue.add(new Kart());
        }
    }

    public String getUsage() {
        return "Helmet usage: " + helmetUsage + " Kart usage: " + kartUsage;
    }

}
