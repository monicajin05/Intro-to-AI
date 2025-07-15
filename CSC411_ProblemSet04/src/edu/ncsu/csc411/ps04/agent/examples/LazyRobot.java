package edu.ncsu.csc411.ps04.agent.examples;

import edu.ncsu.csc411.ps04.agent.Robot;
import edu.ncsu.csc411.ps04.environment.Environment;

/**
 * A VERY dumb agent that attempts to drop game pieces
 * outside of the game board. This is only really useful
 * for debugging your own agent's decisions.
 * DO NOT MODIFY.
 * @author Adam Gaweda
 *
 */
public class LazyRobot extends Robot {

	public LazyRobot(Environment env) {
		super(env);
	}

	/** Returns -1, or an invalid move, thus DO_NOTHING. */
	@Override
	public int getAction() {
		return -1;
	}

}