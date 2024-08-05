package parallelSpeed.client;

class StateObserver implements Runnable {
    private final Rider rider;

    public StateObserver(Rider rider) {
        this.rider = rider;
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                rider.waitForStateChange();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
