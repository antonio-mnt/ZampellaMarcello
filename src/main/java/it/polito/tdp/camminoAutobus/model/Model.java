package it.polito.tdp.camminoAutobus.model;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import it.polito.tdp.camminoAutobus.db.CorsaDao;

public class Model {
	
	CorsaDao dao;
	List<Integer> codiciLocali;
	DirectedWeightedMultigraph<Integer, DefaultWeightedEdge> grafo;
	List<Integer> identificativi;
	
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
	 * @param partenza Codice locale di partenza
	 * @param arrivo codice locale di arrivo
	 * @param orario orario di interesse
	 * @param tipo indica partenza o arrivo all'orario indicato
	 */
	public void creaGrafo(int partenza, int arrivo, String orario, String tipo) {
		grafo=new DirectedWeightedMultigraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		Graphs.addAllVertices(grafo, codiciLocali);
		identificativi=dao.listAllIdCorsa();
		if(tipo.equals("PARTENZA")) {
		for(int identificativo: identificativi) {
			List<FermataAutobus>fermate=dao.getAllFermateByIdentificativo(identificativo,orario);
			for(int k=0;k+1<fermate.size() && fermate.size()>1;k++) {
				
				FermataAutobus fermataPartenza=fermate.get(k);
				FermataAutobus fermataArrivo=fermate.get(k+1);
				Arco arco=new Arco(identificativo);
				grafo.addEdge(fermataPartenza.getCodiceLocale(), fermataArrivo.getCodiceLocale(), arco);
				grafo.setEdgeWeight(arco, Duration.between(fermataPartenza.getOrario(),fermataArrivo.getOrario()).toMinutes());
			}
		}
		} else if(tipo.equals("ARRIVO")) {
			
		} else {
			System.out.println("ERRORE!");
		}
	}

	public List<Collegamento> cercaCodice(String key) {
		return dao.getCodiciLocaliByStazione(key);
		
		
	}


}
