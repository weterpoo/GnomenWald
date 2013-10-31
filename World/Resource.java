package World;

import java.awt.Color;

public class Resource {
	
	public static final Resource WATER = new Resource("Water",0,new Color(64,64,255));
	public static final Resource WHEAT = new Resource("Wheat",1,new Color(235,220,63));
	public static final Resource STONE = new Resource("Stone",2,new Color(186,186,186));
	public static final Resource WOOD  = new Resource("Wood", 3,new Color(102,48,9));	
	public static final Resource[] RESOURCES = {WATER,WHEAT,STONE,WOOD};
	
	private String name;
	private Color color;
	public int id;
	public Resource(String s,int id,Color c)
	{
		this.id = id;
		name = s;
		color = c;
	}
	
	public Color getColor(){return color;}
	
	public static Resource randomResource(){
		int random = (int) (Math.random()*RESOURCES.length);
		return RESOURCES[random];
	}
	
	public String getName()
	{
		return name;
	}
	
	public String toString()
	{
		return name;
	}

}
