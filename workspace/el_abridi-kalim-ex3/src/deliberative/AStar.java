package deliberative;

import logist.task.Task;
import logist.task.TaskSet;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import logist.plan.Action;
import logist.plan.Action.Delivery;
import logist.plan.Action.Pickup;
import logist.topology.Topology.City;

public class AStar {

	private State initialState;
	private Vehicle vehicle;
	private City initialCity;
	private TaskSet tasks;
	private TaskSet currentTasks;
	public HashMap<State, State> parentState;

	public AStar(Vehicle vehicle, TaskSet tasks) {

		this.vehicle = vehicle;
		this.tasks = tasks;
		this.initialCity = vehicle.getCurrentCity();
		this.currentTasks = vehicle.getCurrentTasks();

		//TODO: Initial accumulatedCost should maybe != 0 (will depend on logist handling canceled plans)
		initialState = new State(vehicle, initialCity, tasks, currentTasks, null, 0.0);
		this.parentState = new HashMap<State, State>();
	}

	public Plan search() {

		// create the priority queue and keep track of visited states
		//The queue is not sorted, it's the list of child states that is
		//PriorityQueue<State> queue = new PriorityQueue<State>(new StateComparator());
		PriorityQueue<State> queue = new PriorityQueue<State>(new StateComparator());
		Set<State> visitedStates = new HashSet<State>();
		Vehicle vehicle = this.vehicle;
		City initialCity = this.initialCity;
		TaskSet tasks = this.tasks;

		// start with the initial state
		queue.add(initialState);
		visitedStates.add(initialState);
		int nbExploredState = 0;

		while (queue.size() != 0) {
			State state = queue.poll();

			//A* returns first goal states it finds
			if (state.isGoalState()) {
				nbExploredState++;
				System.out.println("nbExploreStateBeforeFindingOptimal = " + String.valueOf(nbExploredState));
				return getPlan(state);
			}
			nbExploredState++;

			for (State nextState : state.nextLegalStates()) {
				if (!visitedStates.contains(nextState)) {
					this.parentState.put(nextState, state);
					visitedStates.add(nextState);
					queue.add(nextState);
				}
			}
			
		}
		// printing statistics

		throw new AssertionError("No goal state found !");

	}

	class StateComparator implements Comparator<State> {
		// Overriding compare()method of Comparator for ascending order of cost function
		// First version of the heuristics based on the accumulatedCost only
		public int compare(State s1, State s2) {
			if (s1.getAccumulatedCost() + s1.getHeuristic() < s2.getAccumulatedCost() + s2.getHeuristic())
				return -1;
			else if (s1.getAccumulatedCost() + s1.getHeuristic() > s2.getAccumulatedCost() + s2.getHeuristic())
				return 1;
			return 0;
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
		// change variables here
		City oldCity = this.initialCity;
		Plan plan = new Plan(initialCity);
		for (int i = actionList.size() - 1; i >= 0; i--) {
			Action action = actionList.get(i);
			for (City city : oldCity.pathTo(citiesList.get(i))) {
				plan.appendMove(city);
			}
			oldCity = citiesList.get(i);
			plan.append(action);
		}
		return plan;
	}

}
