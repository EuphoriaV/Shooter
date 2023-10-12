package game;

import game.figure.Line;
import game.util.MathUtils;

import java.util.*;

public class BotAI {
    private final Game game;
    private final Map<Player, Double> botsDirections;
    private final Map<Player, Integer> lastShot;
    private final double[] directions =
        {0, Math.PI / 4, Math.PI / 2, 3 * Math.PI / 4, Math.PI, -3 * Math.PI / 4, -Math.PI / 2, -Math.PI / 4};

    public BotAI(Game game) {
        this.game = game;
        botsDirections = new HashMap<>();
        lastShot = new HashMap<>();
    }

    public void moveBots() {
        for (var bot : game.getBots()) {
            move(bot);
        }
    }

    public void setDirections() {
        for (var bot : game.getBots()) {
            botsDirections.put(bot, directions[new Random().nextInt(8)]);
        }
    }

    private void move(Player bot) {
        game.move(bot, botsDirections.getOrDefault(bot, 0.0));
        lastShot.put(bot, lastShot.getOrDefault(bot, 0) + 1);

        List<Player> visibleEnemies = new ArrayList<>();
        for (Player enemy : game.getEnemies(bot)) {
            if (game.isVisible(bot, enemy)) {
                visibleEnemies.add(enemy);
            }
        }
        if (visibleEnemies.isEmpty()) {
            visibleEnemies = game.getEnemies(bot);
        }

        double min = Integer.MAX_VALUE;
        Player closestEnemy = null;
        for (Player visibleEnemy : visibleEnemies) {
            double cur = MathUtils.dist(visibleEnemy.getPos(), bot.getPos());
            if (cur < min) {
                min = cur;
                closestEnemy = visibleEnemy;
            }
        }
        if (closestEnemy == null) {
            return;
        }

        Line line = new Line(bot.getPos(), closestEnemy.getPos());
        game.intersect(bot, line);
        if (MathUtils.inPolygon(line.getY(), closestEnemy.getModel()) && lastShot.get(bot) > 10) {
            game.shoot(bot);
            lastShot.put(bot, 0);
        }
        double angleDistance = MathUtils.getAngle(new Line(bot.getPos(), closestEnemy.getPos())) - bot.getAlpha();
        if (angleDistance > Math.PI) {
            angleDistance -= 2 * Math.PI;
        } else if (angleDistance < -Math.PI) {
            angleDistance += 2 * Math.PI;
        }
        game.turn(bot, angleDistance / 7);
    }
}
