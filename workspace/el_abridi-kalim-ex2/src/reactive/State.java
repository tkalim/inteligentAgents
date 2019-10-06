package reactive;

import logist.topology.Topology.City;
import logist.task.Task;

public class State {

	private City current_city;
	private Task available_task;
	private String state_id;

	public State(City current_city, Task available_task) {
		this.current_city = current_city;
		this.available_task = available_task;
		this.state_id = String.valueOf(current_city.id) + available_task.id;
	}

	public City getCurrentCity() {
		return current_city;
	}

	public Task getAvailable_task() {
		return available_task;
	}

	public String getState_id() {
		return state_id;
	}

}
