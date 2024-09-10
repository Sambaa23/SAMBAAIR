package Updatable.Aereo;

import java.util.Random;

public class AereoFactoryImpl implements AereoFactory {

    private static final String[] MODELLI = {"Boeing", "FlyEmirates", "EasyJet", "AirFrance", "WizzAir", "QatarAirways", "BritshAirways", "AirFrance", "ITAirways"};
    private static final Aereo.TipoVeicolo[] TIPI = Aereo.TipoVeicolo.values();
    private Random random = new Random();

    @Override
    public Aereo creaAereo() {
        long numeroSeriale = random.nextLong();

        String modello = MODELLI[random.nextInt(MODELLI.length)];

        Aereo.TipoVeicolo tipo = TIPI[random.nextInt(TIPI.length)];

        int p;
        switch (tipo) {
            case NAZIONALE -> p=30;
            case INTERNAZIONALE -> p=50;
            case INTERCONTINENTALE -> p=60;
            default -> p=0;
        }

        return new Aereo(numeroSeriale, modello, p, tipo);
    }
}

