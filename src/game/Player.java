package game;

import game.figure.Line;
import game.figure.Point;
import game.figure.Polygon;
import game.util.MathUtils;
import game.view.Texture;
import java.util.List;

public class Player {
    public static final double STEP_LENGTH = 1;
    public static final double VIEW_ANGLE = Math.PI / 2;
    public static final double PLAYER_WIDTH = 5;
    private Point pos;
    private double alpha;
    private int healthPoints;
    private final Texture textureFront;
    private final Texture textureRight;
    private final Texture textureBack;
    private final Texture textureLeft;

    public Player(Texture textureFront, Texture textureRight, Texture textureBack, Texture textureLeft) {
        this.textureFront = textureFront;
        this.textureRight = textureRight;
        this.textureBack = textureBack;
        this.textureLeft = textureLeft;
    }

    public Point getPos() {
        return pos;
    }

    public void setPos(Point pos) {
        this.pos = pos;
    }

    public double getAlpha() {
        updateAlpha();
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
        updateAlpha();
    }

    private void updateAlpha() {
        alpha = (alpha % (2 * Math.PI) + (2 * Math.PI)) % (2 * Math.PI);
    }

    public int getHealthPoints() {
        return healthPoints;
    }

    public void setHealthPoints(int healthPoints) {
        this.healthPoints = healthPoints;
    }

    public Polygon getModel() {
        Line a = MathUtils.getLine(getPos(), getAlpha() - Math.PI / 4, PLAYER_WIDTH);
        Line b = MathUtils.getLine(getPos(), getAlpha() + Math.PI / 4, PLAYER_WIDTH);
        Line c = MathUtils.getLine(getPos(), getAlpha() + 3 * Math.PI / 4, PLAYER_WIDTH);
        Line d = MathUtils.getLine(getPos(), getAlpha() - 3 * Math.PI / 4, PLAYER_WIDTH);
        return new Polygon(
            List.of(a.getY(), b.getY(), c.getY(), d.getY()),
            List.of(textureFront, textureLeft, textureBack, textureRight)
        );
    }
}
