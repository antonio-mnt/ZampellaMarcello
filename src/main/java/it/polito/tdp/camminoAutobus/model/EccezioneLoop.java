package it.polito.tdp.camminoAutobus.model;

@SuppressWarnings("serial")
public class EccezioneLoop extends Exception {
	public EccezioneLoop(FermataAutobus fermataPartenza) {
		System.err.println("ATTENZIONE: E' PRESENTE UN LOOP NON CONSENTITO ALL'INTERNO DEL GRAFO ("+fermataPartenza+")\n MODIFICARE IL DATABASE!");
	}
}
