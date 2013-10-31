package GUI;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import World.Map;

public class MainGUI {

	private static Map map;
	
	public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
    
	public static JFrame f;
    private static void createAndShowGUI() {
        f = new JFrame("Gnomes");
        JFrame.setDefaultLookAndFeelDecorated(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setExtendedState(Frame.MAXIMIZED_BOTH);
        createComponents(f);
        f.pack();
        f.setVisible(true);
    }
    
    private static void createComponents(JFrame f) {
    	int numResources = 0;
    	boolean gotGoodAnswer = false;
    	while(!gotGoodAnswer){
    		gotGoodAnswer = true;
    		try{
    	    	 numResources = Integer.parseInt(JOptionPane.showInputDialog("How many resource sites would you like to have on the map?"));
    		}catch(NumberFormatException nfe){
    			int choice = JOptionPane.showConfirmDialog(null,"You have to enter an integer for this to work", "Border Creation", JOptionPane.OK_CANCEL_OPTION);
    			if(choice==JOptionPane.CANCEL_OPTION){
    				System.exit(0);
    				return;
    			}
    			gotGoodAnswer = false;
    		}
    	}
    	 map = new Map(1350,675,numResources);
        
    	 f.setLayout(new GridBagLayout());
    	 
         GridBagConstraints con = new GridBagConstraints();
         con.fill = GridBagConstraints.HORIZONTAL;

         togPause = new JButton("Toggle Paused");
         addGnome = new JButton("Add a Gnome");
 		 addVillage = new JButton("Add Village");
 		 formCountry = new JButton("Create Country");
 		 deleteSelected = new JButton("Delete Highlighted");
 		 informationOnSelected = new JButton("Display Information");
 		 deselectAll = new JButton("Deselect All");
 		
         con.fill = GridBagConstraints.BOTH;
         
         con.weighty = 1.0;
         con.weightx = .5;
         con.gridx = 0;
         con.gridy = 1;
         f.add(togPause, con);
         
         con.weighty = 1.0;
         con.weightx = .5;
         con.gridx = 1;
         con.gridy = 1;
         f.add(addGnome, con);
         
         con.weighty = 1.0;
         con.weightx = .5;
         con.gridx = 2;
         con.gridy = 1;
         f.add(addVillage, con);
         
         con.weighty = 1.0;
         con.weightx = .5;
         con.gridx = 3;
         con.gridy = 1;
         f.add(formCountry, con);
         
         con.weighty = 1.0;
         con.weightx = .5;
         con.gridx = 4;
         con.gridy = 1;
         f.add(deleteSelected, con);
         
         con.weighty = 1.0;
         con.weightx = .5;
         con.gridx = 5;
         con.gridy = 1;
         f.add(deselectAll, con);

         MapDisplay mapDis = new MapDisplay(map);
         con.gridx = 0;
         con.gridy = 0;
         con.gridwidth = 6;
         con.weightx = 1.0;
         f.add(mapDis, con);
         
        togPause.addActionListener(mapDis);
        addGnome.addActionListener(mapDis);
 		addVillage.addActionListener(mapDis);
 		formCountry.addActionListener(mapDis);
 		deleteSelected.addActionListener(mapDis);
 		deselectAll.addActionListener(mapDis);
    }
    
	public static JButton togPause;
    public static JButton addGnome;
	public static JButton addVillage;
	public static JButton formCountry;
	public static JButton deleteSelected;
	public static JButton informationOnSelected;
	public static JButton deselectAll;


}
