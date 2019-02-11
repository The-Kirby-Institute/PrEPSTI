/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reporter.presenter;

//import java.lang.reflect.Method;
import reporter.* ;

import java.util.ArrayList ;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * For presenting results from the PopulationReporter
 * Methods typically relevant to birth, death, and appropriate ages
 * @author MichaelWalker
 */
public class PopulationPresenter extends Presenter{
    
    private PopulationReporter reporter ;
    
    public static void main(String[] args)
    {
        //String simName = "NoPrepCalibration33Pop40000Cycles3000" ; // Community.NAME_ROOT ; // "introPrepCalibration48Pop40000Cycles7000" ; // args[0] ;
        String simName = "TESTPop40000Cycles400" ;
        String chartTitle = "age_at_death" ; // args[1] ;
        String reportFileName = "output/test/" ; // args[2] ;
        LOGGER.info(chartTitle) ;
        PopulationPresenter populationPresenter = new PopulationPresenter(simName,chartTitle,reportFileName) ;
        
        populationPresenter.plotAgeAtDeath();
        
    }
    
    public PopulationPresenter()
    {
        super() ;   
    }
    
    public PopulationPresenter(String simName, String chartTitle, String fileName)
    {
        super(simName, chartTitle);
        applicationTitle = simName ;
        setReporter(new PopulationReporter(simName,fileName)) ;
    }
    
    public PopulationPresenter(String applicationTitle, String chartTitle, PopulationReporter reporter)
    {
        super(applicationTitle,chartTitle,reporter) ;
        setReporter(reporter) ;
    }
    
    /**
     * Overrides super.setReporter() because reporter is now PopulationReporter
     * @param reporter 
     */
    public void setReporter(PopulationReporter reporter)
    {
        this.reporter = reporter ;
    }

    public void plotPopulationPerCycle()
    {
        LOGGER.info("preparePopulationReport()");
        ArrayList<Object> populationReport = reporter.preparePopulationReport() ;
        
        plotCycleValue("Population",populationReport) ;
    }
    /**
     * Find and plot the number of deaths as a function of time/cycle
     */
    public void plotDeathsPerCycle()
    {
        ArrayList<ArrayList<Object>> deathsPerCycle = reporter.prepareDeathsPerCycleReport() ;
        
        plotEventsPerCycle("Deaths",deathsPerCycle) ;
        
    }

    /**
     * Find and plot the number of deaths as a function of time/cycle
     */
    public void plotBirthsPerCycle()
    {
        ArrayList<ArrayList<Object>> agentBirthReport = reporter.prepareAgentBirthReport() ;
        // ArrayList<String> deathsPerCycle = prepareDeathsPerCycle() ;
        
        plotEventsPerCycle("Births",agentBirthReport) ;
        
    }
    
    /**
     * Plot the number of deaths that occurred for a given age
     */
    public void plotAgeAtDeath()
    {
        HashMap<Object,Number> ageAtDeathReport = reporter.prepareAgeAtDeathReport() ;
        LOGGER.log(Level.INFO,"{0}",ageAtDeathReport) ;
        plotHashMap("Age", "Number of deaths", binHashMap(ageAtDeathReport,"Nb_of_deaths") ) ;
    }


}
