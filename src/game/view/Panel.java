package game.view;

import game.Client;
import game.Game;
import game.Player;
import game.figure.Point;
import game.figure.Polygon;
import game.figure.*;
import game.util.MathUtils;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Panel extends JPanel {
    private final Client client;
    private final int WIDTH;
    private final int HEIGHT;
    private final int RADAR_SIZE = 300;
    private final double DISTANCE = 15;
    private double movementNumber = 0;

    public Panel(Client client) {
        this.client = client;
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        WIDTH = (int) screen.getWidth();
        HEIGHT = (int) screen.getHeight();
        setPreferredSize(screen);
        setLayout(null);
        hideCursor();
        listenShooting();
        listenTurning();
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        drawBackground(g2d);
        drawPov(g2d);
        g2d.setStroke(new BasicStroke(1));
        drawMap(g2d);
        drawHud(g2d);
    }

    private void drawBackground(Graphics2D g2d) {
        g2d.setPaint(Color.BLACK);
        g2d.fillRect(0, 0, WIDTH, HEIGHT / 2);
        g2d.setStroke(new BasicStroke(2));
        g2d.setPaint(Color.DARK_GRAY);
        g2d.fillRect(0, HEIGHT / 2, WIDTH, HEIGHT);
    }

    private void drawPov(Graphics2D g2d) {
        Game game = client.getGame();
        game.updateLines();
        List<Line> lines = game.getLines();
        if (lines == null || lines.isEmpty()) {
            return;
        }

        for (int i = 0; i < WIDTH; i++) {
            int index = (i * game.LINE_COUNT) / WIDTH;
            Line line = lines.get(index);
            double len = MathUtils.length(line);
            double height = (HEIGHT * DISTANCE / MathUtils.length(line));

            Image image = null;
            Figure figure = game.getFigure(line.getY());
            if (figure instanceof Polygon polygon) {
                image = getImage(polygon, line.getY());
            } else if (figure instanceof Circle circle) {
                image = getImage(circle, line.getY());
            } else {
                Player bot = game.getBot(line.getY());
                if (bot != null) {
                    image = getImage(bot.getModel(), line.getY());
                }
            }
            if (image != null) {
                g2d.drawImage(image, i, (int) (HEIGHT / 2 - height), 1, (int) (2 * height), null);
            }
            g2d.setPaint(new Color(0, 0, 0, (int) (255 * (len / game.LINE_LENGTH))));
            g2d.drawLine(i, (int) (HEIGHT / 2 - height), i, (int) (HEIGHT / 2 + height));
        }

        drawBotsHP(g2d);
        drawGun(g2d);
    }

    private void drawBotsHP(Graphics2D g2d) {
        Game game = client.getGame();
        List<Line> lines = game.getLines();
        for (Player bot : game.getBots()) {
            Line line = new Line(game.getPlayer().getPos(), bot.getPos());
            double l = (MathUtils.getAngle(lines.get(lines.size() - 1)) + 2 * Math.PI) % (2 * Math.PI);
            double r = (MathUtils.getAngle(lines.get(0)) + 2 * Math.PI) % (2 * Math.PI);
            double botAngle = (MathUtils.getAngle(line) + 2 * Math.PI) % (2 * Math.PI);
            if (l > r) {
                l -= 2 * Math.PI;
            }
            if (botAngle > r) {
                botAngle -= 2 * Math.PI;
            }
            game.intersect(game.getPlayer(), line);
            if (botAngle <= r && botAngle >= l) {
                int column = (int) (WIDTH * ((r - botAngle) / (r - l)));
                double height = (HEIGHT * DISTANCE / MathUtils.length(line));
                if (game.isVisible(game.getPlayer(), bot)) {
                    int botsHp = bot.getHealthPoints();
                    g2d.setPaint(new Color(255, 255, 255, 100));
                    g2d.setStroke(new BasicStroke(10));
                    g2d.drawRect(
                        (int) (column - height / 2),
                        (int) (HEIGHT / 2 - height * 1.4),
                        (int) (height),
                        (int) (3 * height / 10)
                    );
                    g2d.setPaint(new Color(
                        (int) (255 - 255 * (botsHp / 100.0)),
                        (int) (255 * (botsHp / 100.0)),
                        0,
                        100
                    ));
                    g2d.fillRect(
                        (int) (column - height / 2 + 5),
                        (int) (HEIGHT / 2 - height * 1.4 + 5),
                        (int) ((height - 10) * (botsHp / 100.0)),
                        (int) (3 * height / 10 - 10)
                    );
                }
            }
        }
    }

    private void drawGun(Graphics2D g2d) {
        BufferedImage image;
        try {
            image = ImageIO.read(new File("images", "gun.png"));
        } catch (IOException e) {
            System.err.println("Error while reading file: " + e);
            return;
        }
        int gunHeight = (int) (7 * HEIGHT / 10 - Math.sin(movementNumber) * HEIGHT / 10);
        g2d.drawImage(image, 2 * WIDTH / 3, gunHeight, 6 * WIDTH / 17, 2 * HEIGHT / 5, null);
    }

    private void drawMap(Graphics2D g2d) {
        Game game = client.getGame();
        g2d.setPaint(Color.BLACK);
        g2d.fillRect(0, 0, RADAR_SIZE, RADAR_SIZE);
        g2d.setPaint(Color.WHITE);
        for (Figure figure : game.getFigures()) {
            if (figure instanceof Circle circle) {
                drawCircle(g2d, circle);
            }
            if (figure instanceof Polygon polygon) {
                drawPolygon(g2d, polygon);
            }
        }

        g2d.setColor(Color.GREEN);
        drawPolygon(g2d, game.getPlayer().getModel());

        for (var bot : game.getBots()) {
            g2d.setColor(Color.RED);
            drawPolygon(g2d, bot.getModel());
        }
    }

    private void drawHud(Graphics2D g2d) {
        Game game = client.getGame();
        //drawing crosshair
        g2d.setColor(Color.WHITE);
        g2d.drawLine(WIDTH / 2 - 5, HEIGHT / 2, WIDTH / 2 - 10, HEIGHT / 2);
        g2d.drawLine(WIDTH / 2 + 5, HEIGHT / 2, WIDTH / 2 + 10, HEIGHT / 2);
        g2d.drawLine(WIDTH / 2, HEIGHT / 2 - 5, WIDTH / 2, HEIGHT / 2 - 10);
        g2d.drawLine(WIDTH / 2, HEIGHT / 2 + 5, WIDTH / 2, HEIGHT / 2 + 10);
        //drawing health bar
        int playersHP = game.getPlayer().getHealthPoints();
        g2d.setPaint(new Color(255, 255, 255, 100));
        g2d.setStroke(new BasicStroke(10));
        g2d.drawRect(WIDTH / 40, 9 * HEIGHT / 10, WIDTH / 5, HEIGHT / 20);
        g2d.setPaint(new Color((int) (255 - 255 * (playersHP / 100.0)), (int) (255 * (playersHP / 100.0)), 0, 100));
        g2d.fillRect(
            WIDTH / 40 + 5,
            9 * HEIGHT / 10 + 5,
            (int) ((WIDTH / 5 - 10) * (playersHP / 100.0)),
            HEIGHT / 20 - 10
        );
        //draw stats
        g2d.setFont(new Font("Verdana", Font.BOLD, HEIGHT / 20));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Kills: " + game.getKills(), WIDTH / 50, 17 * HEIGHT / 20);
        g2d.drawString("Deaths: " + game.getDeaths(), WIDTH / 50, 3 * HEIGHT / 4);
    }

    private void drawPolygon(Graphics2D g2d, Polygon polygon) {
        for (Line wall : polygon.getWalls()) {
            drawLine(g2d, wall);
        }
    }

    private void drawCircle(Graphics2D g2d, Circle circle) {
        double scale = RADAR_SIZE / client.getGame().SIZE;
        g2d.drawOval(
            (int) ((circle.center().x() - circle.radius()) * scale),
            (int) (RADAR_SIZE - (circle.center().y() + circle.radius()) * scale),
            (int) (2 * circle.radius() * scale),
            (int) (2 * circle.radius() * scale)
        );
    }

    private void drawLine(Graphics2D g2d, Line line) {
        double scale = RADAR_SIZE / client.getGame().SIZE;
        g2d.drawLine(
            (int) (line.getX().x() * scale),
            (int) (RADAR_SIZE - line.getX().y() * scale),
            (int) (line.getY().x() * scale),
            (int) (RADAR_SIZE - line.getY().y() * scale)
        );
    }

    private Image getImage(Polygon polygon, Point point) {
        Texture texture = polygon.getTexture(point);
        Line wall = polygon.getWall(point);
        if (texture != null && wall != null) {
            double numberOfColumn;
            if (texture.isStretched()) {
                numberOfColumn = MathUtils.dist(wall.getX(), point) / MathUtils.length(wall);
            } else {
                numberOfColumn = MathUtils.dist(wall.getX(), point) / (2 * DISTANCE);
            }
            return texture.getImage()[(int) (numberOfColumn * texture.getImage().length) %
                texture.getImage().length];
        }
        return null;
    }

    private Image getImage(Circle circle, Point point) {
        Texture texture = circle.texture();
        double angle = MathUtils.getAngle(new Line(circle.center(), point));
        angle += Math.PI;
        double numberOfColumn;
        if (circle.texture().isStretched()) {
            numberOfColumn = angle / (2 * Math.PI);
        } else {
            numberOfColumn = angle * circle.radius() / (2 * DISTANCE);
        }
        return texture.getImage()[(int) (numberOfColumn * texture.getImage().length) % texture.getImage().length];
    }

    private void hideCursor() {
        final BufferedImage BLANK_IMG = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        final Cursor BLANK_CURSOR = Toolkit.getDefaultToolkit().createCustomCursor(
            BLANK_IMG, new java.awt.Point(0, 0), "blank cursor"
        );
        setCursor(BLANK_CURSOR);
    }

    private void listenTurning() {
        Robot robot;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseMove(e, robot);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                mouseMove(e, robot);
            }
        });
    }

    private void mouseMove(MouseEvent e, Robot robot) {
        var curPos = e.getLocationOnScreen();
        double dx = WIDTH / 2.0 - curPos.getX();
        double alpha = (dx * (Math.PI / 3)) / WIDTH;
        client.turn(alpha);
        robot.mouseMove(WIDTH / 2, HEIGHT / 2);
    }

    private void listenShooting() {
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                client.shoot();
                movementNumber = Math.PI / 2;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
    }

    public void keepMoving() {
        movementNumber = (movementNumber + 0.1) % (2 * Math.PI);
    }

    public void stopMoving() {
        movementNumber = (movementNumber + 0.1) % (2 * Math.PI);
        if (movementNumber <= 0.1 || Math.abs(movementNumber - Math.PI) <= 0.1) {
            movementNumber = 0;
        }
    }
}
