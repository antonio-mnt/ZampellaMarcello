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
		LocalDate giorno=LocalDate.ofYearDay(1998, 1);
		LocalTime ora=LocalTime.of(23, 12);
		LocalDateTime primo=LocalDateTime.of(giorno, ora);
		LocalDateTime secondo=LocalDateTime.of(giorno, ora);
		System.out.println(primo.equals(secondo));
	}
	
	
}
