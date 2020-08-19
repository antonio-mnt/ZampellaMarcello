package it.polito.tdp.camminoAutobus.model;

import java.time.LocalTime;

public class FermataAutobus {
	
	private Corsa corsa;
	private int numeroFermata;
	private Collegamento collegamento;
	private LocalTime orario;
	public Corsa getCorsa() {
		return corsa;
	}
	public void setCorsa(Corsa corsa) {
		this.corsa = corsa;
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
	public FermataAutobus(Corsa corsa, int numeroFermata, Collegamento collegamento,
			LocalTime orario) {
		super();
		this.corsa = corsa;
		this.numeroFermata = numeroFermata;
		this.collegamento = collegamento;
		this.orario = orario;
	}
	@Override
	public String toString() {
		return "FermataAutobus [corsa=" + corsa + ", numeroFermata=" + numeroFermata
				+ ", collegamento=" + collegamento+ ", orario=" + orario + "]";
	}
	
	
	

}
