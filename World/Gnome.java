package World;

import java.awt.Point;
import java.util.ArrayList;

import GUI.GnomeInfo;
import GUI.MapFunctioner;

public class Gnome extends Renderable{

	
	public static int numGnomes = 0;
	private int id;
	
	private Village curVil;
	private Village futureVil;
	private Road travelingRoad;
	private double xVel;
	private double yVel;

	private double cash;
	private ArrayList<Village> villageLog;
	
	private int[] resources;
	
	public double thirst, hunger, cold, needTool;
	
	private Path path;
	
	private volatile GnomeRunner runner;
	
	public Gnome(Village startVil){
		this.curVil = startVil;
		villageLog = new ArrayList<Village>();
		villageLog.add(startVil);
		x=startVil.getX();
		y=startVil.getY();
		id = numGnomes;
		numGnomes++;
		cash = 10;//gnome's starting cash value
		path = new Path();
		thirst = 0;
		hunger = 0;
		cold = 0;
		needTool = 0;
		resources = new int[Resource.RESOURCES.length];
		for(int i = 0; i < resources.length; i++){
			resources[i] = 10 + (int)((Math.random()*4)-2);
		}
	}
	
	public void runGnome() {
		incrementStats();
		useResource();
		checkifDead();
	}
	
	public int[] getResources(){return resources;}
	
	public void checkifDead() {
		if(thirst >= 2500 && resources[0] <= 0) {
			delete();
			return;
		}
		if(hunger >= 2500 && resources[1] <= 0) {
			delete();
			return;
		}
		if(needTool >= 2500 && resources[2] <= 0) {
			delete();
			return;
		}
		if(cold >= 2500 && resources[3] <= 0) {
			delete();
			return;
		}
	}
	
	public void incrementStats() {
		thirst += ((int) (1 + Math.random() * 5)) / 2;
		hunger += ((int) (1 + Math.random() * 5)) / 2;
		needTool += ((int) (1 + Math.random() * 5)) / 2;
		cold += ((int) (1 + Math.random() * 5)) / 2;
	}
	public void useResource() {
		if(thirst >= 500 && resources[0] > 0) {
			thirst -= 500;
			resources[0] -= 1;
		}
		if(hunger >= 500 && resources[1] > 0) {
			hunger -= 500;
			resources[1] -= 1;
		}
		if(needTool >= 500 && resources[2] > 0) {
			needTool -= 500;
			resources[2] -= 1;
		}
		if(cold >= 500 && resources[3] > 0) {
			cold -= 500;
			resources[3] -=1;
		}
	}
	
	public boolean paused = false;
	public void delete(){
		stopMoving();
		runner.finish();
		if(curVil!=null)
			curVil.removeGnome(this);
		curVil = null;
		if(futureVil!=null)
			futureVil.removeBookingFor(this);
		futureVil = null;
		if(travelingRoad!=null)
			travelingRoad.removeGnome(this);
		travelingRoad = null;
	}
	
	public Path getPath(){return path;}
	
	public Village getLastStop(){
		if(path.getLast()!=null)
			return path.getLast();
		else {
			return futureVil;
		}
	}

	public void setRunner(GnomeRunner runner){this.runner = runner;}
	
	public Village getCurrentVillage(){
		return curVil;
	}
	
	public Village getFutureVil(){
		return futureVil;
	}
	
	public Road getCurrentRoad(){return travelingRoad;}
	
	public int getId(){return id;}
	
	public double getCash(){return cash;}
	
	public void addCash(double d){cash+=d;}
	
	public void removeCash(double d)
	{
		cash-=d;
	}
	
	public void getOnRandom(){
		Road nextRoad = curVil.getRandomOutroad();
		if(nextRoad==null)
			return;
		getOn(nextRoad);
	}
	
	public void getOn(Road r){
		if(curVil.getOutRoads().contains(r)){
			cash-=r.getToll();
			r.getStart().addVillageCash(r.getToll()/2);
			r.getEnd().addVillageCash(r.getToll()/2);
			travelingRoad = r;
			curVil.removeGnome(this);
			curVil = null;
			futureVil = travelingRoad.getEnd();
			futureVil.bookRoomFor(this);
			r.addGnome(this);
			setVels();
		}
	}
	
	public void getOnRoadTo(Village v){
		for(Road r: curVil.getOutRoads()){
			if(r.contains(v)&&!v.isFull()&&!(r.getToll()>cash)){
				getOn(r);
				return;
			}
		}
	}
	
	public boolean isPaused(){
		return (xVel==0&&yVel==0&&curVil==null);
	}
	
	public void stopMoving(){
		xVel = 0;
		yVel = 0;
	}
	
	public void setVels(){
		double[] vels = getVels();
		xVel = vels[0];
		yVel = vels[1];
	}
	
	private double[] getVels(){
		double xComp;
		double yComp;
		int targetX = futureVil.getX();
		int targetY = futureVil.getY();

		xComp = x-targetX;
		yComp = y-targetY;
		if(Math.abs(xComp) > Math.abs(yComp)){
			yComp /= -1*Math.abs(xComp);
			if(xComp>1)
				xComp = -1;
			else 
				xComp = 1;
			}
		else{
			xComp /= -1*Math.abs(yComp);
			if(yComp>1)
				yComp = -1;
			else 
				yComp = 1;
		}
		
		double[] vels = {xComp/2,yComp/2};
		
		return vels;
	}
	
	public void advanceOnRoad(){
		this.x+=xVel;
		this.y+=yVel;
		
		//if you reached the end of the road
		if(futureVil==null)
			getOnRandom();
		if(MapFunctioner.isCloseTo(new Point((int)x,(int)y),new Point(futureVil.getX(),futureVil.getY()),1)){
			curVil = futureVil;
			curVil.addGnome(this);
			villageLog.add(curVil);
			futureVil.removeBookingFor(this);
			futureVil = null;
			travelingRoad.removeGnome(this);
			travelingRoad = null;
		}
	}
	
	public String toString(){
		String out = "";
		out+=id+": ";
		out+=curVil + ", $";
		out+=cash;
		out+="\nvillages: " + villageLog.toString();
		return out;
		
	}
	
	/*
	public void buyResources() {
		for(int i = 0; i < 4; i++) {
			if(cash >= 100 && resourceCounts[i] < 10) {
				resourceCounts[i]++;
				cash = cash - 100;
				curVil.incrementVillageCash();
			}
		}
	}
	*/
	
	
	public int getNumResources(Resource r)
	{
		return resources[r.id];
	}
	
	public void addResources(int amount, Resource r)
	{
		resources[r.id] += amount;
	}
	
	public void removeResources(int amount, Resource r)
	{
		resources[r.id] -= amount;
		if(resources[r.id]<=0)
			resources[r.id]=0;
	}
	
	public Resource getLowestResource()
	{
		return Resource.RESOURCES[Math.min(Math.min(Math.min(resources[0], resources[1]), resources[2]), resources[3])];
	}
	
	public Resource getHighestResource()
	{
		return Resource.RESOURCES[Math.max(Math.max(Math.max(resources[0], resources[1]), resources[2]), resources[3])];
	}
	
		
	public boolean tradedAtCurVil = false;
	public void trade2()
	{
		sell(mostValuableResource());
		sell(mostAbundantResource());
		while(cash>0&&resources[leastAbundantResource().id]<5&&curVil.getResources()[leastAbundantResource().id]>0)
		{
			//System.out.println(cash + " " + curVil.getPrice(leastAbundantResource()));
			if(cash > curVil.getPrice(leastAbundantResource()))
			{
				buy(leastAbundantResource());
			}
			else
				break;
		}
	}
	
	private Resource leastAbundantResource()
	{
		return prioritizeMyResources()[0];
	}

	private Resource mostAbundantResource()
	{
		return prioritizeMyResources()[3];
	}

	private Resource[] prioritizeMyResources()//prioritizes resources from least to greatest based on amounts
	{
		Resource[] priority = new Resource[4];
		int counter = 0;
		while(counter<4)
		{
			int min = counter;
			for(int i = counter; i<priority.length; i++)
			{
				if(resources[i] < resources[min])
					min = i;
			}
			priority[counter] = Resource.RESOURCES[min];
			counter++;
		}
		return priority;
	}
	
	public Resource mostValuableResource()
	{
		if(curVil==null)
			return null;
		int best = 0;
		for(int i = 1; i<resources.length;i++)
		{
			if(resources[i]*curVil.getPrice(Resource.RESOURCES[i])<resources[best]*curVil.getPrice(Resource.RESOURCES[best]))
				best = i;
		}
		return Resource.RESOURCES[best];
	}
	
	public Resource leastValuableResource()
	{
		if(curVil==null)
			return null;
		int worst = 0;
		for(int i = 1; i<resources.length;i++)
		{
			if(resources[i]*curVil.getPrice(Resource.RESOURCES[i]) > resources[worst]*curVil.getPrice(Resource.RESOURCES[worst]))
				worst = i;
		}
		return Resource.RESOURCES[worst];
	}
	
	private void sell(Resource r)
	{
		curVil.buyResource(this, 1, r);
	}
	
	private void buy(Resource r)
	{
		curVil.sellResource(this, 1, r);
	}
	
	public boolean affordToTravel(Road r)
	{
		return cash >= r.getToll();
	}
	
	public Village chooseNextVillage()//chooses the next village based on resources, not cost of the road
	{
		if(path.getVillages().size()>0){
			return path.popNextVillage();
		}
		
		Resource need = leastAbundantResource();
		ArrayList<Village> potentialVillages = new ArrayList<Village>();
		for(int i =0; i<curVil.getOutRoads().size(); i++)
		{
			if(curVil.getOutRoads().get(i).getEnd().prioritizeResources()[3].equals(need))//may need to check other factors
				potentialVillages.add(curVil.getOutRoads().get(i).getEnd());
		}
		if(potentialVillages.size()==0){
			if(curVil!=null&&curVil.cheapestOutRoad()!=null)
				return curVil.cheapestOutRoad().getEnd();
		}
		int best = 0;
		for(int i=1; i<potentialVillages.size(); i++)
		{
			if(potentialVillages.get(i).getNumResources(need)>potentialVillages.get(i).getNumResources(need))
				best = i;
		}
		return potentialVillages.get(best);
	}

	public void createInfoWindow() {
		new GnomeInfo(this);
	}
}
