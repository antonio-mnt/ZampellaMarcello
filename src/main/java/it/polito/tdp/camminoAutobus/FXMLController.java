package it.polito.tdp.camminoAutobus;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import org.jgrapht.graph.DirectedWeightedMultigraph;

import it.polito.tdp.camminoAutobus.model.Arco;
import it.polito.tdp.camminoAutobus.model.Collegamento;
import it.polito.tdp.camminoAutobus.model.Corsa;
import it.polito.tdp.camminoAutobus.model.EccezioneLoop;
import it.polito.tdp.camminoAutobus.model.Model;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FXMLController {
	

    @FXML
    private VBox VboxRicorsione;

    @FXML
    private TextField txtPartenza;

    @FXML
    private ComboBox<Collegamento> cmbPartenza;

    @FXML
    private TextField txtArrivo;

    @FXML
    private ComboBox<Collegamento> cmbArrivo;

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
    private ToggleGroup ricerca;
    

    @FXML
    private HBox HBoxAutobus;
    
    
    @FXML
    private Button btnDettagli;
    

    @FXML
    private HBox HBoxSalva;

    @FXML
    private TextField txtSalva;

    @FXML
    private Button btnSalva;
    

    @FXML
    private Label erroreSalva;
    
    
    private DirectedWeightedMultigraph<Collegamento, Arco> grafo;
    private String strOrario;
    private String sceltaOrario;
    private String sceltaStagione;
	private LocalTime ltorario;
	private ArrayList<Arco> sequenza;
	private LocalDateTime oraFine;
	private Arco arcoFine;
	private Arco arcoPartenza;
	private String sceltaRicerca;
	private int numeroMassimo;
	private LocalDateTime oraPartenza;
	private String sceltaSettimana;
	private boolean stampato;
    private String stemp;
	private int indexBoxAutobus;
	private ObservableList<Node> lista;
	private Model model;
	private Stage stage;
	private Scene oldScene;
	private DateTimeFormatter dtf;
	
    @FXML
    void doInserisciOrarioAttuale(ActionEvent event) {
    	this.txtOrario.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
    }
    
    
    @FXML
    void doCreaGrafo(ActionEvent event) throws EccezioneLoop {
    	this.btnDettagli.setDisable(true);
    	try {
    		DateTimeFormatter strictTimeFormatter = dtf.withResolverStyle(ResolverStyle.STRICT);
            LocalTime.parse(this.txtOrario.getText(), strictTimeFormatter);
    	    } catch (DateTimeParseException | NullPointerException e) {
        		this.txtResult.setStyle("-fx-text-inner-color: red;");
    	        this.txtResult.setText("L'ORARIO INSERITO NON E' CORRETTO. IL FORMATO GIUSTO E' HH:MM");
    	        return ;
    	    }
		this.txtResult.setStyle("-fx-text-inner-color: black;");
    	this.txtResult.setText("Creazione grafo...\n");
    	RadioButton radioOrario=(RadioButton) orario.getSelectedToggle();
    	strOrario=this.txtOrario.getText();
    	sceltaOrario=radioOrario.getText();
    	RadioButton radioStagione=(RadioButton) stagione.getSelectedToggle();
    	sceltaStagione=radioStagione.getText();
    	RadioButton radioSettimana=(RadioButton) settimana.getSelectedToggle();
    	this.sceltaSettimana=radioSettimana.getText();
    	String scelta="orari_"+sceltaStagione.toLowerCase()+"_"+sceltaSettimana.toLowerCase();
		this.ltorario=LocalTime.of(Integer.parseInt(strOrario.substring(0, 2)), Integer.parseInt(strOrario.substring(3)));
    	this.grafo=this.model.creaGrafo(ltorario, sceltaOrario,scelta);
    	if(this.cmbArrivo.getItems().isEmpty()) {
			List<Collegamento> collegamenti=this.model.getCollegamenti();
			this.cmbPartenza.getItems().addAll(collegamenti);
			this.cmbArrivo.getItems().addAll(collegamenti);
    	}
    	this.txtResult.appendText("grafo creato!\n# NODI: "+grafo.vertexSet().size()+"\n# ARCHI: "+grafo.edgeSet().size()+"\n");
		this.btnPercorso.setDisable(false);
		this.VboxRicorsione.setDisable(false);
		this.txtNumeroMassimo.setDisable(false);
		this.HBoxSalva.setDisable(true);
		this.erroreSalva.setVisible(false);
    }



    @FXML
    void doCalcolaPercorso(ActionEvent event) {
    	if(controlloCambioInput()) {
    		return;
    	}
    	this.HBoxSalva.setDisable(true);
    	RadioButton radioRicerca=(RadioButton) this.ricerca.getSelectedToggle();
    	this.sceltaRicerca = radioRicerca.getText();
    	if(!this.checkInput()) {
    		return;
    	}
    	Collegamento arrivo=this.cmbArrivo.getValue();
    	Collegamento partenza=this.cmbPartenza.getValue();
		this.txtResult.setStyle("-fx-text-inner-color: black;");
    	this.txtResult.setText("Inizio ricerca Percorso... \n");
		this.sequenza=this.model.cercaPercorso(partenza,arrivo, ltorario, sceltaOrario,this.numeroMassimo,this.sceltaRicerca);
    	if(sequenza==null || sequenza.size()==0) {
    		this.txtResult.appendText("ATTENZIONE: non e' stato trovato alcun percorso possibile."); 
    		if(sceltaRicerca.equals("TEMPO MINIMO"))
    			this.txtResult.appendText(" Prova ad aumentare il numero di cambi possibili");
    		this.btnDettagli.setDisable(true);
    		return;
    	} else {
    		this.btnDettagli.setDisable(false);
    	}
    	if(sceltaOrario.equals("ARRIVO")) {
    		Collections.reverse(sequenza);
    	}
    	this.arcoPartenza=sequenza.get(0);
    	this.oraPartenza=arcoPartenza.getOrarioPartenza();
    	this.txtResult.appendText("Prendi il bus "+arcoPartenza.getCorsa()+" che parte alle ore "+this.oraPartenza.toLocalTime().format(dtf)+"\n");
    	for(int k=0;k+1<sequenza.size();k++) {
    		Arco arco=sequenza.get(k);
    		Corsa nuovaCorsa=sequenza.get(k+1).getCorsa();
    		if(!nuovaCorsa.equals(arco.getCorsa())) {
    			this.txtResult.appendText("Alle ore "+arco.getOrarioPartenza().plusMinutes((long) grafo.getEdgeWeight(arco)).toLocalTime().format(dtf)
    					+" alla fermata "+grafo.getEdgeTarget(arco)+" scendi dall'autobus e aspetta il bus "+nuovaCorsa+" che arriva alle ore "+sequenza.get(k+1).getOrarioPartenza().toLocalTime().format(dtf)+"\n");
    		}
    	}
    	this.arcoFine=sequenza.get(sequenza.size()-1);
    	this.oraFine=arcoFine.getOrarioPartenza().plusMinutes((long) grafo.getEdgeWeight(arcoFine));
    	this.txtResult.appendText("Arrivo previsto alle ore "+this.oraFine.toLocalTime().format(dtf)+"\n");
    	if(oraPartenza.toLocalTime().isAfter(oraFine.toLocalTime())) {
    		this.txtResult.appendText("ATTENZIONE: il viaggio si svolge in 2 giorni differenti\n");
    	}
    	this.stampato=false;
    	this.erroreSalva.setVisible(false);
    }
    
    

    private boolean controlloCambioInput() {
		if(!this.strOrario.equals(this.txtOrario.getText())) {
			this.txtResult.setStyle("-fx-text-fill: red;");
    		this.txtResult.setText("HAI CAMBIATO L'ORARIO, PRIMA DI CALCOLARE IL PERCORSO CREA NUOVAMENTE IL GRAFO!");
			return true;
		}
		RadioButton radioTemp=(RadioButton) this.orario.getSelectedToggle();
		if(!this.sceltaOrario.equals(radioTemp.getText())) {
			this.txtResult.setStyle("-fx-text-fill: red;");
    		this.txtResult.setText("HAI CAMBIATO IL TIPO DI VIAGGIO, PRIMA DI CALCOLARE IL PERCORSO CREA NUOVAMENTE IL GRAFO!");
			return true;
		}
		radioTemp=(RadioButton) this.stagione.getSelectedToggle();
		if(!this.sceltaStagione.equals(radioTemp.getText())) {
			this.txtResult.setStyle("-fx-text-fill: red;");
    		this.txtResult.setText("HAI CAMBIATO LA STAGIONE SCELTA, PRIMA DI CALCOLARE IL PERCORSO CREA NUOVAMENTE IL GRAFO!");
			return true;
		}
		radioTemp=(RadioButton) this.settimana.getSelectedToggle();
		if(!this.sceltaSettimana.equals(radioTemp.getText())) {
			this.txtResult.setStyle("-fx-text-fill: red;");
    		this.txtResult.setText("HAI CAMBIATO IL PERIODO SCELTO, PRIMA DI CALCOLARE IL PERCORSO CREA NUOVAMENTE IL GRAFO!");
			return true;
		}
		return false;
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
    	
    	if(this.cmbArrivo.getValue().equals(this.cmbPartenza.getValue())) {
    		this.txtResult.setStyle("-fx-text-inner-color: red;");
    		this.txtResult.setText("I VALORI DEI BOX DI PARTENZA E ARRIVO NON POSSONO ESSERE UGUALI!");
    		return false;
    	}
    	
    	if(this.sceltaRicerca.equals("TEMPO MINIMO")) {
	    	stemp=this.txtNumeroMassimo.getText();
	    	if(stemp==null) {
	    		this.txtResult.setStyle("-fx-text-inner-color: red;");
	    		this.txtResult.setText("INSERISCI IL NUMERO MASSIMO DI AUTOBUS!");
	    		return false;
	    	}
	    	if(!isInteger(stemp)) {
	    		this.txtResult.setStyle("-fx-text-inner-color: red;");
	    		this.txtResult.setText("IL VALORE MASSIMO DI AUTOBUS DEVE ESSERE NUMERICO INTERO!");
	    		return false;
	    	}
	    	this.numeroMassimo=Integer.parseInt(stemp);
	    	if(numeroMassimo<=0) {
	    		this.txtResult.setStyle("-fx-text-inner-color: red;");
	    		this.txtResult.setText("IL NUMERO MASSIMO DI AUTOBUS DEVE ESSERE MAGGIORE DI 0!");
	    		return false;
	    	}
    	}
		return true;
	}
    
	
    public static boolean isInteger(String str) { 
	  	  try {  
	  	    Integer.parseInt(str);  
	  	    return true;
	  	  } catch(NumberFormatException e){  
	  	    return false;  
	  	  }  
	  	}
    
    
    @FXML
    void doRicercaCodiciLocali(ActionEvent event) throws IOException {

    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Scene2.fxml"));
		BorderPane root = loader.load();
		CodiciLocaliController controller = loader.getController();
		controller.setModel(model,stage,this.oldScene,this.cmbPartenza,this.cmbArrivo);
		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("/styles/Styles.css").toExternalForm());
		stage.setScene(scene);
		stage.show();
    
    }
    

    @FXML
    void doDettagliPercorso(ActionEvent event) {
    	if(stampato) {
    		return;
    	}
    	this.txtResult.appendText("*** DETTAGLI PERCORSO ***\n");
    	Corsa corsa=this.arcoPartenza.getCorsa();
    	this.txtResult.appendText("++ Corsa iniziale: "+corsa+"\n");
    	String s;
    	Arco arcoPrecedente = null;
    	int contatoreCambi=1;
    	for(Arco arco: this.sequenza) {
    		if(!arco.getCorsa().equals(corsa)) {
    			contatoreCambi++;
    			s=String.format("|%-6s|", arcoPrecedente.getOrarioPartenza().plusMinutes((long) grafo.getEdgeWeight(arcoPrecedente)).toLocalTime().format(dtf).toString());
        		this.txtResult.appendText(s+" "+this.grafo.getEdgeTarget(arcoPrecedente).toString()+"\n");
    			corsa=arco.getCorsa();
    			this.txtResult.appendText("++ Cambio Corsa "+corsa+"\n");
    		}
    		s=String.format("|%-6s|", arco.getOrarioPartenza().toLocalTime().format(dtf).toString());
    		this.txtResult.appendText(s+" "+this.grafo.getEdgeSource(arco).toString()+"\n");
    		arcoPrecedente=arco;
    	}
		s=String.format("|%-6s|", this.oraFine.toLocalTime().format(dtf).toString());
		this.txtResult.appendText(s+" "+this.grafo.getEdgeTarget(arcoFine).toString()+"\n");
		this.txtResult.appendText("*******\n INFORMAZIONI SUL VIAGGIO:\nN° Autobus diversi: "+contatoreCambi+"\n");
		int minutiTotali;
		int hours;
		int minutes;
		this.txtResult.appendText("Tempo da orario indicato: ");
		if(this.sceltaOrario.equals("PARTENZA")) {
			minutiTotali=(int) Duration.between(LocalDateTime.of(LocalDate.ofYearDay(1998, 1),this.ltorario), this.oraFine).toMinutes();
		} else {
			minutiTotali=(int) Duration.between(this.oraPartenza,LocalDateTime.of(LocalDate.ofYearDay(1998, 2),this.ltorario)).toMinutes();
		}
			hours = minutiTotali / 60; 
			minutes = minutiTotali % 60;
			if(hours==0)
				this.txtResult.appendText(minutes+" minuti\n");
			else
				this.txtResult.appendText(hours+" ore e "+minutes+" minuti\n");
		
		this.txtResult.appendText("Tempo di viaggio effettivo: ");
		minutiTotali=(int) Duration.between(this.oraPartenza, this.oraFine).toMinutes();
		hours = minutiTotali / 60; //since both are ints, you get an int
		minutes = minutiTotali % 60;
		if(hours==0)
			this.txtResult.appendText(minutes+" minuti\n");
		else
			this.txtResult.appendText(hours+" ore e "+minutes+" minuti\n");
		
		this.HBoxSalva.setDisable(false);
		stampato=true;
    }
    

    @FXML
    void doSalva(ActionEvent event) {
    	this.erroreSalva.setVisible(false);
    	JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        jfc.setDialogTitle("Scegli la cartella in cui salvare il file:");
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jfc.setAcceptAllFileFilterUsed(false);
        int returnValue = jfc.showSaveDialog(null);
        if (returnValue != JFileChooser.APPROVE_OPTION) {
            //if (!jfc.getSelectedFile().isDirectory())
                return;
        }
        String nomeFile=this.txtSalva.getText();
        String percorso=jfc.getSelectedFile()+"\\"+nomeFile+".txt";
        boolean percorsoEsistente=false;
        try {
			new FileReader(percorso);
			percorsoEsistente=true;
		} catch (FileNotFoundException e) {
			
		}
        if(percorsoEsistente) {
        	this.erroreSalva.setVisible(true);
        	return;
        }
		int indexSalvare=this.txtResult.getText().indexOf("\n")+1;
		
		try {
		      FileWriter myWriter = new FileWriter(percorso);
		      myWriter.write("RICERCA PER : "+this.sceltaOrario+" ALLE ORE "+this.ltorario+" (ORARIO "+this.sceltaStagione+"-"+this.sceltaSettimana+") "
		      		+ "DA "+this.cmbPartenza.getValue()+" A "+this.cmbArrivo.getValue()+"\n"+this.txtResult.getText().substring(indexSalvare));
		      myWriter.close();
		    } catch (IOException e) {
		      e.printStackTrace();
		    }
    }
    
    
    @FXML
    void doInvisible(KeyEvent event) {
    	this.erroreSalva.setVisible(false);

    }
    
    @FXML
    void doEliminaAutobus(ActionEvent event) {
    	//nel caso di scelta di ricerca MINIMI CAMBI non mi interessa il numero massimo di autobus da prendere, quindi elimino 
    	//quella parte dall'interfaccia
    	this.lista=this.VboxRicorsione.getChildren();
    	this.indexBoxAutobus=lista.indexOf(this.HBoxAutobus);
    	lista.remove(this.indexBoxAutobus);
    }
    
    @FXML
    void doAggiungiAutobus(ActionEvent event) {
    	this.lista.add(this.indexBoxAutobus,this.HBoxAutobus);
    }

	public void setModel(Model model, Stage stage, Scene scene) {
		this.stage=stage;
		this.oldScene=scene;
		this.model=model;
		this.btnPercorso.setDisable(true);
		this.VboxRicorsione.setDisable(true);
		this.txtNumeroMassimo.setDisable(true);
		this.btnDettagli.setDisable(true);
		this.HBoxSalva.setDisable(true);
    	this.dtf = DateTimeFormatter.ofPattern("HH:mm");

	}

}
