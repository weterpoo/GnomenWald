package World;

import java.util.HashSet;
import java.util.TreeSet;
import java.util.Vector;

public class MST {
	static Vector<HashSet<Integer>> myNodeIDs = new Vector<HashSet<Integer>>();
	static TreeSet<Road> myEdges = new TreeSet<Road>();
	static TreeSet<Road> getEdges() { return myEdges; }
	static HashSet<Integer> getVertexGroup(int a) { 
		for(HashSet<Integer> nodes : myNodeIDs) {
			if(nodes.contains(a)) {
				return nodes;
			}
		}
		return null;
	}
	
	public static void connectCountry(Country c){
		TreeSet<Road> roads = getAllPossibleRoads(c);
		for(Road r: roads){
			insertEdge(r);
		}
		TreeSet<Road> edges = myEdges;
		for(Road r: edges){
			Road real1 = new Road(r.getStart(),r.getEnd());
			//Road real2 = new Road(real1.getEnd(),real1.getStart());
			//c.addRoad(real2);
		}
	}
	
	private static TreeSet<Road> getAllPossibleRoads(Country c){
		TreeSet<Road> roads = new TreeSet<Road>();
		for(Village v1: c.getVillages()){
			for(Village v2: c.getVillages()){
				if(v1.getID()!=v2.getID()){
					roads.add(new Road(v1,v2,true));
				}
			}
		}
		return roads;
	}
	
	public static void insertEdge(Road s) {
		int ID1 = s.getStart().getID();
		int ID2 = s.getEnd().getID();
		
		HashSet<Integer> listOfIDs1 = getVertexGroup(ID1); 
		HashSet<Integer> listOfIDs2 = getVertexGroup(ID2);
		
		if(listOfIDs1 == null) { 
			myEdges.add(s); 
			if(listOfIDs2 == null) { 
				HashSet<Integer> myIDs = new HashSet<Integer>(); 
				myIDs.add(ID1);
				myIDs.add(ID2);
				myNodeIDs.add(myIDs); 
			}
			else {
				listOfIDs2.add(ID1);
			}
		}
		else {
			if(listOfIDs2 == null) {
				listOfIDs1.add(ID2);
				myEdges.add(s);
			}
			else if(!listOfIDs1.equals(listOfIDs2)) {
				listOfIDs1.addAll(listOfIDs2);
				myNodeIDs.remove(listOfIDs2);
				myEdges.add(s);
			}
		}
	}
	public static String printKruskal(Road a) { 
		return "Connect node " + a.getStart().getID() + " to " + a.getEnd().getID();
	}
}
