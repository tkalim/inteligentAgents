package centralized;

import java.io.File;
//the list of imports
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import logist.LogistSettings;

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

public class Centralized implements CentralizedBehavior {

    enum Algorithm {
  		NAIVE, SLS
  	}

    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long timeout_setup;
    private long timeout_plan;

    /* the planning class */
    private Algorithm algorithm;


    @Override
    public void setup(Topology topology, TaskDistribution distribution,
            Agent agent) {

        // this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config" + File.separator + "settings_default.xml");
        }
        catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }

        // the setup method cannot last more than timeout_setup milliseconds
        timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        // the plan method cannot execute more than timeout_plan milliseconds
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);

        this.topology = topology;
        this.distribution = distribution;
        this.agent = agent;

        String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");

        // Throws IllegalArgumentException if algorithm is unknown
        algorithm = Algorithm.valueOf(algorithmName.toUpperCase());
    }

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
      // Compute the centralized plan with the selected algorithm.
      List<Plan> plans = null;
      switch (algorithm) {
        case NAIVE:
          Naive naive = new Naive(vehicles, tasks);
          plans = naive.plan();
          break;
        case SLS:
        	SLS sls = new SLS(vehicles, tasks, timeout_plan);
        	plans = sls.plan();
		default:
			break;
	  }
	  return plans;
    }

}
