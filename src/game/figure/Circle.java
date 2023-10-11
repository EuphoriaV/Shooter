package game.figure;

import game.view.Texture;

public record Circle(Point center, double radius, Texture texture) implements Figure {
}
