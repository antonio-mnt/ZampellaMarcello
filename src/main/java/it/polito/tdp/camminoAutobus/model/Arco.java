package it.polito.tdp.camminoAutobus.model;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.jgrapht.graph.DefaultWeightedEdge;

public class Arco extends DefaultWeightedEdge {
	
	private LocalDateTime orarioPartenza;
	private Corsa corsa;

	public Arco(Corsa corsa, LocalDateTime inserireTemp) {
		super();
		this.corsa = corsa;
		this.orarioPartenza=inserireTemp;
	}
	
	

	public LocalDateTime getOrarioPartenza() {
		return orarioPartenza;
	}



	public void setOrarioPartenza(LocalDateTime orarioPartenza) {
		this.orarioPartenza = orarioPartenza;
	}


	

	public Corsa getCorsa() {
		return corsa;
	}



	public void setCorsa(Corsa corsa) {
		this.corsa = corsa;
	}



	@Override
	protected Object getSource() {
		// TODO Auto-generated method stub
		return super.getSource();
	}

	@Override
	protected Object getTarget() {
		// TODO Auto-generated method stub
		return super.getTarget();
	}

	@Override
	protected double getWeight() {
		// TODO Auto-generated method stub
		return super.getWeight();
	}



	@Override
	public String toString() {
		return orarioPartenza + ", corsa=" + corsa + "]";
	}
	
	

}
