package deliberative;
/* import table */
import logist.simulation.Vehicle;

import java.util.concurrent.TimeUnit;

import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * An optimal planner for one vehicle.
 */
@SuppressWarnings("unused")
public class Deliberative implements DeliberativeBehavior {

	enum Algorithm {
		BFS, ASTAR, NAIVE, ASTARCONSTANT
	}

	/* Environment */
	Topology topology;
	TaskDistribution td;

	/* the properties of the agent */
	Agent agent;
	int capacity;
	TaskSet carryingTasks;

	/* the planning class */
	Algorithm algorithm;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		this.topology = topology;
		this.td = td;
		this.agent = agent;

		// initialize the planner
		int capacity = agent.vehicles().get(0).capacity();
		String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");

		// Throws IllegalArgumentException if algorithm is unknown
		algorithm = Algorithm.valueOf(algorithmName.toUpperCase());

		// ...
	}

	@Override
	public Plan plan(Vehicle vehicle, TaskSet tasks) {
		Plan plan;
		
		System.out.println("Vehicle name " + vehicle.name());
		System.out.println("Vehicle homecity " + vehicle.homeCity());

		// Compute the plan with the selected algorithm.
		switch (algorithm) {
		case ASTAR:
			long startTimeASTAR = System.currentTimeMillis();

			AStar aStar = new AStar(vehicle, tasks);
			plan = aStar.search();

			long elapsedTimeASTAR = System.currentTimeMillis() - startTimeASTAR;

			System.out.println("AStar Algorithm");
			System.out.println("elapsedTimeBFS " + elapsedTimeASTAR + " Milliseconds");
			System.out.println("tasks.size() " + tasks.size());
			System.out.println("optimal plan distance " + plan.totalDistance());
			System.out.println("----------------------------------");

			break;
		case BFS:
			long startTimeBFS = System.currentTimeMillis();

			BFS bfs = new BFS(vehicle, tasks);
			plan = bfs.search();

			long elapsedTimeBFS = System.currentTimeMillis() - startTimeBFS;

			System.out.println("BFS Algorithm");
			System.out.println("elapsedTimeBFS " + elapsedTimeBFS + " Milliseconds");
			System.out.println("tasks.size() " + tasks.size());
			System.out.println("optimal plan distance " + plan.totalDistance());
			System.out.println("----------------------------------");
			break;

			case NAIVE:
				long startTimeNAIVE = System.currentTimeMillis();

				plan = naivePlan(vehicle, tasks);

				long elapsedTimeNAIVE = System.currentTimeMillis() - startTimeNAIVE;

				System.out.println("NAIVE Algorithm");
				System.out.println("elapsedTimeBFS " + elapsedTimeNAIVE + " Milliseconds");
				System.out.println("tasks.size() " + tasks.size());
				System.out.println("optimal plan distance " + plan.totalDistance());
				System.out.println("----------------------------------");
				break;

			case ASTARCONSTANT:
				long startTimeASTARCONSTANT = System.currentTimeMillis();

				AStarConstant aStarConstant = new AStarConstant(vehicle, tasks);
				plan = aStarConstant.search();

				long elapsedTimeASTARCONSTANT = System.currentTimeMillis() - startTimeASTARCONSTANT;

				System.out.println("ASTARCONSTANT Algorithm");
				System.out.println("elapsedTimeBFS " + elapsedTimeASTARCONSTANT + " Milliseconds");
				System.out.println("tasks.size() " + tasks.size());
				System.out.println("optimal plan distance " + plan.totalDistance());
				System.out.println("----------------------------------");
				break;
		default:
			throw new AssertionError("Should not happen.");
		}
		return plan;
	}

	private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);

		for (Task task : tasks) {
			// move: current city => pickup location
			for (City city : current.pathTo(task.pickupCity))
				plan.appendMove(city);

			plan.appendPickup(task);

			// move: pickup location => delivery location
			for (City city : task.path())
				plan.appendMove(city);

			plan.appendDelivery(task);

			// set current city
			current = task.deliveryCity;
		}
		return plan;
	}

	@Override
	public void planCancelled(TaskSet carriedTasks) {
			//this is already handled due to the fact that in BFS and A*,
			//the initial state is initialised using vehicle.getCurrentTasks()
	}
}
