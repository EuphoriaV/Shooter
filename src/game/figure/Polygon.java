package game.figure;

import game.util.MathUtils;
import game.view.Texture;
import java.util.ArrayList;
import java.util.List;

public record Polygon(List<Point> points, List<Texture> textures) implements Figure {
    public Polygon(List<Point> points, Texture texture) {
        this(points, new ArrayList<>());
        for (int i = 0; i < points.size(); i++) {
            textures.add(texture);
        }
    }

    public List<Line> getWalls() {
        List<Line> walls = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            Point first = points.get(i);
            Point second = points.get((i + 1) % points.size());
            walls.add(new Line(first, second));
        }
        return walls;
    }

    public Texture getTexture(Point point) {
        var walls = getWalls();
        for (int i = 0; i < points.size(); i++) {
            if (MathUtils.inLine(point, walls.get(i))) {
                return textures.get(i);
            }
        }
        return null;
    }

    public Line getWall(Point point) {
        var walls = getWalls();
        for (Line wall : walls) {
            if (MathUtils.inLine(point, wall)) {
                return wall;
            }
        }
        return null;
    }
}
