package reactive;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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

	private Random random;
	private double pPickup;
	private int numActions;
	private Agent myAgent;
	private HashMap<State, Action_at> bestOfStateMap;
	private HashMap<State, Double> valueOfStateMap;
	private HashMap<State, HashMap<Action_at, Double>> qMap;

	public ReactiveSimulation(){
			bestOfStateMap = new HashMap<State, Action_at>();
			valueOfStateMap = new HashMap<State, Double>();
			qMap = new HashMap<State, HashMap<Action_at, Double>>();

	}

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class,
				0.95);

		//get the cost per km
		double costPerKm = agent.vehicles().get(0).costPerKm();

		this.random = new Random();
		this.pPickup = discount;
		this.numActions = 0;
		this.myAgent = agent;


		//Creating the states
		HashMap<Integer, State> states_map = new HashMap<Integer, State>();
		int id = 0;
		for (City from : topology) {
			for(City destination: topology) {
				Task task = new Task(id, from, destination, td.reward(from, destination), td.weight(from, destination));
				State state = new State(from, task);
				states_map.put(state.hashCode(), state);
				id++;
			}
		}

		// new statesMap
		// Map<City, HashMap<Task, State>> statesMap = new HashMap<City, HashMap<Task, State>>();
		// for (City from : topology) {
		// 	Map<Task, State> statesTaskMap = new HashMap<Task, State>();
		// 	for(City destination: topology) {
		// 		Task task = new Task(id, from, destination, td.reward(from, destination), td.weight(from, destination));
		//
		// 	}
		// }

		//Creating the actions
		HashMap<Integer, Action_at> actionsMap = new HashMap<Integer, Action_at>();
		int action_id = 0;
		for (City currentCity : topology) {
			Action_at actionAccept = new Action_at(true, currentCity);
			Action_at actionRefuse = new Action_at(false, currentCity);
			actionsMap.put(action_id, actionAccept);
			action_id++;
			actionsMap.put(action_id, actionRefuse);
		}

		//filling the Reward table
		HashMap<State, HashMap<Action_at, Double>> rewardMap = new HashMap<State, HashMap<Action_at, Double>>();
		for (State state : states_map.values()) {
			HashMap<Action_at, Double> rewardActionMap = new HashMap<Action_at, Double>();
			for (Action_at action : actionsMap.values()) {
				double reward;
				if(action.isAccept_task()){
						reward = state.getAvailable_task().reward - costPerKm * state.getAvailable_task().pathLength();
				}
				else {
					reward = - costPerKm * action.getNext_city().distanceTo(state.getCurrentCity());
				}
				rewardActionMap.put(action, reward);
			}
			rewardMap.put(state, rewardActionMap);
		}

		//filling Transition Probability table
		HashMap<State, HashMap<State, HashMap<Action_at, Double>>> transitionMap = new HashMap<State, HashMap<State, HashMap< Action_at, Double>>>();
		for (State statePrime : states_map.values()) {
			HashMap<State, HashMap<Action_at, Double>> transitionStateMap = new HashMap<State, HashMap<Action_at, Double>>();
			for (State state : states_map.values()) {
				HashMap<Action_at, Double> transitionStateActionMap = new HashMap<Action_at, Double>();
				for (Action_at action : actionsMap.values()) {
					Double transitionProbability = td.probability(action.getNext_city(), statePrime.getAvailable_task().deliveryCity);
					transitionStateActionMap.put(action, transitionProbability);
				}
				transitionStateMap.put(state, transitionStateActionMap);
			}
			transitionMap.put(statePrime, transitionStateMap);
		}


		//Offline training algorithm
		//init of value of S
		for (State state : states_map.values()){
			valueOfStateMap.put(state, 1.0);
		}
		while (true) {
			Boolean convergence = true;
			for (State state : states_map.values()) {
				HashMap<Action_at, Double> qMapAction = new HashMap<Action_at, Double>();
				Double maximum_qValue = 0.0;
				Action_at maximum_qValue_action = new Action_at();
				for (Action_at action : actionsMap.values()){
					Double reward = rewardMap.get(state).get(action);
					//computing sigma Tconvergence
					Double sigmaT = 0.0;

					for (State statePrime : states_map.values()){
						if(valueOfStateMap.containsKey(statePrime)){
							sigmaT += transitionMap.get(statePrime).get(state).get(action) * valueOfStateMap.get(statePrime);
						}
					}
					Double qValue = reward + discount*sigmaT;
					qMapAction.put(action, qValue);
					if(qValue > maximum_qValue){
						maximum_qValue = qValue;
						maximum_qValue_action = action;
					}
					if(Math.abs(qValue - maximum_qValue) < 1e-1 * maximum_qValue){
						convergence = false;
					}
					System.out.println("State hashcode " + state.hashCode() + " qvalue : " + Double.toString(qValue) + " maximum_qValue " + Double.toString(maximum_qValue));
				}
				qMap.put(state, qMapAction);
				valueOfStateMap.put(state, maximum_qValue);
				bestOfStateMap.put(state, maximum_qValue_action);
			}
		}
		}

		private State getCurrentState(Vehicle vehicle, Task availableTask) {
			City currentCity = vehicle.getCurrentCity();
			return new State(currentCity, availableTask);
		}

		@Override
		public Action act(Vehicle vehicle, Task availableTask) {
			Action action;

			State currentState = getCurrentState(vehicle, availableTask);
			City currentCity = vehicle.getCurrentCity();
			Action_at bestAction = bestOfStateMap.get(currentState);

			if (bestAction.isAccept_task()) {
				action = new Pickup(availableTask);
			}
			else {
				action = new Move(bestAction.getNext_city());
			}
			
			numActions++;

			if (numActions >= 1 && numActions % 100 == 0) {
				System.out.println("The total profit after "+numActions+" actions is "+myAgent.getTotalProfit());
				System.out.println("The average profit: "+(myAgent.getTotalProfit() / (double)numActions));
			}

			return action;
		}

	// @Override
	// public Action act(Vehicle vehicle, Task availableTask) {
	// 	Action action;
	//
	// 	if (availableTask == null || random.nextDouble() > pPickup) {
	// 		City currentCity = vehicle.getCurrentCity();
	// 		action = new Move(currentCity.randomNeighbor(random));
	// 	} else {
	// 		action = new Pickup(availableTask);
	// 	}
	//
	// 	if (numActions >= 1) {
	// 		System.out.println("The total profit after "+numActions+" actions is "+myAgent.getTotalProfit()+" (average profit: "+(myAgent.getTotalProfit() / (double)numActions)+")");
	// 	}
	// 	numActions++;
	//
	// 	return action;
	// }


}
