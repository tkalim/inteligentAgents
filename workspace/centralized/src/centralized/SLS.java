package centralized;
/* import table */
import logist.simulation.Vehicle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class SLS {
    private List<Vehicle> vehicles;
    private TaskSet tasks;

    public SLS(List<Vehicle> vehicles, TaskSet tasks){
      this.vehicles = vehicles;
      this.tasks = tasks;
    }

    public List<Plan> plan() {
        long time_start = System.currentTimeMillis();

//		System.out.println("Agent " + agent.id() + " has tasks " + tasks);
        Plan planVehicle1 = slsAlgorithm(vehicles, tasks);

        List<Plan> plans = new ArrayList<Plan>();
        plans.add(planVehicle1);
        while (plans.size() < vehicles.size()) {
            plans.add(Plan.EMPTY);
        }

        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
        System.out.println("The plan was generated in " + duration + " milliseconds.");

        return plans;
    }

    private Plan slsAlgorithm(List<Vehicle> vehicles, TaskSet tasks) {
        Solution A = selectInitialSolution(vehicles, tasks);

        Solution bestSolution = new Solution(A);
        // put the time and # of iterations
        while(){
          Solution oldA = new Solution(A);

          ArrayList<Solution> N = chooseNeighbours(oldA);

          A = localChoice(N, oldA);
        }

        return plan;
    }

    private Solution selectInitialSolution(List<Vehicle> vehicles, TaskSet tasks){
    // construct a solution with the largest vehicle carrying all the tasks in P1,D1,P2,D2...Pn,Dn fashinon
      Solution A = new Solution(vehicles);

      int largestVehicleIdx = largestVehicleIndex(vehicles);


      for(Task t : tasks) {
    	  A.solution.get(largestVehicleIdx).nextTask.add(new TaskTypeTuple(t, "PickUp"));
    	  A.solution.get(largestVehicleIdx).nextTask.add(new TaskTypeTuple(t, "Delivery"));
      }

      return A;
    }

	public int largestVehicleIndex(List<Vehicle> vehicles){
		Vehicle maxCapacityVehicle = vehicles.get(0);
		int index = 0;
		for(int i = 0 ; i < vehicles.size(); i++) {
			if(vehicles.get(i).capacity() > maxCapacityVehicle.capacity()) {
				maxCapacityVehicle = vehicles.get(i);
				index = i;
			}
		}
		return index;
	}
	
	public int randomVehicleIndex(Solution A) {
		// choose random vehicle which has at least one task
		while(true) {
			Random r = new Random();
    		int randomIdx = r.nextInt(A.solution.size());
    		if(!A.solution.get(randomIdx).nextTask.isEmpty())
				return randomIdx;
		}
	}
	
	class SolutionComparator implements Comparator<Solution> {
	    public int compare(Solution a, Solution b) {
	    	double aCost = a.getCost();
	    	double bCost = b.getCost();
	    	if(aCost == bCost) {
	    		// flip-coin
	    		Random r = new Random();
	    		int chance = r.nextInt(2);
	    		if(chance == 1)
	    			return 1;
	    		return -1;
	    	}
	    	return Double.compare(a.getCost(), b.getCost());
	    }
	}

	public Solution localChoice(List<Solution> N, Solution oldA) {
	    Random generator = new Random();
	    int probability = generator.nextInt(10) + 1;
	    int threshold = 3;
	    if(probability <= threshold && !N.isEmpty())
	      return Collections.min(N, new SolutionComparator());
	    return oldA;
	}
	
	public Vehicle 

//	public ArrayList<Solution> chooseNeighbours(Solution oldA){
//		oldA.solution.get(index)
//	}

}
