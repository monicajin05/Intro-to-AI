package edu.ncsu.csc411.ps04.agent.examples;

import java.util.ArrayList;

import edu.ncsu.csc411.ps04.agent.Robot;
import edu.ncsu.csc411.ps04.environment.Environment;

/**
 * A simple agent that drops game pieces vertically along
 * the game board. Once a column is full, it will simply
 * move to the next available column.
 * DO NOT MODIFY.
 * @author Adam Gaweda
 *
 */
public class VerticalRobot extends Robot {

	public VerticalRobot(Environment env) {
		super(env);
	}

	/** Returns the right-most available column */
	@Override
	public int getAction() {
		ArrayList<Integer> possibleActions = env.getValidActions();
		return possibleActions.get(possibleActions.size()-1);
//		return 0;
	}

}