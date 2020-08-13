package it.polito.tdp.camminoAutobus;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;

import it.polito.tdp.camminoAutobus.model.Model;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
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
    void doCalcolaPercorso(ActionEvent event) {
    	if(!this.checkInput()) {
    		return;
    	}
    	
    	
    	int arrivo=this.cmbArrivo.getValue();
    	int partenza=this.cmbPartenza.getValue();
    	this.txtResult.clear();
		this.txtResult.setStyle("-fx-text-inner-color: black;");
    	this.txtResult.setText("Calcolo percorso in corso...\n");
    	RadioButton radio=(RadioButton) orario.getSelectedToggle();
    	this.model.creaGrafo(partenza,arrivo,this.txtOrario.getText(),radio.getText());
    	
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
    	try {
    		DateTimeFormatter strictTimeFormatter = DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT);
            LocalTime.parse(this.txtOrario.getText(), strictTimeFormatter);
    	    } catch (DateTimeParseException | NullPointerException e) {
        		this.txtResult.setStyle("-fx-text-inner-color: red;");
    	        this.txtResult.setText("L'ORARIO INSERITO NON E' CORRETTO. IL FORMATO GIUSTO E' HH:MM");
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
		this.cmbPartenza.getItems().clear();
		this.cmbArrivo.getItems().clear();
		List<Integer> fermate=this.model.listAllFermate();
		this.cmbPartenza.getItems().addAll(fermate);
		this.cmbArrivo.getItems().addAll(fermate);



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
