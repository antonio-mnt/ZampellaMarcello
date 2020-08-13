package it.polito.tdp.camminoAutobus.model;

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
	List<String> identificativi;
	
	public Model() {
		dao=new CorsaDao();
	}

	public List<Integer> listAllFermate() {
		codiciLocali=dao.listAllCodiceLocale();
		return codiciLocali;
		
	}

	/**
	 * 
	 * @param partenza Codice locale di partenza
	 * @param arrivo codice locale di arrivo
	 * @param orario orario di interesse
	 * @param tipo indica partenza o arrivo all'orario indicato
	 */
	public void creaGrafo(int partenza, int arrivo, String orario, String tipo) {
		grafo=new DirectedWeightedMultigraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		Graphs.addAllVertices(grafo, codiciLocali);
		identificativi=dao.listAllIdentificativi();
		for(String temp: identificativi) {
			
		}
	}

	public List<Collegamento> cercaCodice(String key) {
		return dao.getCodiciLocaliByStazione(key);
		
		
	}


}
