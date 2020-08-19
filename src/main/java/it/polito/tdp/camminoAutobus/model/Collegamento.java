package it.polito.tdp.camminoAutobus.model;

public class Collegamento {
	
	private String stazione;
	private int codiceLocale;
	
	
	
	public Collegamento(String stazione, int codiceLocale) {
		super();
		this.stazione = stazione;
		this.codiceLocale = codiceLocale;
	}
	public String getStazione() {
		return stazione;
	}
	public void setStazione(String stazione) {
		this.stazione = stazione;
	}
	public int getCodiceLocale() {
		return codiceLocale;
	}
	public void setCodiceLocale(int codiceLocale) {
		this.codiceLocale = codiceLocale;
	}
	@Override
	public String toString() {
		return codiceLocale+" ("+stazione+")";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + codiceLocale;
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
		Collegamento other = (Collegamento) obj;
		if (codiceLocale != other.codiceLocale)
			return false;
		return true;
	}
	
	
	
	
	

}
