/**
 * 
 */
package agent;

import community.* ;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import reporter.* ;

import reporter.Reporter ;
import site.* ;

import java.util.Random ;

//import com.sun.media.jfxmedia.logging.Logger;
import java.lang.reflect.*;


import java.util.ArrayList;
import java.util.Set ;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.distribution.GammaDistribution;
import static reporter.Reporter.AGENTID;

/**
 * @author <a href = "mailto:mlwalker@kirby.unsw.edu.au">Michael Walker</a>
 *
 */
public abstract class Agent {
    // String representation of Agent subclass
    final private String agent ;

    // Number in Community.population
    private int agentId ;

    /** Age of Agent in cycles (days)*/
    private int age ;

    /** Age beyond which Agents are removed from the population. */
    static int MAX_LIFE = 65 ;
    
    //static String FOLDER_PATH = "/srv/scratch/z3524276/prepsti/"
    //static String FOLDER_PATH = "/short/is14/mw7704/prepsti/"
    static String FOLDER_PATH = ""
    //+  "output/year2007/" ;
    // +  "output/year2010/" ;
    //+  "output/year2012/" ;
     +  "output/test/" ;
    // +  "output/prePrEP/" ;
    
    
    /** Names of Sites for Agent*/ 
    static public String[] SITE_NAMES = new String[] {} ;
    
    
    // Age-specific death rates from ABS.Stat
    static double RISK_60 = 5.6 ;
    static double RISK_55 = 3.7 ;
    static double RISK_50 = 2.5 ;
    static double RISK_45 = 1.6 ;
    static double RISK_40 = 1.2 ; 
    static double RISK_35 = 0.8 ;
    static double RISK_30 = 0.6 ;
    static double RISK_25 = 0.4 ;
    static double RISK_20 = 0.4 ;
    static double RISK_15 = 0.2 ;

    final static int DAYS_PER_YEAR = 365 ;
    
    // Need to generate random numbers
    /** Generate and record Random number seed. */
    static long RANDOM_SEED = System.nanoTime() ;
    static Random RAND = new Random(RANDOM_SEED) ;
    
    static public void SET_RAND(long seed)
    {
        RANDOM_SEED = seed ;
        RAND = new Random(RANDOM_SEED) ;
    }
    
    /** get RANDOM_SEED.
     * @return  */
    static public final long GET_RANDOM_SEED()
    {
        return RANDOM_SEED ;
    }
    
    /** String representation of "true". */
    static String TRUE = "true" ;
    /** String representation of "false". */
    static String FALSE = "false" ;
    /** String representation of "Monogomous". */
    static String MONOGOMOUS = "Monogomous" ;
    /** String representation of "Regular". */
    static String REGULAR = "Regular" ;
    /** String representation of "Casual". */
    static String CASUAL = "Casual" ;
    
    // agentId of next Agent to be created, current number plus one
    static public int NB_AGENTS_CREATED = 0 ;

    /** Probability of screening in a given cycle when symptomatic is false. */
    static double SCREEN_PROBABILITY = 0.001 ;
    
    /** 
     * The fraction by which infidelity is multiplied every additional year after 
 the startAge of 30. 
     */
    static double INFIDELITY_FRACTION = 0.5 ;
    
    /** Standard String null response */
    static String NONE = "none" ;
    
    
    // The maximum number of relationships an agent may be willing to sustain
    //static int MAX_RELATIONSHIPS = 15;
    
    /**
     * Coordinates the reinitialisation of Agent parameters when they change 
     * year-by-year.
     * @param agentList
     * @param year
     * @return 
     */
    static public String REINIT(ArrayList<Agent> agentList, int year) 
    {
        String report = "" ;
        //boolean successful = true ;
        String change = "change" ;
        String methodName = "" ;
        
        
        //TODO: Automate detection of MSM subClass with reflect
        // Update MSM variables
        report += MSM.REINIT(agentList, year) ;
        
        try
        {
            // Needs to be called after MSM.REINIT() specifically MSM.REINIT_RISK_ODDS()
            // due to its updating prepStatus.
            methodName = "screen" ;
            report += Reporter.ADD_REPORT_PROPERTY(change, methodName) ;
            REINIT_SCREEN_CYCLE(agentList, year) ;
            //REINIT_NB_RELATIONSHIPS(agentList, year) ;
            
            //MSM.REINIT_USE_GSN(agentList, year) ;
        }
        catch ( Exception e )
        {
            LOGGER.severe(e.toString() + " in method " + methodName) ;
            //return false ;
        }
        
        return report.concat("!") ;
    }
    
    /**
     * Tests, given by per 1000 per year, from 2007-2018
     * Table 14 ARTB 2018
     */
    static double[] TEST_RATES = {333,340,398,382,383,382,391,419,445,499,488,488,488} ;
    
    /**
     * Adjusts per year the screening period.
     * TODO: Implement reporting of changes.
     * @param (ArrayList) List of Agents to be changed.
     * @param (int) year
     * @throws Exception 
     */
    static private String REINIT_SCREEN_CYCLE(ArrayList<Agent> agentList, int year) throws Exception
    {
        String report = "" ;
        //double[] testRates = new double[] {333,340,398,382,383,382,391,419,445,499,488,488,488} ;
        // 2007 - 2009
        // 333,340,398,
        
        int newScreenCycle ;
        
        if (year >= TEST_RATES.length)
            year = TEST_RATES.length - 1 ;
        
        double testBase ;
        //testBase = testRates[0] ;
        testBase = TEST_RATES[year-1] ;
        
        double ratio = testBase/TEST_RATES[year] ;
        for (Agent agent : agentList)
        {
            newScreenCycle = (int) Math.ceil(ratio * agent.getScreenCycle()) ;
            agent.setScreenCycle(newScreenCycle) ;
        
            // Do not reinitialise MSM on Prep
        }
        return report ;
    }
    
    // number of relationships willing to maintain at once
    private int concurrency ;

    /** odds of choosing a Monogomous Relationship. */
    double monogomousOdds = Monogomous.BREAKUP_PROBABILITY ;
    /** odds of choosing a Regular Relationship. */
    double regularOdds = Regular.BREAKUP_PROBABILITY ;
    /** odds of choosing a Casual Relationship. */
    double casualOdds = Casual.BREAKUP_PROBABILITY ;
 
    // probability of cheating on a monogomous spouse
    private double infidelity = 0.03;

    // current partners
    private ArrayList<Integer> currentPartnerIds = new ArrayList<Integer>() ;


    private ArrayList<Relationship> currentRelationships 
            = new ArrayList<Relationship>() ;

    private int lowerAgentId = 0 ;

    // number of Casual Relationships
    private int casualNumber = 0 ;

    // number of Regular Relationships
    private int regularNumber = 0 ;

    // whether currently in a Monogomous Relationship 
    private boolean inMonogomous = false ;

    // total numberof Relationships
    private int nbRelationships = 0 ;

    // current availability for a new relationship
    private boolean available = true ;

    /** Whether the Agent is infected with an STI of interest. */
    private boolean infectedStatus = false ;
    /** Whether any Site of the Agent is showing symptoms of the STI.
     * Should be false if infecteStatus is false.
     */
    private boolean symptomatic = false ;
    
    /** Days between asymptomatic STI screens . */
    private int screenCycle = 92 ;
    
    /** Cycles remaining until next STI screen. */
    private int screenTime ;

    /** probability of using condom even when apparently safe (PrEP, TasP, etc) */
    protected double probabilityUseCondom = RAND.nextDouble() ;
    
    /** Does the Agent use a GeoSpatial Network (eg Grindr) */
    protected boolean useGSN = false ;

    // names of fields of interest to the census.
    private String[] censusFieldNames = {"agentId","agent","concurrency","infidelity"} ;
            //"symptomatic","available","inMonogomous","regularNumber","casualNumber",
            //"nbRelationships","screenCycle"

    static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger("agent") ;

    /**
     * In subclasses the choice of site might depend on the Class of Relationship.
     * @param agent0
     * @param agent1
     * @param relationshipClazzName
     * @return Probabilistically chosen Site[2] of Sites for sexual contact.
     */
    public static Site[] chooseSites(Agent agent0, Agent agent1, String relationshipClazzName)
    {
        return chooseSites(agent0, agent1) ;
    }
    
    /**
     * TODO: Implement Method based on relative frequency of types of sexual contact.
     * @param agent0
     * @param agent1
     * @param relationshipClazzName
     * @return Probabilistically chosen Site[2] of Sites for sexual contact.
     */
    public static Site[] chooseSitesNew(Agent agent0, Agent agent1, String relationshipClazzName)
    {
        return chooseSites(agent0, agent1, relationshipClazzName) ;
    }
    
    /**
     * @param agent0
     * @param agent1
     * @return Probabilistically chosen Site[2] of Sites for sexual contact.
     */
    public static Site[] chooseSites(Agent agent0, Agent agent1)
    {
        Site site0 = agent0.chooseSite() ;
        Site site1 = agent1.chooseSite(site0) ;
        return new Site[] {site0,site1} ;
    }

    /**
     * 
     * @param agent0
     * @param agent1
     * @param relationshipName
     * @return (boolean) whether condom is used in sexual encounter.
     */
    public static boolean USE_CONDOM(Agent agent0, Agent agent1, String relationshipName)
    {
        return ((agent0.chooseCondom(relationshipName, agent1)) || (agent1.chooseCondom(relationshipName, agent0))) ;
    }

    /**
     * Reloads Agents from a saved simulation to continue it.
     * @param simName 
     * @param rebootFile 
     * @return  
     */
    static public ArrayList<Agent> REBOOT_AGENTS(String simName, boolean rebootFile)
    {
        ArrayList<Agent> agents = new ArrayList<Agent>() ;
        String folderPath = FOLDER_PATH ;
        String SITE = "Site:" ;
        
        // Needed if rebootFile == false
        ArrayList<String> birthReport = new ArrayList<String>() ;
        ArrayList<ArrayList<Object>> agentDeathReport = new ArrayList<ArrayList<Object>>() ;
        String screeningRecord = "" ;
        if (rebootFile)
        {
            String rebootFileName = simName.concat("-REBOOT.txt") ;
            try
            {
                BufferedReader fileReader = new BufferedReader(new FileReader(folderPath + rebootFileName)) ;
                // Find last line
                for (String record = "" ;  record != null ; record = fileReader.readLine() )
                {
                    int agentIndex = record.indexOf(Reporter.AGENTID) ;
                    if (agentIndex > 0)
                    {
                        birthReport.add(record.substring(agentIndex)) ;
                        break ;
                    }
                }
                fileReader.close() ;
            }
            catch (Exception e)
            {
                LOGGER.info(e.toString()) ;
            }
        }
        else
        {
            PopulationReporter populationReporter = new PopulationReporter(simName,folderPath) ;
            //PopulationReporter populationReporter = new PopulationReporter(simName,"/srv/scratch/z3524276/prepsti/output/test/") ;
            //PopulationReporter populationReporter = new PopulationReporter(simName,"/short/is14/mw7704/prepsti/output/year2007/") ;

            birthReport = populationReporter.prepareBirthReport() ;

            agentDeathReport = populationReporter.prepareAgentDeathReport() ;

            ScreeningReporter screeningReporter = new ScreeningReporter(simName,folderPath) ; // "output/prePrEP/") ;
            //ScreeningReporter screeningReporter = new ScreeningReporter(simName,"/srv/scratch/z3524276/prepsti/output/test/") ;
            //ScreeningReporter screeningReporter = new ScreeningReporter(simName,"/short/is14/mw7704/prepsti/output/year2007/") ;

            screeningRecord = screeningReporter.getFinalRecord() ;
        }
        int infectionIndex ;
        int startAge ;
        String id ;
                        
        String className ;
        Site[] sites ;
        ArrayList<String> siteNames = new ArrayList<String>(Arrays.asList(MSM.SITE_NAMES)) ;    // TODO: Replace with Agent.SITE_NAMES
        String infectionString ;
        //int daysPerYear = 365 ;
        
        ArrayList deadAgentIds = new ArrayList<Object>() ; // Get ArrayList of dead Agents so we don't waste time reading their data
        if (!rebootFile)
            for (ArrayList<Object> agentDeathRecord : agentDeathReport)
                deadAgentIds.addAll(agentDeathRecord) ;
        
        // Reboot saved Agent data 
        int birthIndex = 0 ;
        for (String birthRecord : birthReport)
        {
            ArrayList<String> birthList = Reporter.EXTRACT_ARRAYLIST(birthRecord,AGENTID) ;
            if (birthList.isEmpty()) 
                continue ;
            birthIndex += birthList.size() ;
            ArrayList<String> properties = Reporter.IDENTIFY_PROPERTIES(birthList.get(0).substring(0, birthList.get(0).indexOf(SITE))) ;
            
            for (String birth : birthList)
            {
                // Extract Agents still living
                id = Reporter.EXTRACT_VALUE(AGENTID, birth);
                if (deadAgentIds.contains(id))
                    continue ;
                
                startAge = Integer.valueOf(Reporter.EXTRACT_VALUE("age",birth));
                //age += (maxCycles - birthIndex)/daysPerYear ;
                className = Reporter.EXTRACT_VALUE("agent",birth);
                try
                {
                    //clazz = Class.forName(className);
                    MSM newAgent = new MSM(startAge) ;
                    REBOOT_AGENT(newAgent, birth, properties) ;
                    newAgent.clearInfection();
                    agents.add(newAgent) ;
                    
                    // Reload infections
                    if (rebootFile)
                        infectionString = birth ;
                    else
                    {
                        infectionIndex = screeningRecord.indexOf(Reporter.AGENTID.concat(":").concat(id).concat(" ")) ;
                        if (infectionIndex < 0)
                            continue ;
                        infectionString = Reporter.EXTRACT_BOUNDED_STRING(Reporter.AGENTID, screeningRecord, infectionIndex);
                    }
                    
                    //LOGGER.info(infectionString);
                    sites = newAgent.getSites();
                    
                    String stringSite = infectionString.substring(infectionString.indexOf(SITE)) ;
                    //LOGGER.info(stringSite);
                    String propertySite ; // = birthRecord.substring(birthRecord.indexOf(siteString)) ;
                    ArrayList<String> siteProperties ;
                    String valueString ;
                    for (Site site : sites)
                    {
                        propertySite = Reporter.BOUNDED_STRING_BY_VALUE("Site",site.toString(),"Site", stringSite) ;
                        siteProperties = Reporter.IDENTIFY_PROPERTIES(propertySite) ;
                        siteProperties.remove("Site") ;
                        siteProperties.removeAll(Arrays.asList(siteNames)) ;
                        try
                        {
                            for (String property : siteProperties)
                            {
                                // Finished properties and now reading infected Site status
                                //if (siteNames.contains(property))
                                  //  break ;
                                
                                if ("symptomatic".equals(property))
                                {
                                    valueString = Reporter.EXTRACT_VALUE("symptomatic", propertySite);
                                    if (!Reporter.CLEAR.equals(valueString))
                                    {
                                        // Infection status but not symptomatic status
                                        boolean oldSymptomatic = newAgent.getSymptomatic() ;
                                        newAgent.receiveInfection(1.1, site) ;
                                        newAgent.setSymptomatic(oldSymptomatic) ;

                                        // Symptomatic status
                                        newAgent.setSymptomatic(Boolean.valueOf(valueString),site) ;
                                        //TODO: Generalise for multiple arbitrary Site properties, as with newAgent above
                                        newAgent.setInfectionTime(Integer.valueOf(Reporter.EXTRACT_VALUE("infectionTime", stringSite)), site);
                                    }
                                    continue ;
                                }
                                
                                valueString = Reporter.EXTRACT_VALUE(property, propertySite);
                                //if (property.equals("screenInterval"))
                                //  property = "screenCycle" ;
                                Class valueOfClazz ;
                                
                                String getterName = "get" + property.substring(0,1).toUpperCase() 
                                                + property.substring(1) ;
                                Method getterMethod = Site.class.getDeclaredMethod(getterName) ;
                                Class propertyClazz = getterMethod.getReturnType() ;
                                        
                                if (propertyClazz.equals(int.class))
                                    valueOfClazz = Integer.class ;
                                else if (propertyClazz.equals(double.class))
                                    valueOfClazz = Double.class ;
                                else if (propertyClazz.equals(boolean.class))
                                    valueOfClazz = Boolean.class ;
                                else
                                {
                                    //LOGGER.info(property) ;
                                    valueOfClazz = propertyClazz ;
                                }
                                Method valueOfMethod = valueOfClazz.getMethod("valueOf", String.class) ;
                                //LOGGER.log(Level.INFO,"{1} {0}", new Object[] {valueOfMethod.invoke(null,valueString),property});

                                String setterName = "set" + property.substring(0,1).toUpperCase() 
                                        + property.substring(1) ;
                                Method setMethod = Site.class.getDeclaredMethod(setterName, propertyClazz) ;
                                setMethod.invoke(site, valueOfMethod.invoke(null,valueString)) ;
                            }

                        }
                        catch (Exception e)
                        {
                            LOGGER.severe(e.toString());
                            //LOGGER.log(Level.SEVERE, "{0} {1} {2}", new Object[] {propertyClazz, valueString}) ;
                        }
        
                        // Check infection/symptomatic status of site
                        
                        
                        // siteName: indicates infected site
                        /*siteIndex = infectionString.substring(infectionString.indexOf(siteString)).indexOf(site.toString()+":") ;
                        if (siteIndex > 0)    // (infectionString.contains(site.toString()))
                        {
                            Boolean symptoms = Boolean.valueOf(Reporter.EXTRACT_VALUE(site.toString(),infectionString,siteIndex));
                            if (rebootFile)
                            {
                                newAgent.receiveInfection(1.1,site) ;
                                //newAgent.setInfectedStatus(true) ;
                                
                                // Set symptomatic, or not
                                if (symptoms)
                                    newAgent.setSymptomatic(true,site) ;
                                else
                                    newAgent.setSymptomatic(false,site) ;
                                //newAgent.chooseSymptomatic(site) ;
                                
                                // Set remaining infectionTime
                                //infectionTime = Reporter.EXTRACT_VALUE("screenTime", infectionString, siteIndex) ;
                                //site.setInfectionTime(Integer.valueOf(infectionTime)) ;
                            }
                            else
                            {
                                newAgent.reinitInfectedStatus(symptoms, site) ;
                            }
                        //uninfected = false ;
                        }*/
                    }
                    //if (uninfected)
                    //LOGGER.info(infectionString) ; // (String.valueOf(newAgent.getInfectedStatus()));
                }
                catch ( Exception e )
                {
                    LOGGER.log(Level.SEVERE, "{0} {1}", new Object[]{e.getClass().getCanonicalName(), className});
                }
            }
        }
        NB_AGENTS_CREATED = birthIndex ;
        
        return agents ;
    }
    
    /**
     * Restores individual Agents' properties from saved census data from previous simulation.
     * @param newAgent
     * @param census
     * @param propertyArray 
     */
    static void REBOOT_AGENT(Agent newAgent, String census, ArrayList<String> propertyArray)
    {
        propertyArray.remove("agent") ;
        propertyArray.remove("age") ;
        
        Field[] agentDeclaredFields = Agent.class.getDeclaredFields() ;
        
        HashMap<Class,Field[]> clazzFields = new HashMap<Class,Field[]>() ;  
        clazzFields.put(Agent.class , Agent.class.getDeclaredFields()) ;
        //for (Field field : agentDeclaredFields)
          //  agentFieldNames.add(field.getName()) ;
        
        for (Class agentClazz = newAgent.getClass() ; !Agent.class.equals(agentClazz) ; agentClazz = agentClazz.getSuperclass())
            clazzFields.put(agentClazz , agentClazz.getDeclaredFields()) ;
        
        //Class agentClazz = newAgent.getClass().getSuperclass() ;
        //LOGGER.log(Level.INFO,"clazzFields:{0}",clazzFields);

        String valueString = "";
        String setterName ;
        Class propertyClazz = Object.class ;
        Class valueOfClazz = propertyClazz ;
        Class methodClazz = Agent.class ;
        Method setMethod ;
        Method valueOfMethod ;
        String testProperty = "" ;
        try
        {
            for (String property : propertyArray)
            {
                testProperty = property ;
                //LOGGER.info(testProperty) ;
                valueString = Reporter.EXTRACT_VALUE(property, census) ;
                //if (property.equals("screenInterval"))
                  //  property = "screenCycle" ;
                for (Class agentClazz : clazzFields.keySet())
                    for (Field field : clazzFields.get(agentClazz))
                        if (field.getName().equals(property))
                        {
                            propertyClazz = agentClazz.getDeclaredField(property).getType() ;
                            methodClazz = agentClazz ;
                            if (propertyClazz.equals(int.class))
                                valueOfClazz = Integer.class ;
                            else if (propertyClazz.equals(double.class))
                                valueOfClazz = Double.class ;
                            else if (propertyClazz.equals(boolean.class))
                                valueOfClazz = Boolean.class ;
                            else
                            {
                                //LOGGER.info(property) ;
                                valueOfClazz = propertyClazz ;
                            }
                            valueOfMethod = valueOfClazz.getMethod("valueOf", String.class) ;
                            //LOGGER.log(Level.INFO,"{1} {0}", new Object[] {valueOfMethod.invoke(null,valueString),property});

                            setterName = "set" + property.substring(0,1).toUpperCase() 
                                    + property.substring(1) ;
                            setMethod = methodClazz.getDeclaredMethod(setterName, propertyClazz) ;
                            setMethod.invoke(newAgent, valueOfMethod.invoke(null,valueString)) ;
                            break ;
                        }
            }
        }
        catch (Exception e)
        {
            LOGGER.severe(e.toString());
            LOGGER.log(Level.SEVERE, "{0} {1} {2}", new Object[] {propertyClazz, testProperty, valueString}) ;
        }
        
    }
    
    /**
     *  
     * Agent Class  
     * @param startAge - (int) Age at (sexual) birth
     */
    public Agent(int startAge)
    {
            this.agentId = NB_AGENTS_CREATED ;
            NB_AGENTS_CREATED++ ;
            initAge(startAge) ;
            
            initConcurrency() ;
            initInfidelity() ;
            
            // initRelationshipOdds() ;    // Defined in preamble.

            Class<?> clazz = this.getClass() ;
            agent = clazz.asSubclass(clazz).getSimpleName() ;

    }

    public int getAgentId()
    {
            return agentId ;
    }

    private void setAgentId(int id)
    {
        this.agentId = id ;
    }
    
    /**
     * Initialises startAge of new Agents
     * @param startAge = -1, choose random startAge from 16 to 65, for initialising community
     *            = 0 , choose random startAge from 16 to 25, new agents entering sexual maturity
     *            > 0 , set startAge to startAge
     */
    private int initAge(int startAge)
    {
        int ageYears ;
        if (startAge == -1) // Choose random starting Age from 16 to 65
            ageYears = RAND.nextInt(50) + 16 ;
        else if (startAge == 0) // Choose random starting Age from 16 to 25, reaching sexual maturity
            ageYears = RAND.nextInt(10) + 16 ;
        else
            ageYears = startAge ;
        age = ageYears * DAYS_PER_YEAR + RAND.nextInt(DAYS_PER_YEAR) ;
        return age ;
    }

    /**
     * Initialises odds for different Relationship types
     */
    private void initRelationshipOdds()
    {
        try
        {
            // odds of choosing a Monogomous Relationship
            monogomousOdds = RAND.nextDouble() * Monogomous.BREAKUP_PROBABILITY ;
            // odds of choosing a Regular Relationship
            regularOdds = RAND.nextDouble() * Regular.BREAKUP_PROBABILITY ;
            // odds of choosing a Casual Relationship
            casualOdds = RAND.nextDouble() * Casual.BREAKUP_PROBABILITY ;
        }
        catch ( Exception e )
        {
            LOGGER.log(Level.INFO, "{0}", e.getLocalizedMessage());
        }

    }
    
    /**
     * Initialises Agent infectedStatus while ensuring consistency with 
     * Site.infectedStatus .
     * @param startAge
     */
    final public void initInfectedStatus(int startAge)
    {
        if (startAge >= 0)
            return ;
        
        // else startAge<0 indicates initial population
        boolean infected = false ;    //  getInfectedStatus() ;
        for (Site site : getSites())
        {
            infected = (site.initialiseInfection() || infected)  ;
            setSymptomatic(site) ;
        }
        setInfectedStatus(infected) ;
    }
    
    /**
     * 
     * @return (int) startAge of Agent.
     */
    public int getAge() 
    {
        return Math.floorDiv(age,DAYS_PER_YEAR) ;
    }

    /**
     * Sets age of Agent to ageYears years and random (0 to 365) days.
     * @param ageYears
     * @param ageDays 
     */
    public void setAge(int ageYears, int ageDays) 
    {
            age = ageYears * DAYS_PER_YEAR + ageDays ;
    }

    public ArrayList<Relationship> getCurrentRelationships()
    {
            return currentRelationships ;
    }

    public ArrayList<Integer> getCurrentPartnerIds()
    {
            return currentPartnerIds ;
    }
    
    /**
     * 
     * @return (int) the number of current Relationships.
     */
    public int getNumberCurrentRelationships()
    {
        return currentPartnerIds.size() ;
    }

    /**
     * Randomly choose the number of simultaneous relationships an agent may have
     */
    private int initConcurrency()
    {
            concurrency = RAND.nextInt(getMaxRelationships()) + 1 ;
            return concurrency ;
    }

    /**
     * 
     * @return concurrency
     */
    public int getConcurrency()
    {
            return concurrency ;
    }
    
    /**
     * Setter of concurrency.
     * @param newConcurrency 
     */
    public void setConcurrency(int newConcurrency)
    {
        concurrency = newConcurrency ;
    }
    
    /**
     * Getter of useGSN.
     * @return (boolean) whether Agent uses a GSN.
     */
    public boolean getUseGSN()
    {
        return useGSN ;
    }
    
    /**
     * Setter of useGSN.
     * @param gsn 
     */
    public void setUseGSN(boolean gsn)
    {
        useGSN = gsn ;
    }
    	
    abstract int getMaxRelationships() ;
    
    /**
     * 
     * @return (int) the number of orgies in a community per cycle
     */
    //abstract public int getOrgyNumber() ;
    
    /**
     * 
     * @return (int) number of Agents invited to join any given orgy
     */
    abstract public int getGroupSexEventSize();

    /**
     * 
     * @return (double) the probability of Agents joining an orgy when invited
     */
    abstract public double getJoinGroupSexEventProbability() ;
    
    public double getMonogomousOdds()
    {
            return monogomousOdds ;
    }

    public double getRegularOdds()
    {
            return regularOdds ;
    }

    public double getCasualOdds()
    {
            return casualOdds ;
    }

    /**
     * Initialises screenCycle from a Gamma distribution to determine how often 
     * an Agent is screened, and then starts the cycle in a random place so that 
     * not every Agent gets screened at the same time.
     */
    abstract void initScreenCycle(double rescale) ;
    
    abstract void reInitScreenCycle(double rescale) ;
    
    protected int sampleGamma(double shape, double scale, double rescale)
    {
        return (int) new GammaDistribution(shape,scale * rescale).sample() ;
    }
    
    /**
     * Getter for risky status
     * @return riskyStatus (boolean) whether the Agent practices risky behaviour 
     * regarding condom use.
     */
    abstract public boolean getRiskyStatus() ;
    
    /**
     * Setter for riskyStatus.
     * @param risky (boolean) new value for riskyStatus.
     */
    abstract public void setRiskyStatus(boolean risky) ;
    
    /**
     * Randomly choose the agent's probability of cheating on a Monogomous spouse.
     */
    private void initInfidelity()
    {
        int maximum = getMaxRelationships() ;
        infidelity = 0.01 * RAND.nextInt(maximum)/maximum ;
    }
    
    /**
     * Setter for infidelity, probability of cheating on a Monogomous partner.
     * @param newInfidelity (double) New value for infidelity.
     */
    public void setInfidelity(double newInfidelity)
    {
        assert((newInfidelity >= 0) && (newInfidelity <= 1)) : "Error: infidelity should be between 0 and 1" ;
        infidelity = newInfidelity ;
    }

    /**
     * infidelity getter()
     * @return (double) infidelity
     */
    public double getInfidelity()
    {
            return infidelity ;
    }

    /**
     * 
     * @return (String) giving values of the agent's important properties.
     */
    public String getCensusReport()
    {
        String censusReport = "" ;
        censusReport += Reporter.ADD_REPORT_PROPERTY("agentId",agentId) ;
        censusReport += Reporter.ADD_REPORT_PROPERTY("agent",agent) ;
        censusReport += Reporter.ADD_REPORT_PROPERTY("age",getAge()) ;  // Reporter.ADD_REPORT_PROPERTY("startAge", getAge()) ;
        censusReport += Reporter.ADD_REPORT_PROPERTY("concurrency",concurrency) ;
        censusReport += Reporter.ADD_REPORT_PROPERTY("infidelity",infidelity) ;
        censusReport += Reporter.ADD_REPORT_PROPERTY("probabilityUseCondom",probabilityUseCondom) ;
        censusReport += Reporter.ADD_REPORT_PROPERTY("screenCycle",getScreenCycle()) ;
        censusReport += Reporter.ADD_REPORT_PROPERTY("screenTime",screenTime) ;
        
        /*Class fieldClazz ;
        Class agentClazz ;
        String getterName ;
        Method getMethod ;
        for (String fieldName : getCensusFieldNames() )
        {
            censusReport += Reporter.ADD_REPORT_PROPERTY(fieldName,Agent.class.getField(fieldName).get)  ;
            try
            {
                //fieldClazz = this.getClass().getField(fieldName).getClass() ;
                agentClazz = Agent.class ;    // Class.forName("String") ;
                getterName = "get" + fieldName.substring(0,1).toUpperCase() 
                        + fieldName.substring(1) ;
                getMethod = agentClazz.getMethod(getterName, (Class[]) null) ;
                censusReport += Reporter.ADD_REPORT_PROPERTY(fieldName,getMethod.invoke(this)) ;
            }
            catch ( NoSuchMethodException nsme)
            {
                LOGGER.log(Level.INFO, "NSME {0} {1}", new Object[]{fieldName, nsme.getLocalizedMessage()});
            }
            catch (IllegalAccessException iae) 
            {
                Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, iae);
            } 
            catch (IllegalArgumentException iae) 
            {
                Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, iae);
            } 
            catch (InvocationTargetException ite) 
            {
                Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ite);
            }
            //catch ( ClassNotFoundException cnfe)
            {
              //  LOGGER.log(Level.INFO, "CNFE {0} {1}", new Object[]{fieldName, cnfe.getLocalizedMessage()});
            } 
            //catch (NoSuchFieldException nsfe)
            {
              //  LOGGER.log(Level.INFO, "{0} {1}", new Object[]{fieldName, nsfe.getMessage()}) ;
            }
        }*/
        return censusReport ;
    }
    
    /**
     * 
     * @return (String) List of property values needed to pick up the simulation where it left off.
     */
    public String getRebootData()
    {
        String report = getCensusReport() ;
        // Infection status
        /*if (infectedStatus)
            for (Site site : getSites())
                if (site.getInfectedStatus() > 0)
                {
                    report += Reporter.ADD_REPORT_PROPERTY(site.toString(),site.getSymptomatic()) ;
                    report += Reporter.ADD_REPORT_PROPERTY("infectionTime",site.getInfectionTime()) ;
                }*/
        return report ;
    }
    
    /**
     * 
     * @return (String[]) Names of MSM fields of relevance to a census.
     */
    protected String[] getCensusFieldNames()
    {
        return censusFieldNames ;
    }

   
    /**
     * 
     * @return Site[] sites
     */
    abstract public Site[] getSites() ;
    
    /**
     * Used when choosing Site for sexual contact.
     * @return randomly chosen available Site
     */
    abstract Site chooseSite() ;

    /**
     * For when the choice of site depends on an already chosen site
     * @param site (Site) The Site which has already been chosen.
     * @return randomly chosen available Site
     */
    protected Site chooseSite(Site site)
    {
            return chooseSite() ;
    }

    public void ageOneDay()
    {
        age++ ;
    }
    
    /**
     * Called to adjust condom use to reflect behavioural trends.
     */
    public void adjustCondomUse() 
    {
        adjustProbabilityUseCondom() ;
    }
    
    /**
     * Randomly scales down probabilityUseCondom to reflect behavioural trends
     */
    public void adjustProbabilityUseCondom()
    {
        probabilityUseCondom *= RAND.nextDouble() ;
    }
    
    /**
     * Called to adjust condom use to reflect behavioural trends.
     * @param parameter (double) Factor between zero and one for scaling down
     * the probability of using a condom.
     */
    public void adjustCondomUse(double parameter) 
    {
        adjustProbabilityUseCondom(parameter) ;
    }
    
    /**
     * Randomly scales down probabilityUseCondom to no less than bottom times
     * its current value.
     * @param bottom - probabilityUseCondom scales no lower than this.
     */
    public void adjustProbabilityUseCondom(double bottom)
    {
        probabilityUseCondom *= (bottom + (1.0 - bottom) * RAND.nextDouble()) ;
    }
    
    /**
     * Setter for probabilityUseCondom.
     * @param useCondom (double) New probability of using a condom.
     */
    public void setProbabilityUseCondom(double useCondom)
    {
        probabilityUseCondom = useCondom ;
    }
    
    /**
     * Getter for probabilityUseCondom.
     * @return (double) The probably of choosing to use a condom when the choice is made.
     */
    public double getProbabilityUseCondom()
    {
        return probabilityUseCondom ;
    }
    
    /**
     *
     * Rescales the probability of using a condom by factor scale
     * @param scale
     */
    public double scaleProbabilityUseCondom(double scale) 
    {
        probabilityUseCondom *= scale ;
        return probabilityUseCondom ;
    }
    
    /**
     * Agent never again demands a condom
     */
    public void stopCondomUse()
    {
        setProbabilityUseCondom(0.0) ;
    }
    
    /**
     * Concurrency and infidelity decrease from startAge == 30
     * 
     * @return String description of any altered quantities if any, empty otherwise
     */
    protected String ageEffects()
    {
        String report = "" ;
        if (getAge() > 30)
        {
            // TODO: Modify propensity to consent to Casual Relationships
            //report += Reporter.ADD_REPORT_PROPERTY("consentCasual",concurrency) ;
            if (infidelity > 0.0)
            {
                infidelity *= INFIDELITY_FRACTION ;
                report += Reporter.ADD_REPORT_PROPERTY("infidelity",infidelity) ;
            }
        }
        return report ;
    }

    /**
     * 
     * @param relationshipClazzName
     * @param partner
     * @return true if Agent decides to use a condom, false otherwise
     */
    abstract protected boolean chooseCondom(String relationshipClazzName, Agent partner);
            
    /**
     * Probabilistically transmits infection to receiving site.
     * @param transmitProbability
     * @param site
     * @return true if receiving site becomes infected, false otherwise
     */
    public boolean receiveInfection(double transmitProbability, Site site)
    {
            if (site.receiveInfection(transmitProbability))
            {
                infectedStatus = true ;
                setSymptomatic(site) ;
                return true ;
            }
            return false ;
    }

    /**
     * Whether the Agent is infected and symptomatic at any Site with any STI
     * @return symptomatic
     */
    public boolean getSymptomatic()
    {
            return symptomatic ;
    }

    /**
     * Whether the Agent is infected and symptomatic at given Site site with any STI
     * @param site
     * @return site.symptomatic
     */
    public boolean getSymptomatic(Site site)
    {
            return site.getSymptomatic() ;
    }

    /**
     * Setter for symptomatic. Use with caution as Sites are not tracked.
     * @param newSymptomatic
     * @return 
     */
    public boolean setSymptomatic(boolean newSymptomatic)
    {
        return symptomatic = newSymptomatic ;
    }

    /**
     * The Agent becomes symptomatic if and only if the newly infected Site is.
     * @param site
     * @return 
     */
    public boolean setSymptomatic(Site site)
    {
        symptomatic = (site.getSymptomatic() || symptomatic) ;
        return symptomatic ;
    }

    /**
     * The Agent becomes symptomatic if and only if the newly infected Site is.
     * @param symptoms
     * @param site
     * @return 
     */
    public boolean setSymptomatic(boolean symptoms, Site site)
    {
        site.setSymptomatic(symptoms) ;
        symptomatic = (site.getSymptomatic() || symptomatic) ;
        return symptomatic ;
    }
    
    /**
     * Calls site.infectionTime() to set the number of days left for the 
     * untreated infection to clear. Used for rebooting Agents from previous 
     * simulation.
     * @param infectionTime
     * @param site 
     */
    protected void setInfectionTime(int infectionTime, Site site)
    {
        site.setInfectionTime(infectionTime) ;
    }

    /** screenTime setter().
     * @param time */
    public void setScreenTime(int time)
    {
        screenTime = time ;
    }

    /** screenTime getter().
     * @return  
     */
    public int getScreenTime()
    {
        return screenTime ;
    }

    /** 
     * screenCycle setter().
     * @param screen 
     */
    public void setScreenCycle(int screen)
    {
        screenCycle = screen ;
    }

    /** screenCycle getter().
     * @return screenCycle 
     */
    public int getScreenCycle()
    {
        return screenCycle ;
    }
    
    /**
     * Decreases the remaining time until the next screen by one cycle.
     */
    protected void decrementScreenTime()
    {
        screenTime-- ;
    }

    /**
     * May be overridden for specific agent subclasses with specific screening behaviours 
     * @param args (Object[]) for compatability with inherited versions which need input
     * parameters
     * @return Probability of screening when symptomatic is false
     */
    public double getScreenProbability(Object[] args)
    {
            return SCREEN_PROBABILITY ;
    }

    /**
     * Progresses course of STI in Agents who have one.
     * Treats Agents who are symptomatic or randomly choose to be treated.
     * Tracks if treatment was successful.
     * Check if disease has run its course and clears it if so.
     * @return (String) record in STIs progress
     */
    private String progressInfection(String[] args)
    {
        String record = "" ;
        boolean infected ;
        //long startTime = System.nanoTime() ;

        {
            //LOGGER.log(Level.INFO,"infected:{0}",agent.getAgentId());
            //record += Reporter.ADD_REPORT_PROPERTY("agentId",agent.getAgentId()) ;
            infected = getInfectedStatus();
            //record += Reporter.ADD_REPORT_PROPERTY("infected", infected) ;
            
            // Due for an STI screen?
            if (RAND.nextDouble() < getScreenProbability(args)) 
            {
                record += Reporter.ADD_REPORT_PROPERTY("agentId",agentId) ;
                record += Reporter.ADD_REPORT_LABEL("tested") ;
                if (infected)
                {
                    //LOGGER.info("screening agentId:"+String.valueOf(agent.getAgentId())) ;
                    
                    for (Site site : getSites())
                    {
                        if (getInfectedStatus(site) != 0)
                            record += Reporter.ADD_REPORT_PROPERTY(site.toString(), getSymptomatic(site)) ;
                    }
                    treat() ;
                    record += Reporter.ADD_REPORT_LABEL("treated") ;
                }
                record += " " ;
            }
            else if (infected)
            {
                //LOGGER.log(Level.INFO, "infected:{0}", agent.getAgentId());
                record += Reporter.ADD_REPORT_PROPERTY("agentId",agentId) ;
                for (Site site : getSites())
                {
                    if (getInfectedStatus(site) != 0)
                        record += Reporter.ADD_REPORT_PROPERTY(site.toString(), getSymptomatic(site)) ;
                    //LOGGER.info(site.toString()) ;
                }
                
                // agent.progressSitesInfection() allow infection to run one cycle of its course
                // and returns boolean whether agent is cleared (!stillInfected)
                if (progressSitesInfection())
                    record += Reporter.ADD_REPORT_PROPERTY("cleared") ;
                else if (symptomatic)
                    if (treatSymptomatic())  
                        record += Reporter.ADD_REPORT_PROPERTY("tested","treated") ;
            }
        }
        //LOGGER.info(record)
        return record ;
    }

    /**
     * Invoked when Agent is symptomatic. Call each site.treat(). If all treatments successful, call clearSymptomatic()
     * @return true if all sites successfully treated, false otherwise
     */
    public boolean treatSymptomatic()
    {
        Site[] sites = getSites() ;
        boolean successful = false ;
        int incubationLeft ;
        // Check incubation period, are symptoms manifest?
        for (Site site : sites)
            if (site.getSymptomatic())
            {
                incubationLeft = site.getIncubationTime() ;
                successful = (successful || (incubationLeft < 0)) ;
                
                if (incubationLeft >= 0)
                    site.setIncubationTime(incubationLeft - 1);
            }
        
        if (!successful)
            return false ;
        
        for (Site site : sites)
            if ((site.getInfectedStatus()!=0))
                site.treat() ;
//            if ((site.getInfectedStatus()!=0))
//                successful = (successful && site.treat()) ;
        //if (successful) 
        infectedStatus = false ;
        clearSymptomatic();
        screenTime = screenCycle ;
        return successful ;
    }

    /**
     * Invoked when Agent is asymptomatic. Call each site.treat(). If all treatments successful, call clearSymptomatic()
     * @return true if all sites successfully treated, false otherwise
     */
    public boolean treat()
    {
        Site[] sites = getSites() ;
        boolean successful = true ;
        for (Site site : sites)
            if ((site.getInfectedStatus()!=0))
                site.treat() ; //
        infectedStatus = false ;
        clearSymptomatic();
        screenTime = screenCycle ;
        return successful ;
    }

    /**
     * Invoked when Agent is asymptomatic. Call site.treat(). If all treatments successful, call clearSymptomatic()
     * @param site
     * @return true if all sites successfully treated, false otherwise
     */
    public boolean treat(Site site)
    {
        boolean successful = site.treat() ; //
        if (successful)
        {
            infectedStatus = false ;
            clearSymptomatic();
            screenTime = screenCycle ;
        }
        return successful ;
    }

    /**
     * Set symptomatic to false. Called only when all sites have been successfully treated.
     */
    protected void clearSymptomatic()
    {
        symptomatic = false ;
    }
    
    /**
     * Clear infection from all Sites and reset infectedStatus and 
     * symptomatic, accordingly.
     */
    public void clearInfection()
    {
        infectedStatus = false ;
        symptomatic = false ;
        Site[] sites = getSites() ;
        for (Site site : sites)
            site.clearInfection() ;
    }

    /**
     * Getter for infectedStatus
     * @return infectedStatus
     */
    public boolean getInfectedStatus()
    {
        return infectedStatus ;
    }

    /**
     * Getter for site.infectedStatus
     * @param site
     * @return site.infectedStatus
     */
    public int getInfectedStatus(Site site)
    {
        return site.getInfectedStatus() ;
    }

    /**
     * Setter of infectedStatus, and also ensures that symptomatic is
     * false if infectedStatus is false.
     * @param infected 
     */
    public void setInfectedStatus(boolean infected)
    {
        infectedStatus = infected ;
        symptomatic = symptomatic && infectedStatus ;
    }
    
    /**
     * Update symptomatic and infectedStatus according to the status of all 
     * Sites.
     */
    public void updateInfectedStatus()
    {
        infectedStatus = false ;
        Site[] sites = getSites() ;
        for (Site site : sites)
            infectedStatus = (infectedStatus || (site.getInfectedStatus() != 0)) ;
        symptomatic = false ;
        if (infectedStatus)
            for (Site site : sites)
                symptomatic = (symptomatic || site.getSymptomatic()) ;
    }
    
    /**
     * Progress infection, invoking each site.progressSitesInfection and 
 keeping track of whether the Agent is still infected.
     * @return (boolean) whether the infection was cleared
     */
    public boolean progressSitesInfection()
    {
        Site[] sites = getSites() ;
        boolean stillInfected = false ;
        for (Site site : sites)
            if (site.getInfectedStatus() > 0)
                stillInfected = ((!site.progressInfection()) || stillInfected ) ;
        
        setInfectedStatus(stillInfected) ;
        return !stillInfected ;
    }

    /**
     * Called by Methods using consentArgs() to generate desired data
     * TODO: Remove middle step if Relationship.subclass and Agent partner not
     * adequate for decision.
     * 
     * @param consentArgs
     * @return consent(String,Agent)
     */
    public boolean consent(Object[] consentArgs)
    {
        return consent((String) consentArgs[0],(Agent) consentArgs[1]) ;
    }
    
    /**
     * Whether to enter a proposed relationship of class relationshipClazz .
     * Currently according to whether in a monogomous relationship and 
     * the number of relationships already entered compared to concurrency.
     * It is advisable for subclasses of Agent to invoke this Method if they 
     * override with super.consent() .
     * 
     * @param relationshipClazzName - name relationship subclass
     * @param partner - agent for sharing proposed relationship
     * @return (boolean) true if accept and false otherwise
     */
    public boolean consent(String relationshipClazzName, Agent partner)
    {
        if (inMonogomous)
            if (RAND.nextDouble() > infidelity) 
                return false ;
        if (MONOGOMOUS.equals(relationshipClazzName))
            return (0 == currentRelationships.size()) ;
        if (REGULAR.equals(relationshipClazzName))
            return available ;
        else    // Asking for Casual Relationship
            return true ; // consentCasual(partner) ;
        //return available ;
    }
    
    public boolean seekRelationship(String relationshipClazzName)
    {
        if (inMonogomous)
            if (RAND.nextDouble() > infidelity) 
                return false ;
        if (CASUAL.equals(relationshipClazzName))
            return (RAND.nextDouble() < seekRelationshipProbability(CASUAL)) ;
        else if (REGULAR.equals(relationshipClazzName))
            return (RAND.nextDouble() < seekRelationshipProbability(REGULAR)) ;
        else if (0 == currentRelationships.size())   // Seeking Monogomous Relationship
            return (RAND.nextDouble() < seekRelationshipProbability(MONOGOMOUS)) ;
        return false ;
    }
    
    /**
     * 
     * @param relationshipClazzName
     * @return (double) probability of Agent seeking out Relationship of class
     * relationshipClazzName.
     */
    abstract protected double seekRelationshipProbability(String relationshipClazzName);
    
    /**
     * Whether to accept proposed Casual Relationship.
     * @param partner (Agent) with whom Relationship is proposed.
     * @return (Boolean) true if accept and false otherwise
     */
    abstract protected boolean consentCasual(Agent partner);
    
    /**
     * Probabilistically decide to accept invitation to join orgy
     * @param args
     * @return true to join and false otherwise
     */
    final public boolean joinGroupSexEvent(Object[] args)
    {
        if (inMonogomous)
            if (RAND.nextDouble() > infidelity)
                return false ;
        return (RAND.nextDouble() < getJoinGroupSexEventProbability()) ;
    }

    /**
     * Sets the availability according to the number of relationships and participation in 
     * a Regular relationship
     * @return (boolean) available
     */
    protected boolean updateAvailable()
    {
        available = (nbRelationships <= concurrency) ;
        return available ;
    }

    protected boolean getAvailable()
    {
        return available ;
    }

    /**
     * Adds relationship to ArrayList relationships and initiates entry into a relationship
     * @param relationship
     * @return String report number of Relationships
     */
    public String enterRelationship(Relationship relationship)
    {
        //String record = Reporter.ADD_REPORT_PROPERTY(Reporter.AGENTID0, agentId) ;
        int partnerId = relationship.getPartnerId(agentId) ;
        //record += Reporter.ADD_REPORT_PROPERTY(Reporter.AGENTID1,partnerId) ;
        //record += relationship.getRelationship() + " " ;

        currentRelationships.add(relationship) ;
        currentPartnerIds.add(partnerId) ;
        nbRelationships++ ;

        updateAvailable() ;

        return relationship.getRecord() ;
        //return record ;
    }

    /**
     * For new Relationships for which this Agent has the lower agentId
     * @return 
     */
    public String augmentLowerAgentId()
    {
        if (nbRelationships != currentRelationships.size())
            LOGGER.log(Level.SEVERE, "nbRelationships:{0} not equal to currentRelationships.size():{1}", 
                    new Object[]{nbRelationships,currentRelationships.size()});
        
        int lowerAgentInt = (int) Math.pow(2,(nbRelationships - 1)) ;
        if ((lowerAgentId & lowerAgentInt) == lowerAgentInt )
            LOGGER.log(Level.SEVERE, "agentId:{0} already lowerAgentId of Relationship with {3} {1} {2}", 
                    new Object[]{agentId,lowerAgentId,lowerAgentInt,currentRelationships.get((nbRelationships-1)).getPartnerId(agentId)});
        lowerAgentId = lowerAgentId ^ lowerAgentInt;
        return Integer.toString(lowerAgentId) ;
    }

    /**
     * For ending Relationships for which this Agent has the lower agentId
     * @param relationship
     * @return 
     */
    public String diminishLowerAgentId(Relationship relationship)
    {
        int relationshipIndex = currentRelationships.indexOf(relationship) ;
        if (relationshipIndex < 0)
        {
            LOGGER.log(Level.SEVERE, "Agent {0} shares no such Relationship with Agent {1}", 
                    new Object[]{agentId,relationship.getPartnerId(agentId)});
            return "No such relationship" ;
        }
        int lowerAgentInt = (int) Math.pow(2,relationshipIndex) ;
        
        /*if ((lowerAgentId & lowerAgentInt) != lowerAgentInt)
            LOGGER.log(Level.SEVERE, "agentId:{0} lowerAgentId:{1} lowerAgentInt:{2} nbRelationships:{3}", 
                    new Object[]{agentId,lowerAgentId,lowerAgentInt,nbRelationships});
        */
        
        // All bits up to and including relationshipIndex
        int smallerIndexMask = (2 * lowerAgentInt) - 1 ;
        
        // Keep only bits less than relationshipIndex
        int smallerIndex = lowerAgentId & (smallerIndexMask >> 1) ;
        if (smallerIndex > lowerAgentId)
            LOGGER.log(Level.SEVERE,"smallerIndex > lowerAgentId, agentId:{0} smallerIndex:{1} lowerAgentId:{2}", 
                    new Object[]{agentId,smallerIndex,lowerAgentId}) ;
        
        // Keep only bits greater than relationshipIndex
        int greaterIndex = lowerAgentId & ~smallerIndexMask ;
        if (greaterIndex < 0)
            LOGGER.log(Level.SEVERE, "agentId:{0} lowerAgentId:{1} smallerIndex:{2} nbRelationships{3}", 
                    new Object[]{agentId,lowerAgentId,smallerIndex,nbRelationships});
        
        // Move those bits one place to the right (smaller)
        greaterIndex = greaterIndex >> 1 ;
        
        // Combine to find new lowerAgentId
        lowerAgentId = smallerIndex + greaterIndex ;
        
        return Integer.toString(lowerAgentId) ;
    }

    public int getLowerAgentId()
    {
        return lowerAgentId ;
    }

    public void enterRelationship(int agentNb)
    {
        currentPartnerIds.add(agentNb) ;

        nbRelationships++ ;

        // Open to more ?
        updateAvailable() ;
    }

    /**
     * To avoid double application of Relationship.Methods() we act from the 
     * Agent with the smallest agentId
     * @param relationship
     * @return true if relationship left, false otherwise
     */
    final private String endRelationship(Relationship relationship)
    { 
        String record = "" ;
        //record = Reporter.ADD_REPORT_LABEL("death");
        record += Reporter.ADD_REPORT_PROPERTY(Relationship.RELATIONSHIP_ID, relationship.getRelationshipId()) ;
        
        relationship.getPartner(this).leaveRelationship(relationship) ;
        leaveRelationship(relationship) ;

        return record ;
    }

    /**
     * 
     * @return String indicating disease (HIV) status
     */
    public String declareStatus()
    {
        return NONE ;
    }

    /**
     * Override and call in subclass if details about possible partners are 
     * needed.
     * TODO: Currently not used but may be in future if Relationship subclass
     * and Agent partner are not longer enough data
     * 
     * @param relationshipClazzName
     * @param agent
     * @return String[] args of relationshipClazzName and other Properties 
     * relevant to deciding consent()
     */
    protected Object[] consentArgs(String relationshipClazzName, Agent agent) 
    {
        Object[] consentArgs = {relationshipClazzName,agent} ;
        return consentArgs ;
    }

    /**
     * Removes relationship and partner and modifies nbRelationships count by -1
     * TODO: Change to String Method and return report
     * @param relationship
     */
    public void leaveRelationship(Relationship relationship)
    {
        diminishLowerAgentId(relationship) ;
        // Leave specific relationship subclass
        try
        {
            String leaveMethodName = "leave" + relationship.getRelationship() ;
            Method leaveRelationshipMethod = Agent.class.getMethod(leaveMethodName, Relationship.class) ;
            leaveRelationshipMethod.invoke(this, relationship) ;
        }
        catch ( IllegalAccessException iae)
        {
            LOGGER.info(iae.getLocalizedMessage());
        }
        catch ( InvocationTargetException ite )
        {
            LOGGER.info(ite.getLocalizedMessage());
        }
        catch ( NoSuchMethodException nsme )
        {
            LOGGER.info(nsme.getLocalizedMessage()) ;
        }

        /*String debug = Integer.toString(relationship.getPartnerId(agentId)) + "::" ;
        for (int partnerI : currentPartnerIds)
        {
            debug += Integer.toString(partnerI) + " " ;
            debug += Integer.toString(currentPartnerIds.indexOf(partnerI)) + " : ";
        }
        LOGGER.info(debug);*/
        int partnerId = relationship.getPartnerId(agentId) ;
        int partnerIndex = currentPartnerIds.indexOf(partnerId) ;
        currentPartnerIds.remove(partnerIndex) ;
        int relationshipIndex = currentRelationships.indexOf(relationship) ;
        currentRelationships.remove(relationshipIndex) ;
        Relationship.DIMINISH_NB_RELATIONSHIPS() ;
        nbRelationships-- ;
        
    }

    /**
     * 
     * @param agentNb 
     */
    private void leavePartnerId(int agentNb)
    {
        int partnerIndex = currentPartnerIds.indexOf(agentNb) ;
        Relationship doomedRelationship = currentRelationships.get(partnerIndex) ;
        leaveRelationship(doomedRelationship) ;
    }

    public String enterCasual(Relationship relationship)
    {
        String report = "casual:" ;
        report += enterRelationship(relationship) ;
        casualNumber++ ;
        return report;
    }

    protected void enterCasual(int agentNb)
    {
            enterRelationship(agentNb) ;
            casualNumber++ ;
    }

    public String enterRegular(Relationship relationship)
    {
        String report = "regular:" ;
            report += enterRelationship(relationship) ;
            regularNumber++ ;
            return report ;
    }

    protected void enterRegular(int agentNb)
    {
            enterRelationship(agentNb) ;
            regularNumber++ ;
    }

    public String enterMonogomous(Relationship relationship)
    {
        String report = "monogomous:" ;
        report += enterRelationship(relationship) ;
        inMonogomous = true ;
        return report ;
    }

    protected void enterMonogomous(int agentNb)
    {
        enterRelationship(agentNb) ;
        inMonogomous = true ;
    }

    public void leaveCasual(Relationship relationship)
    {
        //leaveRelationship(relationship) ;
        casualNumber-- ;
    }

    public void leaveCasual(int agentNb)
    {
        leaveCasual(agentNb) ;
        casualNumber-- ;
    }

    public void leaveRegular(Relationship relationship)
    {
        //leaveRelationship(relationship) ;
        regularNumber-- ;
    }

    private void leaveRegular(int agentNb)
    {
        //leaveRelationship(agentNb) ;
        regularNumber-- ;
    }

    public void leaveMonogomous(Relationship relationship)
    {
        //leaveRelationship(relationship) ;
        inMonogomous = false ;
    }

    private void leaveMonogomous(int agentNb)
    {
        //leaveRelationship(agentNb) ;
        inMonogomous = false ;
    }

    /**
     * Calls death() to see if Agent dies and removes from agents if so.
     * @return true if agent dies and false otherwise
     */
    final public boolean grimReaper()
    {
        if (RAND.nextDouble() < getRisk() ) 
        {
            death() ;
            return true ;
        }
        return false ;
    }
    
    /**
     * Based on data from the Australian Bureau of Statistics for Australian 
     * population (http://stat.data.abs.gov.au/).
     * @return (double) probability of dying during any cycle depending on the 
 properties, here startAge, of the Agent.
     */
    protected double getRisk()
    {
        double risk ;
        int ageYears = getAge() ;
        if (ageYears > 65)
            return 1.0 ;
        else if (ageYears > 60)
            risk = RISK_60 ;
        else if (ageYears > 55) 
            risk = RISK_55 ;
        else if (ageYears > 50) 
            risk = RISK_50 ;
        else if (ageYears > 45) 
            risk = RISK_45 ;
        else if (ageYears > 40) 
            risk = RISK_40 ;
        else if (ageYears > 35) 
            risk = RISK_35 ;
        else if (ageYears > 30) 
            risk = RISK_30 ;
        else if (ageYears > 25)
            risk = RISK_25 ;
        else if (ageYears > 20)
            risk = RISK_20 ;
        else 
            risk = RISK_15 ;
        double noRisk = Math.pow((1 - risk/1000),1.0/DAYS_PER_YEAR) ;
        return 1 - noRisk ;
    }
        
        
    /**
     * Make agent die and clear their Relationships
 Default based on startAge, uniform probability with cutoff at maxLife
     * @return true if they die, false otherwise
     */
    final private void death()
    {
        //String report = Reporter.ADD_REPORT_LABEL("death") ; 
        //report += Reporter.ADD_REPORT_PROPERTY("agentId",agentId) ;
        //report += Reporter.ADD_REPORT_PROPERTY("startAge",startAge) ;
        //report += "nbPartners:" + String.valueOf(currentRelationships.size()) + " ";
        clearRelationships() ;
        // report ;
    }

    /**
     * Removes all Relationships of Agent. Called by death().
     */
    final private void clearRelationships()
    {
        Relationship relationship ;
        
        int startIndex = (nbRelationships - 1) ;
        String record = "" ;
        for (int relationshipIndex = startIndex ; relationshipIndex >= 0 ; 
                relationshipIndex-- )
        {
            relationship = currentRelationships.get(relationshipIndex) ;
            Agent agentLowerId = relationship.getLowerIdAgent() ;
            record += agentLowerId.endRelationship(relationship) ;
        }
        Relationship.APPEND_DEATH_RECORD(record);
    }
	
    /**
     * 
     * @return subclass.getName() of agent type
     */
    public String toString()
    {
            return Reporter.ADD_REPORT_PROPERTY(agent, agentId) ;
    }

    
    public void reportInfectedStatus()
    {
        Site[] sites = getSites() ;
        String siteReport = "" ;
        for (Site site : sites)
            siteReport += site.toString() + ":" + site.getInfectedStatus() + " " ;
        LOGGER.log(Level.INFO, "Agent:{0} {1}", new Object[]{getInfectedStatus(),siteReport});
    }
     
}
