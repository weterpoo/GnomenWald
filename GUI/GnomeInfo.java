package GUI;


import java.awt.Color;
import java.text.DecimalFormat;

import World.Gnome;
import World.Resource;

public class GnomeInfo extends InfoWindow{

	public GnomeInfo(Gnome target) {
		super(target, Color.RED.darker(),"Gnome "+(target.getId()+1));
	}

	private static DecimalFormat df = new DecimalFormat("#.00");
	protected String getTargetText() {
		String output = "";
		output += "<html>Money:";
		output +="$"+ df.format(((Gnome)target).getCash())+"<br>";
		for(int i = 0; i < Resource.RESOURCES.length; i++){
			output += Resource.RESOURCES[i].getName() + ": " + ((Gnome)target).getResources()[i] + "<br>";
		}
		output += "Thrist: " + (int)(((Gnome)target).thirst) + "<br>";
		output += "Hunger: " + (int)(((Gnome)target).hunger) + "<br>";
		output += "Coldness: " + (int)(((Gnome)target).cold) + "<br>";
		output += "Tool Need: " + (int)(((Gnome)target).needTool) + "<br>";
		output+="</html>";
		return output;
	}

}
