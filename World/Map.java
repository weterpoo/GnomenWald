package World;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import GUI.ResourceHolder;

public class Map {

	int xBounds, yBounds;
	
	private List<Country> countries;
	private List<ResourceHolder> resourceHolders;
	
	public Map(int xBounds, int yBounds, int numResources){
		this.xBounds = xBounds;
		this.yBounds = yBounds;
		resourceHolders = new ArrayList<ResourceHolder>();
		if(numResources>=Resource.RESOURCES.length){
			for(int i = 0; i < Resource.RESOURCES.length;i++){
				resourceHolders.add(new ResourceHolder(Resource.RESOURCES[i],((Math.random())*xBounds),((Math.random()*yBounds))));
			}
			numResources-=Resource.RESOURCES.length;
		}
		for(int i = 0 ; i < numResources; i++){
			resourceHolders.add(new ResourceHolder(Resource.randomResource(),((Math.random())*xBounds),((Math.random()*yBounds))));
		}
		countries = new ArrayList<Country>();
		countries.add(new Country(true));
	}
	
	public int getWidth(){return xBounds;}
	public int getHeight(){return yBounds;}
	
	public Country getUsableCountry(){
		if(!isUsableCountry())
			return null;
		while(true){
			int random = (int) (Math.random()*countries.size());
			if(countries.get(random).getVillages().size()>0)
				return countries.get(random);
		}
	}
	
	private boolean isUsableCountry(){
		for(Country c: countries)
			if(c.getVillages().size()>0)
				return true;
		return false;
	}
	
	public List<ResourceHolder> getResourceHolders(){
		return resourceHolders;
	}
	
	public List<Village> getAllVillages(){
		List<Village> villages = new ArrayList<Village>();
		for(Country c: countries){
			villages.addAll(c.getVillages());
		}
		return villages;
	}
	
	public List<Village> getAllBorderVillages(){
		List<Village> borderVillages = new ArrayList<Village>();
		for(Country c: countries){
			borderVillages.addAll(c.getBorderVillages());
		}
		return borderVillages;
	}
	
	public synchronized List<Gnome> getAllGnomes(){
		List<Gnome> gnomes = new ArrayList<Gnome>();
		for(Country c: countries){
			gnomes.addAll(c.getGnomes());
		}
		return gnomes;
	}
	
	public List<Road> getAllRoads(){
		List<Road> roads = new ArrayList<Road>();
		for(Village v: getAllVillages())
			roads.addAll(v.getOutRoads());
		return roads;
	}
	
	public void addCountry(Country c){
		countries.add(c);
	}
	
	public List<Country> getCountries(){
		return countries;
	}
	
	HashMap<Village,Double> costs;
	HashMap<Village,Village> previous;
	public synchronized void computePaths(Village source){
		costs = new HashMap<Village,Double>();
		previous  = new HashMap<Village,Village>();
		costs.put(source,new Double(0.0));
	    LinkedList<Village> VillageQueue = new LinkedList<Village>();
	    VillageQueue.add(source);

		while (!VillageQueue.isEmpty()) {
		    Village u = VillageQueue.poll();

	            // Visit each Road exiting u
	            for (Road e : u.getOutRoads())
	            {
	                Village v = e.getEnd();
	                double weight = e.getToll();
	                double distanceThroughU = (costs.get(u)==null)?(Double.POSITIVE_INFINITY):(costs.get(u)) + weight;
			if (distanceThroughU < ((costs.get(v)==null)?(Double.POSITIVE_INFINITY):costs.get(v))) {
			    VillageQueue.remove(v);
			    costs.put(v, distanceThroughU);
			    previous.put(v, u);
			    VillageQueue.add(v);
			}
	            }
	        }
	   }
	 public Path getShortestPathTo(Village target,Gnome source) {
	        Path path = new Path();
	        for (Village v = target; v != null; v = previous.get(v))
	            path.add(v,source);
	        Collections.reverse(path.getVillages());
	        path.getVillages().removeFirst();
	        return path;
	 }

	 public Path getCheapestPath(Gnome source, Village destination){
		 computePaths(source.getLastStop());
		 return getShortestPathTo(destination,source);
	 }
	
}
