package it.polito.tdp.camminoAutobus;

import it.polito.tdp.camminoAutobus.model.Collegamento;
import it.polito.tdp.camminoAutobus.model.Model;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class CodiciLocaliController {
	

    @FXML
    private TextField txtPartenza;

    @FXML
    private TextField txtArrivo;

	private Model model;
	
	@FXML
	private TextArea txtResult;
	
    @FXML
    private ComboBox<Collegamento> cmbPartenza;
	
    @FXML
    private ComboBox<Collegamento> cmbArrivo;

	private Stage stage;

	private Scene oldScene;

	private ComboBox<Integer> modificaPartenza;

	private ComboBox<Integer> modificaArrivo;

    @FXML
    void doCercaArrivo(ActionEvent event) {
    	if(this.txtArrivo.getText().equals("")) {
    		this.txtResult.setStyle("-fx-text-inner-color: red;");
    		this.txtResult.setText("INSERIRE UN VALORE DI ARRIVO");
    		return;
    	}
    	this.cmbArrivo.getItems().clear();
    	this.cmbArrivo.getItems().addAll(model.cercaCodice(this.txtArrivo.getText()));
    	

    }

    @FXML
    void doCercaPartenza(ActionEvent event) {
    	if(this.txtPartenza.getText().equals("")) {
    		this.txtResult.setStyle("-fx-text-inner-color: red;");
    		this.txtResult.setText("INSERIRE UN VALORE DI PARTENZA");
    		return;
    	}
    	System.out.println(this.txtPartenza.getText());
    	this.cmbPartenza.getItems().clear();
    	this.cmbPartenza.getItems().addAll(model.cercaCodice(this.txtPartenza.getText()));

    }
    
    @FXML
    void doInserisciDati(ActionEvent event) {
    	if(this.cmbPartenza.getValue()!=null) {
    		this.modificaPartenza.setValue(this.cmbPartenza.getValue().getCodiceLocale());
    	}
    	if(this.cmbArrivo.getValue()!=null) {
    		this.modificaArrivo.setValue(this.cmbArrivo.getValue().getCodiceLocale());
    	}
    	stage.setScene(oldScene);
		stage.show();
    
    }

	public void setModel(Model model, Stage stage, Scene oldScene, ComboBox<Integer> modificaPartenza, ComboBox<Integer> modificaArrivo) {
		this.model=model;
		this.stage=stage;
		this.oldScene=oldScene;
		this.modificaPartenza=modificaPartenza;
		this.modificaArrivo=modificaArrivo;
		
		
	}

}
