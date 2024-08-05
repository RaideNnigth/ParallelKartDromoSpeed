package parallelSpeed;

import parallelSpeed.server.Kartodromo;

public class Main {

    public static void main(String[] args) {
        Kartodromo kartodromo = new Kartodromo();
        try {
            kartodromo.working();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}