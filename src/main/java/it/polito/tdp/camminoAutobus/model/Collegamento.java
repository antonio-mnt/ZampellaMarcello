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
		return stazione;
	}
	
	
	

}
