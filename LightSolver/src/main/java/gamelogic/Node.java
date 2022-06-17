package gamelogic;

import java.util.Objects;

public class Node {

    public enum WallType {
        NO_WALL,
        WALL_WITHOUT_NUMBER,
        WALL_WITH_NUMBER

    }

    public final GameMap.Position position;
    public final WallType type;

    // how many adjacent bulbs are around the node (any number between -1 and 4 with -1 meaning unknown).
    // important if the type of the node is WALL_WITH_NUMBER
    public final int requiredAdjacentBulbs;


    private boolean hasLightBulb = false;
    private int illuminationCounter = 0;
    private boolean excludingLightBulb = false;

    public Node(Node otherNode) {
        this.position = otherNode.getPosition();
        this.type = otherNode.type;
        this.requiredAdjacentBulbs = otherNode.requiredAdjacentBulbs;
        this.hasLightBulb = otherNode.hasLightBulb;
        this.illuminationCounter = otherNode.illuminationCounter;
        this.excludingLightBulb = otherNode.excludingLightBulb;
    }

    public Node(GameMap.Position position, WallType type, int requiredAdjacentBulbs) {
        this.position = position;
        this.type = type;
        this.requiredAdjacentBulbs = requiredAdjacentBulbs;
    }

    public Node(GameMap.Position position, WallType type) {
        this(position, type, -1);
    }

    public boolean isHasLightBulb() {
        return hasLightBulb;
    }

    public void setHasLightBulb(boolean hasLightBulb) {
        this.hasLightBulb = hasLightBulb;
    }

    public boolean isIlluminated() {
        return illuminationCounter > 0;
    }

    public void illuminate() {
        illuminationCounter++;
    }

    public boolean isExcludingLightBulb() {
        return excludingLightBulb;
    }

    public void setExcludingLightBulb(boolean excludingLightBulb) {
        this.excludingLightBulb = excludingLightBulb;
    }

    public boolean isWall() {
        return type == WallType.WALL_WITH_NUMBER || type == WallType.WALL_WITHOUT_NUMBER;
    }

    public WallType getType() {
        return type;
    }

    public int getRequiredAdjacentBulbs() {
        return requiredAdjacentBulbs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return requiredAdjacentBulbs == node.requiredAdjacentBulbs && hasLightBulb == node.hasLightBulb && illuminationCounter == node.illuminationCounter && excludingLightBulb == node.excludingLightBulb && Objects.equals(position, node.position) && type == node.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, type, requiredAdjacentBulbs, hasLightBulb, illuminationCounter, excludingLightBulb);
    }

    public GameMap.Position getPosition() {
        return position;
    }
}
