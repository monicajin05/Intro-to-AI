package edu.ncsu.csc411.ps04.agent.examples;

import java.util.ArrayList;
import java.util.PriorityQueue;

import edu.ncsu.csc411.ps04.agent.Robot;
import edu.ncsu.csc411.ps04.environment.Environment;
import edu.ncsu.csc411.ps04.environment.Position;
import edu.ncsu.csc411.ps04.environment.Status;

/**
 * A rough version of my implementation of a Connect Four agent
 * from grad school. This version did not use any form of search tree
 * and instead only relied on a greedy best-first decision process.
 * The agent looks at all possible next months and determines which
 * will produce the most connected pieces. The logic was if I cluster
 * around a central area, I could eventually produce a "connect 4".
 * Note, this agent DID NOT perform well in the course's tournament.
 * DO NOT MODIFY.
 * @author Adam Gaweda
 *
 */
public class GreedyRobot extends Robot {
	private PriorityQueue<ChoiceNode> choices;

	public GreedyRobot(Environment env) {
		super(env);
	}
	
	/**
	 * Traverse each square on the game board. If a square is my color, then
	 * look at its neighboring squares to count how many are also my color.
	 * @return int the number of squares that have an adjacent square with similar color
	 */
	public int countAdjacentTiles(Position[][] positions) {
		int adjacentCount = 0;
		Status myStatus = super.getRole();
		// Traverse each square on the game board
		for(int row = 0; row < positions.length; row++) {
			for(int col = 0; col < positions[row].length; col++) {
				Status tileStatus = positions[row][col].getStatus();
				// If this square has my game piece on it
				if (myStatus == tileStatus) {
					int[] rows = {-1,0,1}; //left, center, right
					int[] cols = {-1,0,1}; //south, center, north
					// Look at all neighboring squares
					for(int bufferRow : rows) {
						for(int bufferCol : cols) {
							int newRow = row+bufferRow;
							int newCol = col+bufferCol;
							if(newRow < 0 || newRow > positions.length-1) {} // ignore, out of bounds
							else if(newCol < 0 || newCol > positions[row].length-1) {} // ignore, out of bounds
							else if(bufferRow == 0 && bufferCol == 0) {} // ignore, top row
							else {
								Status neighborStatus = positions[newRow][newCol].getStatus();
								// If this neighbor is the same color as my agent
								if (tileStatus == neighborStatus) {
									adjacentCount++;
								}
							}
						}
					}
				}
			}
		}
		return adjacentCount;
	}

	/**
	 * Using a Priority Queue, compare all of the potential "next" valid actions (columns).
	 * Whichever column would produce the most connected squares, that will be the agent's
	 * next action.
	 */
	@Override
	public int getAction() {
		this.choices = new PriorityQueue<ChoiceNode>();
		// getValidActions will give me all the columns that still have
		// an empty square available.
		ArrayList<Integer> options = super.env.getValidActions();
		for(int i = 0; i < options.size(); i++) {
			int col = options.get(i);
			// See what the board will look like if I drop my game piece in "this" column
			Position[][] perception = super.perceive(col, super.env.clonePositions(), super.role);
			// Count adjacent tiles
			int count = countAdjacentTiles(perception);
			// Add to Priority Queue
			choices.add(new ChoiceNode(col, count));
		}
		// Get the columns with the most adjacent squares
		ChoiceNode finalChoice = choices.poll();
		return finalChoice.getCol();
	}

	/**
	 * A simple priority queue node that pushes larger priorities to the top
	 */
	class ChoiceNode implements Comparable<ChoiceNode>{
		private int col, priority;
		public ChoiceNode(int col, int priority) {
			this.col = col;
			this.priority = priority;
		}
		
		public int getCol() {
			return this.col;
		}

		@Override
		public int compareTo(ChoiceNode other) {
			return other.priority - this.priority;
		}
	}
}
