package game.util;

import game.figure.*;

import java.util.ArrayList;
import java.util.List;

public class MathUtils {
    public static final double EPS = 1e-4;

    public static Line getLine(Point point, double alpha, double length) {
        return new Line(point, new Point(
            Math.cos(alpha) * length + point.x(),
            Math.sin(alpha) * length + point.y()
        ));
    }

    public static double dist(Point a, Point b) {
        return Math.sqrt((a.x() - b.x()) * (a.x() - b.x()) + (b.y() - a.y()) * (b.y() - a.y()));
    }

    public static double length(Line a) {
        return dist(a.getX(), a.getY());
    }

    public static boolean inLine(Point point, Line line) {
        LineEquation equation = new LineEquation(line);
        if (Math.abs(equation.getA() * point.y() + equation.getB() * point.x() + equation.getC()) > MathUtils.EPS) {
            return false;
        }
        return point.x() <= Math.max(line.getX().x(), line.getY().x()) &&
            point.x() >= Math.min(line.getX().x(), line.getY().x()) &&
            point.y() <= Math.max(line.getX().y(), line.getY().y()) &&
            point.y() >= Math.min(line.getX().y(), line.getY().y());
    }

    public static boolean inPolygon(Point point, Polygon polygon) {
        for (Line wall : polygon.getWalls()) {
            if (inLine(point, wall)) {
                return true;
            }
        }
        return false;
    }

    public static boolean inCircle(Point point, Circle circle) {
        return Math.abs((circle.center().x() - point.x()) * (circle.center().x() - point.x()) +
            (circle.center().y() - point.y()) * (circle.center().y() - point.y()) -
            circle.radius() * circle.radius()) <= 0.1;
    }

    public static Point intersect(LineEquation first, LineEquation second) {
        if (second.getA() == 0) {
            var t = first;
            first = second;
            second = t;
        }
        if ((first.getA() == second.getA()) && (first.getB() == second.getB())) {
            return null;
        }
        double x, y;
        if (first.getA() == 0) {
            x = -first.getC();
        } else {
            x = (first.getC() * second.getA() - second.getC()) / (second.getB() - second.getA() * first.getB());
        }
        y = -second.getB() * x - second.getC();
        return new Point(x, y);
    }

    public static Point intersect(Line first, Line second) {
        Point intersection =
            intersect(new LineEquation(first), new LineEquation(second));
        if (intersection == null || !inLine(intersection, first) || !inLine(intersection, second)) {
            return null;
        }
        return intersection;
    }

    public static List<Point> intersect(LineEquation lineEquation, Circle circle) {
        List<Point> points = new ArrayList<>();
        double x0 = circle.center().x(), y0 = circle.center().y(), r = circle.radius(), a = lineEquation.getA(), b =
            lineEquation.getB(), c = lineEquation.getC();
        if (a == 0) {
            double x = -c;
            double aa = 1, bb = -2 * y0, cc = y0 * y0 + x0 * x0 + c * c - r * r + 2 * x0 * c;
            double D = bb * bb - 4 * cc * aa;
            if (D == 0) {
                double y = -bb / (2 * aa);
                points.add(new Point(x, y));
            } else if (D > 0) {
                double y1 = (-bb + Math.sqrt(D)) / (2 * aa), y2 = (-bb - Math.sqrt(D)) / (2 * aa);
                points.add(new Point(x, y1));
                points.add(new Point(x, y2));
            }
        } else {
            double aa = 1 + b * b, bb = -2 * x0 + 2 * b * c + 2 * y0 * b, cc =
                x0 * x0 + c * c + y0 * y0 + 2 * y0 * c - r * r;
            double D = bb * bb - 4 * cc * aa;
            if (D == 0) {
                double x = -bb / (2 * aa);
                double y = -b * x - c;
                points.add(new Point(x, y));
            } else if (D > 0) {
                double x1 = (-bb + Math.sqrt(D)) / (2 * aa), x2 = (-bb - Math.sqrt(D)) / (2 * aa);
                double y1 = -b * x1 - c, y2 = -b * x2 - c;
                points.add(new Point(x1, y1));
                points.add(new Point(x2, y2));
            }
        }
        return points;
    }

    public static Point intersect(Line line, Circle circle) {
        List<Point> intersections = intersect(new LineEquation(line), circle);
        Point res = line.getY();
        for (Point point : intersections) {
            if (inLine(point, line) && dist(line.getX(), res) > dist(line.getX(), point)) {
                res = point;
            }
        }
        return res;
    }

    public static double getAngle(Line line) {
        double angle;
        if (new LineEquation(line).getA() == 0) {
            if (line.getY().y() > line.getX().y()) {
                angle = Math.PI / 2;
            } else {
                angle = 3 * Math.PI / 2;
            }
        } else {
            angle = Math.atan((line.getY().y() - line.getX().y()) / (line.getY().x() - line.getX().x()));
            if (line.getY().x() - line.getX().x() < 0) {
                angle += Math.PI;
            }
        }
        return angle;
    }

    public static Line shortestWay(Point point, Line line) {
        LineEquation equation = new LineEquation(line);
        double angle = getAngle(line);
        LineEquation vert = new LineEquation(getLine(point, Math.PI / 2 + angle, 100));
        Point intersection = intersect(equation, vert);
        if (intersection != null && inLine(intersection, line)) {
            return new Line(point, intersection);
        } else {
            return new Line(point, (dist(point, line.getX()) < dist(point, line.getY()) ? line.getX() : line.getY()));
        }
    }
}
