package edu.ncsu.csc411.ps02.agent;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

import edu.ncsu.csc411.ps02.environment.Action;
import edu.ncsu.csc411.ps02.environment.Environment;
import edu.ncsu.csc411.ps02.environment.Position;
import edu.ncsu.csc411.ps02.environment.Tile;
import edu.ncsu.csc411.ps02.environment.TileStatus;

/**
	Represents an intelligent agent moving through a particular room.	
	The robot only has two sensors - the ability to retrieve the 
	the status of all its neighboring tiles, including itself, and the
	ability to retrieve to location of the TARGET tile.
	
	Your task is to modify the getAction method below so that it reaches
	TARGET with a minimal number of steps.
*/

public class Robot {
	private Environment env;
	
	Stack<Tile> stack = new Stack<>();
	Set<Position> visited = new HashSet<>();
	Stack<Tile> movements = new Stack<>();
    private PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
    private Set<Position> closedSet = new HashSet<>();
    private Map<Position, Position> cameFrom = new HashMap<>();
    private Map<Position, Integer> gScore = new HashMap<>();
	
	
	/** Initializes a Robot on a specific tile in the environment. */
	public Robot (Environment env) { this.env = env; }
	
	/**
    Problem Set 02 - Modify the getAction method below in order to simulate
    the passage of a single time-step. At each time-step, the Robot decides
    which tile to move to.
    
    Your task for this Problem Set is to modify the method below such that
    the Robot agent is able to reach the TARGET tile on a given Environment. 
    5 out of the 10 graded test cases, with explanations on how to create new
    Environments, are available under the test package.
    
    This method should return a single Action from the Action class.
    	- Action.DO_NOTHING
    	- Action.MOVE_UP
    	- Action.MOVE_DOWN
    	- Action.MOVE_LEFT
    	- Action.MOVE_RIGHT
	 */

	/**
	   * Executes action for the robot.
	   * 
	   * This method uses a Depth First Search (DFS) approach to find the target tile.
	   * The robot pushes its current tile to a stack if it hasn't been visited yet in order
	   * to track where to go.
	   * If the current tile is the target, the robot will do nothing.
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
	public Action getAction() {
		Position selfPos = env.getRobotPosition(this);
		Position targetPos = env.getTarget();	
		
		if (selfPos.equals(targetPos)) {
			return Action.DO_NOTHING;
		}
		
		Map<String, Position> positions = env.getNeighborPositions(selfPos);
		
		Position above = positions.get("above"); // Either a Tile object or null
	    Position below = positions.get("below"); // Either a Tile object or null
	    Position left = positions.get("left");   // Either a Tile object or null
	    Position right = positions.get("right"); // Either a Tile object or null
		
	    Map<Position, Tile> tiles = env.getTiles();
		Tile selfTile = tiles.get(selfPos);
		Tile aboveTile = tiles.get(above);
		Tile belowTile = tiles.get(below);
		Tile rightTile = tiles.get(right);
		Tile leftTile = tiles.get(left);
	   
	    
	    if (!visited.contains(selfPos)) {
	    	stack.push(selfTile);
	    	visited.add(selfPos);
	    } 
	    
	    if (right != null && !visited.contains(right) && rightTile.getStatus() != TileStatus.IMPASSABLE) {
	        return Action.MOVE_RIGHT;
	    }
	    if (below != null && !visited.contains(below) && belowTile.getStatus() != TileStatus.IMPASSABLE) {
	        return Action.MOVE_DOWN;
	    }
	    if (left != null && !visited.contains(left) && leftTile.getStatus() != TileStatus.IMPASSABLE) {
	        return Action.MOVE_LEFT;
	    }
	    if (above != null && !visited.contains(above) && aboveTile.getStatus() != TileStatus.IMPASSABLE) {
	        return Action.MOVE_UP;
	    }
	    
	    if (!stack.isEmpty()) {
	    	Tile previous = stack.pop();
	    	return moveTo(previous);
	    }
		
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
	private Action moveTo(Tile next) {
		Position selfPos = env.getRobotPosition(this);
		Map<String, Position> positions = env.getNeighborPositions(selfPos);

		Position self = positions.get("self");
	    Position above = positions.get("above");
	    Position below = positions.get("below");
	    Position left = positions.get("left");
	    Position right = positions.get("right");
		
		Map<Position, Tile> tiles = env.getTiles();
		Tile selfTile = tiles.get(selfPos);
		Tile aboveTile = tiles.get(above);
		Tile belowTile = tiles.get(below);
		Tile rightTile = tiles.get(right);
		Tile leftTile = tiles.get(left);

	    if (selfTile.equals(next)) {
	        return Action.DO_NOTHING;
	    }

	    if (movements.isEmpty() || !movements.peek().equals(self)) {
	        movements.push(selfTile);
	    }

	    if (rightTile != null && rightTile.equals(next)) {
	        return Action.MOVE_RIGHT;
	    }
	    else if (belowTile != null && belowTile.equals(next)) {
	        return Action.MOVE_DOWN;
	    } else if (leftTile != null && leftTile.equals(next)) {
	        return Action.MOVE_LEFT;
	    } else if (aboveTile != null && aboveTile.equals(next)) {
	        return Action.MOVE_UP;
	    }

	    if (!movements.isEmpty()) {
	        Tile previousTile = movements.pop();
	        return moveTo(previousTile);
	    }

	    return Action.DO_NOTHING;
	}
	
	/**
	   * Executes action for the robot.
	   * 
	   * This method uses a A* pathfinding algorithm approach to find the target tile.
	   * The robot calculates the optimal path from its current position to the target
	   * using a distance heuristic and the path's fscore.
	   * If the current tile is the target, the robot will do nothing.
	   *
	   * @return An action the robot takes
	   */
	public Action getActionAStar() {
	    Position selfPos = env.getRobotPosition(this);
	    Position targetPos = env.getTarget();

	    if (selfPos.equals(targetPos)) {
	        return Action.DO_NOTHING;
	    }

	    openSet.clear();
	    closedSet.clear();
	    cameFrom.clear();
	    gScore.clear();

	    gScore.put(selfPos, 0);

	    openSet.add(new Node(selfPos, 0, heuristic(selfPos, targetPos)));

	    while (!openSet.isEmpty()) {
	        Node current = openSet.poll();

	        if (current.position.equals(targetPos)) {
	            return reconstructPath(current.position);
	        }

	        closedSet.add(current.position);

	        for (Position neighborPos : env.getNeighborPositions(current.position).values()) {
	            if (neighborPos == null || closedSet.contains(neighborPos)) {
	                continue;
	            }

	            if (env.getTiles().get(neighborPos).getStatus() == TileStatus.IMPASSABLE) {
	                continue;
	            }

	            int tentativeGScore = gScore.get(current.position) + 1;

	            if (!gScore.containsKey(neighborPos) || tentativeGScore < gScore.get(neighborPos)) {
	                cameFrom.put(neighborPos, current.position);
	                gScore.put(neighborPos, tentativeGScore);
	                int fScore = tentativeGScore + heuristic(neighborPos, targetPos);
	                openSet.add(new Node(neighborPos, tentativeGScore, fScore));
	            }
	        }
	    }

	    return Action.DO_NOTHING;
	}

	/**
	 * Reconstructs the path from the target position to the robot's current position.
	 * The cameFrom map stores the previous positions for each node in the path
	 * Determines the next step the robot will take and returns the robot's action.
	 * 
	 * @param target the target position to reconstruct the path from
	 * @return the action to take
	 */
	private Action reconstructPath(Position target) {
	    Position current = target;
	    Stack<Position> path = new Stack<>();

	    while (cameFrom.containsKey(current)) {
	        path.push(current);
	        current = cameFrom.get(current);
	    }

	    if (!path.isEmpty()) {
	        Position next = path.pop();
	        Position selfPos = env.getRobotPosition(this);
	        if (next.getRow() < selfPos.getRow()) {
	            return Action.MOVE_UP;
	        } else if (next.getRow() > selfPos.getRow()) {
	            return Action.MOVE_DOWN;
	        } else if (next.getCol() < selfPos.getCol()) {
	            return Action.MOVE_LEFT;
	        } else if (next.getCol() > selfPos.getCol()) {
	            return Action.MOVE_RIGHT;
	        }
	    }

	    return Action.DO_NOTHING;
	}
	
	/**
	 * heuristic that determines the distance from position a to position b.
	 * 
	 * @param a the starting position
	 * @param b the target position
	 * @return the estimated cost from a to b
	 */
	private int heuristic(Position a, Position b) {
        return Math.abs(a.getRow() - b.getRow()) + Math.abs(a.getCol() - b.getCol());
    }
	
	/**
	 * An inner class that defines a node in the A* algorithm.
	 * Each node has a position, a cost to reach that position, and the total cost to reach the target.
	 */
	private static class Node {
		/** the position of the node */
		Position position;
		/** the cost to reach the node from the start position*/
		int g;
		/** the estimated total cost (g + h) */
		int f;
		
		/**
		 * Constructs a new node
		 * @param position the position of the node
		 * @param g the cost to reach this node
		 * @param h the heuristic value to reach the target
		 */
		Node(Position position, int g, int h) {
			this.position = position;
			this.g = g;
			this.f = g + h;
		}
	}
}