package World;

public abstract class Renderable {

	protected double x, y;
	
	public int getX(){return (int)(x+.5);}
	public int getY(){return (int)(y+.5);}
	
	public abstract void delete();
	public abstract void createInfoWindow();
	
}
