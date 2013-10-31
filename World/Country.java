package World;


import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import GUI.MapDisplay;
import GUI.MapFunctioner;

public class Country {

	public static final Color MAIN_COLOR = MapDisplay.getRandomColor();
	public static final String MAIN_NAME = "GnomenWald";
	
	private List<Village> villages;
	private List<Village> borderVillages;
	private Color color;
	private Point[] polyPoints;
	private Polygon border;
	private String name;
	
	public Country(){
		villages = new ArrayList<Village>();
		borderVillages = new ArrayList<Village>();
	}
	
	public Country(boolean flag){//only used for the first one
		this();
		this.border = null;
		this.polyPoints = null;
		this.color = MAIN_COLOR;
		this.name = MAIN_NAME;
	}
	
	public String getName(){return name;}
	public void setName(String name){this.name = name;}
	
	public void setPolyPoints(Point[] points){this.polyPoints = points;}
	public Point[] getPolyPoints(){return polyPoints;}
	
	public Polygon getBorder(){return border;}
	public void setBorder(Polygon border){this.border=border;}
	
	public Color getColor(){return color;}
	public void setColor(Color c){color = c;}
	
	public void setBorderVillage(Village v){
		if(villages.contains(v))
			borderVillages.add(v);
	}
	public List<Village> getBorderVillages(){return borderVillages;}
	
	public List<Village> getVillages(){return villages;}
	
	public List<Road> getRoads(){
		List<Road> roads = new ArrayList<Road>();
		for(Village v: villages){
			roads.addAll(v.getOutRoads());
		}
		return roads;
	}
	public synchronized List<Gnome> getGnomes(){
		List<Gnome> gnomes = new ArrayList<Gnome>();
		for(Village v: villages)
			gnomes.addAll(v.getGnomes());
		for(Road r: getRoads())
			gnomes.addAll(r.getGnomes());
		return gnomes;
	}
	
	public synchronized Gnome addGnome(){	
		Village place = randomVillage();
		Gnome g = new Gnome(place);
		place.addGnome(g);
		GnomeRunner gr = new GnomeRunner(g);
		g.setRunner(gr);
		gr.start();
		return g;
	}
	
	private Village randomVillageOutOf(List<Village> villages){
		if(villages.size()==0)
			return null;
		int rand = (int)(Math.random()*villages.size());
		return villages.get(rand);
	}
	
	private Village randomVillage(){
		return randomVillageOutOf(villages);
	}
	
	public void removeVillage(Village v){
		if(isBorderVillage(v))
			reformBorder(v);
		villages.remove(v);
		v.delete();
		if(villages.size()>1)
			supplementMST();
	}
	
	public boolean isBorderVillage(Village v){
		return borderVillages.contains(v);
	}
	
	public void resyncVillageToBorder(Village v){
		if(isBorderVillage(v)){
			v.setNewPos(polyPoints[borderVillages.indexOf(v)]);
		}
	}
	
	private void resyncVillagesToBorders(){
		for(Village v: borderVillages){
			resyncVillageToBorder(v);
		}
	}
	
	public boolean updateBorderVillageLoc(Map map,Village v){
		int vIndex = borderVillages.indexOf(v);
		Point vPoint = new Point(v.getX(),v.getY());
		Point p1 = polyPoints[vIndex+1];
		Point p2 = polyPoints[(vIndex==0)?borderVillages.size():vIndex-1];
		Line2D a = new Line2D.Double(vPoint,p1);
		Line2D b = new Line2D.Double(vPoint,p2);
		if(MapFunctioner.isIllegalBorderPlacement(map, a, this)||MapFunctioner.isIllegalBorderPlacement(map, b, this)){
			return false;
		}
		polyPoints[vIndex] = vPoint;
		if(vIndex==0){
			polyPoints[polyPoints.length-1] = vPoint;
		}
		int[] xPoints = new int[polyPoints.length];
		int[] yPoints = new int[polyPoints.length];
		for(int i = 0; i < polyPoints.length; i++){
			Point p = polyPoints[i];
			xPoints[i] = (int) p.getX();
			yPoints[i] = (int) p.getY();
		}
		border = new Polygon(xPoints,yPoints,polyPoints.length);
		return true;
	}
	
	public void reformBorder(Village borderRemoved){
		int i = borderVillages.indexOf(borderRemoved);
		
		Point[] prevPoints = polyPoints;
		polyPoints = new Point[polyPoints.length-1];
		int index = 0;
		for(int pi = 0; pi < prevPoints.length; pi++){
			if(pi!=i){
				polyPoints[index] = prevPoints[pi];
				index++;
			}
		}
		
		polyPoints[polyPoints.length-1] = polyPoints[0];
		
		int[] xPoints = new int[polyPoints.length];
		int[] yPoints = new int[polyPoints.length];
		for(int k = 0; k < polyPoints.length; k++){
			Point p = polyPoints[k];
			xPoints[k] = (int) p.getX();
			yPoints[k] = (int) p.getY();
		}
		border = new Polygon(xPoints,yPoints,polyPoints.length);
		borderVillages.remove(borderRemoved);
	}
	
	/*public void addVillagesNoConnect(int numVillages){
		for(int i = 0; i < numVillages; i++){
			villages.add(new Village());
		}
	}*/
	public void addVillages(List<Village> villages){
		this.villages.addAll(villages);
		connectVillages();
	}
	
	/*public void addVillages(int numVillages){
		for(int i = 0; i < numVillages; i++){
			Village v = new Village();
			villages.add(v);
		}
		connectVillages();
	}*/
	
	public void addVillage(Village v){
		villages.add(v);
		if(villages.size()==1){
			return;
		}
		supplementMST();
	}
	/*public void addVillage(){
		addVillage(new Village());
	}*/
	
	private void deleteAllRoads(){
		for(Village v: villages){
			v.deleteRoads();
		}
	}
	
	public void connectVillages(){
		deleteAllRoads();
		MST.connectCountry(this);
		supplementMST();
	}
	
	//MST will not create loops, this will
	private void supplementMST(){
		List<Village> noOutRoads = getNoOutRoadVillages();
		List<Village> noInRoads = getNoInRoadVillages();
		
		for(Village v: noOutRoads){
			Village closestVillage = findClosestVillageTo(v,noInRoads);
			noInRoads.remove(closestVillage);
			new Road(v,closestVillage);
		}
		
		for(Village v: noInRoads){
			new Road(findClosestVillageTo(v,villages),v);
		}
	}
	
	private Village findClosestVillageTo(Village v,List<Village> lookingSpace){
		double closest = Double.POSITIVE_INFINITY;
		Village closestVil = null;
		for(Village close: lookingSpace){
			double nextDist = v.distanceFrom(close);
			if(nextDist<closest&&!close.equals(v))
				closestVil = close;
		}
		if(closestVil==null){
			double rand = Math.random();
			if(rand>.7)
				return findClosestVillageTo(v,villages);
			else 
				return randomVillageOutOf(getNClosestTo(2,v));
		}
		return closestVil;
	}
	
	private List<Village> getNClosestTo(int n, Village v){
		List<Village> closests = new ArrayList<Village>();
		List<Village> unused = new ArrayList<Village>();
		unused.addAll(villages);
		if(villages.size()<n+1)
			return getNClosestTo(villages.size()-1,v);
		for(int i = 0; i < n+1; i++){
			Village closestLeft = findClosestVillageTo(v,unused);
			unused.remove(closestLeft);
			closests.add(closestLeft);
		}
		closests.remove(0);
		return closests;
	}
	
	private List<Village> getNoOutRoadVillages(){
		List<Village> noOuts = new ArrayList<Village>();
		for(Village v: villages){
			if(v.getOutRoads().size()==0)
				noOuts.add(v);
		}
		return noOuts;
	}
	
	private List<Village> getNoInRoadVillages(){
		List<Village> noIns = new ArrayList<Village>();
		for(Village v: villages){
			if(v.getInRoads().size()==0)
				noIns.add(v);
		}
		return noIns;
	}
	
}
