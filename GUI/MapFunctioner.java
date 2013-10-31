package GUI;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import World.Country;
import World.MST;
import World.Map;
import World.Resource;
import World.Road;
import World.Village;

public class MapFunctioner {
	
	public static double nearestResourceHolderFrom(Map m, Village v, Resource r){
		double minDist = Double.POSITIVE_INFINITY;
		for(ResourceHolder rh: m.getResourceHolders()){
			double dist = Math.hypot(v.getX()-rh.getX(),v.getY()-rh.getY());
			if(dist<minDist&&rh.getResource().equals(r)){
				minDist = dist;
			}
		}
		return minDist;
	}
	
	public static int[] getNumResources(Map m, Village v, int numVillages, int numGnomes){
		int[] numRes = new int[Resource.RESOURCES.length];
		for(int i = 0; i < numRes.length; i++){
			int toAdd = (int) (Math.log(1350.0/nearestResourceHolderFrom(m,v,Resource.RESOURCES[i]))*((double)(numGnomes)/numVillages));
			if(toAdd<1){
				if(nearestResourceHolderFrom(m,v,Resource.RESOURCES[i])==Double.POSITIVE_INFINITY)
					toAdd = 0;
				else
					toAdd = 1;
			}
			numRes[i] = toAdd;
		}
		return numRes;
	}
	
	 public static boolean villageIsBetween(Village v, Point a, Point b){
	    	int maxX = (int) Math.max(a.getX(), b.getX());
	    	int minX = (int) Math.min(a.getX(), b.getX());
	    	int maxY = (int) Math.max(a.getY(), b.getY());
	    	int minY = (int) Math.min(a.getY(), b.getY());
	    	int vilX = v.getX();
	    	int vilY = v.getY();
	    	return (vilX<maxX&&vilX>minX&&vilY<maxY&&vilY>minY);
	}
	 
		
	public static void removeSelectedVillages(Map map){
			List<Village> selectedVillages = getSelectedVillages(map);
			for(Village v: selectedVillages){
				Country c = findHomeCountry(map,v);
				c.removeVillage(v);
			}
			removeUnusedCountries(map);
			reallocateVillages(map);
	}
	
	private static void removeUnusedCountries(Map map){
		for(int i = 1; i < map.getCountries().size(); i++){
			if(map.getCountries().get(i).getBorder()==null||map.getCountries().get(i).getBorderVillages().size()<3||map.getCountries().get(i).getVillages().size()==0)
				map.getCountries().remove(i);
		}
	}
	
	public static void connect(Map map,Village dragSelectionVillage, Village end) {
		if(dragSelectionVillage.getID()!=end.getID())
			new Road(dragSelectionVillage,end);
	}
	
	public static List<Village> getSelectedVillages(Map map){
		List<Village> selectedVillages = new ArrayList<Village>();
		for(Country c: map.getCountries()){
			for(Village v: c.getVillages()){
				if(v.getSelected())
					selectedVillages.add(v);
			}
		}
		return selectedVillages;
	}
	
	public static boolean villageCreating = false;
	public static void createNewVillage(){
		villageCreating = true;
	}
	
	public static void reallocateVillages(Map m){
		List<Village> borderVillages = m.getAllBorderVillages();
		for(Village v: m.getAllVillages()){
			if(!borderVillages.contains(v)){
				Country homeCountry = findHomeCountry(m,v);
				if(!villageInCountryBorder(v,homeCountry)){
					homeCountry.removeVillage(v);
					placeVillageInCorrectCountry(m,v);
				}
			}
		}
	}
	
	public static void placeVillageInCorrectCountry(Map m, Village v){
		for(int i = 1; i < m.getCountries().size(); i++){
			Country c = m.getCountries().get(i);
			if(villageInCountryBorder(v,c)){
				c.addVillage(v);
				return;
			}
		}
		m.getCountries().get(0).addVillage(v);
	}
	
	private static boolean villageInCountryBorder(Village v, Country c){
		Point villagePoint = new Point(v.getX(),v.getY());
		if(c.getBorder()==null)
			return false;
		return c.getBorder().contains(villagePoint);
	}
	
	public static List<Point> tempBorderPoints = new ArrayList<Point>();
	public static boolean borderCreating = false;
	private static boolean hasShownCountryPrompt = false;
	public static void createCountryBorder(Country c){
		tempBorderPoints = new ArrayList<Point>();
		if(!hasShownCountryPrompt){
			int choice = JOptionPane.showConfirmDialog(null,"To create a border for your country, simply click at points that you want the border " +
					"to pass through. \nIf you make a mistake, right click to remove the last point.", "Border Creation", JOptionPane.OK_CANCEL_OPTION);
			if(choice==JOptionPane.CANCEL_OPTION)
				return;
			hasShownCountryPrompt = true;
		}
		String name = JOptionPane.showInputDialog("Please enter a name for the new country");
		c.setName(name);
		borderCreating = true;
	}
	
	public static void addBorderSegmentTo(Map m,Country c,Point next){
		if(tempBorderPoints.size()>0){
			Point prevPoint = tempBorderPoints.get(tempBorderPoints.size()-1);
			Line2D nextSegment = new Line2D.Double(prevPoint.getX(),prevPoint.getY(),next.getX(),next.getY());
			if(isIllegalBorderPlacement(m,nextSegment,null)){
				return;
			}
		}
		tempBorderPoints.add(next);
		if(tempBorderPoints.size()>2){
			if(isCloseTo(next,tempBorderPoints.get(0))){
				tempBorderPoints.remove(tempBorderPoints.size()-1);
				tempBorderPoints.add(tempBorderPoints.get(0));
				int[][] arrays = arraysFromPoints();
				Polygon nextBorder = new Polygon(arrays[0],arrays[1],arrays[0].length);
				c.setBorder(nextBorder);
				c.setPolyPoints(pointArray());
				
				List<Village> insideVillages = getContainedVillages(m,c);
				List<Village> borderVillages = new ArrayList<Village>();
				
				for(int i = 0; i < arrays[0].length-1; i++){
					Village nextVil = new Village(m,arrays[0][i],arrays[1][i]);
					borderVillages.add(nextVil);
				}
				
				insideVillages.addAll(borderVillages);
				c.addVillages(insideVillages);
				
				for(Village borderVil: borderVillages){
					c.setBorderVillage(borderVil);
				}
				
				tempBorderPoints = new ArrayList<Point>();
				borderCreating = false;
			}
		}
	}
	
	public static Country findHomeCountry(Map m, Village v){
		for(Country c: m.getCountries()){
			if(c.getVillages().contains(v))
				return c;
		}
		return null;
	}
	
	public static List<Village> getContainedVillagesNoRemove(Map map, Country c){
		boolean inverse = c.getBorder()==null;//if the country has no border, check all the rest
		
		List<Village> contained = (inverse)?map.getAllVillages():new ArrayList<Village>();
		if(inverse){
			for(Village v: map.getAllVillages()){
				for(Country next: map.getCountries()){
					if(next.getBorder()!=null&&next.getBorder().contains(new Point(v.getX(),v.getY())))
						contained.remove(v);
				}
			}
		}
		else{
			for(Village v: map.getAllVillages()){
				if(c.getBorder().contains(new Point(v.getX(),v.getY()))){
						contained.add(v);
				}
			}
		}	
		return contained;
	}
	
	public static List<Village> getContainedVillages(Map map, Country c){
		List<Village> contained = new ArrayList<Village>();
		for(Country nextCountry: map.getCountries()){
			for(Village v: nextCountry.getVillages()){
				if(c.getBorder().contains(new Point(v.getX(),v.getY()))){
					contained.add(v);
				}
			}
		}
		for(Country nextCountry: map.getCountries()){
			for(Village toRemove: contained){
				nextCountry.removeVillage(toRemove);
			}
		}
		return contained;
	}
	
	private static int defaultRange = 10;
	public static boolean isCloseTo(Point p1, Point p2){
		return isCloseTo(p1,p2,defaultRange);
	}
	
	public static boolean isCloseTo(Point p1, Point p2, int range){
		boolean xClose = Math.abs(p1.getX()-p2.getX()) <=range;
		boolean yClose = Math.abs(p1.getY()-p2.getY()) <=range;
		return xClose&&yClose;
	}
	
	public static boolean isIllegalBorderPlacement(Map m,Line2D segment,Country toIgnore){
		for(int i = 1; i < m.getCountries().size(); i++){
			Country c = m.getCountries().get(i);
			if(c.getBorder()!=null&&c!=toIgnore){
				boolean intersect = false;
				for (int p = 0; p < c.getPolyPoints().length - 1; p++) {
				   intersect = segment.intersectsLine(c.getPolyPoints()[p].getX(), c.getPolyPoints()[p].getY(), c.getPolyPoints()[p+1].getX(), c.getPolyPoints()[p+1].getY());
				   if (intersect)
				      return true;
				}
			}
		}
		for(int i = 0; i < tempBorderPoints.size()-2; i++){
			Point p1 = tempBorderPoints.get(i);
			Point p2 = tempBorderPoints.get(i+1);
			Line2D nextLine = new Line2D.Double(p1.getX(),p1.getY(),p2.getX(),p2.getY());
			if(nextLine.intersectsLine(segment)){
				return true;
			}
		}
		return false;
	}
	
	private static Point[] pointArray(){
		if(tempBorderPoints.size()==0)
			return null;
		Point[] points = new Point[tempBorderPoints.size()];
		for(int i = 0; i < tempBorderPoints.size(); i++){
			points[i] = tempBorderPoints.get(i);
		}
		return points;
	}
	
	public static int[][] arraysFromPoints(){
		if(tempBorderPoints.size()==0)
			return null;
		int[] xPoints = new int[tempBorderPoints.size()];
		int[] yPoints = new int[tempBorderPoints.size()];
		for(int i = 0; i < tempBorderPoints.size(); i++){
			Point p = tempBorderPoints.get(i);
			int x = (int) p.getX();
			int y = (int) p.getY();
			xPoints[i] = x;
			yPoints[i] = y;
		}
		int[][] arrays = {xPoints,yPoints};
		return arrays;
	}
	
	public static void removeBorderPoint(){
		if(tempBorderPoints.size()==0){
			borderCreating = false;
			return;
		}
		tempBorderPoints.remove(tempBorderPoints.size()-1);
	}
	
	
	public static void selectAllHighlighted(Map map, Point rectangleStart, Point rectangleEnd){
	    	if(rectangleStart==null||rectangleEnd==null)
	    		return;
	    	for(Country c: map.getCountries()){
	    		for(Village v: c.getVillages())
	    			if(MapFunctioner.villageIsBetween(v,rectangleStart,rectangleEnd))
	    				v.setSelected(true);
	    	}
	    }
	
    public static void deselectAll(Map map){
    	for(Country c: map.getCountries())
    		for(Village v: c.getVillages())
    			v.setSelected(false);
    }

}
