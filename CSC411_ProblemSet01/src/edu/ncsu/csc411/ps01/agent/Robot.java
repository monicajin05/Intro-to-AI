package edu.ncsu.csc411.ps01.agent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import edu.ncsu.csc411.ps01.environment.Action;
import edu.ncsu.csc411.ps01.environment.Environment;
import edu.ncsu.csc411.ps01.environment.Tile;
import edu.ncsu.csc411.ps01.environment.TileStatus;

/**
  Represents a simple-reflex agent cleaning a particular room.
  The robot only has one sensor - the ability to retrieve the
  the status of all its neighboring tiles, including itself.
 */
public class Robot {
  private Environment env;
  
  Stack<Tile> stack = new Stack<>();
  Set<Tile> visited = new HashSet<>();
  Stack<Tile> movements = new Stack<>();
  private int depthLimit = 1;
  private int currentDepth = 0;

  /** Initializes a Robot on a specific tile in the environment. */
  public Robot(Environment env) { 
    this.env = env;
  }

  /*
    Problem Set 01 - Modify the getAction method below in order to
    simulate the passage of a single time-step. At each time-step, the Robot 
    decides whether to clean the current tile or move tiles.

    Your task for this Problem Set is to modify the method below such that
    the Robot agent is able to clean at least 70% of the available tiles on
    a given Environment. 5 out of the 10 graded test cases, with explanations
    on how to create new Environments, are available under the test package.

    This method should return a single Action from the Action class.
      - Action.CLEAN
      - Action.DO_NOTHING
      - Action.MOVE_UP
      - Action.MOVE_DOWN
      - Action.MOVE_LEFT
      - Action.MOVE_RIGHT
   */

  /**
   * Executes action for the robot.
   * 
   * This method uses a Depth First Search (DFS) approach to clean tiles and progress
   * through the map. 
   * The robot pushes its current tile to a stack if it hasn't been visited yet in order
   * to track where to go.
   * If the current tile is dirty, the robot will clean it.
   * The robot looks for unvisited neighbors and visits them if possible, if not,
   * it will backtrack to a previous tile.
   * 
   * This emulates DFS in that it finds the deepest path.
   * 
   * This method uses a stack to track visited tiles to backtrack to
   * and a set to track visited tiles to avoid revisits.
   * 
   * @return An action the robot takes
   */
  public Action getActionDFS() {
    /* This example code demonstrates the available methods and actions, such as
     * retrieving its senses (neighbor tiles), getting the status of those tiles,
     * and returning the different available Actions env.getNeighboringTiles(this)
     * will return a Map with key-value pairs for each neighbor, using a String
     * key for a Tile value
     */
    Map<String, Tile> positions = env.getNeighborTiles(this);
    Tile self = positions.get("self");
    
    Tile above = positions.get("above"); // Either a Tile object or null
    Tile below = positions.get("below"); // Either a Tile object or null
    Tile left = positions.get("left");   // Either a Tile object or null
    Tile right = positions.get("right"); // Either a Tile object or null

    System.out.println("self: " + self);
    System.out.println("above: " + above);
    System.out.println("left: " + left);
    System.out.println("below: " + below);
    System.out.println("right: " + right);
   
    if (!visited.contains(self)) {
    	stack.push(self);
    	visited.add(self);
    }
    
    if (self.getStatus() == TileStatus.DIRTY) {
    	return Action.CLEAN;
    }
    
    if (right != null && !visited.contains(right) && right.getStatus() != TileStatus.IMPASSABLE) {
        return Action.MOVE_RIGHT;
    }
    if (below != null && !visited.contains(below) && below.getStatus() != TileStatus.IMPASSABLE) {
        return Action.MOVE_DOWN;
    }
    if (left != null && !visited.contains(left) && left.getStatus() != TileStatus.IMPASSABLE) {
        return Action.MOVE_LEFT;
    }
    if (above != null && !visited.contains(above) && above.getStatus() != TileStatus.IMPASSABLE) {
        return Action.MOVE_UP;
    }
    
    if (!stack.isEmpty()) {
    	Tile previous = stack.pop();
    	return moveTo(previous);
    }
    
	return Action.DO_NOTHING;
  }
  
  /**
   * Executes action for the robot.
   * 
   * This method uses a Iterative Deepening Search (IDS) approach to clean tiles and progress
   * through the map. An IDS approach uses dFS with an increasing depth limit.
   * The robot pushes its current tile to a stack if it hasn't been visited yet in order
   * to track where to go.
   * If the current tile is dirty, the robot will clean it.
   * The robot looks for unvisited neighbors within the current depth limit and visits them 
   * if possible, if not, it will backtrack to a previous tile in the stack.
   * It will increase the depth limit and reset when all paths at the current depth limit
   * are explored.
   * 
   * This method uses a stack to track visited tiles to backtrack to, a set to track visited 
   * tiles to avoid revisits, and a depth limit to track the limit the path is limited to.
   * 
   * @return An action the robot takes
   */
  public Action getActionIDS() {
    /* This example code demonstrates the available methods and actions, such as
     * retrieving its senses (neighbor tiles), getting the status of those tiles,
     * and returning the different available Actions env.getNeighboringTiles(this)
     * will return a Map with key-value pairs for each neighbor, using a String
     * key for a Tile value
     */
	Map<String, Tile> positions = env.getNeighborTiles(this);
	Tile self = positions.get("self");
	Tile above = positions.get("above");
	Tile below = positions.get("below");
	Tile left = positions.get("left");
	Tile right = positions.get("right");
	
	if (stack.isEmpty() && currentDepth == 0) {
	    stack.push(self);
	    visited.add(self);
	    System.out.println("Starting IDS with depth limit: " + depthLimit);
	}
	
	if (self.getStatus() == TileStatus.DIRTY) {
	    visited.add(self);
	    return Action.CLEAN;
	}
	
	if (currentDepth < depthLimit) {
	    if (right != null && !visited.contains(right) && right.getStatus() != TileStatus.IMPASSABLE) {
	        stack.push(self);
	        visited.add(right);
	        currentDepth++;
	        return Action.MOVE_RIGHT;
	    }
	    if (below != null && !visited.contains(below) && below.getStatus() != TileStatus.IMPASSABLE) {
	        stack.push(self);
	        visited.add(below);
	        currentDepth++;
	        return Action.MOVE_DOWN;
	    }
	    if (left != null && !visited.contains(left) && left.getStatus() != TileStatus.IMPASSABLE) {
	        stack.push(self);
	        visited.add(left);
	        currentDepth++;
	        return Action.MOVE_LEFT;
	    }
	    if (above != null && !visited.contains(above) && above.getStatus() != TileStatus.IMPASSABLE) {
	        stack.push(self);
	        visited.add(above);
	        currentDepth++;
	        return Action.MOVE_UP;
	    }
	}
	
	// Backtrack
	if (!stack.isEmpty()) {
	    Tile previous = stack.pop();
	    currentDepth--;
	    return moveTo(previous);
	}
	
	// increase the depth limit
	depthLimit++;
	System.out.println("Increasing depth limit to: " + depthLimit);
	stack.clear();
	visited.clear();
	return Action.DO_NOTHING;
  }

  /**
   * Returns the action to bring the robot to a target tile.
   * 
   * Compares the robot's current position with the target. If the robot reaches the target,
   * do nothing. Otherwise, if no valid move exists, the robot should backtrack to a previous tile
   * in the 'movements' stack.
   * 
   * @param target the tile where the robot should move to 
   * @return what action the robot takes
   */
  public Action moveTo(Tile target) {
	    Map<String, Tile> positions = env.getNeighborTiles(this);
	    Tile self = positions.get("self");
	    Tile above = positions.get("above");
	    Tile below = positions.get("below");
	    Tile left = positions.get("left");
	    Tile right = positions.get("right");

	    if (self.equals(target)) {
	        return Action.DO_NOTHING;
	    }

	    if (movements.isEmpty() || !movements.peek().equals(self)) {
	        movements.push(self);
	    }

	    if (right != null && right.equals(target)) {
	        return Action.MOVE_RIGHT;
	    }
	    else if (below != null && below.equals(target)) {
	        return Action.MOVE_DOWN;
	    } else if (left != null && left.equals(target)) {
	        return Action.MOVE_LEFT;
	    } else if (above != null && above.equals(target)) {
	        return Action.MOVE_UP;
	    }

	    if (!movements.isEmpty()) {
	        Tile previousTile = movements.pop();
	        return moveTo(previousTile);
	    }

	    return Action.DO_NOTHING;
  }
  
  @Override
  public String toString() {
    return "Robot [neighbors=" + env.getNeighborTiles(this) + "]";
  }
}