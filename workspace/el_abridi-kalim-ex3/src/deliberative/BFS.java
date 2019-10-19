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
	public HashMap<Integer, State> parentState;
	public HashMap<Integer, Action> parentAction;

	public BFS(Vehicle vehicle, TaskSet tasks) {

		this.vehicle = vehicle;
		this.tasks = tasks;
		this.initialCity = vehicle.getCurrentCity();
		this.currentTasks = vehicle.getCurrentTasks();

		initialState = new State(vehicle, initialCity, tasks, currentTasks, 0);
		this.parentState = new HashMap<Integer, State>();
		this.parentAction = new HashMap<Integer, Action>();
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
		int id = 0;
		visitedStates.add(initialState);

		while (queue.size() != 0) {
			State state = queue.poll();
			//visitedStates.add(state);


			if (state.isGoalState()) {
				return getPlan(state);
			}

			TaskSet unionOfTasks = TaskSet.union(state.getRemainingTasks(), state.getCarryingTasks());
			for (Task task : unionOfTasks) {
				if (state.getRemainingTasks().contains(task) && task.weight <= state.getCurrentCapacity()) {
					TaskSet remainingTasks = state.getRemainingTasks().clone();
					remainingTasks.remove(task);
					TaskSet carryingTasks = state.getCarryingTasks().clone();
					carryingTasks.add(task);
					id++;
					State nextState = new State(vehicle, task.pickupCity, remainingTasks, carryingTasks, id);
					if (!visitedStates.contains(nextState)) {
						this.parentState.put(id, state);
						Pickup pickup = new Pickup(task);
						this.parentAction.put(id, pickup);
						queue.add(nextState);
						visitedStates.add(nextState);
					}
				} else if (state.getCarryingTasks().contains(task)) {
					TaskSet carryingTasks = state.getCarryingTasks().clone();
					carryingTasks.remove(task);
					TaskSet remainingTasks = state.getRemainingTasks().clone();
					id++;
					State nextState = new State(vehicle, task.deliveryCity, remainingTasks, carryingTasks, id);
					if (!visitedStates.contains(nextState)) {
						this.parentState.put(id, state);
						Delivery delivery = new Delivery(task);
						this.parentAction.put(id, delivery);
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
		while (this.parentState.containsKey(currentState.id)) {
			Action parentAction = this.parentAction.get(currentState.id);
			citiesList.add(0, currentState.getCurrentCity());
			currentState = parentState.get(currentState.id);
			actionList.add(0, parentAction);
		}
		// change variables here
		City oldCity = this.initialCity;
		Plan plan = new Plan(initialCity);
		for (int i = 0;  i <= actionList.size() - 1 ; i++) {
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
