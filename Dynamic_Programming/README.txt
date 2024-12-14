Dynamic Programming Coin Collector with Visualization

Description:
This project demonstrates a dynamic programming approach to solve a pathfinding problem on a 2D grid. The goal is to move from the top-left corner of the grid to the bottom-right corner while collecting the maximum number of coins placed randomly on the grid. By leveraging a classic dynamic programming technique, the solution efficiently computes and backtracks the optimal path, then visualizes the result for better comprehension.

Key Features:
	1.	Random Grid Initialization:
The grid’s coins are placed randomly, making each run unique. Approximately 30% of the cells contain coins (controlled by a probability threshold), ensuring variability and a realistic test environment for the algorithm.
	2.	Dynamic Programming for Optimal Pathfinding:
The solution uses a bottom-up dynamic programming table F, where each cell F[i][j] represents the maximum number of coins that can be collected up to that point. This approach systematically builds the solution from the start (top-left) to the end (bottom-right).
	3.	Parent Tracking for Backtracking the Path:
To reconstruct the optimal path, the algorithm stores parent pointers in a dictionary. After populating the dynamic programming table, it follows these pointers backward from the destination to the source to find and return the exact path taken.
	4.	Matplotlib-Based Visualization:
The project incorporates a custom drawing function to generate a clear and intuitive visualization of the grid and the chosen path. Coins are represented as circular markers on the grid, while the optimal path is highlighted in red. The output is saved as an image file and optionally displayed to the user.
	5.	Command-Line Interface:
Users specify the grid dimensions (number of rows and columns) as command-line arguments. This makes it easy to experiment with different grid sizes and complexity levels.

Libraries:

	•	Matplotlib: Visualizing the grid, coins, and the optimal path.
	•	argparse: Parsing command-line arguments for flexible input specifications.
