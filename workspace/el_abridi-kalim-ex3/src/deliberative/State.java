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
				/*
					heuristic for task to be picked-up: since a pick-up task will require to keep the weight
					in the vehicle for (DestinationCity - pickUpCity) distance, we need to prioritize:
						1 - pick-up task with shorter distance to deliver.
						2 - minimum weight to be transported since the pick-up task will prevent from picking up and delivering other task over that distance.
						 => combine the two with the following heuristic: cost = (delivery - pickup) distant * weightOfTask
					effects:
						- Tasks with shorter delivery distances and lighter weight will be prioritized.
						- Tasks with heavy weight/larger distance to delivery will have less priorited in order to deliver maximum of tasks over shorter distance
				*/
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
				/*
					heuristic for task to be delivered: since a delivery task already carried prevent as from collecting other tasks from pick-up along the way
					we need to prioritize tasks to deliver ASAP that have heavy weight, and shorter distance to deliveryCity (deliveryCity - currentCity),
					we then need to prioritize:
						1 - delivering tasks with shorter distance to destination from currentCity.
						2 - delivering tasks that have maximum weight first to free capacity in the vehicle for more tasks to be picked-up
						 => combine the two with the following heuristic: cost = -((delivery - current) distant * weightOfTask)
					effects:
						- Tasks with shorter delivery distances from currentCity (TODO: not really formula should tweaked, inverse maybe?) and larger weight will be prioritized.
						- Tasks with lighter weight/larger distance to delivery will have less priorited in order to free maximum capacity over shorter distance
				*/
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
				&& this.accumulatedCost == other.accumulatedCost;
	}
	
	
	private double computeHeuristic() {
		double heuristic = 0.0;
		for (Task task: this.carryingTasks) {
			heuristic += this.vehicle.costPerKm() * this.getCurrentCity().distanceTo(task.deliveryCity);
		}
		for (Task task: this.remainingTasks) {
			heuristic += this.vehicle.costPerKm() * this.getCurrentCity().distanceTo(task.pickupCity);
			heuristic += this.vehicle.costPerKm() * task.pickupCity.distanceTo(task.deliveryCity);
		}
		return heuristic;
	}



}
