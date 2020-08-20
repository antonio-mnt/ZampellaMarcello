package it.polito.tdp.camminoAutobus.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import it.polito.tdp.camminoAutobus.db.CorsaDao;

public class Model {
	
	private CorsaDao dao;
	private List<Collegamento> collegamenti;
	private DirectedWeightedMultigraph<Collegamento, Arco> grafo;
	private List<Corsa> corse;
	private int numeroMassimo;
	private LocalDateTime orarioIndicato; //questo orario, a seconda dei casi, puo' indicare l'orario di partenza o l'orario di arrivo
	private int tempoMinimo;
	private ArrayList<Collegamento> miglioreSequenza;
	private ArrayList<Arco> miglioreSequenzaArchi;
	private LocalDateTime miglioreOrario;
	private int miglioreCambiAutobus;
	

	public List<Collegamento> listAllCollegamenti() {
		return collegamenti;
	}

	/**
	 * Grafo i cui nodi sono i codici locali (zone della citta'), mentre gli archi sono oggetti il cui peso e' la 
	 * distanza in minuti tra 2 codici locali per una data linea
	 * @param ltorario orario di interesse
	 * @param tipo indica partenza o arrivo all'orario indicato
	 * @param scelta indica la tabella degli orari (estivo/invernale feriale/festivo)
	 * @return 
	 */
	public DirectedWeightedMultigraph<Collegamento, Arco> creaGrafo(LocalTime ltorario, String tipo, String scelta) {
		dao=new CorsaDao(scelta);
		collegamenti=dao.listAllCollegamenti();
		grafo=new DirectedWeightedMultigraph<Collegamento, Arco>(Arco.class);
		Graphs.addAllVertices(grafo, collegamenti);
		
		int oreDistanza=5;
		//sto guardando spostamenti urbani, quindi e' impossibile e controproduttivo che il numero di ore impiegato sia alto
		
		List<FermataAutobus>fermate = null;
		if(tipo.equals("PARTENZA")) {
			//il grafo ha come archi tutte quelle corse che vanno dall'orario selezionato fino a x ore successive
			LocalTime orarioFineInteresse=ltorario.plus(Duration.ofHours(oreDistanza));
			fermate=dao.getCorseByOrari(ltorario, orarioFineInteresse);
		} else if (tipo.equals("ARRIVO")) {
			LocalTime orarioInizioInteresse=ltorario.minus(Duration.ofHours(oreDistanza));
			fermate=dao.getCorseByOrari(orarioInizioInteresse, ltorario);
		}
			LocalDate giornoAttuale=LocalDate.ofYearDay(1998, 1);
			LocalDate giornoSuccessivo=LocalDate.ofYearDay(1998, 2);
			for(int k=0;k+1<fermate.size();k++) {
				FermataAutobus fermataPartenza=fermate.get(k);
				FermataAutobus fermataArrivo=fermate.get(k+1);
				if(fermataPartenza.getCorsa().equals(fermataArrivo.getCorsa())) {
					if(!giornoAttuale.equals(giornoSuccessivo) && fermataArrivo.getOrario().isBefore(ltorario)) {
						//2 casi a testa
						//ho scelto PARTENZA: 1) la corsa avviene tutta nello stesso giorno, quindi le fermate hanno un orario sempre maggiore di quello selezionato, percio' sono sempre in giorno attuale (1)
						//2) corsa avviene in giorni diversi, quindi alcune fermate hanno orario superiore a quello selezionato (es 23:50) e andranno in giorno attuale(1), altre inferiore a quello selezionato (es. 01:00) andranno con giornoSuccessivo (2)
						//se ho scelto ARRIVO 1) tutte le corse nello stesso giorno, quindi le fermate hanno un orario sempre minore di quello selzionato, percio' sono sempre in giornoSuccessivo (2)
						//2) corse in giorni diversi, quindi alcune fermate hanno orario inferiore a quello selezionato (queste rientrano in giorno successivo) (2), le altre hanno giorno inferiore. (1)
						giornoAttuale=giornoSuccessivo;
					}
					LocalDateTime inserireTemp=LocalDateTime.of(giornoAttuale, fermataPartenza.getOrario());
					Arco arco=new Arco(fermataPartenza.getCorsa(),inserireTemp);
					if(fermataPartenza.getCollegamento().equals(fermataArrivo.getCollegamento())) {
						System.out.println(fermataPartenza.getCollegamento()+"      "+fermataPartenza.getCorsa());
					}
					grafo.addEdge(fermataPartenza.getCollegamento(), fermataArrivo.getCollegamento(), arco);
					long peso=Duration.between(fermataPartenza.getOrario(),fermataArrivo.getOrario()).toMinutes();
					if(fermataArrivo.getOrario().isBefore(fermataPartenza.getOrario())) {
						//siamo in una corsa che inizia in un giorno e finisce nel giorno successivo 
						peso=Duration.between(LocalDateTime.of(LocalDate.ofYearDay(1998, 1), fermataPartenza.getOrario()),LocalDateTime.of(LocalDate.ofYearDay(1998, 2), fermataArrivo.getOrario())).toMinutes();
					}
					grafo.setEdgeWeight(arco, peso);
				} else {
					//passo ad una corsa successiva, quindi resetto il giorno.
					giornoAttuale=LocalDate.ofYearDay(1998, 1);
				}
			}
			return grafo;
		}
		

	public List<Collegamento> cercaCodice(String key) {
		return dao.getCodiciLocaliByStazione(key);
	}

	/**
	 * cerca il percorso piu' veloce per arrivare dalla fermata di partenza a quella di destinazione 
	 * @param partenza Codice Locale di partenza
	 * @param arrivo Codice Locale di destinazione
	 * @param orario orario di scelta
	 * @param scelta indica se l'orario deve essere di partenza o di arrivo
	 * @param numeroMassimo numero massimo di autobus consentiti
	 * @return 
	 */
	public ArrayList<Arco> cercaPercorso(Collegamento partenza, Collegamento arrivo, LocalTime orario, String scelta, int numeroMassimo) {
		this.miglioreSequenzaArchi=null;
		ArrayList<Collegamento> parziale=new ArrayList();
		ArrayList<Arco> parzialeArchi=new ArrayList();
		this.tempoMinimo=999999999;
		int cambiAutobus=0;
		this.orarioIndicato=LocalDateTime.of(LocalDate.ofYearDay(1998, 1), orario);
		Corsa corsaAttuale=new Corsa(-1,null,null);
		this.numeroMassimo=numeroMassimo;
		this.miglioreCambiAutobus=numeroMassimo+1;
		if(scelta.equals("PARTENZA")) {
			parziale.add(partenza);
			Set<Arco> successivi = nuoviArchiPartenza(partenza,this.orarioIndicato);
			Collegamento codiceLocaleAttuale=partenza;
			espandiPartenza(parziale,parzialeArchi,codiceLocaleAttuale,successivi, corsaAttuale ,cambiAutobus, arrivo);
		} else if(scelta.equals("ARRIVO")) {
			parziale.add(arrivo);
			this.orarioIndicato=this.orarioIndicato.plusDays(1);
			Set<Arco> precedenti = nuoviArchiArrivo(arrivo,this.orarioIndicato);
			Collegamento codiceLocaleAttuale=arrivo;
			espandiArrivo(parziale,parzialeArchi,codiceLocaleAttuale,precedenti, corsaAttuale ,cambiAutobus, partenza);
		}
		return this.miglioreSequenzaArchi;
		
	}


	private void espandiPartenza(ArrayList<Collegamento> parziale, ArrayList<Arco> parzialeArchi, Collegamento codiceLocaleAttuale, Set<Arco> successivi, Corsa corsaAttuale,
			int cambiAutobus, Collegamento arrivo) {
		
		if(codiceLocaleAttuale.equals(arrivo)) {
			//CONDIZIONE DI TERMINAZIONE
			Arco ultimoArco=parzialeArchi.get(parzialeArchi.size()-1);
			LocalDateTime orarioArrivo=ultimoArco.getOrarioPartenza().plusMinutes((long) grafo.getEdgeWeight(ultimoArco));
			//LocalTime partenzaEffettiva=parzialeArchi.get(0).getOrarioPartenza();
			int tempoReale=(int) Duration.between(this.orarioIndicato, orarioArrivo).toMinutes();
			if(tempoReale<=this.tempoMinimo) {
				if(tempoReale<this.tempoMinimo || cambiAutobus<this.miglioreCambiAutobus) {
					this.tempoMinimo=tempoReale;
					this.miglioreSequenza=new ArrayList<Collegamento>(parziale);
					this.miglioreSequenzaArchi=new ArrayList<Arco>(parzialeArchi);
					this.miglioreOrario=orarioArrivo;
					this.miglioreCambiAutobus=cambiAutobus;
					return;
				}
			}
			
			
			
		}
		
		for(Arco arcoSuccessivo: successivi) {
			Corsa nuovaCorsa=corsaAttuale;
			int nuovoCambiAutobus=cambiAutobus;
			boolean okay;
			if(!parziale.contains(grafo.getEdgeTarget(arcoSuccessivo))) {
					okay = true;
					LocalDateTime orarioAttuale=arcoSuccessivo.getOrarioPartenza().plusMinutes((long) grafo.getEdgeWeight(arcoSuccessivo));
					if(Duration.between(this.orarioIndicato, orarioAttuale).toMinutes()>this.tempoMinimo) {
						okay=false;
					}
					
					if(!arcoSuccessivo.getCorsa().equals(corsaAttuale)) {
						//cambio autobus
						if(cambiAutobus+1<=this.numeroMassimo) {
							nuovaCorsa=arcoSuccessivo.getCorsa();
							nuovoCambiAutobus++;
						} else {
							okay=false;
						}
					}
					
					if(okay) {
						Collegamento colSuccessivo=grafo.getEdgeTarget(arcoSuccessivo);
						parziale.add(colSuccessivo);
						parzialeArchi.add(arcoSuccessivo);
						espandiPartenza(parziale,parzialeArchi, colSuccessivo,this.nuoviArchiPartenza(colSuccessivo,orarioAttuale),nuovaCorsa,nuovoCambiAutobus,arrivo);
						parziale.remove(parziale.indexOf(colSuccessivo));
						parzialeArchi.remove(arcoSuccessivo);
					}
					
					
				}
				
			}
		}

	/**
	 * Gli archi che voglio visitare nella mia ricorsione non sono tutti quelli che collegano due fermate, ma solo uno per 
	 * identificativo diverso, ossia quello più vicino (temporalmente parlando) 
	 * @param partenza id del codice locale di partenza
	 * @param orarioAttuale 
	 * @return Set di archi interessanti
	 */
	private Set<Arco> nuoviArchiPartenza(Collegamento partenza, LocalDateTime orarioAttuale) {
		Set<Arco> successivi=grafo.outgoingEdgesOf(partenza);
		HashMap<String,Arco> considerati=new HashMap<String,Arco>();
		for(Arco arco:successivi) {
			if(!arco.getOrarioPartenza().isBefore(orarioAttuale) ) {
				String identificativo=arco.getCorsa().getIdentificativo();
				if(considerati.containsKey(identificativo)) {
					Arco temp=considerati.get(identificativo);
						if(arco.getOrarioPartenza().isBefore(temp.getOrarioPartenza())) {
							considerati.put(identificativo, arco);
						}
				} else {
					considerati.put(identificativo, arco);
				}
			}
		}
		Set<Arco> archiNuovo=new HashSet<>(considerati.values());
		return archiNuovo;
	}
	
	
	
	private void espandiArrivo(ArrayList<Collegamento> parziale, ArrayList<Arco> parzialeArchi, Collegamento codiceLocaleAttuale, Set<Arco> precedenti, Corsa corsaAttuale,
			int cambiAutobus, Collegamento arrivo) {
		
		if(codiceLocaleAttuale.equals(arrivo)) {
			//CONDIZIONE DI TERMINAZIONE
			Arco primoArco=parzialeArchi.get(parzialeArchi.size()-1);
			LocalDateTime orarioPartenza=primoArco.getOrarioPartenza();
			//LocalTime partenzaEffettiva=parzialeArchi.get(0).getOrarioPartenza();
			int tempoReale=(int) Duration.between(orarioPartenza,this.orarioIndicato).toMinutes();
			if(tempoReale<=this.tempoMinimo) {
				if(tempoReale<this.tempoMinimo || cambiAutobus<this.miglioreCambiAutobus) {
					this.tempoMinimo=tempoReale;
					this.miglioreSequenza=new ArrayList<Collegamento>(parziale);
					this.miglioreSequenzaArchi=new ArrayList<Arco>(parzialeArchi);
					this.miglioreOrario=orarioPartenza;
					this.miglioreCambiAutobus=cambiAutobus;
					return;
				}
			}
			
			
			
		}
		
		for(Arco arcoPrecedente: precedenti) {
			Corsa nuovaCorsa=corsaAttuale;
			int nuovoCambiAutobus=cambiAutobus;
			boolean okay;
			if(!parziale.contains(grafo.getEdgeSource(arcoPrecedente))) {
					okay = true;
					LocalDateTime orarioAttuale=arcoPrecedente.getOrarioPartenza();
					if(Duration.between(orarioAttuale, this.orarioIndicato).toMinutes()>this.tempoMinimo) {
						okay=false;
					}
					if(!arcoPrecedente.getCorsa().equals(corsaAttuale)) {
						//cambio autobus
						if(cambiAutobus+1<=this.numeroMassimo) {
							nuovaCorsa=arcoPrecedente.getCorsa();
							nuovoCambiAutobus++;
						} else {
							okay=false;
						}
					}
					
					if(okay) {
						Collegamento colPrecedente=grafo.getEdgeSource(arcoPrecedente);
						parziale.add(colPrecedente);
						parzialeArchi.add(arcoPrecedente);
						espandiArrivo(parziale,parzialeArchi, colPrecedente,this.nuoviArchiArrivo(colPrecedente,orarioAttuale),nuovaCorsa,nuovoCambiAutobus,arrivo);
						parziale.remove(parziale.indexOf(colPrecedente));
						parzialeArchi.remove(arcoPrecedente);
					}
					
					
				}
				
			}
		}
	
	
	/**
	 * Metodo speculare a nuoviArchiPartenza.
	 * Gli archi che voglio visitare nella mia ricorsione non sono tutti quelli che collegano due fermate, ma solo uno per 
	 * identificativo diverso, ossia quello più vicino (temporalmente parlando) 
	 * @param arrivo
	 * @param orarioAttuale
	 * @return
	 */
	private Set<Arco> nuoviArchiArrivo(Collegamento arrivo, LocalDateTime orarioAttuale) {
		Set<Arco> precedenti=grafo.incomingEdgesOf(arrivo);
		HashMap<String,Arco> considerati=new HashMap<String,Arco>();
		for(Arco arco:precedenti) {
			if(!arco.getOrarioPartenza().isAfter(orarioAttuale) ) {
				String identificativo=arco.getCorsa().getIdentificativo();
				if(considerati.containsKey(identificativo)) {
					Arco temp=considerati.get(identificativo);
						if(arco.getOrarioPartenza().isAfter(temp.getOrarioPartenza())) {
							considerati.put(identificativo, arco);
						}
				} else {
					considerati.put(identificativo, arco);
				}
			}
		}
		Set<Arco> archiNuovo=new HashSet<>(considerati.values());
		return archiNuovo;
	}


	public LocalDateTime getMiglioreOrario() {
		return miglioreOrario;
	}
	
	
	
		
	}
