package parallelSpeed.client;

import parallelSpeed.server.Kartodromo;

import java.util.Random;

public class Pilot {
    private final String name;
    private final int age;
    private final long lapTime;
    private final int minLapTime = 5000;
    private final int maxLapTime = 7000;

    private boolean hasHelmet = false;
    private boolean hasKart = false;

    public Pilot(String name, int age) {
        this.name = name;
        this.age = age;
        Random random = new Random();
        this.lapTime = random.nextInt(this.minLapTime, this.maxLapTime);
    }

    public void run() {
        try {
            if (age <= 14) {

            } else {
                Kartodromo.acquireKart(this);
                Kartodromo.acquireHelmet(this);
            }
            System.out.println(name + " is running for " + lapTime + "ms");
            Thread.sleep(lapTime); // Simulate the pilot running the lap

        } catch (InterruptedException e) {
            System.out.println(name + " was interrupted: " + e.getMessage());
        } finally {
            // Ensure resources are released even if an exception occurs
            if (hasHelmet) {
                Kartodromo.releaseHelmet();
                hasHelmet = false; // Reset the state
            }
            if (hasKart) {
                Kartodromo.releaseKart();
                hasKart = false; // Reset the state
            }
        }
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }


    public void setHelmet() {
        this.hasHelmet = true;
    }

    public void setKart() {
        this.hasKart = true;
    }
}
