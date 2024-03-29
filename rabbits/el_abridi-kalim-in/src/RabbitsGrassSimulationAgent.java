import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;




/**
 * Class that implements the simulation agent for the rabbits grass simulation.

 * @author
 */

public class RabbitsGrassSimulationAgent implements Drawable {
	
	private int x;
	private int y;
	private int vX;
	private int vY;
	private int grass;
	private int energyLevel;
	private static int IDNumber = 0;
	private int ID;
	private RabbitsGrassSimulationSpace rgsSpace;
	
	public RabbitsGrassSimulationAgent() {
		x = -1;
		y = -1;
		grass = 0;
		setVxVy();
		energyLevel = 5;
		IDNumber ++;
		ID = IDNumber;
		
	}
	
	private void setVxVy() {
		vX = 0;
		vY = 0;
		while(((vX == 0) && (vY == 0)) || (vX*vY!=0)) {
			vX = (int)Math.floor(Math.random() * 3) - 1;
			vY = (int)Math.floor(Math.random() * 3) - 1;
		}
	}
	
	public void setXY(int newX, int newY) {
		x = newX;
		y = newY;
	}
	
	public void setRabbitsGrassSimulationSpace(RabbitsGrassSimulationSpace rgss) {
		rgsSpace = rgss;
	}
	
	public String getID(){
	    return "A-" + ID;
	  }

	public int getGrass(){
	    return grass;
  		}
	
	public int getEnergyLevel() {
		return energyLevel;
	}
	
	public void report(){
	    System.out.println(getID() +
	                       " at " +
	                       x + ", " + y +
	                       " has " +
	                       getGrass() + " grass" +
	                       " and " +
	                       getEnergyLevel() + " energy level.");
	  }

	public void draw(SimGraphics G) {
		// TODO Auto-generated method stub
		G.drawFastRoundRect(Color.blue);
	}

	public int getX() {
		// TODO Auto-generated method stub
		return x;
	}

	public int getY() {
		// TODO Auto-generated method stub
		return y;
	}
	
	public void step(){		
		int newX = x + vX;
		int newY = y + vY;
		
		Object2DGrid grid = rgsSpace.getCurrentAgentSpace();
		newX = (newX + grid.getSizeX()) % grid.getSizeX();
	    newY = (newY + grid.getSizeY()) % grid.getSizeY();

	    if(tryMove(newX, newY)){
		grass += rgsSpace.takeGrassAt(newX, newY);
		energyLevel += grass;
	    }
	    else {
	    	//RabbitsGrassSimulationAgent cda = rgsSpace.getAgentAt(newX, newY);
	    	setVxVy();
	    }
		energyLevel--;
	}
		
	public void setEnergyLevel(int ne) {
		energyLevel = ne;
	}
	
	public void setGrass(int ne) {
		grass = ne;
	}
	  
	
	private boolean tryMove(int newX, int newY) {
		return rgsSpace.moveRabbitAt(x, y, newX, newY);
	}

}
