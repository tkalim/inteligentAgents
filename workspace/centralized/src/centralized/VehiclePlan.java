package centralized;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Random;

import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.behavior.CentralizedBehavior;
import logist.agent.Agent;
import logist.config.Parsers;
import logist.simulation.Vehicle;
import logist.plan.Action;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class VehiclePlan {

	private Vehicle vehicle;
	private LinkedList<Action> actions;
	private HashMap<Action, Integer> pickUpDeliveryPos;

	public VehiclePlan(Vehicle vehicle, LinkedList<Action>  actions) {
		this.vehicle = vehicle;
		this.actions = new LinkedList<Action>();
		this.pickUpDeliveryPos = new HashMap<Action, Integer>();
	}

	void changingVehicle(VehiclePlan v1, VehiclePlan v2) {
		// transfer first task of v1 to v2
		// difficulty here compared to handout pdp as csp is deciding
		// how to put the task in v2 as a task is pickup + delivery
		// and we don't know when to schedule delivery in the second Vehicle
		// given that there are other tasks load as well contrary to the handout!
		// we will choose a position at random in the action sequence after the pickup
		// position
		// the compilation inheritely comes from the fact that a pickup is not followed directly by its pickUp
		// it may happen that the optimal path is pickUpT1, PickUpT2, DeliverT2, DeliverT1. Therefore, the timing
		// structure is more complex when a vehicle can hold different tasks at the same time.
		Action tPickUp = v1.getFirstAction();
		Action tDelivery = v1.getFirstAction();

		// remove that action from v1 (pickup + delivery)
		v1.removeFirstAction();
		v2.addFirstAction(tPickUp);
		v2.addFirstActionDeliveryAtRand(tDelivery);
	}


	public void addFirstAction(Action tPickUp){
		this.actions.add(0, tPickUp);
	}

	public Plan getPlan(){
		return new Plan(this.vehicle.homeCity(), actions);
	}

	public double getCost(){
		Plan plan = getPlan();
		return plan.totalDistance() * vehicle.costPerKm();
	}

	public void addFirstActionDeliveryAtRand(Action tDelivery){
		// insert the delivery of the pickUp at random position in the future.
		// since we cannot translate the position of delivery from v1 to v2
		// since they had different timelines and different tasks at hand.
		int min = 1;
		int max = this.actions.size();
		// give a random number between [min, max]
		Random rand = new Random();
		int index = rand.nextInt((max - min) + 1) + min;
		this.actions.add(index, tDelivery);
	}

	public Vehicle getVehicle() {
		return vehicle;
	}
	public LinkedList<Action> getActions() {
		return actions;
	}
	public Action getAction(int index) {
		return actions.get(index);
	}
	public Action getFirstAction(){
		// first action is always a pick-up
		return this.getAction(0);
	}
	public void setActions(LinkedList<Action> actions) {
		this.actions = actions;
	}
	public void setAction(int index, Action action) {
		this.actions.set(index, action);
	}
	public void setFirstAction(Action action){
		this.actions.set(0, action);
	}
	public Action getDelivery(Action pickUp){
		return actions.get(pickUpDeliveryPos.get(pickUp));
	}
	public void removeFirstAction(){
		// the remove action is always a pickup
		// in this function we will remove the pickup and its corresponding delivery

		// remove the delivery
		this.actions.remove(pickUpDeliveryPos.get(getFirstAction()));
		// remove the correspondance from the HashMap
		this.pickUpDeliveryPos.remove(getFirstAction());
		// remove the pickup
		this.actions.remove();
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
		if (vehicle == null) {
			if (other.vehicle != null)
				return false;
		} else if (!vehicle.name().equals(other.vehicle.name()))
			return false;
		return true;
	}



}
