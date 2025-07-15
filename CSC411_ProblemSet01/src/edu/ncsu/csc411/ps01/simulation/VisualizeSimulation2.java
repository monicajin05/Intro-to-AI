package edu.ncsu.csc411.ps01.simulation;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import edu.ncsu.csc411.ps01.agent.Robot2;
import edu.ncsu.csc411.ps01.environment.Environment2;
import edu.ncsu.csc411.ps01.environment.Position;
import edu.ncsu.csc411.ps01.environment.Tile;
import edu.ncsu.csc411.ps01.environment.TileStatus;
import edu.ncsu.csc411.ps01.utils.ColorPalette;
import edu.ncsu.csc411.ps01.utils.ConfigurationLoader;
import edu.ncsu.csc411.ps01.utils.MapManager;

/**
 * A Visual Guide toward testing whether your robot
 * agent is operating correctly. This visualization
 * will run for 200 time steps, afterwards it will
 * output the number of tiles cleaned, and a percentage
 * of the room cleaned.
 * DO NOT MODIFY.

 * @author Adam Gaweda
 */
public class VisualizeSimulation2 extends JFrame {
  private static final long serialVersionUID = 1L;
  private EnvironmentPanel2 envPanel;
  private Environment2 env;
  private String mapFile = "maps/public/map05.txt";
  private String configFile = "config/configNormal.txt";

  /** Builds the environment; while not necessary for this problem set,
   * this could be modified to allow for different types of environments,
   * for example loading from a file, or creating multiple agents that
   * can communicate/interact with each other.
   * The map variable allows you to customize the environment to any configuration.
   * Each line in the list represents a row in the environment, and each character in
   * a string represents a column. The Environment constructor that accepts a String list
   * will review each character and set that tile's status to one of the following mappings.
   *   'D': TileStatus.DIRTY
   *   'C': TileStatus.CLEAN
   *   'W': TileStatus.IMPASSABLE
   */
  public VisualizeSimulation2() {
    // Loads configurations from the config directory. If the configNormal file is too large
    // for your monitor's resolution, you can set configFile to configSmall.txt,
    // or configLarge.txt if you want to increase the screen size.
    Properties properties = ConfigurationLoader.loadConfiguration(configFile);
    // final int iterations = Integer.parseInt(properties.getProperty("ITERATIONS", "200"));
    final int iterations = 1200;
    final int tilesize = Integer.parseInt(properties.getProperty("TILESIZE", "50"));
    final int delay = Integer.parseInt(properties.getProperty("DELAY", "200"));
    final boolean debug = Boolean.parseBoolean(properties.getProperty("DEBUG", "true"));

    // Currently loads the first public test case, but you can change the map file
    // or make your own!
    String[] map = MapManager.loadMap(mapFile);
    this.env = new Environment2(map);
    envPanel = new EnvironmentPanel2(this.env, iterations, tilesize, delay, debug);
    add(envPanel);
  }

  /** Runs the visualization for the simulation. */
  public static void main(String[] args) {
    JFrame frame = new VisualizeSimulation2();

    frame.setTitle("CSC 411 - Problem Set 01");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
    frame.setResizable(false);
  }
}

@SuppressWarnings("serial")
class EnvironmentPanel2 extends JPanel {
  private Timer timer;
  private Environment2 env;
  private ArrayList<Robot2> robots;
  private static int ITERATIONS;
  public static int TILESIZE;
  public static int DELAY; // milliseconds
  public static boolean DEBUG;

  // Designs a GUI Panel based on the dimensions of the Environment and implements 
  // a Timer object to run the simulation. This timer will iterate through time-steps
  // with a X ms delay (or wait X ms before updating again).
  public EnvironmentPanel2(Environment2 env, int iterations, int tilesize, int delay, boolean debug) {
    ITERATIONS = iterations;
    TILESIZE = tilesize;
    DELAY = delay;
    DEBUG = debug;

    setPreferredSize(new Dimension(env.getCols() * TILESIZE, env.getRows() * TILESIZE));
    this.env = env;
    this.robots = env.getRobots();

    this.timer = new Timer(DELAY, new ActionListener() {
      int timeStepCount = 0;
      @Override
	public void actionPerformed(ActionEvent e) {
        try {
          // Wrapped in try/catch in case the Robot's decision results
          // in a crash; we'll treat that the same as Action.DO_NOTHING
          env.updateEnvironment();
        } catch (Exception ex) {
          if (DEBUG) {
            String error = "[ERROR AGENT CRASH AT TIME STEP %03d] %s\n";
            System.out.printf(error, timeStepCount, ex);
          }
        }
        repaint();
        timeStepCount++;

        // Stop the simulation if either of the following conditions occur
        // 1) The Environment has no more dirty tiles
        if (env.getNumCleanedTiles() == env.getNumTiles()) {
          timer.stop();
          env.printPerformanceMeasure();
        }
        // 2) The simulation has iterated through the passed number of iterations
        if (timeStepCount == ITERATIONS) {
          timer.stop();
          env.printPerformanceMeasure();
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
    Map<Position, Tile> tiles = env.getTiles();
    for (Position p : tiles.keySet()) {
      if (tiles.get(p).getStatus() == TileStatus.CLEAN) {
        g.setColor(ColorPalette.SILVER);
      } else if (tiles.get(p).getStatus() == TileStatus.DIRTY) {
        g.setColor(ColorPalette.BROWN);
      } else if (tiles.get(p).getStatus() == TileStatus.IMPASSABLE) {
        g.setColor(ColorPalette.BLACK);
      }
      g.fillRect(p.getCol() * TILESIZE, 
          p.getRow() * TILESIZE,
          TILESIZE, TILESIZE);

      g.setColor(ColorPalette.BLACK);
      g.drawRect(p.getCol() * TILESIZE, 
          p.getRow() * TILESIZE,
          TILESIZE, TILESIZE);
    }
    // Paint Robot
    g.setColor(ColorPalette.GREEN);
    for (Robot2 robot : robots) {
      Position robotPos = env.getRobotPosition(robot);
      g.fillOval(robotPos.getCol() * TILESIZE + (TILESIZE / 4), 
          robotPos.getRow() * TILESIZE + (TILESIZE / 4),
          TILESIZE / 2, TILESIZE / 2);
    }
  }
}