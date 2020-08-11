package it.polito.tdp.camminoAutobus;

import java.net.URL;
import java.util.ResourceBundle;

import it.polito.tdp.camminoAutobus.model.Model;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class FXMLController {
	
	private Model model;
    
    @FXML
    private Label label;
    
    @FXML
    private void handleButtonAction(ActionEvent event) {
        System.out.println("You clicked me!!");
        label.setText("Hello World!");
    }
    

	public void setModel(Model model) {
		this.model=new Model();
		
	}    
}
