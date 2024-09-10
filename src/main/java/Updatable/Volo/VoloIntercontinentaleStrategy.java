package Updatable.Volo;

import Updatable.Aereo.Aereo;

public class VoloIntercontinentaleStrategy implements TipoVoloStrategy {
    @Override
    public Aereo.TipoVeicolo determinaTipoVolo() {
        return Aereo.TipoVeicolo.INTERCONTINENTALE;
    }//determinaTipoVolo
}//VoloIntercontinentaleStrategy
