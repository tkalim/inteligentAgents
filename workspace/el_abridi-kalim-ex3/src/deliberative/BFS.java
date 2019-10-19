package deliberative;

import logist.task.Task;
import logist.task.TaskSet;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import java.util.LinkedList;
import java.util.Set;
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
	public HashMap<State, Action> parentAction;

	public BFS(Vehicle vehicle, TaskSet tasks) {

		this.vehicle = vehicle;
		this.tasks = tasks;
		this.initialCity = vehicle.getCurrentCity();
		this.currentTasks = vehicle.getCurrentTasks();

		initialState = new State(vehicle, initialCity, tasks, currentTasks);
		this.parentState = new HashMap<State, State>();
		this.parentAction = new HashMap<State, Action>();
	}

	public Plan search() {

		// create the queue and keep track of visited states
		LinkedList<State> queue = new LinkedList<State>();
		Set<State> visitedStates = new HashSet<State>();
		Vehicle vehicle = this.vehicle;
		City initialCity = this.initialCity;
		TaskSet tasks = this.tasks;

		// start with the initial state
		queue.add(initialState);
		visitedStates.add(initialState);

		while (queue.size() != 0) {
			State state = queue.poll();

			if (state.isGoalState()) {
				return getPlan(state);
			}

			for (Task remainingTask : state.getRemainingTasks()) {
				if (remainingTask.weight <= state.getCurrentCapacity()) {
					TaskSet remainingTasks = state.getRemainingTasks().clone();
					remainingTasks.remove(remainingTask);
					TaskSet carryingTasks = state.getCarryingTasks().clone();
					carryingTasks.add(remainingTask);
					State nextState = new State(vehicle, remainingTask.pickupCity, remainingTasks, carryingTasks);
					if (!visitedStates.contains(nextState)) {
						this.parentState.put(nextState, state);
						Pickup pickup = new Pickup(remainingTask);
						this.parentAction.put(nextState, pickup);
						queue.add(nextState);
						visitedStates.add(nextState);
					}
				}
			}

			for (Task carryingTask : state.getCarryingTasks()) {
				if (state.getCarryingTasks().contains(carryingTask)) {
					TaskSet carryingTasks = state.getCarryingTasks().clone();
					carryingTasks.remove(carryingTask);
					TaskSet remainingTasks = state.getRemainingTasks().clone();
					State nextState = new State(vehicle, carryingTask.deliveryCity, remainingTasks, carryingTasks);
					if (!visitedStates.contains(nextState)) {
						this.parentState.put(nextState, state);
						Delivery delivery = new Delivery(carryingTask);
						this.parentAction.put(nextState, delivery);
						queue.add(nextState);
						visitedStates.add(nextState);
					}
				}
			}

		}

		throw new AssertionError("No goal state, weird !!");

	}

	public Plan getPlan(State state) {
		State currentState = state;
		ArrayList<Action> actionList = new ArrayList<Action>();

		// list to help fill in the move() actions
		ArrayList<City> citiesList = new ArrayList<City>();
		while (this.parentState.containsKey(currentState)) {
			Action parentAction = this.parentAction.get(currentState);
			citiesList.add(currentState.getCurrentCity());
			currentState = parentState.get(currentState);
			actionList.add(parentAction);
		}
		// change variables here
		City oldCity = this.initialCity;
		Plan plan = new Plan(initialCity);
		for (int i = actionList.size() - 1;  i >= 0 ; i--) {
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
