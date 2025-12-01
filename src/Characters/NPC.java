package Characters;

import java.util.ArrayList;
import javafx.scene.image.Image;

public abstract class NPC {

    protected String name;
    protected ArrayList<String> dialogue;
    protected transient Image fxImage;

    public NPC(String name, String sprite) {
        loadFxImage(sprite);
        setName(name);

    }

    private void loadFxImage(String spritePath) {
        if (!(spritePath == null || spritePath.isEmpty())) {

            try {
                fxImage = new Image(getClass().getResourceAsStream(spritePath));
                if (fxImage.isError()) {
                    fxImage = null;
                }
            } catch (Throwable ignored) {
                fxImage = null;
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public Image getFxImage() {
        return fxImage;
    }

}
