package it.polito.tdp.camminoAutobus.model;

public class Corsa {
	
	private int id;
	private String linea;
	private String identificativo;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getLinea() {
		return linea;
	}
	public void setLinea(String linea) {
		this.linea = linea;
	}
	public String getIdentificativo() {
		return identificativo;
	}
	public void setIdentificativo(String identificativo) {
		this.identificativo = identificativo;
	}
	public Corsa(int id, String linea, String identificativo) {
		super();
		this.id = id;
		this.linea = linea;
		this.identificativo = identificativo;
	}
	@Override
	public String toString() {
		return "Corsa [id=" + id + ", linea=" + linea + ", identificativo=" + identificativo + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Corsa other = (Corsa) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	
	

}
