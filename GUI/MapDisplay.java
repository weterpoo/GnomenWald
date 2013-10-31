package GUI;


import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import World.Country;
import World.Gnome;
import World.Map;
import World.Road;
import World.Village;

public class MapDisplay extends JPanel implements ActionListener,Runnable{

	private static final long serialVersionUID = -7100418635786046078L;
	
	private Map map;

	Point rectangleStart;
	Point rectangleEnd;
	
	public MapDisplay(Map map) {
		this.map = map;
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		setDoubleBuffered(true);
		
		setPreferredSize(new Dimension(0,680));
		
		MouseHandler mh = new MouseHandler(this);
		
		addMouseListener(mh);
		addMouseMotionListener(mh);

		setVisible(true);
		
		start();
    }
	
	public synchronized void start() {
		new Thread(this).start();
	}
	public void printToFile()
	{
		final String FILE_NAME = "Gnomenwald.txt";
		FileOutputStream FOS = null;
		try {
			FOS = new FileOutputStream(FILE_NAME);
		} catch (FileNotFoundException e) {
			System.out.print("error");
		}
		PrintWriter writer = new PrintWriter(FOS);
		String output = "GNOMENWALD DATA\n----------------\n";
		for(Village v : map.getAllVillages())
		{
			output+=v.toString() +"\n";
		}
		output += Gnome.numGnomes - map.getAllGnomes().size() + " gnomes died.";
		writer.println(output);
		writer.close();
	}
	private boolean finished = false;
	private void finish(){finished = true;printToFile();System.exit(0);}
	
	private boolean paused = false;
	private synchronized void togglePaused(){
		paused = !paused;
		List<Gnome> gnomes = map.getAllGnomes();
		if(paused){
			for(Gnome gn: gnomes){
				gn.stopMoving();
				gn.paused = true;
			}
		}else
			for(Gnome gn: gnomes){
				gn.setVels();
				gn.paused = false;
			}
	}
	
	int waitingTime = 0;
	public void run(){
		while(!finished){
			if(waitingTime>=2000){
				update();
				waitingTime = 0;
			}
			render();
			try{
				Thread.sleep(15);
				if(!paused)
					waitingTime+=15;
			}catch(Exception e){
				break;
			}
		}
	}
	
	private synchronized void update(){
		List<Village> allVils = map.getAllVillages();
		List<Gnome> allGnomes = map.getAllGnomes();
		for(Village v: allVils){
			v.addResources(MapFunctioner.getNumResources(map,v,allVils.size(),allGnomes.size()));
		}
	}
	
	public Map getMap(){return map;};
	
	public Point newVillagePoint = null;
	public Country creatingCountry = null;
	
	public boolean specifyingPath = false;
	public Point pathPoint = null;
	public Gnome specialGnome = null;
	
	public synchronized void actionPerformed(ActionEvent ae) {
		if(ae.getSource().equals(MainGUI.togPause)){
			togglePaused();
		}
		else if(ae.getSource().equals(MainGUI.addGnome)){
			for(int i = 0; i < 1; i++){
				Country c = map.getUsableCountry();
				if(c==null)
					return;
				map.getUsableCountry().addGnome();
			}
		}
	    else if(ae.getSource().equals(MainGUI.addVillage)){
			MapFunctioner.createNewVillage();
		}else if(ae.getSource().equals(MainGUI.formCountry)){
			Country c = new Country();
			creatingCountry = c;
			MapFunctioner.createCountryBorder(c);
			c.setColor(getRandomColor());
			map.addCountry(c);
		}else if(ae.getSource().equals(MainGUI.deleteSelected)){
			MapFunctioner.removeSelectedVillages(map);
		}else if(ae.getSource().equals(MainGUI.deselectAll)){
			MapFunctioner.deselectAll(map);
		}
		repaint();
	}
	
	public static Color getRandomColor(){
		int red = (int)(Math.random()*40);
		int green = (int)(Math.random()*130+50);
		int blue = (int)(Math.random()*90+30);
		if(green>120){
			blue+=18;
		}
		if(green<70){
			green+=16;
			blue-=16;
		}
		if((green-blue)<30){
			blue-=16;
		}
		if((blue-red)<30){
			blue+=8;
		}
		return new Color(red,green,blue);
	}
	
	public void render(){
		repaint();
	}
	
	Village dragSelectionVillage;
	Point dragSelectionEndPoint;
    public void paintComponent(Graphics gBasic) {
        super.paintComponent(gBasic);       
        Graphics2D g = (Graphics2D)(gBasic);
        
	    g.setRenderingHint(
	        RenderingHints.KEY_ANTIALIASING,
	        RenderingHints.VALUE_ANTIALIAS_ON);
	    
        setBackground(Country.MAIN_COLOR);
        
        Font tempFont = g.getFont();
		g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
	    for(Country c: map.getCountries())
        	paintCountry(g,c);
		g.setColor(Color.WHITE);
		g.drawString(Country.MAIN_NAME, ((getWidth()/2)-Country.MAIN_NAME.length()*4), (getHeight()-20));
	    g.setFont(tempFont);
	    for(ResourceHolder rh: map.getResourceHolders()){
	    	paintResource(rh,g);
	    }
        for(Country c: map.getCountries()){
        	g.setColor(Color.BLACK);
        	for(Village v: c.getVillages())
        		paintVillage(v,g);
        	for(Road r: c.getRoads())
        		paintRoad(r,g);
        	g.setColor(Color.RED);
        }
        for(Gnome gnome: map.getAllGnomes()){
        	paintGnome(gnome,g);
        }
        if(specifyingPath){
        	g.setColor(Color.WHITE);
        	g.fillRect((int)(pathPoint.getX()-gnomeWidth/2),(int)(pathPoint.getY()-gnomeWidth/2), gnomeWidth, gnomeWidth);
        }
        if(newVillagePoint!=null){
        	g.setColor(new Color(0,0,0,128));
        	g.fillOval((int)newVillagePoint.getX()-villageWidth/2,(int)newVillagePoint.getY()-villageWidth/2,
        			   villageWidth,villageWidth);
        }
        if(dragSelectionVillage!=null&&dragSelectionEndPoint!=null)
        	drawArrow((Graphics2D) g,dragSelectionVillage.getX(),dragSelectionVillage.getY(),
        			(int)(dragSelectionEndPoint.getX()),(int)(dragSelectionEndPoint.getY()));
        paintCreatingBorder(g);
        paintHighlighter(g);
        g.dispose();
        //For testing the random colors
       /* for(int i = 0; i < (14*8); i++){
        	g.setColor(getRandomColor());
        	System.out.println(((i*100)%1400));
        	g.fillRect((i%14)*100, (i/14)*100, 100, 100);
        	g.setColor(Color.BLACK);
        	g.drawRect((i%14)*100, (i/14)*100, 100, 100);
        }*/
    }  
    
    private void paintCountry(Graphics2D g, Country c){
    	if(c.getBorder()!=null){
    	   	g.setColor(c.getColor());
    	  	g.fillPolygon(c.getBorder());
    	   	
    	  	Stroke tempStroke = g.getStroke();
    	   	g.setColor(Color.BLACK);
    		g.setStroke(new BasicStroke(2));
    		g.drawPolygon(c.getBorder());
    		g.setStroke(tempStroke);

        	if(c.getName()!=null){
        		Point[] points = c.getPolyPoints();
            	int minY = (int) points[0].getY();
            	int minX = (int) points[0].getX();
            	int maxX = (int) points[0].getX();
            	for(Point p: points){
            		if(p.getY()>minY)//greater than because y axis is backwards
            			minY = (int) p.getY();
            		if(p.getX()>maxX)
            			maxX = (int) p.getX();
            		if(p.getX()<minX)
            			minX = (int) p.getX();
            	}
            	int midX = (minX+maxX)/2;
            	int numChars = c.getName().length();
            	int drawX = midX-numChars*4;
        		g.setColor(Color.WHITE);
        		g.drawString(c.getName(),drawX,minY+20);
        	}
    	}
    }
    
    private static int resourceWidth = 8;
    private void paintResource(ResourceHolder rh, Graphics2D g){
    	g.setColor(rh.getResource().getColor());
    	int xAdj = rh.getX()-(resourceWidth/2);
    	int yAdj = rh.getY()-(resourceWidth/2);
    	g.fillOval(xAdj, yAdj, resourceWidth, resourceWidth);
    	g.setColor(Color.BLACK);
    	g.drawOval(xAdj, yAdj, resourceWidth, resourceWidth);
    }
    
    private static int borderMarkerWidth = 10;
    private void paintCreatingBorder(Graphics2D g){
    	int[][] pointArrays = MapFunctioner.arraysFromPoints();
  	    if(pointArrays!=null){
  	    	g.setColor(Color.BLACK);
  	    	Stroke prevStroke = g.getStroke();
  	    	Stroke drawingStroke = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
  	 	    g.setStroke(drawingStroke);
  	    	g.drawPolyline(pointArrays[0], pointArrays[1], pointArrays[0].length);
  	    	g.setStroke(prevStroke);
  	    	
  	    	for(int i = 0; i < pointArrays[0].length; i++){
  	    		g.setColor(Color.DARK_GRAY);
  	    		g.fillRect(pointArrays[0][i]-borderMarkerWidth/2, pointArrays[1][i]-borderMarkerWidth/2, borderMarkerWidth, borderMarkerWidth);
  	    		g.setColor(Color.BLACK);
  	    		g.drawRect(pointArrays[0][i]-borderMarkerWidth/2, pointArrays[1][i]-borderMarkerWidth/2, borderMarkerWidth, borderMarkerWidth);
  	    	}
  	    }
    }
    
    private void paintHighlighter(Graphics g){
    	if(rectangleStart!=null&&rectangleEnd!=null){
    		g.setColor(Color.BLACK);
    		g.drawRect(Math.min((int)rectangleStart.getX(), (int)rectangleEnd.getX()),
 				   Math.min((int)rectangleStart.getY(), (int)rectangleEnd.getY()),
 				   (int)Math.abs(rectangleEnd.getX()-rectangleStart.getX()),
 				   (int)Math.abs(rectangleEnd.getY()-rectangleStart.getY()));
    		g.setColor(new Color(128,255,255,128));
    		g.fillRect(Math.min((int)rectangleStart.getX(), (int)rectangleEnd.getX()),
    				   Math.min((int)rectangleStart.getY(), (int)rectangleEnd.getY()),
    				   (int)Math.abs(rectangleEnd.getX()-rectangleStart.getX()),
    				   (int)Math.abs(rectangleEnd.getY()-rectangleStart.getY()));
    	}
    }
    
    public static final int villageWidth = 12;
    private void paintVillage(Village v, Graphics g){
    	int xAdj = v.getX()-(villageWidth/2);
    	int yAdj = v.getY()-(villageWidth/2);
    	g.fillOval(xAdj, yAdj, villageWidth, villageWidth);
    	g.drawString(v.getName(),xAdj,yAdj-villageWidth/2);
    	if(v.getSelected()){
    		Color last = g.getColor();
    		g.setColor(Color.WHITE);
    		g.drawRect(xAdj-1, yAdj-1, villageWidth+1, villageWidth+1);
    		g.setColor(last);
    	}
    }
    
    private void paintRoad(Road r, Graphics g){
    	Graphics2D g2d = (Graphics2D)g;
    	int startPointX = r.getStart().getX();
    	int startPointY = r.getStart().getY();
    	int endPointX =   r.getEnd().getX();
    	int endPointY =   r.getEnd().getY();
        g2d.setStroke(new BasicStroke(1));
        g2d.drawLine(startPointX, startPointY, endPointX, endPointY);
    	
    	
        Polygon arrowHead = new Polygon();  
        arrowHead.addPoint( 0,villageWidth/2);
        arrowHead.addPoint( -villageWidth/2, -villageWidth/2);
        arrowHead.addPoint( villageWidth/2,-villageWidth/2);
        
        double midPointX = ((double)(startPointX)+endPointX)/2.0;
        double midPointY = ((double)(startPointY)+endPointY)/2.0;

        drawArrowHead(g2d,r.getTheta(),midPointX,midPointY);
    }

    public static int gnomeWidth = 4;
    private synchronized void paintGnome(Gnome gn, Graphics g){
    	boolean special = false;
    	if(specialGnome!=null&&specialGnome.getId()==gn.getId()){
    		g.setColor(Color.WHITE);
    		special = true;
    		if(gn.getPath().getVillages().size()>0){
        		for(Village v: gn.getPath().getVillages()){
        			g.fillOval((int)(v.getX()-gnomeWidth/2),(int)(v.getY()-gnomeWidth/2), gnomeWidth, gnomeWidth);
        		}
        		if(gn.getFutureVil()!=null)
        			g.fillOval((int)(gn.getFutureVil().getX()-gnomeWidth/2),(int)(gn.getFutureVil().getY()-gnomeWidth/2), gnomeWidth, gnomeWidth);
        		g.setColor(Color.MAGENTA);
        		Village last = gn.getPath().getVillages().getLast();
        		g.fillRect((int)(last.getX()-gnomeWidth/2),(int)(last.getY()-gnomeWidth/2), gnomeWidth, gnomeWidth);
    		}
    		g.setColor(Color.MAGENTA);
    	}
    	int xAdj = gn.getX()-(gnomeWidth/2);
    	int yAdj = gn.getY()-(gnomeWidth/2);
    	g.fillOval(xAdj, yAdj, gnomeWidth, gnomeWidth);
    	if(special)
    		g.setColor(Color.RED.brighter());
    }
    
    private int arrowLength = 12;
    private double arrowElevation = Math.PI/8;
    private void drawArrowHead(Graphics2D g2d, double theta, double x0, double y0)  
    {  
        double x = x0 - arrowLength * Math.cos(theta + arrowElevation);  
        double y = y0 - arrowLength * Math.sin(theta + arrowElevation);  
        g2d.draw(new Line2D.Double(x0, y0, x, y));  
        x = x0 - arrowLength * Math.cos(theta - arrowElevation);  
        y = y0 - arrowLength * Math.sin(theta - arrowElevation);  
        g2d.draw(new Line2D.Double(x0, y0, x, y));  
    }  
    
    private void drawArrow(Graphics2D g2d, int x0, int y0, int x1, int y1){
    	g2d.setColor(Color.BLACK);
    	g2d.draw(new Line2D.Double(x0,y0,x1,y1));
    	double theta = Math.atan2(y1-y0, x1-x0);
    	drawArrowHead(g2d,theta,+((double)(x0)+x1)/2.0,+((double)(y0)+y1)/2.0);
    }

}
