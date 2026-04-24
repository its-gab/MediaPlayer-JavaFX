package mediaplayer;

import java.io.File;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.stage.Stage;

public class Main extends Application{
	File file = new File("src/mediaplayer/audios");
	ListView<String> songsList = new ListView<>();
	ArrayList<Media> songs = new ArrayList<>();
	
	public void loadSongs() {
		String[] files = file.list();
		for (int i = 0; i < files.length; i++) {
			File currentFile = new File(file.getPath()+"\\"+files[i]);
			if (!currentFile.isFile()) continue;
			
			System.out.println(files[i]);
			songsList.getItems().add(files[i]);
			System.out.println(currentFile.getAbsolutePath());
			songs.add(new Media(currentFile.toURI().toString()));
		}
	}
	
	public void mouseClick(MouseEvent e) {
		
	}
	
	@Override
	public void start(Stage stage) {
		BorderPane root = new BorderPane();
		
		if (!file.exists()) {
			file.mkdir();
		}
		
		root.setCenter(songsList);
		
		HBox controls = new HBox();
		controls.setAlignment(Pos.CENTER);
		controls.setPadding(new Insets(16));
		controls.setSpacing(30);
		
		songsList.setOnMouseClicked(e -> mouseClick(e));
		
		Button bBack = new Button("⏮");
		Button bPlay = new Button("▶");
		Button bForward = new Button("⏭");
		
		controls.getChildren().addAll(bBack, bPlay, bForward);
		
		root.setBottom(controls);
		
		Scene scene = new Scene(root, 640, 480);
		stage.setScene(scene);
		stage.setTitle("Media Player");
		stage.show();
		
		loadSongs();
		
		root.requestFocus();
	}
	
	public static void main(String[] args) {
		launch(args);
	}

}
