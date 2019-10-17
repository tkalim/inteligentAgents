package deliberative;

import java.util.List;
import logist.task.Task;
import logist.topology.Topology.City;


public class State {
	
	private City currentCity;
	private List<Task> carryingTasks;
	private List<Task> remainingTasks;
	
	//maybe some id ?
	
	public State(City currentCity, List<Task> remainingTasks, List<Task> carryingTasks) {
		super();
		this.currentCity = currentCity;
		this.remainingTasks = remainingTasks;
		this.carryingTasks = carryingTasks;
	}

	public City getCurrentCity() {
		return currentCity;
	}

	public List<Task> getCarryingTasks() {
		return carryingTasks;
	}

	public List<Task> getRemainingTasks() {
		return remainingTasks;
	}
	
	
}
