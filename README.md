# LightsPuzzleSolver-Akari

Akari is a logic puzzle game. One has to place light bulbs in a grid and has to light up all fields. A light bulb always just lights up its 
corresponding horizontal and vertical line. There are walls on the map, which block the light. Some walls have a number, which specify
how many light bulbs have to surround it. 

In this repository a informed depth search is implemented, which can solve light puzzles up to a dimension of 28x28 or sometimes higher.

## Algorithm
The search algorithm is completely implemented in Java. Light Puzzles can be fed into the software as csv file. The csv gets converted to a Gamestate.
In each step of the search most often some bulbs can be set with 100% certainty. These are set first and after it all remaining possibilities get ranked according to specific heuristics. The possiblilty with the highest value is choosen for the next depth of the depth first search.
