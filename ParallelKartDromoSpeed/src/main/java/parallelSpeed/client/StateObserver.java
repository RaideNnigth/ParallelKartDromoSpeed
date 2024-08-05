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
        while (true){
            try {
                rider.waitForStateChange();
                RiderState currentState = rider.getState();
                if (currentState == RiderState.FINISHED) {
                    break; // Exit loop if finished
                } else if (currentState == RiderState.WAITING_FOR_RESOURCES) {
                    rider.tryAcquireResources();
                } else if (currentState == RiderState.READY_TO_RUN) {
                    rider.run();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
