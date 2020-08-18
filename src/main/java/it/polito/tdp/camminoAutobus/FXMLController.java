package it.polito.tdp.camminoAutobus;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jgrapht.graph.DirectedWeightedMultigraph;

import it.polito.tdp.camminoAutobus.model.Arco;
import it.polito.tdp.camminoAutobus.model.Model;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FXMLController {

    @FXML
    private TextField txtPartenza;

    @FXML
    private ComboBox<Integer> cmbPartenza;

    @FXML
    private TextField txtArrivo;

    @FXML
    private ComboBox<Integer> cmbArrivo;

    @FXML
    private Button btnPercorso;
    
    @FXML
    private TextField txtOrario;

    @FXML
    private RadioButton radioPartenza;

    @FXML
    private ToggleGroup orario;

    @FXML
    private RadioButton radioArrivo;
    
    @FXML
    private TextArea txtResult;
    
    @FXML
    private TextField txtNumeroMassimo;
    
    @FXML
    private ToggleGroup stagione;

    @FXML
    private ToggleGroup settimana;
    

    @FXML
    private VBox VboxCodici;
    

    @FXML
    private ProgressBar progressBar;
    
    private DirectedWeightedMultigraph<Integer, Arco> grafo;
    private String strOrario;
    private String sceltaOrario;
    private String sceltaStagione;

	private LocalTime ltorario;
    
    @FXML
    void doCreaGrafo(ActionEvent event) {
    	try {
    		DateTimeFormatter strictTimeFormatter = DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT);
            LocalTime.parse(this.txtOrario.getText(), strictTimeFormatter);
    	    } catch (DateTimeParseException | NullPointerException e) {
        		this.txtResult.setStyle("-fx-text-inner-color: red;");
    	        this.txtResult.setText("L'ORARIO INSERITO NON E' CORRETTO. IL FORMATO GIUSTO E' HH:MM");
    	        return ;
    	    }
    	this.progressBar.setProgress(0.5);
    	this.txtResult.clear();
		this.txtResult.setStyle("-fx-text-inner-color: black;");
    	this.txtResult.setText("Creazione grafo...\n");
    	RadioButton radioOrario=(RadioButton) orario.getSelectedToggle();
    	strOrario=this.txtOrario.getText();
    	sceltaOrario=radioOrario.getText();
    	RadioButton radioStagione=(RadioButton) stagione.getSelectedToggle();
    	sceltaStagione=radioStagione.getText();
    	RadioButton radioSettimana=(RadioButton) settimana.getSelectedToggle();
    	String sceltaSettimana=radioSettimana.getText();
    	String scelta="orari_"+sceltaStagione.toLowerCase()+"_"+sceltaSettimana.toLowerCase();
		this.ltorario=LocalTime.of(Integer.parseInt(strOrario.substring(0, 2)), Integer.parseInt(strOrario.substring(3)));
    	this.grafo=this.model.creaGrafo(ltorario, sceltaOrario,scelta);
    	this.txtResult.appendText("grafo creato!\n# NODI: "+grafo.vertexSet().size()+"\n# ARCHI: "+grafo.edgeSet().size()+"\n");
		this.btnPercorso.setDisable(false);
		this.VboxCodici.setDisable(false);
		this.txtNumeroMassimo.setDisable(false);
		this.cmbPartenza.getItems().clear();
		this.cmbArrivo.getItems().clear();
		List<Integer> fermate=this.model.listAllFermate();
		this.cmbPartenza.getItems().addAll(fermate);
		this.cmbArrivo.getItems().addAll(fermate);

    }



    @FXML
    void doCalcolaPercorso(ActionEvent event) {
		
    	if(!this.checkInput()) {
    		return;
    	}
    	int arrivo=this.cmbArrivo.getValue();
    	int partenza=this.cmbPartenza.getValue();
		this.txtResult.setStyle("-fx-text-inner-color: black;");
    	this.txtResult.setText("Inizio ricerca Percorso... \n");
		ArrayList<Arco> sequenza=this.model.cercaPercorso(partenza,arrivo, ltorario, sceltaOrario,Integer.parseInt(this.txtNumeroMassimo.getText()));
    	if(sequenza==null || sequenza.size()==0) {
    		this.txtResult.appendText("ATTENZIONE: non e' stato trovato alcun percorso possibile. Prova ad aumentare il numero di cambi possibili"); 
    		return;
    	}
    	if(sceltaOrario.equals("ARRIVO")) {
    		Collections.reverse(sequenza);
    	}
    	Arco arcoPartenza=sequenza.get(0);
    	this.txtResult.appendText("Prendi il bus "+arcoPartenza.getCorsa().getIdentificativo()+" che parte alle ore "+arcoPartenza.getOrarioPartenza().toLocalTime()+"\n");
    	for(int k=0;k+1<sequenza.size();k++) {
    		Arco arco=sequenza.get(k);
    		String nuovo=sequenza.get(k+1).getCorsa().getIdentificativo();
    		if(!nuovo.equals(arco.getCorsa().getIdentificativo())) {
    			this.txtResult.appendText("Alle ore "+arco.getOrarioPartenza().plusMinutes((long) grafo.getEdgeWeight(arco)).toLocalTime()
    					+" alla fermata "+grafo.getEdgeTarget(arco)+" scendi dall'autobus e aspetta il bus "+nuovo+" che arriva alle ore "+sequenza.get(k+1).getOrarioPartenza().toLocalTime()+"\n");
    		}
    	}
    	Arco arcofine=sequenza.get(sequenza.size()-1);
    	LocalTime oraFine=arcofine.getOrarioPartenza().plusMinutes((long) grafo.getEdgeWeight(arcofine)).toLocalTime();
    	this.txtResult.appendText("Arrivo previsto alle ore "+oraFine+"\n");
    	if(arcoPartenza.getOrarioPartenza().toLocalTime().isAfter(oraFine)) {
    		this.txtResult.appendText("ATTENZIONE: il viaggio si svolge in 2 giorni differenti\n");
    	}
    }
    
    

    private boolean checkInput() {
    	if(this.cmbArrivo.getValue()==null) {
    		this.txtResult.setStyle("-fx-text-fill: red;");
    		this.txtResult.setText("SCEGLI UN VALORE DAL BOX ARRIVO!");
    		return false;
    	}
    	if(this.cmbPartenza.getValue()==null) {
    		this.txtResult.setStyle("-fx-text-inner-color: red;");
    		this.txtResult.setText("SCEGLI UN VALORE DAL BOX PARTENZA!");
    		return false;
    	}
    	
    	
    	stemp=this.txtNumeroMassimo.getText();
    	if(stemp==null) {
    		this.txtResult.setStyle("-fx-text-inner-color: red;");
    		this.txtResult.setText("INSERISCI IL NUMERO MASSIMO DI AUTOBUS!");
    		return false;
    	}
    	if(!this.isInteger(stemp)) {
    		this.txtResult.setStyle("-fx-text-inner-color: red;");
    		this.txtResult.setText("IL VALORE MASSIMO DI AUTOBUS DEVE ESSERE NUMERICO INTERO!");
    		return false;
    	}
    	int numeroMassimo=Integer.parseInt(stemp);
    	if(numeroMassimo==0) {
    		this.txtResult.setStyle("-fx-text-inner-color: red;");
    		this.txtResult.setText("IL NUMERO MASSIMO DI AUTOBUS DEVE ESSERE MAGGIORE DI 0!");
    		return false;
    	}
		return true;
	}
    
    String stemp;

    
    
    @FXML
    void doRicercaCodiciLocali(ActionEvent event) throws IOException {

    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Scene2.fxml"));
		BorderPane root = loader.load();
		CodiciLocaliController controller = loader.getController();
		controller.setModel(model,this.stage,this.oldScene,this.cmbPartenza,this.cmbArrivo);
		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("/styles/Styles.css").toExternalForm());
		stage.setScene(scene);
		stage.show();
    
    }
    
    
	private Model model;

	private Stage stage;

	private Scene oldScene;

	public void setModel(Model model, Stage stage, Scene scene) {
		this.stage=stage;
		this.oldScene=scene;
		this.model=model;
		this.btnPercorso.setDisable(true);
		this.VboxCodici.setDisable(true);
		this.txtNumeroMassimo.setDisable(true);


	}
	
    public static boolean isInteger(String str) { 
	  	  try {  
	  	    Integer.parseInt(str);  
	  	    return true;
	  	  } catch(NumberFormatException e){  
	  	    return false;  
	  	  }  
	  	}

}
