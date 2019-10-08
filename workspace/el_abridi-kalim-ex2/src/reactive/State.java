package reactive;

import logist.topology.Topology.City;
import logist.task.Task;

public class State {

	public City city;
	public City task;

	public State(City city, City task) {
		this.city = city;
		this.task = task;
	}

	public City getCity() {
		return city;
	}

	public City getTask() {
		return task;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result + ((task == null) ? 0 : task.hashCode());
		return result;
	}

	public String toString() {
		return "hashCode: " + hashCode() + " currentCity:"
					 + getCity() + " task: " + getTask();
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
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		if (task == null) {
			if (other.task != null)
				return false;
		} else if (!task.equals(other.task))
			return false;
		return true;
	}
}
