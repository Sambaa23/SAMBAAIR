package Updatable.Prenotazione;

import Updatable.Cliente.Cliente;
import Updatable.Volo.Volo;

import java.io.Serializable;
import java.util.Objects;

public class PrenotazioneID implements Serializable {
    private Cliente cliente;
    private Volo volo;

    public PrenotazioneID(Cliente cliente, Volo volo){
        this.cliente = cliente;
        this.volo = volo;
    }//Costruttore normale

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Updatable.Prenotazione.PrenotazioneID that)) return false;
        return Objects.equals(cliente, that.cliente) && Objects.equals(volo, that.volo);
    }//equals

    @Override
    public int hashCode() {
        return Objects.hash(cliente, volo);
    }//hashCode
}//PrenotazioneID
