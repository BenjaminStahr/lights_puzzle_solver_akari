package strategies;

import gamelogic.GameMap;
import gamelogic.GameState;
import gamelogic.Node;

import java.util.*;

public class ClearMoves {

    // this method tries to find necessary required bulb placements for this game state and places them, if it finds some
    public static GameState solve(GameState state) throws UnsolvableGameStateException {
        Object[] mapAsArray = state.gameMap.map.keySet().toArray();
        for (int i = 0; i < mapAsArray.length; i++)
        {
            Node n = state.gameMap.map.get(mapAsArray[i]);
            GameMap.Position position = n.getPosition();
            List<Node> relevantNodes = state.getGameMap().getRelevantNodes(position, true);

            // if it still needs to be illuminated:
            // check whether there may be only one possible Node to do so
            // if on vertical and horizontal axis is just one possible placement to light this field then we can place the light bulb
            // heuristic 1
            if (!n.isIlluminated() && !n.isWall() && !n.isHasLightBulb()) {
                int possibilities = 0;
                Node lastPossibility = null;
                for (Node rn : relevantNodes) {
                    if (!rn.isExcludingLightBulb() && !rn.isHasLightBulb() && !rn.isIlluminated()) {
                        possibilities++;
                        lastPossibility = rn;
                    }
                }

                if (possibilities == 1) {
                    state.getGameMap().placeLightBulb(lastPossibility.getPosition().x, lastPossibility.getPosition().y);
                // no need for further calculation, when there are no possibilities
                } else if (possibilities == 0) {
                    break;
                }
            }

            // if it is a wall with constraint
            // check the neighbours transitively; maybe it can be/is already solved
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

                // the required bulbs are already there, the rest can be excluded
                if (requiredBulbs == 0) {
                    for (Node wn : wallNeighbours) {
                        if (!wn.isHasLightBulb() && !wn.isWall()) {
                            wn.setExcludingLightBulb(true);
                        }

                    }
                }

                // the Nodes that are left match the needed bulbs exactly
                if (wallNeighbours.size() - nExclusions == requiredBulbs) {

                    for (Node wn : wallNeighbours) {
                        if (state.getGameMap().isLightBulbPlacementAllowed(wn.getPosition())) {
                            state.getGameMap().placeLightBulb(wn.getPosition().x, wn.getPosition().y);
                        } else if (!wn.isHasLightBulb() && !wn.isWall()) {
                            wn.setExcludingLightBulb(true);
                        }

                    }
                }
                switch (requiredBulbs) {
                    // if there is no need for more light bulbs, just exclude it
                    case 0:
                        for (Node wn : wallNeighbours) {

                            if (!wn.isExcludingLightBulb()) {
                                wn.setExcludingLightBulb(true);
                            }
                        }
                        break;
                    case 1:

                    case 2:
                        if (wallNeighbours.size() - nExclusions == requiredBulbs + 1) {
                            // if 2 neighbors next to each other free, exclude between
                            // exclude overlapping relevant nodes
                            Set<Node> allRelevantNodes = new HashSet<>();
                            for (Node wn : wallNeighbours) {
                                if (!wn.isWall() && !wn.isIlluminated() && !wn.isHasLightBulb() && !wn.isExcludingLightBulb()) {
                                    relevantNodes = state.getGameMap().getRelevantNodes(wn.getPosition(), false);
                                    for (Node rn : relevantNodes) {
                                        if (!rn.isExcludingLightBulb() && !rn.isHasLightBulb() && !rn.isIlluminated()) {
                                            if (!allRelevantNodes.add(rn)) {
                                                rn.setExcludingLightBulb(true);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    case 3:
                        //exclude (x-1,y-1),(x-1,y+1),(x+1,y-1),(x+1,y+1)
                        // exclude corners
                        state.getGameMap().getNodeAt(position.x - 1, position.y - 1).setExcludingLightBulb(true);
                        state.getGameMap().getNodeAt(position.x - 1, position.y + 1).setExcludingLightBulb(true);
                        state.getGameMap().getNodeAt(position.x + 1, position.y - 1).setExcludingLightBulb(true);
                        state.getGameMap().getNodeAt(position.x + 1, position.y + 1).setExcludingLightBulb(true);
                        break;

                    case 4:
                        state.getGameMap().getNodeAt(position.x - 1, position.y - 1).setExcludingLightBulb(true);
                        state.getGameMap().getNodeAt(position.x - 1, position.y + 1).setExcludingLightBulb(true);
                        state.getGameMap().getNodeAt(position.x + 1, position.y - 1).setExcludingLightBulb(true);
                        state.getGameMap().getNodeAt(position.x + 1, position.y + 1).setExcludingLightBulb(true);
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
                                }
                            }
                        }
                }
            }
        }
        return state;
    }

    // this is our dead end detection, sometimes a solution gets into an invalid state, even when the partial solution is correct, in this case we
    // cancel further search in this child, because here a correct solution can't be found
    public static boolean checkStateSolvable(GameState state) {
        Object[] mapAsArray = state.gameMap.map.keySet().toArray();
        for (int i = 0; i < mapAsArray.length; i++) {
            Node n = state.gameMap.map.get(mapAsArray[i]);
            GameMap.Position position = n.getPosition();
            List<Node> relevantNodes = state.getGameMap().getRelevantNodes(position, true);

            // if it still needs to be illuminated:
            // check whether there may be only one possible Node to do so
            // if on vertical and horizontal axis is just one possible placement to light this field then we can place the light bulb
            // heuristic 1
            if (!n.isIlluminated() && !n.isWall() && !n.isHasLightBulb()) {
                int possibilities = 0;
                for (Node rn : relevantNodes) {
                    if (!rn.isExcludingLightBulb() && !rn.isHasLightBulb() && !rn.isIlluminated()) { //wall ist sowieso nicht relevant
                        possibilities++;
                    }
                }
                if (possibilities == 0) {
                    return false;
                }
            }
        }

        // check if wall number can't be achieved anymore
        for (int i = 0; i < mapAsArray.length; i++) {
            Node n = state.gameMap.map.get(mapAsArray[i]);
            GameMap.Position position = n.getPosition();
            //List<Node> relevantNodes = state.getGameMap().getRelevantNodes(position, true);

            if (n.isWall()) {
                List<Node> neighbours = state.getGameMap().getNeighbours(position.x, position.y);
                int freeSpaces = 0;
                int satisfiedBulbs = 0;
                for (Node rn : neighbours) {
                    if (!rn.isExcludingLightBulb() && !rn.isHasLightBulb() && !rn.isIlluminated() && !rn.isWall()) {
                        freeSpaces++;
                    }
                    if (rn.isHasLightBulb()) {
                        satisfiedBulbs++;
                    }
                }
                int neededLightBulbs = n.requiredAdjacentBulbs - satisfiedBulbs;
                if (freeSpaces < neededLightBulbs) {
                    return false;
                }
            }
        }
        return true;
    }
}

