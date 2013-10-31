package World;




public class GnomeRunner extends Thread{
	
	private Gnome g;
	private int id;
	
	public GnomeRunner(Gnome g){
		this.id = g.getId();
		this.g = g;
	}
	
	
	private boolean finished = false;
	public void finish(){finished = true;}
	public void run(){
		while(!finished){
			try{
				sleep(10);
			}catch(InterruptedException ie){
				break;
			}
			if(g.getCurrentRoad()!=null){
				g.advanceOnRoad();
			}
			else if(g.getCurrentVillage()!=null){
				if(g.tradedAtCurVil){
					g.getOnRoadTo(g.chooseNextVillage());
					g.tradedAtCurVil = false;
				}else{
					g.trade2();
					g.tradedAtCurVil = true;
				}
			}
			if(!g.paused)
				g.runGnome();
		}
	}
	
}