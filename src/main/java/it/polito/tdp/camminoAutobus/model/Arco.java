package it.polito.tdp.camminoAutobus.model;

import java.time.LocalTime;

import org.jgrapht.graph.DefaultWeightedEdge;

public class Arco extends DefaultWeightedEdge {
	
	private LocalTime orarioPartenza;
	private Corsa corsa;

	public Arco(Corsa corsa, LocalTime orarioPartenza) {
		super();
		this.corsa = corsa;
		this.orarioPartenza=orarioPartenza;
	}
	
	

	public LocalTime getOrarioPartenza() {
		return orarioPartenza;
	}



	public void setOrarioPartenza(LocalTime orarioPartenza) {
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
		return "Arco [corsa=" + corsa + ", weight=" + this.getWeight() + ", source=" + this.getSource() + ", target="
				+ this.getTarget() + "]";
	}
	
	
	
	
	
	
	

}
