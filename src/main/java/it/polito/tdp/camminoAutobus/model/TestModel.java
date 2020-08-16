package it.polito.tdp.camminoAutobus.model;

import java.time.Duration;
import java.time.LocalTime;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.camminoAutobus.db.CorsaDao;

public class TestModel {

	public static void main(String[] args) {
		/*
		DirectedWeightedMultigraph<Integer, Arco> grafo=new DirectedWeightedMultigraph<Integer, Arco>(Arco.class);
		grafo.addVertex(1);
		grafo.addVertex(2);
		Arco arco=new Arco(1,null);
		grafo.addEdge(1, 2, arco);
		grafo.setEdgeWeight(arco, 5);
		arco=new Arco(1,null);
		grafo.addEdge(1, 2, arco);
		grafo.setEdgeWeight(arco, 10);
		//arco.setIdentificativo("piazza");
		System.out.println(grafo.getAllEdges(1, 2));
		*/
		LocalTime primo=LocalTime.of(23, 00);
		LocalTime secondo=LocalTime.of(23, 00);
    	System.out.println(primo==(secondo));

		
		


	}

}
