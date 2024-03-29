 import java.awt.Color;
 import java.util.ArrayList;

import uchicago.src.sim.analysis.BinDataSource;
import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.OpenHistogram;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.util.SimUtilities;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author 
 */


public class RabbitsGrassSimulationModel extends SimModelImpl {		

	//Default Values
	private static final int NUMINITRABBITS = 10;
	private static final int WORLDSIZE = 20;
	private static final int NUMINITGRASS = 100;
	private static final int BIRTH_THRESHOLD = 10;
	private static final int GRASS_GROWTH_RATE = 10;
	
	private int numInitRabbits = NUMINITRABBITS;
	private int gridSize = WORLDSIZE;
	private int numInitGrass = NUMINITGRASS;
	private int grassGrowthRate = GRASS_GROWTH_RATE;
	private int birthThreshold = BIRTH_THRESHOLD;
	
	private Schedule schedule;

	private RabbitsGrassSimulationSpace rgsSpace;
	
	private ArrayList agentList;
	
	private DisplaySurface displaySurf;
	
	private OpenSequenceGraph amountOfGrassInSpace;
	private OpenSequenceGraph amountOfRabbitsInSpace;
	private OpenHistogram rabbitsGrassDistribution;
	
	class grassInSpace implements DataSource, Sequence {
		
		public Object execute() {
			return new Double(getSValue());
		}
		
		public double getSValue() {
			return (double)rgsSpace.getTotalGrass();
		}
	}
	
	class rabbitsInSpace implements DataSource, Sequence {
		
		public Object execute() {
			return new Double(getSValue());
		}
		
		public double getSValue() {
			return (double) countLivingAgents();
		}
	}
	
	class rabbitGrass implements BinDataSource{
	    public double getBinValue(Object o) {
	      RabbitsGrassSimulationAgent cda = (RabbitsGrassSimulationAgent)o;
	      return (double)cda.getGrass();
	    }
	  }	
	
	public static void main(String[] args) {
			
			System.out.println("Rabbit skeleton");

			SimInit init = new SimInit();
			RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
			// Do "not" modify the following lines of parsing arguments
			if (args.length == 0) // by default, you don't use parameter file nor batch mode 
				init.loadModel(model, "", false);
			else
				init.loadModel(model, args[0], Boolean.parseBoolean(args[1]));
			
		}

		
		public void begin() {
			// TODO Auto-generated method stub
			buildModel();
		    buildSchedule();
		    buildDisplay();		
		    
		    displaySurf.display();
		    amountOfGrassInSpace.display();
		    //amountOfRabbitsInSpace.display();
		    rabbitsGrassDistribution.display();
		}
		
		public void buildModel(){
			System.out.println("Running BuildModel");
			rgsSpace = new RabbitsGrassSimulationSpace(gridSize);
			rgsSpace.spreadGrass(numInitGrass);
			
			for (int i = 0; i < numInitRabbits; i++) {
				addNewAgent();
			}
			for(int i = 0; i < agentList.size(); i++){
		      RabbitsGrassSimulationAgent cda = (RabbitsGrassSimulationAgent)agentList.get(i);
		      cda.report();
		    }
		}

		public void buildSchedule(){
			System.out.println("Running BuildSchedule");
			
			class RabbitsGrassStep extends BasicAction {
			      public void execute() {
			    	SimUtilities.shuffle(agentList);			        
			        for(int i =0; i < agentList.size(); i++){
			          RabbitsGrassSimulationAgent cda = (RabbitsGrassSimulationAgent)agentList.get(i);
			          cda.step();
			          cda.report();
			          if (cda.getEnergyLevel() > birthThreshold) {
			        	  addNewAgent();
			        	  cda.setEnergyLevel(cda.getEnergyLevel() - birthThreshold);
			        	  //cda.setGrass(cda.getEnergyLevel() - birthThreshold);
			          }
			        }
			        
			        rgsSpace.spreadGrass(grassGrowthRate);
			        int deadRabbits = reapDeadRabbits();
			        
			        displaySurf.updateDisplay();
			      }
			    }

		    	schedule.scheduleActionBeginning(0, new RabbitsGrassStep());
		    	
		    	class RabbitsGrassCountLiving extends BasicAction {
		    		public void execute() {
		    			countLivingAgents();
		    		}
		    	}
		    	
		    	schedule.scheduleActionAtInterval(10, new RabbitsGrassCountLiving());
		    	class RabbitsGrassUpdateGrassInSpace extends BasicAction {
	    	      public void execute(){
	    	        amountOfGrassInSpace.step();
	    	      }
	    	      
	    	      
		  }
		   schedule.scheduleActionAtInterval(10, new RabbitsGrassUpdateGrassInSpace());
		   
		   /*class RabbitsGrassUpdateRabbitsInSpace extends BasicAction {
	    	      public void execute(){
	    	        amountOfRabbitsInSpace.step();
	    	      }
		  }
		   schedule.scheduleActionAtInterval(10, new RabbitsGrassUpdateRabbitsInSpace());
		   */
		   class RabbitsGrassSimulationUpdateRabbitsGrass extends BasicAction {
			      public void execute(){
			        rabbitsGrassDistribution.step();
			      }
			    }

			    schedule.scheduleActionAtInterval(10, new RabbitsGrassSimulationUpdateRabbitsGrass());
		}

		public void buildDisplay(){
			System.out.println("Running BuildDisplay");
			ColorMap map = new ColorMap();

		    for (int i = 1; i < 16; i++) {
		    	map.mapColor(16-i, new Color(0, (int) (i*8 + 127), 0));
		    }
		    map.mapColor(0, Color.black);

		    Value2DDisplay displayGrass =
		        new Value2DDisplay(rgsSpace.getCurrentRabbitsGrassSimulationSpace(), map);
		    
		    Object2DDisplay displayAgents = new Object2DDisplay(rgsSpace.getCurrentAgentSpace());
		    displayAgents.setObjectList(agentList);

		    displaySurf.addDisplayableProbeable(displayGrass, "Grass");
		    displaySurf.addDisplayableProbeable(displayAgents, "Agents");
		    
		    amountOfGrassInSpace.addSequence("Grass In Space", new grassInSpace());
		    amountOfGrassInSpace.addSequence("Rabbits in Space", new rabbitsInSpace());
		    rabbitsGrassDistribution.createHistogramItem("Rabbits Grass",agentList,new rabbitGrass());
		  }
		
		private void addNewAgent() {
			RabbitsGrassSimulationAgent a = new RabbitsGrassSimulationAgent();
			agentList.add(a);
			rgsSpace.addAgent(a);
		}
		
		private int reapDeadRabbits(){
		    int count = 0;
		    for(int i = (agentList.size() - 1); i >= 0 ; i--){
		      RabbitsGrassSimulationAgent cda = (RabbitsGrassSimulationAgent)agentList.get(i);
		      if(cda.getEnergyLevel() < 1){
		        rgsSpace.removeRabbitAt(cda.getX(), cda.getY());
		        agentList.remove(i);
		        count++;
		      }
		    }
		    return count;
		  }
		
		private int countLivingAgents() {
			int livingAgents = 0;
			for(int i = 0; i < agentList.size(); i++){
				RabbitsGrassSimulationAgent cda = (RabbitsGrassSimulationAgent)agentList.get(i);
			      if(cda.getEnergyLevel() > 0) livingAgents++;
			    }
			    System.out.println("Number of living agents is: " + livingAgents);

			    return livingAgents;
		}

		public String[] getInitParam() {
			// TODO Auto-generated method stub
			// Parameters to be set by users via the Repast UI slider bar
			// Do "not" modify the parameters names provided in the skeleton code, you can add more if you want 
			String[] params = { "GridSize", "NumInitRabbits", "NumInitGrass", "GrassGrowthRate", "BirthThreshold", "RabbitMinLifespan", "RabbitMaxLifespan"};
			return params;
		}

		public String getName() {
			// TODO Auto-generated method stub
			return "Rabbits and Grass";
		}

		public Schedule getSchedule() {
			// TODO Auto-generated method stub
			return schedule;
		}

		public void setup() {
			// TODO Auto-generated method stub
			rgsSpace = null;
			agentList = new ArrayList();
		    schedule = new Schedule(1);
			
			if (displaySurf != null){
		      displaySurf.dispose();
		    }
		    displaySurf = null;
		    
		    if(amountOfGrassInSpace != null) {
		    	amountOfGrassInSpace.dispose();
		    }
		    amountOfGrassInSpace = null;
		    
		    if(amountOfRabbitsInSpace != null) {
		    	amountOfRabbitsInSpace.dispose();
		    }
		    amountOfRabbitsInSpace = null;
		    
		    if (rabbitsGrassDistribution != null){
		        rabbitsGrassDistribution.dispose();
		      }
		      rabbitsGrassDistribution = null;
		    
	    	//Create Displays
		    displaySurf = new DisplaySurface(this, "Rabbits Model Window 1");
		    amountOfGrassInSpace = new OpenSequenceGraph("Amount of Grass In Space", this);
		    //amountOfRabbitsInSpace = new OpenSequenceGraph("Amount of Rabbits In Space", this);
		    rabbitsGrassDistribution = new OpenHistogram("Rabbits Grass", 8, 0);
		    
		    //Register Displays
		    registerDisplaySurface("Rabbits Model Window 1", displaySurf);
		    this.registerMediaProducer("Plot", amountOfGrassInSpace);
		    //this.registerMediaProducer("Plot", amountOfRabbitsInSpace);
		}
		
		public int getNumInitRabbits(){
		    return numInitRabbits;
		  }

		public void setNumInitRabbits(int na){
		    numInitRabbits = na;
		  }
		
		public int getGridSize(){
		    return gridSize;
		  }

		public void setGridSize(int na){
		    gridSize = na;
		  }
		
		public int getNumInitGrass(){
		    return numInitGrass;
		  }

		public void setNumInitGrass(int na){
		    numInitGrass = na;
		  }
		
		public int getGrassGrowthRate(){
		    return grassGrowthRate;
		  }

		public void setGrassGrowthRate(int na){
		    grassGrowthRate = na;
		  }
		
		public int getBirthThreshold(){
		    return birthThreshold;
		  }

		public void setBirthThreshold(int na){
		    birthThreshold = na;
		  }
}
