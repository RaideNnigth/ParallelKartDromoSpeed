package parallelSpeed.client;

import parallelSpeed.server.Helmet;
import parallelSpeed.server.Kart;

import java.util.Random;

public class Person {
    private final String name;
    private final int age;

    // Variables to control the Queue and resources
    private int priority;
    private Kart kart;
    private Helmet helmet;

    // final variables to manipulate how the Person will behave
    private final int minimalLapTime = 5000; // 5000 ms or 5 realTimeMinutes
    private final int maxLapTime = 7000; // 7000 ms or 7 realTimeMinutes

    // Variables to register the info about time and resources for further report
    private final long arrivalTime;
    private final int lapWillBeThisLong;
    private long waitTime;


    public Person(String name, int age) {
        this.name = name;
        this.age = age;
        this.priority = 1;
        this.arrivalTime = System.currentTimeMillis();
        this.lapWillBeThisLong = new Random().nextInt(minimalLapTime, maxLapTime);
    }

    public void run() throws InterruptedException {
        // Kids under 14 years old must get the helmet first, adults like to get the kart first
        // The person is now ready to run use the helmet and kart for a while
        // and then return them to the stock (sleep)
        waitTime =  System.currentTimeMillis() - arrivalTime;
        Thread.sleep(lapWillBeThisLong);
        System.out.println("Person " + name + " has finished running after waiting for: "
                + waitTime + "ms. LapTime " + lapWillBeThisLong + "ms.");

        // Finished running
        // Don't fucking forget to return your stuffy

    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public int getPriority() {
        return priority;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }

    public long getWaitTime() {
        return waitTime;
    }

    public void incrementPriority() {
        priority++;
    }

    public void incrementPriority(int incrementBy) {
        priority += incrementBy;
    }

    public Helmet setHelmet(Helmet helmet) {
        this.helmet = helmet;
        return this.helmet;
    }

    public Kart setKart(Kart kart) {
        this.kart = kart;
        return this.kart;
    }

    public Helmet offerHelmet() {
        return this.helmet;
    }

    public Kart offerKart() {
        return this.kart;
    }

    public boolean hasHelmet() {
        return helmet != null;
    }

    public boolean hasKart() {
        return helmet != null;
    }
}
