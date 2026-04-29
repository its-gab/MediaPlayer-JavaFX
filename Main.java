package mediaplayer;

import java.io.File;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application{
	File songsFolder = new File("src/mediaplayer/audios");
	ListView<String> songsList = new ListView<>();
	ArrayList<Media> songs = new ArrayList<>();
	ArrayList<Integer> songsHistory = new ArrayList<>();
	MediaPlayer player;
	
	Label lTime = new Label("--:--/--:--");
	
	Button bBack = new Button("⏮");
	Button bPlay = new Button("▶");
	Button bForward = new Button("⏭");
	
	ProgressBar songProgress = new ProgressBar(0);
	
	double sound = 1.0;
	
	boolean autoPlay = false;
	
	boolean dragging = false;
	boolean wasPlayingBeforeDragging = false;
	
	int currentSong;
	int positionInHistory;
	
	public void loadSongs() {
		String[] files = songsFolder.list();
		if (files == null) return;
		
		for (int i = 0; i < files.length; i++) {
			File currentFile = new File(songsFolder,files[i]);
			if (!currentFile.isFile()) continue;
			 
			String extension = files[i].substring(files[i].lastIndexOf('.') + 1);
			if (!extension.equals("mp3") && !extension.equals("wav")) continue;
			
			songsList.getItems().add(files[i].substring(0, files[i].lastIndexOf('.')));
			
			songs.add(new Media(currentFile.toURI().toString()));
		}
	}
	
	public void uploadSong() {
		//TODO
	}
	
	public void setupMedia() {
		if (player == null) return;
		
		player.setVolume(sound);
	}
	
	public String formatTime(Duration duration) {
		int seconds = duration.toSeconds();
		
	}
	
	public void startSong(int index) {
		currentSong = index;
		
		if (player != null) player.stop();
		
		try {
		    player = new MediaPlayer(songs.get(index));
		} catch (Exception e) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setHeaderText("Audio Error");
			alert.setContentText("No audio device found.");
			alert.show();
		    return;
		}
		player.play();
		
		player.setOnEndOfMedia(() -> playNextSong());
		
		player.currentTimeProperty().addListener(e -> {
			if (player.getTotalDuration() == null) return;
			if (player.getTotalDuration().isUnknown()) return;
			if (dragging) return;
			
			double duration = player.getTotalDuration().toMillis();
			double currentTime = player.getCurrentTime().toMillis();
			
			
			
			
			songProgress.setProgress(currentTime / duration);
			
		});
		
		setupMedia();
		
		bPlay.setText("⏸");
	}
	
	public void playOrPause() {
		if (player == null) return;
		
		if (player.getStatus() == Status.PAUSED) {
			player.play();
			bPlay.setText("⏸");
		} else if (player.getStatus() == Status.PLAYING) {
			player.pause();
			bPlay.setText("▶");
		}
	}
	
	public void backOrForward(int i) {
		if (songsHistory.size() == 0) return;
		
		if (positionInHistory + i < 0) return;
		
		if (positionInHistory + i >= songsHistory.size()) {
			playNextSong();
			return;
		}
		
		positionInHistory += i;
		startSong(songsHistory.get(positionInHistory));
	}
	
	public void playNextSong() {
		int newSongIndex = currentSong + 1;
		
		 if (newSongIndex >= songs.size()) {
			newSongIndex = 0;
		}
		
		startSong(newSongIndex);
		songsHistory.add(newSongIndex);
		positionInHistory = songsHistory.size() - 1;
	}
	
	
	
	public void listEvent(MouseEvent e) {
		if (e.getButton() != MouseButton.PRIMARY) return;
		
		int index = songsList.getSelectionModel().getSelectedIndex();
		if (index == -1 || index == currentSong) return;
		
		for (int i = songsHistory.size() - 1; i > positionInHistory + 1; i--) {
			songsHistory.remove(i);
		}
		
		startSong(index);
		songsHistory.add(index);
		positionInHistory = songsHistory.size() - 1;
	}
	
	@Override
	public void start(Stage stage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(16));
		
		if (!songsFolder.exists()) {
			songsFolder.mkdir();
		}
		
		songsList.setFocusTraversable(false);
		songsList.setOnMouseClicked(e -> listEvent(e));
		root.setCenter(songsList);
		
		
		bBack.setOnAction(e -> backOrForward(-1));
		bPlay.setOnAction(e -> playOrPause());
		bForward.setOnAction(e -> backOrForward(1));
		
		HBox lowerControls = new HBox();
		lowerControls.setAlignment(Pos.CENTER);
		lowerControls.setPadding(new Insets(8, 0, 0, 0));
		lowerControls.setSpacing(30);
		lowerControls.getChildren().addAll(bBack, bPlay, bForward);
		
		songProgress.setPrefWidth(500);
		songProgress.setPrefHeight(20);
		songProgress.setOnMouseDragged(e -> {
			if (player == null) return;
			
			if (!dragging) {
				dragging = true;
				
				if (player.getStatus() == Status.PLAYING) {
					player.pause();
					wasPlayingBeforeDragging = true;
				}
			}
			
			double percent = e.getX()/songProgress.getWidth();
			songProgress.setProgress(percent);
			
		});
		
		songProgress.setOnMouseReleased(e -> {
			if (player == null) return;
			
			dragging = false;
			
			if (wasPlayingBeforeDragging) {
				player.play();
				wasPlayingBeforeDragging = false;
			}
			
			double songPercent = e.getX()/songProgress.getWidth();
			double newTime = player.getTotalDuration().toMillis() * songPercent;
			
			player.seek(Duration.millis(newTime));
			
		});
		
		HBox topControls = new HBox();
		topControls.setAlignment(Pos.CENTER);
		topControls.setPadding(new Insets(16, 0, 0, 0));
		topControls.setSpacing(30);
		topControls.getChildren().addAll(songProgress, lTime);
		
		VBox bottomBox = new VBox();
		bottomBox.getChildren().addAll(topControls, lowerControls);
		
		root.setBottom(bottomBox);
		
		Scene scene = new Scene(root, 640, 480);
		stage.setScene(scene);
		stage.setTitle("Media Player");
		stage.show();
		
		loadSongs();
	}
	
	public static void main(String[] args) {
		launch(args);
	}

}
