package edu.ncsu.csc411.ps06.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

import edu.ncsu.csc411.ps06.environment.Action;
import edu.ncsu.csc411.ps06.environment.Environment;
import edu.ncsu.csc411.ps06.environment.Position;
import edu.ncsu.csc411.ps06.environment.Tile;
import edu.ncsu.csc411.ps06.environment.TileStatus;

/**
Represents a planning agent within an environment modeled after
the Chip's Challenge Windows 95 game. This agent must develop a
plan for navigating the environment to collect chips and keys
in order to reach the environment's portal (goal condition).

Problem Set 06 - In this problem set, you will be developing a planning
  agent to navigate the environment to collect chips scattered across the
  map. In order to reach the portal (goal condition), the agent must collect
  all the chips first. In order to do this, the agent will also need to collect
  assorted keys that can be used to unlock doors blocking some of the chips.

  Map difficulties increase by the number of subgoals that the agent must complete.
  While I will be able to assist in getting started debugging, planning is not a 
  simple algorithm and still a complex task for even the most advanced AIs. 
  This of this as one of those "unsolvable" math problems scrawled in chalk on some 
  abandoned blackboard. 
  
  That is to say, you are on your own in this "mostly uncharted" territory.
*/

public class Robot {
	private Environment env;
	private PriorityQueue<Node> openSet = new PriorityQueue<>();
    private Set<StateTuple> closedSet = new HashSet<>();
    private Map<StateTuple, StateTuple> cameFrom = new HashMap<>();
    private Map<StateTuple, Integer> gScore = new HashMap<>();
    
    public Map<StateTuple, Integer> getGScores() {
    	return new HashMap<>(gScore);
    }
    
    public Map<StateTuple, StateTuple> getCameFrom() {
    	return new HashMap<>(cameFrom);
    }

	/** Initializes a Robot on a tile.
	 * @param env the environment
   	*/
    public Robot (Environment env) { this.env = env; }
	
	/**	This getAction() uses an A* method to traverse through the map and find the optimal path to the goal
	 * while picking up chips and keys for their respective doors.
	 * The getAction() method maps paths by assigning each node a gScore and fScore to find the most optimal next step.
	 * 
	 * The agent evaluates possible next states by avoiding walls and water, picking up keys and chips, 
	 * and skipping doors unless the required key is collected.
	 * 
	 * If a valid path to the goal is found, the agent returns the next movement. Otherwise, it uses
	 * an exploration method to look for accessible keys.
	 * 
	 * @return should return a single Action from the Action class.
	*/ 
    public Action getAction() {
      Position selfPos = env.getRobotPosition(this);
      Map<TileStatus, ArrayList<Position>> envPositions = env.getEnvironmentPositions();
      Position targetPos = envPositions.get(TileStatus.GOAL).get(0);
      ArrayList<Position> chips = envPositions.get(TileStatus.CHIP);
      int totalChips = chips.size();

      Map<TileStatus, Integer> currentKeys = new HashMap<>();
      ArrayList<String> inventory = env.getRobotHoldings(this);
      for (String item : inventory) {
          TileStatus keyStatus = TileStatus.valueOf(item);
          currentKeys.put(keyStatus, currentKeys.getOrDefault(keyStatus, 0) + 1);
      }

      Map<TileStatus, Set<Position>> availableKeys = new HashMap<>();
      for (TileStatus keyType : new TileStatus[]{TileStatus.KEY_BLUE, TileStatus.KEY_RED, 
                                               TileStatus.KEY_GREEN, TileStatus.KEY_YELLOW}) {
          availableKeys.put(keyType, new HashSet<>(envPositions.getOrDefault(keyType, new ArrayList<>())));
      }

      openSet.clear();
      closedSet.clear();
      cameFrom.clear();
      gScore.clear();

      Node start = new Node(selfPos, 0, heuristic(selfPos, targetPos), currentKeys, 0);
      openSet.add(start);
      gScore.put(start.state, 0);

      while (!openSet.isEmpty()) {
          Node current = openSet.poll();
          StateTuple currentState = current.state;

          if (current.position.equals(targetPos) && current.chips == totalChips) {
              return reconstructPath(currentState);
          }

          if (closedSet.contains(currentState)) continue;
          closedSet.add(currentState);

          for (Position neighborPos : env.getNeighborPositions(current.position).values()) {
              if (neighborPos == null) continue;

              TileStatus tile = env.getTiles().get(neighborPos).getStatus();


              if (tile == TileStatus.WALL || tile == TileStatus.WATER) continue;
              if (tile == TileStatus.DOOR_GOAL && current.chips < totalChips) continue;

              Map<TileStatus, Integer> newKeys = new HashMap<>(currentState.keys);
              int newChips = currentState.chips;

              if (isKey(tile) && availableKeys.get(tile).contains(neighborPos)) {
                  newKeys.put(tile, newKeys.getOrDefault(tile, 0) + 1);
                  availableKeys.get(tile).remove(neighborPos);
          
              }

              if (tile == TileStatus.CHIP) {
                  newChips++;
              }

              if (isDoor(tile)) {
                  TileStatus requiredKey = keyDoor(tile);
                  if (!newKeys.containsKey(requiredKey) || newKeys.get(requiredKey) <= 0) {
                      continue; // Skip
                  }
              }

              StateTuple neighborState = new StateTuple(neighborPos, newKeys, newChips);
              int tentativeG = gScore.get(currentState) + 1;

              if (!gScore.containsKey(neighborState) || tentativeG < gScore.get(neighborState)) {
                  cameFrom.put(neighborState, currentState);
                  gScore.put(neighborState, tentativeG);
                  int f = tentativeG + heuristic(neighborPos, targetPos);
                  openSet.add(new Node(neighborPos, tentativeG, f, newKeys, newChips));
              }
          }
      }

      return exploreForKeys();
  }

  /**
   * Exploration behavior for when no path is found.
   * This program prioritizes moving towards visible keys, and if there are none, it moves to the right.
   * @return an action to explore paths
   */
  	private Action exploreForKeys() {
  		Position selfPos = env.getRobotPosition(this);
  		Map<String, Tile> neighbors = env.getNeighborTiles(this);
  
  		// Prioritize moving toward visible keys
  		for (Map.Entry<String, Tile> entry : neighbors.entrySet()) {
  			TileStatus status = entry.getValue().getStatus();
  			if (isKey(status)) {
  				switch(entry.getKey()) {
  				case "above": return Action.MOVE_UP;
  				case "below": return Action.MOVE_DOWN;
  				case "left": return Action.MOVE_LEFT;
  				case "right": return Action.MOVE_RIGHT;
				}
			}
		}            
		return Action.MOVE_RIGHT; 
	}
    
    /**
     * Checks to see if the TileStatus is a key.
     * @param status the TileStatus.
     * @return true if the TileStatus is a key.
     */
	private boolean isKey(TileStatus status) {
		return status == TileStatus.KEY_BLUE || status == TileStatus.KEY_GREEN || status == TileStatus.KEY_RED || status == TileStatus.KEY_YELLOW;
	}
	
	/**
	 * Checks to see if the TileStatus is a door.
	 * @param status the TileStatus.
	 * @return true if the TileStatus is a door.
	 */
	private boolean isDoor(TileStatus status) {
		return status == TileStatus.DOOR_BLUE || status == TileStatus.DOOR_GREEN || status == TileStatus.DOOR_RED || status == TileStatus.DOOR_YELLOW;
	}
	
	/**
	 * Matches the door to its required key.
	 * @param door the door to evaluate.
	 * @return the key that opens the given door.
	 */
	private TileStatus keyDoor(TileStatus door) {
		switch (door) {
		case DOOR_BLUE:
			return TileStatus.KEY_BLUE;
		case DOOR_GREEN:
			return TileStatus.KEY_GREEN;
		case DOOR_RED:
			return TileStatus.KEY_RED;
		case DOOR_YELLOW:
			return TileStatus.KEY_YELLOW;
		default:
			return null;
		}
	}
	
	/**
	 * Reconstructs the path from the target position to the robot's current position.
	 * The cameFrom map stores the previous positions for each node in the path
	 * Determines the next step the robot will take and returns the robot's action.
	 * 
	 * @param target the target position to reconstruct the path from
	 * @return the action to take
	 */
	private Action reconstructPath(StateTuple targetState) {
	    Stack<Position> path = new Stack<>();
	    StateTuple current = targetState;
	
	    while (cameFrom.containsKey(current)) {
	    	path.push(current.pos);
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
	
	@Override
	public String toString() {
		return "Robot [pos=" + env.getRobotPosition(this) + "]";
	}
	
	/**
	 * Inner class that describes a Node 
	 */
	class Node implements Comparable<Node> {
		/** Position */
		Position position;
		/** the g score */
		int g;
		/** the f score */
		int f;
		/** the map of keys */
		Map<TileStatus, Integer> keys; // keys to open doors
		/** the number of chips */
		int chips;
		/** the state */
		StateTuple state;
		
		public Node(Position position, int g, int f, Map<TileStatus, Integer> keys, int chips) {
			this.position = position;
			this.g = g;
			this.f = f;
			this.keys = new HashMap<>(keys);
			this.chips = chips;
			this.state = new StateTuple(position, this.keys, chips);
		}
		
		@Override
		public int compareTo(Node other) {
			return Integer.compare(this.f, other.f);
		}
	}
	
	/**
	 * Inner class that describes the state (position, keys, and chips) of each Node
	 */
	public class StateTuple {
		/** Position */
		public Position pos;
		/** the map of keys */
		public Map<TileStatus, Integer> keys;
		/** the number of chips */
		public int chips;
		
		public StateTuple(Position pos, Map<TileStatus, Integer> keys, int chips) {
			this.pos = pos;
			this.keys = new HashMap<>(keys);
			this.chips = chips;
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof StateTuple)) return false;
			StateTuple other = (StateTuple) o;
			return this.pos.equals(other.pos) &&
					this.keys.equals(other.keys) &&
					this.chips == other.chips;
		}
		
		@Override
		public int hashCode() {
		    return pos.hashCode() + keys.hashCode() * 31 + Integer.hashCode(chips);
		}
	}
}