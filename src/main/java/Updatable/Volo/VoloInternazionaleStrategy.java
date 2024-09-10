package Updatable.Volo;

import Updatable.Aereo.Aereo;

public class VoloInternazionaleStrategy implements TipoVoloStrategy {
    @Override
    public Aereo.TipoVeicolo determinaTipoVolo() {
        return Aereo.TipoVeicolo.INTERNAZIONALE;
    }//determinaTipoVolo
}//VoloInternazionaleStrategy
