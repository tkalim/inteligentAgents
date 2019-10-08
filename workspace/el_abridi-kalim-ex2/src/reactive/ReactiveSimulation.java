package reactive;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.HashSet;

import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class ReactiveSimulation implements ReactiveBehavior {

	private int numActions;
	private Agent myAgent;

	// TODO: comment these
	private HashMap<State, City> bestStateAction = new HashMap<State, City>();
	private HashMap<State, Double> bestStateActionQValue = new HashMap<State, Double>();
	private ArrayList<City> tasks;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class,
				0.95);

		this.numActions = 0;
		this.myAgent = agent;

		// list of tasks
		tasks = new ArrayList<Topology.City>(topology.cities());
		//System.out.println("tasks = " + tasks);
		//System.out.println("here1");

		// The state of being in the same city (no task)
		tasks.add(null);
		Boolean convergence = false;
		while(convergence == false){
			// we assume it is converging unless we found better improvement
			convergence = true;
			for (City city : topology) {
				for (City task : tasks) {
					//System.out.println("City " + city + " task" + task);
					// skip state with task of the same city
					if (city.equals(task))
						continue;
						// initiliaze current state
						State state = new State(city, task);
						System.out.println("testing state " + state);

						// initiliaze the current Q value to the minimum with
						// current bestActionQvalue correspondigly
						double bestQValue = 0;
						City bestActionQvalue = null;

						// legal destination from state =
						// neighboring cities + city of task
						HashSet<City> legalDestinationsOfState =
													new HashSet();
						//System.out.println("neighbord of = " state.getCity() + " : " + state.getCity().neighbors());
						legalDestinationsOfState.addAll(state.getCity().neighbors());
						//TODO: comment this one
						if(state.getTask() != null)
							legalDestinationsOfState.add(state.getTask());

						//System.out.println("legalDestinationsOfState = " + legalDestinationsOfState);
						// compute Qvalue of all potention actions from the current state
						for(City action : legalDestinationsOfState){
							if(task == null)
								System.out.println("I AM HERE");
							double QValue = reward(state, action, td, agent) +
															discount * sigmaTransitionProb(state, action, td);
							if(task == null)
								System.out.println("Qvalue = " + QValue);
							//ystem.out.println("legalDestination = " + action + "QValue " + QValue);
							if(bestQValue < QValue){
								bestQValue = QValue;
								bestActionQvalue = action;
							}
						}
						System.out.println("state " + state + " bestQValue " + bestQValue);
						if(!bestStateActionQValue.getOrDefault(state, 0.0).equals(bestQValue)){
							System.out.println("I AM HERE2" + bestQValue);
							bestStateActionQValue.put(state, bestQValue);
							bestStateAction.put(state, bestActionQvalue);
							convergence = false;
						}

				}
			}
		}

		// System.out.println("****************************");
		// for (HashMap.Entry<State, City> entry : bestStateAction.entrySet()) {
		// 	System.out.println(entry.getKey() + "=" + entry.getValue());
		// }
		// for (HashMap.Entry<State, Double> entry : bestStateActionQValue.entrySet()) {
		// 	System.out.println(entry.getKey() + "=" + entry.getValue());
		// }

	}

	private Double reward(State state, City action, TaskDistribution td, Agent agent){
		Double reward = action.equals(state.getTask()) ? td.reward(state.getCity(), action) : 0.0;
		Double cost = state.getCity().distanceTo(action) * agent.vehicles().get(0).costPerKm();
		return reward - cost;
	}

	private Double sigmaTransitionProb(State state, City action, TaskDistribution td){
		// For all possible next state' (prime)
		Double q = 0.0;
		for (City taskP : tasks) {
			State stateP = new State(action, taskP);
			q += transitionProbability(state, action, stateP, td) * bestStateActionQValue.getOrDefault(stateP, 0.0);
		}
		return q;

	}

	// probability to reach state stateP from State by taking action Action
	private double transitionProbability(State state, City action, State stateP, TaskDistribution td) {
		return state.getCity().equals(action) || state.getCity().equals(state.getTask())
				|| state.getCity().equals(stateP.getCity())
				|| !stateP.getCity().equals(action) ? 0.0 : td.probability(stateP.getCity(), stateP.getTask());
	}


	@Override
	public Action act(Vehicle vehicle, Task availableTask) {

		System.out.println("****************************");
		for (HashMap.Entry<State, City> entry : bestStateAction.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue() + " value = " + bestStateActionQValue.get(entry.getKey()));
		}

		Action action;

		City city = vehicle.getCurrentCity();
		State state = new State(city, availableTask == null ? null : availableTask.deliveryCity);
		System.out.println("state: " + state);
		City dest = bestStateAction.get(state);
		System.out.println("destination city:" + dest);

		if (dest.equals(state.getTask())) {
			action = new Pickup(availableTask);
		}
		else {
			action = new Move(dest);
		}

		numActions++;

		if (numActions >= 1 && numActions % 100 == 0) {
			System.out.println("The total profit after "+numActions+" actions is "+myAgent.getTotalProfit());
			System.out.println("The average profit: "+(myAgent.getTotalProfit() / (double)numActions));
		}

		return action;
	}


}
