package reactive;

import logist.topology.Topology.City;
import logist.task.Task;

public class State {

	private City city;
	private Task task;

	public State(City city, Task task) {
		this.city = city;
		this.task = task;
	}

	public City getCity() {
		return city;
	}

	public Task getTask() {
		return task;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result + ((task == null) ? 0 : task.id);
		return result;
	}

	public String toString() {
		return "hashCode: " + hashCode() + " currentCity:"
					 + getCity() + " task: " + getTask();
	}

}
