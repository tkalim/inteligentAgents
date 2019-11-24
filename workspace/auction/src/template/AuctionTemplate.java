package template;

import java.io.File;
//the list of imports
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import logist.LogistSettings;
import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.config.Parsers;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 * 
 */
@SuppressWarnings("unused")
public class AuctionTemplate implements AuctionBehavior {

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private Random random;
	private Vehicle vehicle;
	private City currentCity;
    private long timeout_plan;
    private long timeout_bid;
    private long timeout_setup;
    private double minOpponentBid;
    private double margin;
    private double upperMargin;
    private double lowerMargin;
    private double opponentMargin;
    private double opponentUpperMargin;
    private double opponentLowerMargin;
    private int round = 0;
    private int initialDiscountRounds = 5;
    private double initialDiscount = 0.5;
    
    private SLS sls;
    private SLS potentialNewSls;
    private SLS opponentSls;
    private SLS opponentPotentialNewSls;
    

	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {
		
		  // this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config" + File.separator + "settings_auction.xml");
        }
        catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
		this.vehicle = agent.vehicles().get(0);
		this.currentCity = vehicle.homeCity();

		long seed = -9019554669489983951L * currentCity.hashCode() * agent.id();
		this.random = new Random(seed);
		// the plan method cannot execute more than timeout_plan milliseconds
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);
        timeout_bid = ls.get(LogistSettings.TimeoutKey.BID);
        timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        minOpponentBid = Double.MAX_VALUE;
        
        // setting the PDP 
        // TODO: double check this new TaskSet is not creating some funky problems
        // agent.getTasks() is expected to be empty
        assert agent.getTasks().size() == 0;
        sls = new SLS(agent.vehicles(), timeout_bid/2);
        // opponent will have a different set of vehicles with their homecity and capacity
        // let's assume for now they are the same
        opponentSls = new SLS(agent.vehicles(), timeout_bid/2);
        
        // hyperparam
        upperMargin = 0.8;
        lowerMargin = 0.7;
        margin = (upperMargin + lowerMargin) / 2;
        
        opponentUpperMargin = 0.9;
        opponentLowerMargin = 0.8;
        opponentMargin = (opponentUpperMargin + opponentLowerMargin) / 2;
        
        
        
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {	
		double bid = bids[agent.id()];
		double opponentBid = bids[(agent.id() + 1) % 2];
		minOpponentBid = Math.min(minOpponentBid, opponentBid);
		
		if (winner == agent.id()) {
			System.out.println("I won the previous bid");
			System.out.println("my bid " + bid);
			System.out.println("opponent bid " + opponentBid);
			
			//currentCity = previous.deliveryCity;
			// potentialNewSls is with the new task included
			sls = potentialNewSls;
			// adding more to our margin while not exceeding the max
			margin = Math.min(upperMargin, margin + 0.01);
			// we assume that the opponent is lowering its margin to match us in the next bid
			opponentMargin = Math.max(opponentLowerMargin, opponentMargin - 0.01);
		}
		else {
			System.out.println("OPPONENT won the previous bid");
			System.out.println("my bid " + bid);
			System.out.println("opponent bid " + opponentBid);
			// potentialNewSls is with the new task included
			opponentSls = opponentPotentialNewSls;
			// decreansing our margin to become more competitive while not going below the lowerbound
			margin = Math.max(lowerMargin, margin - 0.01);
			// we assume that the opponent trying to higher its margin to gain more money in the next bid
			// while not exceeding its uppermargin
			opponentMargin = Math.min(opponentUpperMargin, opponentMargin + 0.01);
		}
		System.out.println("------------------------");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");

	}
	
	@Override
	public Long askPrice(Task task) {
		System.out.println("ROUND " + round);
		int largestVehicleIdx = SLS.largestVehicleIndex(agent.vehicles());
		if (agent.vehicles().get(largestVehicleIdx).capacity() < task.weight)
			return null;

//		long distanceTask = task.pickupCity.distanceUnitsTo(task.deliveryCity);
//		long distanceSum = distanceTask
//				+ currentCity.distanceUnitsTo(task.pickupCity);
//		double marginalCost = Measures.unitsToKM(distanceSum
//				* vehicle.costPerKm());
//
//		double ratio = 1.0 + (random.nextDouble() * 0.05 * task.id);
//		double bid = ratio * marginalCost;
//		System.out.println("Agent " + agent.id() + " bidding price of " + task.id + " is " + (long) Math.round(bid));
//		return (long) Math.round(bid);
		
		// initializing two new SLS planning with the additional task
		potentialNewSls = new SLS(sls, task, timeout_bid/2);
		potentialNewSls.plan();
		opponentPotentialNewSls = new SLS(opponentSls, task, timeout_bid/2);
		opponentPotentialNewSls.plan();
		
		assert(potentialNewSls.bestSolution.getNumberOfTasks() == sls.bestSolution.getNumberOfTasks() + 1);
		assert(opponentPotentialNewSls.bestSolution.getNumberOfTasks() == opponentSls.bestSolution.getNumberOfTasks() + 1);
		
		double marginalCost;
		double opponentMarginalCost;
		if(round != 0) {
			marginalCost = potentialNewSls.bestSolution.getCost() - sls.bestSolution.getCost();
			opponentMarginalCost = opponentPotentialNewSls.bestSolution.getCost() - opponentSls.bestSolution.getCost();
		}
		else {
			marginalCost = potentialNewSls.bestSolution.getCost();
			opponentMarginalCost = opponentPotentialNewSls.bestSolution.getCost();
		}
		System.out.println("Task = " + task);
		System.out.println("Task reward " + task.reward);
		System.out.println("marginCost = " + marginalCost);
		System.out.println("opponentMarginalCost = " + opponentMarginalCost);
		System.out.println("minOpponentBid = " + minOpponentBid);
		double distanceTask = task.pickupCity.distanceTo(task.deliveryCity);
		double distanceSum = distanceTask
				+ currentCity.distanceTo(task.pickupCity);
		double dummycost = distanceSum * vehicle.costPerKm();
		System.out.println("dummy distance =" + distanceSum);
		System.out.println("dummycost =" + dummycost);

		
		
		// rule-based bidding
		double bid = Math.min(marginalCost * margin, opponentMarginalCost * opponentMargin);
		
		if(round > 0 && bid < minOpponentBid) {
			bid = Math.max(0, minOpponentBid - 1);
		}
		
		if(task.reward >= dummycost) {
			bid = 0;
		}
		
		// initial aggresive discounting for the first tasks
		// initialDiscountRounds might be different depending on the # of takss offered (look at the TD)
		if(round < initialDiscountRounds) {
			bid = bid * initialDiscount;
		}
		
		round++;
		return (long) Math.floor(bid);
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		System.out.println("I am now doing the final planning");
		// TODO: fix the timeout to be the one of the plan instead of bid
    	SLS sls = new SLS(vehicles, tasks, timeout_plan);
    	return sls.plan();
	}

}
