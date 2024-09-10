package UpdatableTest.Cliente;

import static org.junit.jupiter.api.Assertions.*;

import Updatable.Aereo.Aereo;
import Updatable.Biglietto.Biglietto;
import Updatable.Cliente.Cliente;
import Updatable.Prenotazione.Prenotazione;
import Updatable.Volo.Volo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;

public class ClienteTest {

    private Cliente cliente;
    private Volo volo;
    private Aereo aereo;

    @BeforeEach
    void setUp() {
        aereo = new Aereo(126456789, "Boeing 747", 300, Aereo.TipoVeicolo.INTERCONTINENTALE);
        volo = new Volo("BCN", "LAX", LocalTime.of(14, 0), LocalDate.of(2024, 2, 18), aereo);
        cliente = new Cliente("email@test.com", "Mario", "Rossi", "Via Roma 1", LocalDate.of(1980, 5, 15));
    }

    @Test
    void testGetEmail() {
        assertEquals("email@test.com", cliente.getEmail());
    }

    @Test
    void testGetNome() {
        assertEquals("Mario", cliente.getNome());
    }

    @Test
    void testGetCognome() {
        assertEquals("Rossi", cliente.getCognome());
    }

    @Test
    void testGetIndirizzo() {
        assertEquals("Via Roma 1", cliente.getIndirizzo());
    }

    @Test
    void testGetDataNascita() {
        assertEquals(LocalDate.of(1980, 5, 15), cliente.getDataNascita());
    }

    @Test
    void testAggiungiPrenotazione() {
        Prenotazione prenotazione = new Prenotazione(cliente, volo);
        cliente.aggiungiPrenotazione(prenotazione);
        assertEquals(1, cliente.getPrenotazioni().size());
        assertThrows(IllegalArgumentException.class, () -> cliente.aggiungiPrenotazione(prenotazione));
    }

    @Test
    void testRichiediModificaBiglietto() {
        Biglietto biglietto = new Biglietto(cliente, "Luca", "Bianchi", volo, 150.0, "economy");
        Volo nuovoVolo = new Volo("BCN", "LAX", LocalTime.of(14, 15), LocalDate.of(2024, 4, 18), aereo);
        cliente.richiediModificaBiglietto(biglietto, nuovoVolo);
        assertEquals(nuovoVolo, biglietto.getVolo());
    }

    @Test
    void testClone() {
        Cliente clone = cliente.clone();
        assertEquals(cliente, clone);
        assertNotSame(cliente, clone);
    }

    @Test
    void testEquals() {
        Cliente sameCliente = new Cliente("email@test.com", "Mario", "Rossi", "Via Roma 1", LocalDate.of(1980, 5, 15));
        assertTrue(cliente.equals(sameCliente));
        Cliente differentCliente = new Cliente("email@other.com", "Giovanni", "Verdi", "Via Milano 2", LocalDate.of(1990, 10, 22));
        assertFalse(cliente.equals(differentCliente));
    }

    @Test
    void testHashCode() {
        Cliente sameCliente = new Cliente("email@test.com", "Mario", "Rossi", "Via Roma 1", LocalDate.of(1980, 5, 15));
        assertEquals(cliente.hashCode(), sameCliente.hashCode());
    }

    @Test
    void testToString() {
        String expected = "Cliente{email='email@test.com', nome='Mario', cognome='Rossi', indirizzo='Via Roma 1', dataNascita=1980-05-15, prenotazioni=[]}";
        assertEquals(expected, cliente.toString());
    }
}
