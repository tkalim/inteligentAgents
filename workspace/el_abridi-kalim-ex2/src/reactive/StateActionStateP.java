package reactive;

import logist.topology.Topology.City;
import logist.task.Task;

public class StateActionStateP {

	private State state;
	private State stateP;
	private City action;

	public StateActionStateP(State state, City action, State stateP) {
		this.state = state;
		this.action = action;
	}

	public State getState() {
		return state;
	}
	public State getStateP() {
		return stateP;
	}

	public City getAction() {
		return action;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((stateP == null) ? 0 : stateP.hashCode());
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		return result;
	}
}
