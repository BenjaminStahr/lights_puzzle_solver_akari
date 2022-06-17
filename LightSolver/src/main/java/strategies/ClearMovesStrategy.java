package strategies;

import gamelogic.GameMap;
import gamelogic.GameState;
import gamelogic.Node;

import java.util.*;

public class ClearMovesStrategy implements StrategyIf {
    private GameState initialState;
    private boolean visualizationEnabled = false;
    private Set<Node> finishedNodes = new HashSet<>();
    public int placementCounter = 0;
    public int pruningCount = 0;
    Set<Node> seenNodes = new HashSet<>();

    /*
    the heuristic is here only for practical purposes, it's always null
     */
    public ClearMovesStrategy(GameState initialState, InformedDepthFirstStrategy.SelectionHeuristic heuristic) {
        this.initialState = new GameState(initialState);
    }

    private GameState solve(GameState state) {

        while (!state.isSolved()) {
            if (visualizationEnabled)
                state.visualize();

            solveClearMoves(state);
        }
        System.out.println("Solved! Placements: " + placementCounter);
        System.out.println("Average branching factor: N/A");
        System.out.println("Backtracking steps: N/A");
        System.out.println("Pruning operations: " + pruningCount);
        return state;
    }


    /*
     the wall number constraints 0 and 4 always have clear bulb placement/exclusion
     the other wall numbers can also lead to clear placements if there are more walls around them
     --> they can be solved once initially
     */
    private GameState solveInitialClearMoves(GameState state) {
        Iterator<Node> nodeIterator = state.getGameMap().nodeIterator();
        while (nodeIterator.hasNext()) {
            Node n = nodeIterator.next();

            if (n.getType() == Node.WallType.WALL_WITH_NUMBER) {
                List<Node> wallNeighbours = state.getGameMap().getNeighbours(n.getPosition().x, n.getPosition().y);
                int requiredBulbs = n.getRequiredAdjacentBulbs();
                switch (requiredBulbs) {
                    case 0:
                        for (Node wn : wallNeighbours) {

                            if (!wn.isExcludingLightBulb()) {
                                wn.setExcludingLightBulb(true);
                                pruningCount++;
                            }
                        }
                        continue;

                    case 1:
                    case 2:
                    case 3:
                    case 4:

                        int wnWalls = 0;
                        for (Node wn : wallNeighbours) {
                            if (wn.isWall()) {
                                wnWalls++;
                            }
                        }

                        if (wallNeighbours.size() - wnWalls == requiredBulbs) {

                            for (Node wn : wallNeighbours) {
                                if (!wn.isWall()) {
                                    state.getGameMap().placeLightBulb(wn.getPosition().x, wn.getPosition().y);
                                    placementCounter++;
                                }
                            }
                        }
                }
            }
        }
        return state;
    }

    /*
     this method must not create illegal states!
     */
    private GameState solveClearMoves(GameState state) {

        List<Node> nodes = new ArrayList<>();
        Iterator<Node> nodeIterator = state.getGameMap().nodeIterator();
        while (nodeIterator.hasNext()) {
            Node n = nodeIterator.next();
            if (finishedNodes.contains(n)) {
                continue;
            }
            nodes.add(n);
        }
        /*
         shuffling avoids always looking at the same nodes
         */
        Collections.shuffle(nodes);

        for (Node n : nodes) {
            GameMap.Position position = n.getPosition();

            List<Node> relevantNodes = state.getGameMap().getRelevantNodes(position, true);

            /*
             if it still needs to be illuminated:
             check whether there may be only one possible Node to do so
             */
            if (!n.isIlluminated() && !n.isWall() && !n.isHasLightBulb()) {
                int possibilities = 0;
                Node lastPossibility = null;
                for (Node rn : relevantNodes) {
                    if (!rn.isExcludingLightBulb() && !rn.isHasLightBulb() && !rn.isIlluminated()) { //wall ist sowieso nicht relevant
                        possibilities++;
                        lastPossibility = rn;
                    }
                }

                if (possibilities == 1) {
                    state.getGameMap().placeLightBulb(lastPossibility.getPosition().x, lastPossibility.getPosition().y);
                    placementCounter++;
                    finishedNodes.add(n);
                    return state;
                }
            }

            /*
             if it is a wall with constraint:
             check the neighbours transitively; maybe it can be/is already solved
             */
            if (n.getType() == Node.WallType.WALL_WITH_NUMBER) {
                List<Node> wallNeighbours = state.getGameMap().getNeighbours(position.x, position.y);

                int requiredBulbs = n.getRequiredAdjacentBulbs();
                int nExclusions = 0;

                for (Node wn : wallNeighbours) {
                    if (wn.isHasLightBulb()) {
                        requiredBulbs--;
                        nExclusions++;
                    } else if (wn.isWall() || wn.isExcludingLightBulb() || wn.isIlluminated()) {
                        nExclusions++;
                    }
                }


                /*
                 when the required bulbs are already there, the rest can be excluded
                 */
                if (requiredBulbs == 0) {
                    for (Node wn : wallNeighbours) {
                        if (!wn.isHasLightBulb() && !wn.isWall()) {
                            wn.setExcludingLightBulb(true);
                            pruningCount++;
                        }

                    }
                    finishedNodes.add(n);
                    return state;
                }

                /*
                 when the free Nodes that are left match the needed bulbs exactly, they can be placed
                 */
                if (wallNeighbours.size() - nExclusions == requiredBulbs) {

                    for (Node wn : wallNeighbours) {
                        if (state.getGameMap().isLightBulbPlacementAllowed(wn.getPosition())) {
                            state.getGameMap().placeLightBulb(wn.getPosition().x, wn.getPosition().y);
                            placementCounter++;
                        } else if (!wn.isHasLightBulb() && !wn.isWall() && !wn.isExcludingLightBulb()) {
                            wn.setExcludingLightBulb(true);
                            pruningCount++;
                        }

                    }
                    finishedNodes.add(n);
                    return state;
                }
                switch (requiredBulbs) {
                    /*
                     case 0 & 4 are unnecessary, because they are solved initially
                     */

                    case 1:
                    case 2:

                        /*
                         when there is one more free Node than required bulbs, in certain cases there can be exclusions
                         */
                        if (wallNeighbours.size() - nExclusions == requiredBulbs + 1) {
                            /*
                             if 2 neighbors next to each other are free, exclude between them
                             (--> exclude overlapping relevant nodes)
                             */

                            Set<Node> allRelevantNodes = new HashSet<>();
                            for (Node wn : wallNeighbours) {
                                if (!wn.isWall() && !wn.isIlluminated() && !wn.isHasLightBulb() && !wn.isExcludingLightBulb()) {
                                    relevantNodes = state.getGameMap().getRelevantNodes(wn.getPosition(), false);
                                    for (Node rn : relevantNodes) {
                                        if (!rn.isExcludingLightBulb() && !rn.isHasLightBulb() && !rn.isIlluminated()) {
                                            if (!allRelevantNodes.add(rn)) {
                                                rn.setExcludingLightBulb(true);
                                                pruningCount++;
                                            }
                                        }
                                    }
                                }
                            }

                        }
                        break;


                    case 3:
                        /*
                         exclude (x-1,y-1),(x-1,y+1),(x+1,y-1),(x+1,y+1)
                         (but only do this once, otherwise it's inefficient)
                         */
                        if (!seenNodes.contains(n)) {
                            state.getGameMap().getNodeAt(position.x - 1, position.y - 1).setExcludingLightBulb(true);
                            state.getGameMap().getNodeAt(position.x - 1, position.y + 1).setExcludingLightBulb(true);
                            state.getGameMap().getNodeAt(position.x + 1, position.y - 1).setExcludingLightBulb(true);
                            state.getGameMap().getNodeAt(position.x + 1, position.y + 1).setExcludingLightBulb(true);
                            pruningCount += 4;
                            seenNodes.add(n);
                            return state;
                        }

                }
            }
        }

        return state;
    }

    @Override
    public void enableVisualization(boolean enable) {
        this.visualizationEnabled = enable;
    }

    @Override
    public GameState solve() throws UnsolvableGameStateException {
        GameState state = solveInitialClearMoves(initialState);
        System.out.println("Initially clear moves:");
        state.getGameMap().visualize();
        return solve(state);
    }

}

