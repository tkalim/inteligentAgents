package centralized;
/* import table */
import logist.simulation.Vehicle;

import java.util.ArrayList;
import java.util.List;
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
        Plan planVehicle1 = slsAlgorithm(vehicles.get(0), tasks);

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

    private Plan slsAlgorithm(Vehicle vehicle, TaskSet tasks) {
        solution A = selectInitialSolution();

        solution bestSolution = new Solution(A);
        // put the time and # of iterations
        while(){
          Solution oldA = new Solution(A);

          ArrayList<Solution> N = chooseNeighbours(oldA);

          A = localChoice(N, oldA);
        }
        
        return plan;
    }

}
