package deliberative;

import logist.task.Task;
import logist.topology.Topology.City;
import logist.task.TaskSet;
import logist.plan.Action;
import logist.plan.Action.Delivery;
import logist.plan.Action.Pickup;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;

import logist.simulation.Vehicle;

public class State {

	private City currentCity;
	private TaskSet carryingTasks;
	private TaskSet remainingTasks;
	private Vehicle vehicle;
	private int currentCapacity;
	private Action action;

	public State(Vehicle vehicle, City currentCity, TaskSet remainingTasks, TaskSet carryingTasks, Action action) {
		super();
		this.currentCity = currentCity;
		this.remainingTasks = remainingTasks;
		this.carryingTasks = carryingTasks;
		this.vehicle = vehicle;
		this.currentCapacity = getCurrentCapacity();
		this.action = action;
	}

	public City getCurrentCity() {
		return currentCity;
	}

	public TaskSet getCarryingTasks() {
		return carryingTasks;
	}

	public TaskSet getRemainingTasks() {
		return remainingTasks;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public Action getAction() {
		return action;
	}

	public Boolean isGoalState() {
		return(this.carryingTasks.isEmpty() && this.remainingTasks.isEmpty());
		}

	public int getCurrentCapacity() {
		int payloadWeight = 0;
		for (Task task : carryingTasks) {
			payloadWeight += task.weight;
		}
		return vehicle.capacity() - payloadWeight;
	}

	public LinkedList<State> nextLegalStates(){
		LinkedList<State> nextlegalstates = new LinkedList<State>();

		// adding states made of remaining task
		for (Task remainingTask : getRemainingTasks()) {
			if (remainingTask.weight <= getCurrentCapacity()) {
				TaskSet remainingTasks = getRemainingTasks().clone();
				remainingTasks.remove(remainingTask);
				TaskSet carryingTasks = getCarryingTasks().clone();
				carryingTasks.add(remainingTask);
				Pickup pickup = new Pickup(remainingTask);
				State nextState = new State(vehicle, remainingTask.pickupCity, remainingTasks, carryingTasks, pickup);
				nextlegalstates.add(nextState);
			}
		}

		// adding states made of carrying task
		for (Task carryingTask : getCarryingTasks()) {
				TaskSet carryingTasks = getCarryingTasks().clone();
				carryingTasks.remove(carryingTask);
				TaskSet remainingTasks = getRemainingTasks().clone();
				Delivery delivery = new Delivery(carryingTask);
				State nextState = new State(vehicle, carryingTask.deliveryCity, remainingTasks, carryingTasks, delivery);
				nextlegalstates.add(nextState);
		}
		return nextlegalstates;
	}

	@Override
	public int hashCode() {
		return Objects.hash(carryingTasks, currentCapacity, currentCity, remainingTasks);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		return Objects.equals(carryingTasks, other.carryingTasks) && currentCapacity == other.currentCapacity
				&& Objects.equals(currentCity, other.currentCity)
				&& Objects.equals(remainingTasks, other.remainingTasks);
	}



}
