package GUI;


import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.SwingUtilities;

import World.Country;
import World.Gnome;
import World.Path;
import World.Village;

public class MouseHandler implements MouseMotionListener,MouseListener{
    private MapDisplay md;
	
	public MouseHandler(MapDisplay md){
		this.md = md;
	}

    private void tryToggleSelect(Point point) {
		Village clickedVillage = getVillageAt(point);
		if(clickedVillage!=null){
			clickedVillage.toggleSelected();
			return;
		}
	}
	
    
    private Village getVillageAt(Point p){
    	Village possible = null;
    	for(Country c: md.getMap().getCountries()){
			for(Village v: c.getVillages()){
				if(villageBoxContains(v,p)){
					if(v.equals(moving))
						return v;
					else
						possible = v;
				}
			}
		}
    	return possible;
    }
    
    private synchronized Gnome getGnomeAt(Point p){
    	for(Gnome gn: md.getMap().getAllGnomes()){
    		if(gnomeBoxContains(gn,p)){
    			return gn;
    		}
    	}
    	return null;
    }
    
	private boolean villageBoxContains(Village v, Point p){
		int minX = (int) (v.getX()-MapDisplay.villageWidth);
		int minY = (int) (v.getY()-MapDisplay.villageWidth);
		int maxX = (int) (v.getX()+MapDisplay.villageWidth);
		int maxY = (int) (v.getY()+MapDisplay.villageWidth);

		if(p!=null&&p.getX()<maxX&&(p.getX()>minX)&&(p.getY()<maxY)&&(p.getY()>minY))
			return true;
		return false;
	}
	
	private synchronized boolean gnomeBoxContains(Gnome gn, Point p){
		int minX = (int) (gn.getX()-MapDisplay.gnomeWidth);
		int minY = (int) (gn.getY()-MapDisplay.gnomeWidth);
		int maxX = (int) (gn.getX()+MapDisplay.gnomeWidth);
		int maxY = (int) (gn.getY()+MapDisplay.gnomeWidth);

		if(p!=null&&p.getX()<maxX&&(p.getX()>minX)&&(p.getY()<maxY)&&(p.getY()>minY))
			return true;
		return false;
	}
	
	private static Point curPos = null;
	private static Village moving = null;
    public void mouseDragged(MouseEvent e)  { 
    	curPos = new Point(e.getX(), e.getY());

    	if(SwingUtilities.isLeftMouseButton(e)){
	 		if(md.rectangleStart==null){
	 			md.rectangleStart = new Point(e.getX(),e.getY());
	 		}else{
	 			md.rectangleEnd = new Point(e.getX(),e.getY());
	 		}
	 		if(md.rectangleStart==null||md.rectangleEnd==null){
	 	 		moving = getVillageAt(curPos);
	 			if(moving!=null){
	 				moving.killGnomes();
	 				Country homeCountry = MapFunctioner.findHomeCountry(md.getMap(),moving);
	 				if(homeCountry.isBorderVillage(moving)){
	 					moving.setNewPos(curPos);
	 					homeCountry.updateBorderVillageLoc(md.getMap(),moving);
	 				}
	 				else if(!(MapFunctioner.getContainedVillagesNoRemove(md.getMap(),homeCountry).contains(moving))){
	 					homeCountry.removeVillage(moving);
	 					MapFunctioner.placeVillageInCorrectCountry(md.getMap(), moving);
	 				}
	 				moving.setNewPos(curPos);
	 				md.rectangleStart=null;
	 				md.rectangleEnd=null;
	 			}
	 		}
    	}else if(SwingUtilities.isRightMouseButton(e)){
    		if(md.dragSelectionVillage==null)
    			md.dragSelectionVillage = getVillageAt(curPos);
    		if(md.dragSelectionVillage!=null){
    			md.dragSelectionEndPoint = curPos;
    		}
    	}
    }
    
    public void mouseClicked(MouseEvent e) { 
    	Point point = new Point(e.getX(),e.getY());
    	Village vAt = getVillageAt(point);
    	Gnome gAt = getGnomeAt(point);
    	
    	if(SwingUtilities.isRightMouseButton(e)){
    		if(gAt!=null){
    			gAt.createInfoWindow();
    			return;
    		}else if(vAt!=null){
    			vAt.createInfoWindow();
    			return;
    		}
    	}
    	
        if(md.specifyingPath){
            	if(vAt!=null){
            		if(md.specialGnome!=null&&md.specialGnome.getLastStop()!=null){
                		Path additive = md.getMap().getCheapestPath(md.specialGnome, vAt);
                		md.specialGnome.getPath().add(additive,gAt);
            		}
            		return;
            	}
        }
        if(gAt!=null){
        	if(gAt.equals(md.specialGnome)){
        		md.specialGnome=null;
        		md.specifyingPath=false;
        		md.pathPoint=null;
        		return;
        	}
        	md.specifyingPath = true;
        	md.pathPoint = point;
        	if(gAt.equals(md.specialGnome)){
        		md.specialGnome = null;
        		return;
        	}
        	md.specialGnome = gAt;
        	return;
        }
        
        tryToggleSelect(point);
        
        if(MapFunctioner.borderCreating){
        	if(SwingUtilities.isLeftMouseButton(e))
        		MapFunctioner.addBorderSegmentTo(md.getMap(),md.creatingCountry,point);
        	else if(SwingUtilities.isRightMouseButton(e))
        		MapFunctioner.removeBorderPoint();
        }
        if(MapFunctioner.villageCreating){
        	if(SwingUtilities.isLeftMouseButton(e)){
        		Village newVil = new Village(md.getMap(),(int)point.getX(),(int)point.getY());
        		MapFunctioner.placeVillageInCorrectCountry(md.getMap(), newVil);
        	}
        	MapFunctioner.villageCreating = false;
    		md.newVillagePoint = null;
        }
    }
	
    public void mouseMoved(MouseEvent e)    { 
    	if(md.specifyingPath){
    		md.pathPoint = new Point(e.getX(),e.getY());
    	}
    	if(MapFunctioner.villageCreating){
    		md.newVillagePoint = new Point(e.getX(),e.getY());
    	}
    }
    public void mouseExited(MouseEvent e)   { }
    public void mouseReleased(MouseEvent e) { 
    	if(SwingUtilities.isLeftMouseButton(e)){
    		if(moving!=null){
    			Country homeCountry = MapFunctioner.findHomeCountry(md.getMap(), moving);
    			if(homeCountry!=null&&homeCountry.isBorderVillage(moving)){
    				homeCountry.resyncVillageToBorder(moving);
    				MapFunctioner.reallocateVillages(md.getMap());
    			}
    		}
    		MapFunctioner.selectAllHighlighted(md.getMap(),md.rectangleStart,md.rectangleEnd);
    		md.rectangleStart = null; 
    		md.rectangleEnd = null;
    	}else if(SwingUtilities.isRightMouseButton(e)){
    		Village end = getVillageAt(md.dragSelectionEndPoint);
    		if(end!=null&&!end.equals(md.dragSelectionEndPoint))
    				MapFunctioner.connect(md.getMap(),md.dragSelectionVillage,end);
    		md.dragSelectionVillage = null;
    		md.dragSelectionEndPoint = null;
    	}
    }
    public void mouseEntered(MouseEvent e)  { }
    public void mousePressed(MouseEvent e)  { }	
    
}
