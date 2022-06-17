package strategies;

import gamelogic.GameMap;
import gamelogic.GameState;
import gamelogic.Node;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class DepthFirstStrategy implements StrategyIf {
    private GameState initialState;
    private boolean visualizationEnabled = false;
    private Set<Integer> visitedStates = new HashSet<>();
    public int placementCounter = 0;
    public float numberOfChildNodesTotal = 0;
    public int backtrackingSteps = 0;

    /*
    the heuristic is here only for practical purposes, it's always null
     */
    public DepthFirstStrategy(GameState initialState, InformedDepthFirstStrategy.SelectionHeuristic heuristic) {
        this.initialState = new GameState(initialState);
    }

    private GameState solve(GameState state) throws UnsolvableGameStateException {
        if (visualizationEnabled) {
            state.visualize();
        }
        ChildStateIterator it = new ChildStateIterator(state);
        UnsolvableGameStateException lastError = null;

        /*
        try placing light bulbs as long a possible
         */
        while (it.hasNext()) {
            GameState childState;
            try {
                do {
                    childState = it.next();
                    numberOfChildNodesTotal++;
                } while (visitedStates.contains(childState.hashCode()));
            } catch (NoSuchElementException e) {
                throw new UnsolvableGameStateException("No more child states.");
            }

            /*
            if the puzzle is solved, quit solving and return the results
             */
            if (childState.isSolved()) {
                System.out.println("Solved! Placements: " + placementCounter);
                System.out.println("Average branching factor: " + numberOfChildNodesTotal / placementCounter);
                System.out.println("Backtracking steps: " + backtrackingSteps);
                System.out.println("Pruning/searching ratio: N/A");
                return childState;
            }

            try {
                return solve(childState);
            } catch (UnsolvableGameStateException e) {
                /*
                if this branch didn't work out, try the next
                 */
                lastError = e;
                if (visualizationEnabled) {
                    childState.visualize();
                    System.out.println("Backtracking...");
                }
                backtrackingSteps++;
            }

        }
        if (lastError != null) {
            throw new UnsolvableGameStateException(lastError);
        } else {
            throw new UnsolvableGameStateException("No child states.");
        }
    }

    @Override
    public void enableVisualization(boolean enable) {
        this.visualizationEnabled = enable;
    }

    @Override
    public GameState solve() throws UnsolvableGameStateException {
        return solve(initialState);
    }

    /*
    an iterator class for handling the nodes and game states while doing the depth first search
     */
    private class ChildStateIterator {
        private GameState parent;
        private Iterator<Node> nodeIterator;

        private ChildStateIterator(GameState parent) {
            this.parent = parent;
            nodeIterator = parent.getGameMap().nodeIterator(true);
        }

        public boolean hasNext() {
            return nodeIterator.hasNext();
        }

        public GameState next() {
            /*
            if no nodes are left the branch is left via Exception
             */
            if (!hasNext()) {
                visitedStates.add(parent.hashCode());
                throw new NoSuchElementException();
            }

            /*
            see if the node is relevant of bulb placement
             */
            Node node = nodeIterator.next();
            if (node.isWall() || node.isExcludingLightBulb() || node.isHasLightBulb() || node.isIlluminated()) {
                return next();
            }

            /*
            see if a bulb can be placed; if yes, place it and continue iterating through the nodes
             */
            if (parent.getGameMap().isLightBulbPlacementAllowed(node.getPosition())) {
                GameState state = new GameState(parent);

                GameMap.Position position = node.getPosition();
                state.getGameMap().placeLightBulb(position.x, position.y);
                placementCounter++;

                if (state.getGameMap().checkWallConstraint(true)) {
                    return state;
                } else {
                    visitedStates.add(state.hashCode());
                }
            }
            return next();
        }
    }
}
