package UpdatableTest.Prenotazione;

import static org.junit.jupiter.api.Assertions.*;

import Updatable.Aereo.Aereo;
import Updatable.Prenotazione.Prenotazione;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import Updatable.Cliente.Cliente;
import Updatable.Volo.Volo;
import Updatable.Biglietto.Biglietto;
import java.time.LocalDate;
import java.time.LocalTime;

public class PrenotazioneTest {

    private Prenotazione prenotazione;
    private Cliente cliente;
    private Volo volo;
    private Biglietto biglietto;
    private Aereo aereo;

    @BeforeEach
    public void setUp() {
        // Creazione cliente
        cliente = new Cliente("mario.rossi@example.com", "Mario", "Rossi", "Via Roma 1", LocalDate.of(1980, 1, 1));

        // Creazione dell'aereo
        aereo = new Aereo(123456789, "Boeing 747", 300, Aereo.TipoVeicolo.INTERCONTINENTALE);

        // Creazione del volo con l'aereo associato
        volo = new Volo("FCO", "JFK", LocalTime.of(14, 0), LocalDate.of(2023, 12, 1), aereo);

        // Creazione della prenotazione
        prenotazione = new Prenotazione(cliente, volo);

        // Creazione del biglietto
        biglietto = new Biglietto(cliente, "Mario", "Rossi", volo, 500.0, "Economy");
    }

    @Test
    public void testAggiungiBigliettoCorretto() {
        prenotazione.aggiungiBiglietto(biglietto);
        assertEquals(1, prenotazione.getBiglietti().size());
    }

    @Test
    public void testAggiungiBigliettoIncorretto() {
        Cliente altroCliente = new Cliente("luca.verdi@example.com", "Luca", "Verdi", "Via Milano 2", LocalDate.of(1990, 1, 1));
        Biglietto altroBiglietto = new Biglietto(altroCliente, "Luca", "Verdi", volo, 400.0, "Economy");
        assertThrows(IllegalArgumentException.class, () -> prenotazione.aggiungiBiglietto(altroBiglietto));
    }

    @Test
    public void testIsScadutaPrenotazioneNonScaduta() {
        Volo voloFuturo = new Volo("FCO", "JFK", LocalTime.of(14, 0), LocalDate.now().plusDays(10), aereo);
        Prenotazione prenotazioneFuturo = new Prenotazione(cliente, voloFuturo);
        assertFalse(prenotazioneFuturo.isScaduta());
    }

    @Test
    public void testIsScadutaPrenotazioneScaduta() {
        Volo voloPassato = new Volo("FCO", "JFK", LocalTime.of(14, 0), LocalDate.now().minusDays(5), aereo);
        Prenotazione prenotazionePassata = new Prenotazione(cliente, voloPassato);
        assertTrue(prenotazionePassata.isScaduta());
    }
}