package it.polito.tdp.camminoAutobus;

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
import it.polito.tdp.camminoAutobus.model.Model;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
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
    private ProgressBar progressBar;
    

    @FXML
    private ToggleGroup ricerca;
    

    @FXML
    private HBox HBoxAutobus;
    
    
    @FXML
    private Button btnDettagli;
    

    @FXML
    private Button btnSalva;
    
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
    
    @FXML
    void doCreaGrafo(ActionEvent event) {
    	this.btnDettagli.setDisable(true);
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
		this.VboxRicorsione.setDisable(false);
		this.txtNumeroMassimo.setDisable(false);
		this.cmbPartenza.getItems().clear();
		this.cmbArrivo.getItems().clear();
		List<Collegamento> collegamenti=this.model.listAllCollegamenti();
		this.cmbPartenza.getItems().addAll(collegamenti);
		this.cmbArrivo.getItems().addAll(collegamenti);
		

    }



    @FXML
    void doCalcolaPercorso(ActionEvent event) {
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
    		this.txtResult.appendText("ATTENZIONE: non e' stato trovato alcun percorso possibile. Prova ad aumentare il numero di cambi possibili"); 
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
    	this.txtResult.appendText("Prendi il bus "+arcoPartenza.getCorsa().getIdentificativo()+" che parte alle ore "+this.oraPartenza.toLocalTime()+"\n");
    	for(int k=0;k+1<sequenza.size();k++) {
    		Arco arco=sequenza.get(k);
    		Corsa nuovaCorsa=sequenza.get(k+1).getCorsa();
    		if(!nuovaCorsa.equals(arco.getCorsa())) {
    			this.txtResult.appendText("Alle ore "+arco.getOrarioPartenza().plusMinutes((long) grafo.getEdgeWeight(arco)).toLocalTime()
    					+" alla fermata "+grafo.getEdgeTarget(arco)+" scendi dall'autobus e aspetta il bus "+nuovaCorsa+" che arriva alle ore "+sequenza.get(k+1).getOrarioPartenza().toLocalTime()+"\n");
    		}
    	}
    	this.arcoFine=sequenza.get(sequenza.size()-1);
    	this.oraFine=arcoFine.getOrarioPartenza().plusMinutes((long) grafo.getEdgeWeight(arcoFine));
    	this.txtResult.appendText("Arrivo previsto alle ore "+this.oraFine.toLocalTime()+"\n");
    	if(oraPartenza.toLocalTime().isAfter(oraFine.toLocalTime())) {
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
	    	if(!this.isInteger(stemp)) {
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
    
    private String stemp;

	private int indexBoxAutobus;

	private ObservableList<Node> lista;

    
    
    @FXML
    void doRicercaCodiciLocali(ActionEvent event) throws IOException {

    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Scene2.fxml"));
		BorderPane root = loader.load();
		CodiciLocaliController controller = loader.getController();
		Stage stageNuovo=new Stage();
		controller.setModel(model,stageNuovo,this.oldScene,this.cmbPartenza,this.cmbArrivo);
		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("/styles/Styles.css").toExternalForm());
		stageNuovo.setScene(scene);
		stageNuovo.show();
		//stage.setScene(scene);
		//stage.show();
    
    }
    

    @FXML
    void doDettagliPercorso(ActionEvent event) {
    	this.txtResult.appendText("*** DETTAGLI PERCORSO ***\n");
    	Corsa corsa=this.arcoPartenza.getCorsa();
    	this.txtResult.appendText("++ Corsa iniziale: "+corsa+"\n");
    	String s;
    	Arco arcoPrecedente = null;
    	int contatoreCambi=1;
    	for(Arco arco: this.sequenza) {
    		if(!arco.getCorsa().equals(corsa)) {
    			contatoreCambi++;
    			s=String.format("|%-6s|", arcoPrecedente.getOrarioPartenza().plusMinutes((long) grafo.getEdgeWeight(arcoPrecedente)).toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")).toString());
        		this.txtResult.appendText(s+" "+this.grafo.getEdgeTarget(arcoPrecedente).toString()+"\n");
    			corsa=arco.getCorsa();
    			this.txtResult.appendText("++ Cambio Corsa "+corsa+"\n");
    		}
    		s=String.format("|%-6s|", arco.getOrarioPartenza().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")).toString());
    		this.txtResult.appendText(s+" "+this.grafo.getEdgeSource(arco).toString()+"\n");
    		arcoPrecedente=arco;
    	}
		s=String.format("|%-6s|", this.oraFine.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")).toString());
		this.txtResult.appendText(s+" "+this.grafo.getEdgeTarget(arcoFine).toString()+"\n");
		this.txtResult.appendText("++++++++++++\n INFORMAZIONI SUL VIAGGIO:\nN° Autobus diversi: "+contatoreCambi+"\n");
		int minutiTotali;
		int hours;
		int minutes;
		if(this.sceltaOrario.equals("PARTENZA")) {
			this.txtResult.appendText("Tempo da orario indicato: ");
			minutiTotali=(int) Duration.between(LocalDateTime.of(LocalDate.ofYearDay(1998, 1),this.ltorario), this.oraFine).toMinutes();
			hours = minutiTotali / 60; 
			minutes = minutiTotali % 60;
			if(hours==0)
				this.txtResult.appendText(minutes+" minuti\n");
			else
				this.txtResult.appendText(hours+" ore e "+minutes+" minuti\n");
		}
		this.txtResult.appendText("Tempo di viaggio effettivo: ");
		minutiTotali=(int) Duration.between(this.oraPartenza, this.oraFine).toMinutes();
		hours = minutiTotali / 60; //since both are ints, you get an int
		minutes = minutiTotali % 60;
		if(hours==0)
			this.txtResult.appendText(minutes+" minuti\n");
		else
			this.txtResult.appendText(hours+" ore e "+minutes+" minuti\n");
		
		this.btnSalva.setDisable(false);
		
		/*
		try {
		      FileWriter myWriter = new FileWriter("C:\\Users\\Utente\\Desktop\\filename.txt");
		      myWriter.write(this.txtResult.getText());
		      myWriter.close();
		      System.out.println("Successfully wrote to the file.");
		    } catch (IOException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }
		    */
    	
    }
    

    @FXML
    void doSalva(ActionEvent event) {
    	JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        jfc.setDialogTitle("Wybierz folder do konwersji: ");
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jfc.setAcceptAllFileFilterUsed(false);
        int returnValue = jfc.showSaveDialog(null);
        if (returnValue != JFileChooser.APPROVE_OPTION) {
            //if (!jfc.getSelectedFile().isDirectory())
                return;
        }
		int a=this.txtResult.getText().indexOf("\n")+1;
		try {
		      FileWriter myWriter = new FileWriter(jfc.getSelectedFile()+"\\filename.txt");
		      myWriter.write(this.txtResult.getText().substring(a));
		      myWriter.close();
		    } catch (IOException e) {
		      e.printStackTrace();
		    }
    }
    
    @FXML
    void doEliminaAutobus(ActionEvent event) {
    	this.lista=this.VboxRicorsione.getChildren();
    	this.indexBoxAutobus=lista.indexOf(this.HBoxAutobus);
    	lista.remove(this.indexBoxAutobus);
    }
    
    @FXML
    void doAggiungiAutobus(ActionEvent event) {
    	this.lista.add(this.indexBoxAutobus,this.HBoxAutobus);
    }
    
	private Model model;

	private Stage stage;

	private Scene oldScene;

	public void setModel(Model model, Stage stage, Scene scene) {
		this.stage=stage;
		
		this.oldScene=scene;
		this.model=model;
		this.btnPercorso.setDisable(true);
		this.VboxRicorsione.setDisable(true);
		this.txtNumeroMassimo.setDisable(true);
		this.btnDettagli.setDisable(true);
		this.btnSalva.setDisable(true);
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
