package it.polito.tdp.camminoAutobus.model;

import org.jgrapht.graph.DefaultWeightedEdge;

public class Arco extends DefaultWeightedEdge {
	
	private int identificativo;

	public Arco(int identificativo) {
		super();
		this.identificativo = identificativo;
	}

	public int getIdentificativo() {
		return identificativo;
	}

	public void setIdentificativo(int identificativo) {
		this.identificativo = identificativo;
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
		return "Arco [identificativo=" + identificativo + ", weight=" + this.getWeight() + ", source=" + this.getSource() + ", target="
				+ this.getTarget() + "]";
	}
	
	
	
	
	
	
	

}
