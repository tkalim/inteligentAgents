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
			//System.out.println("winner of the bid is " + agent.id());
			//currentCity = previous.deliveryCity;
			// potentialNewSls is with the new task included
			sls = potentialNewSls;
			// adding more to our margin while not exceeding the max
			margin = Math.min(upperMargin, margin + 0.01);
			// we assume that the opponent is lowering its margin to match us in the next bid
			opponentMargin = Math.max(opponentLowerMargin, opponentMargin - 0.01);
		}
		else {
			// potentialNewSls is with the new task included
			opponentSls = opponentPotentialNewSls;
			// decreansing our margin to become more competitive while not going below the lowerbound
			margin = Math.max(lowerMargin, margin - 0.01);
			// we assume that the opponent trying to higher its margin to gain more money in the next bid
			// while not exceeding its uppermargin
			opponentMargin = Math.min(opponentUpperMargin, opponentMargin + 0.01);
		}
		
		
	}
	
	@Override
	public Long askPrice(Task task) {

		if (vehicle.capacity() < task.weight)
			return null;

		long distanceTask = task.pickupCity.distanceUnitsTo(task.deliveryCity);
		long distanceSum = distanceTask
				+ currentCity.distanceUnitsTo(task.pickupCity);
		double marginalCost = Measures.unitsToKM(distanceSum
				* vehicle.costPerKm());

		double ratio = 1.0 + (random.nextDouble() * 0.05 * task.id);
		double bid = ratio * marginalCost;
		System.out.println("Agent " + agent.id() + " bidding price of " + task.id + " is " + (long) Math.round(bid));
		return (long) Math.round(bid);
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
    	SLS sls = new SLS(vehicles, tasks, timeout_plan);
    	List<Plan> plans = sls.plan();
		return plans;
	}

}
