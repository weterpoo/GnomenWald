package GUI;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;

import World.Renderable;

public abstract class InfoWindow extends JFrame implements Runnable{

	private static final long serialVersionUID = -6687961528465327501L;
	
	protected Renderable target;
	private JLabel info;
	
	public InfoWindow(Renderable target, Color c, String title){
		super(title);
		this.target = target;
		setResizable(false);
		setDefaultLookAndFeelDecorated(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setAlwaysOnTop(true);
		info = new JLabel("");
		info.setOpaque(true);
		info.setDoubleBuffered(true);
		info.setBackground(c);
		info.setForeground(Color.WHITE);
		add(info);
		setMinimumSize(new Dimension(200,175));
		setSize(200,175);
		pack();
		setVisible(true);
		start();
	}
	
	public synchronized void start() {
		new Thread(this).start();
	}
	
	private boolean finished = false;
	private void finish(){finished = true;}
	public void run(){
		while(!finished){
			updateLabel();
			repaint();
			try{
				Thread.sleep(200);
			}catch(Exception e){
				break;
			}
		}
	}
	private void updateLabel() {
		info.setText(getTargetText());
	}
	
	protected abstract String getTargetText();
	
	

}
