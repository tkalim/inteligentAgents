package deliberative;

import logist.task.Task;
import logist.task.TaskSet;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import logist.plan.Action;
import logist.plan.Action.Delivery;
import logist.plan.Action.Pickup;
import logist.topology.Topology.City;

public class BFS {

	private State initialState;
	private Vehicle vehicle;
	private City initialCity;
	private TaskSet tasks;
	private TaskSet currentTasks;
	public HashMap<State, State> parentState;

	public BFS(Vehicle vehicle, TaskSet tasks) {

		this.vehicle = vehicle;
		this.tasks = tasks;
		this.initialCity = vehicle.getCurrentCity();
		this.currentTasks = vehicle.getCurrentTasks();

		initialState = new State(vehicle, initialCity, tasks, currentTasks, null, 0.0);
		this.parentState = new HashMap<State, State>();
	}

	public Plan search() {

		long startTime = System.nanoTime();

		// create the queue and keep track of visited states
		Queue<State> queue = new LinkedList<State>();
		Set<State> visitedStates = new HashSet<State>();
		Vehicle vehicle = this.vehicle;
		City initialCity = this.initialCity;
		TaskSet tasks = this.tasks;
		State minCostState = null;

		// start with the initial state
		queue.add(initialState);
		visitedStates.add(initialState);
		int nbExploredGoalState = 0;
		int nbExploredState = 0;
		int nbExploreGoalStateBeforeFindingOptimal = 0;
		int nbExploreStateBeforeFindingOptimal = 0;

		while (queue.size() != 0) {
			State state = queue.poll();

			if(state.isGoalState()) {
				nbExploredGoalState++;
			}
			nbExploredState++;

			if(state.isGoalState() &&
			(minCostState == null || minCostState.getAccumulatedCost() > state.getAccumulatedCost())){
				minCostState = state;
				nbExploreGoalStateBeforeFindingOptimal = nbExploredGoalState;
				nbExploreStateBeforeFindingOptimal = nbExploredState;
			}

			for (State nextState : state.nextLegalStates()) {
				if (!visitedStates.contains(nextState)) {
					this.parentState.put(nextState, state);
					queue.add(nextState);
					visitedStates.add(nextState);
				}
			}
		}

		// printing statistics
		System.out.println("nbExploreGoalStateBeforeFindingOptimal = " + String.valueOf(nbExploreGoalStateBeforeFindingOptimal));
		System.out.println("nbExploreStateBeforeFindingOptimal = " + String.valueOf(nbExploreStateBeforeFindingOptimal));

		if (minCostState == null) {
			throw new AssertionError("No goal state found !");
		}
		else {
			return getPlan(minCostState);
		}

	}
	public Plan getPlan(State state) {
		State currentState = state;
		ArrayList<Action> actionList = new ArrayList<Action>();

		// list to help fill in the move() actions
		ArrayList<City> citiesList = new ArrayList<City>();
		while (this.parentState.containsKey(currentState)) {
			Action action = currentState.getAction();
			citiesList.add(currentState.getCurrentCity());
			currentState = parentState.get(currentState);
			actionList.add(action);
		}
		City tempCity = this.initialCity;
		Plan plan = new Plan(initialCity);
		for (int i = actionList.size() - 1;  i >= 0 ; i--) {
			Action action = actionList.get(i);
			for (City city : tempCity.pathTo(citiesList.get(i))) {
				plan.appendMove(city);
			}
			tempCity = citiesList.get(i);
			plan.append(action);
		}
		return plan;
	}

}
