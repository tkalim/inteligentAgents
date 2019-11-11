package template;
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

public class Naive {
    private List<Vehicle> vehicles;
    private TaskSet tasks;

    public Naive(List<Vehicle> vehicles, TaskSet tasks){
      this.vehicles = vehicles;
      this.tasks = tasks;
    }

    public List<Plan> plan() {
        long time_start = System.currentTimeMillis();

//		System.out.println("Agent " + agent.id() + " has tasks " + tasks);
        Plan planVehicle1 = naiveAlgorithm(vehicles.get(0), tasks);

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

    private Plan naiveAlgorithm(Vehicle vehicle, TaskSet tasks) {
        City current = vehicle.getCurrentCity();
        Plan plan = new Plan(current);

        for (Task task : tasks) {
            // move: current city => pickup location
            for (City city : current.pathTo(task.pickupCity)) {
                plan.appendMove(city);
            }

            plan.appendPickup(task);

            // move: pickup location => delivery location
            for (City city : task.path()) {
                plan.appendMove(city);
            }

            plan.appendDelivery(task);

            // set current city
            current = task.deliveryCity;
        }
        return plan;
    }

}
