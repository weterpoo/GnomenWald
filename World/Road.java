package World;


import java.util.ArrayList;
import java.util.List;

import World.*;

public class Road implements Comparable{
	
	
	private Village start;
	private Village end;
	private double toll;
	private volatile List<Gnome> travelingGnomes;
	//private double income;
	double theta;
	
	public Road(Village start, Village end){
		this.start = start;
		this.end = end;
		toll = 1;
		travelingGnomes = new ArrayList<Gnome>();
		updateVillages();
	}
	
	public Road(Village start, Village end, boolean fakeFlag){
		this.start = start;
		this.end = end;
	}
	
	public void delete(){
		start.getOutRoads().remove(this);
		end.getInRoads().remove(this);
		killGnomes();
	}
	
	public void killGnomes(){travelingGnomes = new ArrayList<Gnome>();}
	
	public synchronized List<Gnome> getGnomes(){return travelingGnomes;}
	
	public synchronized void addGnome(Gnome gn){
		travelingGnomes.add(gn);
	}
	
	public synchronized void removeGnome(Gnome remove){
		for(int i = 0; i < travelingGnomes.size(); i++){
			Gnome gn = travelingGnomes.get(i);
			//will actually remove all if something strange happened
			if(gn.getId()==(remove.getId()))
				travelingGnomes.remove(i);
		}
				
	}
	
	public boolean contains(Village v){
		return start.equals(v)||end.equals(v);
	}
	
	public double getLength(){
		return start.distanceFrom(end);
	}
	
	public double getToll(){return getLength()/2500.0;}
	
	public double getTheta(){
		return Math.atan2(end.getY()-start.getY(), end.getX()-start.getX());
	}
	
	private void updateVillages(){
		start.addOutRoad(this);
		end.addInRoad(this);
	}
	
	public Village getStart(){return start;}
	public Village getEnd(){return end;}
	
	public void extend(Road other){
		this.end = other.end;
	}
	
	public void divideIncome()//splits toll tax to the connecting villages
	{
		/*
		 * if(income%2==0)
		 * {
		 * start.add(income/2);
		 * end.add(income/2);
		 * income=0;
		 * }
		 * else
		 * {
		 * income--;
		 * start.add(income/2);
		 * end.add(income/2);
		 * income=1;
		 * }
		 */
	}
	
	public String toString(){
		String out = start+","+end;
		return out;
	}

	public int compareTo(Object arg0) {
		if(((Road)arg0).getLength()>getLength())
			return -1;
		return 1;
	}
}
