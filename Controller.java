import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Controller implements Observer{

	private String username;
	private String password;
//	private ArrayList<User2> logins;
	private Client client;
	private String selected1;
	private boolean read;

	public Controller() {

		client = new Client();
		
	}

	
	@FXML
	private Button loginbutton;
	@FXML
	private Button signOut;
	@FXML
	private Button okButton;
	@FXML
	private TextArea textArea;
	@FXML
	private ChoiceBox <String> choiceBox;
	@FXML
	private Label Xbutton;
	@FXML
	private Label xbutton;
	@FXML 
	private Label xbuttond;
	@FXML
	private TextField usernamefield;
	@FXML
	private PasswordField passwordfield;
	@FXML
	private TextArea TextArea;
	
	@FXML
	private void setItems() {
		ObservableList<String> journals = FXCollections.observableArrayList(client.getJournals());
		choiceBox.setItems(journals);
		okButton.setOnAction(e -> getChoice(choiceBox));
	}
	
	@FXML private void getChoice(ChoiceBox <String> choiceBox) {
		Client.read(true);
		client.setSelectedJournal(choiceBox.getValue());
	}
	@FXML
	void exit() {
		Stage stage = (Stage) Xbutton.getScene().getWindow();
		stage.close();
	}
	
	@FXML
	void exit2() {
		Stage stage = (Stage) xbutton.getScene().getWindow();
		stage.close();
	}
	@FXML
	void exitd() {
		Stage stage = (Stage) xbuttond.getScene().getWindow();
		stage.close();
	}


	@FXML
	void getUsername() {
		username = usernamefield.getText();
		System.out.println("Username is : " + username);
		client.setUsername(username);
	}

	@FXML
	void getPassword() {
		password = passwordfield.getText();
		client.setPassword(password);

	}
	@FXML
	void fillChoiceBox() {
	}

	/*
	 * @FXML void login() { System.out.println("Password is : " + password);
	 * System.out.println("Username is : " + username); }
	 */
	@FXML
	void login(ActionEvent event) throws IOException {
		Client.togglelogin(true);		
		try {
			Client.main(null);		


		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(Client.getAck());
		if(Client.getAck()) {	
			System.out.println(username.charAt(0));
			switch (username.charAt(0)) {
 
			case 'p':
				Parent rootp = FXMLLoader.load(getClass().getResource("Patient.fxml"));
				Scene scene2p = new Scene(rootp);
				Stage windowp = (Stage) ((Node) event.getSource()).getScene().getWindow();
				windowp.setScene(scene2p);
				windowp.show();
				break;
			case 'n':
				Parent rootn = FXMLLoader.load(getClass().getResource("Nurse.fxml"));
				Scene scene2n = new Scene(rootn);
				Stage windown = (Stage) ((Node) event.getSource()).getScene().getWindow();
				windown.setScene(scene2n);
				windown.show();
				break;
			case 'd':
				Parent rootd = FXMLLoader.load(getClass().getResource("Doctor.fxml"));
				Scene scene2d = new Scene(rootd);
				Stage windowd = (Stage) ((Node) event.getSource()).getScene().getWindow();
				windowd.setScene(scene2d);
				windowd.show();
				break;
			case 'g':
				Parent rootg = FXMLLoader.load(getClass().getResource("Government.fxml"));
				Scene scene2g = new Scene(rootg);
				Stage windowg = (Stage) ((Node) event.getSource()).getScene().getWindow();
				windowg.setScene(scene2g);
				windowg.show();
				break;
			}

	
		}
		
		
	}
	@FXML 
	void logout(ActionEvent event) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("gui.fxml"));
		Scene scene2 = new Scene(root);
		Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
		window.setScene(scene2);
		window.show();
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}

	
	
	/*					
			Parent root = FXMLLoader.load(getClass().getResource("scene2.fxml"));
			Scene scene2 = new Scene(root);
			Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
			window.setScene(scene2);
			window.show();
*/

}
