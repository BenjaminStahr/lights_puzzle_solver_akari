package gamelogic;

import java.util.*;

public class GameMap {
    public Map<Position, Node> map;
    private final int width;
    private final int height;

    /**
     * Copy constructor
     */
    public GameMap(GameMap otherMap) {
        this.width = otherMap.width;
        this.height = otherMap.height;
        map = new HashMap<>();
        otherMap.map.forEach((position, node) -> map.put(position, new Node(node)));
    }

    public GameMap(String[][] lines) {
        height = lines.length;
        width = lines[0].length;
        parseLines(lines);
    }

    private void parseLines(String[][] lines) {
        map = new HashMap<>();
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                Node node;
                Position position = new Position(i, j);
                switch (lines[j][i]) {
                    case "":
                        node = new Node(position, Node.WallType.NO_WALL);
                        break;
                    case "x":
                        node = new Node(position, Node.WallType.WALL_WITHOUT_NUMBER);
                        break;
                    case "0":
                        node = new Node(position, Node.WallType.WALL_WITH_NUMBER, 0);
                        break;
                    case "1":
                        node = new Node(position, Node.WallType.WALL_WITH_NUMBER, 1);
                        break;
                    case "2":
                        node = new Node(position, Node.WallType.WALL_WITH_NUMBER, 2);
                        break;
                    case "3":
                        node = new Node(position, Node.WallType.WALL_WITH_NUMBER, 3);
                        break;
                    case "4":
                        node = new Node(position, Node.WallType.WALL_WITH_NUMBER, 4);
                        break;
                    default:
                        throw new IllegalStateException(String.format("%s is not a valid node state", lines[i][j]));
                }
                map.put(position, node);
            }
        }
    }

    public List<Node> getNeighbours(int x, int y) {
        List<Node> list = new ArrayList<>(4);

        if (x - 1 >= 0)
            list.add(getNodeAt(x - 1, y));

        if (x + 1 < width)
            list.add(getNodeAt(x + 1, y));

        if (y - 1 >= 0)
            list.add(getNodeAt(x, y - 1));

        if (y + 1 < height)
            list.add(getNodeAt(x, y + 1));

        return list;
    }

    /**
     * Gets relevant (horizontal and vertical) neighbours for a given position.
     */
    public List<Node> getRelevantNodes(Position position, boolean includePosition) {
        int x = position.x;
        int y = position.y;
        List<Node> list = new ArrayList<>();

        if (includePosition) {
            list.add(getNodeAt(x, y));
        }

        for (int i = x + 1; i < width; i++) {
            // if wall stop
            Node n = getNodeAt(i, y);
            if (n.isWall())
                break;
            list.add(n);
        }

        for (int i = x - 1; i >= 0; i--) {
            // if wall stop
            Node n = getNodeAt(i, y);
            if (n.isWall())
                break;
            list.add(n);
        }


        for (int i = y + 1; i < height; i++) {
            // if wall stop
            Node n = getNodeAt(x, i);
            if (n.isWall())
                break;
            list.add(n);
        }

        for (int i = y - 1; i >= 0; i--) {
            // if wall stop
            Node n = getNodeAt(x, i);
            if (n.isWall())
                break;
            list.add(n);
        }

        return list;
    }

    public Node getNodeAt(int x, int y) {
        Position position = new Position(x, y);
        Node node = map.get(position);
        return node;
    }

    public boolean isLightBulbPlacementAllowed(Position position) {
        Node node = getNodeAt(position.x, position.y);
        if (node.isWall()) {
            return false;
        }
        if (node.isIlluminated()) {
            return false;
        }
        if (node.isHasLightBulb()) {
            return false;
        }
        if (node.isExcludingLightBulb()) {
            return false;
        }
        if (!satisfiesWallConstraints(node.getPosition())) {
            return false;
        }
        return true;
    }

    // checks for each neighbour if its a wall and when yes if its wall constraint would be satisfied if we place a light bulb
    // necessary for modeling the problem as a constraint satisfaction problem
    // expects valid position
    public boolean satisfiesWallConstraints(Position position) {
        boolean topTrue = true;
        Node topNode = getNodeAt(position.x, position.y - 1);
        if (topNode != null) {
            topTrue = checkWallConstraint(topNode.getPosition());
        }

        boolean downTrue = true;
        Node downNode = getNodeAt(position.x, position.y + 1);
        if (downNode != null) {
            downTrue = checkWallConstraint(downNode.getPosition());
        }

        boolean leftTrue = true;
        Node leftNode = getNodeAt(position.x - 1, position.y);
        if (leftNode != null) {
            leftTrue = checkWallConstraint(leftNode.getPosition());
        }

        boolean rightTrue = true;
        Node rightNode = getNodeAt(position.x + 1, position.y);
        if (rightNode != null) {
            rightTrue = checkWallConstraint(rightNode.getPosition());
        }
        return topTrue && rightTrue && leftTrue && downTrue;
    }

    // expects valid position
    public boolean checkWallConstraint(Position position) {
        Node node = getNodeAt(position.x, position.y);
        if (!(node.type == Node.WallType.WALL_WITH_NUMBER)) {
            return true;
        }
        if (getCurrentNumberOfLightBulbNeighbours(position) < node.requiredAdjacentBulbs) {
            return true;
        }
        else {
            return false;
        }
    }

    // expects valid position
    public int getCurrentNumberOfLightBulbNeighbours(Position position) {
        int counter = 0;
        Node upNode = getNodeAt(position.x, position.y-1);
        if (upNode != null && upNode.isHasLightBulb()) {
            counter++;
        }
        Node downNode = getNodeAt(position.x, position.y+1);
        if (downNode != null && downNode.isHasLightBulb()) {
            counter++;
        }
        Node leftNode = getNodeAt(position.x-1, position.y);
        if (leftNode != null && leftNode.isHasLightBulb()) {
            counter++;
        }
        Node rightNode = getNodeAt(position.x+1, position.y);
        if (rightNode != null && rightNode.isHasLightBulb()) {
            counter++;
        }
        return counter;
    }

    public void placeLightBulb(int x, int y) {
        Node node = getNodeAt(x, y);
        if (node.getType() != Node.WallType.NO_WALL) {
            throw new IllegalStateException("Cannot place light bulb on wall.");
        }
        if (node.isIlluminated()) {
            throw new IllegalStateException("Cannot place light bulb on illuminated node.");
        }
        if (node.isHasLightBulb()) {
            throw new IllegalStateException("Cannot place light bulb on node with light bulb.");
        }
        if (node.isExcludingLightBulb()) {
            throw new IllegalStateException("Cannot place light bulb on exclusion node.");
        }

        node.setHasLightBulb(true);
        List<Node> relevantNodes = getRelevantNodes(node.getPosition(), true);
        relevantNodes.forEach(Node::illuminate);
    }

    // win condition
    public boolean checkConstraints() {
        return checkWallConstraint(false) && checkIllumination();
    }

    public boolean checkWallConstraint(boolean allowLessThanRequiredBulbs) {
        boolean isValid = true;

        for (Node node : map.values()) {
            Position position = node.getPosition();
            if (node.getType() == Node.WallType.WALL_WITH_NUMBER) {
                int requiredAdjacentBulbs = node.getRequiredAdjacentBulbs();
                int adjacentBulbs = 0;
                for (Node neighbour : getNeighbours(position.x, position.y)) {
                    if (neighbour.isHasLightBulb()) {
                        adjacentBulbs++;
                    }
                }
                if (allowLessThanRequiredBulbs && adjacentBulbs > requiredAdjacentBulbs) {
                    isValid = false;
                    break;
                } else if (!allowLessThanRequiredBulbs && requiredAdjacentBulbs != adjacentBulbs) {
                    isValid = false;
                    break;
                }
            }
        }
        return isValid;
    }

    private boolean checkIllumination() {
        boolean isValid = true;
        // check for all Nodes with wallNumber -2 if they are illuminated
        for (Node node : map.values()) {
            if (node.getType() == Node.WallType.NO_WALL) {
                if (!node.isIlluminated()) {
                    isValid = false;
                    break;
                }
            }
        }
        return isValid;
    }

    public int getSize() {return map.size();}

    public void visualize() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Node node = getNodeAt(x, y);

                switch (node.getType()) {
                    case NO_WALL:
                        if (node.isHasLightBulb()) {
                            System.out.print("B");
                        } else if (node.isExcludingLightBulb()) {
                            System.out.print("x");
                            if (node.isIlluminated()) {
                                System.out.print("\u0303");
                            }
                        } else {
                            System.out.print("_");
                            if (node.isIlluminated()) {
                                System.out.print("\u0303");
                            }
                        }
                        break;
                    case WALL_WITHOUT_NUMBER:
                        System.out.print("W");
                        break;
                    case WALL_WITH_NUMBER:
                        System.out.print(node.getRequiredAdjacentBulbs());
                        break;
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    public Iterator<Node> nodeIterator() {
        return nodeIterator(false);
    }

    public Iterator<Node> nodeIterator(boolean shuffle) {
        if (shuffle) {
            List<Node> l = new ArrayList<>(map.values());
            Collections.shuffle(l);
            return l.iterator();
        } else {
            return map.values().iterator();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameMap gameMap = (GameMap) o;
        return width == gameMap.width && height == gameMap.height && Objects.equals(map, gameMap.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map, width, height);
    }


    public static class Position {
        public final int x;
        public final int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Position position = (Position) o;
            return x == position.x && y == position.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public String toString() {
            return "Position{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }
}
