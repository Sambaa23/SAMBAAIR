package Updatable.Cliente;

import Updatable.Biglietto.BigliettoFidelity;
import Updatable.Prenotazione.PrenotazioneFidelity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EstrattoConto extends EstrattoContoAbstract{
    private ClienteFedelta cliente;
    private List<String> estratto;

    public EstrattoConto(ClienteFedelta cliente) {
        this.cliente = cliente;
        this.estratto = generaEstratto();
    }//Costruttore normale

    private List<String> generaEstratto() {
        List<String> estratto = new ArrayList<>();
        estratto.add("Estratto conto punti fedeltà per il cliente: " + cliente.getNome() + " " + cliente.getCognome());
        estratto.add("Punti accumulati totali: " + cliente.getPuntiAccumulati());
        estratto.add("");
        estratto.add("Dettaglio punti per volo:");

        for (PrenotazioneFidelity prenotazione : cliente.getPrenotazioni()) {
            for (BigliettoFidelity biglietto : prenotazione.getBiglietti()){
                estratto.add("Volo del " + biglietto.getVolo().dataVolo() + ": " + biglietto.getVolo().getPunti() + " punti");
            }
        }
        return estratto;
    }//generaEstratto

    public ClienteFedelta getCliente() {
        return cliente;
    }


    public List<String> getEstratto() {
        return estratto;
    }


    public void stampaEstratto() {
        for (String riga : estratto) {
            System.out.println(riga);
        }
    }//stampaEstratto

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EstrattoConto that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(cliente, that.cliente) && Objects.equals(estratto, that.estratto);
    }//equals

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cliente, estratto);
    }//hashCode

    @Override
    public String toString() {
        return "EstrattoConto{" +
                "cliente=" + cliente +
                ", estratto=" + estratto +
                '}';
    }//toString
}//estrattoConto
