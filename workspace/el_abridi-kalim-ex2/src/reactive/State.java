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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((current_city == null) ? 0 : current_city.hashCode());
		result = prime * result + ((available_task == null) ? 0 : available_task.id);
		return result;
	}

}
