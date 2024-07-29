package parallelSpeed.server;

import java.util.ArrayList;
import java.util.concurrent.*;


//TODO: check if it is needed to manage the how many karts the track can have at the same time
//TODO: Use semaphore to control helmet and kart quantity


public class Stock {

    private static volatile Stock instance;

    private final int helmetQuantity;
    private final int kartQuantity;
    private final int trackQuantity;

    private final ArrayList<Helmet> helmets = new ArrayList<>();
    private final ArrayList<Kart> karts = new ArrayList<>();
    private final ArrayList<Track> tracks = new ArrayList<>();

    private final Semaphore helmetSemaphore;
    private final Semaphore kartSemaphore;
    private final Semaphore trackSemaphore;

    private Stock(int helmetQuantity, int kartQuantity, int trackQuantity) {
        this.helmetQuantity = helmetQuantity;
        this.kartQuantity = kartQuantity;
        this.trackQuantity = trackQuantity;

        initResources();

        this.helmetSemaphore = new Semaphore(helmetQuantity);
        this.kartSemaphore = new Semaphore(kartQuantity);
        this.trackSemaphore = new Semaphore(trackQuantity);
    }

    public static Stock getInstance(int helmetQuantity, int kartQuantity, int trackQuantity) {
        if (instance == null) {
            synchronized (Stock.class) {
                if (instance == null) {
                    instance = new Stock(helmetQuantity, kartQuantity, trackQuantity);
                }
            }
        }
        return instance;
    }


    public Helmet fetchHelmet() throws InterruptedException {
        helmetSemaphore.acquire();
        return getAvailableHelmet();
    }

    private synchronized Helmet getAvailableHelmet() {
        for (int i = 0; i < helmets.size(); i++) {
            if (helmets.get(i) != null) {
                Helmet helmet = helmets.get(i);
                helmets.set(i, null); // Mark as taken
                return helmet;
            }
        }
        return null;
    }

    public void releaseHelmet(Helmet helmet) {
        if (releaseHelmetResource(helmet)) {
            helmetSemaphore.release(); // Release the permit
        }
    }

    private synchronized boolean releaseHelmetResource(Helmet helmet) {
        for (int i = 0; i < helmets.size(); i++) {
            if (helmets.get(i) == null) {
                helmets.set(i, helmet); // Return the helmet
                return true;
            }
        }
        return false;
    }

    public Kart fetchKart() throws InterruptedException {
        kartSemaphore.acquire();
        return getAvailableKart();
    }

    private synchronized Kart getAvailableKart() {
        for (int i = 0; i < karts.size(); i++) {
            if (karts.get(i) != null) {
                Kart kart = karts.get(i);
                karts.set(i, null); // Mark as taken
                return kart;
            }
        }
        return null;
    }

    public void releaseKart(Kart kart) {
        if (releaseKartResource(kart)) {
            kartSemaphore.release(); // Release the permit
        }
    }

    private synchronized boolean releaseKartResource(Kart kart) {
        for (int i = 0; i < karts.size(); i++) {
            if (karts.get(i) == null) {
                karts.set(i, kart); // Return the kart
                return true;
            }
        }
        return false;
    }

    // Utils, helpers, getters and setters

    public int getHelmetQuantity() {
        return helmetQuantity;
    }

    public int getKartQuantity() {
        return kartQuantity;
    }

    public int getTrackQuantity() {
        return trackQuantity;
    }

    private void initResources() {
        for (int i = 0; i < helmetQuantity; i++) {
            helmets.add(new Helmet());
        }

        for (int i = 0; i < kartQuantity; i++) {
            karts.add(new Kart());
        }

        for (int i = 0; i < trackQuantity; i++) {
            tracks.add(new Track());
        }
    }
}
