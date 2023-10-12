package game.view;

import game.util.TextureUtils;

import java.awt.Image;

public class Texture {
    private final boolean stretched;
    private final Image[] image;

    public boolean isStretched() {
        return stretched;
    }

    public Texture(String name) {
        this(name, false);
    }

    public Texture(String name, boolean stretched) {
        this.stretched = stretched;
        image = TextureUtils.getImage(name);
    }

    public Image[] getImage() {
        return image;
    }
}
