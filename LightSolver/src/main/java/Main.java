import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import gamelogic.GameMap;
import gamelogic.GameState;
import strategies.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static strategies.InformedDepthFirstStrategy.SelectionHeuristic.*;


public class Main {

    /*
    constants for better visualizing via console output
     */
    final static String ANSI_RESET = "\u001B[0m";
    final static String ANSI_RED = "\u001B[31m";
    final static String ANSI_YELLOW = "\u001B[33m";
    final static String ANSI_GREEN = "\u001B[32m";

    /*
    the heuristics used for the InformedDepthFirstStrategy
     */
    final static List<InformedDepthFirstStrategy.SelectionHeuristic> heuristics = new ArrayList<>(
            Arrays.asList( MOST_EXCLUDED_BUT_FREE_FIRST, MOST_WALLS, MOST_LIGHT_UP, MOST_WALLS_HIGH_NUMBERS)
    );

    public static void main(String[] args) throws
            InterruptedException,
            IOException,
            CsvException,
            InvocationTargetException,
            InstantiationException,
            IllegalAccessException {

        /*GameState puzzle1 = readPuzzle("lights1.csv");
        GameState puzzle2 = readPuzzle("lights2.csv");
        GameState puzzle3 = readPuzzle("lights3.csv");
        GameState puzzle4 = readPuzzle("lights4.csv");
        GameState puzzle5 = readPuzzle("lights5.csv");


        //preparing the puzzles and strategies for iteration
        List<GameState> initialPuzzles = Arrays.asList(puzzle1, puzzle2, puzzle3, puzzle4, puzzle5);
        List<Class<? extends StrategyIf>> strategies = new ArrayList<>();
        strategies.add(InformedDepthFirstStrategy.class);
        strategies.add(DepthFirstStrategy.class);
        strategies.add(ClearMovesStrategy.class);


        //iterating through each strategy (and heuristic, when available)

        for (Class<? extends StrategyIf> strategyClass : strategies) {

            System.out.println(ANSI_YELLOW + "\n######### Using strategy " + strategyClass.getSimpleName() + " #########" + ANSI_RESET);
            if (strategyClass == InformedDepthFirstStrategy.class) {
                for (InformedDepthFirstStrategy.SelectionHeuristic heuristic : heuristics) {
                    List<GameState> puzzles = new ArrayList<>();
                    for (GameState p : initialPuzzles) {
                        puzzles.add(new GameState(p));
                    }
                    System.out.println(ANSI_YELLOW + "\n######### Using heuristic " + heuristic.name() + " #########" + ANSI_RESET);
                    solveAllPuzzles(puzzles, strategyClass, heuristic);
                }
            } else {
                List<GameState> puzzles = new ArrayList<>();
                for (GameState p : initialPuzzles) {
                    puzzles.add(new GameState(p));
                }
                solveAllPuzzles(puzzles, strategyClass, null);
            }
        }*/
        // if you want to go through all the different approaches for lights1-5 comment in above code
        // and outcomment the following code
        // following is the solving for a 14x14 puzzle implemented
        GameState puzzleHard = readPuzzle("lights_14x14_hard.csv");
        System.out.println(ANSI_YELLOW + "\n######### Solving puzzle no. " + puzzleHard + " #########" + ANSI_RESET);

        System.out.println("Initial state:");
        puzzleHard.getGameMap().visualize();
        System.out.println();
        System.out.println("Solving...");
        System.out.println();
        InformedDepthFirstStrategy exampleStrategy =
                new InformedDepthFirstStrategy(puzzleHard, MOST_EXCLUDED_BUT_FREE_FIRST);
        exampleStrategy.enableVisualization(true);
        try {
            Date date = new Date();
            GameState result = exampleStrategy.solve();
            System.out.println("Time needed: " + (new Date().getTime() - date.getTime()));
            System.out.println("");
            System.out.println(ANSI_GREEN + "Result:");
            if (result != null) {
                result.visualize();
                System.out.println(ANSI_RESET);
            } else {
                System.out.println(ANSI_RESET + "No solution found");
            }
        } catch (UnsolvableGameStateException e) {
            e.printStackTrace();
        }
        // comment in if you want to go through all the strategies
        //StrategyIf strategy = (StrategyIf) constructor.newInstance(puzzle, heuristic);
    }

    /*
    iterating through the puzzles with a certain strategy (and possibly heuristic)
     */
    private static void solveAllPuzzles(List<GameState> puzzles, Class<? extends
            StrategyIf> strategyClass, InformedDepthFirstStrategy.SelectionHeuristic heuristic) throws
            InvocationTargetException, InstantiationException, IllegalAccessException, InterruptedException {

        Constructor<?> constructor = strategyClass.getConstructors()[0];

        int currentPuzzle = 1;
        for (GameState puzzle : puzzles) {

            System.out.println(ANSI_YELLOW + "\n######### Solving puzzle no. " + currentPuzzle + " #########" + ANSI_RESET);

            System.out.println("Initial state:");
            puzzle.getGameMap().visualize();
            System.out.println();
            System.out.println("Solving...");
            System.out.println();

            StrategyIf strategy = (StrategyIf) constructor.newInstance(puzzle, heuristic);

            /*
            threading is used for making the attempts interruptable
            (thus only needing to start the main method once)
             */
            Thread t = new Thread(() -> {
                try {
                    Date date = new Date();
                    GameState result = strategy.solve();
                    System.out.println("Time needed: " + (new Date().getTime() - date.getTime()));
                    System.out.println("");
                    System.out.println(ANSI_GREEN + "Result:");
                    if (result != null) {
                        result.visualize();
                        System.out.println(ANSI_RESET);
                    } else {
                        System.out.println(ANSI_RESET + "No solution found");
                    }
                } catch (UnsolvableGameStateException e) {
                    System.out.println(String.format("Game is not solvable: %s", e.getMessage()));
                }
            });

            t.start();
            /*
            after the specified number of milliseconds the attempt is interrupted
            and the solving of next puzzle (or strategy) starts
             */
            t.join(180_000);
            if (t.getState() == Thread.State.RUNNABLE) {
                System.out.println(ANSI_RED + "Time limit exceeded" + ANSI_RESET);
            }

            currentPuzzle++;
        }

    }

    private static GameState readPuzzle(String a) throws
            IOException, CsvException {
        Main obj = new Main();
        Class class1 = obj.getClass();
        InputStream stream = class1.getResourceAsStream(a);
        CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
        CSVReader reader = new CSVReaderBuilder(new BufferedReader(new InputStreamReader(stream))).withCSVParser(parser).build();
        List<String[]> lines = reader.readAll();
        String[][] parsedCSV = lines.toArray(new String[lines.size()][]);
        GameMap map = new GameMap(parsedCSV);
        GameState root = new GameState(map);

        return root;
    }
}

