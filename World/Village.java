package World;


import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import GUI.MapFunctioner;
import GUI.VillageInfo;

public class Village extends Renderable{
	private static int numVillages = 0;
	
	private int name;
	private List<Road> outRoads;
	private List<Road> inRoads;
	private volatile List<Gnome> residentGnomes;
	private volatile List<Gnome> bookedRooms;
	private double villageCash;
	private int[] numResources;
	private double[] sellPrice;
	private double[] buyPrice;
	
	private int numBuildings = 1;
	private static final int capacityPerBuilding = 5;
	private int pricePerBuilding = 5;
	
	private Map m;
	
	private int x,y;
	
	private boolean selected = false;
	
	public Village(Map m,int x, int y){
		this.m = m;
		this.x = x;
		this.y = y;
		villageCash = 5.0;
		outRoads = new ArrayList<Road>();
		inRoads  = new ArrayList<Road>();
		residentGnomes = new ArrayList<Gnome>();
		bookedRooms = new ArrayList<Gnome>();
		this.name = numVillages+1;
		numVillages++;
		setupNumResources();
		setupResourcePrices();
	}
	
	private void setupResourcePrices(){
		double[] resourceRate = new double[Resource.RESOURCES.length];
		for(int i = 0; i < resourceRate.length; i++){
			resourceRate[i] = (int) MapFunctioner.nearestResourceHolderFrom(m, this, Resource.RESOURCES[i]);
		}
		buyPrice = new double[Resource.RESOURCES.length];
		sellPrice = new double[buyPrice.length];
		for(int i = 0; i < buyPrice.length; i++){
			buyPrice[i] = resourceRate[i]/1000.0;
			sellPrice[i] = buyPrice[i];
		}
	}
	private void setupNumResources(){
		double[] resourceRate = new double[Resource.RESOURCES.length];
		for(int i = 0; i < resourceRate.length; i++){
			resourceRate[i] = (int) MapFunctioner.nearestResourceHolderFrom(m, this, Resource.RESOURCES[i]);
		}
		numResources = new int[Resource.RESOURCES.length];
		for(int i = 0; i < numResources.length; i++){
			int numRes = (int) (Math.log(1350.0/resourceRate[i])*20);
			if(numRes<=0)
				numRes = 1;
			numResources[i] = numRes;
		}
	}
	
	public void addResources(int[] resources){
		for(int i = 0; i < numResources.length; i++){
			numResources[i] += resources[i];
		}
	}
	
	public void addVillageCash(double newcash){
		villageCash+=newcash;
		if(villageCash>pricePerBuilding+1){
			villageCash-=pricePerBuilding;
			numBuildings++;
			pricePerBuilding++;
		}
	}
	
	public int getBuildings() {	return numBuildings; }
	
	public synchronized void killGnomes(){
		List<Gnome> toDeleteGnomes = new ArrayList<Gnome>();
 		for(Gnome gn: residentGnomes)
			toDeleteGnomes.add(gn);
 		for(Road r: getOutRoads())
 			r.killGnomes();
 		for(Road r: getInRoads())
 			r.killGnomes();
 		for(Gnome gn: toDeleteGnomes)
 			gn.delete();
		residentGnomes = new ArrayList<Gnome>();
		bookedRooms = new ArrayList<Gnome>();
	}
	
	public synchronized List<Gnome> getGnomes(){return residentGnomes;}
	
	public int getSize(){return residentGnomes.size()+bookedRooms.size();}
	
	public void bookRoomFor(Gnome gn){
		bookedRooms.add(gn);
	}
	
	public synchronized void removeBookingFor(Gnome gn){
		List<Gnome> toRemoveGnomes = new ArrayList<Gnome>();
		for(Gnome g: bookedRooms){
			if(g==null||(gn!=null&&g.getId()==gn.getId()))
				toRemoveGnomes.add(g);
		}
		for(Gnome g: toRemoveGnomes){
			bookedRooms.remove(g);
		}
	}
	
	public boolean isEmpty(){return residentGnomes.size()+bookedRooms.size()==0;}
	
	public boolean isFull(){return residentGnomes.size()+bookedRooms.size()>=getMaxCapacity();}
	
	public int getMaxCapacity(){return numBuildings*capacityPerBuilding;}
	
	public synchronized void addGnome(Gnome gn){
		residentGnomes.add(gn);
	}
	
	public synchronized void removeGnome(Gnome remove){
		for(int i = 0; i < residentGnomes.size(); i++){
			Gnome gn = residentGnomes.get(i);
			//will actually remove all of same id if something strange happened
			if(gn.getId()==(remove.getId()))
				residentGnomes.remove(i);
		}
				
	}
	
	public boolean getSelected(){return selected;}
	public void toggleSelected(){selected = !selected;}
	
	public double distanceFrom(Village other){
		int dx = other.x-this.x;
		int dy = other.y-this.y;
		return Math.sqrt((dx*dx)+(dy*dy));
	}
	
	public double distanceFrom(Gnome gn){
		int dx = gn.getX()-this.x;
		int dy = gn.getY()-this.y;
		return Math.sqrt((dx*dx)+(dy*dy));
	}
	
	public void setNewPos(Point p){
		this.x = (int) p.getX();
		this.y = (int) p.getY();
		setupResourcePrices();
	}
	
	public int getX(){return x;}
	public int getY(){return y;}
	
	public int getID(){
		return name;
	}
	
	public String getName(){
		return ""+name;
	}
	
	public List<Road> getOutRoads(){return outRoads;}
	public List<Road> getInRoads(){return inRoads;}
	
	public void addOutRoad(Road r){outRoads.add(r);}
	public void addInRoad(Road r){inRoads.add(r);}
	
	public void removeRoads(){
		outRoads = new ArrayList<Road>();
		inRoads  = new ArrayList<Road>();
	}
	
	public Road getRandomOutroad(){
		if(outRoads.size() == 0){
			return null;
		}
		int r = (int) (Math.random()*outRoads.size());
		return outRoads.get(r);
	}
	
	public boolean connectsTo(Village other){
		for(Road r: outRoads){
			if(r.getEnd().equals(other))
				return true;
		}
		return false;
	}
	
	public String toString(){
		String output = ""+name + "\n";
		output+= "Village Money: $" + villageCash;
		return output;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public int getNumResources(Resource r)
	{
		return numResources[r.id];
	}
	
	public double getPrice(Resource r)
	{
		return buyPrice[r.id];
	}
	
	public Resource[] prioritizeResources()//prioritizes resources from least to greatest based on amounts
	{
		Resource[] priority = new Resource[4];
		int counter = 0;
		while(counter<4)//change to 4 if it doesn't work
		{
			int min = counter;
			for(int i = counter; i<priority.length; i++)
			{
				if(numResources[i] < numResources[min])
					min = i;
			}
			priority[counter] = Resource.RESOURCES[min];
			counter++;
		}
		return priority;
	}
	
	public int[] getResources(){
		return numResources;
	}
	
	public double getCash(){return villageCash;}
	
	public void buyResource(Gnome g, int amount, Resource r)//the village is buying resources
	{
		if(villageCash<=0 || amount>g.getNumResources(r))
			return;
		while(amount*buyPrice[r.id] > villageCash)
			amount--;
		g.addCash(amount*buyPrice[r.id]);
		g.removeResources(amount, r);
		numResources[r.id]+=amount;
		villageCash-=amount*buyPrice[r.id];
	}
	
	public void sellResource(Gnome g, int amount, Resource r)
	{
		if(numResources[r.id]<=0)
			return;
		while(amount>numResources[r.id])
			amount--;
		g.addResources(amount,r);
		g.removeCash(amount*sellPrice[r.id]);
		numResources[r.id]-=amount;
		villageCash+=amount*sellPrice[r.id];
	}
	
	public void delete() {
		deleteRoads();
		killGnomes();
	}
	
	public synchronized void deleteRoads(){
		List<Road> toDeleteRoads = new ArrayList<Road>();
		for(Road r: inRoads){
			r.getStart().getOutRoads().remove(r);
			toDeleteRoads.add(r);
		}
		for(Road r: outRoads){
			r.getEnd().getInRoads().remove(r);
			toDeleteRoads.add(r);
		}
		for(Road r: toDeleteRoads)
			r.delete();
		outRoads = new ArrayList<Road>();
		inRoads  = new ArrayList<Road>();
		
	}
	
	public void createInfoWindow() {
		new VillageInfo(this);
	}

	public Road cheapestOutRoad() {
		double minToll = Double.POSITIVE_INFINITY;
		Road minTollRoad =  null;
		for(Road r: getOutRoads()){
			double toll = r.getToll();
			if(toll<minToll){
				minToll = toll;
				minTollRoad = r;
			}
		}
		return minTollRoad;
	}

}
