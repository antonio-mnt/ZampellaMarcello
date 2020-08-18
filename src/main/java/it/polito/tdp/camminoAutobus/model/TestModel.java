package it.polito.tdp.camminoAutobus.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
		
		LocalDateTime orarioPartenza=LocalDateTime.of(LocalDate.ofYearDay(1998, 1), LocalTime.of(23, 30));
		LocalDateTime orarioArrivo=LocalDateTime.of(LocalDate.ofYearDay(1998, 2), LocalTime.of(2, 50));
		System.out.println(Duration.between(orarioPartenza, orarioArrivo).toMinutes());
		LocalDate aa=LocalDate.ofYearDay(1998, 1);
		aa=aa.plusDays(1);
		System.out.println(aa);

		
		


	}

}
