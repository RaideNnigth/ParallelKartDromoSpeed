package parallelSpeed;

import parallelSpeed.client.Pilot;
import parallelSpeed.server.Kartodromo;

import java.util.Random;

/*
 * Create MultipleThreads to simulate random number of groups getting into a Kartodromo.
 * Each group has a random number of pilots.
 * Each pilot has a random age and a random lap time.
 * The pilots must acquire a helmet and a kart before running.
 * The pilots with age <= 14 must acquire the helmet first.
 * The pilots must release the helmet and the kart after running.
 * The pilots must run for the lap time.
 * The pilots must be interrupted if they are waiting for resources for more than 1 second.
 * The pilots with age <= 14 must have priority to acquire the helmet.
 *
 * Will simulate work day with 8 hours of work. Each hour has 60 minutes and each minute will be 1 second in real time.
 */


public class Main {

    private static final Random random = new Random();
    private static final int MIN_PILOTS = 4;
    private static final int MAX_PILOTS = 10;

    private static final int MINUTES_OF_DAY = 8 * 60;

    private static int HOW_MANY_HAVE_COME = 0;

    public static void main(String[] args) {

        for (int i = 0; i < MINUTES_OF_DAY; i++) {
            int numPilots = random.nextInt(MIN_PILOTS, MAX_PILOTS);
            int shouldCreateGroup = random.nextInt(0, 10);
            if (shouldCreateGroup < 5) {
                createGroup(numPilots, i);
            }
        }

        // Wait for all threads to finish
        try {
            Thread.sleep((MINUTES_OF_DAY * 1000) + 5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("All pilots have finished running");
        System.out.println("Karts used: " + Kartodromo.getKartsUsage());
        System.out.println("Helmets used: " + Kartodromo.getHelmetsUsage());
        //System.out.println("How many pilots did not Ran: " + Kartodromo.getNotRanPilots());

    }

    private static void createGroup(int numPilots, int minuteOfTheDay) {
        for (int i = 0; i < numPilots; i++) {
            int age = random.nextInt(10, 20);
            Pilot pilot = new Pilot("P_" + minuteOfTheDay + "-" + i, age);
            HOW_MANY_HAVE_COME++;
            Thread thread = new Thread(pilot::run);
            thread.start();
        }
    }
}