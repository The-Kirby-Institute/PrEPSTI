/**
 * 
 */
package PRSP.PrEPSTI.community;

import PRSP.PrEPSTI.agent.* ;


/**
 * @author MichaelWalker
 *
 */
public class Monogomous extends Relationship {
    
    /** The number of current Monogomous Relationships. */
    static int NB_MONOGOMOUS = 0 ;
    
    /** Probability of breakup() in a given cycle. */
    public static double BREAKUP_PROBABILITY = 0.0001 ;
    

    // Part of how the number of sexual encounters is determined.
    static double ENCOUNTER_PROBABILITY = 0.2 ; // 0.6 ;


    // Logger
    java.util.logging.Logger logger = java.util.logging.Logger.getLogger("longTerm") ;

    /**
     * 
     */
    public Monogomous() {
        super() ;
        NB_MONOGOMOUS++ ;
    }

    /**
     * 
     */
    public Monogomous(Agent agent1, Agent agent2) {
        super(agent1,agent2) ;
        NB_MONOGOMOUS++ ;
    }

    /**
     * The probability of any sexual contact in any cycle.
     * @return 
     */
    @Override
    protected double getEncounterProbability()
    {
        return ENCOUNTER_PROBABILITY ;
    }
    
    /**
     * TODO: Finalise the form of how this is handled  
     * @return the int number of sexual contacts for a given encounter
     */
    protected int chooseNbContacts()
    {
    	return RAND.nextInt(3) + 1 ;
    }
    
    /**
     * Since breakupProbability is static, this getter allows it to be called
     * from the Relationship base class
     * @return (double) the probability of a relationship ending in a given cycle
     */
    @Override
    public double getBreakupProbability()
    {
        return BREAKUP_PROBABILITY ;
    }
    
    /**
     * Adds Agents to Relationship and establishes which has the lower AgentId
     * Arrange that agent0 should always have the lower agentId. Override to adjust
     * the value of inMonogomous.
     * 
     * @param agent0
     * @param agent1
     * @return (String) report
     */
    @Override
    public String addAgents(Agent agent0, Agent agent1)
    {
    	agent0.setInMonogomous(true) ;
    	agent1.setInMonogomous(true) ;
    	
    	return super.addAgents(agent0, agent1) ;
    }

    /**
     * 
     * @return (int) The current number of Monogomous Relationships.
     */
    public int getNbMonogomous()
    {
        return NB_MONOGOMOUS ;
    }
    
    
    protected void diminishNbMonogomous()
    {
        NB_MONOGOMOUS-- ;
    }
    
    
}
