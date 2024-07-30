package parallelSpeed.client;

import parallelSpeed.server.Helmet;
import parallelSpeed.server.Kart;
import parallelSpeed.server.Stock;
import parallelSpeed.server.Track;

import java.util.Random;

public class Person {
    private final String name;
    private final int age;
    private final int arrivalTime;
    private int playTime;

    private int timeToRun;

    private Track track;
    private Kart kart;
    private Helmet helmet;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
        this.arrivalTime = (int) System.currentTimeMillis();
        this.timeToRun = new Random().nextInt(0, 2);
    }

    public void run(Stock stock) throws InterruptedException {
        Helmet helmet;
        Kart kart;

        // Kids under 14 years old must get the helmet first, adults like to get the kart first
        if (age < 14) {
            helmet = stock.fetchHelmet();
            kart = stock.fetchKart();
        } else {
            kart = stock.fetchKart();
            helmet = stock.fetchHelmet();
        }
        // The track is the last thing to get
        //stock.fetchTrack();

        // The person is now ready to run use the helmet and kart for a while
        // and then return them to the stock (sleep)
        System.out.println(name + " is running on the track using helmet and kart.");

        Thread.sleep(timeToRun);
        playTime = (int) System.currentTimeMillis();
        System.out.println(name + " has finished running on the track, releasing helmet and kart.");

        // Don't fucking forget to return your stuffy
        stock.releaseHelmet(helmet);
        stock.releaseKart(kart);
        //stock.returnTrack();
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public boolean hasHelmet() {
        return helmet != null;
    }

    public boolean hasKart() {
        return kart != null;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getPlayTime(){
        return playTime;
    }

    public int getWaitingTime(){
        return playTime - arrivalTime;
    }
}
