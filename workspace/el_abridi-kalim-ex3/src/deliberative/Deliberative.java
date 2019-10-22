package deliberative;
/* import table */
import logist.simulation.Vehicle;
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
		BFS, ASTAR
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

		// Compute the plan with the selected algorithm.
		switch (algorithm) {
		case ASTAR:
			// ...
			// TODO: go back to naive implementation for analysis
			//plan = naivePlan(vehicle, tasks);
			System.out.println("Algorithm used for search: ASTAR");
			AStar aStar = new AStar(vehicle, tasks);
			plan = aStar.search();
			System.out.println(plan.toString());
			break;
		case BFS:
			// ...
			System.out.println("Algorithm used for search: BFS");
			BFS bfs = new BFS(vehicle, tasks);
			plan = bfs.search();
			break;
		default:
			throw new AssertionError("Should not happen.");
		}
		System.out.println(plan.toString());
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
