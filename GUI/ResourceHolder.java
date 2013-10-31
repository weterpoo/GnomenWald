package GUI;

import World.Resource;

public class ResourceHolder {
	
	private Resource resource;
	private double x,y;
	
	public ResourceHolder(Resource r, double x, double y){
		resource = r;
		this.x = x;
		this.y = y;
	}
	
	public Resource getResource(){return resource;}
	
	public int getX(){return (int)x;}
	public int getY(){return (int)y;}
}
