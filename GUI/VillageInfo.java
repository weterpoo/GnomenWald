package GUI;


import java.awt.Color;
import java.text.DecimalFormat;

import World.Resource;
import World.Village;

public class VillageInfo extends InfoWindow{
	
	public VillageInfo(Village village) {
		super(village,Color.BLUE.darker(), "Village "+(village.getName()));
	}

	private static DecimalFormat df = new DecimalFormat("0.00");
	protected String getTargetText() {
		String output = "<html>";
		output+="Village Money: " + "$"+df.format(((Village)target).getCash())+"<br>";
		output+="Number of Buildings: " + ((Village)target).getBuildings() + "<br><br>";
		for(int i = 0; i < Resource.RESOURCES.length; i++){
			output += Resource.RESOURCES[i].getName() + ": " + ((Village)target).getResources()[i]+":";
			output += "    $"+df.format(((Village)target).getPrice(Resource.RESOURCES[i]))+"<br>";
		}
		output += "</html>";
		return output;
	}

}
