package strategies;

import gamelogic.GameState;

/**
 * The interface on the basis of which the strategies are implemented
 */
public interface StrategyIf {
    void enableVisualization(boolean enable);
    GameState solve() throws UnsolvableGameStateException;
}
