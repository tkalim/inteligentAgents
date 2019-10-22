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
import java.util.Collections;

import logist.simulation.Vehicle;

public class State {

	private City currentCity;
	private TaskSet carryingTasks;
	private TaskSet remainingTasks;
	private Vehicle vehicle;
	private int currentCapacity;
	private Action action;
	private double accumulatedCost;
	private double heuristic;

	public State(Vehicle vehicle, City currentCity, TaskSet remainingTasks, TaskSet carryingTasks, Action action, double accumulatedCost) {
		super();
		this.currentCity = currentCity;
		this.remainingTasks = remainingTasks;
		this.carryingTasks = carryingTasks;
		this.vehicle = vehicle;
		this.currentCapacity = vehicle.capacity() - carryingTasks.weightSum();
		this.action = action;
		this.accumulatedCost = accumulatedCost;
		this.heuristic = this.computeHeuristic();
	}

	public City getCurrentCity() {
		return currentCity;
	}

	public double getHeuristic() {
		return heuristic;
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
		return currentCapacity;
	}

	public double getAccumulatedCost() {
		return accumulatedCost;
	}

	public LinkedList<State> nextLegalStates(){
		LinkedList<State> nextlegalstates = new LinkedList<State>();
		int costPerKm = vehicle.costPerKm();

		// adding states made of remaining task
		for (Task remainingTask : getRemainingTasks()) {
			if (remainingTask.weight <= getCurrentCapacity()) {
				TaskSet remainingTasks = getRemainingTasks().clone();
				remainingTasks.remove(remainingTask);
				TaskSet carryingTasks = getCarryingTasks().clone();
				carryingTasks.add(remainingTask);
				Pickup pickup = new Pickup(remainingTask);
				double accumulatedCost = this.getAccumulatedCost() + costPerKm * getCurrentCity().distanceTo(remainingTask.pickupCity);
				State nextState = new State(vehicle, remainingTask.pickupCity, remainingTasks, carryingTasks, pickup, accumulatedCost);
				nextlegalstates.add(nextState);
			}
		}

		// adding states made of carrying task
		for (Task carryingTask : getCarryingTasks()) {
				TaskSet carryingTasks = getCarryingTasks().clone();
				carryingTasks.remove(carryingTask);
				TaskSet remainingTasks = getRemainingTasks().clone();
				Delivery delivery = new Delivery(carryingTask);
				double accumulatedCost = this.getAccumulatedCost() + costPerKm * getCurrentCity().distanceTo(carryingTask.deliveryCity);
				State nextState = new State(vehicle, carryingTask.deliveryCity, remainingTasks, carryingTasks, delivery, accumulatedCost);
				nextlegalstates.add(nextState);
		}

		// shuffle the results to prevent a similar implementation to the naive (1st pickup 2nd delivery)
		Collections.shuffle(nextlegalstates);

		return nextlegalstates;
	}

	@Override
	public int hashCode() {
		return Objects.hash(carryingTasks, currentCapacity, currentCity, remainingTasks, accumulatedCost);
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
				&& Objects.equals(remainingTasks, other.remainingTasks)
				// "==" gives best result in A*, but is it correct ?
				&& this.accumulatedCost + this.getHeuristic() >= other.accumulatedCost + other.getHeuristic();
	}
	
//	
//	private double computeHeuristic() {
//		double heuristic = 0.0;
//		for (Task task: this.carryingTasks) {
//			heuristic += this.vehicle.costPerKm() * this.getCurrentCity().distanceTo(task.deliveryCity);
//		}
//		for (Task task: this.remainingTasks) {
//			heuristic += this.vehicle.costPerKm() * this.getCurrentCity().distanceTo(task.pickupCity);
//			heuristic += this.vehicle.costPerKm() * this.getCurrentCity().distanceTo(task.deliveryCity);
//		}
//		return heuristic;
//	}
	

	private double computeHeuristic() {
		Double heuristic = 0.0;
		City currentCity = this.getCurrentCity();
		for (Task task : this.carryingTasks) {
			heuristic += vehicle.costPerKm() * currentCity.distanceTo(task.deliveryCity);
		}
		for (Task task : this.remainingTasks) {
			heuristic += vehicle.costPerKm() * currentCity.distanceTo(task.deliveryCity);
		}
		
		return heuristic;
	}

	@Override
	public String toString() {
		return "State [currentCity=" + currentCity + ", carryingTasks=" + carryingTasks + ", remainingTasks="
				+ remainingTasks + ", vehicle=" + vehicle + ", currentCapacity=" + currentCapacity + ", action="
				+ action + ", accumulatedCost=" + accumulatedCost + ", heuristic=" + heuristic + "]";
	}


}
