package template;

import java.util.ArrayList;
import java.util.List;

import logist.plan.Plan;
import logist.simulation.Vehicle;

public class Solution {
	public ArrayList<VehiclePlan> solution;

	public Solution(List<Vehicle> vehicles){
		solution = new ArrayList<VehiclePlan>();
		for(Vehicle v: vehicles){
			solution.add(new VehiclePlan(v));
		}
	}
	
	// copy constructor 
	Solution(Solution s) { 
		solution = new ArrayList<VehiclePlan>();
		// deep-copy
		for(VehiclePlan vp : s.solution) {
			solution.add(new VehiclePlan(vp));
		}
    }
	
	public double getCost() {
		double accumulatedCost = 0.0;
		for(VehiclePlan p: solution) {
			accumulatedCost += p.getCost();
		}
		return accumulatedCost;
	}
	
	public List<Plan> getPlans(){
		ArrayList<Plan> plans = new ArrayList<Plan>();
		for(VehiclePlan vp: solution) {
			plans.add(vp.getPlan());
		}
		return plans;
	}
	
	public int getNumberOfTasks() {
		int count = 0;
		for(VehiclePlan vp: solution) {
			count += vp.nextTask.size() / 2;
		}
		return count;
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Total Cost: " + Double.toString(this.getCost()) + "\n");
        str.append("Plans\n");
        for(VehiclePlan vp: this.solution) {
        	Plan plan = vp.getPlan();
        	str.append("Vehicle " + vp.vehicle.id() + " " + ", # of tasks: " + this.solution.size()/2 + ", distance " + 
        				plan.totalDistance() + ", costPerkm " + vp.vehicle.costPerKm() + ", Capacity" + vp.vehicle.capacity() + "\n");
        	str.append(plan + "\n");
        }
        return str.toString();
	}

}
