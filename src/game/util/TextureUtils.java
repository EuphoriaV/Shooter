package game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TextureUtils {
    private final static BufferedImage BLANK_IMG = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

    public static Image[] getImage(String name) {
        BufferedImage img = BLANK_IMG;
        try {
            img = ImageIO.read(new File("images/" + name));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Image[] image = new Image[img.getWidth()];
        for (int i = 0; i < image.length; i++) {
            image[i] = img.getSubimage(Math.min(i, img.getWidth() - 1), 0, 1, img.getHeight());
        }
        return image;
    }
}
