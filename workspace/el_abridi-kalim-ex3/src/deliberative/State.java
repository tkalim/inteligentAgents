package deliberative;

import logist.task.Task;
import logist.topology.Topology.City;
import logist.task.TaskSet;
import logist.plan.Action;
import logist.plan.Action.Delivery;
import logist.plan.Action.Pickup;
import java.util.ArrayList;
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
	
	public ArrayList getPossibleActions() {
		ArrayList<Action> possibleActions = new ArrayList<Action>();
		
		for (Task carriedTask : this.carryingTasks) {
			Delivery possibleAction = new Delivery(carriedTask); 
			possibleActions.add(possibleAction);
		}
		
		for (Task remainingTask : this.remainingTasks) {
			Pickup possibleAction = new Pickup(remainingTask);
			if (remainingTask.weight <= this.currentCapacity) {
				possibleActions.add(possibleAction);
			}
		}
		
		return possibleActions;
	}
	
	public Boolean isGoalState() {
		return(this.carryingTasks.isEmpty() && this.remainingTasks.isEmpty());
		}
	
	public int getCurrentCapacity() {
		int paylodWeight = 0;
		for (Task task : carryingTasks) {
			paylodWeight += task.weight;
		}
		return vehicle.capacity() - paylodWeight;
	}
	
}
