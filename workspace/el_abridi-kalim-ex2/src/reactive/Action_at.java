package reactive;

import logist.topology.Topology.City;

public class Action_at {

	private boolean accept_task;
	private City next_city;
	private int action_id;

	public Action_at(boolean accept_task, City next_city) {
		this.accept_task = accept_task;
		this.next_city = next_city;
		this.action_id = 0;

	}

	public Action_at() {
		this.accept_task = null;
		this.next_city = null;
		this.action_id = null;
	}

	public boolean isAccept_task() {
		return accept_task;
	}

	public City getNext_city() {
		return next_city;
	}

	public int getAction_id() {
		return action_id;
	}

}
