package it.polito.tdp.camminoAutobus.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import it.polito.tdp.camminoAutobus.db.CorsaDao;

public class Model {
	
	private CorsaDao dao;
	private List<Collegamento> collegamenti;
	private DirectedWeightedMultigraph<Collegamento, Arco> grafo;
	private int numeroMassimo;
	private LocalDateTime orarioIndicato; //questo orario, a seconda dei casi, puo' indicare l'orario di partenza o l'orario di arrivo
	private int tempoMinimo;
	private ArrayList<Arco> miglioreSequenzaArchi;
	private LocalDateTime miglioreOrario;
	private int miglioreCambiAutobus;
	private String sceltaRicerca;
	private int oreDistanza;
	private int tempoEffettivoMinimo;
	

	public List<Collegamento> listAllCollegamenti() {
		return collegamenti;
	}
	
	
	public List<Collegamento> getCollegamenti() {
		return collegamenti;
	}


	/**
	 * Grafo i cui nodi sono i codici locali (zone della citta'), mentre gli archi sono oggetti il cui peso e' la 
	 * distanza in minuti tra 2 codici locali per una data linea
	 * @param ltorario orario di interesse
	 * @param tipo indica partenza o arrivo all'orario indicato
	 * @param scelta indica la tabella degli orari (estivo/invernale feriale/festivo)
	 * @return grafo creato
	 * @throws EccezioneLoop 
	 */
	public DirectedWeightedMultigraph<Collegamento, Arco> creaGrafo(LocalTime ltorario, String tipo, String scelta)
			throws EccezioneLoop {
		dao=new CorsaDao(scelta);
		collegamenti=dao.listAllCollegamenti();
		grafo=new DirectedWeightedMultigraph<Collegamento, Arco>(Arco.class);
		Graphs.addAllVertices(grafo, collegamenti);
		
		this.oreDistanza=3;
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
			//boolean nuovaCorsa=true;
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
						//ho notato che uno dei maggiori problemi nel database di prova era la presenza di righe uguali adiacenti
						throw new EccezioneLoop(fermataPartenza);
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
		
	/**
	 * data una stringa , ricerca tutti i collegamenti (codice locale, Descrizione stazione) la cui descrizione stazione contiene la stringa
	 * @param Stringa da contenere
	 * @return Lista di collegamenti
	 */
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
	 * @param sceltaRicerca 
	 * @return 
	 */
	public ArrayList<Arco> cercaPercorso(Collegamento partenza, Collegamento arrivo, LocalTime orario, String scelta,
			int numeroMassimo, String sceltaRicerca) {
		this.miglioreSequenzaArchi=null;
		ArrayList<Collegamento> parziale=new ArrayList<Collegamento>();
		ArrayList<Arco> parzialeArchi=new ArrayList<Arco>();
		this.tempoMinimo=this.oreDistanza*60+1; //tempo minimo massimo e' dato dal tempo massimo deciso per il grafo
		this.tempoEffettivoMinimo=this.tempoMinimo;
		int cambiAutobus=0;
		this.orarioIndicato=LocalDateTime.of(LocalDate.ofYearDay(1998, 1), orario);
		Corsa corsaAttuale=new Corsa(-1,null,null,null);
		this.sceltaRicerca=sceltaRicerca;
    	if(this.sceltaRicerca.equals("TEMPO MINIMO")) {
    		this.numeroMassimo=numeroMassimo;
    	}
    	
    	
		this.miglioreCambiAutobus=9; //limite che vale sia per sceltaRicerca tempo minimo che per cambi minimi. Specialmente per la seconda e' importante non avere un numero troppo alto per non 
		//rendere la ricorsione eccessivamente lunga, considerando che e' altamente improbabile dover utilizzare un numero molto alto di autobus per spostarsi
		if(scelta.equals("PARTENZA")) {
			parziale.add(partenza);
			Set<Arco> successivi = nuoviArchiPartenza(partenza,this.orarioIndicato,parziale,parzialeArchi);
			Collegamento codiceLocaleAttuale=partenza;
			espandiPartenza(parziale,parzialeArchi,codiceLocaleAttuale,successivi, corsaAttuale ,cambiAutobus, arrivo);
		} else if(scelta.equals("ARRIVO")) {
			parziale.add(arrivo);
			this.orarioIndicato=this.orarioIndicato.plusDays(1);
			Set<Arco> precedenti = nuoviArchiArrivo(arrivo,this.orarioIndicato,parziale,parzialeArchi);
			Collegamento codiceLocaleAttuale=arrivo;
			espandiArrivo(parziale,parzialeArchi,codiceLocaleAttuale,precedenti, corsaAttuale ,cambiAutobus, partenza);
		}
		return this.miglioreSequenzaArchi;
		
	}


	private void espandiPartenza(ArrayList<Collegamento> parziale, ArrayList<Arco> parzialeArchi, Collegamento codiceLocaleAttuale, Set<Arco> successivi, Corsa corsaAttuale,
			int cambiAutobus, Collegamento arrivo) {
		
		if(codiceLocaleAttuale.equals(arrivo)) {
			//condizione di terminazione
			Arco ultimoArco=parzialeArchi.get(parzialeArchi.size()-1);
			LocalDateTime orarioArrivo=ultimoArco.getOrarioPartenza().plusMinutes((long) grafo.getEdgeWeight(ultimoArco));
			LocalDateTime partenzaEffettiva=parzialeArchi.get(0).getOrarioPartenza();
			int tempoEffettivo=(int) Duration.between(partenzaEffettiva, orarioArrivo).toMinutes();
			int tempoReale=(int) Duration.between(this.orarioIndicato, orarioArrivo).toMinutes();
			if(this.sceltaRicerca.equals("TEMPO MINIMO")) {
				if(tempoReale<=this.tempoMinimo) {
					//questa e' la prima condizione, se non e' vera questa non considero ne' cambi ne' tempo effettivo
					if(tempoReale<this.tempoMinimo || cambiAutobus<this.miglioreCambiAutobus || tempoEffettivo<this.tempoEffettivoMinimo) {
						this.tempoMinimo=tempoReale;
						this.miglioreSequenzaArchi=new ArrayList<Arco>(parzialeArchi);
						this.miglioreOrario=orarioArrivo;
						this.miglioreCambiAutobus=cambiAutobus;
						this.tempoEffettivoMinimo=tempoEffettivo;
						return;
					}
				}
			} else {
				if(cambiAutobus<=this.miglioreCambiAutobus) {
					//questa e' la prima condizione, se non e' vera questa non considero ne' tempo reale ne' tempo effettivo
					if(tempoReale<this.tempoMinimo || cambiAutobus<this.miglioreCambiAutobus || tempoEffettivo<this.tempoEffettivoMinimo) {
						this.tempoMinimo=tempoReale;
						this.miglioreSequenzaArchi=new ArrayList<Arco>(parzialeArchi);
						this.miglioreOrario=orarioArrivo;
						this.miglioreCambiAutobus=cambiAutobus;
						this.tempoEffettivoMinimo=tempoEffettivo;
						return;
					}
				}
			}	
		}
		
		for(Arco arcoSuccessivo: successivi) {
			Corsa nuovaCorsa=corsaAttuale;
			int nuovoCambiAutobus=cambiAutobus;
			boolean okay=true;
			//Nella funzione di ricerca degli archi ho gia' inserito la condizione che non voglio archi che portino a nodi gia' considerati
				LocalDateTime orarioAttuale=arcoSuccessivo.getOrarioPartenza().plusMinutes((long) grafo.getEdgeWeight(arcoSuccessivo));
				//il mio orario attuale e' l'orario di partenza per l'arco + il tempo necessario per il trasferimento (dato dal peso)
				int minutiPassati=(int) Duration.between(this.orarioIndicato, orarioAttuale).toMinutes();
				int tempLimiteAutobus;
				if(this.sceltaRicerca.equals("TEMPO MINIMO")) {
					tempLimiteAutobus=this.numeroMassimo;
				} else {
					tempLimiteAutobus=this.miglioreCambiAutobus;
				}
				if(!arcoSuccessivo.getCorsa().equals(corsaAttuale)) {
					//cambio autobus
					if(cambiAutobus+1<=tempLimiteAutobus) {
						nuovaCorsa=arcoSuccessivo.getCorsa();
						nuovoCambiAutobus++;
					} else {
						okay=false;
					}
				}
				if(okay) {
					if(this.sceltaRicerca.equals("TEMPO MINIMO")) {
						if(minutiPassati>this.tempoMinimo) {
							okay=false;
						}
					} else {
						if(nuovoCambiAutobus==this.miglioreCambiAutobus && minutiPassati>this.tempoMinimo)
							//la doppia condizione e' data dal fatto che non puo' il numero degli autobus esssere maggiore, se e' minore non guardo il tempo
							// perche' do priorita' ai cambi, se e' uguale vedo il tempo
							okay=false;
					}
				}
				
				if(okay) {
					Collegamento colSuccessivo=grafo.getEdgeTarget(arcoSuccessivo);
					parziale.add(colSuccessivo);
					parzialeArchi.add(arcoSuccessivo);
					espandiPartenza(parziale,parzialeArchi, colSuccessivo,this.nuoviArchiPartenza(colSuccessivo,orarioAttuale,parziale,parzialeArchi),nuovaCorsa,nuovoCambiAutobus,arrivo);
					parziale.remove(parziale.indexOf(colSuccessivo));
					parzialeArchi.remove(arcoSuccessivo);
				}	
			}
		}

	/**
	 * La funzione restituisce un solo arco per identificativo. Inoltre solo quegli archi il cui identificativo non e' stato gia' considerato o che e' considerato, ma con orario di partenza inferiore.
	 * identificativo diverso, ossia quello più vicino (temporalmente parlando) 
	 * @param partenza id del codice locale di partenza
	 * @param orarioAttuale 
	 * @param parziale 
	 * @param parzialeArchi 
	 * @return Set di archi interessanti
	 */
	private Set<Arco> nuoviArchiPartenza(Collegamento partenza, LocalDateTime orarioAttuale, ArrayList<Collegamento> parziale, ArrayList<Arco> parzialeArchi) {
		ArrayList<Arco> successivi=new ArrayList<Arco>(grafo.outgoingEdgesOf(partenza));
		ArrayList<Arco> eliminare=new ArrayList<Arco>();
		for(Arco arco:successivi) {
			if(parziale.contains(this.grafo.getEdgeTarget(arco))) {
				//vado ad eliminare tutti quegli archi che mi portano a fermate che ho gia' visitato.
				eliminare.add(arco);
			}
		}
		if(eliminare.size()>0) {
			successivi.removeAll(eliminare);
		}
		HashMap<String,Arco> considerati=new HashMap<String,Arco>();
		//vado a considerare solo 1 arco per identificativo il cui orario di partenza (orario di partenza+peso) e' NON PRIMA e il piu'
		//vicino possibile al mio orario attuale
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
		if(parzialeArchi.size()>2) {
			Corsa ultimaCorsa=parzialeArchi.get(parzialeArchi.size()-1).getCorsa();
			ArrayList<Corsa> corseVisitate=new ArrayList<Corsa>();
			for(Arco ar:parzialeArchi) {
				Corsa temp=ar.getCorsa();
				if(!corseVisitate.contains(temp)) {
					//parzialeArchi e' costituito da una serie di Arco con stesse Corsa. una volta che io ho studiato una Corsa 
					//e' inutile fare lo stesso procedimento per un arco con stessa corsa.
					corseVisitate.add(temp);
					Arco eliminaArco = null;
					for(Arco ar2:archiNuovo) {
						Corsa corsaDaControllare=ar2.getCorsa();
						if(temp.getIdentificativo().equals(corsaDaControllare.getIdentificativo()) && !corsaDaControllare.equals(ultimaCorsa)) {
							//degli possibili corse che posso prendere, voglio eliminare quelle corse che hanno un identificativo che ho gia' preso
							//e il cui orario di partenza sia maggiore dell'orario di partenza della corsa che ho gia' preso. Questo perche' se ho gia' preso
							// il 23 e' inutile scendere (ho di fatto escluso l'ultima corsa presa, in quanto sono ancora sopra) e poi salire su un altro 23,
							//a meno che io non abbia preso un altro autobus che abbia tagliato la strada del 23, facendomi prendere un 23 che  e' partito prima
							LocalTime primaPartenzaCorsa=temp.getOraPartenza();
							LocalTime utlimaPartenzaCorsa=this.orarioIndicato.plusHours(this.oreDistanza).toLocalTime();
							LocalTime controlloPartenza=corsaDaControllare.getOraPartenza();
							if(!controlloPartenza.isBefore(primaPartenzaCorsa) && !controlloPartenza.isAfter(utlimaPartenzaCorsa)) {
								//la corsa da controllare non deve essere all'interno di un intervallo di orari che parte dall'orario di
								//partenza della corsa gia' inserita e l'ultimo orario possibile.
								eliminaArco=ar2;
							}
						}
					}
				if(eliminaArco!=null) {
					archiNuovo.remove(eliminaArco);
				}
				}
			}
		}
		
		return archiNuovo;
	}
	
	
	
	private void espandiArrivo(ArrayList<Collegamento> parziale, ArrayList<Arco> parzialeArchi, Collegamento codiceLocaleAttuale, Set<Arco> precedenti, Corsa corsaAttuale,
			int cambiAutobus, Collegamento arrivo) {
		
		if(codiceLocaleAttuale.equals(arrivo)) {
			//CONDIZIONE DI TERMINAZIONE
			Arco primoArco=parzialeArchi.get(parzialeArchi.size()-1);
			LocalDateTime orarioPartenza=primoArco.getOrarioPartenza();
			int tempoReale=(int) Duration.between(orarioPartenza,this.orarioIndicato).toMinutes();
			Arco ultimoArco=parzialeArchi.get(0);
			LocalDateTime orarioArrivoEffettivo=ultimoArco.getOrarioPartenza().plusMinutes((long) grafo.getEdgeWeight(ultimoArco));
			int tempoEffettivo=(int) Duration.between(orarioPartenza, orarioArrivoEffettivo).toMinutes();
			if(this.sceltaRicerca.equals("TEMPO MINIMO")) {
				if(tempoReale<=this.tempoMinimo) {
					if(tempoReale<this.tempoMinimo || cambiAutobus<this.miglioreCambiAutobus || tempoEffettivo<this.tempoEffettivoMinimo) {
						this.tempoMinimo=tempoReale;
						this.miglioreSequenzaArchi=new ArrayList<Arco>(parzialeArchi);
						this.miglioreOrario=orarioPartenza;
						this.miglioreCambiAutobus=cambiAutobus;
						this.tempoEffettivoMinimo=tempoEffettivo;
						return;
					}
				}
			}  else {
				if(cambiAutobus<=this.miglioreCambiAutobus) {
					if(tempoReale<this.tempoMinimo || cambiAutobus<this.miglioreCambiAutobus || tempoEffettivo<this.tempoEffettivoMinimo) {
						this.tempoMinimo=tempoReale;
						this.miglioreSequenzaArchi=new ArrayList<Arco>(parzialeArchi);
						this.miglioreOrario=orarioPartenza;
						this.miglioreCambiAutobus=cambiAutobus;
						this.tempoEffettivoMinimo=tempoEffettivo;
						return;
					}
				}
			}	
		}
		
		for(Arco arcoPrecedente: precedenti) {
			Corsa nuovaCorsa=corsaAttuale;
			int nuovoCambiAutobus=cambiAutobus;
			boolean okay;
			okay = true;
			LocalDateTime orarioAttuale=arcoPrecedente.getOrarioPartenza();
			//Sto facendo il processo inverso, quindi il mio orario attuale e' l'orario di partenza dell'arco considerato
			int minutiPassati=(int) Duration.between(orarioAttuale, this.orarioIndicato).toMinutes();
			int tempLimiteAutobus;
			if(this.sceltaRicerca.equals("TEMPO MINIMO")) {
				tempLimiteAutobus=this.numeroMassimo;
			} else {
				tempLimiteAutobus=this.miglioreCambiAutobus;
			}
			if(!arcoPrecedente.getCorsa().equals(corsaAttuale)) {
				//cambio autobus
				if(cambiAutobus+1<=tempLimiteAutobus) {
					nuovaCorsa=arcoPrecedente.getCorsa();
					nuovoCambiAutobus++;
				} else {
					okay=false;
				}
			}
			if(this.sceltaRicerca.equals("TEMPO MINIMO")) {
				if(minutiPassati>this.tempoMinimo) {
					okay=false;
				}
			} else {
				if(cambiAutobus==this.miglioreCambiAutobus && minutiPassati>this.tempoMinimo)
					okay=false;
					//la doppia condizione e' data dal fatto che non puo' il numero degli autobus esssere maggiore, se e' minore non guardo il tempo
					// perche' do priorita' ai cambi, se e' uguale vedo il tempo
			}
			
			
			if(okay) {
				Collegamento colPrecedente=grafo.getEdgeSource(arcoPrecedente);
				parziale.add(colPrecedente);
				parzialeArchi.add(arcoPrecedente);
				espandiArrivo(parziale,parzialeArchi, colPrecedente,this.nuoviArchiArrivo(colPrecedente,orarioAttuale,parziale,parzialeArchi),nuovaCorsa,nuovoCambiAutobus,arrivo);
				parziale.remove(parziale.indexOf(colPrecedente));
				parzialeArchi.remove(arcoPrecedente);
			}			
		}
	}
	
	
	/**
	 * Metodo speculare a nuoviArchiPartenza.
	 * La funzione restituisce un solo arco per identificativo.
	 * Inoltre solo quegli archi il cui identificativo non e' stato gia' considerato o che e' considerato, ma con orario di partenza inferiore.
	 * @param arrivo
	 * @param orarioAttuale
	 * @param parzialeArchi 
	 * @param parziale 
	 * @return
	 */
	private Set<Arco> nuoviArchiArrivo(Collegamento arrivo, LocalDateTime orarioAttuale, ArrayList<Collegamento> parziale, ArrayList<Arco> parzialeArchi) {
		ArrayList<Arco> precedenti=new ArrayList<Arco>(grafo.incomingEdgesOf(arrivo));
		ArrayList<Arco> eliminare=new ArrayList<Arco>();
		for(Arco arco:precedenti) {
			if(parziale.contains(this.grafo.getEdgeSource(arco))) {
				//vado ad eliminare tutti quegli archi che mi portano a fermate che ho gia' visitato.
				eliminare.add(arco);
			}
		}
		if(eliminare.size()>0) {
			precedenti.removeAll(eliminare);
		}
		HashMap<String,Arco> considerati=new HashMap<String,Arco>();
		//vado a considerare solo 1 arco per identificativo il cui orario di arrivo (orario di partenza+peso) e' NON DOPO e il piu'
		//vicino possibile al mio orario attuale
		for(Arco arco:precedenti) {
			if(!arco.getOrarioPartenza().plusMinutes((long) grafo.getEdgeWeight(arco)).isAfter(orarioAttuale) ) {
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
		if(parzialeArchi.size()>2) {
			Corsa ultimaCorsa=parzialeArchi.get(parzialeArchi.size()-1).getCorsa();
			ArrayList<Corsa> corseVisitate=new ArrayList<Corsa>();
			for(Arco ar:parzialeArchi) {
				Corsa temp=ar.getCorsa();
				if(!corseVisitate.contains(temp)) {
					//parzialeArchi e' costituito da una serie di Arco con stesse Corsa. una volta che io ho studiato una Corsa 
					//e' inutile fare lo stesso procedimento per un arco con stessa corsa.
					corseVisitate.add(temp);
					Arco eliminaArco = null;
					for(Arco ar2:archiNuovo) {
						Corsa corsaDaControllare=ar2.getCorsa();
						if(temp.getIdentificativo().equals(corsaDaControllare.getIdentificativo()) && !corsaDaControllare.equals(ultimaCorsa)) {
							//degli possibili corse che posso prendere, voglio eliminare quelle corse che hanno un identificativo che ho gia' preso
							//e il cui orario di partenza sia maggiore dell'orario di partenza della corsa che ho gia' preso. Questo perche' se ho gia' preso
							// il 23 e' inutile scendere (ho di fatto escluso l'ultima corsa presa, in quanto sono ancora sopra) e poi salire su un altro 23,
							//a meno che io non abbia preso un altro autobus che abbia tagliato la strada del 23, facendomi prendere un 23 che  e' partito prima
							LocalTime primaPartenzaCorsa=temp.getOraPartenza();
							LocalTime utlimaPartenzaCorsa=this.orarioIndicato.minusHours(this.oreDistanza).toLocalTime();
							LocalTime controlloPartenza=corsaDaControllare.getOraPartenza();
							if(!controlloPartenza.isAfter(primaPartenzaCorsa) && !controlloPartenza.isBefore(utlimaPartenzaCorsa)) {
								//la corsa da controllare non deve essere all'interno di un intervallo di orari che parte dall'orario di
								//partenza della corsa gia' inserita e l'ultimo orario possibile.
								eliminaArco=ar2;
							}
						}
					}
				if(eliminaArco!=null) {
					archiNuovo.remove(eliminaArco);
				}
				}
			}
		}
		
		return archiNuovo;
	}
	
	
		
	}
