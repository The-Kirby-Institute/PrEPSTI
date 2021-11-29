/**
 * 
 */
package PRSP.PrEPSTI.reporter;

import PRSP.PrEPSTI.agent.MSM;
import PRSP.PrEPSTI.community.Community;
import PRSP.PrEPSTI.configloader.ConfigLoader;
//import community.* ;

import java.io.*;

import java.lang.reflect.*;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;    // Redundant 31/08/2020
import java.util.Collections;
import java.util.* ;
import java.util.HashMap ;

import java.util.logging.Level;

//import org.apache.commons.math3.stat.descriptive.moment.Mean ;    // Redundant 31/08/2020

//import org.jfree.chart.* ;

/**
 * @author Michael Walker
 */
public class Reporter {

    private static final String COLUMN_NAME = "_COLUMN_NAME_" ;

    /** Name of simulation. */
    protected String simName ;    
    /** Input report. */ 
    protected ArrayList<String> input ;

    /** Output report. */
    protected ArrayList<String> output ;
    
    /** Reader for accessing saved reports. */
    protected Reader reader ;
    
    static public String YEAR = "year" ;
    
    /**
     * To avoid generating reports more than once. 
     * reportName maps to report.
     */
    static protected HashMap<String,Object> REPORT_LIST = new HashMap<String,Object>() ;

    
    /**
     * Clears REPORT_LIST so that fresh ones can be generated for the next simulation.
     */
    static public void CLEAR_REPORT_LIST()
    {
        REPORT_LIST.clear() ;
    }

    
    /** Whether to automatically save reports */
    public static boolean WRITE_REPORT;
    
    /** Path to folder for saving reports. */

    // old paths
    // public static String REPORT_FOLDER = "C:\\Users\\MichaelWalker\\UNSW\\NSW PRSP - Documents\\Model development\\output\\" ;
    // public static String DATA_FOLDER = "C:\\Users\\MichaelWalker\\OneDrive - UNSW\\gonorrhoeaPrEP\\simulator\\PrEPSTI2\\data_files\\" ;
    
    // loaded paths
    public static String REPORT_FOLDER;
    public static String 
	    ;

    /** Names of properties for filtering records. */
    private ArrayList<String> filterPropertyNames = new ArrayList<String>() ;
    
    /**
     * (String) values of properties for filtering records. An empty
     * String means that the property need only be present
     */
    private ArrayList<String> filterPropertyValues = new ArrayList<String>() ;

    /** The number of Community cycles to pass between reports. */ 
    protected int outputCycle = 1 ;

    /** String representation of 'None'. */
    static String NONE = "None" ;
    /** static String representation of 'true'. */
    static public String TRUE = "true" ;
    /** static String representation of 'false'. */
    static public String FALSE = "false" ;
    /** static String representation of 'clear'. */
    static public String CLEAR = "clear" ;
    /** static String representation of ','. */
    static public String COMMA = "," ;
    /** static String representation of '.csv'. */
    static public String CSV = ".csv" ;
    /** static String representation of 'agentId'. */
    static public String AGENTID = "agentId" ;
    /** static String representation of 'agentId0'. */
    static public String AGENTID0 = "agentId0" ;
    /** static String representation of 'agentId1'. */
    static public String AGENTID1 = "agentId1" ;
    /** static String representation of 'relationshipId'. */
    static public String RELATIONSHIPID = "relationshipId" ;
    /** static String representation of one empty space. */
    static public String SPACE = " " ;
    /** static String representation of one empty String. */
    static public String EMPTY = "" ;
    
    /** Number of days in a year. */
    static public int DAYS_PER_YEAR = 365 ;
    /** Number of days in a month. */
    static final int DAYS_PER_MONTH = 31 ;

    
    /** Logger of Reporter Class. */
    static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger("reporter") ;

    public static final String ADD_REPORT_LABEL(String label)
    {
        return label + ":" ;
    }
    
    public static final String ADD_REPORT_PROPERTY(String label, String value)
    {
    	// StringBuilder sb = new StringBuilder();
        // sb.append(ADD_REPORT_LABEL(label));
        // sb.append(value);
        // sb.append(" ");
        // // String report = ADD_REPORT_LABEL(label) ;
        // return sb.toString() ;
        return ADD_REPORT_LABEL(label) + value + " ";
    }
        
    public static final String ADD_REPORT_PROPERTY(String label)
    {
        return ADD_REPORT_PROPERTY(label,"") ;
    }
        
    public static final String ADD_REPORT_PROPERTY(String label, Object value)
    {
        return ADD_REPORT_PROPERTY(label, String.valueOf(value)) ;
    }
    
    public static final String ADD_REPORT_VALUE(Object value)
    {
        return String.valueOf(value) + " " ;
    }
    
    /**
     * Avoid having to add ":" whenever the index of a property name is needed.
     * Used when startIndex is zero or not given
     * @param property
     * @param record
     * @return indexOf(property + ":")
     */
    public static final int INDEX_OF_PROPERTY(String property, String record)
    {
        return INDEX_OF_PROPERTY(property,0,record) ;
    }
    
    /**
     * Avoid having to add ":" whenever the index of a property name is needed
     * @param property
     * @param startIndex
     * @param report
     * @return indexOf(property + ":")
     */
    public static final int INDEX_OF_PROPERTY(String property, int startIndex, String report)
    {
        property += ":" ;
        return report.indexOf(property,startIndex) ;
    }
    
    /**
     * 
     * @param mapList - either HashMap or ArrayList
     * @param elementNb
     * @return (String) representation of mapList.get(elementNb)
     */
    protected static String PRESENT_ELEMENT(Object mapList, Object elementNb)
    {
        if (mapList instanceof HashMap)
        {
            return GET_ELEMENT((HashMap<?,?>) mapList, elementNb).toString() ;
        }
    return GET_ELEMENT((ArrayList<?>) mapList, (Integer) elementNb).toString() ;
    }

    /**
     * 
     * @param hashmap
     * @param elementNb
     * @return (Object) hashmap.get(elementNb) or (Object) "None" if elementKey not a key 
     */
    private static Object GET_ELEMENT(HashMap<?,?> hashmap, Object elementKey)
    {
            if (hashmap.containsKey(elementKey))
            {
                    return (Object) hashmap.get(elementKey) ;
            }
            String message = "None" ;
            return (Object) message ;
    }

    /**
     * 
     * @param arrayList
     * @param elementNb
     * @return (Object) arrayList[elementNb] or (Object) "None" if not available
     */
    private static Object GET_ELEMENT(ArrayList<?> arrayList, int elementNb)
    {
            if (arrayList.size() > elementNb)
            {
                    return (Object) arrayList.get(elementNb) ;
            }
            String message = NONE ;
            return (Object) message ;
    }
    
    /**
     * Allows division without having to check that the denoominator is non-zero.
     * @param numerator (double)
     * @param denominator (double)
     * @return (double) 0.0 if denominator is zero, numerator/denominator otherwise
     */
    protected static double SAFE_DIVISION(double numerator, double denominator)
    {
    	if (denominator == 0.0)
    		return 0.0 ;
    	else
    		return numerator/denominator ;
    }
    
    /**
     * Filters records leaving only those encounters containing propertyName with (String) value.
     * @param propertyName
     * @param value
     * @param bound
     * @param fullReport
     * @return 
     */
    protected static ArrayList<String> FILTER_REPORT(String propertyName, String value, String bound, ArrayList<String> fullReport)
    {
        ArrayList<String> filteredReport = new ArrayList<String>() ;
        
        String filteredRecord ;
        
        for (String record : fullReport)
        {
            filteredRecord = BOUNDED_STRING_BY_VALUE(propertyName,value,bound,record);
            filteredReport.add(filteredRecord) ;
        }
        
        return filteredReport ;
    }
    
    
    /**
     * Sorts String entries of unsortedReport according to value of propertyName.
     * @param unsortedReport
     * @param sortingReport
     * @return 
     */
    protected static HashMap<Comparable<?>,HashMap<Comparable<?>,Integer>> SORT_REPORT(HashMap<Comparable<?>,Integer> unsortedReport, 
            HashMap<Object,Object> sortingReport)
    {
        HashMap<Comparable<?>,HashMap<Comparable<?>,Integer>> outputHashMap = new HashMap<Comparable<?>,HashMap<Comparable<?>,Integer>>() ;
        //HashMap<Object,?> entryHashMap = new HashMap<Object,Object>() ;
        
        ArrayList<String> unsortedEntries ;
        Object sortingValue ;
                
        for (Comparable<?> unsortedKey : unsortedReport.keySet())
        {
                Comparable<?> sortingKey = (Comparable<?>) sortingReport.get(unsortedKey) ;
                /*if (!entryHashMap.containsKey(sortingKey))
                    entryHashMap.put(sortingValue, ) ;
                entryHashMap.put(sortingValue,entryHashMap.get(sortingValue) + entry) ;
            }
            for (Object entryKey : entryHashMap.keySet())
            {*/
                if (!outputHashMap.containsKey(sortingKey))
                    outputHashMap.put(sortingKey, new HashMap<Comparable<?>,Integer>()) ;
                outputHashMap.get(sortingKey).put(unsortedKey, unsortedReport.get(unsortedKey)) ;
            //}
        }
        return outputHashMap ;
    }
    
    /**
     * Sorts entries of unsortedReport according to sortingReport, only considering
     * the values in (Object[]) values.
     * @param unsortedReport
     * @param sortingReport
     * @param values
     * @return 
     */
    protected static HashMap<Object,HashMap<Comparable<?>,ArrayList<Comparable<?>>>> 
        SORT_RECORD(HashMap<Comparable<?>,ArrayList<Comparable<?>>> unsortedReport, 
            HashMap<Comparable<?>, ArrayList<Comparable<?>>> sortingReport, Object[] values)
    {
        HashMap<Object,HashMap<Comparable<?>,ArrayList<Comparable<?>>>> sortedReport 
                = new HashMap<Object,HashMap<Comparable<?>,ArrayList<Comparable<?>>>>() ;
        for (Object value : values )
        {
            sortedReport.put(value, new HashMap<Comparable<?>,ArrayList<Comparable<?>>>()) ;
            for (Comparable<?> key : unsortedReport.keySet())
            {
                ArrayList<Comparable<?>> arrayList = new ArrayList<Comparable<?>>() ;
                for (Comparable entry : unsortedReport.get(key))
                    if (sortingReport.get(value).contains(entry))
                        arrayList.add(entry) ;
                if (!arrayList.isEmpty())
                    sortedReport.get(value).put(key,arrayList) ;
            }
        }
        return sortedReport ;
    }

    /**
     * Sorts entries of unsortedReport according to sortingReport, only considering
     * the values in (Object[]) values.
     * The nested HashMap in unsortedReport is intended to hold temporal (cycle) data.
     * @param unsortedReport
     * @param sortingReport
     * @param values
     * @return 
     */
    protected static HashMap<Object,HashMap<Comparable<?>,HashMap<Comparable<?>,ArrayList<Comparable<?>>>>> 
        SORT_REPORT(HashMap<Comparable<?>,HashMap<Comparable<?>,ArrayList<Comparable<?>>>> unsortedReport, 
            HashMap<Comparable<?>, ArrayList<Comparable<?>>> sortingReport, Object[] values) 
    {
        HashMap<Object,HashMap<Comparable<?>,HashMap<Comparable<?>,ArrayList<Comparable<?>>>>> sortedReport 
                = new HashMap<Object,HashMap<Comparable<?>,HashMap<Comparable<?>,ArrayList<Comparable<?>>>>>() ;
        
        for (Object value : values )
        {
            sortedReport.put(value, new HashMap<Comparable<?>,HashMap<Comparable<?>,ArrayList<Comparable<?>>>>()) ;
        
            HashMap<Comparable<?>,HashMap<Comparable<?>,ArrayList<Comparable<?>>>> hashMap1 = new HashMap<Comparable<?>,HashMap<Comparable<?>,ArrayList<Comparable<?>>>>() ;
            for (Comparable<?> key1 : unsortedReport.keySet())
            {
                HashMap<Comparable<?>,ArrayList<Comparable<?>>> hashMap2 = new HashMap<Comparable<?>,ArrayList<Comparable<?>>>() ;
                for (Comparable<?> key2 : unsortedReport.get(key1).keySet())
                {
                    if (!sortingReport.get(value).contains(String.valueOf(key2)))
                        continue ;
                    hashMap2.put(key2, unsortedReport.get(key1).get(key2)) ;
                            //updateHashMap(key1,unsortedReport.get(key1).get(key2),hashMap1) ;
                }
                if (!hashMap2.keySet().isEmpty())
                    hashMap1.put(key1, hashMap2) ;
            }     
            if (!hashMap1.keySet().isEmpty())
                sortedReport.put(value, hashMap1) ;
          // logger.log(level.info, "{0}", sortedReport);
        }
        return sortedReport ;
    }
        
    /**
     * Extracts bounded substrings whose propertyName == value
     * @param propertyName 
     * @param value
     * @param bound - String bounding substrings of interest
     * @param string
     * @return String boundedOutput
     */
    public static String BOUNDED_STRING_BY_VALUE(String propertyName, String value, String bound, String string)
    {
    	StringBuilder sbBoundedOutput = new StringBuilder() ;    // "" ;
        String boundedString ;
        for (int indexStart = Reporter.INDEX_OF_PROPERTY(bound,string) ; indexStart >= 0 ; indexStart = INDEX_OF_PROPERTY(bound,indexStart+1,string))
        {
            boundedString = EXTRACT_BOUNDED_STRING(bound, string, indexStart) ;
            // This if statement moved to COMPARE_VALUE()
            //if (INDEX_OF_PROPERTY(propertyName,boundedString) >= 0)
            
            // TODO: Label Sites site0, site1 in generation of encounterString so that boolean || is not necessary
            if (COMPARE_VALUE(propertyName,value,boundedString) || Reporter.COMPARE_VALUE(propertyName,value,boundedString,boundedString.lastIndexOf(propertyName))) 
                sbBoundedOutput.append(boundedString) ;
        }
        return sbBoundedOutput.toString() ;
    }
    
    /**
     * 
     * @param propertyName
     * @param values
     * @param bound
     * @param report
     * @return HashMap key:values, entries: ArrayList of values of bound
     */
    protected static HashMap<Comparable<?>,ArrayList<Comparable<?>>> SORT_BOUNDED_STRING_ARRAY(String propertyName, String[] values, String bound, ArrayList<String> report)
    {
        HashMap<Comparable<?>,ArrayList<Comparable<?>>> sortedHashMap = new HashMap<Comparable<?>,ArrayList<Comparable<?>>>() ;
        int indexStart ;
        String boundedString ;
        Object key ;
        String boundValue ;
        
        // Initialise output HashMap
        for (Comparable<?> value : values)
            sortedHashMap.put(value,new ArrayList<Comparable<?>>()) ;
        sortedHashMap.put(NONE,new ArrayList<Comparable<?>>()) ;

        for (String record : report)
        {
            key = "" ;
            String checkRecord = BOUNDED_STRING_BY_CONTENTS(propertyName,bound,record) ;
            if (checkRecord.isEmpty())
            {
                LOGGER.warning(propertyName + " " + bound + " checkRecord is empty " + record);
                continue ;
            }
            indexStart = Reporter.INDEX_OF_PROPERTY(bound,checkRecord);
            while (indexStart >= 0)
            {
                boundedString = EXTRACT_BOUNDED_STRING(bound, checkRecord, indexStart) ;
                key = EXTRACT_VALUE(propertyName,boundedString) ;
                if (String.valueOf(key).isEmpty())
                    break ;
                else
                {
                    boundValue = EXTRACT_VALUE(bound,boundedString) ;
                    sortedHashMap = UPDATE_HASHMAP((Comparable) key,(Comparable) boundValue,sortedHashMap) ;
                }
                indexStart = INDEX_OF_PROPERTY(bound,indexStart+1,checkRecord);
            }
            //LOGGER.log(Level.INFO, "key:{0}", new Object[]{key});
            //LOGGER.log(Level.INFO, "{0}", sortedHashMap);
        }
        return sortedHashMap ;
    }
    
    /**
     * Extracts bounded substrings containing propertyName as substring
     * @param propertyName 
     * @param bound - String bounding substrings of interest
     * @param string
     * @return String boundedOutput
     */
    protected static String BOUNDED_STRING_BY_CONTENTS(String propertyName, String bound, String string)
    {
    	StringBuilder sbBoundedOutput = new StringBuilder() ; // = "" ;
        String boundedString ;
        for (int indexStart = Reporter.INDEX_OF_PROPERTY(bound,string) ; indexStart >= 0 ; indexStart = INDEX_OF_PROPERTY(bound,indexStart+1,string) )
        {
            boundedString = EXTRACT_BOUNDED_STRING(bound, string, indexStart) ;
            if (boundedString.contains(propertyName))   //(COMPARE_VALUE(propertyName,value,boundedString)) 
                sbBoundedOutput.append(boundedString) ;
        }
        return sbBoundedOutput.toString() ;
    }

    /**
     * Extracts bounded substrings containing propertyName as substring with value
     * in @param values.
     * @param propertyName 
     * @param values 
     * @param bound - String bounding substrings of interest
     * @param string
     * @return String boundedOutput
     */
    protected static String BOUNDED_STRING_FROM_ARRAY(String propertyName, ArrayList<?> values, String bound, String string)
    {
        StringBuilder sbBoundedOutput = new StringBuilder() ;    // "" ;
        String boundedString ;
        String value ;
        for (int indexStart = Reporter.INDEX_OF_PROPERTY(bound,string) ; indexStart >= 0 ; indexStart = INDEX_OF_PROPERTY(bound,indexStart+1,string) )
        {
            boundedString = EXTRACT_BOUNDED_STRING(bound, string, indexStart) ;
            value = EXTRACT_VALUE(propertyName, boundedString) ;
            if (values.contains(value))   //(COMPARE_VALUE(propertyName,value,boundedString)) 
                sbBoundedOutput.append(boundedString) ;
        }
        return sbBoundedOutput.toString() ;
    }

    /**
     * 
     * @param string
     * @param bound
     * @return (ArrayList(String)) of bounded substrings of string.
     */
    public static ArrayList<String> EXTRACT_ARRAYLIST(String string, String bound)
    {
        return EXTRACT_ARRAYLIST(string, bound, "") ;
    }
    
        /**
     * 
     * @param string
     * @param bound
     * @param flag
     * @return (ArrayList(String)) of bounded substrings of string containing 
     * flag as a substring.
     */
    protected static ArrayList<String> EXTRACT_ARRAYLIST(String string, String bound, String flag)
    {
        ArrayList<String> outputArray = new ArrayList<String>() ;
        String extractedString ;
        
        // Require bounded strings contain flag.
        if (!flag.isEmpty())
            string = BOUNDED_STRING_BY_CONTENTS(flag,bound,string) ;
        
        // Extract individual bounded strings.
        for (int indexStart = Reporter.INDEX_OF_PROPERTY(bound, string) ; indexStart >= 0 ;  indexStart = INDEX_OF_PROPERTY(bound, indexStart+1, string))
        {
            extractedString = EXTRACT_BOUNDED_STRING(bound, string, indexStart);
            if (!extractedString.isEmpty()) 
                outputArray.add(extractedString) ;
        }
        return outputArray ;
    }

    /**
     * 
     * @param bound - subString bounding subStrings of interest
     * @param string - String to parse
     * @param indexStart - index in string of first bound
     * @return subString of string bounded by bound
     */
    public static String EXTRACT_BOUNDED_STRING(String bound, String string, int indexStart)
    {
        int index0 = INDEX_OF_PROPERTY(bound, indexStart, string) ;
        if (index0 == -1)
            return "" ;
        int index1 = INDEX_OF_PROPERTY(bound,index0+1,string) ;
        if (index1 == -1) index1 = string.length() ;
        return string.substring(index0, index1) ;

    }
    
    /**
     * Split a record string by a specific property and save to a hashmap
     * where the key is the property extracted from each sub-record
     * @param property
     * @param record
     * @return
     */
    public static HashMap<String, String> SPLIT_RECORD_BY_PROPERTY(String property, String record) 
    {
        return SPLIT_RECORD_BY_PROPERTY(property, record, new HashSet<String>());
    }



    /**
     * Split a record string by a specific property and save to a hashmap
     * where the key is the property extracted from each sub-record
     * @param property
     * @param record
     * @param keys: only add keys that exist in this set, if it is empty, add all keys
     * @return
     */
    public static HashMap<String, String> SPLIT_RECORD_BY_PROPERTY(String property, String record, HashSet<String> keys) 
    {
        HashMap<String, String> splitRecord = new HashMap<String,String>();

        int previousIndex = 0;
        int count  = 0;

        // Identify where property exists in the string one by one
        for (int i = -1; (i = record.indexOf(property, i + 1)) != -1; ++i) 
        {
            if (count > 0) 
            {
                String recordString = record.substring(previousIndex, i).trim();
                String key = EXTRACT_VALUE(property, recordString);
                if (keys.size() == 0 || keys.contains(key)) 
                	splitRecord.put(key, recordString) ;
                if (null == recordString)
                	LOGGER.info(key) ;
            }
            previousIndex = i;
            count++;
        }

        // ADD the last record
        String recordString = record.substring(previousIndex, record.length()).trim();
        String key = EXTRACT_VALUE(property, recordString);
        if (null == recordString)
        	LOGGER.info(key) ;
        if (keys.size() == 0 || keys.contains(key) && key.length() > 0) 
        	splitRecord.put(key, recordString);

        return splitRecord;
    }

    /**
     * When position within string is not known, call EXTRACT_VALUE(startIndex = 0)
     * @param propertyName - name of variable whose value is wanted
     * @param string
     * @return (String) value of propertyName
     */
    public static String EXTRACT_VALUE(String propertyName, String string)
    {
        return Reporter.EXTRACT_VALUE(propertyName, string, 0) ;
    }

    /**
     * 
     * @param propertyName
     * @param string
     * @return ArrayList of (String) values of propertyName from String string
     */
    public static ArrayList<Comparable<?>> EXTRACT_ALL_VALUES(String propertyName, String string)
    {
        return Reporter.EXTRACT_ALL_VALUES(propertyName,string,0) ;
    }
    
    /**
     * 
     * @param propertyName
     * @param string
     * @param startIndex
     * @return ArrayList of (String) values of propertyName from String string
     */
    public static ArrayList<Comparable<?>> EXTRACT_ALL_VALUES(String propertyName, String string, int startIndex)
    {
        ArrayList<Comparable<?>> values = new ArrayList<Comparable<?>>() ;
        int index = INDEX_OF_PROPERTY(propertyName,startIndex,string) ; 
        
        while ( index >= 0 )
        {
            values.add(Reporter.EXTRACT_VALUE(propertyName, string, index)) ;
            index = INDEX_OF_PROPERTY(propertyName, index+1, string) ;
        }
        return values ;
    }
    
    /**
     * The space character indicates the end of the value.  
     * @param propertyName - property whose value is wanted
     * @param string - information source/report
     * @param startIndex - string index of value, assumed exact if index greater 
     * than 0, otherwise search
     * @return (String) value of valueName as stored in string
     */
    public static String EXTRACT_VALUE(String propertyName, String string, int startIndex)
    {
        // Find value of valueName in string
        startIndex = INDEX_OF_PROPERTY(propertyName, startIndex, string) ;
        if (startIndex < 0)
            return "" ;
        startIndex += propertyName.length() + 1 ;    // +1 is for ":" following propertyName
        int valueEndIndex = string.indexOf(SPACE, startIndex) ;
        if (valueEndIndex < 0)
            valueEndIndex = string.length() ;
        return string.substring(startIndex, valueEndIndex) ;
    }

    /**
     * 
     * @param propertyName
     * @param string
     * @param startIndex
     * @return (String) value of propertyName in string if it is the next property,
     * otherwise return "None"
     */
    protected static String EXTRACT_VALUE_IF_NEXT(String propertyName, String string, int startIndex)
    {
        startIndex = IS_PROPERTY_NAME_NEXT(propertyName, string, startIndex) ;
        if (startIndex > 0)
            return Reporter.EXTRACT_VALUE(propertyName, string, startIndex) ;
        return NONE ;
    }

    /**
     * 
     * @param record
     * @return String[] pairs of agentIds corresponding to relationships described in record
     */
    protected String[] EXTRACT_AGENTIDS(String record)
    {
        return Reporter.this.EXTRACT_AGENTIDS(record, 0) ;
    }
    
    /**
     * 
     * @param record
     * @param startIndex
     * @return String[] pairs of agentIds corresponding to relationships described in record
     */
    protected String[] EXTRACT_AGENTIDS(String record, int startIndex)
    {
            String agentId0 = Reporter.EXTRACT_VALUE("agentId0", record, startIndex) ;

            startIndex = INDEX_OF_PROPERTY("agentId1", startIndex, record) ;
            String agentId1 = Reporter.EXTRACT_VALUE("agentId1", record, startIndex) ;
            return new String[] {agentId0,agentId1} ;
    }
    
    /**
     * 
     * @param propertyName
     * @param string
     * @param startIndex
     * @return index of propertyName in string if propertyName is next property in string, -1 otherwise
     */
    protected static int IS_PROPERTY_NAME_NEXT(String propertyName, String string, int startIndex)
    {
        int colonIndex = string.indexOf(":",startIndex) ;
        int propertyIndex = INDEX_OF_PROPERTY(propertyName, startIndex, string) ;
        
        // If propertyName names the first property after position startIndex
        if ((propertyIndex < colonIndex) && (propertyIndex > 1))
            return propertyIndex ;
        return -1 ;
    }
    
    /**
     * Compares the String representation of the value of propertyName to @param value
     * @param propertyName
     * @param value
     * @param string
     * @param startIndex
     * @return true if the String representation of the value of propertyName equals (String) value
     */
    protected static boolean COMPARE_VALUE(String propertyName, String value, String string, int startIndex)
    {
        if (INDEX_OF_PROPERTY(propertyName,startIndex,string) >= 0)
            return Reporter.EXTRACT_VALUE(propertyName, string, startIndex).equals(value) ;
        return false ;
    }
    
    /**
     * Finds the number of times propertyName occurs in string and the number of 
     * times it has the value value.toString().
     * @param propertyName
     * @param value
     * @param string
     * @param startIndex
     * @return (int[2]) The number of 'propertyName has value' incidents, number of propertyName incidents.
     */
    protected static int[] COUNT_VALUE_INCIDENCE(String propertyName, String value, String string, int startIndex)
    {
        int count = 0 ;
        int total = 0 ;
        for (int index = INDEX_OF_PROPERTY(propertyName, startIndex, string) ; index >= 0 ; 
            index = INDEX_OF_PROPERTY(propertyName, index+1, string))
        {
            total++ ;
            if (Reporter.COMPARE_VALUE(propertyName, value, string, index))
                count++ ;
        }
        if (total == 0)
            return new int[] {0,0} ;
        return new int[] {count,total} ;
    }
    
    /**
     * Compares the String representation of the value of propertyName to @param value
     * @param propertyName
     * @param value
     * @param string
     * @return true if the String representation of the value of propertyName equals (String) value
     */
    protected static boolean COMPARE_VALUE(String propertyName, String value, String string)
    {
        return Reporter.COMPARE_VALUE(propertyName, value, string, Reporter.INDEX_OF_PROPERTY(propertyName,string)) ;
    }
    
    
    /*protected static HashMap<String,ArrayList<String>> updateStringHashMap(String keyString, String entryString, HashMap<String,ArrayList<String>> valueMap)
    {
        
    }*/
    
    /**
     * Puts entries into HashMap whose keys are the agentIds
     * and values are arrays of their partners Ids. 
     * Creates key and associated int[] if necessary.
     * 
     * @param key - (String) usually agentId but need not be.
     * @param entry - String to convert and go into int[] at key. 
     * @param valueMap - Adding entry and sometimes key to this HashMap
     * @return partnerMap - HashMap indicating partnerIds of each agent (key: agentId)
     */
    /*protected static HashMap<Integer,ArrayList<Integer>> UPDATE_HASHMAP(String keyString, String entryString, HashMap<Integer,ArrayList<Integer>> valueMap)
    {
        int key = Integer.valueOf(keyString) ;
        int boundValue = Integer.valueOf(entryString) ;
        return UPDATE_HASHMAP(key, boundValue, valueMap) ;
    }*/
    
    /**
     * Puts entries into HashMap whose keys are the agentIds
     * and values are arrays of their partners Ids. 
     * Creates key and associated Object[] if necessary.
     * 
     * @param key - usually agentId but need not be.
     * @param entry - Object to go into Object[] at key. 
     * @param valueMap - Adding boundValue and sometimes key to this HashMap
     * @return partnerMap - HashMap indicating partnerIds of each agent (key: agentId)
     */
    public static HashMap<Comparable<?>,ArrayList<Comparable<?>>> UPDATE_HASHMAP(Comparable key, Comparable entry, HashMap<Comparable<?>,ArrayList<Comparable<?>>> valueMap)
    {
        return Reporter.UPDATE_HASHMAP(key, entry, valueMap, true) ;
    }
		
    /**
     * Puts entries into HashMap whose keys are the agentIds
     * and values are arrays of their partners Ids. 
     * Creates key and associated int[] if necessary.
     * @param key - usually agentId but need not be.
     * @param entry - int to go into int[] at key. 
     * @param valueMap - Adding boundValue and sometimes key to this HashMap
     * @param allowDuplicates
     * @return partnerMap - HashMap indicating partnerIds of each agent (key: agentId)
     */
    protected static HashMap<Comparable<?>,ArrayList<Comparable<?>>> UPDATE_HASHMAP(Comparable key, Comparable entry, HashMap<Comparable<?>,ArrayList<Comparable<?>>> valueMap, boolean allowDuplicates)
    {
        //HashMap<Integer,ArrayList<Integer>> partnerMap = new HashMap<Integer,ArrayList<Integer>>() ;
        
        ArrayList<Comparable<?>> entryArray ;
        if (valueMap.containsKey(key))
        {
            entryArray = valueMap.get(key) ;
        }
        else
        {
            entryArray = new ArrayList<Comparable<?>>() ;
        }
        if (allowDuplicates || !entryArray.contains(entry))
        {
            entryArray.add(entry) ;
            valueMap.put(key, entryArray) ;
        }

        return valueMap ;
    }
    
    /**
     * Adds (Integer) entry to value of (Object) key in valueMap.
     * @param key
     * @param entry
     * @param valueMap
     * @return 
     */
    protected static HashMap<Object,Integer> UPDATE_HASHMAP(Object key, Integer entry, HashMap<Object,Integer> valueMap)
    {
        Integer listEntry ;
        
        if (valueMap.containsKey(key))
            listEntry = valueMap.get(key) ;
        else
            listEntry = 0 ;
            
        valueMap.put(key, listEntry + entry) ;

        return valueMap ;
    }
		
    /**
     * Increments entries into HashMap whose keys are the agentIds
     * Creates key and associated int[] if necessary.
     * @param key - usually agentId but need not be.
     * @param valueMap - Adding boundValue and sometimes key to this HashMap
     * @return partnerMap - HashMap indicating partnerIds of each agent (key: agentId)
     */
    protected static HashMap<Comparable<?>,Number> INCREMENT_HASHMAP(Comparable key, HashMap<Comparable<?>,Number> valueMap)
    {
        //HashMap<Integer,ArrayList<Integer>> partnerMap = new HashMap<Integer,ArrayList<Integer>>() ;
        
        if (valueMap.containsKey(key))
            valueMap.put(key, valueMap.get(key).intValue() + 1) ;
        else
            valueMap.put(key, 1) ;
        
        return valueMap ;
    }
	
    /**
     * Puts entries in HashMap<Integer,HashMap<Integer,ArrayList<Integer>>> after 
     * converting input Strings to Integer.
     * @param keyString
     * @param entryString
     * @param cycle
     * @param valueMap
     * @return 
     */
    /*protected static HashMap<Integer,HashMap<Integer,ArrayList<Integer>>> UPDATE_HASHMAP(String keyString, String entryString, 
            int cycle, HashMap<Integer,HashMap<Integer,ArrayList<Integer>>> valueMap)
    {
        int key = Integer.valueOf(keyString) ;
        int boundValue = Integer.valueOf(entryString) ;
        return UPDATE_HASHMAP(key, boundValue, cycle, valueMap) ;
    }*/
    
    /**
     * Puts entries in HashMap(Integer,HashMap(Integer,ArrayList(Integer))), 
     * creating keys in either HashMap when necessary and simply updating otherwise.
     * @param key
     * @param key2
     * @param cycle
     * @param valueMap
     * @return 
     */
    protected static HashMap<Comparable<?>,HashMap<Comparable<?>,ArrayList<Comparable<?>>>> UPDATE_HASHMAP(Object key, 
            Object key2, int cycle, HashMap<Comparable<?>,HashMap<Comparable<?>,ArrayList<Comparable<?>>>> valueMap)
    {
        //HashMap<Integer,ArrayList<Integer>> partnerMap = new HashMap<Integer,ArrayList<Integer>>() ;
        
        HashMap<Comparable<?>,ArrayList<Comparable<?>>> entryHashMap ;
        if (valueMap.containsKey(key))
        {
            entryHashMap = valueMap.get(key) ;
        }
        else
        {
            entryHashMap = new HashMap<Comparable<?>,ArrayList<Comparable<?>>>() ;
            //entryArray.add(key2) ;
        }
        valueMap.put((Comparable) key, UPDATE_HASHMAP((Comparable<?>) key2, (Comparable<?>) cycle, entryHashMap)) ;

        return valueMap ;
    }
    
    /**
     * Converts HashMap(Object,ArrayList(Object)) to HashMap(String,ArrayList(String)) .
     * @param objectHashMap
     * @return 
     */
    static protected HashMap<String,ArrayList<String>> HASHMAP_STRING(HashMap<Object,ArrayList<Object>> objectHashMap)
    {
        HashMap<String,ArrayList<String>> stringHashMap = new HashMap<String,ArrayList<String>>() ;
        
        ArrayList<Object> entryObject ;
        ArrayList<String> entryString = new ArrayList<String>() ;
        
        for (Object key : objectHashMap.keySet())
        {
            entryObject = objectHashMap.get(key) ;
            for (Object entry : entryObject)
                entryString.add((String) entry) ;
            stringHashMap.put((String) key, entryString ) ;
        }
        return stringHashMap ;
    }
		
    /**
     * Converts HashMap(Object,ArrayList(Object)) to HashMap(Number,ArrayList(Number))
     * @param objectHashMap
     * @return 
     */
    static protected HashMap<Number,ArrayList<Number>> HASHMAP_NUMBER(HashMap<Object,ArrayList<Object>> objectHashMap)
    {
        HashMap<Number,ArrayList<Number>> numberHashMap = new HashMap<Number,ArrayList<Number>>() ;
        
        ArrayList<Object> entryObject ;
        ArrayList<Number> entryNumber = new ArrayList<Number>() ;
        
        for (Object key : objectHashMap.keySet())
        {
            entryObject = objectHashMap.get(key) ;
            for (Object entry : entryObject)
                entryNumber.add((Number) entry) ;
            numberHashMap.put((Number) key, entryNumber ) ;
        }
        return numberHashMap ;
    }
		
    /**
     * Given objectHashMap maps key to ((HashMap) subKey maps to object), finds 
     * number of subKeys for each key.
     * @param objectHashMap
     * @return (HashMap) showing number of keys in each subHashMap.
     */
    static protected HashMap<Object,Number> COUNT_SUB_KEYS(HashMap<Object,HashMap<Object,ArrayList<Object>>> objectHashMap)
    {
        HashMap<Object,Number> outputHashMap = new HashMap<Object,Number>() ;
        
        for (Object key : objectHashMap.keySet() )  // numberRecentRelationshipsReport.keySet())
            outputHashMap.put(key, objectHashMap.get(key).keySet().size()) ;
    
        return outputHashMap ;
    }
    
    /**
     * Converts HashMap(Object,HashMap(Object,ArrayList(Object)))
     * to HashMap(Number,HashMap(Number,ArrayList(Number)))
     * @param objectHashMapHashMap
     * @return 
     */
    static protected HashMap<Number,HashMap<Number,ArrayList<Number>>> HASHMAP_HASHMAP_NUMBER(HashMap<Object,HashMap<Object,ArrayList<Object>>> objectHashMapHashMap )
    {
        HashMap<Number,HashMap<Number,ArrayList<Number>>> numberHashMapHashMap = new HashMap<Number,HashMap<Number,ArrayList<Number>>>() ;
        
        HashMap<Number,ArrayList<Number>> numberHashMap = new HashMap<Number,ArrayList<Number>>() ;
        
        for (Object key : objectHashMapHashMap.keySet())
        {
            numberHashMap = HASHMAP_NUMBER(objectHashMapHashMap.get(key)) ;
            numberHashMapHashMap.put((Number) key, numberHashMap) ;
        }
        return numberHashMapHashMap ;
    }
    
    /**
     * Restructures paramHashMap so that most-nested values become keys.
     * Values are HashTable of ArrayList of nested keys.
     * key1 maps to key2 maps to arrayValue becomes arrayValue maps to key1 maps to key2 .
     * @param paramHashMap
     * @return HashTable
     */
    static public HashMap<Comparable<?>,HashMap<Comparable<?>,ArrayList<Comparable<?>>>> 
        INVERT_HASHMAP_HASHMAP(HashMap<Comparable<?>,HashMap<Comparable<?>,ArrayList<Comparable<?>>>> paramHashMap)
    {
        //LOGGER.info("INVERT_HASHMAP_HASHMAP()") ;
        HashMap<Comparable<?>,HashMap<Comparable<?>,ArrayList<Comparable<?>>>> invertedHashMap = new HashMap<Comparable<?>,HashMap<Comparable<?>,ArrayList<Comparable<?>>>>() ;
        HashMap<Comparable<?>,ArrayList<Comparable<?>>> cycleHashMap ;
        
        for( Comparable key1 : paramHashMap.keySet() )
        {
            for (Comparable key2 : paramHashMap.get(key1).keySet())
            {
            //LOGGER.info(paramHashMap.get(key1).get(key2).toString());
                for (Comparable cycle : paramHashMap.get(key1).get(key2))
                {
            //LOGGER.info(cycle.toString());
                    if (!invertedHashMap.keySet().contains(cycle))
                        cycleHashMap = new HashMap<Comparable<?>,ArrayList<Comparable<?>>>() ;
                    else 
                        cycleHashMap = invertedHashMap.get(cycle) ;
                    invertedHashMap.put(cycle, UPDATE_HASHMAP(key1,key2,cycleHashMap)) ;
                }
            }
        }
        return invertedHashMap ;
    }
        
    /**
     * Convert ArrayList of HashMaps to form usable by multiPlotCycle().
     * @param initialList
     * @param initialKeys
     * @return (HashMap) of Number[] 
     */
    static public ArrayList<ArrayList<Object>> 
        INVERT_ARRAY_HASHMAP(ArrayList<HashMap<Object,Number>> initialList, Object[] initialKeys)
    {
        ArrayList<ArrayList<Object>> invertedList = new ArrayList<ArrayList<Object>>() ;
        
        int arraySize = initialKeys.length ;
        
        for (HashMap<Object,Number> record : initialList)
        {
            ArrayList<Number> innerList = new ArrayList<Number>() ;
            
            // Substitute values into invertedMap        
            for (int index = 0 ; index < arraySize ; index++ )
            {
                Object key = initialKeys[index] ;
                if (record.containsKey(key))
                    innerList.add(record.get(key)) ;
                else
                    innerList.add(0) ;
            }
            
            invertedList.add((ArrayList<Object>) innerList.clone()) ;
        }
        
        return invertedList ;
    }
    
    /**
     * Convert HashMap of HashMaps to form usable by plotHashMap().
     * @param initialMap
     * @param initialKeys
     * @return (HashMap) of Number[] 
     */
    static public HashMap<Comparable<?>,Number[]> 
        INVERT_HASHMAP_ARRAY(HashMap<Object,HashMap<Comparable,Number>> initialMap, Object[] initialKeys)
    {
        HashMap<Comparable<?>,Number[]> invertedMap = new HashMap<Comparable<?>,Number[]>() ;
        
        HashMap<Object,Number> innerMap ;
        
        ArrayList<Number> invertList ;
        
        int arraySize = initialKeys.length ;
        
        // Set up invertedMap to be able to hold values
        for (Object key : initialKeys)
            for (Comparable innerKey : initialMap.get(key).keySet()) 
                if (!invertedMap.containsKey(innerKey))
                    invertedMap.put(innerKey, new Number[initialKeys.length]) ;
            
        // Substitute values into invertedMap        
        for (int index = 0 ; index < arraySize ; index++ )
            for (Object innerKey : invertedMap.keySet())
            {
                Object key = initialKeys[index] ;
                if (initialMap.get(key).containsKey(innerKey))
                    invertedMap.get(innerKey)[index] = initialMap.get(key).get(innerKey) ;
                else
                    invertedMap.get(innerKey)[index] = 0 ;
            }
        
        return invertedMap ;
    }
    
    /**
     * Convert HashMap of HashMaps to form usable by plotSpline().
     * @param initialMap
     * @param initialKeys
     * @return (HashMap) whose inner key maps to (Number[]) values in order determined by 
     * looping through initialKeys 
     */
    static public HashMap<Comparable<?>,Number[]> 
        INVERT_HASHMAP_LIST(HashMap<Comparable<?>,HashMap<Comparable<?>,Number>> initialMap, Object[] initialKeys)
    {
        HashMap<Comparable<?>,Number[]> invertedMap = new HashMap<Comparable<?>,Number[]>() ;
        
        int arraySize = initialKeys.length ;
        
        // Set up invertedMap to be able to hold values
        for (Object key : initialKeys)
            for (Comparable innerKey : initialMap.get(key).keySet()) 
                if (!invertedMap.containsKey(innerKey))
                    invertedMap.put(innerKey, new Number[arraySize]) ;
            
        // Substitute values into invertedMap        
        for (int index = 0 ; index < arraySize ; index++ )
            for (Object innerKey : invertedMap.keySet())
            {
                Object key = initialKeys[index] ;
                if (initialMap.containsKey(key) && initialMap.get(key).containsKey(innerKey))
                    invertedMap.get(innerKey)[index] = initialMap.get(key).get(innerKey) ;
                else
                    invertedMap.get(innerKey)[index] = 0 ;
            }
        
        return invertedMap ;
    }
    
    /**
     * Sorts hashMap entries according to SORT_BOUNDED_STRING_ARRAY, only including values in
 (Object[]) values.
     * @param hashMap
     * @param sortedHashMap
     * @param values
     * @return 
     */
    static protected HashMap<Object,HashMap<Object,ArrayList<Object>>> SORT_HASHMAP(HashMap<Object,ArrayList<Object>> hashMap, 
            HashMap<Object,ArrayList<Object>> sortedHashMap, Object[] values )
    {
        // Output HashMap
        HashMap<Object,HashMap<Object,ArrayList<Object>>> outputHashMap 
                = new HashMap<Object,HashMap<Object,ArrayList<Object>>>() ;
        
        // Sorted HashMap entries
        HashMap<Object,ArrayList<Object>> newEntry = new HashMap<Object,ArrayList<Object>>() ;
        for (Object value : values)
            newEntry.put(value, new ArrayList<Object>()) ;
        
        // Sorting loop.
        for (Object value : values)
        {
            for (Object key : hashMap.keySet())
                for (Object entry : hashMap.get(key))
                    if (sortedHashMap.get(value).contains(entry))
                        newEntry.get(key).add(entry) ;
            outputHashMap.put(value,newEntry) ;
        }
        return outputHashMap ;
    }
    
    /**
     * Invoked to sort HashMap entries when all categories are of interest.
     * @param hashMap
     * @param sortedHashMap
     * @return 
     */
    static protected HashMap<Object,HashMap<Object,ArrayList<Object>>> SORT_HASHMAP(HashMap<Object,ArrayList<Object>> hashMap, 
            HashMap<Object,ArrayList<Object>> sortedHashMap)
    {
        Object[] values = sortedHashMap.keySet().toArray() ;
        return SORT_HASHMAP(hashMap, sortedHashMap, values ) ;
    }
    
    /**
     * For sorting HashMaps when only one value is required
     * @param hashMap
     * @param sortedHashMap
     * @param value
     * @return 
     */
    static protected HashMap<Object,ArrayList<Object>> SORT_HASHMAP(HashMap<Object,ArrayList<Object>> hashMap, 
            HashMap<Object,ArrayList<Object>> sortedHashMap, Object value )
    {
        return SORT_HASHMAP(hashMap, sortedHashMap, new Object[] {value} ).get(value) ;
    }
    
    /**
     * Invoked to sort hashMap.keySet() according to values.
     * @param hashMap
     * @param sortingHashMap
     * @param values
     * @return 
     */
    static protected HashMap<Object,HashMap<Object,Object>> SORT_HASHMAP_KEYS(HashMap<Object,Object> hashMap, 
            HashMap<Object,ArrayList<Object>> sortingHashMap, Object[] values)
    {
        HashMap<Object,HashMap<Object,Object>> outputHashMap = new HashMap<Object,HashMap<Object,Object>>() ;
        
        HashMap<Object,Object> keyHashMap = new HashMap<Object,Object>() ;
        for (Object value : values)
        {
            for (Object key : hashMap.keySet())
                if (sortingHashMap.get(value).contains(key))
                    keyHashMap.put(key, hashMap.get(key)) ;
            outputHashMap.put(value, keyHashMap) ;
        }
        return outputHashMap ;
    }

    /**
     * converts a space separated string with colon separated key:value pairs into a hashmap
     * @param string
     * @return
     */
    static public HashMap<String, String> STRING_TO_HASHMAP(String string) 
    {
        HashMap<String, String> toReturn = new HashMap<String, String>();

        String[] stringSplit = string.split(" ");

        for (String s : stringSplit) {
            s = s.trim();
            String[] sSplit = s.split(":");
            if (sSplit.length > 1) toReturn.put(sSplit[0], sSplit[1]);
        }

        return toReturn;
    }

    /**
     * converts a hashmap into a string with space-separated key:value pairs
     * @param report
     * @param properties
     * @return
     */
    static public String HASHMAP_TO_STRING(HashMap<String, String> report, String[] properties) 
    {
        float t0 = System.nanoTime();
        String toReturn = "";
        Set<String> keySet = report.keySet();
        for (String property : properties) 
        {
            if (keySet.contains(property)) 
            {
                toReturn += property + ":" + report.get(property) + " ";
            }
        }
        float t1 = System.nanoTime();
        Community.RECORD_METHOD_TIME("Reporter.HASHMAP_TO_STRING", t1 - t0);
        return toReturn.trim();
    }
    
    /**
     * Used to go back from end of Report by a specified amount. Checks that this 
     * does not go past the beginning of the report to cause an error.
     * @param backYears
     * @param backMonths
     * @param backDays
     * @param maxCycles
     * @return (int) The number of cycles specified by backYears, backMonths, backDays
     * or maxCycles, whichever is smaller. 
     */
    static protected int GET_BACK_CYCLES(int backYears, int backMonths, int backDays, int maxCycles)
    {
        float t0 = System.nanoTime();
        int backCycles ;
        
        // Don't go further back than records allow.
        if ((backYears * DAYS_PER_YEAR) > maxCycles)
        {
            backYears = maxCycles/DAYS_PER_YEAR ;
            LOGGER.warning("Tried to go back more years than records allow.") ;
        }
        backCycles = backYears * DAYS_PER_YEAR ;
        
        if ((backMonths * DAYS_PER_MONTH + backCycles) > maxCycles)
        {
            backMonths = ((maxCycles - backCycles)/DAYS_PER_MONTH) ;
            LOGGER.warning("Tried to go back more months than records allow.") ;
        }
        backCycles += backMonths * DAYS_PER_MONTH + backDays ;
        
        if (backCycles > maxCycles)
        {
            backCycles = maxCycles ;
            LOGGER.warning("Tried to go back more days than records allow.") ;
        }

        float t1 = System.nanoTime();
        Community.RECORD_METHOD_TIME("Reporter.GET_BACK_CYCLES(y,m,d,maxCycles)", t1 - t0);
        
        return backCycles ;
    }
    
    /**
     * Extracts keys, usually agentIds, of HashMap in each record.
     * @param cycles
     * @param report
     * @return HashMap showing the cycles containing each key 
     */
    static protected HashMap<Comparable<?>,ArrayList<Comparable<?>>> FIND_AGENTID_KEYS(Integer[] cycles, ArrayList<HashMap<Comparable,?>> report)
    {
        HashMap<Comparable<?>,ArrayList<Comparable<?>>> agentIdKeys = new HashMap<Comparable<?>,ArrayList<Comparable<?>>>() ;

        for (int index : cycles)
            for ( Comparable agentId : report.get(index).keySet() )
                agentIdKeys = UPDATE_HASHMAP(index,agentId,agentIdKeys) ;
        return agentIdKeys ;
    }

    /**
     * Extracts values from ArrayList value, usually agentIds, of HashMap in each record.
     * @param cycles
     * @param report
     * @return HashMap showing the cycles containing each value in ArrayList value of HashMap 
     */
    static protected HashMap<Comparable<?>,ArrayList<Comparable<?>>> FIND_AGENTID_VALUES(Integer[] cycles, ArrayList<HashMap<Comparable,ArrayList<Comparable>>> report)
    {
        HashMap<Comparable<?>,ArrayList<Comparable<?>>> agentIdValues = new HashMap<Comparable<?>,ArrayList<Comparable<?>>>() ;

        for (int index : cycles)
        {
            HashMap<Comparable,ArrayList<Comparable>> cycleHashMap = report.get(index) ;
            for ( Comparable key : cycleHashMap.keySet() )
                for ( Comparable agentId : cycleHashMap.get(key))
                    agentIdValues = UPDATE_HASHMAP((Comparable) index,agentId,agentIdValues) ;
        }
        return agentIdValues ;
    }
    
    /**
     * 
     * @param propertyName
     * @param reportList
     * @return (ArrayList) report with (ArrayList) subreports where the values 
     * in the subreports are averaged over the innermost ArrayList.
     */
    static public ArrayList<ArrayList<Object>> 
        PREPARE_MEAN_REPORT(String propertyName, ArrayList<ArrayList<ArrayList<Object>>> reportList)
    {
        // Find mean of reports
        ArrayList<ArrayList<Object>> meanReport = new ArrayList<ArrayList<Object>>() ;
        
        ArrayList<ArrayList<Object>> firstReport = reportList.get(0) ;
        int nbReports = reportList.size() ;
        int nbCycles = firstReport.size() ;
        int nbSubReports = firstReport.get(0).size() ;
        for (int cycle = 0 ; cycle < nbCycles ; cycle++)
        {
            String itemString ;
            ArrayList<Object> meanRecord = new ArrayList<Object>() ;
            for (int itemIndex = 0 ; itemIndex < nbSubReports ; itemIndex++ )
            {
                double itemValue = 0.0 ;
                for (ArrayList<ArrayList<Object>> report : reportList)
                {
                    ArrayList<Object> record = report.get(cycle) ;
                    itemValue += Double.valueOf(Reporter.EXTRACT_VALUE(propertyName,String.valueOf(record.get(itemIndex)))) ;
                }
                itemString = Reporter.ADD_REPORT_PROPERTY(propertyName, itemValue/nbReports) ;
                meanRecord.add(itemString) ;
            }
            meanReport.add((ArrayList<Object>) meanRecord.clone()) ;
        }
        return meanReport ;
    }

    /**
     * 
     * @param propertyName
     * @param reportList
     * @return (ArrayList) report with (ArrayList) subreports where the values 
     * in the subreports are averaged over the innermost ArrayList.
     */
    static public HashMap<Object,String> 
        PREPARE_MEAN_HASHMAP_REPORT(String propertyName, ArrayList<HashMap<Comparable,String>> reportList)
    {
        // Find mean of reports
        HashMap<Object,String> meanReport = new HashMap<Object,String>() ;
        
        HashMap<Comparable,String> firstReport = reportList.get(0) ;
        int nbReports = reportList.size() ;
        String firstReportString = firstReport.values().iterator().next() ;
        ArrayList<String> reportProperties ;
        if (propertyName.isEmpty())
            reportProperties = IDENTIFY_PROPERTIES(firstReportString) ;
        else
        {
            reportProperties = new ArrayList<String>() ;
            reportProperties.add(propertyName) ;
        }
        
        for (Comparable key : firstReport.keySet())
        {
            String itemString ;
            String meanRecord = "" ;
            //Number[] meanRecord = new Number[nbSubReports] ;
            for (String property : reportProperties) 
            {
                double itemValue = 0.0 ;
                for (HashMap<Comparable,String> report : reportList)
                {
                    String record = report.get(key) ;
                    itemValue += Double.valueOf(Reporter.EXTRACT_VALUE(property,record)) ;
                }
                itemString = Reporter.ADD_REPORT_PROPERTY(property, itemValue/nbReports) ;
                meanRecord = meanRecord.concat(itemString) ;
            }
            meanReport.put(key,meanRecord) ;
        }
        return meanReport ;
    }


//     /**
//      * 
//      * Averages over reports in (ArrayList) REPORT_LIST and saves it if static variable
//  WRITE_REPORT is true.
//      * @param reportList
//      * @param categoryName
//      * @param reportName
//      * @param nameSimulation
//      * @return (ArrayList) report with (ArrayList) subreports where the values 
//      * in the subreports are averaged over the innermost ArrayList.
//      */
//     static public HashMap<Comparable,String> 
//         PREPARE_MEAN_CI_HASHMAP_REPORT(ArrayList<HashMap<Comparable,String>> reportList, String categoryName, String reportName, String nameSimulation)
//     {
//         // Find mean of reports
//         HashMap<Comparable,String> meanReport = new HashMap<Comparable,String>() ;
        
//         HashMap<Comparable,String> firstReport = reportList.get(0) ;
//         int nbReports = reportList.size() ;
//         String firstReportString = (String) String.valueOf(firstReport.values().iterator().next()) ;
//         String meanRecord = "" ;
//         ArrayList<String> reportProperties = IDENTIFY_PROPERTIES(firstReportString) ;
        
        
//         for (Comparable key : firstReport.keySet())
//         {
//             //LOGGER.info("year:" + key.toString());
//             for (String propertyName : reportProperties)
//             {
//                 Double itemValue = 0.0 ;
//                 ArrayList<String> values = new ArrayList<String>();

//                 for (HashMap<Comparable,String> report : reportList)
//                 {
//                     String record = report.get(key) ;
//                     String valueStr = Reporter.EXTRACT_VALUE(propertyName,record) ;
//                     // LOGGER.info("Test " + valueStr + " " + String.valueOf(nbReports) + "\n");

//                     try
//                     {
//                         itemValue += Double.valueOf(valueStr) ;
//                         values.add(valueStr);
//                     }
//                     catch (Exception e)
//                     {
//                         LOGGER.log(Level.SEVERE,"report:{0} propertyName:{1} {2}", new Object[] {reportList.indexOf(report),propertyName,e.toString()}) ;
//                         itemValue += 0.0 ;
//                         values.add("0.0");
//                     }
//                 }


//                 Double mean = itemValue / Double.valueOf(nbReports) ;
//                 Double stdDev = Reporter.getStandardDeviationDoubleValueFromArrayListOfDouble(values, mean);
//                 Double[] confidenceInerval = Reporter.get95ConfidenceIntervalDoubleArray(mean, stdDev, Double.valueOf(values.size()));
                
//                 String meanAndCIString =    String.valueOf(mean) + " " +
//                                             String.valueOf(confidenceInerval[0]) + " " +
//                                             String.valueOf(confidenceInerval[1]) ;

//                 String itemString = Reporter.ADD_REPORT_PROPERTY(propertyName, meanAndCIString) ;
//                 meanRecord = meanRecord.concat(itemString) ;
//             }
//             meanReport.put(key,meanRecord) ;
            
//             // Prepare for next key
//             meanRecord = "" ;
//         }
        
//         if (WRITE_REPORT)
//             WRITE_CSV_STRING(meanReport, categoryName, reportName, nameSimulation, REPORT_FOLDER) ;
        
//         return meanReport ;
//     }


    static public String 
        PREPARE_MEAN_REPORT(ArrayList<String> reportList)
    {
        // Find mean of reports
        String meanReport = "" ; 
        
        String firstReport = reportList.get(0) ;
        int nbReports = reportList.size() ;
        ArrayList<String> reportProperties = IDENTIFY_PROPERTIES(firstReport) ;
        for (String propertyName : reportProperties)
        {
            double itemValue = 0.0 ;
            for (String record : reportList)
                itemValue += Double.valueOf(Reporter.EXTRACT_VALUE(propertyName,record)) ;
            meanReport += Reporter.ADD_REPORT_PROPERTY(propertyName, itemValue/nbReports) ;
        }
        return meanReport ;
    }

    /**
     * Finds mean of reports in REPORT_LIST but never writes it to a .csv file.
     * @param reportList
     * @return (ArrayList) report with (ArrayList) subreports where the values 
     * in the subreports are averaged over the innermost ArrayList.
     */
    static public HashMap<Comparable<?>,String> 
        PREPARE_MEAN_HASHMAP_REPORT(ArrayList<HashMap<Comparable<?>,String>> reportList)
        {
            boolean writeLocal = WRITE_REPORT ;
            WRITE_REPORT = false ;
            HashMap<Comparable<?>,String> meanReport = PREPARE_MEAN_HASHMAP_REPORT(reportList, "", "", "") ;
            WRITE_REPORT = writeLocal ;
            return meanReport ;
        }
    

    /**
     * 
     * Averages over reports in (ArrayList) REPORT_LIST and saves it if static variable
 WRITE_REPORT is true.
     * @param reportList
     * @param categoryName
     * @param reportName
     * @param nameSimulation
     * @return (ArrayList) report with (ArrayList) subreports where the values 
     * in the subreports are averaged over the innermost ArrayList.
     */
    static public HashMap<Comparable<?>,String> 
        PREPARE_MEAN_HASHMAP_REPORT(ArrayList<HashMap<Comparable<?>,String>> reportList, String categoryName, String reportName, String nameSimulation)
    {
        // Find mean of reports
        HashMap<Comparable<?>,String> meanReport = new HashMap<Comparable<?>,String>() ;
        
        HashMap<Comparable<?>,String> firstReport = reportList.get(0) ;
        int nbReports = reportList.size() ;
        String firstReportString = (String) String.valueOf(firstReport.values().iterator().next()) ;
        String meanRecord = "" ;
        ArrayList<String> reportProperties = IDENTIFY_PROPERTIES(firstReportString) ;
        
        
        for (Comparable key : firstReport.keySet())
        {
            //LOGGER.info("year:" + key.toString());
            for (String propertyName : reportProperties)
            {
                double itemValue = 0.0 ;
                for (HashMap<Comparable<?>,String> report : reportList)
                {
                    String record = report.get(key) ;
                    try
                    {
                        itemValue += Double.valueOf(Reporter.EXTRACT_VALUE(propertyName,record)) ;
                    }
                    catch (Exception e)
                    {
                        LOGGER.log(Level.SEVERE,"report:{0} propertyName:{1} {2}", new Object[] {reportList.indexOf(report),propertyName,e.toString()}) ;
                        itemValue += 0.0 ;
                    }
                }
                meanRecord += ADD_REPORT_PROPERTY(propertyName,itemValue/nbReports) ;
            }
            meanReport.put(key,meanRecord) ;
            
            // Prepare for next key
            meanRecord = "" ;
        }
        
        if (WRITE_REPORT)
            WRITE_CSV_STRING(meanReport, categoryName, reportName, nameSimulation, REPORT_FOLDER) ;
        
        return meanReport ;
    }
        
    /**
     * 
     * Averages over entries in sorted reports in (ArrayList) REPORT_LIST.
     * Avoids saving it now, better done at plot Method.
     * @param reportList
     * @param categoryName
     * @param sortingProperty
     * @param reportName
     * @param nameSimulation
     * @return (ArrayList) report with (ArrayList) subreports where the values 
     * in the subreports are averaged over the innermost ArrayList.
     */
    static public HashMap<Object,HashMap<Object,String>> 
        PREPARE_MEAN_HASHMAP_REPORT(ArrayList<HashMap<Object,HashMap<Comparable<?>,String>>> reportList, String categoryName, String sortingProperty, String reportName, String nameSimulation)
    {
        // Find mean of reports
        HashMap<Object,HashMap<Object,String>> meanReport = new HashMap<Object,HashMap<Object,String>>() ;
        
        boolean writeLocal = WRITE_REPORT ;
        WRITE_REPORT = false ;
        
        // Cycle through sortingProperty values
        HashMap<Object,HashMap<Comparable<?>,String>> firstReport = reportList.get(0) ;
        Set<Object> sortingValues = (Set<Object>) firstReport.keySet() ;
        
        for (Object sortingValue : sortingValues)
        {
            ArrayList<HashMap<Comparable<?>,String>> sortedList = new ArrayList<HashMap<Comparable<?>,String>>() ;
            for (HashMap<Object,HashMap<Comparable<?>,String>> report : reportList)
                sortedList.add(report.get(sortingValue)) ;
            meanReport.put(sortingValue, (HashMap<Object,String>) PREPARE_MEAN_HASHMAP_REPORT(sortedList, categoryName, reportName, nameSimulation).clone()) ;
        }
        
        //if (writeLocal)
            //WRITE_CSV_STRING(meanReport, categoryName, reportName, nameSimulation, REPORT_FOLDER) ;
        WRITE_REPORT = writeLocal ;
        
        return meanReport ;
    }
    

    /**
     * 
     * @param simNames
     * @param folderPath
     * @param startYear
     * @param endYear
     * @return (HashMap) report with where the values are averaged reports in REPORT_LIST.
     */
    static public HashMap<Comparable<?>,Number[]> PREPARE_GRAY_REPORT(String[] simNames, String folderPath, int startYear, int endYear)
    {
        HashMap<Comparable<?>,Number[]> grayReport = new HashMap<Comparable<?>,Number[]>() ;
        ArrayList<HashMap<Comparable<?>,String>> hashMapReports = new ArrayList<HashMap<Comparable<?>,String>>() ;
        //ArrayList<ArrayList<Object>> arrayListReports = new ArrayList<ArrayList<Object>>() ;
        ArrayList<String> reportNames = new ArrayList<String>() ;
//        int nbColumns = 8 ; // 4+4+4+1+1 ;
        ArrayList<String> colNames = new ArrayList<String>() ;
        
        String[] siteNames = MSM.SITE_NAMES ;
        String[] relationshipClazzNames = new String[] {"Casual","Regular","Monogomous"} ;
        
//        for (int siteIndex = 0 ; siteIndex < siteNames.length ; siteIndex++ )
//            arrayNames[siteIndex] = siteNames[siteIndex] ;
//        arrayNames[siteNames.length] = "all" ;
//        
        ArrayList<Comparable<?>> sortedYears = new ArrayList<Comparable<?>>() ;
        
        int backYears = 1 + endYear - startYear ;
        
        ArrayList<HashMap<Comparable<?>,String>> prevalenceReports = new ArrayList<HashMap<Comparable<?>,String>>() ;  
        ArrayList<HashMap<Comparable<?>,String>> prevalenceHivReports = new ArrayList<HashMap<Comparable<?>,String>>() ;  
        ArrayList<HashMap<Comparable<?>,String>> prevalenceRiskyReports = new ArrayList<HashMap<Comparable<?>,String>>() ;  
        ArrayList<HashMap<Comparable<?>,String>> notificationReports = new ArrayList<HashMap<Comparable<?>,String>>() ;
        ArrayList<HashMap<Comparable<?>,String>> notificationHivReports = new ArrayList<HashMap<Comparable<?>,String>>() ;
        ArrayList<HashMap<Comparable<?>,String>> incidenceReports = new ArrayList<HashMap<Comparable<?>,String>>() ;
        ArrayList<HashMap<Comparable<?>,String>> incidenceHivReports = new ArrayList<HashMap<Comparable<?>,String>>() ;
        ArrayList<HashMap<Comparable<?>,String>> riskyIncidenceReports = new ArrayList<HashMap<Comparable<?>,String>>() ;
        ArrayList<HashMap<Comparable<?>,String>> riskyIncidenceHivReports = new ArrayList<HashMap<Comparable<?>,String>>() ;
        ArrayList<HashMap<Comparable<?>,String>> condomlessReports = new ArrayList<HashMap<Comparable<?>,String>>() ;
        ArrayList<HashMap<Comparable<?>,String>> condomlessHivReports = new ArrayList<HashMap<Comparable<?>,String>>() ;
        
        
        ArrayList<HashMap<Comparable,Number>> beenTestedReports = new ArrayList<HashMap<Comparable,Number>>() ;
        ArrayList<ArrayList<String>> condomUseReports = new ArrayList<ArrayList<String>>() ;
        for (String simulation : simNames)
        {
            ScreeningReporter screeningReporter = new ScreeningReporter(simulation, folderPath);
            prevalenceReports.add(screeningReporter.prepareYearsPrevalenceRecord(siteNames, backYears, endYear, "")) ;
            prevalenceHivReports.add(screeningReporter.prepareYearsPrevalenceRecord(siteNames, backYears, endYear, "statusHIV")) ;
            prevalenceRiskyReports.add(screeningReporter.prepareYearsPrevalenceRecord(siteNames, backYears, endYear, "riskyStatus")) ;
            notificationReports.add(screeningReporter.prepareYearsNotificationsRecord(siteNames, backYears, endYear)) ;
            notificationHivReports.add(screeningReporter.prepareSortedYearsNotificationsRecord(siteNames, backYears, endYear, "statusHIV")) ;
            EncounterReporter encounterReporter = new EncounterReporter(simulation, folderPath);
            incidenceReports.add(encounterReporter.prepareYearsIncidenceRecord(siteNames, backYears, endYear)) ;
            incidenceHivReports.add(encounterReporter.prepareYearsIncidenceReport(siteNames, backYears, endYear, "statusHIV")) ;
            condomlessReports.add(encounterReporter.preparePercentAgentCondomlessYears(relationshipClazzNames, backYears, endYear, "statusHIV", false, "")) ;
            condomlessHivReports.add(encounterReporter.preparePercentAgentCondomlessYears(relationshipClazzNames, backYears, endYear, "statusHIV", false, "statusHIV")) ;
            beenTestedReports.add(screeningReporter.prepareYearsBeenTestedReport(backYears, 0, 0, endYear)) ;
            condomUseReports.add(encounterReporter.prepareYearsCondomUseRecord(backYears, endYear)) ;
        }
        HashMap<Comparable<?>,String> prevalenceRecordYears = PREPARE_MEAN_HASHMAP_REPORT(prevalenceReports) ;
        hashMapReports.add((HashMap<Comparable<?>,String>) prevalenceRecordYears) ;
        reportNames.add("prevalence") ;
        HashMap<Comparable<?>,String> prevalenceHivRecordYears = PREPARE_MEAN_HASHMAP_REPORT(prevalenceHivReports) ;
        hashMapReports.add((HashMap<Comparable<?>,String>) prevalenceHivRecordYears) ;
        reportNames.add("prevalence_HIV") ;
        HashMap<Comparable<?>,String> prevalenceRiskyRecordYears = PREPARE_MEAN_HASHMAP_REPORT(prevalenceRiskyReports) ;
        hashMapReports.add((HashMap<Comparable<?>,String>) prevalenceRiskyRecordYears) ;
        reportNames.add("prevalence_risky") ;
        HashMap<Comparable<?>,String> notificationsRecordYears = PREPARE_MEAN_HASHMAP_REPORT(notificationReports) ;
        hashMapReports.add((HashMap<Comparable<?>,String>) notificationsRecordYears) ;
        reportNames.add("notifications") ;
        HashMap<Comparable<?>,String> notificationsHivRecordYears = PREPARE_MEAN_HASHMAP_REPORT(notificationHivReports) ;
        hashMapReports.add((HashMap<Comparable<?>,String>) notificationsHivRecordYears) ;
        reportNames.add("notifications_HIV") ;
        HashMap<Comparable<?>,String> incidenceReportYears = PREPARE_MEAN_HASHMAP_REPORT(incidenceReports) ;
        hashMapReports.add((HashMap<Comparable<?>,String>) incidenceReportYears) ;
        reportNames.add("incidence") ;
        HashMap<Comparable<?>,String> incidenceHivReportYears = PREPARE_MEAN_HASHMAP_REPORT(incidenceHivReports) ;
        hashMapReports.add((HashMap<Comparable<?>,String>) incidenceHivReportYears) ;
        reportNames.add("incidence_HIV") ;
        HashMap<Comparable<?>,String> riskyIncidenceReportYears = PREPARE_MEAN_HASHMAP_REPORT(riskyIncidenceReports) ;
        hashMapReports.add((HashMap<Comparable<?>,String>) riskyIncidenceReportYears) ;
        reportNames.add("risky_incidence") ;
        HashMap<Comparable<?>,String> riskyIncidenceHivReportYears = PREPARE_MEAN_HASHMAP_REPORT(riskyIncidenceHivReports) ;
        hashMapReports.add((HashMap<Comparable<?>,String>) riskyIncidenceHivReportYears) ;
        reportNames.add("risky_incidence_HIV") ;
        HashMap<Comparable<?>,String> condomlessReportYears = PREPARE_MEAN_HASHMAP_REPORT(condomlessReports) ;
        hashMapReports.add((HashMap<Comparable<?>,String>) condomlessReportYears) ;
        reportNames.add("condomless") ;
        HashMap<Comparable<?>,String> condomlessHivReportYears = PREPARE_MEAN_HASHMAP_REPORT(condomlessHivReports) ;
        hashMapReports.add((HashMap<Comparable<?>,String>) condomlessHivReportYears) ;
        reportNames.add("condomless_HIV") ;
        
        HashMap<Comparable,Number> beenTestedReportYears = MEAN_HASHMAP_REPORT(beenTestedReports) ;
        ArrayList<String> condomUseYears = AVERAGED_REPORT(condomUseReports,"proportion") ;
        
        // Construct Gray report
        ArrayList<String> properties ;
        for (int year = startYear ; year <= endYear ; year++)
        {
            ArrayList<Number> valuesList = new ArrayList<Number>() ;
            for (int reportIndex = 0 ; reportIndex < hashMapReports.size() ; reportIndex++ )
            {
                HashMap<Comparable<?>,String> hashMapReport = hashMapReports.get(reportIndex) ;
                properties = IDENTIFY_PROPERTIES(hashMapReport.get(year)) ;
                // LOGGER.info(reportNames.get(reportIndex) + " " + properties.toString() + " " + hashMapReport.get(year));
                //LOGGER.log(Level.INFO, "{0}", prevalenceRecordYears.get(year));
                for (int propertyIndex = 0 ; propertyIndex < properties.size() ; propertyIndex++ )    // 
                {
                    if (year == startYear)
                        colNames.add(reportNames.get(reportIndex) + "_" + properties.get(propertyIndex)) ; //TODO
                    valuesList.add(Double.valueOf(EXTRACT_VALUE(properties.get(propertyIndex),hashMapReport.get(year)))) ;
                }
            }
            String condomValue = condomUseYears.get(year - startYear).toString() ;
            valuesList.add(Double.valueOf(EXTRACT_VALUE("proportion",condomValue))) ;
//            valuesList.add(beenTestedReportYears.get(year)) ;
            if (year == startYear)
            {
                colNames.add("condom_use") ; //TODO
//                colNames.add("testing_coverage") ;
            }       
            
            Number[] valuesArray = valuesList.toArray(new Number[0]) ; // new Number[valuesList.size()] ;
            grayReport.put(year, valuesArray) ;
            sortedYears.add(year) ;
        }
        
        String[] colNamesArray = (String[]) colNames.toArray(new String[0]) ;
        
        WRITE_CSV(grayReport,"year",colNamesArray,sortedYears,"Gray_report",simNames[0],REPORT_FOLDER) ;
        return grayReport ;
    }
    
    /**
     * Stores an ArrayList (String) report as a csv file for other packages to read.
     * @param report 
     * @param reportName 
     * @param simName 
     * @param folderPath 
     */
    static public void WRITE_CSV(ArrayList<String> report, String reportName, String simName, String folderPath)
    {
        Reporter.WRITE_CSV(report, new String[] {reportName}, simName, folderPath) ;
    }


    /**
     * Stores an ArrayList (String) report as a csv file for other packages to read.
     * @param report 
     * @param scoreNames 
     * @param simName 
     * @param folderPath 
     */
    static public void WRITE_CSV(ArrayList<String> report, String[] scoreNames, String simName, String folderPath)
    {
        String filePath ;
        String line ;
        
        ArrayList<String> properties = new ArrayList<String>() ;
        String fileHeader = "cycle," ;
        
        //Object firstRecord = report.get(0) ;
        filePath = folderPath + simName + scoreNames[0] + ".csv";
        properties = IDENTIFY_PROPERTIES(report.get(0)) ;   
        //for (int index = 1 ; index < properties.size() ; index++ )
          //  fileHeader += "," + properties.get(index) ;
        fileHeader += String.join(COMMA, properties) ;
        try
        {
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(filePath,false));
            fileWriter.write(fileHeader) ;
            fileWriter.newLine();
            int cycle = 0 ;
            for (String record : report)
            {
                line = String.valueOf(cycle) ;
                for (String property : properties)
                    line += COMMA + EXTRACT_VALUE(property,record) ;
                fileWriter.write(line) ;
                fileWriter.newLine() ;
                cycle++ ;
            }
            fileWriter.close() ;
        }
        catch( Exception e )
        {
            LOGGER.severe(e.toString()) ;
        }
    }


    /**
     * Stores an ArrayList (String) report as a csv file for other packages to read.
     * @param report 
     * @param scoreNames 
     * @param property 
     * @param simName 
     * @param folderPath 
     * @return (boolean) true if successful but false if an Exception is encountered.
     */
    static public boolean WRITE_CSV(ArrayList<ArrayList<String>> report, String[] scoreNames, String property, String simName, String folderPath)
    {
        String filePath ;
        String line ;
        int cycle ;
        String fileHeader = "cycle," ;
        String record ;
        
        filePath = folderPath + simName + property + ".csv";
        fileHeader += String.join(COMMA, scoreNames) ;
        fileHeader += COMMA + property ;
            
        try
        {
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(filePath,false));
            fileWriter.write(fileHeader) ;
            fileWriter.newLine();
            
            int nbCycles = report.get(0).size() ;
            for (cycle = 0 ; cycle < nbCycles ; cycle++ )
            {
                line = String.valueOf(cycle) ;
                //for (String entry : (ArrayList<String>) record)
                for (int scoreIndex = 0 ; scoreIndex < scoreNames.length ; scoreIndex++ ) 
                {
                    record = report.get(scoreIndex).get(cycle) ;
                    line += COMMA + EXTRACT_VALUE(property, (String) record) ;
            }
                fileWriter.write(line) ;
                fileWriter.newLine() ;
            }
            fileWriter.close() ;
        }
        catch( Exception e )
        {
            LOGGER.severe(e.toString()) ;
            return false ;
        }
        
        return true ;
    }
    
    /**
     * Stores a (HashMap of Numbers) report as a csv file for other packages to read.
     * @param report 
     * @param categoryName 
     * @param scoreNames 
     * @param reportName 
     * @param simName 
     * @param folderPath 
     */
    static public boolean WRITE_CSV(HashMap<Comparable<?>,Number[]> report, String categoryName, String[] scoreNames, String reportName, String simName, String folderPath)
    {
        return WRITE_CSV(report, categoryName, scoreNames, new ArrayList<Comparable<?>>(), reportName, simName, folderPath) ;
    }
    
    /**
     * Stores a (HashMap of ArrayList or Object) report as a csv file for other packages to read.
     * @param report 
     * @param categoryName 
     * @param scoreName 
     * @param reportName 
     * @param simName 
     * @param folderPath 
     */
    static public boolean WRITE_CSV(HashMap<Comparable<?>,Number> report, String categoryName, String scoreName, String reportName, String simName, String folderPath)
    {
        HashMap<Comparable<?>,Number[]> convertedReport = new HashMap<Comparable<?>,Number[]>() ;
        for (Comparable key : report.keySet())
            convertedReport.put(key, new Number[] {report.get(key)}) ;
        
        return WRITE_CSV(convertedReport, categoryName, new String[] {scoreName}, new ArrayList<Comparable<?>>(report.keySet()), reportName, simName, folderPath) ;
    }
    
    /**
     * Stores a (HashMap of Array of Numbers) report as a csv file for other packages to read.
     * @param report 
     * @param categoryName 
     * @param scoreNames 
     * @param reportName 
     * @param simName 
     * @param folderPath 
     */
    static public boolean WRITE_CSV(HashMap<Comparable<?>,Number[]> report, String categoryName, String[] scoreNames, ArrayList<Comparable<?>> categoryList, String reportName, String simName, String folderPath)
    {
        String filePath = folderPath + simName + reportName + ".csv" ;
        String line ;
        Object[] value ;
        
        int nbProperties = 0 ;
        //Determine if report values are ArrayList<Object>
        Object[] firstRecord = (Object[]) report.values().toArray()[0] ;
        //LOGGER.log(Level.INFO, "{0}", firstRecord);

        String fileHeader = categoryName.concat(COMMA) ;
        fileHeader += String.join(COMMA, scoreNames)  ;
        //LOGGER.info(fileHeader) ;
        
        //HashMap<Object,Number[]> writeReport = report.size()
        try
        {
            nbProperties = firstRecord.length ;
            //fileHeader += COMMA + reportName ;
        }
        catch ( Exception e )
        {
            LOGGER.severe("values are not Arrays");
            nbProperties = 1 ;
        }
        //LOGGER.log(Level.INFO, "nbProperties:{1}", new Object[] {nbProperties});
        if (categoryList.isEmpty())
            categoryList = new ArrayList<Comparable<?>>(report.keySet()) ;
        categoryList.sort(null) ;
        
        try
        {
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(filePath,false));
            fileWriter.write(fileHeader) ;
            fileWriter.newLine() ;
            for (Object key : categoryList)
            {
                value = report.get(key) ;
                line = String.valueOf(key) ;
                if (nbProperties > 1)
                    for (Object item : value)
                        line += COMMA + String.valueOf(item) ;
                else
                    line += COMMA + String.valueOf(value[0]) ;
                fileWriter.write(line) ; 
                fileWriter.newLine() ;
                //LOGGER.info(line);
            }
            fileWriter.close() ;
        }
        catch( Exception e )
        {
            LOGGER.severe(e.toString()) ;
            return false ;
        }
        return true ;
    }
    
    /**
     * Reads in values from multiple files whose names are derived from simNames 
     * and writes them all to one file for coplotting.
     * @param simNames
     * @param reportName
     * @param folderPath
     * @return 
     */
    static public boolean MULTI_WRITE_CSV(ArrayList<String> simNames, String reportName, String folderPath) {
        ArrayList<String> outputReport = new ArrayList<String>() ;
        // 
        
        ArrayList<String> properties = new ArrayList<String>() ;
        String propertyValues ;
        try
        {
            for (String simulationName : simNames)
            {
                BufferedReader fileReader 
                        = new BufferedReader(new FileReader(folderPath + reportName + "_" + simulationName + ".txt")) ;

                // Read input file of simulationName
                String fileLine = fileReader.readLine() ;
                fileReader.close() ;
                
                if (properties.isEmpty())
                    properties = IDENTIFY_PROPERTIES(fileLine) ;
                
                propertyValues = simulationName + COMMA ;
                int propertyIndex = 0 ;    // Avoids repeating values when properties are repeated
                for (String property : properties)
                {
                    if (property.isEmpty())
                        continue ;
                    propertyValues += EXTRACT_VALUE(property,fileLine,propertyIndex) + COMMA ;
                    propertyIndex = INDEX_OF_PROPERTY(property,propertyIndex,fileLine) + 1 ;
                }
                // Remove trailing COMMA
                propertyValues = propertyValues.substring(0, propertyValues.length() - 1) ;
                outputReport.add(propertyValues) ;
                
            }
            properties.add(0,"simName") ; 
            outputReport.add(0,String.join(COMMA, properties)) ;
            
        }
        catch ( Exception e )
        {
            LOGGER.severe(e.toString());
            //return false;
        }
        
        String filePath = folderPath + reportName + "_" + simNames.get(0) + ".csv" ;
        try
        {
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(filePath,false));
            for (String row : outputReport)
            {
                fileWriter.write(row) ; 
                fileWriter.newLine() ;
                // LOGGER.info(row);
            }
            fileWriter.close() ;
        }
        catch( Exception e )
        {
            LOGGER.severe(e.toString()) ;
            return false ;
        }
        
        return true ;        
    }
    

    /**
     * Reads in values from multiple files whose names are derived from simNames 
     * and writes them all to one file for coplotting.
     * @param simNames
     * @param categoryName
     * @param scoreName
     * @param reportName
     * @param folderPath
     * @return 
     */
    static public boolean MULTI_WRITE_CSV(  List<String> simNames,
                                            String categoryName,
                                            String scoreName,
                                            String reportName,
                                            String folderPath) 
    {
        
        // LOGGER.info("@@TEST: here multi write csv");                           
                                                
        HashMap<Comparable,String> outputReport = new HashMap<Comparable,String>() ;
        
        // To keep track of scores and find their mean
        HashMap<Comparable,Double> totalReport = new HashMap<Comparable,Double>() ;
        
        // Load prior data when looking at at-risk incidence
        String fileName = "gonoGoneWild" ;
        HashMap<Comparable,String[]> priorData = new HashMap<Comparable,String[]>() ;
        String[] priorScoreNames = new String[0] ;
        Integer[] priorScoreIndices = new Integer[2] ;
        
        // Set up header
        String firstLine = "median" + COMMA ;
        firstLine += "lower" + COMMA;
        firstLine += "upper" + COMMA;

        // String firstLine = "";
        // if (reportName.contains("riskyIncidence_HIV"))
        // {
        //     firstLine = "gono-gone-wild" + COMMA + "Ann. Surv. Rep." + COMMA + firstLine ;
        //     priorData = READ_CSV_STRING(fileName, DATA_FOLDER) ;
        //     priorScoreNames = priorData.get(categoryName) ;
        
        //     // Which columns in gonoGoneWild.csv have the scoreName data 
        //     int priorIndex = 0 ;
        //     for (int scoreIndex = 0 ; scoreIndex < priorScoreNames.length ; scoreIndex++ )
        //     {
            //         if (priorScoreNames[scoreIndex].contains(scoreName))
            //         {
                //             priorScoreIndices[priorIndex] = scoreIndex ;
                //             priorIndex++ ;
        //         }
        //         if (priorIndex == priorScoreIndices.length)
        //             break ;
        //     }
        // } 
        
        //HashMap<Comparable,String> meanReport = PREPARE_MEAN_HASHMAP_REPORT(scoreName,simNames) ;
        
        for (String simulationName : simNames)
        {
            try
            {
                BufferedReader fileReader 
                    = new BufferedReader(new FileReader(folderPath + reportName + "_" + simulationName + ".txt")) ;

                // Read last line of input file of simulationName
                String fileLine = "" ; // fileReader.readLine() ;
                String fileLine2 = fileReader.readLine() ;
                //fileLine2 = fileReader.readLine() ;
                while (fileLine2 != null)
                {
                    fileLine = fileLine2 ;
                    fileLine2 = fileReader.readLine() ;
                }
                // LOGGER.info(fileLine);
                fileReader.close() ;
                
                
                // If file is successfully read
                firstLine += simulationName + COMMA ;

                //ArrayList<String> scoreNames = IDENTIFY_PROPERTIES(fileLine) ;
                //? categoryName = scoreNames[0] ;
                //int scoreIndex = Arrays.asList(scoreNames).indexOf(scoreName) ;


                String scoreValue ;

                //ArrayList<String> keyValues = new ArrayList<String>() ;
                int paramIndex = fileLine.indexOf("{",10) ;
                if (paramIndex < 0)
                    paramIndex = 0 ;
                
                int keyIndex = fileLine.indexOf("=",paramIndex) ;
                int spaceIndex = paramIndex ;
                int keyIndex2 ;
                
                //while (fileLine != null)
                while (keyIndex < fileLine.length())
                {
                    String categoryValue = fileLine.substring(spaceIndex + 1, keyIndex) ;
                    keyIndex2 = fileLine.indexOf("=",keyIndex + 1) ;
                    if (keyIndex2 < 0)
                        keyIndex2 = fileLine.length() ;
                    String scoreString = fileLine.substring(keyIndex, keyIndex2) ;
                    if (!scoreString.contains(":"))
                    {
                        int commaIndex = scoreString.indexOf(",") ;
                        if (commaIndex < 0)
                        	commaIndex = scoreString.length() - 1 ;
                        scoreValue = scoreString.substring(1,commaIndex) ;
                    }
                    else
                        scoreValue = EXTRACT_VALUE(scoreName,scoreString) ;
                    //LOGGER.log(Level.INFO, "year:{0} score:{1}", new Object[] {categoryValue,scoreString});
                    keyIndex = keyIndex2 ;
                    // Select SPACE immediately before "="
                    spaceIndex = fileLine.lastIndexOf(SPACE, keyIndex) ;
                    
                    //if (Integer.valueOf(categoryValue) < 2015)
                    //	continue ;
                    
                    // Initialise fileLine for Each categoryValue with "categoryValue"
                    
                    if (!outputReport.containsKey(categoryValue))
                    {
                        String initCategoryValue = categoryValue ;
                        
                        // if reading from gonoGoneWild.csv
                        if (priorScoreNames.length > 0)
                        {
                            for (int priorIndex : priorScoreIndices)
                            {
                                String[] priorValues = new String[] {} ;
                                if (priorData.containsKey(categoryValue))
                                    priorValues = priorData.get(categoryValue) ;
                                if (priorIndex < priorValues.length)
                                    initCategoryValue += COMMA + priorValues[priorIndex] ;
                                else
                                    initCategoryValue += COMMA + EMPTY ;
                            }
                        }
                        totalReport.put(categoryValue, 0.0) ;
                        outputReport.put(categoryValue, initCategoryValue) ;
                    }
                    outputReport.put(categoryValue, String.join(COMMA, outputReport.get(categoryValue), scoreValue)) ;
                    totalReport.put(categoryValue, totalReport.get(categoryValue) + Double.valueOf(scoreValue)) ;
                }   
            }
            catch ( Exception e )
            {
                LOGGER.severe(e.toString());
                continue ;
                //return false;
            }
            
        }
        
        // Insert mean scoreValues after priorData if present but before simulations
        int nbSimulations = simNames.size() ;
        LOGGER.info(String.valueOf(nbSimulations) + " simulations included.") ;
        
        int nbPriors = firstLine.split(COMMA).length - simNames.size() -1 ;
        String outputValue ;

        // indices for medianAndRange matches with the one declared in
        // Reporter.generateMedianAndRangeArrayFromValuesArray
        int yValueIndex = 0;
        int lowerIndex = 1;
        int upperIndex = 2;
        
        for (Comparable categoryValue : outputReport.keySet() ) 
        {
            String valuesCommaSeparatedString = outputReport.get(categoryValue);

            // extract information into an arraylist, remove leading and trailing whitespace before and after a comma
            String[] valuesArray = valuesCommaSeparatedString.split("\\s*,\\s*"); // split on comma and leading/trailing spaces
            ArrayList<String> valuesArrayList = new ArrayList<String>(Arrays.asList(valuesArray));
            String year = valuesArrayList.remove(0); // remove the year and store it in a variable
            
            String[] medianAndRange = Reporter.GENERATE_MEDIAN_AND_RANGE_ARRAY_FROM_VALUES_ARRAY(valuesArray);

            // // calculate confidence intervals
            // Double[] confidenceInterval = Reporter.get95ConfidenceIntervalDoubleArray(  meanValue,
            //                                                                             standardDeviation,
            //                                                                             (double) nbSimulations);

            // insert year -> mean -> lower -> upper into array list
            // this then gets converted back into a comma separated string
            valuesArrayList.add(0, medianAndRange[upperIndex] );  // add higher to the start of values
            valuesArrayList.add(0, medianAndRange[lowerIndex] );  // add lower to start of values
            valuesArrayList.add(0, medianAndRange[yValueIndex]);  // add median to start of values
            valuesArrayList.add(0, year);  // add year to start of values
            
            outputValue = String.join(",", valuesArrayList);
            outputReport.put(categoryValue, outputValue);
        }

        ArrayList<String> categoryValues = new ArrayList<String>() ;
        Collections.addAll(categoryValues, outputReport.keySet().toArray(new String[] {})) ;
        Collections.sort(categoryValues,String.CASE_INSENSITIVE_ORDER) ;

        // write to csv
        String filePath = DATA_FOLDER + reportName + "_" + scoreName + "_" + simNames.get(0) + ".csv" ;    // REPORT_FOLDER 
        try
        {
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(filePath,false));
            firstLine = firstLine.substring(0, firstLine.length() - 1) ;
            fileWriter.write(categoryName + COMMA + firstLine) ;
            fileWriter.newLine() ;
            for (Object key : categoryValues)
            {
                fileWriter.write(outputReport.get(key)) ; 
                fileWriter.newLine() ;
                //LOGGER.info(line);
            }
            fileWriter.close() ;
        }
        catch( Exception e )
        {
            LOGGER.severe(e.toString()) ;
            return false ;
        }
        LOGGER.info(filePath) ;
        
        return true ;
    }

    /**
     * Returns an array with 3 strings where:
     * String[3], String[0] = median, String[1] = lower range, String[2] = upper range
     * 
     * @param values : String[], does not need to be sorted
     * @return String[]
     * @author dstass
     */
    public static String[] GENERATE_MEDIAN_AND_RANGE_ARRAY_FROM_VALUES_ARRAY(String[] values) {
        int VALUES_TO_ADD = 3; // y-value, lower, upper
        String[] toReturn = new String[VALUES_TO_ADD];

        ArrayList<String> valuesArrayList = new ArrayList<String>(Arrays.asList(values));
        String year = valuesArrayList.remove(0); // remove the year and store it in a variable
        ArrayList<Double> sortedValues = Reporter.convertArrayListStringToSortedArrayListDouble(valuesArrayList);
        
        // calculate standard deviation of the interdecile range of values
        // this will hopefully remove some outliers so this standard deviation represents 
        // most of the data (outliers do not factor in the calculations of this number)
        ArrayList<Double> sortedValuesInterdecile = new ArrayList<Double>();
        for (Double value : sortedValues) 
        	sortedValuesInterdecile.add(value);
        Reporter.removeOutliersFromSortedArrayListPercentileMethod(sortedValuesInterdecile, 0.25);        
        Double numValues = (double) sortedValuesInterdecile.size();
        Double meanValue = 0.0;
        for (int i = 0; i < sortedValuesInterdecile.size(); ++i) meanValue += sortedValuesInterdecile.get(i);
        meanValue /= numValues;
        Double standardDeviation = Reporter.getStandardDeviationDoubleValueFromArrayListOfDoubles(sortedValuesInterdecile, meanValue);
        
        // transform sorted data by removing outliers
        // Reporter.removeOutliersFromSortedArrayListPercentileMethod(sortedValues, 0.10);
        Reporter.removeOutliersFromSortedArrayListDeviationMethod(sortedValues, meanValue, standardDeviation);

        // get the median from a sorted array list
        Double medianValue = Reporter.extractMedianFromSortedArrayList(sortedValues);

        int yValueIndex = 0;
        int lowerIndex = 1;
        int upperIndex = 2;

        toReturn[yValueIndex] = String.valueOf(medianValue);
        toReturn[lowerIndex] = String.valueOf(sortedValues.get(0));
        toReturn[upperIndex] = String.valueOf(sortedValues.get(sortedValues.size() - 1));
        
        return toReturn;
    }
    /**
     * Returns median of a sorted list
     * @param values : ArrayList<Double>, sorted ascending, size > 0
     * @return Double: median
     * @author dstass
     */
    private static Double extractMedianFromSortedArrayList(ArrayList<Double> values) {
        int valuesSize = values.size() ;
        if (valuesSize == 0) 
        	return 0.0 ;
        if (valuesSize % 2 == 1) 
        	return values.get(valuesSize/2) ;
        else 
        {
            int midLeft = valuesSize/2 - 1 ;
            int midRight = valuesSize/2 ;
            return (values.get(midLeft) + values.get(midRight))/2 ;
        }
    }
    /**
     * Description:
     * Remove values outside of given interpercentile range
     * 
     * Example of interpercentile values:
     * QUARTILE = 0.25
     * DECILE = 0.10
     * 0.025 --> 2.5% -> 97.5%
     * 
     * @param values : ArrayList<Double>, sorted ascending
     * @param interpercentile : Double, 0 < interpercentile < 0.5
     */
    private static void removeOutliersFromSortedArrayListPercentileMethod(ArrayList<Double> values, double interpercentile) {
        int removeFromEnds = (int) (interpercentile * values.size()); // flooring
        int removeFromLeft = removeFromEnds;
        int removeFromRight = removeFromEnds;
        // Remove outliers from the left
        for (int i = 0; i < removeFromEnds; ++i) values.remove(0);

        // Remove outliers from the right
        for (int i = 0; i < removeFromEnds; ++i) values.remove(values.size() - 1);
    }

    /**
     * 
     * Description: Remove data points that are over 3 standard deviations from the mean of the dataset
     * 
     * @param values : ArrayList<Double>, sorted ascending
     * @param mean : Double
     * @param standardDeviation : Double
     * @author dstass
     */
    private static void removeOutliersFromSortedArrayListDeviationMethod(ArrayList<Double> values, Double mean, Double standardDeviation) {       
        Double MAX_DEVIATION = 3.0;  // 1 = 68, 2 = 95, 3 = 99.7
        int removeFromLeft = 0;
        int removeFromRight = 0;
        
        // Find how many outliers on the lower end (lower than mean)
        while (removeFromLeft < values.size()) {
            Double differenceFromMean = values.get(removeFromLeft) - mean;
            Double deviationFromMean = Math.abs(differenceFromMean / standardDeviation);
            if (deviationFromMean <= MAX_DEVIATION) break;
            removeFromLeft += 1;
        }
        
        // Find how many outliers on the upper end (higher than the mean)
        while (removeFromRight < values.size()) {
            Double differenceFromMean = values.get(values.size() - 1 - removeFromRight) - mean;
            Double deviationFromMean = Math.abs(differenceFromMean / standardDeviation);
            if (deviationFromMean <= MAX_DEVIATION) break;
            removeFromRight += 1;
        }
        
        if (removeFromLeft + removeFromRight > values.size()) return;
        if (removeFromLeft + removeFromRight > 0) {
            int debugTotalRemoves = removeFromLeft + removeFromRight;
        }

        // Remove outliers from the left
        for (int i = 0; i < removeFromLeft; ++i) values.remove(0);

        // Remove outliers from the right
        for (int i = 0; i < removeFromRight; ++i) values.remove(values.size() - 1);
    }
    
    /**
     * Converts an ArrayList<String> to a sorted ArrayList<Double>
     * @param strArrayList
     * @return
     */
    private static ArrayList<Double> convertArrayListStringToSortedArrayListDouble(ArrayList<String> strArrayList) {
        ArrayList<Double> toReturn = new ArrayList<Double>();
        for (String s : strArrayList) 
        {
        	if ("null".equals(s)) 
        		s = "0" ;
        	toReturn.add(Double.valueOf(s));
        }
        Collections.sort(toReturn);
        return toReturn;
    }


    /**
     * 
     * @param values - arraylist of Strings that can be parsed as a double
     * @param mean - double value
     * @return - a double representing the standard deviation
     * @author dstass
     */
    private static Double getStandardDeviationDoubleValueFromArrayListOfDoubles(ArrayList<Double> values, Double mean) {
        Double squaredDifferenceOngoingSum = 0.0;
        for (int i = 0; i < values.size(); ++i) {
            Double valueDouble = values.get(i);
            squaredDifferenceOngoingSum += Math.pow((valueDouble - mean), 2);            
        }
        Double variance = squaredDifferenceOngoingSum/ values.size();
        Double standardDeviation = Math.sqrt(variance);
        return standardDeviation;
    }


    /**
     * 
     * @param meanValue
     * @param standardDeviation
     * @param sampleSize - must not be zero
     * @return an array containing two doubles, lower and upper values ofr 95% CI
     * @author dstass
     */
    private static Double[] get95ConfidenceIntervalDoubleArray(Double meanValue, Double standardDeviation, Double sampleSize) {
        
        Double[] toReturn = new Double[2];

        // hard-coded 95% confidence interval
        Double confidenceLevel = 1.96;
        
        Double difference = (confidenceLevel * (standardDeviation)) / Math.sqrt(sampleSize);
        toReturn[0] = meanValue - difference;
        toReturn[1] = meanValue + difference;
        return toReturn;
    }

    /**
     * Extracts the first three items where items at index:
     * 1) y-value
     * 2) lower bound (such as lower range)
     * 3) upper bound
     * returns a HashMap with key being the given HashMap's key and 
     * value being a String array String[] with 3 items described above in that order
     * @param csvHashMap
     * @return
     */
    public static HashMap<String, String[]> extractYValueAndRange(HashMap<Comparable, String[]> csvHashMap) 
    {
        int FIRST_N_ITEMS_TO_EXTRACT = 3; // mean, upper and lower

        int Y_INDEX = 0;
        int LOWER_INDEX = 1;
        int HIGHER_INDEX = 2;

        HashMap<String, String[]> toReturn = new HashMap<String, String[]>();

        for (Comparable key : csvHashMap.keySet()) {
            String[] keyValue = csvHashMap.get(key);
            String keyValueString = "";

            String[] toExtract = new String[FIRST_N_ITEMS_TO_EXTRACT];

            toExtract[Y_INDEX] = keyValue[Y_INDEX];
            toExtract[LOWER_INDEX] = keyValue[LOWER_INDEX];
            toExtract[HIGHER_INDEX] = keyValue[HIGHER_INDEX];

            toReturn.put(key.toString(), toExtract);
        }

        return toReturn;
    }

    
    /**
     * Extracts item at index and returns it in nbCopies = 3 copies.
     * Imitates the form of extractYValueAndRange() given above.
     * @param csvHashMap
     * @param itemIndex
     * @return
     */
    public static HashMap<String, String[]> extractYValueAndRange(HashMap<Comparable, String[]> csvHashMap, int itemIndex) 
    {
        HashMap<String, String[]> toReturn = new HashMap<String, String[]>();
    
        int nbCopies = 3 ;

        String[] toExtract = new String[nbCopies] ;

        for (Comparable key : csvHashMap.keySet()) 
        {
            String[] keyValue = csvHashMap.get(key);
            String keyValueString ;
            if (itemIndex < keyValue.length)
            	keyValueString = keyValue[itemIndex] ;
            else
            	continue ;

            for (int index = 0 ; index < nbCopies ; index++ )
            {
            	LOGGER.log(Level.INFO,"first:{0} index:{1} value:{2}", (Object[]) keyValue) ;
            	LOGGER.info(String.valueOf(keyValue.length) + " " + String.valueOf(key)) ;
            	toExtract[index] = keyValueString ;
            }

            toReturn.put(key.toString(), toExtract.clone());
        }

        return toReturn;
    }

    
    /**
     * Sums residuals between the simulations given by simNames and the data in file
     * referenceFileName and sorts simNames accordingly.
     * Assumes that categoryName heads the first column.
     * @param simNames
     * @param categoryName
     * @param scoreNames
     * @param weight
     * @param reportName
     * @param referenceFileName
     * @param folderPath
     * @return
     */
    static public ArrayList<String> CLOSEST_SIMULATIONS(ArrayList<String> simNames, String categoryName, String[] scoreNames, double[] weight, String reportName, String folderPath, String referenceFileName, String referenceFolder)
    {
        ArrayList<String> sortedSimNames = new ArrayList<String>() ;

        HashMap<Double,ArrayList<String>> comparisonReport = new HashMap<Double,ArrayList<String>>() ;

        // Read in data to compare against
        HashMap<Comparable,String[]> referenceReport = READ_CSV_STRING(referenceFileName, referenceFolder) ;
        ArrayList<String> dataScoreNames = new ArrayList<String>() ;
        LOGGER.info(referenceReport.keySet().toString());
        Collections.addAll(dataScoreNames, referenceReport.get(categoryName)) ;
        String dataSuffix = "_wild" ;
        //int scoreIndex = dataScoreNames.indexOf(scoreName + dataSuffix) ;
        // These are to read in values from referenceReport
        String[] referenceValues ;
        String referenceValue ;
        double totalResidual = 0.0 ;

        for (String simulationName : simNames)
        {
            try
            {
                BufferedReader fileReader
                    = new BufferedReader(new FileReader(folderPath + reportName + "_" + simulationName + ".txt")) ;

                // Read input file of simulationName
                String fileLine = "" ; // fileReader.readLine() ;
                String fileLine2 = fileReader.readLine() ;

                while (fileLine2 != null)    // Read last line of file
                {
                    fileLine = fileLine2 ;
                    fileLine2 = fileReader.readLine() ;
                }
                fileReader.close() ;

                Double scoreValue ;
                Double residualSum = 0.0 ;

                //ArrayList<String> keyValues = new ArrayList<String>() ;
                int paramIndex = fileLine.indexOf("{",10) ;
                if (paramIndex < 0)
                    paramIndex = 0 ;

                int keyIndex = fileLine.indexOf("=",paramIndex) ;
                int spaceIndex = paramIndex ;
                int keyIndex2 ;

                //while (fileLine != null)
                while (keyIndex < fileLine.length())
                {
                    String categoryValue = fileLine.substring(spaceIndex + 1, keyIndex).strip() ;
                    keyIndex2 = fileLine.indexOf("=",keyIndex + 1) ;
                    if (keyIndex2 < 0)
                        keyIndex2 = fileLine.length() ;
                    String scoreString = fileLine.substring(keyIndex, keyIndex2) ;

                    keyIndex = keyIndex2 ;

                    // Select SPACE immediately before "="
                    spaceIndex = fileLine.lastIndexOf(SPACE, keyIndex) ;

                    // Initialise fileLine for Each categoryValue with "categoryValue"
                    if (!referenceReport.containsKey(categoryValue))
                        continue ;
                    referenceValues = referenceReport.get(categoryValue) ;
                    for (int weightIndex = 0 ; weightIndex < weight.length ; weightIndex++ )
                    {
                    String scoreName = scoreNames[weightIndex] ;
                    int scoreIndex = dataScoreNames.indexOf(scoreName + dataSuffix) ;

                    scoreValue = Double.valueOf(EXTRACT_VALUE(scoreName,scoreString)) ;
                    //LOGGER.log(Level.INFO, "year:{0} score:{1}", new Object[] {categoryValue,scoreString});


                    // Initialise fileLine for Each categoryValue with "categoryValue"
                    if (referenceValues.length <= (scoreIndex + 1))
                        continue ;

                    //LOGGER.info("referenceValues " + categoryValue);
                    // Check we have a comparison for categoryValue
                    referenceValue = referenceValues[scoreIndex] ;
                    if (referenceValue.isEmpty())
                        continue ;
                    Double referenceDouble = Double.valueOf(referenceValue) ;
                    residualSum += weight[weightIndex] * Math.pow(referenceDouble - scoreValue, 2) ;
                }
                }
                if (!comparisonReport.containsKey(residualSum))
                    comparisonReport.put(residualSum,new ArrayList<String>()) ;
                comparisonReport.get(residualSum).add(simulationName) ;
                totalResidual += residualSum ;
                //LOGGER.info(comparisonReport.toString()) ;
            }
            catch ( Exception e )
            {
                LOGGER.info(e.toString());
                LOGGER.info(simulationName) ;
                //simNames.remove(simIndex) ;
            }

        }
        // Sort keySet to get simNames in order of lowest sum-of-squares
        ArrayList<Double> sortedResidualSums = new ArrayList<Double>(comparisonReport.keySet()) ;
        Collections.sort(sortedResidualSums) ;
        
        //LOGGER.info(sortedResidualSums.toString());

        LOGGER.info(String.valueOf(totalResidual)) ;
        for (Double residual : sortedResidualSums)
            for (String simName : comparisonReport.get(residual))
                sortedSimNames.add(simName) ;

        return sortedSimNames ;

    }


    /**
     * Sums residuals between the simulations given by simNames and the data in file
     * referenceFileName and sorts simNames accordingly.
     * Assumes that categoryName heads the first column.
     * @param simNames
     * @param categoryName
     * @param scoreName
     * @param reportName
     * @param referenceFileName
     * @param folderPath
     * @return 
     */
    static public ArrayList<String> CLOSEST_SIMULATIONS(ArrayList<String> simNames, String categoryName, String scoreName, String reportName, String folderPath, String referenceFileName, String referenceFolder)
    {
        ArrayList<String> sortedSimNames = new ArrayList<String>() ;
        
        HashMap<Double,ArrayList<String>> comparisonReport = new HashMap<Double,ArrayList<String>>() ;
        
        // Read in data to compare against
        HashMap<Comparable,String[]> referenceReport = READ_CSV_STRING(referenceFileName, referenceFolder) ;
        ArrayList<String> scoreNames = new ArrayList<String>() ;
        Collections.addAll(scoreNames, referenceReport.get(categoryName)) ;
        String dataSuffix = "_wild" ;
        int scoreIndex = scoreNames.indexOf(scoreName + dataSuffix) ;
        // These are to read in values from referenceReport
        String[] referenceValues ;
        String referenceValue ;
        
        for (String simulationName : simNames)
        {
            try
            {
                BufferedReader fileReader 
                    = new BufferedReader(new FileReader(folderPath + reportName + "_" + simulationName + ".txt")) ;
                
                // Read input file of simulationName
                String fileLine = "" ; // fileReader.readLine() ;
                String fileLine2 = fileReader.readLine() ;
                //fileLine2 = fileReader.readLine() ;
                while (fileLine2 != null)    // Read last line of file
                {
                    fileLine = fileLine2 ;
                    fileLine2 = fileReader.readLine() ;
                }
                fileReader.close() ;
                
                Double scoreValue ;
                Double residualSum = 0.0 ;

                //ArrayList<String> keyValues = new ArrayList<String>() ;
                int paramIndex = fileLine.indexOf("{",10) ;
                if (paramIndex < 0)
                    paramIndex = 0 ;
                
                int keyIndex = fileLine.indexOf("=",paramIndex) ;
                int spaceIndex = paramIndex ;
                int keyIndex2 ;
                
                //while (fileLine != null)
                while (keyIndex < fileLine.length())
                {
                    String categoryValue = fileLine.substring(spaceIndex + 1, keyIndex) ;
                    keyIndex2 = fileLine.indexOf("=",keyIndex + 1) ;
                    if (keyIndex2 < 0)
                        keyIndex2 = fileLine.length() ;
                    String scoreString = fileLine.substring(keyIndex, keyIndex2) ;
                    
                    keyIndex = keyIndex2 ;
                    scoreValue = Double.valueOf(EXTRACT_VALUE(scoreName,scoreString)) ;
                    //LOGGER.log(Level.INFO, "year:{0} score:{1}", new Object[] {categoryValue,scoreString});
                    

                    // Select SPACE immediately before "="
                    spaceIndex = fileLine.lastIndexOf(SPACE, keyIndex) ;
                    // Initialise fileLine for Each categoryValue with "categoryValue"

                    //LOGGER.info("referenceValues " + categoryValue);
                    // Check we have a comparison for categoryValue 
                    if (!referenceReport.containsKey(categoryValue))
                        continue ;
                    referenceValues = referenceReport.get(categoryValue) ;
                    if (referenceValues.length <= (scoreIndex + 1))
                        continue ;
                    referenceValue = referenceValues[scoreIndex] ;
                    if (referenceValue.isEmpty())
                        continue ;
                    Double referenceDouble = Double.valueOf(referenceValue) ;
                    residualSum += Math.pow(referenceDouble - scoreValue, 2) ;
                }
                if (!comparisonReport.containsKey(residualSum))
                    comparisonReport.put(residualSum,new ArrayList<String>()) ;
                comparisonReport.get(residualSum).add(simulationName) ;
            }
            catch ( Exception e )
            {
                LOGGER.severe(e.toString());
                LOGGER.severe(simulationName) ;
                //simNames.remove(simIndex) ;
            }

        }
        
        // Sort keySet to get simNames in order of lowest sum-of-squares
        ArrayList<Double> sortedResidualSums = new ArrayList<Double>(comparisonReport.keySet()) ;
        Collections.sort(sortedResidualSums) ;
        
        for (Double residual : sortedResidualSums)
            for (String simName : comparisonReport.get(residual))
                sortedSimNames.add(simName) ;
        
        return sortedSimNames ;
        
    }
    
    /**
     * Reads in .csv files named in fileNames and creates a new .csv file
     * with contribution from each of them.
     * Currently takes only first column.
     * TODO: Allow specification property to be read and multiple files when multiple properties
     * @param fileNames - (ArrayList(String)) Names of files to be read
     * @param folderPath - (String) Name of folder containing files
     * @return (boolean) - true if successful, false otherwise
     */
    public static boolean MERGE_HASHMAP_CSV(ArrayList<String> fileNames, String reportName, String scoreName, String folderPath)
    {
        HashMap<Comparable<?>,Number[]> report = new HashMap<Comparable<?>,Number[]>() ;
        
        String fileName ;
        Comparable key ;
        int scoreIndex = -1 ;
        String[] firstLine = new String[] {} ;
        String[] firstOutput = new String[fileNames.size()] ;
        
        for (int fileIndex = 0 ; fileIndex < fileNames.size() ; fileIndex++ )
        {
        	try
        	{
        		fileName = fileNames.get(fileIndex) ;
        		//firstOutput[fileIndex] = fileName ;
        		
        		BufferedReader fileReader 
                = new BufferedReader(new FileReader(folderPath + fileName + reportName + CSV)) ;
        
                // Get first line
                String record = fileReader.readLine() ; // .substring(1) ;    // Remove invisible character at beginning of first entry
                String[] recordArray = record.split(COMMA) ;
                
                for (int recordIndex = 1 ; recordIndex < recordArray.length ; recordIndex++ )
                {
            		if (recordArray[recordIndex].equals(scoreName))
                	{
                		scoreIndex = recordIndex ; 
                		break ;
                	}
                }
                assert(scoreIndex >= 0) ;
                
                if (firstLine.length == 0)
                	firstLine = new String[] {recordArray[0], fileName} ;
            
        
                record = fileReader.readLine() ;
                recordArray = record.split(COMMA) ;
                
                // report.put(Reporter.COLUMN_NAME, recordArray);

                while (recordArray.length > 1)    // ((record != null) && (!record.isEmpty()))
                {
                	key = recordArray[0] ;
            	    if (!report.containsKey(key))
            	    	report.put(key, new Number[fileNames.size()]) ;
            	    
            	    Number[] valueArray = report.get(key).clone() ;
                //if (recordArray.length > 1)
                {   
                	valueArray[fileIndex] = Double.valueOf(recordArray[scoreIndex]) ;
                    report.put(key, valueArray) ;
                }
                //else
                  //  report.put(key, new String[0]) ;

                    record = fileReader.readLine() ;
                    if (record != null)
                        recordArray = record.split(COMMA) ;
                    else
                    	recordArray = new String[] {} ;
            
                }
                fileReader.close() ;

        		firstOutput[fileIndex] = fileName ;
        	}
            catch ( Exception e )
            {
            	LOGGER.severe(e.toString()) ;
            	//return false ;
            }
    	}
        
         // Prepare to write merged file to disk
        fileName = fileNames.get(0) ;
        //fileName = fileName.substring(0, fileName.indexOf(CSV)) ;
        reportName += "_" + scoreName ;
        
        
        return WRITE_CSV(report, firstLine[0], firstOutput, "Merged" + reportName , fileName, DATA_FOLDER) ;
        
        }
        		
        
     /**
      * Remove and adjusts David's code to handle all such situations.
     * @param inputReport
     * @return
     */
    public static HashMap<Comparable<?>,Number[]> PREPEND_YANDRANGE(HashMap<Comparable<?>,String[]> inputReport)
    {
    	HashMap<Comparable<?>,Number[]> outputReport = new HashMap<Comparable<?>,Number[]>() ;
    	
    	int VALUES_TO_ADD = 3; // y-value, lower, upper
        Number[] medianAndRange = new Number[VALUES_TO_ADD];

    	for (Comparable<?> categoryValue : inputReport.keySet() ) 
        {
    		LOGGER.info(categoryValue.toString()) ;
            //String valuesCommaSeparatedString = inputReport.get(categoryValue) ;

            // extract information into an arraylist, remove leading and trailing whitespace before and after a comma
            String[] valuesArray = inputReport.get(categoryValue) ;    // valuesCommaSeparatedString.split("\\s*,\\s*"); // split on comma and leading/trailing spaces
            ArrayList<String> valuesArrayList = new ArrayList<String>(Arrays.asList(valuesArray));
            LOGGER.info(valuesArrayList.toString()) ;
            ArrayList<Number> numberArrayList = new ArrayList<Number>() ;
            //for (String valueString : valuesArrayList )
            //	numberArrayList.add(Double.valueOf(valueString)) ;
            LOGGER.info(numberArrayList.toString()) ;
            
            //String year = valuesArrayList.remove(0); // remove the year and store it in a variable
            ArrayList<Double> sortedValues = Reporter.convertArrayListStringToSortedArrayListDouble(valuesArrayList);
            
            // calculate standard deviation of the interdecile range of values
            // this will hopefully remove some outliers so this standard deviation represents 
            // most of the data (outliers do not factor in the calculations of this number)
            ArrayList<Double> sortedValuesInterdecile = new ArrayList<Double>();
            for (Double value : sortedValues) 
            	sortedValuesInterdecile.add(value);
            Reporter.removeOutliersFromSortedArrayListPercentileMethod(sortedValuesInterdecile, 0.25);        
            Double numValues = (double) sortedValuesInterdecile.size();
            Double meanValue = 0.0;
            for (int i = 0; i < sortedValuesInterdecile.size(); ++i) 
            	meanValue += sortedValuesInterdecile.get(i) ;
            meanValue /= numValues ;
            Double standardDeviation = Reporter.getStandardDeviationDoubleValueFromArrayListOfDoubles(sortedValuesInterdecile, meanValue) ;
            
            // transform sorted data by removing outliers
            // Reporter.removeOutliersFromSortedArrayListPercentileMethod(sortedValues, 0.10);
            Reporter.removeOutliersFromSortedArrayListDeviationMethod(sortedValues, meanValue, standardDeviation);

            LOGGER.info(sortedValues.toString()) ;
            // get the median from a sorted array list
            Double medianValue = Reporter.extractMedianFromSortedArrayList(sortedValues);

            numberArrayList.add(medianValue) ;
            numberArrayList.add(sortedValuesInterdecile.get(sortedValuesInterdecile.size() - 1)) ;
            numberArrayList.add(sortedValuesInterdecile.get(0)) ;
                        // // calculate confidence intervals
            // Double[] confidenceInterval = Reporter.get95ConfidenceIntervalDoubleArray(  meanValue,
            //                                                                             standardDeviation,
            //                                                                             (double) nbSimulations);

            // insert year -> mean -> lower -> upper into array list
            // this then gets converted back into a comma separated string
            //valuesArrayList.add(0, year);  // add year to start of values
            
            outputReport.put(categoryValue, (Number[]) numberArrayList.toArray(new Number[] {})) ;
            
        }
            
    return outputReport ;
        
        
    }
    
    /**
     * Read .csv file and return its contents as a HashMap where String maps to 
     * String[]. We will also skip the first rowsToSkip lines.
     * No safety implemented, method assumes rowsToSkip will be less than
     * the total number of lines in the file.
     * 
     * @param fileName
     * @param folderPath
     * @param rowsToSkip: rowsToSkip < number of lines
     * @return 
     */
    public static HashMap<Comparable, String[]> READ_CSV_STRING(String fileName, String folderPath, int rowsToSkip)
    {
        HashMap<Comparable,String[]> report = new HashMap<Comparable,String[]>() ;
        
        String key ;
        
        try
        {
            BufferedReader fileReader 
                    = new BufferedReader(new FileReader(folderPath + fileName + CSV)) ;
            
            // Get first line
            String record = fileReader.readLine().substring(1) ;

            // Skip rowsToSkip number of lines:
            for (int i = 0; i < rowsToSkip; ++i) 
            {
                record = fileReader.readLine();
            }

            String[] recordArray = record.split(COMMA) ;
            // Remove invisible character at beginning of first key
            key = recordArray[0] ;
            
            // report.put(Reporter.COLUMN_NAME, recordArray);

            while ((record != null) && (!record.isEmpty()))
            {
                if (recordArray.length > 1)
                {   
                    String[] valueArray = new String[recordArray.length-1] ;
                    for (int index = 1 ; index < recordArray.length ; index++ )
                        valueArray[index-1] = recordArray[index] ;
                    report.put(key, valueArray) ;
                }
                else
                    report.put(key, new String[0]) ;

                record = fileReader.readLine() ;
                if (record != null)
                {
                    recordArray = record.split(COMMA) ;
                    key = recordArray[0] ;
                }
                
            }
            fileReader.close() ;
        }
        catch ( Exception e )
        {
            LOGGER.severe(e.toString());
        }
     
        return report ;
    }


    /**
     * Read .csv file and return its contents as a HashMap where String maps to 
     * String[] .
     * @param fileName
     * @param folderPath
     * @return 
     */
    public static HashMap<Comparable,String[]> READ_CSV_STRING(String fileName, String folderPath)
    {
        return READ_CSV_STRING(fileName, folderPath, 0);
        // HashMap<Comparable,String[]> report = new HashMap<Comparable,String[]>() ;
        
        // String key ;
        
        // try
        // {
        //     BufferedReader fileReader 
        //             = new BufferedReader(new FileReader(folderPath + fileName + CSV)) ;
            
        //     // Find last line
        //     String record = fileReader.readLine() ;  
        //     String[] recordArray = record.split(COMMA) ;
        //     // Remove invisible character at beginning of first key
        //     key = recordArray[0].substring(1) ;
                
        //     while ((record != null) && (!record.isEmpty()))
        //     {
        //         if (recordArray.length > 1)
        //         {
        //             String[] valueArray = new String[recordArray.length - 1] ;
        //             for (int index = 1 ; index < recordArray.length ; index++ )
        //                 valueArray[index - 1] = recordArray[index] ;
        //             report.put(key, valueArray) ;
        //         }
        //         else
        //             report.put(key, new String[0]) ;

        //         record = fileReader.readLine() ;
        //         if (record != null)
        //         {
        //             recordArray = record.split(COMMA) ;
        //             key = recordArray[0] ;
        //         }
                
        //     }
        //     fileReader.close() ;
        // }
        // catch ( Exception e )
        // {
        //     LOGGER.info(e.toString());
        // }
     
        // return report ;
    }


    /**
     * Stores a (HashMap of String records) report as a .csv file for other packages to read.
     * @param report
     * @param categoryName
     * @param categoryList
     * @param reportName
     * @param simName
     * @param folderPath 
     */
    static public boolean WRITE_CSV_STRING(HashMap<Comparable<?>,String> report, String categoryName, ArrayList<Comparable<?>> categoryList, String reportName, String simName, String folderPath)
    {
        HashMap<Comparable<?>,Number[]> newReport = new HashMap<Comparable<?>,Number[]>() ;
        
        String scoreString ;
        String reportValue ;
        Number scoreValue ;
        ArrayList<String> propertyList = IDENTIFY_PROPERTIES(report.values().iterator().next()) ;
        String[] propertyArray = new String[propertyList.size()] ;
        boolean propertyConstructed = false ;
        for (Comparable key : report.keySet())
        {
            reportValue = report.get(key) ;
            
            Number[] numberArray = new Number[propertyList.size()] ;
            for (int propertyIndex = 0 ; propertyIndex < numberArray.length ; propertyIndex++ )
            {
                scoreString = EXTRACT_VALUE(propertyList.get(propertyIndex),reportValue) ;
                if (int.class.isInstance(scoreString) || Integer.class.isInstance(scoreString)) 
                    scoreValue = Integer.valueOf(scoreString) ;
                else
                    scoreValue = Double.valueOf(scoreString) ;
                numberArray[propertyIndex] = scoreValue ;
                
                if (!propertyConstructed)
                    propertyArray[propertyIndex] = propertyList.get(propertyIndex) ;
            }
            newReport.put(key, numberArray) ;
        }
        
        return WRITE_CSV(newReport, categoryName, propertyArray, categoryList, reportName, simName, folderPath) ;
    }

    /**
     * Stores a (HashMap of String records) report as a csv file for other packages to read.
     * @param report
     * @param categoryName
     * @param reportName
     * @param simName
     * @param folderPath 
     * @return  true if file is written successfully and false otherwise.
     */
    static public boolean WRITE_CSV_STRING(HashMap<Comparable<?>,String> report, String categoryName, String reportName, String simName, String folderPath)
    {
        return WRITE_CSV_STRING(report, categoryName, new ArrayList<Comparable<?>>(), reportName, simName, folderPath) ;
    }
    
    static public boolean DUMP_OUTPUT(String reportName, String simName, String folderPath, Object dumpReport)
    {
        String fileName = reportName + "_" + simName + ".txt" ;
        try
        {
            BufferedWriter metadataWriter = new BufferedWriter(new FileWriter(folderPath + fileName,true)) ;
            metadataWriter.write(dumpReport.toString()) ;
            metadataWriter.newLine() ;
            metadataWriter.close() ;
        }
        catch ( Exception e )
        {
            LOGGER.severe(e.toString()) ;
            return false ;
        }
        return true ;
    }
    
    
    /**
     * 
     * @param reportList
     * @param reportName
     * @param simName
     * @param folderPath
     * @return (boolean) true if save was successful and false otherwise.
     */
    static public boolean WRITE_CSV_DISTRIBUTION(ArrayList<HashMap<Comparable<?>,String>> reportList, String reportName, String simName, String folderPath)
    {
        HashMap<Comparable,ArrayList<String>> distribution = new HashMap<Comparable,ArrayList<String>>() ;
        
        ArrayList<String> properties = new ArrayList<String>() ;
        // One line in .csv file per report
        
        
        // Put keys (years) in order. 
        //TODO: Upgrade to source 8 and more general Comparator keyComparator = Comparator.naturalOrder() ;
        ArrayList<String> reportKeys = new ArrayList<String>() ;
        HashMap<Comparable<?>,String> firstReport = reportList.get(0) ;
        properties = IDENTIFY_PROPERTIES(firstReport.get(firstReport.keySet().iterator().next())) ;
        for (Comparable key : firstReport.keySet())
            reportKeys.add(String.valueOf(key)) ;
        reportKeys.sort(String.CASE_INSENSITIVE_ORDER) ;

        // Generate column names
        //for (String property : properties)
          //      colNames.add(property) ;
        
        
        String filePath = folderPath + simName + "_distribution.csv";
        String fileHeader = "year," + String.join(COMMA, properties) ;
           
        //Write to file while cycling through each report.
        // Construct output distribution with minimal calls to reportList
        for (int reportIndex = 0 ; reportIndex < reportList.size() ; reportIndex++ )
        {
            HashMap<Comparable<?>,String> report = reportList.get(reportIndex) ;
            //Collections<Comparable> yearKeys =  report.keySet()
            for (Comparable year : report.keySet())
            {
                String yearlyOutput = report.get(year) ;

                ArrayList<String> reportValues ;
                if (distribution.containsKey(year))
                    reportValues = distribution.get(year) ;
                else
                {
                    reportValues = new ArrayList<String>(Arrays.asList(new String[reportList.size() * properties.size() + 1])) ;
                    reportValues.set(0, year.toString()) ;
                }
                for (int propertyIndex = 0 ; propertyIndex < properties.size() ; propertyIndex++ )
                {
                    int recordIndex = reportIndex + propertyIndex * reportList.size() + 1 ;
                    reportValues.set(recordIndex,EXTRACT_VALUE(properties.get(propertyIndex),yearlyOutput)) ;
                }

                distribution.put(year, ((ArrayList<String>) reportValues.clone())) ;
            }

                //for (String entry : (ArrayList<String>) record)
        }
        try
        {
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(filePath,false));
            fileWriter.write(fileHeader) ;
            fileWriter.newLine();
            for (Comparable year : distribution.keySet())
            {
                String line = String.join(COMMA, distribution.get(year)) ;
                fileWriter.write(line) ;
                fileWriter.newLine() ;
            }
            fileWriter.close() ;
        }
        catch( Exception e )
        {
            LOGGER.severe(e.toString()) ;
            return false ;
        }
        
        return true ;
    }
    
    /**
     * TODO: Unit test
     * @param record
     * @return (ArrayList) String names of properties with given values in record.
     */
    static public ArrayList<String> IDENTIFY_PROPERTIES(String record)
    {
        ArrayList<String> propertyArray = new ArrayList<String>() ;
        
        int colonIndex = record.indexOf(":") ;
        int spaceIndex ;
        int nextColonIndex ;
        int propertyIndex = 0 ;
        
        while (colonIndex > 0)
        {
            nextColonIndex = record.indexOf(":",colonIndex+1) ;
            spaceIndex = record.lastIndexOf(" ",nextColonIndex + 1) ;
            if  (spaceIndex > colonIndex || nextColonIndex < 0)
            {
                propertyArray.add(record.substring(propertyIndex, colonIndex)) ;
                propertyIndex = spaceIndex + 1 ;
            }
            else
                propertyIndex = colonIndex + 1 ;
            colonIndex = nextColonIndex ;
        }
        return propertyArray ;
    }
    
    /**
     * TODO: Unit test
     * @param record
     * @return (ArrayList) String names of labels without given values in record.
     */
    static public ArrayList<String> IDENTIFY_LABELS(String record)
    {
        ArrayList<String> labelArray = new ArrayList<String>() ;
        
        int colonIndex = record.indexOf(":") ;
        int spaceIndex ;
        int nextColonIndex ;
        int propertyIndex = 0 ;
        
        while (colonIndex > 0)
        {
            nextColonIndex = record.indexOf(":",colonIndex+1) ;
            spaceIndex = record.indexOf(" ",colonIndex) ;
            if  (spaceIndex < nextColonIndex || nextColonIndex < 0)
            {
                propertyIndex = spaceIndex + 1 ;
            }
            else
            {
                labelArray.add(record.substring(propertyIndex, colonIndex)) ;
                propertyIndex = colonIndex + 1 ;
            }
            colonIndex = nextColonIndex ;
        }
        return labelArray ;
    }
    
    /**
     * 
     * @param label
     * @param record
     * @return (String) Properties and their values under a given label.
     */
    static public String EXTRACT_LABEL_STRING(String label, String record)
    {
        return  EXTRACT_LABEL_STRING(label, record, IDENTIFY_LABELS(record)) ;
    }
    
    
    /**
     * 
     * @param label
     * @param record
     * @param labelList (ArrayList) of possible labels.
     * @return (String) Properties and their values under a given label.
     */static public String EXTRACT_LABEL_STRING(String label, String record, ArrayList<String> labelList)
    {
        ArrayList<String> loopList = (ArrayList<String>) labelList.clone() ;
        loopList.remove(label) ;
        int labelIndex = record.indexOf(label) + label.length() + 1 ;
        int listIndex = record.length() ;
        int nextIndex = labelIndex ;
        
        for (String otherLabel : loopList)
        {
            nextIndex = record.indexOf(otherLabel, labelIndex) ;
            if ((nextIndex < listIndex) && (nextIndex > 0))
                listIndex = nextIndex ;
        }
        
        return record.substring(labelIndex, listIndex) ;
    }
    
    /**
     * Averages value of propertyName over (ArrayList) reports
     * @param reports
     * @param propertyName
     * @return (ArrayList) AVERAGED_REPORT
     */
    static public ArrayList<String> AVERAGED_REPORT(ArrayList<ArrayList<String>> reports, String propertyName)
    {
        ArrayList<String> propertyNames = new ArrayList<String>() ;
        propertyNames.add(propertyName) ;
        return AVERAGED_REPORT(reports, propertyNames) ;
    }
    
    /**
     * Averages value of propertyName over (ArrayList) reports
     * @param reports
     * @param propertyNames
     * @return (ArrayList) AVERAGED_REPORT
     */
    static public ArrayList<String> AVERAGED_REPORT(ArrayList<ArrayList<String>> reports, ArrayList<String> propertyNames)
    {
        ArrayList<String> averagedReport = new ArrayList<String>() ;
        
        String record ;
        String meanRecord ;
        double meanValue ;
        
        int nbRecords = reports.get(0).size() ;
        int nbReports = reports.size() ;
        
        ArrayList<String> meanProperties ;
        if (propertyNames.isEmpty())
            meanProperties = IDENTIFY_PROPERTIES((String) reports.get(0).get(0)) ;
        else
            meanProperties = propertyNames ;
        
        for (int cycle = 0 ; cycle < (nbRecords) ; cycle++ )
        {
            meanRecord = "" ;
            for (String property : meanProperties)
            {
                meanValue = 0.0 ;
                for (ArrayList<String> report : reports)
                {
                    record = (String) report.get(cycle) ;
                    meanValue += Double.valueOf(EXTRACT_VALUE(property,record)) ;
                }
                meanValue = meanValue/nbReports ;
                meanRecord += ADD_REPORT_PROPERTY(property,meanValue) ;
            }
            averagedReport.add(meanRecord) ;
        }
      // logger.log(level.info, "{0}", averagedReport);
        return averagedReport ;
    }

    /**
     * Averages over (Number[]) entries in (ArrayList) reports
     * @param reports 
     * @return (HashMap) AVERAGED_REPORT
     */
    static public HashMap<Object,Number[]> AVERAGED_HASHMAP_REPORT(ArrayList<HashMap<Object,Number[]>> reports)
    {
        HashMap<Object,Number[]> averagedReport = new HashMap<Object,Number[]>() ;
        
        double nbReports = reports.size() ;
        double meanValue ;
        int nbEntries = 0 ;
        
        HashMap<Object,Number[]> sampleReport = reports.get(0) ;
        nbEntries = sampleReport.values().iterator().next().length ;
        
        for (Object key : sampleReport.keySet())
        {
            Number[] meanRecord = new Number[nbEntries] ;
            for (int entry = 0 ; entry < nbEntries ; entry++ )
            {
                meanValue = 0.0 ;
                for (HashMap<Object,Number[]> report : reports)
                    meanValue += report.get(key)[entry].doubleValue() ;
                meanRecord[entry] = meanValue/nbReports ;
            }
            averagedReport.put(key,(Number[]) meanRecord.clone()) ;
        }
        
        return averagedReport ;
    }

    /**
     * Averages over (Number) entries in (ArrayList) reports
     * @param reports 
     * @return (HashMap) AVERAGED_REPORT
     */
    static public HashMap<Comparable,Number> MEAN_HASHMAP_REPORT(ArrayList<HashMap<Comparable,Number>> reports)
    {
        HashMap<Comparable,Number> averagedReport = new HashMap<Comparable,Number>() ;
        
        double nbReports = reports.size() ;
        double meanValue ;
        
        //HashMap<Object,Number> sampleReport = reports.get(0) ;
        
        ArrayList<Comparable> keyList = new ArrayList<Comparable>() ;
        for (HashMap<Comparable,Number> report : reports)
            for (Comparable key : report.keySet())
                if (!keyList.contains(key))
                    keyList.add(key) ;
        
        
        for (Comparable key : keyList)
        {
            meanValue = 0.0 ;
            for (HashMap<Comparable,Number> report : reports)
                if (report.containsKey(key) && report.get(key) != null)
                    meanValue += report.get(key).doubleValue() ;
            averagedReport.put(key,meanValue/nbReports) ;
        }
        return averagedReport ;
    }
    
    /**
     * Converts an ArrayList year-by-year report to a HashMap report
     * @param arrayReport
     * @param finalYear
     * @return 
     */
    static public HashMap<Comparable<?>,String> ARRAY_TO_HASHMAP(ArrayList<String> arrayReport, int finalYear)
    {
        HashMap<Comparable<?>,String> hashMapReport = new HashMap<Comparable<?>,String>() ;
                
        int nbYears = arrayReport.size() ;
        int yearKey = finalYear ;
        String yearOutput ;
        for (int year = nbYears - 1 ; year >= 0 ; year-- )
        {
            yearOutput = arrayReport.get(year) ;
            // Trim [] from Output
            yearOutput = yearOutput.substring(1, yearOutput.length() - 1) ;
            
            hashMapReport.put(yearKey,yearOutput) ;
            yearKey-- ;
        }
        
        return hashMapReport ;
    }

    public Reporter()
    {
	    // Needed to work around HPC access issues.
        reader = new Reader() ;
    }
    
    public Reporter(String simname, ArrayList<String> report)
    {
        input = report ;
        simName = simname ;
        //this.generateReports = generateReports ;
        //this.encounterReports = encounterReports ;
        //this.clearReports = clearReports ;
        //this.screenReports = screenReports ;
    }
    
    public Reporter(String simname, String fileName)
    {
        float t0 = System.nanoTime();
        initReporter(simname, fileName) ;
        float t1 = System.nanoTime();
        Community.RECORD_METHOD_TIME("Reporter.CONSTRUCTOR", t1 - t0);
    }

    /**
     * Initialises Reporter from saved simulation files. Allows for easier
     * construction using reflection.
     * @param simname
     * @param fileName 
     */
    protected final void initReporter(String simname, String fileName)
    {
        simName = simname ;
        String reporterName = this.getClass().asSubclass(this.getClass()).getSimpleName().toLowerCase() ;
        // What sort of Reporter is this? Needed to identify files.
        reporterName = reporterName.substring(0,reporterName.lastIndexOf("reporter")) ;
        reader = new Reader(simname, reporterName, fileName) ;
        input = reader.updateOutputArray() ;
        
    }
    
    /**
     * Loads the next Report file into input if there is another file to read.
     * Resets fileIndex to 0 if there is not.
     * @return true if update successful, false if all files have already been read.
     */
    protected boolean updateReport()
    {
        //LOGGER.info(String.valueOf(reader.fileIndex) + reader.fileNames.get(reader.fileIndex)) ;
        if (reader.fileIndex >= reader.fileNames.size())
        {
            //reader.fileIndex = 0 ;    // This may cause problems somewhere, possibly unnecessary
            input = reader.updateOutputArray() ;
            return false ;
        }
        input = reader.updateOutputArray();
        return true ;
    }
    
    /**
     * 
     * @param backCycles
     * @param endCycle
     * @return Report of backCycles records leading up to endCycle'th
     */
    protected ArrayList<String> getBackCyclesReport(int backCycles, int endCycle)
    {
        return reader.getBackCyclesReport(backCycles, endCycle) ;
    }
        
    /**
     * Resets reader to first input file before returning.
     * @return Last saved file of reader.
     */
    protected ArrayList<String> getFinalReport()
    {
        ArrayList<String> finalReport = reader.getFinalReport() ;
        updateReport() ;
        return finalReport ;
    }
    
    /**
     * Reads value from corresponding METADATA file.
     * @return (int) the total number of cycles in the corresponding simulation.
     */
    public int getMaxCycles()
    {
        return Integer.valueOf(getMetaDatum("Community.MAX_CYCLES")) ;
    }
    
    /**
     * Reads value from corresponding METADATA file.
     * @return (int) the total number of cycles in the corresponding simulation.
     */
    public int getPopulation()
    {
        return Integer.valueOf(getMetaDatum("Community.POPULATION")) ;
    }
    
    /**
     * Reads String from METADATA file, strips square brackets, and splits into
     * String[] at "," .
     * @return (String[]) names of Sites included in simulation.
     */
    protected String[] getSiteNames()
    {
        //Read (String) line in METADATA file
        String nameString = getMetaDatum("Agent.SITE_NAMES") ;
        int leftIndex = nameString.indexOf("[") ;
        int rightIndex = nameString.indexOf("]") ;
        
        // Convert to String[] and remove whitespace
        String[] nameArray = nameString.substring(leftIndex, rightIndex).split(COMMA) ;
        for (int nameIndex = 0 ; nameIndex < nameArray.length ; nameIndex++ )
            nameArray[nameIndex] = nameArray[nameIndex].trim() ;
            
        return nameArray ;
    }
    
    protected ArrayList<String> getBackCyclesReport(int backYears, int backMonths, int backDays)
    {
        int backCycles = getBackCycles(backYears, backMonths, backDays) ;
        return reader.getBackCyclesReport(backCycles) ;
    }
    
    protected ArrayList<String> getBackCyclesReport(int backYears, int backMonths, int backDays, int endCycle)
    {
        int backCycles = GET_BACK_CYCLES(backYears, backMonths, backDays, endCycle) ;
        
        return reader.getBackCyclesReport(backCycles,endCycle) ;
    }
    
    protected int getBackCycles(int backYears, int backMonths, int backDays)
    {
        int maxCycles = getMaxCycles() ; 
        
        return GET_BACK_CYCLES(backYears, backMonths, backDays, maxCycles) ;
    }
    
    
    /**
     * 
     * @return (String) Path to folder with input files from simulation.
     */
    public String getFolderPath()
    {
        return reader.getFolderPath() ;
    }
    
    /**
     * 
     * @return (ArrayList(String)) complete input from concatenation of all
     * input files.
     */
    public ArrayList<String> getFullInput()
    {
        ArrayList<String> fullInput = (ArrayList<String>) input.clone() ;
        
        while(updateReport())
            fullInput.addAll((ArrayList<String>) input.clone()) ;
        return fullInput ;
    }
    
    /**
     * FIXME: only compatible with Reporter initiated from file.
     * @return opening record of opening input file 
     */
    public String getInitialRecord()
    {
        String initialRecord = "" ;
        try
        {
            initialRecord = reader.getInitialRecord() ;
        }
        catch( Exception e )
        {
            LOGGER.severe(e.toString()) ;
        }
        return initialRecord ;
    }
    
    /**
     * FIXME: only compatible with Reporter initiated from file.
     * @return final record of final input file 
     */
    public String getFinalRecord()
    {
        String finalRecord = "" ;
        try
        {
            finalRecord = reader.getFinalRecord() ;
        }
        catch( Exception e )
        {
            LOGGER.severe(e.toString()) ;
        }
        return finalRecord ;
    }
    
    /**
     * 
     * @param metaDatum
     * @return (String) value of metaDatum from reader.metaData .
     */
    public String getMetaDatum(String metaDatum)
    {
        return reader.getMetaDatum(metaDatum).trim() ;
    }
    
    /**
     * 
     * @param recordNb
     * @return output[reportNb] or error String if not available
     */
    protected String presentRecord(int recordNb)
    {
        if (recordNb < output.size())
            return output.get(recordNb) ;

        String message = "Requested cycle " + Integer.toString(recordNb) + "unavailable" ;
        return message ;
    }
    
    /**
     * If report reportName has already been found and stored then return it.
     * Otherwise, find prepareReport method, prepare the report and store it in
     * Reporter.reportList for later. Used when there are no parameters.
     * @param reportName
     * @param reporter
     * @return reporter.report specified by reportName
     */
    public Object getReport(String reportName, Reporter reporter)
    {
        return getReport(reportName, reporter, new Class[] {}, new Object[] {}) ;
    }
    
    
    /**
     * If report reportName has already been found and stored then return it.
     * Otherwise, find prepareReport method, prepare the report and store it in
     * Reporter.reportList for later.
     * @param reportName
     * @param reporter
     * @param parametersClazzes
     * @param parameters
     * @return (Object) reporter.report specified by reportName
     */
    public Object getReport(String reportName, Reporter reporter, Class[] parametersClazzes, Object[] parameters)
    {
        if (REPORT_LIST.containsKey(reportName))
        {
            //LOGGER.info("recall " + reportName) ;
            return REPORT_LIST.get(reportName) ;
        }
        
        String methodName = reportName.substring(0, 1).toUpperCase() 
                + reportName.substring(1) ;
        methodName = "prepare" + methodName + "Report" ;
        
        return getReport(reportName, methodName, reporter, parametersClazzes, parameters) ;
    }
    
    /**
     * Invokes Method methodName
     * @param reportName
     * @param methodName
     * @param reporter
     * @param parametersClazzes
     * @param parameters
     * @return (Object) reporter.report specified by reportName
     */
    public Object getReport(String reportName, String methodName, Reporter reporter, Class[] parametersClazzes, Object[] parameters)
    {
        Object report = new Object() ;
        
        Class reporterClazz = reporter.getClass().asSubclass(Reporter.class) ;
        //LOGGER.info("prepare " + reportName) ;
        try
        {
            // Call prepareReport()
            Method prepareMethod = reporterClazz.getDeclaredMethod(methodName, parametersClazzes) ;
            report = prepareMethod.invoke(reporter, parameters) ;
            // Save in REPORT_LIST for later retrieval
            REPORT_LIST.put(reportName, report) ;
            //LOGGER.log(Level.INFO, "{0}", REPORT_LIST.keySet());
        }
        catch ( Exception e )
        {
            LOGGER.severe(e.toString());
        }
        //LOGGER.log(Level.INFO, "{1} {0}", new Object[] {report,reportName}) ;
        
        return report ;
    }
    
    /**
     * If report reportName has already been found and stored then return it.
     * Otherwise, find prepareReport method, prepare the report and store it in
     * Reporter.REPORT_LIST for later.
     * @param recordName
     * @param reporter
     * @param parametersClazzes
     * @param parameters
     * @return reporter.report specified by reportName
     */
    public Object getRecord(String recordName, Reporter reporter, Class[] parametersClazzes, Object[] parameters)
    {
        if (REPORT_LIST.containsKey(recordName))
        {
            //LOGGER.info("recall " + recordName) ;
            return REPORT_LIST.get(recordName) ;
        }
        
        String methodName = recordName.substring(0, 1).toUpperCase() 
                + recordName.substring(1) ;
        methodName = "prepare" + methodName + "Record" ;
        
        return getReport(recordName, methodName, reporter, parametersClazzes, parameters) ;
    }
    
    
    /**
     * Adds filter to Reporter.
     * @param name
     * @param value 
     */
    protected void addFilter(String name, String value)
    {
        filterPropertyNames.add(name) ;
        filterPropertyValues.add(value) ;
    }
    
    /**
     * Removes from each Record those bounded substrings either missing required 
     * property or with incorrect value for that property.
     * TODO: Implement Arrays of values.
     * @param rawReport
     * @return 
     */
    protected ArrayList<String> applyFilters(ArrayList<String> rawReport)
    {
        ArrayList<String> filteredReport = new ArrayList<String>() ;
        
        String bound = getFilterBound(rawReport.get(0)) ;
        
        for (int filterIndex = 0 ; filterIndex < filterPropertyNames.size() ; filterIndex++ )
        {
            String filterName = filterPropertyNames.get(filterIndex) ;
            String filterValue = filterPropertyValues.get(filterIndex) ;
            if (filterValue.isEmpty())    // Require contents only
                for (String record : rawReport)
                    filteredReport.add(BOUNDED_STRING_BY_CONTENTS(filterName,bound,record)) ;
            else    // Property filterName required to have filterValue
                for (String record : rawReport)
                    filteredReport.add(BOUNDED_STRING_BY_VALUE(filterName, filterValue, bound, record)); // Filter record // Filter record
        }
        return filteredReport ;
    }
    
    /**
     * 
     * @param rawRecord - Usually first record in Report to be filtered.
     * @return 
     */
    protected String getFilterBound(String rawRecord)
    {
        String bound ;
        int commaIndex = rawRecord.indexOf(",") ;
        int spaceIndex = rawRecord.indexOf(" ") ;
        int colonIndex1 = rawRecord.indexOf(":") ;
        int colonIndex2 = rawRecord.indexOf(":",colonIndex1+1) ;
        if (colonIndex2 > spaceIndex)
            bound = rawRecord.substring(commaIndex+1, colonIndex1) ;
        else    // spaceIndex > colonIndex2
            bound = rawRecord.substring(colonIndex1+1, colonIndex2) ;
        return bound ;
    }

    /**
     * getter() for simName.
     * @return (String) simName
     */
    public String getSimName()
    {
        return simName ;
    }

    /**
     * 
     * @param fileName
     * @param filePath
     * @return
     */
    public static HashMap<String, Long> parseSeedsFromMetadata(String fileName, String filePath) {
        
        HashMap<String, Long> toReturn = new HashMap<String, Long>();
        BufferedReader reader;
        String STOP_READING = "Relationship.BURNIN_COMMENCE:";

        Set<String> seedTypes = new HashSet<String>(
            Arrays.asList("Community.REBOOT_SEED", "Agent.REBOOT_SEED", "Site.REBOOT_SEED", "Relationship.REBOOT_SEED"));

        try {
            reader = new BufferedReader(new FileReader(filePath+fileName+"-METADATA.txt"));
            String line = reader.readLine();
            while (line != null) {
                if (line.startsWith(STOP_READING)) break;
                boolean colonExists = line.contains(":");
                if (colonExists) {
                    int colonIndex = line.indexOf(":");
                    String extractedType = line.substring(0, colonIndex);
                    if (seedTypes.contains(extractedType)) {
                        toReturn.put(extractedType, Long.valueOf(line.substring(colonIndex + 1, line.length()).trim() ));
                    }
                }
                line = reader.readLine();
            }

        } catch (IOException e) {
            LOGGER.severe(e.toString());
        }
        return toReturn;
    }

    /**
     * 
     * Reads all lines up to Relationship.BURNIN
     * separate key:value pairs
     * save this into a hashmap of type hashmap<str, long>
     */
    public static HashMap<String, Long> PARSE_INFORMATION_FROM_METADATA(String fileName, String filePath) {
        HashMap<String, Long> toReturn = new HashMap<String, Long>();
        BufferedReader reader;
        String STOP_READING = "Relationship.BURNIN_COMMENCE:";

        Set<String> information = new HashSet<String>(Arrays.asList("Community.MAX_CYCLES"));

        try {
            reader = new BufferedReader(new FileReader(filePath+fileName+"-METADATA.txt"));
            String line = reader.readLine();
            while (line != null) {
                if (line.startsWith(STOP_READING)) break;
                boolean colonExists = line.contains(":");
                if (colonExists) {
                    int colonIndex = line.indexOf(":");
                    String extractedType = line.substring(0, colonIndex);
                    if (information.contains(extractedType)) {
                        toReturn.put(extractedType, Long.valueOf(line.substring(colonIndex + 1, line.length()).trim() ));
                    }
                }
                line = reader.readLine();
            }

        } catch (IOException e) {
            LOGGER.severe(e.toString());
        }
        return toReturn;
    }

    /**
    * @param originalPath
    * @param originalFileName
    * @param newPath
    * @param newFileName
    * @param modifications
    */
    public static void DUPLICATE_METADATA_WITH_MODIFIED_PROPERTIES
        (String originalPath, String originalFileName, String newPath,
        String newFileName, HashMap<String, String> modifications)
    {
    	originalFileName = originalPath + originalFileName + "-METADATA.txt";
        newFileName = newPath + newFileName + "-METADATA.txt";
        DUPLICATE_FILE_WITH_MODIFIED_PROPERTIES(originalFileName, newFileName, modifications);
    }

    /**
     * duplicate two files, modifying each key-value paired lines separated by ':'
     * based on a given hashmap of modifications
     * @param originalFile
     * @param newFile
     * @param modifications
     */
    public static void DUPLICATE_FILE_WITH_MODIFIED_PROPERTIES
        (String originalFile, String newFile, HashMap<String, String> modifications)
    {   

        // read the old file and add data to newFileText
        BufferedReader reader;
        String newFileText = "";
        try 
        {
            reader = new BufferedReader(new FileReader(originalFile));
            String line = reader.readLine();
            while (line != null) {
                boolean colonExists = line.contains(":");
                if (colonExists) {
                    int colonIndex = line.indexOf(":");
                    String extractedType = line.substring(0, colonIndex);
                    if (modifications.containsKey(extractedType)) {
                        newFileText += extractedType + ':' + String.valueOf(modifications.get(extractedType)) + '\n';
                    } else {
                        newFileText += line + '\n';
                    }
                }
                line = reader.readLine();
            }

        } 
        catch (IOException e) 
        {
            LOGGER.severe(e.toString());
        }

        // write new data in newFileText to newFile
        BufferedWriter fileWriter;
        try
        {                 
            fileWriter = new BufferedWriter(new FileWriter(newFile, false)) ;
            fileWriter.write(newFileText);
            fileWriter.close();
        }
        catch ( Exception e )
        {
            LOGGER.log(Level.SEVERE, e.toString()) ;
        }
    }

    /**
     * 
     * @param args 
     */
    public static void main(String[] args)
    {
        ConfigLoader.load() ;
        String simName = "" ;
        if (args.length > 0)
        	simName = args[0] ;
        boolean allReports = false ;
        boolean mergeReports = true ;
        boolean findReports = !mergeReports ;
        boolean findBest = false ;
        int cutoff = 50 ;
        String folderPath = "/srv/scratch/z3524276/prepsti/output/prep/" ;
        //String folderPath = "/srv/scratch/z3524276/prepsti/output/top50/" ;
        //String folderPath = "/srv/scratch/z3524276/prepsti/output/long_sims/" ;
	#boolean whole1000 = true ;
        #boolean selectBest = false ;
        String scoreName = "Pharynx_true" ;
        

        //String prefix = "to2019noAdjustCondom" ;
        String prefix = "Publish" ;
        //String prefix = "to2019serosortA" ;
        //String prefix = "from2015to2035prep123screenpublish" ;
        //String prefix = "to2030linearPrep154to2019noAdjustCondom" ;
        //String prefix = "from2015to2035prepRollout24from2015to2035prepRollout22from2015to2035prepRollout20publish" ;
        //String prefix = "from2015to2035noPreppublish" ;
        String prefix = "from2020to2035constantpublish" ;
        //String prefix = "from2015to2035prepNoTestpublish" ; // to2019noAdjustCondom" ;
        //String prefix = "from2015to2035screenNoPreppublish" ; // to2019noAdjustCondom" ;
        //String prefix = "from2015to2025constantto2019noAdjustCondom" ; 
        
        //String suffix = "" ;
        //String suffix = "Pop40000Cycles5110" ;
        //String suffix = "Pop40000Cycles6205" ;
        //String suffix = "Pop40000Cycles5840" ;
        //String suffix = "Pop40000Cycles7665" ;
        String suffix = "Pop40000Cycles4745" ;
        //String suffix = "Pop40000Cycles2190" ;
        
        //String reportName = "CumulativeInfectionsReport" ;
        //String reportName = "riskyIncidence" ;
        String reportName = "trueIncidenceCheck" ;
        String sortingProperty = "statusHIV" ;
        //String sortingProperty = "prepStatus" ;
        reportName += "_" + sortingProperty ;
        String scoreName = "all_true" ;
        
        ArrayList<String> simNameList = new ArrayList<String>() ;
        int END_YEAR = 2035 ;
        int START_YEAR = 2020 ;
        int backYears = END_YEAR + 1 - START_YEAR ;
        
        //String letter2 = "C" ;
        /*
        for (String letter2 : new String[] {"A","B","C","D","E","F","G","H","I","J"})
    	    for (String letter0 : new String[] {"a","b","c","d","e","f","g","h","i","j"})
            	for (String letter1 : new String[] {"a","b","c","d","e","f","g","h","i","j"})
        		    simNameList.add(prefix + letter2 + letter0 + letter1 + suffix) ;
        		    */


        if (mergeReports || simNameList.isEmpty())
        {
        	//for (String letter50 : new String[] {"Eje","Ajh","Fjh","Ibh","Eja","Jha","Hjc","Adg","Hhb","Ibd","Cah","Edj","Dhc","Feh","Dbc","Ddd","Jcg","Dif","Iae","Eci","Gfi","Fah","Djh","Dea","Bde","Cfa","Ghf","Adc","Jbg","Gbc","Jjh","Afb","Jbc","Faj","Dbd","Agj","Edb","Eia","Iii","Aic","Hhe","Fdf","Fde","Def","Eac","Gib","Bfa","Aac","Bag","Dfg"})
            //for (String letter50 : new String[] {"Hje","Big","Iib","Bje","Gfi","Jhd","Jij","Icg","Dhe","Ejf","Bbg","Bee","Hgd","Bfi","Daj","Ccd","Hib","Ada","Hjg","Acb","Bid","Ihb","Gfa","Fdg","Ide","Dgi","Chi","Ghf","Hfb","Aib","Fjh","Ied","Hbe","Ege","Aei","Cha","Egj","Bib","Cbh","Fjd","Bhc","Aah","Agg","Jhh","Cef","Chj","Ccc","Hid","Abd","Dji"})
        	for (String letter50 : new String[] {"Fdh","Fch","Iig","Jaf","Beg","Bdb","Hdi","Hcj","Gdi","Cgb","Ceb","Fjh","Jjc","Ajc","Dcj","Dad","Hfj","Dfd","Dgj","Cag","Gia","Cbe","Cbg","Cai","Iii","Jge","Hjd","Cei","Hdg","Hdd","Cbb","Icc","Cie","Ifj","Cce","Iac","Bge","Ejc","Hhc","Ahe","Fdi","Ahb","Dca","Daj","Daf","Abj","Afb","Dje","Abg","Cdf"})                 
        		simNameList.add(prefix + letter50 + suffix) ;
                             
=======
            for (String letter50 : new String[] {"Hje","Big","Iib","Bje","Gfi","Jhd","Jij","Icg","Dhe","Ejf","Bbg","Bee","Hgd","Bfi","Daj",
            		"Ccd","Hib","Ada","Hjg","Acb","Bid","Ihb","Gfa","Fdg","Ide","Dgi","Chi","Ghf","Hfb","Aib","Fjh","Ied","Hbe","Ege","Aei",
            		"Cha","Egj","Bib","Cbh","Fjd","Bhc","Aah","Agg","Jhh","Cef","Chj","Ccc","Hid","Abd","Dji"})
                             simNameList.add(prefix + letter50 + suffix) ;
                    //simNameList.add(prefix + letter0 + letter1 + suffix) ;
            simName = simNameList.get(0) ;
        }	

        if (simNameList.isEmpty())
        	simNameList.add(simName) ;
        
        String[] simNames = simNameList.toArray(new String[] {}) ;
        
        LOGGER.info(simNameList.toString()) ;
        
        if (!mergeReports && simNameList.size() < 101 && findReport)
        {
        	String[] siteNames = MSM.SITE_NAMES ;
        	for (String simNameLoop : simNameList) 
        	{
        		ScreeningReporter screeningReporter = new ScreeningReporter(simName, folderPath) ;
                EncounterReporter encounterReporter = new EncounterReporter(simNameLoop, folderPath) ;
                
                //HashMap<Comparable<?>,String> incidenceReport = screeningReporter.prepareYearsAtRiskIncidenceReport(siteNames, 13, END_YEAR, sortingProperty) ;
                //incidenceReportPrep = screeningReporter.prepareYearsAtRiskIncidenceReport(siteNames, 41, END_YEAR, prepStatus) ;
                //HashMap<Comparable,Number> beenTestedReport ;
                //beenTestedReport = screeningReporter.prepareYearsBeenTestedReport(backYears, 0, 0, END_YEAR) ;
                //ArrayList<Object> condomUseReport ;
                HashMap<Comparable<?>,String> incidenceReport = encounterReporter.prepareYearsIncidenceReport(siteNames, backYears, END_YEAR, sortingProperty) ;
                //HashMap<Comparable<?>,HashMap<Comparable<?>,Number>> cumulativeIncidenceReport = encounterReporter.prepareCumulativeAgentReceptionReport(sortingProperty) ;
                //HashMap<Comparable<?>,HashMap<Comparable<?>,Number>> incidenceReport = encounterReporter.prepareNumberAgentReceptionReport(sortingProperty) ;
                //String[] sortingValues = new String[incidenceReport.keySet().size()] ;
                //Object[] sortingArray = incidenceReport.keySet().toArray() ;
                //for (int sortingIndex = 0 ; sortingIndex < sortingArray.length ; sortingIndex++ )
                //	sortingValues[sortingIndex] = sortingArray[sortingIndex].toString() ;
                //HashMap<Comparable<?>,Number[]> invertedIncidenceReport = INVERT_HASHMAP_LIST(incidenceReport,sortingArray) ;
                
                //HashMap<Comparable,String> disclosureReport = encounterReporter.prepareYearsDisclosureReport(13,2019) ;
                //HashMap<Comparable,String> condomlessReport = encounterReporter.preparePercentAgentCondomlessYears(relationshipClazzNames, backYears, END_YEAR, "statusHIV", false, "") ;
                //HashMap<Comparable,String> condomlessHivReport = encounterReporter.preparePercentAgentCondomlessYears(relationshipClazzNames, backYears, END_YEAR, "statusHIV", false, "statusHIV") ;
                    //condomUseReport = (ArrayList<Object>) encounterReporter.prepareYearsCondomUseRecord(backYears, END_YEAR).clone() ;
                //Reporter.DUMP_OUTPUT("beenTestedReport",simName,folderPath,beenTestedReport);
                

                //Reporter.WRITE_CSV(invertedIncidenceReport,YEAR,sortingValues,reportName,simNameLoop,folderPath) ;
                Reporter.WRITE_CSV_STRING(incidenceReport,YEAR,reportName,simName,folderPath) ;
        	}
        
        	
        }
        else if (mergeReports)
    		MERGE_HASHMAP_CSV(simNameList,reportName,scoreName,folderPath) ;
        else
        {
            //String[] simNames = new String[] {"newSortRisk12aPop40000Cycles1825"} ;
            //ArrayList<String> closestSimulations
            String[] scoreNames = new String[] {"all_false","all_true"} ;
            double[] weight = new double[] {1,1/12.25} ;
            
            if (!selectBest)
            	cutoff = simNameList.size() ;
            else
                simNameList = CLOSEST_SIMULATIONS(simNameList,"year",scoreNames,weight,"riskyIncidence_HIV",folderPath,"gonoGoneWild","data_files/") ;
            LOGGER.info(String.valueOf(simNameList.size()) + " simulations included.") ;
            //MULTI_WRITE_CSV(simNameList, "condomUse", folderPath) ; // "C:\\Users\\MichaelWalker\\OneDrive - UNSW\\gonorrhoeaPrEP\\simulator\\PrEPSTI\\output\\prep\\") ; //
            if (simNameList.size() < cutoff)
                cutoff = simNameList.size() ;
            //MULTI_WRITE_CSV(simNameList, "year", "been_tested", "beenTestedReport", folderPath) ; // "C:\\Users\\MichaelWalker\\OneDrive - UNSW\\gonorrhoeaPrEP\\simulator\\PrEPSTI\\output\\prep\\") ; // 
            MULTI_WRITE_CSV(simNameList.subList(0, cutoff), "year", scoreName, "riskyIncidence_HIV", folderPath) ; // "C:\\Users\\MichaelWalker\\OneDrive - UNSW\\gonorrhoeaPrEP\\simulator\\PrEPSTI\\output\\prep\\") ; // 
            //MULTI_WRITE_CSV(simNameList.subList(0, cutoff), "year", "all_true", "incidence_HIV", folderPath) ; // "C:\\Users\\MichaelWalker\\OneDrive - UNSW\\gonorrhoeaPrEP\\simulator\\PrEPSTI\\output\\prep\\") ; // 
            LOGGER.info(simNameList.subList(0, cutoff).toString()) ;
            // LOGGER.info(String.valueOf(cutoff) + " simulations included.") ;
            //PREPARE_GRAY_REPORT(simNames,folderPath,2007,2017) ;
        }
    }

    /**
     * Generate metaLabel and metaData up to a particular cycle by reading reports
     * @param simName
     * @param rebootCycle
     * @return
     */
    public static HashMap<String, ArrayList<String>> GENERATE_REBOOT_DATA_UPTO_CYCLE(String simName, int rebootCycle) {
        HashMap<String, ArrayList<String>> rebootData = new HashMap<String, ArrayList<String>>();
        PopulationReporter populationReporter = new PopulationReporter(simName, ConfigLoader.REBOOT_PATH);
        RelationshipReporter relationshipReporter = new RelationshipReporter(simName, ConfigLoader.REBOOT_PATH);
        ScreeningReporter screeningReporter = new ScreeningReporter(simName, ConfigLoader.REBOOT_PATH);

        int cycleToGenerateReportUpTo = rebootCycle;

        // generate rebooted metalabels and metadata
        ArrayList<String> metaLabels = new ArrayList<String>() ; 
        ArrayList<String> metaData = new ArrayList<String>() ;


        /* * * * * * * * * *
         *      AGENTS     *
         * * * * * * * * * */

        // generate our reboot census
        HashMap<Integer, String> populationCensusUpToCycle = populationReporter.prepareCensusReport(cycleToGenerateReportUpTo, screeningReporter);


        // extract agent census data and write to internal metadata
        // sort agents by id
        TreeSet<Integer> sortedAgentKeySet = new TreeSet<Integer>();
        sortedAgentKeySet.addAll(populationCensusUpToCycle.keySet());

        // add rebooted agent data to metadata
        metaLabels.add("Agents") ;
        String agentsReboot = "" ;
        for (Integer agentId : sortedAgentKeySet) 
        {
            String newAgentRecord = populationCensusUpToCycle.get(agentId);
            agentsReboot += newAgentRecord;
        }
        metaData.add(agentsReboot) ;


        /* * * * * * * * * *
         *  RELATIONSHIPS  *
         * * * * * * * * * */

        // extract relationship data and write to internal metadata
        HashMap<Integer, String> relationshipRecordHashMap = relationshipReporter.prepareRelationshipRecordHashMap(cycleToGenerateReportUpTo);

        TreeSet<Integer> sortedRelationshipKeySet = new TreeSet<Integer>();
        sortedRelationshipKeySet.addAll(relationshipRecordHashMap.keySet());

        // add rebooted relationship data to metadata
        metaLabels.add("Relationships") ;
        String relationshipsReboot = "" ;
        for (Integer relationshipId : sortedRelationshipKeySet)
            relationshipsReboot += relationshipRecordHashMap.get(relationshipId) + ' ' ;
        metaData.add(relationshipsReboot) ;

        // dump new metadata
        String rebootedSimName = simName + "FROM" + String.valueOf(rebootCycle);
        String rebootedFolderPath = Community.FILE_PATH;

        // rebootPathAndNames.put("rebootedSimName", rebootedSimName);
        // rebootPathAndNames.put("rebootedFolderPath", rebootedFolderPath);
        rebootData.put("metaLabels", metaLabels);
        rebootData.put("metaData", metaData);

        return rebootData;
    }
/**
     * Object to read saved File output and feed it to Reporter
     */
    private class Reader
    {
    //	ArrayList<String> encounterReports, ArrayList<String> clearReports, ArrayList<String> screenReports)
        private ArrayList<String> fileNames = new ArrayList<String>() ;
        private String folderPath ;
        private int fileIndex = 0;
        private int cyclesPerFile ;
        private String simName ;
        ArrayList<String> metaData ;
        
        private Reader()
        {
            
        }
        
        private Reader(String simName, String reporterName, String filePath)
        {
            this.simName = simName ;
            this.folderPath = filePath ;
            fileIndex = 0 ;
            fileNames = initFileNames(simName + reporterName) ;
            initMetaData() ;
            cyclesPerFile = initCyclesPerFile() ;
        }
        
        /**
         * 
         * @return (String) path to folder with Reader's files.
         */
        private String getFolderPath()
        {
            return folderPath ;
        }
        
        /**
         * Updates Reader output from next file.
         * TODO: Implement all this with an iterator.
         * @return Updated Reporter.input .
         */
        private ArrayList<String> updateOutputArray()
        {
            ArrayList<String> outputArray = new ArrayList<String>() ;
            String record ;
            if (fileIndex >= fileNames.size())
            {
                fileIndex = 0 ;
                return outputArray ;
            }
            try
            {
                //LOGGER.info(folderPath + getFileName());
                BufferedReader fileReader = new BufferedReader(new FileReader(folderPath + getFileName())) ;
                record = fileReader.readLine() ;
                if (record == null)
                    LOGGER.warning("Empty report file");
                while (record != null)
                {
                    outputArray.add(record) ;
                    record = fileReader.readLine() ;
                }
                fileReader.close() ;
            }
            catch ( Exception e )
            {
                LOGGER.log(Level.SEVERE, e.toString());
                record = "" ;
            }
            if (outputArray.isEmpty())
                LOGGER.log(Level.SEVERE, "Empty Report from File at {0}", new Object[]{folderPath});
            
            return outputArray ;
        }
        
        /**
         * Adjusts the value of fileIndex to keep track of Report files. 
         * @return (String) fileName of next file.
         */
        private String getFileName()
        {
            fileIndex++ ;
            return fileNames.get(fileIndex-1) ;
        }
        
        /**
         * 
         * @param simName
         * @return ArrayList of fileNames, excluding the METADATA file.
         */
        private ArrayList<String> initFileNames(String simName)
        {
            ArrayList<String> nameArray = new ArrayList<String>() ;
            File folder = new File(folderPath) ;
            for (File file : folder.listFiles()) {
                if (file.isFile()) 
                {
                    String fileName = file.getName() ;
                    if (fileName.endsWith("-METADATA.txt") || fileName.endsWith("-REBOOT.txt"))
                        continue ;
                    if (fileName.startsWith(simName) && fileName.endsWith("txt"))
                        nameArray.add(fileName) ;
                }
            }
            //LOGGER.log(Level.INFO, "{0}", nameArray) ;
            //Collections.sort(nameArray, String.CASE_INSENSITIVE_ORDER) ;
            nameArray.sort(String.CASE_INSENSITIVE_ORDER);
            //LOGGER.log(Level.INFO, "{0}", nameArray) ;
            return nameArray ;
        }
        
        /**
         * 
         * @return (int) number of cycles in each file.
         */
        private int initCyclesPerFile()
        {
            if (fileNames.size() == 1)
                return Integer.valueOf(getMetaDatum("Community.MAX_CYCLES").trim()) ; // Cannot use getMaxCycles()
            String fileName1 = fileNames.get(1) ;
            int dashIndex = fileName1.indexOf("-") + 1 ; // Want following position
            int dotIndex = fileName1.indexOf("txt") - 1 ; // -1 for "."
            //LOGGER.log(Level.INFO, "{0} {1} {2}", new Object[] {fileName1,dashIndex,dotIndex});
            return Integer.valueOf(fileName1.substring(dashIndex,dotIndex)) ;
        }
        
        /**
         * @return opening record of opening input file
         * @throws FileNotFoundException
         * @throws IOException 
         */
        private String getInitialRecord() throws FileNotFoundException, IOException
        {
            BufferedReader fileReader = new BufferedReader(new FileReader(folderPath + fileNames.get(0))) ;
            String record = fileReader.readLine() ;
            fileReader.close() ;
            //LOGGER.info(record) ;
            if (record == null)
                LOGGER.warning("Empty report file");

            return record ;
        }
    
        /**
         * @return opening record of opening input file
         * @throws FileNotFoundException
         * @throws IOException 
         */
        private String getFinalRecord() throws FileNotFoundException, IOException
        {
            // Read final input file
            BufferedReader fileReader = new BufferedReader(new FileReader(folderPath + fileNames.get(fileNames.size()-1))) ;
            String outputString = "" ;
            // Find last line
            for (String record = "" ;  record != null ; record = fileReader.readLine() )
                outputString = record ;
            fileReader.close() ;
            
            if (outputString.isEmpty())
                LOGGER.warning("Empty report file");

            return outputString ;
        }
    
        /**
         * 
         * @return (ArrayList) Final saved installment of report.
         */
        private ArrayList<String> getFinalReport()
        {
            fileIndex = fileNames.size() - 1 ;
            return updateOutputArray() ;
        }
        
        /**
         * 
         * @param backCycles
         * @param endCycle
         * @return Report of backCycles records leading up to endCycle'th
         */
        private ArrayList<String> getBackCyclesReport(int backCycles, int endCycle)
        {
            // System.out.println("getBackCyclesReport(" + backCycles + "," + endCycle + ")");
            float t0 = System.nanoTime();

            ArrayList<String> outputList = new ArrayList<String>() ;
            int cycleFileIndex ;
            try
            {
                int startCycle = endCycle - backCycles ;
                // Open file
                cycleFileIndex = Math.floorDiv(startCycle,cyclesPerFile) ;
                //getSpecificFile(startCycle) ;
                BufferedReader fileReader = new BufferedReader(new FileReader(folderPath + fileNames.get(cycleFileIndex))) ;
                        
                boolean newFile = false ;

                // Move to starting line in file
                int startLine = startCycle % cyclesPerFile ;
                int endLine = startLine + backCycles ;
                int pauseLine ;
                //LOGGER.log(Level.INFO, "startLine:{0} startCycle:{1} endLine:{2} fileName:{3}", new Object[] {startLine,startCycle,endLine,fileNames.get(cycleFileIndex)}) ;
                
                String outputString ;

                int readLines = 0 ;
                // Skip unwanted lines
                for (int lineNb = 0 ; lineNb < startLine ; lineNb++ )
                    fileReader.readLine() ;
                while (readLines < backCycles)
                {
                    // Open new file if reached end of previous one
                    if (newFile)
                    {
                        fileReader = new BufferedReader(new FileReader(folderPath + fileNames.get(cycleFileIndex))) ;
                        newFile = false ;
                    }

                    // End loop at end of file if not before
                    pauseLine = endLine;
                    if (pauseLine > cyclesPerFile)
                        pauseLine = cyclesPerFile ;

                    //LOGGER.log(Level.INFO, "startLine:{0} pauseLine:{1} readLines:{2} fileName:{3}", new Object[] {startLine,pauseLine,readLines,fileNames.get(cycleFileIndex)});
                    for (int lineNb = startLine ; lineNb < pauseLine ; lineNb++ )
                    {
                        outputString = fileReader.readLine() ;
                        if (null == outputString)
                        {
                            LOGGER.severe(fileNames.get(cycleFileIndex) + " has null line " + String.valueOf(lineNb)) ;
                            assert(2 < 0) ;
                            break ;
                        }
                        
                        outputList.add(outputString) ;
                        readLines++ ;
                    }
                    fileReader.close() ;

                    // Prepare for next file
                    startLine = 0 ;
                    endLine -= cyclesPerFile ;
                    cycleFileIndex++ ;
                    newFile = true ;
                }
            }
            catch( Exception e )
            {
                LOGGER.severe(e.toString());
                assert(2 < 0) ;
            }

            float t1 = System.nanoTime();
            Community.RECORD_METHOD_TIME("Reporter.getBackCyclesReport(backCycles,endCycles)", t1 - t0);
            
            return outputList;
        }



        /**
         * Reads backwards through the files. Used when only last backCycles are 
         * of interest.
         * @param backCycles
         * @return Report of last backCycles read from files .
         */
        private ArrayList<String> getBackCyclesReport(int backCycles)
        {
            ArrayList<String> outputList = new ArrayList<String>() ;
            ArrayList<String> fileList = new ArrayList<String>() ;
            
            int cycleFileIndex = fileNames.size() - 1 ;
            // LOGGER.info("cycleFileIndex:" + String.valueOf(cycleFileIndex));
            // From which line do we had files
            int fromLine = 0 ;
            
            try
            {
                while (backCycles > 0)
                {
                    BufferedReader fileReader = new BufferedReader(new FileReader(folderPath + fileNames.get(cycleFileIndex))) ;
                    cycleFileIndex = cycleFileIndex - 1 ; 

                    for (String record = fileReader.readLine() ;  record != null ; record = fileReader.readLine() )
                    {
                        fileList.add(record) ;
                        backCycles-- ;
                    }
                    if (backCycles < 0)
                        fromLine = -backCycles ;

                    outputList.addAll(0,fileList.subList(fromLine, fileList.size())) ;

                    fileReader.close() ;
                }
            }
            catch ( Exception e )
            {
                LOGGER.log(Level.SEVERE, "{0}", e.toString());
            }
            if (outputList.isEmpty())
                LOGGER.log(Level.SEVERE, "Empty Report from File at {0}", new Object[]{folderPath});
            
            //fileIndex = 0 ;
            
            return outputList ;
        }
        
        /**
         * Initialises metaData property of Reader from file,
         */
        private void initMetaData() 
        {
            metaData = new ArrayList<String>() ;
            try
            {
                BufferedReader fileReader = new BufferedReader(new FileReader(folderPath + simName + "-METADATA.txt")) ;
                ArrayList<String> outputString = new ArrayList<String>() ;
                // Find last line
                for (String record = "" ;  record != null ; record = fileReader.readLine() )
                    outputString.add(record) ;
                
                fileReader.close() ;

                metaData = outputString ;
            }
            catch (Exception e)
            {
                LOGGER.severe(e.toString()) ;
            }
        }
        
        /**
         * 
         * @param metaDatum
         * @return (String) value of metaDatum from metaData.
         */
        private String getMetaDatum(String metaDatum)
        {
            for (String record : metaData)
            {
                if (record.isEmpty())
                    continue ;
                int colonIndex = record.indexOf(":") ;
                if (metaDatum.equals(record.substring(0, colonIndex)))
                    return record.substring(colonIndex + 1) ;
            }
            return "" ;
        }
    }
}
