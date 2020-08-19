package it.polito.tdp.camminoAutobus.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.camminoAutobus.db.CorsaDao;

public class TestModel {

	public static void main(String[] args) {
		LocalTime ltorario=LocalTime.of(15, 00);
		String tipo="PARTENZA";
		String scelta="orari_estivo_feriale";
		CorsaDao dao=new CorsaDao(scelta);
		List<Collegamento> collegamenti = dao.listAllCollegamenti();
		DirectedWeightedMultigraph<Collegamento, Arco> grafo = new DirectedWeightedMultigraph<Collegamento, Arco>(Arco.class);
		Graphs.addAllVertices(grafo, collegamenti);
		
		int oreDistanza=3;
		//sto guardando spostamenti urbani, quindi e' impossibile e controproduttivo che il numero di ore impiegato sia alto
		long inizio=System.nanoTime();
		/*
		if(tipo.equals("PARTENZA")) {
			//il grafo ha come archi tutte quelle corse che vanno dall'orario selezionato fino a x ore successive
			LocalTime orarioFineInteresse=ltorario.plus(Duration.ofHours(oreDistanza));
			List<Corsa> corse = dao.listAllCorseByOrario(ltorario, orarioFineInteresse);
			for(Corsa corsa: corse) {
				List<FermataAutobus>fermate=dao.getAllFermateById(corsa.getId(),ltorario, orarioFineInteresse);				
				LocalDate giornoTemp=LocalDate.ofYearDay(1998, 1);
				int flag=0;
				for(int k=0;k+1<fermate.size();k++) {
					FermataAutobus fermataPartenza=fermate.get(k);
					FermataAutobus fermataArrivo=fermate.get(k+1);
					LocalDateTime inserireTemp=LocalDateTime.of(giornoTemp, fermataPartenza.getOrario());
					Arco arco=new Arco(corsa,inserireTemp);
					grafo.addEdge(fermataPartenza.getCollegamento(), fermataArrivo.getCollegamento(), arco);
					long peso=Duration.between(fermataPartenza.getOrario(),fermataArrivo.getOrario()).toMinutes();
					if(fermataArrivo.getOrario().isBefore(fermataPartenza.getOrario())) {
						//in questo caso si passa ad un giorno successivo, questo accade una sola volta.
						giornoTemp=giornoTemp.plusDays(1);
						peso=Duration.between(LocalDateTime.of(LocalDate.ofYearDay(1998, 1), fermataPartenza.getOrario()),LocalDateTime.of(LocalDate.ofYearDay(1998, 2), fermataArrivo.getOrario())).toMinutes();
						flag++;
						if(flag>1) {
							System.out.println("ERRORE! all'interno di una corsa ci puo' essere un solo caso di fermata successiva che abbia tempo minore della precedente,"
									+ " ossia quando si passa ad un giorno successivo");
						}
					}
					grafo.setEdgeWeight(arco, peso);
				}
				
			}
			System.out.println(grafo.edgeSet().size());
		}
		*/
		
	
		
		
		
		
		
		if(tipo.equals("PARTENZA")) {
			//il grafo ha come archi tutte quelle corse che vanno dall'orario selezionato fino a x ore successive
			LocalTime orarioFineInteresse=ltorario.plus(Duration.ofHours(oreDistanza));
				List<FermataAutobus>fermate=dao.getNuovo(ltorario, orarioFineInteresse);				
				LocalDate giornoTemp=LocalDate.ofYearDay(1998, 1);
				int flag=0;
				for(int k=0;k+1<fermate.size();k++) {
					FermataAutobus fermataPartenza=fermate.get(k);
					FermataAutobus fermataArrivo=fermate.get(k+1);
					if(fermataPartenza.getCorsa().equals(fermataArrivo.getCorsa())) {
						LocalDateTime inserireTemp=LocalDateTime.of(giornoTemp, fermataPartenza.getOrario());
						Arco arco=new Arco(fermataPartenza.getCorsa(),inserireTemp);
						grafo.addEdge(fermataPartenza.getCollegamento(), fermataArrivo.getCollegamento(), arco);
						long peso=Duration.between(fermataPartenza.getOrario(),fermataArrivo.getOrario()).toMinutes();
						if(fermataArrivo.getOrario().isBefore(fermataPartenza.getOrario())) {
							//in questo caso si passa ad un giorno successivo, questo accade una sola volta.
							giornoTemp=giornoTemp.plusDays(1);
							peso=Duration.between(LocalDateTime.of(LocalDate.ofYearDay(1998, 1), fermataPartenza.getOrario()),LocalDateTime.of(LocalDate.ofYearDay(1998, 2), fermataArrivo.getOrario())).toMinutes();
							flag++;
							if(flag>1) {
								System.out.println("ERRORE! all'interno di una corsa ci puo' essere un solo caso di fermata successiva che abbia tempo minore della precedente,"
										+ " ossia quando si passa ad un giorno successivo");
							}
						}
						grafo.setEdgeWeight(arco, peso);
				} 
				}
		}
		System.out.println(grafo.edgeSet().size());
		long fine=System.nanoTime();
		double distanza=(double) (fine-inizio)/1000000000;
		System.out.println(distanza);
	}
	
	
}
