package centralized;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import logist.plan.Action.Delivery;
import logist.plan.Action.Pickup;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class VehiclePlan {

	public Vehicle vehicle;
	// the actions are reverse to add in "front" in O(1)
	public ArrayList<TaskTypeTuple> nextTask;
	public Plan plan;

	public VehiclePlan(Vehicle vehicle) {
		this.vehicle = vehicle;
		this.nextTask = new ArrayList<TaskTypeTuple>();
		this.plan = null;
	}

	// should be careful and should clone the vehicle before doing the modifications
	// should be careful if v1 does not have any task
	public static Solution changingVehicle(Solution A, int v1Idx, int v2Idx) {
		
		// construct a new solution based on the previous one
		Solution newA = new Solution(A);
		
		// fetch the first task from v1 (pickup + its delivery)
		TaskTypeTuple tPickUp = newA.solution.get(v1Idx).removeFirstPickUpAction();
		TaskTypeTuple tDelivery = newA.solution.get(v1Idx).removeDeliveryOfFirstPickUpAction(tPickUp);

		// adding the first task of v1 to v2
		newA.solution.get(v2Idx).nextTask.add(tDelivery);
		newA.solution.get(v2Idx).nextTask.add(tPickUp);
		
		// check constraint and return newA if all fine
		return newA.solution.get(v2Idx).checkMaxCapacityContraint() == false? null: newA;
	}

	public Solution changingTaskOrder(Solution A, int vIdx, int idx1, int idx2){
		// possible violations:
		// a pickup changed to a future position in which delivery happened before
		// a delivery changed to a past position in which pick is happening after
		// weight
		Solution newA = new Solution(A);
		Collections.swap(newA.solution.get(vIdx).nextTask, idx1, idx2);

		return checkTimelineConstraint() && checkMaxCapacityContraint() ? newA : null;
	}

	public boolean checkMaxCapacityContraint(){
		int accumulated_capacity = 0;
		for(TaskTypeTuple t: nextTask) {
			if(t.type.equals("Delivery")){
				accumulated_capacity = accumulated_capacity - t.task.weight;
			} else if(t.type.equals("PickUp")){
				accumulated_capacity = accumulated_capacity + t.task.weight;
			}

			if(accumulated_capacity > vehicle.capacity()){
				return false;
			}
		}
		return true;
	}
	
	public boolean checkTimelineConstraint() {
		HashSet<Integer> s = new HashSet<Integer>();
		for(TaskTypeTuple t: nextTask) {
			if(t.type.equals("PickUp")){
				s.add(t.task.id);
			} else if(t.type.equals("Delivery") && !s.contains(t.task.id)){
				return false;
			}
		}
		return true;
	}
	

	public TaskTypeTuple removeFirstPickUpAction() {
		return nextTask.remove(0);
	}

	public TaskTypeTuple removeDeliveryOfFirstPickUpAction(TaskTypeTuple tPickUp) {
		TaskTypeTuple tDelivery = tPickUp.getReverseTask();
		nextTask.remove(tPickUp.getReverseTask());
		return tDelivery;
	}

	public Plan getPlan(){
		if(this.plan != null)
			return this.plan;

		City startingCity = this.vehicle.homeCity();
		Plan plan = new Plan(startingCity);
		for(int i = 0; i <= nextTask.size(); i--) {
			City taskCity = null;
			if(nextTask.get(i).type.equals("PickUp")){
				taskCity = nextTask.get(i).task.pickupCity;
				for(City c: startingCity.pathTo(taskCity)){
					plan.appendMove(c);
				}
				plan.appendPickup(nextTask.get(i).task);
				startingCity = nextTask.get(i).task.pickupCity;
			} else if(nextTask.get(i).type.equals("Delivery")){
				taskCity = nextTask.get(i).task.deliveryCity;
				for(City c: startingCity.pathTo(taskCity)){
					plan.appendMove(c);
				}
				plan.appendDelivery(nextTask.get(i).task);
				startingCity = nextTask.get(i).task.deliveryCity;
			}
		}
		return plan;
	}




	public double getCost(){
		Plan plan = getPlan();
		return plan.totalDistance() * vehicle.costPerKm();
	}


}
