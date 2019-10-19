package deliberative;

import logist.task.Task;
import logist.topology.Topology.City;
import logist.task.TaskSet;
import logist.plan.Action;
import logist.plan.Action.Delivery;
import logist.plan.Action.Pickup;
import java.util.ArrayList;
import java.util.Objects;

import logist.simulation.Vehicle;

public class State {
	
	private City currentCity;
	private TaskSet carryingTasks;
	private TaskSet remainingTasks;
	private Vehicle vehicle;
	private int currentCapacity;
	public int id;
	
	//maybe some id ?
	
	public State(Vehicle vehicle, City currentCity, TaskSet remainingTasks, TaskSet carryingTasks, int id) {
		super();
		this.currentCity = currentCity;
		this.remainingTasks = remainingTasks;
		this.carryingTasks = carryingTasks;
		this.vehicle = vehicle;
		this.currentCapacity = getCurrentCapacity();
		this.id = id;
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
