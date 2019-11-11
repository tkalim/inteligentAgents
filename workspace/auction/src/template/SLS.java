package template;
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
    long timeout;
    long time_start;
	Random r;


    public SLS(List<Vehicle> vehicles, TaskSet tasks, long timeout){
      this.vehicles = vehicles;
      this.tasks = tasks;
      this.timeout = timeout;
      this.r = new Random();
    }

    public List<Plan> plan() {
        time_start = System.currentTimeMillis();

        List<Plan> plans = slsAlgorithm(vehicles, tasks);

        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
        System.out.println("The plan was generated in " + duration + " milliseconds.");

        return plans;
    }

    private List<Plan> slsAlgorithm(List<Vehicle> vehicles, TaskSet tasks) {
        //Solution A = selectInitialSolutionLargestVehicle(vehicles, tasks);
        //Solution A = selectInitialSolutionRandomVehicle(vehicles, tasks);
    	Solution A = selectInitialSolutionClosestVehicle(vehicles, tasks);
        
        System.out.println("InitialSolution: \n" + A);
        
        int max_iter = 1000000;
        int iter = 0;
        
        // put the time and # of iterations
        while(max_iter > iter && !isTimeout()){
          Solution oldA = new Solution(A);

          ArrayList<Solution> N = chooseNeighbours(oldA);

          A = localChoice(N, oldA);
          
          iter += 1;
        }
        
        System.out.println("Number of iterations: " + iter);
        System.out.println("Final Solution: \n " + A);

        return A.getPlans();
    }
    
    private boolean isTimeout() {
    	return System.currentTimeMillis() - time_start >= 0.95*timeout;
    }
    
    private Solution selectInitialSolutionLargestVehicle(List<Vehicle> vehicles, TaskSet tasks){
    // construct a solution with the largest vehicle carrying all the tasks in P1,D1,P2,D2...Pn,Dn fashion
      Solution A = new Solution(vehicles);

      int largestVehicleIdx = largestVehicleIndex(vehicles);


      for(Task t : tasks) {
    	  A.solution.get(largestVehicleIdx).nextTask.add(new TaskTypeTuple(t, "PickUp"));
    	  A.solution.get(largestVehicleIdx).nextTask.add(new TaskTypeTuple(t, "Delivery"));
      }

      return A;
    }
    
    private Solution selectInitialSolutionRandomVehicle(List<Vehicle> vehicles, TaskSet tasks){
    // construct a solution with the largest vehicle carrying all the tasks in P1,D1,P2,D2...Pn,Dn fashion
      Solution A = new Solution(vehicles);


      for(Task t : tasks) {
    	  int randomVehicleIdx;
    	  // find a random vehicle that is able to carry the task
    	  while(true) {
    		  randomVehicleIdx = r.nextInt(vehicles.size());
    		  // add it only if the vehicle is able to carry it
    		  if(A.solution.get(randomVehicleIdx).vehicle.capacity() - A.solution.get(randomVehicleIdx).getCurrentLoad() >= t.weight) {
    			  break;
    		  }
    	  }
    	  A.solution.get(randomVehicleIdx).nextTask.add(new TaskTypeTuple(t, "PickUp"));
    	  A.solution.get(randomVehicleIdx).nextTask.add(new TaskTypeTuple(t, "Delivery"));
      }

      return A;
    }
    
    private Solution selectInitialSolutionClosestVehicle(List<Vehicle> vehicles, TaskSet tasks){
      Solution A = new Solution(vehicles);

      // assign a task to the closest vehicle
      for(Task t : tasks) {
    	  int closestVehicleIdx = -1;
    	  double closestVehicleDistance = Double.MAX_VALUE;
    	  for(int i = 0; i < vehicles.size(); i++) {
    		  if(closestVehicleDistance > t.pickupCity.distanceTo(A.solution.get(i).vehicle.homeCity())
    				&& A.solution.get(i).vehicle.capacity() - A.solution.get(i).getCurrentLoad() >= t.weight) {
    			  closestVehicleIdx = i;
    			  closestVehicleDistance = t.pickupCity.distanceTo(A.solution.get(i).vehicle.homeCity());
    		  }
    	  }
    	  A.solution.get(closestVehicleIdx).nextTask.add(new TaskTypeTuple(t, "PickUp"));
    	  A.solution.get(closestVehicleIdx).nextTask.add(new TaskTypeTuple(t, "Delivery"));
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
	    		int chance = r.nextInt(2);
	    		if(chance == 1)
	    			return 1;
	    		return -1;
	    	}
	    	return Double.compare(a.getCost(), b.getCost());
	    }
	}

	public Solution localChoice(List<Solution> N, Solution oldA) {
	    int probability = r.nextInt(10) + 1;
	    int threshold = 3;
	    if(probability <= threshold && !N.isEmpty())
	      return Collections.min(N, new SolutionComparator());
	    return oldA;
	}
	
	public ArrayList<Solution> chooseNeighbours(Solution A){
		ArrayList<Solution> N = new ArrayList<Solution>();
		int randomVehicleIdx = randomVehicleIndex(A);
		
		// Applying the changing vehicle operator
		for(int i = 0 ; i < A.solution.size(); i++) {
			if(randomVehicleIdx != i) {
				Solution newA = VehiclePlan.changingVehicle(A, randomVehicleIdx, i);
				if(newA != null)
					N.add(newA);		
			}
		}
		
		if(A.solution.get(randomVehicleIdx).nextTask.size() >= 2) {
			for(int i = 0 ; i < A.solution.get(randomVehicleIdx).nextTask.size() - 1; i++) {
				for(int j = i + 1; j < A.solution.get(randomVehicleIdx).nextTask.size(); j++) {
					Solution newA = VehiclePlan.changingTaskOrder(A, randomVehicleIdx, i, j);
					if(newA != null)
						N.add(newA);	
				}
			}
		}
		
		return N;
	}

}
