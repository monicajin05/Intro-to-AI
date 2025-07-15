package edu.ncsu.csc411.ps06.simulation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import edu.ncsu.csc411.ps06.agent.Robot;
import edu.ncsu.csc411.ps06.environment.Environment;
import edu.ncsu.csc411.ps06.environment.Position;
import edu.ncsu.csc411.ps06.environment.Tile;
import edu.ncsu.csc411.ps06.utils.ConfigurationLoader;
import edu.ncsu.csc411.ps06.utils.MapManager;
import edu.ncsu.csc411.ps06.agent.Robot.StateTuple;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;
import java.util.HashMap;

public class ScorePanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;

    public ScorePanel() {
        setLayout(new BorderLayout());
        String[] columns = {"Position", "gScore", "fScore", "Keys", "Chips"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(120); 
        table.getColumnModel().getColumn(3).setPreferredWidth(120); 
        add(new JScrollPane(table), BorderLayout.CENTER);
        setPreferredSize(new Dimension(300, 400));
    }

    public void updateScores(Map<StateTuple, Integer> gScores, Map<StateTuple, StateTuple> cameFrom) {
        model.setRowCount(0);
        for (StateTuple state : gScores.keySet()) {
            int g = gScores.get(state);
            StateTuple from = cameFrom.get(state);
            int f = g + (from == null ? 0 : heuristic(state.pos, from.pos));
            model.addRow(new Object[]{
                state.pos.toString(),
                g,
                f,
                state.keys.keySet().toString(),
                state.chips
            });
        }
    }

    private int heuristic(Position a, Position b) {
        return Math.abs(a.getRow() - b.getRow()) + Math.abs(a.getCol() - b.getCol());
    }
}
