package game;

import game.view.Frame;
import game.view.Panel;
import game.view.Texture;
import javax.swing.Timer;

public class Client {
    private Game game;
    private Player player;
    private Panel panel;
    private boolean moveLeft;
    private boolean moveRight;
    private boolean moveBackward;
    private boolean moveForward;

    public void startGame() {
        Texture front = new Texture("front.png", true);
        Texture right = new Texture("right.png", true);
        Texture back = new Texture("back.png", true);
        Texture left = new Texture("left.png", true);
        player = new Player(front, right, back, left);
        game = new Game(player);

        panel = new Panel(this);
        new Frame(panel, this);

        Timer moveUpdate = new Timer(3, e -> {
            if (isMoveLeft() || isMoveRight() || isMoveBackward() || isMoveForward()) {
                move();
            }
            panel.repaint();
        });
        moveUpdate.start();
    }

    public void move() {
        int dir = 0;
        if (moveForward) {
            dir += 3;
        }
        if (moveBackward) {
            dir -= 3;
        }
        if (moveLeft) {
            dir += 1;
        }
        if (moveRight) {
            dir -= 1;
        }
        double alpha = switch (dir) {
            case 4 -> Math.PI / 4;
            case 3 -> 0;
            case 2 -> -Math.PI / 4;
            case 1 -> Math.PI / 2;
            case -1 -> -Math.PI / 2;
            case -2 -> 3 * Math.PI / 4;
            case -3 -> Math.PI;
            case -4 -> -3 * Math.PI / 4;
            default -> -1;
        };
        if (alpha != -1) {
            game.move(player, alpha);
        }
    }

    public void turn(double alpha) {
        game.turn(player, alpha);
    }

    public void shoot() {
        game.shoot(player);
    }

    public Game getGame() {
        return game;
    }

    public boolean isMoveLeft() {
        return moveLeft;
    }

    public void setMoveLeft(boolean moveLeft) {
        this.moveLeft = moveLeft;
    }

    public boolean isMoveRight() {
        return moveRight;
    }

    public void setMoveRight(boolean moveRight) {
        this.moveRight = moveRight;
    }

    public boolean isMoveBackward() {
        return moveBackward;
    }

    public void setMoveBackward(boolean moveBackward) {
        this.moveBackward = moveBackward;
    }

    public boolean isMoveForward() {
        return moveForward;
    }

    public void setMoveForward(boolean moveForward) {
        this.moveForward = moveForward;
    }

    public static void main(String[] args) {
        new Client().startGame();
    }
}
