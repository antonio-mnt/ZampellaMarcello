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
	
	CorsaDao dao;
	List<Integer> codiciLocali;
	DirectedWeightedMultigraph<Integer, Arco> grafo;
	List<Corsa> corse;
	private int numeroMassimo;
	private LocalDateTime orarioIndicato; //questo orario, a seconda dei casi, puo' indicare l'orario di partenza o l'orario di arrivo
	private int tempoMinimo;
	private ArrayList<Integer> miglioreSequenza;
	private ArrayList<Arco> miglioreSequenzaArchi;
	private LocalDateTime miglioreOrario;
	private int miglioreCambiAutobus;
	

	public List<Integer> listAllFermate() {
		return codiciLocali;
	}

	/**
	 * Grafo i cui nodi sono i codici locali (zone della citta'), mentre gli archi sono oggetti il cui peso e' la 
	 * distanza in minuti tra 2 codici locali per una data linea
	 * @param ltorario orario di interesse
	 * @param tipo indica partenza o arrivo all'orario indicato
	 * @param scelta indica la tabella degli orari (estivo/invernale feriale/festivo)
	 * @return 
	 */
	public DirectedWeightedMultigraph<Integer, Arco> creaGrafo(LocalTime ltorario, String tipo, String scelta) {
		dao=new CorsaDao(scelta);
		codiciLocali=dao.listAllCodiceLocale();
		grafo=new DirectedWeightedMultigraph<Integer, Arco>(Arco.class);
		Graphs.addAllVertices(grafo, codiciLocali);
		
		int oreDistanza=3;
		//sto guardando spostamenti urbani, quindi e' impossibile e controproduttivo che il numero di ore impiegato sia alto
		
		
		if(tipo.equals("PARTENZA")) {
			//il grafo ha come archi tutte quelle corse che vanno dall'orario selezionato fino a x ore successive
			LocalTime orarioFineInteresse=ltorario.plus(Duration.ofHours(oreDistanza));
			corse=dao.listAllCorseByOrario(ltorario, orarioFineInteresse);
			for(Corsa corsa: corse) {
				List<FermataAutobus>fermate=dao.getAllFermateById(corsa.getId(),ltorario, orarioFineInteresse);
				LocalDate giornoTemp=LocalDate.ofYearDay(1998, 1);
				int flag=0;
				for(int k=0;k+1<fermate.size();k++) {
					FermataAutobus fermataPartenza=fermate.get(k);
					FermataAutobus fermataArrivo=fermate.get(k+1);
					if(fermataArrivo.getOrario().isBefore(fermataPartenza.getOrario())) {
						//in questo caso si passa ad un giorno successivo, questo accade una sola volta.
						giornoTemp=giornoTemp.plusDays(1);
						flag++;
						if(flag>1) {
							System.out.println("ERRORE! all'interno di una corsa ci puo' essere un solo caso di fermata successiva che abbia tempo minore della precedente,"
									+ " ossia quando si passa ad un giorno successivo");
						}
					}
					LocalDateTime inserireTemp=LocalDateTime.of(giornoTemp, fermataPartenza.getOrario());
					Arco arco=new Arco(corsa,inserireTemp);
					grafo.addEdge(fermataPartenza.getCodiceLocale(), fermataArrivo.getCodiceLocale(), arco);
					grafo.setEdgeWeight(arco, Duration.between(fermataPartenza.getOrario(),fermataArrivo.getOrario()).toMinutes());
				}
				
			}
		} else if(tipo.equals("ARRIVO")) {
			//il grafo ha come archi tutte quelle corse che vanno dal x ore precedenti fino all'orario selezionato. Il caso e' molto simile al precedente,
			// basta infatti aggiustare gli orari passate alle funzioni listAllCorseByOrario e getAllFermateById
			LocalTime orarioInizioInteresse=ltorario.minus(Duration.ofHours(oreDistanza));
			corse=dao.listAllCorseByOrario(orarioInizioInteresse, ltorario);
			for(Corsa corsa: corse) {
				List<FermataAutobus>fermate=dao.getAllFermateById(corsa.getId(),orarioInizioInteresse, ltorario);
				LocalDate giornoTemp=LocalDate.ofYearDay(1998, 1);
				int flag=0;
				for(int k=0;k+1<fermate.size();k++) {
					FermataAutobus fermataPartenza=fermate.get(k);
					FermataAutobus fermataArrivo=fermate.get(k+1);
					LocalDateTime inserireTemp=LocalDateTime.of(giornoTemp, fermataPartenza.getOrario());
					Arco arco=new Arco(corsa,inserireTemp);
					grafo.addEdge(fermataPartenza.getCodiceLocale(), fermataArrivo.getCodiceLocale(), arco);
					long peso=Duration.between(fermataPartenza.getOrario(),fermataArrivo.getOrario()).toMinutes();
					if(fermataArrivo.getOrario().isBefore(fermataPartenza.getOrario())) {
						//in questo caso si passa ad un giorno successivo, questo accade una sola volta.
						giornoTemp=giornoTemp.plusDays(1);
						peso=Duration.between(fermataArrivo.getOrario(),fermataPartenza.getOrario()).toMinutes();
						flag++;
						if(flag>1) {
							System.out.println("ERRORE! all'interno di una corsa ci puo' essere un solo caso di fermata successiva che abbia tempo minore della precedente,"
									+ " ossia quando si passa ad un giorno successivo");
						}
					}
					grafo.setEdgeWeight(arco, peso);
				}
				
			}
		
			
			
		} else {
			System.out.println("ERRORE!");
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
	public ArrayList<Arco> cercaPercorso(int partenza, int arrivo, LocalTime orario, String scelta, int numeroMassimo) {
		this.miglioreSequenzaArchi=null;
		ArrayList<Integer> parziale=new ArrayList();
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
			int codiceLocaleAttuale=partenza;
			espandiPartenza(parziale,parzialeArchi,codiceLocaleAttuale,successivi, corsaAttuale ,cambiAutobus, arrivo);
		} else if(scelta.equals("ARRIVO")) {
			parziale.add(arrivo);
			Set<Arco> precedenti = nuoviArchiArrivo(arrivo,this.orarioIndicato);
			int codiceLocaleAttuale=arrivo;
			espandiArrivo(parziale,parzialeArchi,codiceLocaleAttuale,precedenti, corsaAttuale ,cambiAutobus, partenza);
		}
		System.out.println(this.miglioreSequenzaArchi);
		return this.miglioreSequenzaArchi;
		
	}


	private void espandiPartenza(ArrayList<Integer> parziale, ArrayList<Arco> parzialeArchi, int codiceLocaleAttuale, Set<Arco> successivi, Corsa corsaAttuale,
			int cambiAutobus, int arrivo) {
		
		if(codiceLocaleAttuale==arrivo) {
			//CONDIZIONE DI TERMINAZIONE
			Arco ultimoArco=parzialeArchi.get(parzialeArchi.size()-1);
			LocalDateTime orarioArrivo=ultimoArco.getOrarioPartenza().plusMinutes((long) grafo.getEdgeWeight(ultimoArco));
			//LocalTime partenzaEffettiva=parzialeArchi.get(0).getOrarioPartenza();
			int tempoReale=(int) Duration.between(this.orarioIndicato, orarioArrivo).toMinutes();
			if(tempoReale<=this.tempoMinimo) {
				if(tempoReale<this.tempoMinimo || cambiAutobus<this.miglioreCambiAutobus) {
					this.tempoMinimo=tempoReale;
					this.miglioreSequenza=new ArrayList<Integer>(parziale);
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
						int codSuccessivo=grafo.getEdgeTarget(arcoSuccessivo);
						parziale.add(codSuccessivo);
						parzialeArchi.add(arcoSuccessivo);
						espandiPartenza(parziale,parzialeArchi, codSuccessivo,this.nuoviArchiPartenza(codSuccessivo,orarioAttuale),nuovaCorsa,nuovoCambiAutobus,arrivo);
						parziale.remove(parziale.indexOf(codSuccessivo));
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
	private Set<Arco> nuoviArchiPartenza(int partenza, LocalDateTime orarioAttuale) {
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
	
	
	
	private void espandiArrivo(ArrayList<Integer> parziale, ArrayList<Arco> parzialeArchi, int codiceLocaleAttuale, Set<Arco> precedenti, Corsa corsaAttuale,
			int cambiAutobus, int arrivo) {
		
		if(codiceLocaleAttuale==arrivo) {
			//CONDIZIONE DI TERMINAZIONE
			Arco primoArco=parzialeArchi.get(parzialeArchi.size()-1);
			LocalDateTime orarioPartenza=primoArco.getOrarioPartenza();
			//LocalTime partenzaEffettiva=parzialeArchi.get(0).getOrarioPartenza();
			int tempoReale=(int) Duration.between(orarioPartenza,this.orarioIndicato).toMinutes();
			if(tempoReale<=this.tempoMinimo) {
				if(tempoReale<this.tempoMinimo || cambiAutobus<this.miglioreCambiAutobus) {
					this.tempoMinimo=tempoReale;
					this.miglioreSequenza=new ArrayList<Integer>(parziale);
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
						int codPrecedente=grafo.getEdgeSource(arcoPrecedente);
						parziale.add(codPrecedente);
						parzialeArchi.add(arcoPrecedente);
						espandiArrivo(parziale,parzialeArchi, codPrecedente,this.nuoviArchiArrivo(codPrecedente,orarioAttuale),nuovaCorsa,nuovoCambiAutobus,arrivo);
						parziale.remove(parziale.indexOf(codPrecedente));
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
	private Set<Arco> nuoviArchiArrivo(int arrivo, LocalDateTime orarioAttuale) {
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
