package deliberative;

import logist.task.Task;
import logist.task.TaskSet;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashMap;
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
		ArrayList<State> visitedStates = new ArrayList<State>();
		Vehicle vehicle = this.vehicle;
		City initialCity = this.initialCity;
		TaskSet tasks = this.tasks;

		// start with the initial state
		queue.add(initialState);
		int id = 0;

		while (queue.size() != 0) {
			State state = queue.poll();
			visitedStates.add(state);

			if (state.isGoalState()) {
				System.out.println("found goal state");
				return getPlan(state);
			}

			TaskSet unionOfTasks = TaskSet.union(state.getRemainingTasks(), state.getCarryingTasks());
			for (Task task : unionOfTasks) {
				if (state.getRemainingTasks().contains(task) && task.weight <= state.getCurrentCapacity()) {
					System.out.println(id + " is being visitied as remaining task");
					TaskSet remainingTasks = state.getRemainingTasks().clone();
					remainingTasks.remove(task);
					TaskSet carryingTasks = state.getCarryingTasks().clone();
					carryingTasks.add(task);
					id++;
					State nextState = new State(vehicle, task.pickupCity, remainingTasks, carryingTasks, id);
					if (!isStateVisited(nextState, visitedStates)) {
						this.parentState.put(id, state);
						Pickup pickup = new Pickup(task);
						this.parentAction.put(id, pickup);
						queue.add(nextState);
						System.out.println(id + " has been added to queue");
					}
				} else if (state.getCarryingTasks().contains(task)) {
					System.out.println(id + " is being visited as carrying task");
					TaskSet carryingTasks = state.getCarryingTasks().clone();
					carryingTasks.remove(task);
					TaskSet remainingTasks = state.getRemainingTasks().clone();
					remainingTasks.add(task);
					id++;
					State nextState = new State(vehicle, task.deliveryCity, remainingTasks, carryingTasks, id);
					if (!isStateVisited(nextState, visitedStates)) {
						this.parentState.put(id, state);
						Delivery delivery = new Delivery(task);
						this.parentAction.put(id, delivery);
						queue.add(nextState);
						System.out.println(id + "has been added to queue");
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
			citiesList.add(currentState.getCurrentCity());
			currentState = parentState.get(currentState.id);
			actionList.add(0, parentAction);
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

	public Boolean isStateVisited(State state, ArrayList<State> visitedStates) {
		for (State visitedState : visitedStates) {
			if ((visitedState.getCurrentCity().equals(state.getCurrentCity()))
					&& (visitedState.getRemainingTasks().equals(state.getRemainingTasks()))
					&& (visitedState.getCarryingTasks().equals(state.getCarryingTasks()))) {
				return true;
			}
		}
		return false;
	}
}
