# LightsPuzzleSolver-Akari

Akari is a logic puzzle game. One has to place light bulbs in a grid and light up all fields. A light bulb always lights up its 
corresponding horizontal and vertical lines. There are walls on the map, which block the light. Some walls have a number, which specifies
how many light bulbs have to surround them. 

In this repository, an informed depth search is implemented, which can solve light puzzles up to a dimension of 28x28 or sometimes higher.

## Setup

Java: OpenJDK 15.0.2
Maven: Ensure Maven is installed on your machine.

Execute the main method located in src/main/java/Main.java. Inside the method, different heuristics can be selected.

## Algorithm
The search algorithm is completely implemented in Java. Light Puzzles can be fed into the software as a CSV file. The CSV gets converted to a Gamestate.
In each step of the search most often some bulbs can be set with 100% certainty. These are set first and all remaining possibilities are ranked according to specific heuristics. The possibility with the highest value is chosen for the next depth of the depth-first search.
