package Updatable.Volo;

import Updatable.Aereo.Aereo;

public class VoloNazionaleStrategy implements TipoVoloStrategy {
    @Override
    public Aereo.TipoVeicolo determinaTipoVolo() {
        return Aereo.TipoVeicolo.NAZIONALE;
    }//determinaTipoVolo
}//VoloNazionaleStrategy
