package it.polito.tdp.camminoAutobus.model;

import java.time.LocalTime;

public class FermataAutobus {
	
	private String identificativo;
	private int numeroFermata;
	private Collegamento collegamento;
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
	public Collegamento getCollegamento() {
		return collegamento;
	}
	public void setCollegamento(Collegamento collegamento) {
		this.collegamento = collegamento;
	}

	public LocalTime getOrario() {
		return orario;
	}
	public void setOrario(LocalTime orario) {
		this.orario = orario;
	}
	public FermataAutobus(String identificativo, int numeroFermata, Collegamento collegamento,
			LocalTime orario) {
		super();
		this.identificativo = identificativo;
		this.numeroFermata = numeroFermata;
		this.collegamento = collegamento;
		this.orario = orario;
	}
	@Override
	public String toString() {
		return "FermataAutobus [identificativo=" + identificativo + ", numeroFermata=" + numeroFermata
				+ ", collegamento=" + collegamento+ ", orario=" + orario + "]";
	}
	
	
	

}
