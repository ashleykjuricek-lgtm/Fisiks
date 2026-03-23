package simulation;
/*
 *The MIT License (MIT)
 * 
 *Copyright (c) 2013 Robert Adkins
 *
 *Permission is hereby granted, free of charge, to any person obtaining a copy
 *of this software and associated documentation files (the "Software"), to deal
 *in the Software without restriction, including without limitation the rights
 *to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *copies of the Software, and to permit persons to whom the Software is
 *furnished to do so, subject to the following conditions:
 *
 *The above copyright notice and this permission notice shall be included in
 *all copies or substantial portions of the Software.
 *
 *THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *THE SOFTWARE.
 */

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import world.World;

public class Fisiks extends JPanel {

	private static final long serialVersionUID = -7854467439567829357L;

	private World world;
	private HashMap<String, Simulation> sims;

	private JComboBox<String> worldBox;
	private DefaultComboBoxModel<String> worldBoxModel;
	
	private CardLayout cards;

	// Main menu components
	private JPanel mainMenu;
	private JButton playBtn;

	private JPanel simPanel;
	private JPanel worldMenu;
	private JButton simMenuBtn;
	private JButton simPlayBtn;
	private JButton simPauseBtn;

	// initialize applet settings
	public void init() {

		setFocusable(true);
		requestFocusInWindow();
		setSize(800, 400);
		setPreferredSize(new Dimension(800, 400));
		cards = new CardLayout();
		setLayout(cards);

		world = World.makeWorld(getWidth(), getHeight() - 40, 40, true);
		sims = new HashMap<String, Simulation>();

		initGUI();

		Simulation blocks = new Blocks();
		Simulation gas = new Gas();
		
		sims.put(blocks.getName(), blocks);
		sims.put(gas.getName(), gas);
		
		for(String key: sims.keySet())
			worldBoxModel.addElement(key);
	}

	// start main timer
	public void start() {
		cards.show(this, "Main Menu");
	}

	// stop and remove all threads from pool
	public void stop() {
		world.destroy();
	}

	// close pool and double buffer graphics
	public void destroy() {

	}

	// initialize GUI components
	private void initGUI() {

		mainMenu = new JPanel();
		mainMenu.setDoubleBuffered(true);
		mainMenu.setBackground(Color.black);
		mainMenu.setLayout(null);

		JLabel welcomeLbl = new JLabel("Fisiks Simulator");
		welcomeLbl.setHorizontalAlignment(JLabel.CENTER);
		welcomeLbl.setFont(new Font("Comic Sans MS", Font.BOLD, 27));
		welcomeLbl.setForeground(Color.white);
		welcomeLbl.setBounds(150, 100, 500, 50);

		playBtn = initButton("Go", 350, 220, 100, 50);
		simMenuBtn = initButton("Menu", 0,0, 100, 40);
		simPlayBtn = initButton("Play", 100, 0, 100, 40);
		simPauseBtn = initButton("Pause", 200, 0, 100, 40);

		worldBoxModel = new DefaultComboBoxModel<String>();
		
		worldBox = new JComboBox<String>(worldBoxModel);
		worldBox.setBounds(250, 130, 300, 100);
		
		mainMenu.add(welcomeLbl);
		mainMenu.add(playBtn);
		mainMenu.add(worldBox);
		
		simPanel = new JPanel();
		simPanel.setBackground(Color.gray);
		simPanel.setLayout(null);
		
		world.setLocation(0, getHeight() - world.getHeight());
		
		worldMenu = new JPanel();
		worldMenu.setLayout(null);
		worldMenu.setBounds(0, 0, getWidth(), getHeight() - world.getHeight());
		
		worldMenu.add(simMenuBtn);
		worldMenu.add(simPlayBtn);
		worldMenu.add(simPauseBtn);
		
		simPanel.add(worldMenu);
		simPanel.add(world);
		
		worldMenu = new JPanel();

		add(mainMenu, "Main Menu");
		add(simPanel, "Game");
	}
	
	private JButton initButton(String name, int x, int y, int width, int height) {
		JButton toReturn = new JButton(name);
		toReturn.setBackground(Color.white);
		toReturn.setFont(new Font("Comic Sans MS", Font.PLAIN, 27));
		toReturn.setBounds(x, y, width, height);
		toReturn.addActionListener(new MenuListener());
		return toReturn;
	}

	// handle GUI events
	private class MenuListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {

			if(e.getActionCommand().equals(playBtn.getActionCommand())) {

				sims.get(worldBoxModel.getSelectedItem()).init();
				world.play();
				cards.show(Fisiks.this, "Game");
			}
			else if(e.getActionCommand().equals(simMenuBtn.getActionCommand())) {
				simPanel.remove(world);
				world.destroy();
				cards.show(Fisiks.this, "Main Menu");
				world = World.makeWorld(getWidth(), getHeight() - 40, 40, true);
				world.setLocation(0, getHeight() - world.getHeight());
				simPanel.add(world);
			}
			else if(e.getActionCommand().equals(simPlayBtn.getActionCommand())) {
				world.play();
			}
			else if(e.getActionCommand().equals(simPauseBtn.getActionCommand())) {
				world.pause();
			}
		}	
	}
}
