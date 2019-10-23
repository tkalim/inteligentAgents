package centralized;
import java.util.Objects;

import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.behavior.CentralizedBehavior;
import logist.agent.Agent;
import logist.config.Parsers;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class VehiclePlan {
	
	private Vehicle vehicle;
	private Plan plan;
	
	public VehiclePlan(Vehicle vehicle, Plan plan) {
		this.vehicle = vehicle;
		this.plan = plan;
	}
	
	public Vehicle getVehicle() {
		return vehicle;
	}
	public Plan getPlan() {
		return plan;
	}

	public void setPlan(Plan plan) {
		this.plan = plan;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(vehicle.name());
	}

	@Override
	public boolean equals(Object obj) {
		// we do not need to check the plan equality since every vehicle
		// has a unique plan
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VehiclePlan other = (VehiclePlan) obj;
		if (plan == null && other.plan != null) {
			return false;
		}
		if (vehicle == null) {
			if (other.vehicle != null)
				return false;
		} else if (!vehicle.name().equals(other.vehicle.name()))
			return false;
		return true;
	}
	

}
