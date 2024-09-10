package UpdatableTest.Prenotazione;

import static org.junit.jupiter.api.Assertions.*;

import Updatable.Aereo.Aereo;
import Updatable.Prenotazione.PrenotazioneFidelity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import Updatable.Cliente.ClienteFedelta;
import Updatable.Volo.Volo;
import Updatable.Biglietto.BigliettoFidelity;
import java.time.LocalDate;
import java.time.LocalTime;

public class PrenotazioneFidelityTest {

    private PrenotazioneFidelity prenotazioneFidelity;
    private ClienteFedelta cliente;
    private Volo volo;
    private BigliettoFidelity biglietto;
    private Aereo aereo;

    @BeforeEach
    public void setUp() {
        // Creazione cliente fidelizzato
        cliente = new ClienteFedelta("mario.rossi@example.com", "Mario", "Rossi", "Via Roma 1", LocalDate.of(1980, 1, 1));

        // Creazione dell'aereo
        aereo = new Aereo(123456789, "Boeing 747", 300, Aereo.TipoVeicolo.INTERCONTINENTALE);

        // Creazione del volo con l'aereo associato
        volo = new Volo("FCO", "JFK", LocalTime.of(14, 0), LocalDate.of(2023, 12, 1), aereo);

        // Creazione della prenotazione
        prenotazioneFidelity = new PrenotazioneFidelity(cliente, volo);

        // Creazione del biglietto fidelity
        biglietto = new BigliettoFidelity(cliente, "Mario", "Rossi", volo, 500.0, "Economy");
    }

    @Test
    public void testAggiungiBigliettoFidelityCorretto() {
        prenotazioneFidelity.aggiungiBiglietto(biglietto);
        assertEquals(1, prenotazioneFidelity.getBiglietti().size());
    }

    @Test
    public void testAggiungiBigliettoFidelityIncorretto() {
        ClienteFedelta altroCliente = new ClienteFedelta("luca.verdi@example.com", "Luca", "Verdi", "Via Milano 2", LocalDate.of(1990, 1, 1));
        BigliettoFidelity altroBiglietto = new BigliettoFidelity(altroCliente, "Luca", "Verdi", volo, 400.0, "Economy");
        assertThrows(IllegalArgumentException.class, () -> prenotazioneFidelity.aggiungiBiglietto(altroBiglietto));
    }

    @Test
    public void testIsScadutaPrenotazioneFidelityNonScaduta() {
        Volo voloFuturo = new Volo("FCO", "JFK", LocalTime.of(14, 0), LocalDate.now().plusDays(10), aereo);
        PrenotazioneFidelity prenotazioneFuturo = new PrenotazioneFidelity(cliente, voloFuturo);
        assertFalse(prenotazioneFuturo.isScaduta());
    }

    @Test
    public void testIsScadutaPrenotazioneFidelityScaduta() {
        Volo voloPassato = new Volo("FCO", "JFK", LocalTime.of(14, 0), LocalDate.now().minusDays(5), aereo);
        PrenotazioneFidelity prenotazionePassata = new PrenotazioneFidelity(cliente, voloPassato);
        assertTrue(prenotazionePassata.isScaduta());
    }
}
