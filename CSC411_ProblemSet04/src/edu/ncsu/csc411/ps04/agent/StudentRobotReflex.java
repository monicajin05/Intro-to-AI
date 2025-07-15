package edu.ncsu.csc411.ps04.agent;

import java.util.ArrayList;

import edu.ncsu.csc411.ps04.environment.Environment;
import edu.ncsu.csc411.ps04.environment.Position;
import edu.ncsu.csc411.ps04.environment.Status;

public class StudentRobotReflex extends Robot {
	
	private int bestMove;

	public StudentRobotReflex(Environment env) {
		super(env);
	}

	
	/**
	 * 
	 */
	public int getAction() {
		ArrayList<Integer> validMoves = env.getValidActions();
		Position[][] board = env.clonePositions();
		
		Status opponent = (role == Status.RED) ? Status.YELLOW : Status.RED;
		
		for (int col: validMoves) {
			Position[][] simulatedBoard = simulateMove(board, col, role);
			if (evaluateBoard(simulatedBoard) >= 1000) {
				return col;
			}
		}
		
		for (int col: validMoves) {
			Position[][] simulatedBoard = simulateMove(board, col, opponent);
			if (evaluateBoard(simulatedBoard) <= -1000) {
				return col;
			}
		}
		
		// Default center column
		if (validMoves.contains(3)) {
			return 3;
		}
		
		return validMoves.get(0);
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
	            
	            // if there's a three in a row, prioritize blocking
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
}
