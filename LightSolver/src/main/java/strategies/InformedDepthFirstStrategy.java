package strategies;

import gamelogic.*;

import java.util.List;

public class InformedDepthFirstStrategy implements StrategyIf {

    public enum SelectionHeuristic {
        MOST_LIGHT_UP,
        MOST_WALLS,
        MOST_EXCLUDED_BUT_FREE_FIRST,
        MOST_WALLS_HIGH_NUMBERS
    }

    // used to calculate the average branching factor (average number of children), is used as quality measure
    public float numberOfChildNodesTotal = 0;
    public int backtrackingSteps = 0;
    public int searchBulbs = 0;
    public int pruningBulbs = 0;

    GameState root;
    GameState solvedState;

    SelectionHeuristic chosenSelectionHeuristic;

    public InformedDepthFirstStrategy(GameState root, SelectionHeuristic heuristic) {
        this.root = root;
        this.chosenSelectionHeuristic = heuristic;
    }

    private GameState solve(GameState currentState) throws UnsolvableGameStateException
    {
        if (currentState.isSolved())
        {
            solvedState = currentState;
            System.out.println("Average branching factor: " + numberOfChildNodesTotal / searchBulbs);
            System.out.println("Backtracking steps: " + backtrackingSteps);
            System.out.println("Number of placed searching bulbs: "+ searchBulbs);
            System.out.println("Number of placed pruning bulbs: " + pruningBulbs);
            System.out.println("Ratio between pruning and searching bulbs: "+ ((float)searchBulbs)/((float) pruningBulbs));
            return solvedState;
        }
        try
        {
            int nextIndex = -1;
            currentState.currentIndexPointer++;
            if (currentState.currentIndexPointer < currentState.possiblePlacementPositions.size()) {
                nextIndex = currentState.possiblePlacementPositions.get(currentState.currentIndexPointer).index;
            }
            // iterates through the children of a node
            while (nextIndex != -1 && !currentState.isSolved())
            {
                // comment this in if you want to see, how far the search has already gone
                /*if (searchBulbs % 1000 == 0) {
                    printCounters(currentState);
                }*/
                GameState nextState = new GameState(currentState);
                Object key = currentState.gameMap.map.keySet().toArray()[nextIndex];
                Node nextNode = currentState.gameMap.map.get(key);
                // choice for the next node
                nextState.gameMap.placeLightBulb(nextNode.getPosition().x, nextNode.getPosition().y);
                searchBulbs++;
                // clear moves strategy iterative, kind of the arc consistency algorithm
                int placedBulbs;
                do {
                    placedBulbs = getNumOfPlacedBulbs(nextState);
                    nextState = ClearMoves.solve(nextState);
                    pruningBulbs = pruningBulbs + (getNumOfPlacedBulbs(nextState)-placedBulbs);
                } while (placedBulbs < getNumOfPlacedBulbs(nextState));
                applyChosenSelectionHeuristic(nextState);
                // dead end detection, avoids unsolvable child states
                if (ClearMoves.checkStateSolvable(nextState)) {
                    solvedState = solve(nextState);
                    backtrackingSteps++;
                }
                currentState.currentIndexPointer++;
                if (currentState.currentIndexPointer < currentState.possiblePlacementPositions.size()) {
                    nextIndex = currentState.possiblePlacementPositions.get(currentState.currentIndexPointer).index;
                }
                else {
                    nextIndex = -1;
                }
                if (solvedState != null && solvedState.isSolved())
                {
                    return solvedState;
                }
            }
            if (currentState.isSolved())
            {
                solvedState = currentState;
                return solvedState;
            }
            else
            {
                throw new UnsolvableGameStateException("No more child states.");
            }
        }
        catch (UnsolvableGameStateException ignored) {}
        return solvedState;
    }

    // gives an overview at which point the algorithm is at runtime, just for testing purposes
    public void printCounters(GameState state) {
        GameState p = state;
        while (p != null)
        {
            System.out.println("Depth: "+ getNumParents(p)+ " Pointer: "+ p.currentIndexPointer);
            p = p.parent;
        }
    }
    public int getNumParents(GameState state)
    {
        int counter = 0;
        GameState p = state.parent;
        while (p != null)
        {
            counter++;
            p = p.parent;
        }
        return counter;
    }

    public int getNumOfPlacedBulbs(GameState state) {
        int counter = 0;
        for (int i = 0; i < state.getGameMap().getSize(); i++ ) {
            Object key = state.gameMap.map.keySet().toArray()[i];
            Node nextNode = state.gameMap.map.get(key);
            if (nextNode.isHasLightBulb()) {
                counter++;
            }
        }
        return counter;
    }

    @Override
    public void enableVisualization(boolean enable) {}

    @Override
    public GameState solve() throws UnsolvableGameStateException {
        // sets obligatory bulbs around walls and inside holes
        preprocessObligatoryWallConstraints(root.gameMap);
        preprocessHoles(root.gameMap);
        // tries to derive additional bulb placements from already set ones
        int preprocessPlacedBulbs;
        do {
            preprocessPlacedBulbs = getNumOfPlacedBulbs(root);
            root = ClearMoves.solve(root);
        } while (preprocessPlacedBulbs < getNumOfPlacedBulbs(root));
        System.out.println("preprocessing constraints");
        System.out.println("already placed bulbs: "+ getNumOfPlacedBulbs(root));
        root.visualize();
        System.out.println("##########################");
        pruningBulbs += getNumOfPlacedBulbs(root);
        // orders the possible child nodes of root corresponding to chosen heuristic
        applyChosenSelectionHeuristic(root);
        return solve(root);
    }

    public void applyChosenSelectionHeuristic(GameState state) {
        switch (chosenSelectionHeuristic) {
            case MOST_LIGHT_UP -> setPlacementIndexesMostLightUp(state);
            case MOST_WALLS -> setPlacementIndexesWallBased(state);
            case MOST_EXCLUDED_BUT_FREE_FIRST -> setPlacementIndexesBasedOnExclusionsAndWalls(state);
            case MOST_WALLS_HIGH_NUMBERS -> setPlacementIndexesHighWallBased(state);
        }
    }


    // possible selection heuristic, maximizes light up fields
    public void setPlacementIndexesMostLightUp(GameState state) {
        int index = 0;
        while (index != -1) {
            index = getNextIndex(state, index);
            if (index != -1) {
                int currentLightUpFields = getLightUpFields(state);
                GameState tempState = new GameState(state);
                Object key = tempState.gameMap.map.keySet().toArray()[index];
                Node nextNode = tempState.gameMap.map.get(key);
                tempState.gameMap.placeLightBulb(nextNode.getPosition().x, nextNode.getPosition().y);
                int placedLightUpFields = getLightUpFields(tempState);
                state.possiblePlacementPositions.add(new WeightedIndexPair(index, placedLightUpFields - currentLightUpFields));
            } else {
                break;
            }
            index++;
        }
        state.possiblePlacementPositions.sort(new InformedSearchSorter());
        if (state.possiblePlacementPositions.size() > 0) {
            numberOfChildNodesTotal += state.possiblePlacementPositions.size();
        }
    }

    // possible selection heuristic, takes positions with many walls first
    public void setPlacementIndexesWallBased(GameState state) {
        int index = 0;
        while (index != -1) {
            index = getNextIndex(state, index);
            if (index != -1) {
                Object key = state.gameMap.map.keySet().toArray()[index];
                Node nextNode = state.gameMap.map.get(key);
                List<Node> neighbours = state.getGameMap().getNeighbours(nextNode.position.x, nextNode.position.y);
                int wallNeighbours = 0;
                for (Node rn : neighbours) {
                    if (rn.isWall()) {
                        wallNeighbours++;
                    }
                }
                state.possiblePlacementPositions.add(new WeightedIndexPair(index, wallNeighbours));
            } else {
                break;
            }
            index++;
        }
        state.possiblePlacementPositions.sort(new InformedSearchSorter());
        if (state.possiblePlacementPositions.size() > 0) {
            numberOfChildNodesTotal += state.possiblePlacementPositions.size();
        }
    }

    // hybrid heuristic which decides on lit up exclusions and prefers bulbs not near walls
    public void setPlacementIndexesBasedOnExclusionsAndWalls(GameState state) {
        int index = 0;
        while (index != -1) {
            index = getNextIndex(state, index);
            if (index != -1) {
                int currentExclusions = getExclusionsNotLitUp(state);
                GameState tempState = new GameState(state);
                Object key = tempState.gameMap.map.keySet().toArray()[index];
                Node nextNode = tempState.gameMap.map.get(key);
                tempState.gameMap.placeLightBulb(nextNode.getPosition().x, nextNode.getPosition().y);
                int exclusionsLitUp = currentExclusions-getExclusionsNotLitUp(tempState);
                List<Node> neighbours = state.getGameMap().getNeighbours(nextNode.position.x, nextNode.position.y);
                int wallNeighbours = 0;
                for (Node rn : neighbours) {
                    if (rn.isWall()) {
                        wallNeighbours++;
                    }
                }
                state.possiblePlacementPositions.add(new WeightedIndexPair(index, wallNeighbours != 0 ? 2 * exclusionsLitUp - 1 : 2 * exclusionsLitUp));
            } else {
                break;
            }
            index++;
        }
        state.possiblePlacementPositions.sort(new InformedSearchSorter());
        if (state.possiblePlacementPositions.size() > 0) {
            numberOfChildNodesTotal += state.possiblePlacementPositions.size();
        }
    }

    // possible selection heuristic, prefers many walls with high numbers
    public void setPlacementIndexesHighWallBased(GameState state) {
        int index = 0;
        while (index != -1) {
            index = getNextIndex(state, index);
            if (index != -1) {
                Object key = state.gameMap.map.keySet().toArray()[index];
                Node nextNode = state.gameMap.map.get(key);
                List<Node> neighbours = state.getGameMap().getNeighbours(nextNode.position.x, nextNode.position.y);
                int wallMeasure = 0;
                for (Node rn : neighbours) {
                    if (rn.isWall()) {
                        wallMeasure++;
                        if (rn.type == Node.WallType.WALL_WITH_NUMBER) {
                            wallMeasure += rn.requiredAdjacentBulbs;
                        }
                    }
                }
                state.possiblePlacementPositions.add(new WeightedIndexPair(index, wallMeasure));
            } else {
                break;
            }
            index++;
        }
        state.possiblePlacementPositions.sort(new InformedSearchSorter());
        if (state.possiblePlacementPositions.size() > 0) {
            numberOfChildNodesTotal += state.possiblePlacementPositions.size();
        }
    }

    public int getExclusionsNotLitUp(GameState state) {
        int exclusionCounter = 0;
        for (int i = 0; i < state.gameMap.map.size(); i++) {
            Object key = state.gameMap.map.keySet().toArray()[i];
            Node nextNode = state.gameMap.map.get(key);
            if (!nextNode.isIlluminated() && nextNode.isExcludingLightBulb()) {
                exclusionCounter++;
            }
        }
        return exclusionCounter;
    }

    public int getLightUpFields (GameState state) {
        int lightCounter = 0;
        for (int i = 0; i < state.gameMap.map.size(); i++) {
            Object key = state.gameMap.map.keySet().toArray()[i];
            Node nextNode = state.gameMap.map.get(key);
            if (nextNode.isIlluminated()) {
                lightCounter++;
            }
        }
        return lightCounter;
    }

    public int getNextIndex(GameState state, int index)
    {
        if (index < state.getGameMap().getSize() && index > -1)
        {

            Object key = state.gameMap.map.keySet().toArray()[index];
            Node current = state.gameMap.map.get(key);

            // the conditions here guarantee that only valid positions are choosen for the search, so we
            // ensure, that we solve the light puzzles as CSP, additionally this reduces the branching factor, which increases performance
            while (((current.isWall() ||
                    current.isExcludingLightBulb() ||
                    current.isHasLightBulb() ||
                    current.isIlluminated()) ||
                    !(state.getGameMap().satisfiesWallConstraints(current.getPosition())))
                    && index < state.getGameMap().getSize()-1)
            {
                index += 1;
                key = state.gameMap.map.keySet().toArray()[index];
                current = state.gameMap.map.get(key);
            }
            if (state.getGameMap().isLightBulbPlacementAllowed(current.getPosition()))
            {
                return index;
            }
        }
        return -1;
    }

    //######################################################################
    //following methods are just for preprocessing, before the search starts

    // hole preprocessing
    // if there is a hole somewhere, just fill it, there has to be a light bulb inside
    public void preprocessHoles(GameMap field) {
        Object[] arrayMap = field.map.keySet().toArray();
        for (int i = 0; i < arrayMap.length; i++)
        {
            Object key = field.map.keySet().toArray()[i];
            Node node = field.map.get(key);
            boolean upTrue = false;
            Node upNode = field.getNodeAt(node.getPosition().x, node.getPosition().y-1);
            if (upNode == null || upNode.isWall()) {
                upTrue = true;
            }

            boolean downTrue = false;
            Node downNode = field.getNodeAt(node.getPosition().x, node.getPosition().y+1);
            if (downNode == null || downNode.isWall()) {
                downTrue = true;
            }
            boolean leftTrue = false;
            Node leftNode = field.getNodeAt(node.getPosition().x-1, node.getPosition().y);
            if (leftNode == null || leftNode.isWall()) {
                leftTrue = true;
            }
            boolean rightTrue = false;
            Node rightNode = field.getNodeAt(node.getPosition().x+1, node.getPosition().y);
            if (rightNode == null || rightNode.isWall()) {
                rightTrue = true;
            }
            if (rightTrue &&
            leftTrue &&
            upTrue &&
            downTrue &&
            field.isLightBulbPlacementAllowed(node.getPosition()) &&
            field.satisfiesWallConstraints(node.getPosition())) {
                field.placeLightBulb(node.getPosition().x, node.getPosition().y);
            }
        }
    }


    // wall number preprocessing
    // if we have a 4 wall we can place light bulbs all around it, same with a three wall with another wall or noting on one site and so on
    public void preprocessObligatoryWallConstraints(GameMap field) {
        Object[] arrayMap = field.map.keySet().toArray();
        for (int i = 0; i < arrayMap.length; i++) {
            Object key = field.map.keySet().toArray()[i];
            Node node = field.map.get(key);
            if (checkIfLightBulbsAroundPositionObligatory(field, node.getPosition())) {
                Node upNode = field.getNodeAt(node.getPosition().x, node.getPosition().y-1);
                if (upNode != null && !upNode.isWall()) {
                    field.placeLightBulb(upNode.getPosition().x, upNode.getPosition().y);
                }
                Node downNode = field.getNodeAt(node.getPosition().x, node.getPosition().y+1);
                if (downNode != null && !downNode.isWall()) {
                    field.placeLightBulb(downNode.getPosition().x, downNode.getPosition().y);
                }
                Node leftNode = field.getNodeAt(node.getPosition().x-1, node.getPosition().y);
                if (leftNode != null && !leftNode.isWall()) {
                    field.placeLightBulb(leftNode.getPosition().x, leftNode.getPosition().y);
                }
                Node rightNode = field.getNodeAt(node.getPosition().x+1, node.getPosition().y);
                if (rightNode != null && !rightNode.isWall()) {
                    field.placeLightBulb(rightNode.getPosition().x, rightNode.getPosition().y);
                }
            }
        }
    }

    public boolean checkIfLightBulbsAroundPositionObligatory(GameMap field, GameMap.Position position) {
        int maxCounter = 0;
        Node upNode = field.getNodeAt(position.x, position.y-1);
        if (upNode != null && upNode.type == Node.WallType.NO_WALL) {
            maxCounter++;
        }
        Node downNode = field.getNodeAt(position.x, position.y+1);
        if (downNode != null && downNode.type == Node.WallType.NO_WALL) {
            maxCounter++;
        }
        Node leftNode = field.getNodeAt(position.x - 1, position.y);
        if (leftNode != null && leftNode.type == Node.WallType.NO_WALL) {
            maxCounter++;
        }
        Node rightNode = field.getNodeAt(position.x+1, position.y);
        if (rightNode != null && rightNode.type == Node.WallType.NO_WALL) {
            maxCounter++;
        }
        Node node = field.getNodeAt(position.x, position.y);
        return maxCounter == node.requiredAdjacentBulbs;
    }
}
