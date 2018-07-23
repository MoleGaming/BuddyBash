package buddybash;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import javax.imageio.ImageIO;

import java.awt.Polygon;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import buddybash.internals.SettingsIO;

public class BuddyBash extends Application {
	private ArrayList<KeyCode> pressedKeys = new ArrayList<KeyCode>();
	private ArrayList<Long> heldSince = new ArrayList<Long>();
	private SettingsIO settings = new SettingsIO("settings.txt");
	private int screenW, screenH;

	private long tick = 0;

	private Pane root = new Pane();

	private FightSprite testFighter = new FightSprite(100, 100, "Hugh", 1);
	private FightSprite testFighter2 = new FightSprite(600, 100, "Hugh", 1);

	public Parent createContent() {
		root.setPrefSize(screenW, screenH);
		
		root.getChildren().add(testFighter);

		AnimationTimer timer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				update();
			}
		};

		timer.start();

		return root;
	}

	@Override
	public void start(Stage stage) throws Exception {
		getSettings();
		Scene scene = new Scene(createContent());

		scene.setOnKeyPressed(e -> {
			if (!pressedKeys.contains(e.getCode())) {
				pressedKeys.add(e.getCode());
				heldSince.add(tick);
			}
		});

		scene.setOnKeyReleased(e -> {
			if(pressedKeys.contains(e.getCode())){
				int index = pressedKeys.indexOf(e.getCode());
				pressedKeys.remove(index);
				heldSince.remove(index);
			}
		});

		stage.setScene(scene);
		stage.show();

	}

	private List<Sprite> sprites() {
		return root.getChildren().stream().map(n -> (Sprite) n).collect(Collectors.toList());
	}

	public void update() {
		tick++;
		for (Sprite s : sprites()) {
			switch (s.type) {
			case "Fighter":
				FightSprite fs = (FightSprite) s;
				fs.nextFrame();
				fs.parseInputs(pressedKeys);
				break;
			}
		}
	}

	public void getSettings() {
		if (settings.settingExists("resolution")) {
			String[] temp = settings.getSetting("resolution").toString().split("x");
			screenW = Integer.parseInt(temp[0]);
			screenH = Integer.parseInt(temp[1]);
		}
	}

	private static class Sprite extends ImageView {
		final String type;
		Polygon hitbox;

		public Sprite(int x, int y, String type, Image image) {
			super(image);
			this.type = type;
			this.setTranslateX(x);
			this.setTranslateY(y);
		}
		
	}

	private static class FightSprite extends Sprite {
		private ArrayList<KeyCode> rButtons = new ArrayList<KeyCode>();
		private ArrayList<KeyCode> lButtons = new ArrayList<KeyCode>();
		private ArrayList<KeyCode> uButtons = new ArrayList<KeyCode>();
		private ArrayList<KeyCode> dButtons = new ArrayList<KeyCode>();
		final String fighter;
		ArrayList<Image> animation;
		private int aniFrame = 0;
		HashMap<String, ArrayList<Image>> animations;
		HashMap<String, ArrayList<Polygon>> hitboxes;
		static HashMap<String, ArrayList<Image>> tempAnis;

		FightSprite(int x, int y, String fighter, double scale) {
			super(x, y, "Fighter", (tempAnis = getFighterAnimations(fighter)).get("idle").get(0));
			this.fighter = fighter;
			this.animations = tempAnis;
			this.animation = animations.get("idle");
			this.setScaleX(scale);
			this.setScaleY(scale);
			initInputs();
		}
		
		private void initInputs(){
			rButtons.add(KeyCode.D);
			rButtons.add(KeyCode.RIGHT);
			lButtons.add(KeyCode.A);
			lButtons.add(KeyCode.LEFT);
			uButtons.add(KeyCode.W);
			uButtons.add(KeyCode.UP);
			dButtons.add(KeyCode.S);
			dButtons.add(KeyCode.DOWN);
		}

		public void nextFrame() {
			aniFrame = (aniFrame + 1) % animation.size();
			this.setImage(animation.get(aniFrame));
		}

		public void setAnimation(String name) {
			animation = animations.get(name);
			this.setImage(animation.get(0));
		}
		
		public void parseInputs(ArrayList<KeyCode> pressed){
			for(KeyCode button : pressed){
				if(rButtons.contains(button))
					this.setTranslateX(this.getTranslateX()+10);
				if(lButtons.contains(button))
					this.setTranslateX(this.getTranslateX()-10);
				if(dButtons.contains(button))
					this.setTranslateY(this.getTranslateY()+10);
				if(uButtons.contains(button))
					this.setTranslateY(this.getTranslateY()-10);
			}
		}

		public static HashMap<String, ArrayList<Image>> getFighterAnimations(String name) {
			try {
				HashMap<String, ArrayList<Image>> out = new HashMap<String, ArrayList<Image>>();
				String filePath = FightSprite.class.getProtectionDomain().getCodeSource().getLocation()
						+ "buddybash/animations/" + name;
				filePath = filePath.replaceAll("%20", " ").substring(6);
				File fFolder = new File(filePath);
				for (String aName : fFolder.list()) {
					File aFolder = new File(filePath + "/" + aName);
					ArrayList<Image> frames = new ArrayList<Image>();
					for (String fName : aFolder.list()) {
						Image frame = SwingFXUtils.toFXImage(ImageIO.read(new File(filePath + "/" + aName + "/" + fName)), null);
						frames.add(frame);
					}
					out.put(aName, frames);
				}
				return out;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	private static class Animation {
		ArrayList<Image> frames;

		Animation(ArrayList<Image> frames) {
			this.frames = frames;
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}