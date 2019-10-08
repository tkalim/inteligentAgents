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
	private HashMap<StateAction, Double> rewardStateAction = new HashMap<StateAction, Double>();
	private HashMap<StateActionStateP, Double> transitionProbability = new HashMap<StateActionStateP, Double>();

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class,
				0.95);

		this.numActions = 0;
		this.myAgent = agent;

		// list of tasks
		tasks = new ArrayList<City>(topology.cities());
		//System.out.println("tasks = " + tasks);

		// The state of taking no task
		tasks.add(null);

		Boolean convergence = false;
		while(convergence == false){
			// we assume it is converging unless we found better improvement
			convergence = true;

			for (City city : topology) {
				for (City task : tasks) {
					//System.out.println("City " + city + " task" + task);

					// skip state with task of the same city
					if (!city.equals(task)){

						// initiliaze current state
						State state = new State(city, task);
						System.out.println("testing state " + state);

						// initiliaze the current Q value to the minimum (0)
						double bestQValue = 0;
						City bestActionQvalue = null;

						// legal destination from state = neighboring cities + city of the task
						HashSet<City> legalDestinationsOfState =
													new HashSet<City>(state.getCity().neighbors());
						if(state.getTask() != null)
							legalDestinationsOfState.add(state.getTask());

						//System.out.println("legalDestinationsOfState = " + legalDestinationsOfState);
						// compute Qvalue of all potention actions from the current state
						for(City action : legalDestinationsOfState){
							double QValue = reward(state, action, td, agent) +
															discount * sigmaTransitionProb(state, action, td);
							//ystem.out.println("legalDestination = " + action + "QValue " + QValue);
							if(bestQValue < QValue){
								bestQValue = QValue;
								bestActionQvalue = action;
							}
						}

						System.out.println("state " + state + " bestQValue " + bestQValue);
						if(!bestStateActionQValue.getOrDefault(state, 0.0).equals(bestQValue)){
							bestStateActionQValue.put(state, bestQValue);
							bestStateAction.put(state, bestActionQvalue);
							convergence = false;
						}
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

		// store the reward
		// memoize the result for optimization and store it in a table
		StateAction stateAction = new StateAction(state, action);
		rewardStateAction.put(stateAction, reward - cost);
		
		return reward - cost;
	}

	private Double sigmaTransitionProb(State state, City action, TaskDistribution td){
		// Iterate over all possible state prime
		Double q = 0.0;
		for (City taskP : tasks) {
			State stateP = new State(action, taskP);
			q += transitionProbability(state, action, stateP, td) * bestStateActionQValue.getOrDefault(stateP, 0.0);
			
			// memoize the result
			StateActionStateP stateActionStateP = new StateActionStateP(state, action, stateP); 
			transitionProbability.put(stateActionStateP, transitionProbability(state, action, stateP, td));
			
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

		// if best destination from current state has pick-up otherwise just move to it
		if (dest.equals(state.getTask()))
			action = new Pickup(availableTask);
		else
			action = new Move(dest);

		numActions++;

		if (numActions >= 1 && numActions % 100 == 0) {
			System.out.println("The total profit after "+numActions+" actions is "+myAgent.getTotalProfit());
			System.out.println("The average profit: "+(myAgent.getTotalProfit() / (double)numActions));
		}

		return action;
	}


}
