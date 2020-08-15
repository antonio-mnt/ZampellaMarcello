package it.polito.tdp.camminoAutobus.model;

import java.time.LocalTime;

public class FermataAutobus {
	
	private String identificativo;
	private int numeroFermata;
	private int codiceLocale;
	private String Desc_Stazione;
	private LocalTime orario;
	public String getIdentificativo() {
		return identificativo;
	}
	public void setIdentificativo(String identificativo) {
		this.identificativo = identificativo;
	}
	public int getNumeroFermata() {
		return numeroFermata;
	}
	public void setNumeroFermata(int numeroFermata) {
		this.numeroFermata = numeroFermata;
	}
	public int getCodiceLocale() {
		return codiceLocale;
	}
	public void setCodiceLocale(int codiceLocale) {
		this.codiceLocale = codiceLocale;
	}
	public String getDesc_Stazione() {
		return Desc_Stazione;
	}
	public void setDesc_Stazione(String desc_Stazione) {
		Desc_Stazione = desc_Stazione;
	}
	public LocalTime getOrario() {
		return orario;
	}
	public void setOrario(LocalTime orario) {
		this.orario = orario;
	}
	public FermataAutobus(String identificativo, int numeroFermata, int codiceLocale, String desc_Stazione,
			LocalTime orario) {
		super();
		this.identificativo = identificativo;
		this.numeroFermata = numeroFermata;
		this.codiceLocale = codiceLocale;
		Desc_Stazione = desc_Stazione;
		this.orario = orario;
	}
	@Override
	public String toString() {
		return "FermataAutobus [identificativo=" + identificativo + ", numeroFermata=" + numeroFermata
				+ ", codiceLocale=" + codiceLocale + ", Desc_Stazione=" + Desc_Stazione + ", orario=" + orario + "]";
	}
	
	
	

}
