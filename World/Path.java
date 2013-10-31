package World;

import java.util.LinkedList;
import java.util.Queue;

public class Path {

	private LinkedList<Village> villages;
	
	public Path(){
		villages = new LinkedList<Village>();
	}
	
	public Village getLast(){
		if(villages.size()==0)
			return null;
		return villages.getLast();
	}
	
	public Village popNextVillage(){
		return villages.poll();
	}
	
	public LinkedList<Village> getVillages(){return villages;}
	
	public void add(Village v,Gnome pFor){
		villages.add(v);
		v.bookRoomFor(pFor);
	}
	
	public void add(Path p,Gnome pFor){
		if(p!=null)
			villages.addAll(p.getVillages());
		for(Village v: p.getVillages())
			v.bookRoomFor(pFor);
	}
}
