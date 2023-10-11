package game.view;

import game.Client;
import game.Player;
import game.figure.Circle;
import game.figure.Figure;
import game.figure.Line;
import game.figure.Point;
import game.figure.Polygon;
import game.util.MathUtils;
import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.JPanel;

public class Panel extends JPanel {
    private final Client client;
    private final int WIDTH;
    private final int HEIGHT;
    private final int RADAR_SIZE = 300;
    private final double DISTANCE = 15;

    public Panel(Client client) {
        this.client = client;
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        WIDTH = (int) screen.getWidth();
        HEIGHT = (int) screen.getHeight();
        setPreferredSize(screen);
        setLayout(null);

        final BufferedImage BLANK_IMG = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        final Cursor BLANK_CURSOR = Toolkit.getDefaultToolkit().createCustomCursor(
            BLANK_IMG, new java.awt.Point(0, 0), "blank cursor"
        );
        setCursor(BLANK_CURSOR);

        Robot robot;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                var curPos = e.getLocationOnScreen();
                double dx = WIDTH / 2.0 - curPos.getX();
                double alpha = (dx * (Math.PI / 3)) / WIDTH;
                client.turn(alpha);
                robot.mouseMove(WIDTH / 2, HEIGHT / 2);
            }
        });
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                client.shoot();
            }

            @Override
            public void mousePressed(MouseEvent e) {
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

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setPaint(Color.BLACK);
        g2d.fillRect(0, 0, WIDTH, HEIGHT / 2);
        g2d.setStroke(new BasicStroke(2));
        g2d.setPaint(Color.DARK_GRAY);
        g2d.fillRect(0, HEIGHT / 2, WIDTH, HEIGHT);

        drawPov(g2d);
        drawMap(g2d);
        drawHud(g2d);
    }

    private void drawPov(Graphics2D g2d) {
        client.getGame().updateLines();
        List<Line> lines = client.getGame().getLines();
        if (lines == null || lines.isEmpty()) {
            return;
        }
        for (int i = 0; i < WIDTH; i++) {
            int index = (i * client.getGame().LINE_COUNT) / WIDTH;
            Line line = lines.get(index);
            double len = MathUtils.length(line);
            double height = (HEIGHT * DISTANCE / len);
            Image image = null;
            Figure figure = client.getGame().getFigure(line.getY());
            if (figure instanceof Polygon polygon) {
                image = getImage(polygon, line.getY());
            } else if (figure instanceof Circle circle) {
                image = getImage(circle, line.getY());
            } else {
                Player bot = client.getGame().getBot(line.getY());
                if (bot != null) {
                    image = getImage(bot.getModel(), line.getY());
                }
            }
            if (image != null) {
                g2d.drawImage(image, i, (int) (HEIGHT / 2 - height), 1, (int) (2 * height), null);
            }
            g2d.setPaint(new Color(0, 0, 0, (int) (255 * (len / client.getGame().LINE_LENGTH))));
            g2d.drawLine(i, (int) (HEIGHT / 2 - height), i, (int) (HEIGHT / 2 + height));
        }
    }

    private void drawMap(Graphics2D g2d) {
        g2d.setPaint(Color.BLACK);
        g2d.fillRect(0, 0, RADAR_SIZE, RADAR_SIZE);
        g2d.setPaint(Color.WHITE);
        for (Figure figure : client.getGame().getFigures()) {
            if (figure instanceof Circle circle) {
                drawCircle(g2d, circle);
            }
            if (figure instanceof Polygon polygon) {
                drawPolygon(g2d, polygon);
            }
        }

        g2d.setColor(Color.GREEN);
        drawPolygon(g2d, client.getGame().getPlayer().getModel());

        for (var bot : client.getGame().getBots()) {
            g2d.setColor(Color.RED);
            drawPolygon(g2d, bot.getModel());
        }
    }

    private void drawHud(Graphics2D g2d) {
        //drawing crosshair
        g2d.setPaint(Color.WHITE);
        g2d.drawLine(WIDTH / 2 - 5, HEIGHT / 2, WIDTH / 2 - 10, HEIGHT / 2);
        g2d.drawLine(WIDTH / 2 + 5, HEIGHT / 2, WIDTH / 2 + 10, HEIGHT / 2);
        g2d.drawLine(WIDTH / 2, HEIGHT / 2 - 5, WIDTH / 2, HEIGHT / 2 - 10);
        g2d.drawLine(WIDTH / 2, HEIGHT / 2 + 5, WIDTH / 2, HEIGHT / 2 + 10);
        //drawing health bar
        int playersHP = client.getGame().getPlayer().getHealthPoints();
        g2d.setStroke(new BasicStroke(10));
        g2d.drawRect(WIDTH / 40, 9 * HEIGHT / 10, WIDTH / 5, HEIGHT / 20);
        g2d.setPaint(new Color((int) (255 - 255 * (playersHP / 100.0)), (int) (255 * (playersHP / 100.0)), 0));
        g2d.fillRect(
            WIDTH / 40 + 5,
            9 * HEIGHT / 10 + 5,
            (int) ((WIDTH / 5 - 10) * (playersHP / 100.0)),
            HEIGHT / 20 - 10
        );
        //draw stats
        g2d.setFont(new Font("Verdana", Font.BOLD, HEIGHT / 20));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Kills: " + client.getGame().getKills(), WIDTH / 50, 17 * HEIGHT / 20);
        g2d.drawString("Deaths: " + client.getGame().getDeaths(), WIDTH / 50, 3 * HEIGHT / 4);
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
}
