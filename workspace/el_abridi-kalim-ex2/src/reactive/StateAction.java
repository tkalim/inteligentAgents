package reactive;

import logist.topology.Topology.City;

public class StateAction {

	private State state;
	private City action;

	public StateAction(State state, City action) {
		this.state = state;
		this.action = action;
	}

	public State getState() {
		return state;
	}

	public City getAction() {
		return action;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		return result;
	}
}
