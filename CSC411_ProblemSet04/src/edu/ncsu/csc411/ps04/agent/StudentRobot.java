package edu.ncsu.csc411.ps04.agent;

import java.util.ArrayList;

import edu.ncsu.csc411.ps04.environment.Environment;
import edu.ncsu.csc411.ps04.environment.Position;
import edu.ncsu.csc411.ps04.environment.Status;

public class StudentRobot extends Robot {
	
	private int bestMove;

	public StudentRobot(Environment env) {
		super(env);
	}

	/**
	 * Problem Set 04 - For this Problem Set you will design an agent that can play Connect Four. 
	 * The goal of Connect Four is to "connect" four (4) markers of the same color (role) 
	 * horizontally, vertically, or diagonally. In this exercise your getAction method should 
	 * return an integer between 0 and 6 (inclusive), representing the column you would like to 
	 * "drop" your marker. Unlike previous Problem Sets, in this environment, you will be alternating 
	 * turns with another agent.
	 * 
	 * There are multiple example agents found in the edu.ncsu.csc411.ps04.examples package.
	 * Each example agent provides a brief explanation on its decision process, as well as demonstrations
	 * on how to use the various methods from Environment. In order to pass this Problem Set, you must
	 * successfully beat RandomRobot, VerticalRobot, and HorizontalRobot 70% of the time as both the
	 * YELLOW and RED player. This is distributed across the first six (6) test cases. In addition,
	 * you have the chance to earn EXTRA CREDIT by beating GreedyRobot (test cases 07 and 08) 70% of
	 * the time (10% possible, 5% per test case). Finally, if you successfully pass the test cases,
	 * you are welcome to test your implementation against your classmates.
	 * 
	 * While Simple Reflex or Model-based agent may be able to succeed, consider exploring the Minimax
	 * search algorithm to maximize your chances of winning. While the first two will be easier, you may
	 * want to place priority on moves that prevent the adversary from winning.
	 */
	
	/**
	 * This method uses a minimax algorithm to evaluate the robot's best move
	 * 
	 * First checks if any move results in an opponent's win using evaluate board & blocks them
	 * Otherwise, it uses minimax to evaluate the best move within a depth of 5
	 */
	@Override
	public int getAction() {
	    bestMove = -1;
	    int depth = 5;  

	    ArrayList<Integer> validMoves = env.getValidActions();
	    Position[][] board = env.clonePositions();

	    Status opponent = (role == Status.RED) ? Status.YELLOW : Status.RED;
	    for (int col : validMoves) {
	        Position[][] simulatedBoard = simulateMove(board, col, opponent);
	        if (evaluateBoard(simulatedBoard) == -1000) {
	            return col; 
	        }
	    }

	    minimax(board, depth, true);
	    
	    if (bestMove == -1 && !validMoves.isEmpty()) {
            bestMove = validMoves.get(0);
        }

	    return bestMove;
	}

	/**
	 * Simulates a move and stores it in a new board
	 * @param board the board to modify
	 * @param col the col to move to
	 * @param player the player
	 * @return the new edited board
	 */
	private Position[][] simulateMove(Position[][] board, int col, Status player) {
        Position[][] newBoard = env.clonePositions(); 
        for (int row = env.getRows() - 1; row >= 0; row--) {
        	// if the status at a tile is blank, then drop the player there
            if (newBoard[row][col].getStatus() == Status.BLANK) {
                newBoard[row][col] = new Position(row, col, player); 
                break;
            }
        }
        return newBoard;
    }
	
	/**
	 * Minimax algorithm that determines what the final evaluated score is by using maximizing and
	 * minimizing levels to find the optimal move
	 * Uses recursion to traverse levels of depth
	 * @param board the board to evaluate
	 * @param depth the maximum depth
	 * @param isMaximizing whether the level is maximizing or minimizing
	 * @return the optimal score
	 */
	private int minimax(Position[][] board, int depth, boolean isMaximizing) {
		Status winner = env.getGameStatus();

        if (winner == role) return 1000;
        if (winner == Status.RED_WIN || winner == Status.YELLOW_WIN) return -1000; 
        if (winner == Status.DRAW || depth == 0) return evaluateBoard(board);

        ArrayList<Integer> validMoves = env.getValidActions();

        if (validMoves.isEmpty()) {
            return evaluateBoard(board);
        }

        if (isMaximizing) {
            int maxScore = Integer.MIN_VALUE;
            for (int col : validMoves) {
                Position[][] newBoard = perceive(col, board, role);
                int score = minimax(newBoard, depth - 1, false);
                if (score > maxScore) {
                    maxScore = score;
                    bestMove = col; 
                }
            }
            return maxScore;
        } else {
            int minScore = Integer.MAX_VALUE;
            Status opponent = (role == Status.RED) ? Status.YELLOW : Status.RED;
            for (int col : validMoves) {
                Position[][] newBoard = perceive(col, board, opponent);
                int score = minimax(newBoard, depth - 1, true);
                if (score < minScore) {
                    minScore = score;
                }
            }
            return minScore;
        }
	}
	
	/**
	 * Helper method that evaluates the board to determine next move
	 * @param board the board to evaluate
	 * @return the priority score
	 */
	private int evaluateBoard(Position[][] board) {
		int score = 0;
        Status opponent = (role == Status.RED) ? Status.YELLOW : Status.RED;

        if (evaluateLines(board, opponent) >= 1000) {
            return -1000; 
        }

	    score += evaluateLines(board, role); 
	    score -= evaluateLines(board, opponent); 

	    return score;
	}
	
	/**
	 * Method that evaluates whether or not a player should prioritize blocking their opponent based on 
	 * how close they are to winning
	 * Looks at vertical, horizontal, and diagonal lines
	 * @param board the board to evaluate
	 * @param player the player's status
	 * @return the priority score
	 */
	private int evaluateLines(Position[][] board, Status player) {
	    int score = 0;

	    // horizontal
	    for (int r = 0; r < env.getRows(); r++) {
	        for (int c = 0; c < env.getCols() - 3; c++) {
	            int count = 0;
	            int blanks = 0;
	            for (int i = 0; i < 4; i++) {
	                if (board[r][c + i].getStatus() == player) {
	                    count++;
	                } else if (board[r][c + i].getStatus() == Status.BLANK) {
	                    blanks++;
	                } else {
	                    count = 0; 
	                    break;
	                }
	            }
	            // if there's a three in a row, prioritize blocking
	            if (count == 3 && blanks == 1) {
	                score += 1000;
	            } else {
	                score += evaluateSequence(count);
	            }
	        }
	    }

	    // vertical
	    for (int c = 0; c < env.getCols(); c++) {
	        for (int r = 0; r < env.getRows() - 3; r++) {
	            int count = 0;
	            int blanks = 0;
	            for (int i = 0; i < 4; i++) {
	                if (board[r + i][c].getStatus() == player) {
	                    count++;
	                } else if (board[r + i][c].getStatus() == Status.BLANK) {
	                    blanks++;
	                } else {
	                    count = 0; 
	                    break;
	                }
	            }
	            if (count == 3 && blanks == 1) {
	                score += 1000;
	            } else {
	                score += evaluateSequence(count);
	            }
	        }
	    }

	    // diagonal
	    for (int r = 0; r < env.getRows() - 3; r++) {
	        for (int c = 0; c < env.getCols() - 3; c++) {
	            int count = 0;
	            int blanks = 0;
	            for (int i = 0; i < 4; i++) {
	                if (board[r + i][c + i].getStatus() == player) {
	                    count++;
	                } else if (board[r + i][c + i].getStatus() == Status.BLANK) {
	                    blanks++;
	                } else {
	                    count = 0; 
	                    break;
	                }
	            }
	            if (count == 3 && blanks == 1) {
	                score += 1000;
	            } else {
	                score += evaluateSequence(count);
	            }
	        }
	    }

	    // diagonal
	    for (int r = 3; r < env.getRows(); r++) {
	        for (int c = 0; c < env.getCols() - 3; c++) {
	            int count = 0;
	            int blanks = 0;
	            for (int i = 0; i < 4; i++) {
	                if (board[r - i][c + i].getStatus() == player) {
	                    count++;
	                } else if (board[r - i][c + i].getStatus() == Status.BLANK) {
	                    blanks++;
	                } else {
	                    count = 0; 
	                    break;
	                }
	            }
	            if (count == 3 && blanks == 1) {
	                return 1000;
	            } else {
	                score += evaluateSequence(count);
	            }
	        }
	    }

	    return score;
	}

	/**
	 * Method that evaluates the priority of blocking the opponent given how many tokens they have in a row.
	 * @param count the number of tokens in a row
	 * @return 10 if the count is 2, 100 if the count is 3, 1000 if the count is 4
	 */
	private int evaluateSequence(int count) {
	    switch (count) {
	        case 2: return 10; 
	        case 3: return 100; 
	        case 4: return 1000; 
	        default: return 0; 
	    }
	}

	
	/**
	 * 
	 */
	public int getActionReflex() {
		ArrayList<Integer> validMoves = env.getValidActions();
		Position[][] board = env.clonePositions();
		
		Status opponent = (role == Status.RED) ? Status.YELLOW : Status.RED;
		
		for (int col: validMoves) {
			Position[][] simulatedBoard = simulateMove(board, col, role);
			if (evaluateBoard(simulatedBoard) == 1000) {
				return col;
			}
		}
		
		for (int col: validMoves) {
			Position[][] simulatedBoard = simulateMove(board, col, opponent);
			if (evaluateBoard(simulatedBoard) == 1000) {
				return col;
			}
		}
		
		// Default center column
		if (validMoves.contains(3)) {
			return 3;
		}
		
		return validMoves.get(0);
	}
}
