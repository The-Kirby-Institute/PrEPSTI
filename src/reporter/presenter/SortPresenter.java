/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reporter.presenter;

import java.lang.reflect.* ;
import java.util.ArrayList ;
import java.util.Arrays;
import java.util.HashMap ;
import java.util.logging.Level;

import reporter.Reporter ;
import reporter.SortReporter ;
import reporter.EncounterReporter ;
import static reporter.presenter.Presenter.LOGGER;


/**
 *
 * @author MichaelWalker
 */
public class SortPresenter extends Presenter {
    
    private SortReporter reporter ;
    
    public static void main(String[] args) // "test1","encounter","population","fileName","test1","plotReceiveSortPrepStatusReport","false"
    {
        LOGGER.info(args[0]);
        String simName = args[0] ;
        LOGGER.info(args[1]);
        String unsortedName = "reporter." + args[1] + "Reporter" ;
        LOGGER.info(args[2]);
        String sortingName = "reporter." + args[2] + "Reporter" ;
        LOGGER.info(args[3]);
        String reportFileName = args[3] ;
        LOGGER.info(args[4]);
        String chartTitle = args[4] ;
        String methodName = args[5] ;
        
        try
        {
        Class unsortedClazz = Class.forName(unsortedName) ; // .asSubclass(Reporter.class) ;
        Class sortingClazz = Class.forName(sortingName) ;
        Class[] argClazzArray = new Class[] {String.class,String.class} ;
        LOGGER.info("Construct Reporters");
        Reporter unsortedReporter = (Reporter) unsortedClazz.getDeclaredConstructor(argClazzArray).newInstance(simName,reportFileName) ;
        Reporter sortingReporter =  (Reporter) sortingClazz.getDeclaredConstructor(argClazzArray).newInstance(simName,reportFileName) ;
        LOGGER.info("sorters");
        SortReporter sortReporter = new SortReporter(simName,unsortedReporter,sortingReporter) ;
        SortPresenter sortPresenter = new SortPresenter(simName,chartTitle,sortReporter) ;
        LOGGER.info("declare method");
        Method method = sortPresenter.getClass().getMethod(methodName,String.class) ;
        
        if (args.length > 6)
            method.invoke(sortPresenter, (Object[]) Arrays.copyOfRange(args,6,args.length)) ;
        else
            method.invoke(sortPresenter) ;
        }
        catch ( Exception e )
        {
            LOGGER.log(Level.SEVERE, "{0} {1}", new Object[] {e.toString(),e.getLocalizedMessage()});
        }
        
    }
    
    public SortPresenter()
    {
        super() ;
    }
    
    public SortPresenter(String applicationTitle, String chartTitle)
    {
        super(applicationTitle, chartTitle);
    }
    
    // TODO: Implement this, to construct SortPresenter from saved .txt file.
    /*public SortPresenter(String simName, String chartTitle, String reportFilePath)
    {
        super(simName,chartTitle,reportFilePath) ;
        applicationTitle = simName ;
        setReporter(new SortReporter(simName,reportFilePath)) ;
    }
    */
    
    public SortPresenter(String applicationTitle, String chartTitle, SortReporter reporter)
    {
        super(applicationTitle,chartTitle, reporter) ;
        setReporter(reporter) ;
    }
    
    /**
     * Overrides super.setReporter() because reporter is now PopulationReporter
     * @param reporter 
     */
    public void setReporter(SortReporter reporter)
    {
        this.reporter = reporter ;
    }

    /**
     * Plots the number of infections received by Agents with PrEP status,
     * given by (boolean) value, per cycle.
     * @param value 
     */
    public void plotReceiveSortPrepStatusReport(String value)
    {
        LOGGER.info("prepareReceiveSortPrepStatusReport");
        HashMap<Object,HashMap<Object,ArrayList<Object>>> receiveSortPrepStatusReport 
                = reporter.prepareReceiveSortPrepStatusReport(value) ;
        LOGGER.log(Level.INFO, "{0}", receiveSortPrepStatusReport);
        HashMap<Object,HashMap<Object,ArrayList<Object>>> invertedPrepStatusReport 
                = SortReporter.invertHashMapHashMap(receiveSortPrepStatusReport) ;
        LOGGER.info("prepareTransmissionCountReport");
        ArrayList<ArrayList<Object>> nbTransmissionReport 
                = ((EncounterReporter) reporter.getUnsortedReporter()).prepareReceiveCountReport(invertedPrepStatusReport) ;
        LOGGER.log(Level.INFO, "{0}", nbTransmissionReport);
        LOGGER.info("plotCycleValue");
        plotEventsPerCycle("nbTransmissions", nbTransmissionReport) ;
    }
    
    /**
     * Plots the prevalence of STI among MSM with partnerCount new Relationships in the 
     * past backYears years.
     */
    public void plotSortPrevalence(int partnerCount, int backYear)
    {
        HashMap<Object,Number> prevalenceSortCount = new HashMap<Object,Number>() ;
        
        ArrayList<Object> prevalenceSortReport ;
        String prevalenceSortRecord ;
        Number prevalence ;
        for (int nbPartner = 1 ; nbPartner <= partnerCount ; nbPartner++ )
        {
            LOGGER.info("nbPartner:" + String.valueOf(nbPartner));
            prevalenceSortReport = reporter.prepareSortPrevalenceReport(nbPartner, backYear) ;
            if (prevalenceSortReport.size() > 0)
            {
                prevalenceSortRecord = String.valueOf(prevalenceSortReport.get((prevalenceSortReport.size()-1))) ;
                prevalence = (Number) Double.valueOf(Reporter.extractValue("prevalence", prevalenceSortRecord)) ;
            }
            else 
                break ;
            prevalenceSortCount.put(nbPartner, prevalence) ;
            LOGGER.info(prevalenceSortRecord);
        }
        
        plotHashMapNumber("nbPartners","prevalence", prevalenceSortCount) ;
    }
    
    /**
     * Plots the mean number of relationships entered by an Agent by a given age.
     */
    public void plotAgeNumberEnteredRelationshipRecord()
    {
        HashMap<Object,Double> ageNumberEnteredRelationshipRecord 
                = reporter.prepareAgeNumberEnteredRelationshipRecord() ;
        
        plotHashMapDouble("age","nbPartners", ageNumberEnteredRelationshipRecord ) ;
    }
    
}
