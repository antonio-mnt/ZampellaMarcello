package it.polito.tdp.camminoAutobus.model;

import java.time.Duration;
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
	private LocalTime orarioPartenza;
	private int tempoMinimo;
	private ArrayList<Integer> miglioreSequenza;
	private ArrayList<Arco> miglioreSequenzaArchi;
	private LocalTime miglioreOrarioArrivo;
	
	public Model() {
		dao=new CorsaDao();
	}

	public List<Integer> listAllFermate() {
		codiciLocali=dao.listAllCodiceLocale();
		return codiciLocali;
		
	}

	/**
	 * Grafo i cui nodi sono i codici locali (zone della citta'), mentre gli archi sono oggetti il cui peso e' la 
	 * distanza in minuti tra 2 codici locali per una data linea
	 * @param orario orario di interesse
	 * @param tipo indica partenza o arrivo all'orario indicato
	 * @param sceltaSettimana 
	 * @param sceltaStagione 
	 */
	public void creaGrafo(String orario, String tipo, String sceltaStagione, String sceltaSettimana) {
		//AGGIUSTARE SCELTA STAGIONE E SCELTA SETTIMANA
		grafo=new DirectedWeightedMultigraph<Integer, Arco>(Arco.class);
		Graphs.addAllVertices(grafo, codiciLocali);
		corse=dao.listAllCorse();
		if(tipo.equals("PARTENZA")) {
		for(Corsa corsa: corse) {
			List<FermataAutobus>fermate=dao.getAllFermateById(corsa.getId(),orario);
			for(int k=0;k+1<fermate.size();k++) {
				FermataAutobus fermataPartenza=fermate.get(k);
				FermataAutobus fermataArrivo=fermate.get(k+1);
				Arco arco=new Arco(corsa,fermataPartenza.getOrario());
				grafo.addEdge(fermataPartenza.getCodiceLocale(), fermataArrivo.getCodiceLocale(), arco);
				grafo.setEdgeWeight(arco, Duration.between(fermataPartenza.getOrario(),fermataArrivo.getOrario()).toMinutes());
			}
			
		}
		} else if(tipo.equals("ARRIVO")) {
			
		} else {
			System.out.println("ERRORE!");
		}
		System.out.println("GRAFO DI "+grafo.vertexSet().size()+" nodi e "+grafo.edgeSet().size()+" archi");
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
		ArrayList<Integer> parziale=new ArrayList();
		ArrayList<Arco> parzialeArchi=new ArrayList();
		parziale.add(partenza);
		this.tempoMinimo=999999999;
		int cambiAutobus=0;
		Set<Arco> successivi = nuoviArchi(partenza,orario);
		Corsa corsaAttuale=new Corsa(-1,null,null);
		int codiceLocaleAttuale=partenza;
		this.numeroMassimo=numeroMassimo;
		this.orarioPartenza=orario;
		espandi(parziale,parzialeArchi,codiceLocaleAttuale,successivi, corsaAttuale ,cambiAutobus, arrivo);
		return this.miglioreSequenzaArchi;
		
	}


	private void espandi(ArrayList<Integer> parziale, ArrayList<Arco> parzialeArchi, int codiceLocaleAttuale, Set<Arco> successivi, Corsa corsaAttuale,
			int cambiAutobus, int arrivo) {
		
		if(codiceLocaleAttuale==arrivo) {
			//CONDIZIONE DI TERMINAZIONE
			Arco ultimoArco=parzialeArchi.get(parzialeArchi.size()-1);
			LocalTime orarioArrivo=ultimoArco.getOrarioPartenza().plusMinutes((long) grafo.getEdgeWeight(ultimoArco));
			//LocalTime partenzaEffettiva=parzialeArchi.get(0).getOrarioPartenza();
			int tempoReale=(int) Duration.between(this.orarioPartenza, orarioArrivo).toMinutes();
			if(tempoReale<this.tempoMinimo) {
				this.tempoMinimo=tempoReale;
				this.miglioreSequenza=new ArrayList<Integer>(parziale);
				this.miglioreSequenzaArchi=new ArrayList<Arco>(parzialeArchi);
				this.miglioreOrarioArrivo=orarioArrivo;
				return;
			}
		}
		
				
		
		for(Arco arcoSuccessivo: successivi) {
			Corsa nuovaCorsa=corsaAttuale;
			int nuovoCambiAutobus=cambiAutobus;
			boolean okay;
			if(!parziale.contains(grafo.getEdgeTarget(arcoSuccessivo))) {
					okay = true;
					LocalTime orarioAttuale=arcoSuccessivo.getOrarioPartenza().plusMinutes((long) grafo.getEdgeWeight(arcoSuccessivo));
					if(Duration.between(this.orarioPartenza, orarioAttuale).toMinutes()>this.tempoMinimo) {
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
						espandi(parziale,parzialeArchi, codSuccessivo,this.nuoviArchi(codSuccessivo,orarioAttuale),nuovaCorsa,nuovoCambiAutobus,arrivo);
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
	private Set<Arco> nuoviArchi(int partenza, LocalTime orarioAttuale) {
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

	public DirectedWeightedMultigraph<Integer, Arco> getGrafo() {
		return grafo;
	}

	public LocalTime getMiglioreOrarioArrivo() {
		return miglioreOrarioArrivo;
	}
	
	
	
		
	}
