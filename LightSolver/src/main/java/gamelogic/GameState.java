package gamelogic;

import strategies.WeightedIndexPair;

import java.util.ArrayList;

public class GameState {
    public GameMap gameMap;
    public GameState parent;

    // maps possible indexes to the number of affected light bulbs in this state
    // in the tree search we go over this list
    public ArrayList<WeightedIndexPair> possiblePlacementPositions = new ArrayList<>();

    // pointer to the current observed index of the placement positions
    public int currentIndexPointer = 0;

    public GameState(GameMap gameMap) {
        this.gameMap = gameMap;
        parent = null;
    }


    /**
     * copy constructor.
     */
    public GameState(GameState parent) {
        this(new GameMap(parent.gameMap));
        this.parent = parent;
    }

    public boolean isSolved() {
        return gameMap.checkConstraints();
    }

    public void visualize() {
        gameMap.visualize();
    }

    public GameMap getGameMap() {
        return gameMap;
    }




}
