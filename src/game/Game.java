package game;

import game.figure.Circle;
import game.figure.Figure;
import game.figure.Line;
import game.figure.Point;
import game.figure.Polygon;
import game.util.MathUtils;
import game.view.Texture;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;
import static game.Player.PLAYER_WIDTH;
import static game.Player.STEP_LENGTH;
import static game.Player.VIEW_ANGLE;

public class Game {
    private final Player player;
    private final List<Player> bots;
    private final List<Figure> figures;
    private final List<Point> spawns;
    private List<Line> lines;
    private final BotAI botAI;
    public final double SIZE = 500;
    public final double LINE_LENGTH = 700;
    public final int LINE_COUNT = 1000;
    private final int BOT_NUMBERS = 4;
    private final int DAMAGE = 25;
    private int kills = 0;
    private int deaths = 0;

    public Game(Player player) {
        this.player = player;
        bots = new ArrayList<>();
        figures = new ArrayList<>();
        lines = new ArrayList<>();
        spawns = new ArrayList<>();
        initMap();
        initSpawns();
        spawn(player);
        initBots();

        botAI = new BotAI(this);
        Timer setDirectionsTimer = new Timer(1000, e -> botAI.setDirections());
        setDirectionsTimer.start();
        Timer moveBotsTimer = new Timer(10, e -> botAI.moveBots());
        moveBotsTimer.start();
    }

    private void initMap() {
        Texture bricks = new Texture("bricks.png");
        Texture planks = new Texture("planks.png");
        Texture wood = new Texture("wood.png");
        Texture stoneBricks = new Texture("stone_bricks.png");
        Texture stone = new Texture("stone.png");
        Texture birch = new Texture("birch.png");

        figures.add(new Polygon(List.of(
            new Point(50, 50),
            new Point(450, 50),
            new Point(450, 450),
            new Point(50, 450)
        ), planks));
        figures.add(new Circle(new Point(50, 50), 50, wood));
        figures.add(new Circle(new Point(450, 50), 50, wood));
        figures.add(new Circle(new Point(50, 450), 50, wood));
        figures.add(new Circle(new Point(450, 450), 50, wood));
        figures.add(new Polygon(List.of(
            new Point(110, 130),
            new Point(110, 230),
            new Point(120, 230),
            new Point(120, 130)
        ), stoneBricks));
        figures.add(new Polygon(List.of(
            new Point(110, 270),
            new Point(110, 370),
            new Point(120, 370),
            new Point(120, 270)
        ), bricks));
        figures.add(new Polygon(List.of(
            new Point(390, 130),
            new Point(390, 230),
            new Point(380, 230),
            new Point(380, 130)
        ), bricks));
        figures.add(new Polygon(List.of(
            new Point(390, 270),
            new Point(390, 370),
            new Point(380, 370),
            new Point(380, 270)
        ), stoneBricks));
        figures.add(new Circle(new Point(80, 250), 8, birch));
        figures.add(new Circle(new Point(420, 250), 8, birch));
        figures.add(new Polygon(List.of(
            new Point(160, 100),
            new Point(160, 140),
            new Point(230, 140),
            new Point(230, 100)
        ), stone));
        figures.add(new Polygon(List.of(
            new Point(340, 400),
            new Point(340, 360),
            new Point(270, 360),
            new Point(270, 400)
        ), stone));
        figures.add(new Circle(new Point(270, 120), 15, wood));
        figures.add(new Circle(new Point(230, 380), 15, wood));
        figures.add(new Polygon(List.of(
            new Point(310, 110),
            new Point(310, 120),
            new Point(350, 120),
            new Point(350, 110)
        ), stoneBricks));
        figures.add(new Polygon(List.of(
            new Point(190, 390),
            new Point(190, 380),
            new Point(150, 380),
            new Point(150, 390)
        ), stoneBricks));
    }

    private void initSpawns() {
        spawns.add(new Point(80, 150));
        spawns.add(new Point(80, 350));
        spawns.add(new Point(420, 150));
        spawns.add(new Point(420, 350));
        spawns.add(new Point(250, 75));
        spawns.add(new Point(250, 425));
    }

    private void initBots() {
        Texture front = new Texture("front.png", true);
        Texture right = new Texture("right.png", true);
        Texture back = new Texture("back.png", true);
        Texture left = new Texture("left.png", true);
        for (int i = 0; i < BOT_NUMBERS; i++) {
            Player bot = new Player(front, right, back, left);
            bots.add(bot);
            spawn(bot);
        }
    }

    private void spawn(Player player) {
        player.setPos(getSpawn(player));
        player.setHealthPoints(100);
    }

    public void move(Player player, double alpha) {
        alpha += player.getAlpha();
        Point newPos = new Point(
            player.getPos().x() + Math.cos(alpha) * STEP_LENGTH,
            player.getPos().y() + Math.sin(alpha) * STEP_LENGTH
        );

        List<Line> walls = getWalls(player);
        for (Line wall : walls) {
            Line shortestWay = MathUtils.shortestWay(newPos, wall);
            if (MathUtils.length(shortestWay) > PLAYER_WIDTH) {
                continue;
            }
            Line distanceBetweenWall =
                MathUtils.getLine(shortestWay.getY(), MathUtils.getAngle(shortestWay) + Math.PI, PLAYER_WIDTH);
            newPos = distanceBetweenWall.getY();
        }

        for (Line wall : walls) {
            Line shortestWay = MathUtils.shortestWay(newPos, wall);
            if (MathUtils.length(shortestWay) + MathUtils.EPS < PLAYER_WIDTH) {
                return;
            }
        }
        player.setPos(newPos);
    }

    public void turn(Player player, double alpha) {
        player.setAlpha(player.getAlpha() + alpha);
    }

    public void shoot(Player player) {
        Line line = MathUtils.getLine(player.getPos(), player.getAlpha(), Integer.MAX_VALUE);
        intersect(player, line);
        for (Player enemy : getEnemies(player)) {
            if (MathUtils.inPolygon(line.getY(), enemy.getModel())) {
                enemy.setHealthPoints(enemy.getHealthPoints() - DAMAGE);
                if (enemy.getHealthPoints() <= 0) {
                    if (player == this.player) {
                        kills++;
                    }
                    if (enemy == this.player) {
                        deaths++;
                    }
                    spawn(enemy);
                }
            }
        }
    }

    public void updateLines() {
        double delta = VIEW_ANGLE / LINE_COUNT;
        double start = player.getAlpha() - VIEW_ANGLE / 2;
        double end = player.getAlpha() + VIEW_ANGLE / 2;
        List<Line> newLines = new ArrayList<>();
        for (double i = end; i >= start; i -= delta) {
            Line newLine = MathUtils.getLine(player.getPos(), i, LINE_LENGTH);
            intersect(player, newLine);
            newLines.add(newLine);
        }
        lines = newLines;
    }

    public void intersect(Player player, Line line) {
        for (Figure figure : figures) {
            if (figure instanceof Circle circle) {
                intersect(line, circle);
            } else if (figure instanceof Polygon polygon) {
                intersect(line, polygon);
            }
        }
        for (var enemy : getEnemies(player)) {
            intersect(line, enemy.getModel());
        }
    }

    private void intersect(Line line, Polygon polygon) {
        for (Line wall : polygon.getWalls()) {
            Point intersection = MathUtils.intersect(line, wall);
            if (intersection != null && MathUtils.inLine(intersection, line)) {
                line.setY(intersection);
            }
        }
    }

    private void intersect(Line line, Circle circle) {
        Point intersection = MathUtils.intersect(line, circle);
        if (intersection != null && MathUtils.inLine(intersection, line)) {
            line.setY(intersection);
        }
    }

    private List<Line> getWalls(Player player) {
        List<Line> walls = new ArrayList<>();
        for (Figure figure : figures) {
            if (figure instanceof Polygon polygon) {
                walls.addAll(polygon.getWalls());
            }
            if (figure instanceof Circle circle) {
                Line line = new Line(circle.center(), player.getPos());
                if (MathUtils.length(line) < circle.radius()) {
                    line = MathUtils.getLine(circle.center(), MathUtils.getAngle(line), circle.radius() + 10);
                }
                Point intersection = MathUtils.intersect(line, circle);
                Line first = MathUtils.getLine(intersection, MathUtils.getAngle(line) + Math.PI / 2, 1);
                Line second = MathUtils.getLine(intersection, MathUtils.getAngle(line) - Math.PI / 2, 1);
                walls.add(new Line(first.getY(), second.getY()));
            }
        }
        for (var enemy : getEnemies(player)) {
            walls.addAll(enemy.getModel().getWalls());
        }
        return walls;
    }

    private Point getSpawn(Player player) {
        double max = 0;
        Point bestSpawn = null;
        for (Point spawn : spawns) {
            double cur = Integer.MAX_VALUE;
            for (Player enemy : getEnemies(player)) {
                cur = Math.min(cur, MathUtils.dist(spawn, enemy.getPos()));
            }
            if (max < cur) {
                max = cur;
                bestSpawn = spawn;
            }
        }
        return bestSpawn;
    }

    public Figure getFigure(Point point) {
        for (Figure figure : figures) {
            if (figure instanceof Polygon polygon && MathUtils.inPolygon(point, polygon)) {
                return polygon;
            }
            if (figure instanceof Circle circle && MathUtils.inCircle(point, circle)) {
                return circle;
            }
        }
        return null;
    }

    public Player getBot(Point point) {
        for (Player bot : bots) {
            if (MathUtils.inPolygon(point, bot.getModel())) {
                return bot;
            }
        }
        return null;
    }

    public List<Figure> getFigures() {
        return figures;
    }

    public List<Line> getLines() {
        return lines;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Player> getBots() {
        return bots;
    }

    public List<Player> getEnemies(Player player) {
        if (player == this.player) {
            return bots;
        } else {
            var enemies = new ArrayList<>(bots);
            enemies.remove(player);
            enemies.add(this.player);
            return enemies;
        }
    }

    public boolean isVisible(Player player, Player target) {
        Line line = new Line(player.getPos(), target.getPos());
        intersect(player, line);
        return MathUtils.inPolygon(line.getY(), target.getModel());
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }
}
