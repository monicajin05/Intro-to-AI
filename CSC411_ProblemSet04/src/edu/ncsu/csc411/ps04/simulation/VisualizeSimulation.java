package edu.ncsu.csc411.ps04.simulation;

import edu.ncsu.csc411.ps04.agent.examples.GreedyRobot;
import edu.ncsu.csc411.ps04.agent.examples.HorizontalRobot;
import edu.ncsu.csc411.ps04.agent.examples.LazyRobot;
import edu.ncsu.csc411.ps04.agent.examples.RandomRobot;
import edu.ncsu.csc411.ps04.agent.examples.VerticalRobot;
import edu.ncsu.csc411.ps04.agent.Robot;
import edu.ncsu.csc411.ps04.agent.StudentRobot;
import edu.ncsu.csc411.ps04.agent.StudentRobotReflex;
import edu.ncsu.csc411.ps04.environment.Environment;
import edu.ncsu.csc411.ps04.environment.Position;
import edu.ncsu.csc411.ps04.environment.Status;
import edu.ncsu.csc411.ps04.utils.ColorPalette;
import edu.ncsu.csc411.ps04.utils.ConfigurationLoader;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * A Visual Guide toward testing whether your robot
 * agent is operating correctly. This visualization
 * will run for 100 time steps, afterwards it will
 * output the number of tiles cleaned, and a percentage
 * of the room cleaned.
 * DO NOT MODIFY.
 * @author Adam Gaweda
 */
public class VisualizeSimulation extends JFrame {
	private static final long serialVersionUID = 1L;
	private EnvironmentPanel envPanel;
	private TeamNamePanel teamNamePanel;
	private Environment env;
	private boolean DEBUG;
	private String configFile = "config/configNormal.txt";
	
	public VisualizeSimulation() {
		// Currently uses the first public test case
		Properties properties = ConfigurationLoader.loadConfiguration(configFile);
		int ITERATIONS = Integer.parseInt(properties.getProperty("ITERATIONS", "200"));
		int TILESIZE = Integer.parseInt(properties.getProperty("TILESIZE", "50"));
		int DELAY = Integer.parseInt(properties.getProperty("DELAY", "200"));
		this.DEBUG = Boolean.parseBoolean(properties.getProperty("DEBUG", "true"));
		String YELLOW = properties.getProperty("YELLOW_AGENT");
		String RED = properties.getProperty("RED_AGENT");
		
		this.env = new Environment();
		// Load the YELLOW and RED agents
		this.env.addPlayer(new GreedyRobot(this.env), Status.YELLOW);
		this.env.addPlayer(new StudentRobot(this.env), Status.RED);
		
		// Build the Game Board and Team Name Panels
    	envPanel = new EnvironmentPanel(this.env, ITERATIONS, TILESIZE, DELAY, this.DEBUG);
    	teamNamePanel = new TeamNamePanel(YELLOW, RED);
		this.add(envPanel, BorderLayout.CENTER);	
		this.add(teamNamePanel, BorderLayout.SOUTH);
	}
	
	/**
	 * A simple function to determine which agent class to load on to the game board
	 */
	private Robot getAgent(String agentName, Environment env) {
		switch (agentName) {
			case "RandomRobot": return new RandomRobot(env);
			case "GreedyRobot": return new GreedyRobot(env);
			case "VerticalRobot": return new VerticalRobot(env);
			case "HorizontalRobot": return new HorizontalRobot(env);
			case "LazyRobot": return new LazyRobot(env);
			case "StudentRobot": return new StudentRobot(env);
			default:
				if (this.DEBUG) {
					String msg = String.format("%s not found, defaulting to LazyRobot", agentName);
					System.out.println(msg);
				}
				return new LazyRobot(env);
		}
	}

	public static void main(String[] args) {
	    JFrame frame = new VisualizeSimulation();

	    frame.setTitle("CSC 411 - Problem Set 04");
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.pack();
	    frame.setVisible(true);
    }

	@SuppressWarnings("serial")
	class EnvironmentPanel extends JPanel{
		private Timer timer;
		private Environment env;
		private int ITERATIONS;
		public int TILESIZE;
		public int DELAY; // milliseconds
		public boolean DEBUG;
		
		// Designs a GUI Panel based on the dimensions of the Environment and implements 
		// a Timer object to run the simulation. This timer will iterate through time-steps
		// with a 500ms delay (or wait 500ms before updating again).
		public EnvironmentPanel(Environment env, int iterations, int tilesize, int delay, boolean debug) {
			this.env = env;
			ITERATIONS = iterations; // Not used, since game will play to completion
			TILESIZE = tilesize;
			DELAY = delay;
			DEBUG = debug;
		    setPreferredSize(new Dimension( env.getCols() * (int)(TILESIZE * 1), 
		    								env.getRows() * (int)(TILESIZE * 1)));
			
			this.timer = new Timer(DELAY, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						// Wrapped in try/catch in case the Robot's decision results
						// in a crash; we'll treat that the same as Action.DO_NOTHING
						env.updateEnvironment();
					} catch (Exception ex) {
						if (DEBUG) {
							if (ex instanceof ArrayIndexOutOfBoundsException)
								System.out.println("Agent attempted to use an invalid column, skipping.");
							System.out.println(ex);
						}
					}
					repaint();
					
					// Stop the simulation if status indicates a winner or draw
					Status state = env.getGameStatus();
					switch(state) {
						case DRAW:
							System.out.println("DRAW!");
							teamNamePanel.displayGameOver(state);
							timer.stop();
							break;
						case RED_WIN:
							System.out.println("RED WINS!");
							teamNamePanel.displayGameOver(state);
							timer.stop();
							break;
						case YELLOW_WIN:
							System.out.println("YELLOW WINS!");
							teamNamePanel.displayGameOver(state);
							timer.stop();
							break;
						default:
							// Game is still ongoing
							break;
					}
				}
			});
			this.timer.start();
		}
		
		/*
		 * The paintComponent method draws all of the objects onto the
		 * panel. This is updated at each time step when we call repaint().
		 */
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			// Paint Environment Tiles
			Position[][] positions = env.clonePositions();
			for (int row = 0; row < positions.length; row++) {
				for (int col = 0; col < positions[row].length; col++) {
					// Paint the background squares
					g.setColor(ColorPalette.SILVER);
			        g.fillRect( col * TILESIZE, 
		                        row * TILESIZE,
		                        TILESIZE, TILESIZE);
			        
			        // Paint the background squares' outlines
			        g.setColor(ColorPalette.BLACK);
		            g.drawRect( col * TILESIZE, 
		                        row * TILESIZE,
		                        TILESIZE, TILESIZE);
		            
		            // Paint Circles for each piece on the game board
					if(positions[row][col].getStatus() == Status.RED) {
						// Paint Red Ovals
						g.setColor(ColorPalette.RED);
					    g.fillOval(col * TILESIZE+TILESIZE/4, 
					    		    row * TILESIZE+TILESIZE/4,
			    		            TILESIZE/2, TILESIZE/2);
					    g.setColor(ColorPalette.BLACK);
					    g.drawOval(col * TILESIZE+TILESIZE/4, 
				    		    row * TILESIZE+TILESIZE/4,
		    		            TILESIZE/2, TILESIZE/2);
		            } else if(positions[row][col].getStatus() == Status.YELLOW) {
		            	// Paint Yellow Ovals
						g.setColor(ColorPalette.YELLOW);
					    g.fillOval(col * TILESIZE+TILESIZE/4, 
					    		    row * TILESIZE+TILESIZE/4,
			    		            TILESIZE/2, TILESIZE/2);
					    g.setColor(ColorPalette.BLACK);
					    g.drawOval(col * TILESIZE+TILESIZE/4, 
				    		    row * TILESIZE+TILESIZE/4,
		    		            TILESIZE/2, TILESIZE/2);
		            }
				}
		    }
		}
	}
	//Inner Class Score Panel
	@SuppressWarnings("serial")
	class TeamNamePanel extends JPanel{
		private JLabel agentLabelYellow;
		private JLabel agentLabelRed;
		private JLabel gameStatusLabel;
		private String agentNameYellow;
		private String agentNameRed;
		private Status gameStatus;
		// A small, simple JLabel to display which agent is YELLOW and RED
		public TeamNamePanel(String yellowName, String redName) {
			
			this.gameStatus = Status.ONGOING;
			String fontFamily = "monospaced";
			String template = "%-15s: %-20s";
		    Font font = new Font(fontFamily, Font.PLAIN, 12);
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			
			// Game Status' Label
			this.gameStatusLabel = new JLabel(String.format(template, "Game Status", gameStatus));
			this.gameStatusLabel.setFont(font);
			this.gameStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			add(gameStatusLabel);
			
			// Yellow Agent's Label
			this.agentNameYellow = yellowName;
			this.agentLabelYellow = new JLabel(String.format(template, "Yellow Agent", this.agentNameYellow));
			this.agentLabelYellow.setFont(font);
			this.agentLabelYellow.setAlignmentX(Component.CENTER_ALIGNMENT);
			add(agentLabelYellow);
	
			// Red Agent's Label
			this.agentNameRed = redName;
			this.agentLabelRed = new JLabel(String.format(template, "Red Agent", this.agentNameRed));
			this.agentLabelRed.setFont(font);
			this.agentLabelRed.setAlignmentX(Component.CENTER_ALIGNMENT);
			add(agentLabelRed);
		}
		
		public void displayGameOver(Status state) {
			this.gameStatus = state;
			repaint();
		}
		
		/*
		 * The paintComponent method draws all of the objects onto the
		 * panel. This is updated at each time step when we call repaint().
		 */
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			// Update the current and best score to the Environment's current configuration
			String fontFamily = "monospaced";
			String template = "%-15s: %-20s";
		    Font font = new Font(fontFamily, Font.BOLD, 12);
			if (gameStatus == Status.YELLOW_WIN) {
				setBackground(ColorPalette.LIGHTYELLOW);
				setOpaque(true);
				this.gameStatusLabel.setFont(font);
				this.gameStatusLabel.setText(String.format(template, "Game Status", "YELLOW WINS!"));
			} else if (gameStatus == Status.RED_WIN) {
				setBackground(ColorPalette.LIGHTRED);
				setOpaque(true);
				this.gameStatusLabel.setFont(font);
				this.gameStatusLabel.setText(String.format(template, "Game Status", "RED WINS!"));
			}  else if (gameStatus == Status.DRAW) {
				setBackground(ColorPalette.LIGHTBLUE);
				setOpaque(true);
				this.gameStatusLabel.setFont(font);
				this.gameStatusLabel.setText(String.format(template, "Game Status", "DRAW!"));
			}
		}
	}
}