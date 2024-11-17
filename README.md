# Lightspuzzle Solver - Akari

**Lightspuzzle Solver - Akari** is a university project focused on solving 28x28 cell Akari (or "Lights") puzzles, where players must place bulbs on a grid to light up every open cell according to strict adjacency rules around walls. The project addresses Akari as a constraint satisfaction problem (CSP), combining informed search techniques with custom heuristics to solve large puzzles.

## Project Overview

The solver employs an **Informed Depth Search** algorithm in Java, designed to meet Akari's constraints. A key challenge of the project was developing effective heuristics to guide bulb placement decisions. We developed and evaluated various heuristic strategies, essential for narrowing down possible moves based on factors like wall proximity and potential illumination range, making it possible to find solutions within just a few minutes.


### Key Features
- **Custom Heuristics for Search Efficiency**:
  - **Most Excluded, Free First**: Prioritizes placements that maximize illumination while adhering to constraints. This heuristic was particularly effective in reducing search time for complex puzzles by selecting moves that illuminated many cells.
  - **Most Light Up**: Places bulbs to maximize immediate cell illumination but did not perform as consistently across puzzles due to less constraint focus.
  - **High Wall Numbers First**: Focuses on placing bulbs near walls with high number requirements, useful for puzzles where wall constraints heavily guided bulb positions.

- **ClearMoves Strategy**: A preprocessing step that eliminates invalid configurations early, simplifying the search by avoiding potential dead ends from the start.

- **Constraint Management**: Models all Akari rules as hard constraints to validate each state, including ensuring each open cell is illuminated and no bulbs interfere with one another's line of sight.

## Evaluation

### Evaluation

Our evaluation underscored that heuristics were essential not only for improving search efficiency but also for making it possible to find solutions within a reasonable time frame. Without heuristics, the search space for a 28x28 grid would be too vast to find a solution even within hours.

- **Most Excluded, Free First**: This heuristic was consistently the most effective overall. By prioritizing cells with restricted placement options, it focused the search on high-potential moves while minimizing backtracking.

- **Most Walls**: Ideal for puzzles with complex wall structures, this heuristic focused on cells near walls, helping to quickly establish a structured path. By aligning bulb placements closely with wall constraints, it often solved puzzles more efficiently than other approaches when wall density was high.

- **High Wall Numbers First**: This heuristic performed well on simpler puzzles where high-numbered wall constraints could be used to guide initial moves effectively. It focused on cells near walls with the highest number requirements, which allowed for quicker determination of bulb placement in these areas. However, its utility diminished as puzzles became more complex.

- **Most Light Up**: Although it aimed to maximize illuminated cells, this heuristic was less effective in maintaining constraint satisfaction, often leading to increased backtracking. While intuitive, its sole focus on illumination made it less reliable, especially in puzzles where wall constraints played a significant role.

Overall, **Most Excluded, Free First** and **Most Walls** proved to be the most adaptable and effective heuristics, demonstrating the importance of careful heuristic selection. The project highlighted that, for NP-complete problems like Akari, informed heuristics are not just beneficial but essential for achieving solutions within a manageable time frame.

## Setup

- **Java Version**: OpenJDK 15.0.2
- **Maven**: Required for dependency management.
- **Execution**: Run `Main.java` in `src/main/java`, selecting different heuristics to test on input puzzles in CSV format.
