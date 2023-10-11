package game.figure;

import game.util.MathUtils;

//represents ay + bx + c = 0
public class LineEquation {
    private final double a;
    private final double b;
    private final double c;

    public LineEquation(Line line) {
        this(line.getX(), line.getY());
    }

    public LineEquation(Point x, Point y) {
        if (Math.abs(x.x() - y.x()) <= MathUtils.EPS) {
            a = 0;
            b = 1;
            c = -x.x();
        } else {
            a = 1;
            b = -(x.y() - y.y()) / (x.x() - y.x());
            c = -x.y() - b * x.x();
        }
    }

    public double getA() {
        return a;
    }

    public double getB() {
        return b;
    }

    public double getC() {
        return c;
    }
}
