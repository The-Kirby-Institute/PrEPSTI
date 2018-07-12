/**
 * 
 */
package site;

import com.sun.media.jfxmedia.logging.Logger;

/**
 * @author MichaelWalker
 *
 */
public class Urethra extends Site {

    // Constants that vary according to Site subclass
    // Probability of initial gonorrhoea infection 
    // static double INITIAL = 0.3 ;

    // Probability of positive symptomatic status if infected
    static double SYMPTOMATIC_PROBABILITY = 0.8 ;
    
    /** Probability of Site being infected initially */
    static double INITIAL = 0.03 ;
    
    /**
     * Duration of gonorrhoea infection in Urethra.
     * Proper value unknown as almost always treated. 
     * Value for Rectum taken.
     */
    static int INFECTION_DURATION = 183 ;
    
    static int SYMPTOMATIC_DURATION = 3 ;
    
    /**
     * Probability of seeking treatment in a given cycle if infected with 
     * gonorrhoea. This value chosen so 91% probability of treatment within 
     * 3 days.
     */
    static double TREATMENT_PROBABILITY = 0.6 ; // 0.6 ;
    
    /**
     * 
     */
    public Urethra() 
    {
        super() ;
        Logger.logMsg(0, this.getSite());
    }
    
    /**
     * Determine whether Urethra initially infected.
     * @return false
     */
//    @Override
//    public boolean initialiseInfection()
//    {
//        return false ;
//    }
    
    /**
     * 
     * @return Probability of site being infected initially.
     */
    protected double getInfectedProbability()
    {
        return INITIAL ;
    }

    /**
     * 
     * @return The probability of an infection at this Site causing symptoms
     */        
    protected double getSymptomaticProbability()
    {
        return SYMPTOMATIC_PROBABILITY ;
    }

    
    protected int getInfectionDuration()
    {
        return INFECTION_DURATION ;
    }
    
    /**
     * 
     * @return Mean duration of symptomatic infection (before treatment).
     */
    @Override
    protected int getSymptomaticDuration()
    {
        return SYMPTOMATIC_DURATION ;
    }
    
    /**
     * 
     * @return Probability of treatment for an STI being sought and successful.
     */
    @Override
    protected double getTreatmentProbability()
    {
        return TREATMENT_PROBABILITY ;
    }

}