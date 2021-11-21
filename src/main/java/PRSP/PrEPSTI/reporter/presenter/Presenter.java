/*
 * Presenter class, takes data from Reporter objects and presents them
 * graphically.
 */
package PRSP.PrEPSTI.reporter.presenter;

import PRSP.PrEPSTI.configloader.ConfigLoader;
import PRSP.PrEPSTI.reporter.*;
import PRSP.PrEPSTI.community.Community;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Shape;
import java.awt.BasicStroke;
import java.awt.geom.Ellipse2D;
import java.awt.Toolkit;
import java.awt.font.TextAttribute;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.text.DecimalFormat ;

import org.jfree.chart.*;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.chart.plot.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
//import org.jfree.chart.ui.RefineryUtilities;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.title.LegendTitle;

import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.renderer.category.GroupedStackedBarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.data.KeyToGroupMap;
import org.jfree.data.category.*;
//import org.jfree.data.general.* ;
import org.jfree.data.xy.XYDataset;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection ;  
import org.jfree.chart.util.ShapeUtils;


import java.lang.reflect.* ;
import java.util.* ;

import java.io.File ;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException ;
import java.util.logging.Level;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator ;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction ;
import org.jfree.chart.ui.RectangleEdge ;
import org.jfree.chart.annotations.* ;

//import statec.Extrapolate.Value;

/**
 * Created 22/03/2018
 * @author Michael Walker
 * Email: mlwalker@kirby.unsw.edu.au, m.walker@aip.org.au
 */
public class Presenter {
    
    private Reporter reporter ;
    
    //protected ArrayList<ArrayList<Object>> categoryData = new ArrayList<ArrayList<Object>>() ;
    protected ArrayList<ArrayList<Object>> scoreData = new ArrayList<ArrayList<Object>>() ;
    protected String applicationTitle ;
    public String chartTitle ;
    
    static protected String BASE = "base" ;
    static protected String INTERVAL = "interval" ;
    static protected String PROPORTION = "proportion" ;
    static protected String GROUP = "__" ;

    static final String CSV = Reporter.CSV ;
    static final String COMMA = Reporter.COMMA ;

    // Used for controlling if and what is co-plotted from file.
    public static boolean PLOT_FILE;    
    public static boolean SAVE_PLOT ;
    public static String FOLDER_PATH;
    public static String FILENAME; // "incidence_kirby2018" ; // "meanNotificationRate" ; // "unique_positivity_urethra" ; // "notifications" ; //  
    //static String[] DATA_SCORE = new String[] {"hiv_negative","hiv_positive"} ;
    //static String[] DATA_SCORE = new String[] {"data_notifications","data_notification_rate"} ; // 
    public static String[] DATA_SCORE;
            
    private boolean stacked = true ; // 
    private BarChart_AWT chart_awt ;
    
    private String folderPath = Community.FILE_PATH ;


    public static int FIGURE_WIDTH;
    public static int FIGURE_HEIGHT;


    /* * * * * * * * * * * * * * * * * * * * * *
     *         LINE GRAPH DRAWING INFO         *
     * * * * * * * * * * * * * * * * * * * * * */
    public final String ERROR_INTERVALS = "ERROR_INTERVALS";
    public final String SHADED_REGION = "SHADED_REGION";


    // !!
    private String lineGraphErrorType = ERROR_INTERVALS;
    private boolean drawPoints = false ;  // draw each individual point for a line graph true by default
    private boolean drawError = false ;
    private boolean insetLegend = true ;  // determines whether the legend is inset into 

    static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger("presenter") ;
    
    static protected String GET_TIME_PERIOD_STRING(int backYears, int backMonths, int backDays)
    {
        String timeString = "" ;
        if (backYears > 1) 
            timeString += String.valueOf(backYears) + " Years " ;
        else if (backYears == 1) 
            timeString += "1 Year " ;
        
        if (backMonths > 1) 
            timeString += String.valueOf(backMonths) + " Months " ;
        else if (backMonths == 1) 
            timeString += "1 Month " ;
        
        if (backDays > 1) 
            timeString += String.valueOf(backDays) + " Days" ;
        else if (backDays == 1) 
            timeString += "1 Day" ;
        
        if ((!timeString.contains("Month")) && (!timeString.contains("Day")))
            timeString = "year" ;
        
        return timeString ;
    }
    
    /**
     * Generates a suitable label for plots involving a sorting property 
     * such as HIV status. Assumes label has form propertyName_sortingValue .
     * @param sortingProperty (String) The name of the property by which the Agents
     * are sorted. Empty if Agents are not sorted.
     * @param label
     * @return (String) The generated label
     */
    static protected String GENERATE_SORTED_LABEL(String label, String sortingProperty)
    {
        // Replace label only if a sortingProperty is included
        if (sortingProperty.isEmpty())
            return label ;
        
        String[] labelParts = label.split("_") ;
        
        // Represent name of sortingProperty
        if (sortingProperty.endsWith("Status"))
            sortingProperty = sortingProperty.substring(0, sortingProperty.indexOf("Status")) ;
        else if (sortingProperty.startsWith("status"))
            sortingProperty = sortingProperty.substring(6) ;    // "status".length() = 6
        
        // Represent value of sortingProperty
        String valueString = labelParts[labelParts.length - 1] ;
        if ("true".equals(valueString))
            valueString = "positive" ;
        else if ("false".equals(valueString))
            valueString = "negative" ;
        else if (labelParts.length == 1)
            return String.join("_", labelParts[0], sortingProperty) ;
        
        return String.join("_", labelParts[0], sortingProperty, valueString) ;
    }
    
    /**
     * Generate a label for the y-axis from the scoreNames.
     * @param scoreNames
     * @return (String)
     */
    static private String GET_Y_LABEL(String[] scoreNames)
    {
        String scoreName = "" ;
        String name2 ;
        for (String name : scoreNames)
        {
            if (name.contains(GROUP))
                name2 = name.substring(0, name.indexOf(GROUP)) ;
            else
                name2 = name ;
            if (!scoreName.contains(name2))
                scoreName += "/" + name2 ;
        }
        
        return scoreName.substring(1) ;
    }
    
    /**
     * Adds data from given report to the given dataset
     * @param dataset
     * @param report
     * @param readScores - (String[]) valueTypes given in new report
     * @return (DefaultCategoryDataset) dataset with new data from report
     */
    static public DefaultCategoryDataset EXPAND_DATASET(DefaultCategoryDataset dataset, HashMap<Comparable<?>,Number[]> report, String[] readScores )
    {
        Number[] scoreValueArray ;
        for (Object key : report.keySet())
        {
            scoreValueArray = report.get(key) ;
          // logger.log(level.info,"{0}",scoreValueArray) ;
            //if (dataset.getColumnKeys().contains(String.valueOf(key))) 
                for (int scoreIndex = 0 ; scoreIndex < scoreValueArray.length ; scoreIndex++ )
                {
                    //int scoreIndex = 1 ;
                    Number scoreValue = scoreValueArray[scoreIndex] ;
                    String scoreName = readScores[scoreIndex] ;
                    dataset.addValue( scoreValue, scoreName, String.valueOf(key)) ;
                }
        }
        /**
        LOGGER.info("// Remove categories not in report.keySet") ;
        ArrayList<Object> removeKeys = new ArrayList<Object>() ;
        for (Object key : dataset.getColumnKeys())
            if (!report.containsKey(key))
                removeKeys.add(key);
        
        for (Object key : removeKeys)
            dataset.removeColumn((Comparable) key);
            */

        return dataset ;
    }
    
    /**
     * Adds data from given report to the given dataset
     * @param xySeriesCollection
     * @param report
     * @param readScores - (String[]) valueTypes given in new report
     * @return (DefaultCategoryDataset) dataset with new data from report
     */
    static public XYSeriesCollection EXPAND_DATASET(XYSeriesCollection xySeriesCollection, HashMap<Comparable<?>,Number[]> report, String[] readScores)
    {
        Number[] scoreValueArray ;
        Number categoryValue ;
        Number scoreValue ;
        Comparable category ;
        
        for (int scoreIndex = 0 ; scoreIndex < readScores.length ; scoreIndex++ )
        {
            XYSeries xySeries = new XYSeries(readScores[scoreIndex]) ;
            for (Object key : report.keySet())
            {
                category = (Comparable) key ;

                scoreValueArray = report.get(key) ;
                //LOGGER.log(Level.INFO,"{0}",scoreValueArray) ;
                scoreValue = scoreValueArray[scoreIndex] ;
                if (int.class.isInstance(category) || Integer.class.isInstance(category)) 
                {
                    categoryValue = Integer.valueOf(category.toString()) ;
                    xySeries.add((Integer) categoryValue, scoreValue, false);
                }
                else
                {
                    categoryValue = Double.valueOf(category.toString()) ;
                    xySeries.add((Double) categoryValue, scoreValue, false);
                }
            }
            try
            {
                xySeriesCollection.addSeries((XYSeries) xySeries.clone()) ;
            }
            catch( Exception e )
            {
                LOGGER.severe(e.toString());
                e.getStackTrace() ;
            }
        }
        
        return xySeriesCollection ;
    }
    
    /**
     * Reads .csv file so that its data may be plotted.
     * @param fileName
     * @return (HashMap) Report (Object) key maps to (Number) value
     */
    static HashMap<Comparable,Number> READ_HASHMAP_NUMBER_CSV(String fileName)
    {
        HashMap<Comparable,Number> hashMapNumber = new HashMap<Comparable,Number>() ;
        
        String folder = "data_files/" ;
        String fileHeader ;
        String[] arrayHeader  = new String[] {} ;
        
        // Plotting Integer or Double?
        Comparable key ;
        String[] recordArray ;
        Number[] valueArray ;
        int recordLength = 0 ;
        
        try
        {
            BufferedReader fileReader 
                    = new BufferedReader(new FileReader(folder + fileName + CSV)) ;
            fileHeader = fileReader.readLine() ;
            
            arrayHeader = fileHeader.split(COMMA) ;
            recordLength = arrayHeader.length ;
            
            // Find last line
            String record = fileReader.readLine() ;  
            recordArray = record.split(COMMA) ;
            
            valueArray = new Number[recordLength - 1] ;
            while ((record != null) && (!record.isEmpty()))
            {
                recordArray = record.split(COMMA) ;
                try
                {
                    key = Integer.valueOf(recordArray[0]) ;
                }
                catch ( Exception e )
                {
                    key = recordArray[0] ;
                }

                for (int index = 1 ; index < recordLength ; index++ )
                {
                    try
                    {
                        valueArray[index - 1] = (Number) Integer.valueOf(recordArray[index]) ;
                    }
                    catch ( Exception e )
                    {
                        valueArray[index - 1] = (Number) Double.valueOf(recordArray[index]) ;
                    }
                }
                hashMapNumber.put(key, valueArray[0]) ;
                record = fileReader.readLine() ;
            }
            fileReader.close() ;
        }
        catch ( Exception e )
        {
            LOGGER.severe(e.toString());
        }
        
        return hashMapNumber ;
    }
    
    /**
     * Reads .csv file so that its data may be plotted.
     * @param fileName
     * @return (HashMap) Report (Object) key maps to (Number) value
     */
    static HashMap<Comparable<?>,Number[]> READ_HASHMAP_NUMBER_ARRAY_CSV(String fileName)
    {
        HashMap<Comparable<?>,Number[]> hashMapNumberArray = new HashMap<Comparable<?>,Number[]>() ;
        
        String fileHeader ;
        String[] arrayHeader  = new String[] {} ;
        
        // Plotting Integer or Double?
        Comparable key = 0 ;
        String[] recordArray ;
        Number[] valueArray ;
        int recordLength = 0 ;
        
        try
        {
            BufferedReader fileReader 
                    = new BufferedReader(new FileReader(FOLDER_PATH + fileName + CSV)) ;
            fileHeader = fileReader.readLine() ;
            
            arrayHeader = fileHeader.split(COMMA) ;
            recordLength = arrayHeader.length ;
            
            // Find last line
            String record = fileReader.readLine() ;  
            recordArray = record.split(COMMA) ;
            
            while ((record != null) && (!record.isEmpty()))
            {
                valueArray = new Number[recordLength - 1] ;
                // LOGGER.info(record);
                recordArray = record.split(COMMA) ;
                try
                {
                    key = Integer.valueOf(recordArray[0]) ;
                }
                catch ( Exception e )
                {
                    key = recordArray[0] ;
                }
                if ((Integer) key < 2017)
                {
                    for (int index = 1 ; index < recordLength ; index++ )
                    {
                        try
                        {
                            valueArray[index - 1] = (Number) Integer.valueOf(recordArray[index]) ;
                        }
                        catch ( Exception e )
                        {
                            valueArray[index - 1] = (Number) Double.valueOf(recordArray[index]) ;
                        }
                        //LOGGER.log(Level.INFO, "{0} ",valueArray[index - 1]) ;
                    }
                    hashMapNumberArray.put(key, (Number[]) valueArray.clone()) ;
                }
                record = fileReader.readLine() ;
            }
            fileReader.close() ;
        }
        catch ( Exception e )
        {
            LOGGER.severe(e.toString());
        }
        
        return hashMapNumberArray ;
    }
    
    /**
     * Load report .csv files and return a report with the mean of all entries.
     * @param fileNames
     * @return true if .csv file is written successfully and false otherwise.
     */
    public static boolean MEAN_HASHMAP_NUMBER_ARRAY_CSV(String[] fileNames)
    {
        HashMap<Comparable<?>,Number[]> meanHashMapNumberArray = new HashMap<Comparable<?>,Number[]>() ;
        
        Number[] entries ;
        String fileHeader = "" ;
        String[] arrayHeader ;
        ArrayList<HashMap<Comparable<?>,Number[]>> meanHashMapList = new ArrayList<HashMap<Comparable<?>,Number[]>>() ;
        
        try
        {
            BufferedReader fileReader 
                    = new BufferedReader(new FileReader(FOLDER_PATH + fileNames[0] + CSV)) ;
            fileHeader = fileReader.readLine() ;
            fileReader.close();
        }
        catch ( Exception e )
        {
            LOGGER.severe(e.toString()) ;
            return false ;
        }
        // Read in HashMaps from .csv files
        for (String fileName : fileNames)
            meanHashMapList.add(READ_HASHMAP_NUMBER_ARRAY_CSV(fileName)) ;
        
        HashMap<Comparable<?>,Number[]> hashMap0 = meanHashMapList.get(0) ;
        int entryLength = hashMap0.get(hashMap0.keySet().toArray()[0]).length ;
        Number[] sumEntry = Arrays.copyOf(new Number[] {0.0}, entryLength) ;
        
        for (Comparable<?> key : hashMap0.keySet())
            meanHashMapNumberArray.put(key, sumEntry) ;
        
        // Add all entries
        for (HashMap<Comparable<?>,Number[]> hashMap : meanHashMapList)
            for (Comparable<?> key : hashMap.keySet())
            {
                entries = hashMap.get(key) ;
                sumEntry = meanHashMapNumberArray.get(key) ;
                for (int index = 0 ; index < entries.length ; index++ )
                    sumEntry[index] = sumEntry[index].doubleValue() + entries[index].doubleValue() ;
                meanHashMapNumberArray.put(key,sumEntry) ;
            }
        
        // Divide to find mean
        for (Comparable<?> key : meanHashMapNumberArray.keySet())
        {
            entries = meanHashMapNumberArray.get(key) ;
            for (int index = 0 ; index < sumEntry.length ; index++ )
                entries[index] = entries[index].doubleValue()/entryLength ;
            meanHashMapNumberArray.put(key, entries) ;
        }
        
        arrayHeader = fileHeader.split(COMMA) ;
        Reporter.WRITE_CSV(meanHashMapNumberArray, arrayHeader[0], 
                (String[]) Arrays.asList(fileHeader).subList(1, arrayHeader.length).toArray(), fileNames[0], "_MEAN", FOLDER_PATH) ;
        //return meanHashMapNumberArray ;
        
        return true ;
    }
    
    public static void main(String[] args)
    {
        ConfigLoader.load() ;
        //String simName = "NoPrepCalibration12Pop40000Cycles2000" ;
        //String simName = "RelationshipCalibrationPop40000Cycles200" ; // "testPlotCondomUsePop4000Cycles500" ; // args[0] ;
        String folder = "output/long_sims/" ;
        //String folder = "data_files/" ;
        //String fileName = "incidence" ;
        //String property = "PrEP users with cumulative number of infections" ; // 0.31" ;
        //String property = "Counter-factual scenarios" ;
        String property = "Effect of extended PrEP roll-out (HIV-negative)" ;
        //String property = "Effect of PrEP-users screening period" ; // (PrEP use grows linearly)" ;
        String chartTitle = property ;
        // LOGGER.info(chartTitle) ;
        String[] legend ;
        // legend = args ;
        //legend = new String[] {"true","false"} ;
        legend = new String[] {"to 2019 - 0.31","to 2020 - 0.39","to 2022 - 0.53","to 2024 - 0.67"} ;
        //legend = new String[] {" 92 days (standard)","123 days","154 days","184 days"} ; //,"216 days"} ;    // " 78 days",,"106 days"
        //legend = new String[] {"Historical PrEP rollout","PrEP roll-out w/o change in condom use","PrEP roll-out w/o extra screening","No PrEP roll-out"} ;
        //PREP_PROBABILITY_ARRAY = new double[] {0.39,0.46,0.53,0.60,0.67,0.74} ; // 2013 to 2019
        // gonoGoneWild header  
        // year	all_false_wild	all_false_ASR	all_true_wild	all_true_ASR	Urethra_false_wild	Urethra_false_ASR	Urethra_true_wild	Urethra_true_ASR	
        //    Rectum_false_wild	Rectum_false_ASR	Rectum_true_wild	Rectum_true_ASR	Pharynx_false_wild	Pharynx_false_ASR	Pharynx_true_wild	Pharynx_true_ASR
        
        
        Presenter presenter = new Presenter(args[0],chartTitle) ;
        LOGGER.info((Arrays.asList(args)).toString()) ;
        
        HashMap<String, HashMap<String,String[]>> propertyToYAndRange = new HashMap<String, HashMap<String,String[]>>();
        String fileName ;
        //for (String fileName : args)
        boolean loopThroughFileNames = true ;
        if (loopThroughFileNames)
            for (int argIndex = 0 ; argIndex < args.length ; argIndex++ )
            {
            	fileName = args[argIndex] ;
        	    LOGGER.info(fileName);
                HashMap<Comparable, String[]> readCSV = Reporter.READ_CSV_STRING(fileName, FOLDER_PATH, 1);
                HashMap<String, String[]> yAndRange = Reporter.extractYValueAndRange(readCSV);
                propertyToYAndRange.put(legend[argIndex], yAndRange);
            }
        //LOGGER.info(propertyToYAndRange.toString()) ;
        

    	/*HashMap<Comparable, String[]> goneWild = Reporter.READ_CSV_STRING("gonoGoneWild", "data_files/", 1);
    	LOGGER.info(String.valueOf(goneWild.keySet())) ;
    	for (int index : new int[] {0,1,2,3} ) // = legend.length - 2 ; index < legend.length ; index++ )
        {
            HashMap<String, String[]> yAndRange = Reporter.extractYValueAndRange(goneWild,index);
            propertyToYAndRange.put(legend[index+2], yAndRange) ;
        }
        */
        
        //presenter.plotMedianAndRangeFromCSVFileNames(args, chartTitle, "Incidence-rate (per 100 person-years)", "year", legend) ;
        
        presenter.plotShadedHashMapStringCI(propertyToYAndRange,"incidence-rate (per 100 person-years)","year", legend) ;    // ScreeningPresenter.INCIDENCE
        
        /*
        ScreeningReporter screeningReporter = 
                //new ScreeningReporter("prevalence",community.screeningReport) ;
                new ScreeningReporter(simName,folder) ;
        ArrayList<Object> pharynxPrevalenceReport = screeningReporter.preparePrevalenceReport("Pharynx") ;
      // logger.log(level.info, "{0}", pharynxPrevalenceReport.get(0));
        Reporter.WRITE_CSV(pharynxPrevalenceReport, "Pharynx", simName, folder);
        */
        
        
        //presenter.readCSV(simName, chartTitle, folder);
        //HashMap<Object,Number[]> hashMapNumber = READ_HASHMAP_NUMBER_ARRAY_CSV(fileName);
        //HashMap<Object,Number> HASHMAP_NUMBER = READ_HASHMAP_NUMBER_CSV(fileName);
        //LOGGER.log(Level.INFO, "{0}", hashMapNumber );
    }
    
    public Presenter() 
    {
    	// Needed to work around HPC access issues.
    	chart_awt = new BarChart_AWT() ;
    }

    public Presenter(String simName, String chartTitle)
    {
        this.applicationTitle = simName ;
        this.chartTitle = chartTitle ;
        chart_awt = new BarChart_AWT(applicationTitle, chartTitle) ;
        
    }
    
    public Presenter(String simName, String chartTitle, String reportFilePath)
    {
        this.applicationTitle = simName ;
        this.chartTitle = chartTitle ;
        chart_awt = new BarChart_AWT(applicationTitle, chartTitle) ;
        folderPath = reportFilePath ;
        setReporter(new Reporter(simName,reportFilePath)) ;
    }
    
    public Presenter(String applicationTitle, String chartTitle, Reporter reporter)
    {
        this.applicationTitle = applicationTitle ;
        this.chartTitle = chartTitle ;
        chart_awt = new BarChart_AWT(applicationTitle, chartTitle) ;
        setReporter(reporter) ;
    }
    
    public void setReporter(Reporter reporter)
    {
        this.reporter = reporter ;
    }

    /**
     * Reads specified .csv file, reconstructs report and plots it through 
     * appropriate plotting method.
     * @param reportName
     * @param folderName 
     */
    //private void readCSV(String simName, String reportName, String folderPath)
    private void readCSV(String fileName, String folderPath)
    {
        String fileHeader ;
        String[] arrayHeader  = new String[] {} ;
        
        // Plotting Integer or Double?
        Comparable key ;
        String[] recordArray ;
        String scoreName = "" ;
        Number[] valueArray ;
        int recordLength = 0 ;
        
        // Yet to determine if Array or single Number
        HashMap<Comparable<?>,Number> hashMapNumber = new HashMap<Comparable<?>,Number>() ;
        HashMap<Comparable<?>,Number[]> hashMapArray = new HashMap<Comparable<?>,Number[]>() ;
        
        try
        {
            BufferedReader fileReader 
                    = new BufferedReader(new FileReader(folderPath + fileName + CSV)) ;
            fileHeader = fileReader.readLine() ;
            
            arrayHeader = fileHeader.split(COMMA) ;
            recordLength = arrayHeader.length ;

            // Find last line
            String record = fileReader.readLine() ;  
            recordArray = record.split(COMMA) ;
            if (recordLength > recordArray.length)
            {
                recordLength-- ;
                scoreName = arrayHeader[recordLength] ;
            }
            
            valueArray = new Number[recordLength - 1] ;
            while (record != null)
            {
                recordArray = record.split(COMMA) ;
                try
                {
                    key = Integer.valueOf(recordArray[0]) ;
                }
                catch ( Exception e )
                {
                    key = recordArray[0] ;
                }

                for (int index = 1 ; index < recordLength ; index++ )
                {
                    try
                    {
                        valueArray[index - 1] = (Number) Integer.valueOf(recordArray[index]) ;
                    }
                    catch ( Exception e )
                    {
                        valueArray[index - 1] = (Number) Double.valueOf(recordArray[index]) ;
                    }
                }
                if (recordLength > 2)
                    hashMapArray.put(key, (Number[]) valueArray.clone() ) ;
                else
                    hashMapNumber.put(key, valueArray[1]) ;
                record = fileReader.readLine() ;
            }
            fileReader.close() ;
        }
        catch ( Exception e )
        {
            LOGGER.severe(e.toString());
        }
        
        if (recordLength > 2)
        {
            //plotHashMap(arrayHeader[0],(String[]) Arrays.copyOfRange(arrayHeader, 1, recordLength),hashMapArray) ;
            plotSpline(arrayHeader[0],scoreName,hashMapArray,(String[]) Arrays.copyOfRange(arrayHeader, 1, recordLength)) ;
        }
        else
        {
          // logger.log(level.info, "{0}", hashMapNumber) ;
            plotHashMap(arrayHeader[0],arrayHeader[1],hashMapNumber) ;
        }
    }
    
    /**
     * Extracts information from a range of .csv files
     * where each .csv file is name a property IF:
     * * legend is null OR
     * * legend.length does NOT equal fileNames.length
     * 
     * @param fileNames - full path of fileNames we are extracting information from
     * @param title - title of our chart
     * @param yLabel - label for our y-axis
     * @param xLabel - label for our x-axis
     * @param legend - String array of properties
     */
    public void plotMedianAndRangeFromCSVFileNames(String[] fileNames, String title, String yLabel, String xLabel, String[] legend) 
    {
        HashMap<String, HashMap<String,String[]>> propertyToYAndRange = new HashMap<String, HashMap<String,String[]>>();

        for (int i = 0; i < fileNames.length; ++i) {

            // if the legend is the same size as fileNames, we use the legend instead
            String property = (legend != null && legend.length == fileNames.length) ? legend[i] : fileNames[i];

            String fileName = fileNames[i];
            HashMap<Comparable, String[]> readCSV = Reporter.READ_CSV_STRING(fileName, FOLDER_PATH, 1);
            
            int VALUES_TO_ADD = 3; // y-value, lower, upper
            int yValueIndex = 0;
            int lowerIndex = 1;
            int upperIndex = 2;

            for (Comparable<?> keyCmp : readCSV.keySet()) {
                String[] values = readCSV.get(keyCmp);
                String[] to_add = Reporter.GENERATE_MEDIAN_AND_RANGE_ARRAY_FROM_VALUES_ARRAY(values);

                String[] newValues = new String[values.length + VALUES_TO_ADD];
                newValues[yValueIndex] = to_add[yValueIndex];
                newValues[lowerIndex] = to_add[lowerIndex];
                newValues[upperIndex] = to_add[upperIndex];
                for (int j = 0; j < values.length; ++j)
                    newValues[VALUES_TO_ADD+j] = values[j];
                readCSV.put(keyCmp, newValues); 
            }
            
            HashMap<String, String[]> yAndRange = Reporter.extractYValueAndRange(readCSV);
            propertyToYAndRange.put(property, yAndRange);
        }
        
        plotShadedHashMapStringCI(propertyToYAndRange, yLabel, xLabel, legend);
    }
    
    /**
     * Fits an Array of splines to Arrays of points from a report.
     * @param hashMapReport
     * @return (PolynomialSplineFunction[]) 
     */
    protected PolynomialSplineFunction[] generateFunctions(HashMap<Comparable<?>,Number[]> hashMapReport)
    {
        int valueLength = 0 ;
        for (Number[] value : hashMapReport.values())
            valueLength = value.length ;
        
        PolynomialSplineFunction[] functions = new PolynomialSplineFunction[valueLength] ;
        
        SplineInterpolator splineInterp = new SplineInterpolator();
        
        int arrayLength = hashMapReport.keySet().size() ;
        double[] xValues = new double[arrayLength] ;
        double[][] yValues = new double[valueLength][arrayLength] ;
        
        int keyCount = 0 ;
        for (Object key : hashMapReport.keySet())
        {
            xValues[keyCount] = Double.valueOf(key.toString()) ;
            for (int functionIndex = 0 ; functionIndex < valueLength ; functionIndex++ )
                yValues[functionIndex][keyCount] = hashMapReport.get(key)[functionIndex].doubleValue() ;
            keyCount++ ;
        }

        for (int functionIndex = 0 ; functionIndex < valueLength ; functionIndex++ )
            functions[functionIndex] = splineInterp.interpolate(xValues, yValues[functionIndex]);
        
        return functions ;
    }
    
    /**
     * 
     * @param unbinned
     * @param scoreName
     * @param interval - (int) size of bins
     * @return HashMap with keyValues() binned.
     */
    protected HashMap<Object,Number> regularBinHashMap(HashMap<Object,Number> unbinned, String scoreName, int interval)
    {
        HashMap<Object,Number> binned = new HashMap<Object,Number>() ;
        
        // Find keys in order
        ArrayList<Object> categoryEntry = new ArrayList<Object>() ;
        double scoreValue ;
        String categoryValue ;
        
        // Put keys in order
        for (Object key : unbinned.keySet())
        {
            if (key.equals(null))
                continue ;
            categoryEntry.add(key) ;
        }
        categoryEntry.sort(null);
        int totalDigits = ((int) Math.log10(Integer.valueOf(String.valueOf(categoryEntry.get(categoryEntry.size() - 1))))) + 1 ;
        int dataSize = categoryEntry.size() ;
        
        // Bin entries
        int openSegmentNb = 0 ;
        int closeSegmentNb = openSegmentNb + interval ;    // (int) Math.pow(base, nextIndex) - 1 ;    // First category stands alone
        //scoreValueArray = scoreData.get(index) ;
        int nbDigits = 1 ;

        while (closeSegmentNb > openSegmentNb)
        {
            // Initialise scoreValue
            scoreValue = 0.0 ;
            categoryValue  = //String.valueOf(categoryEntry.get(openSegmentNb)) + "-" +
                    String.valueOf(categoryEntry.get(closeSegmentNb-1)) ;
            if (0 == Integer.valueOf(String.valueOf(categoryEntry.get(openSegmentNb))))
            	nbDigits = 1 ;
            else
                nbDigits = ((int) Math.log10(Integer.valueOf(String.valueOf(categoryEntry.get(openSegmentNb))))) + 1 ;
            for (int addSpace = nbDigits ; addSpace < totalDigits ; addSpace++ )
                categoryValue = " ".concat(categoryValue) ;
            
            // loop through bin
            for (int segmentIndex = openSegmentNb ; segmentIndex < closeSegmentNb ; segmentIndex++ )
                scoreValue += unbinned.get(categoryEntry.get(segmentIndex)).doubleValue() ;
                    
            // Add bin to dataset
            binned.put(categoryValue, scoreValue) ;
                    
            // prepare for next bin
            openSegmentNb = closeSegmentNb ;    // (int) Math.pow(base, binIndex) - 1 ;    // -1 java counts from 0
            closeSegmentNb = openSegmentNb + interval  ;    // (int) Math.pow(base, nextIndex) - 1 ;    // -1 include closeSegmentNB in for-loop
            if (closeSegmentNb > dataSize) 
                closeSegmentNb = dataSize ;
                    
        }
        return binned ;
    }
        
    /**
     * // TODO: Clean this up and the one above it too.
     * @param unbinned
     * @param scoreName (redundant)
     * @return HashMap with keyValues() binned.
     */
    public HashMap<Comparable<?>,Number> binHashMap(HashMap<Comparable<?>,Number> unbinned, String scoreName)
    {
        HashMap<Comparable<?>,Number> binned = new HashMap<Comparable<?>,Number>() ;
        
        // Find keys in order
        ArrayList<Object> categoryEntry = new ArrayList<Object>() ;
        Integer[] categories = new Integer[] {0,2,11,51,100,100} ;
        
        double scoreValue ;
        int base = 2 ;
        String categoryValue = "-1" ;
        
        // Put keys in order
        for (Object key : unbinned.keySet())
        {
            if (key.equals(null))
                continue ;
            categoryEntry.add(key) ;
        }
        categoryEntry.sort(null);
        int totalDigits = ((int) Math.log10(Integer.valueOf(String.valueOf(categoryEntry.get(categoryEntry.size() - 1))))) + 1 ;
        int dataSize = categoryEntry.size() ;
        
        // Bin entries
        int categoryIndex = 1 ;
        int openSegmentNb = 0 ;
        int closeSegmentNb = categories[categoryIndex] ;    // (int) Math.pow(base, nextIndex) - 1 ;    // First category stands alone
        //int openSegmentIndex = 0 ;
        //int closeSegmentIndex = 1 ;
        
        //scoreValueArray = scoreData.get(index) ;
        int nbDigits = 1 ;

        
        while (categoryIndex < categories.length)   // (closeSegmentNb > openSegmentNb)
        {
        	//openSegmentNb = categoryEntry.get(openSegmentIndex) ;
        	//closeSegmentNb = categoryEntry.get(closeSegmentIndex) ;
        	
            // Initialise scoreValue
            scoreValue = 0.0 ;
            categoryValue = String.valueOf(categoryEntry.get(openSegmentNb)) + categoryValue ;
            if (0 == Integer.valueOf(String.valueOf(categoryEntry.get(openSegmentNb))))
            	nbDigits = 1 ;
            else
                nbDigits = ((int) Math.log10(Integer.valueOf(String.valueOf(categoryEntry.get(openSegmentNb))))) + 1 ;
            for (int addSpace = nbDigits ; addSpace < totalDigits ; addSpace++ )
                categoryValue = " ".concat(categoryValue) ;
            
            // loop through bin
            for (int segmentIndex = openSegmentNb ; segmentIndex < closeSegmentNb ; segmentIndex++ )
                scoreValue += unbinned.get(categoryEntry.get(segmentIndex)).doubleValue() ;
                    
            // Add bin to dataset
            binned.put(categoryValue, scoreValue) ;
                    
            if (categoryIndex == categories.length) 
            	break ;
            // prepare for next bin
            openSegmentNb = closeSegmentNb ;    // (int) Math.pow(base, binIndex) - 1 ;    // -1 java counts from 0
            closeSegmentNb = categories[categoryIndex] ;    // (closeSegmentNb + 1) * base - 1  ;    // (int) Math.pow(base, nextIndex) - 1 ;    // -1 include closeSegmentNB in for-loop
            categoryIndex++ ;
            if ((closeSegmentNb > dataSize) || (closeSegmentNb == openSegmentNb))
                closeSegmentNb = dataSize ;
            if (closeSegmentNb == openSegmentNb)
            	categoryValue = "+" ;
            else
                categoryValue = "-" + String.valueOf(categoryEntry.get(closeSegmentNb-1)) ;
                    
        }

        return binned ;
    }
        
    /**
     * 
     * @param unbinned
     * @param scoreNames
     * @return HashMap with keyValues() binned.
     */
    public HashMap<Comparable<?>,Number[]> binHashMap(HashMap<Comparable<?>,Number[]> unbinned, String[] scoreNames)
    {
        HashMap<Comparable<?>,Number[]> binned = new HashMap<Comparable<?>,Number[]>() ;
        
        // Find keys in order
        ArrayList<Object> categoryEntry = new ArrayList<Object>() ;
        Integer[] openIndices = new Integer[] {0,1,2,11,51,100} ;
        Integer[] closeIndices = new Integer[] {0,1,10,50,99,1000} ;
        //Integer[] openIndices = new Integer[] {0,1,2,10,20,50,100} ;
        //Integer[] closeIndices = new Integer[] {0,1,9,19,49,99,1000} ;
        ArrayList<Number> scoreValue ;
        Number[] scoreValueArray ;
        Number[] hashMapValue ;
        int base = 2 ;
        String categoryValue = "" ;
        
        // Put keys in order
        for (Object key : unbinned.keySet())
        {
            if (key.equals(null))
                continue ;
            categoryEntry.add(key) ;
        }
        categoryEntry.sort(null);
        int totalDigits = ((int) Math.log10(Integer.valueOf(String.valueOf(categoryEntry.get(categoryEntry.size() - 1))))) + 1 ;
        int dataSize = categoryEntry.size() ;
        
        //scoreValueArray = scoreData.get(index) ;

        //while (closeSegmentNb > openSegmentNb)
        for (int categoryIndex = 0 ; categoryIndex < openIndices.length ; categoryIndex++ )
        {
            // Bin entries
            int openSegmentNb = openIndices[categoryIndex] ;
            int closeSegmentNb = closeIndices[categoryIndex] ;    // (int) Math.pow(base, nextIndex) - 1 ;    // First category stands alone

            // Initialise scoreValue
            scoreValue = new ArrayList<Number>() ;
            for (String scoreName1 : scoreNames)
                scoreValue.add(0.0) ;
            //LOGGER.info(scoreValue.toString());

            categoryValue  = String.valueOf(categoryEntry.get(openSegmentNb)) ;
            if (closeSegmentNb >= dataSize) 
                closeSegmentNb = dataSize - 1 ;
            if (closeSegmentNb > openSegmentNb)
                categoryValue += "-" + String.valueOf(categoryEntry.get(closeSegmentNb)) ;
            int nbDigits = ((int) Math.log10(Integer.valueOf(String.valueOf(categoryEntry.get(openSegmentNb))))) + 1 ;
            if (nbDigits < 1)
                nbDigits = 1 ;
            for (int addSpace = nbDigits ; addSpace < totalDigits ; addSpace++ )
                categoryValue = " ".concat(categoryValue) ;
            
            // loop through bin
            for (int segmentIndex = openSegmentNb ; segmentIndex <= closeSegmentNb ; segmentIndex++ )
            {
                scoreValueArray = unbinned.get(categoryEntry.get(segmentIndex)) ;
                // Add scores
                for (int scoreIndex = 0 ; scoreIndex < scoreValueArray.length ; scoreIndex++ )
                    scoreValue.set(scoreIndex, scoreValue.get(scoreIndex).doubleValue() 
                            + scoreValueArray[scoreIndex].doubleValue()) ;
            }
                    
            // Add bin to dataset
            binned.put(categoryValue, (Number[]) scoreValue.toArray(new Number[0])) ;
                    
          // logger.log(level.info, "closeSegmentNb {0}, openSegmentNb {1}, categoryValue {2}", new Object[] {closeSegmentNb,openSegmentNb,categoryValue}) ;
            // prepare for next bin
            //openSegmentNb = closeSegmentNb ;    // (int) Math.pow(base, binIndex) - 1 ;    // -1 java counts from 0
            //closeSegmentNb = (closeSegmentNb) * base   ;    // (int) Math.pow(base, nextIndex) - 1 ;    // -1 include closeSegmentNB in for-loop
            //categoryValue = "-" + String.valueOf(categoryEntry.get(closeSegmentNb - 1)) ;
            //
          // logger.log(level.info, "{0} {1} {2} {3}", binned.get(categoryValue)) ;
                    
        }

        return binned ;
    }
    
    
    protected HashMap<Object,Number> binCumulativeHashMap(HashMap<Object,Number> unbinned, String scoreName)
    {
        HashMap<Object,Number> binned = new HashMap<Object,Number>() ;
        
        // Find keys in order
        ArrayList<Object> categoryEntry = new ArrayList<Object>() ;
        Number scoreValue ;
        int base = 2 ;
        String categoryValue = "" ;
        
        // Put keys in order
        for (Object key : unbinned.keySet())
        {
            if (key.equals(null))
                continue ;
            categoryEntry.add(key) ;
        }
        categoryEntry.sort(null);
        int totalDigits = ((int) Math.log10(Integer.valueOf(String.valueOf(categoryEntry.get(categoryEntry.size() - 1))))) + 1 ;
        int dataSize = categoryEntry.size() ;
        
        // Bin entries
        int openSegmentNb = 0 ;
        int closeSegmentNb = 1 ;    // (int) Math.pow(base, nextIndex) - 1 ;    // First category stands alone
        //scoreValueArray = scoreData.get(index) ;

        while (closeSegmentNb > openSegmentNb)
        {
            // Initialise scoreValue
            categoryValue  = String.valueOf(categoryEntry.get(openSegmentNb)) ;
            int nbDigits = ((int) Math.log10(Integer.valueOf(String.valueOf(categoryEntry.get(openSegmentNb))))) + 1 ;
            for (int addSpace = nbDigits ; addSpace < totalDigits ; addSpace++ )
                categoryValue = " ".concat(categoryValue) ;
            
            // skip over bin
            scoreValue = unbinned.get(categoryEntry.get(openSegmentNb)) ;
                    
            // Add bin to dataset
            binned.put(categoryValue, scoreValue) ;
                    
            // prepare for next bin
            openSegmentNb = closeSegmentNb ;    // (int) Math.pow(base, binIndex) - 1 ;    // -1 java counts from 0
            closeSegmentNb = (closeSegmentNb + 1) * base - 1  ;    // (int) Math.pow(base, nextIndex) - 1 ;    // -1 include closeSegmentNB in for-loop
            if (closeSegmentNb > dataSize) 
                closeSegmentNb = dataSize ;
                    
        }

        return binned ;
    }
        
    
    /**
     * Presents quantity scoreName as a function of time/cycle
     * @param scoreName name of quantity on y-axis
     * @param reportName which report are we presenting
     */
    protected void plotChart(String scoreName, String reportName)
    {
        // Get full report reportName
        ArrayList<String> reportArray = getReportArray(reportName) ;
        
        callPlotChart(scoreName, reportArray) ;
    }
    
    /**
     * Generates network diagram from data in hashMapArray.
     * @param xLabel
     * @param yLabel
     * @param hashMapArray 
     */
    protected void plotNetwork(String xLabel, String yLabel, ArrayList<HashMap<Comparable<?>,ArrayList<Comparable<?>>>> hashMapArray)
    {
        chart_awt.callPlotNetwork(chartTitle, hashMapArray, xLabel, yLabel) ;
    }

    /**
     * Presents reportArray as a function of time/cycle.
     * @param scoreName
     * @param reportArray 
     */
    protected void callPlotChart(String scoreName, ArrayList<String> reportArray)
    {
        //LOGGER.info("callPlotChart()") ;
        // Extract data from reportArray
        //LOGGER.info(reportArray.toString());
        ArrayList<ArrayList<String>> dataList = parseReportArray(scoreName, reportArray) ;
        //LOGGER.info(dataList.toString());
        // Send data to be processed and presented
        chart_awt.callPlotChart(chartTitle,dataList,scoreName) ;
    }
    
    /**
     * Plots multiple spline plots of the data in reportArray on the same graph.
     * @param categoryName
     * @param scoreName
     * @param reportArray 
     */
    protected void multiPlotSpline(String categoryName, String scoreName, HashMap<Comparable<?>,Number[]> reportArray)
    {
        plotSpline(categoryName, scoreName, reportArray, new String[] {""}) ;
    }
    
    /**
     * Presents reportArray as a collection of spline plots.
     * @param categoryName
     * @param scoreName
     * @param reportArray (HashMap) Report to be plotted.
     */
    protected void plotSpline(String categoryName, String scoreName, HashMap<Comparable<?>,Number[]> reportArray, String[] legend)
    {
        //LOGGER.info("callPlotChart()") ;
        // Extract data from reportArray
        PolynomialSplineFunction[] functions = generateFunctions(reportArray) ;
        
        //double[] domain = getDomain(reportArray.keySet(),procedure,interval) ;
        double[] domain = new double[reportArray.keySet().size()] ;
        int index = 0 ;
        for ( Object xValue : reportArray.keySet() )
        {
            domain[index] = Double.valueOf(xValue.toString()) ;
            index++ ;
        }
        Arrays.sort(domain);
        
        // Send data to be processed and presented
        chart_awt.callPlotSpline(chartTitle,functions,domain,scoreName,categoryName,legend) ;
    }
    
    
    protected void plotSpline(String categoryName, String scoreName, HashMap<Comparable<?>,Number> reportArray)
    {
        HashMap<Comparable<?>,Number[]> newReportArray = new HashMap<Comparable<?>,Number[]>() ;
        
        for ( Comparable<?> key : reportArray.keySet())
            newReportArray.put(key, new Number[] {reportArray.get(key)}) ;
        
        plotSpline(categoryName, scoreName, newReportArray, new String[] {""}) ;
    }
    
    /**
     * 
     * @param keys
     * @param procedure
     * @param interval
     * @return (Double[]) of points on x-axis for interpolating spline.
     */
    private double[] getDomain(Set keys, String procedure, int interval)
    {
        int keyCount = 0 ;
        double[] domain = new double[keys.size()] ;
        if (procedure.equals(BASE))
        {
            for (int nextValue = 1 ; nextValue <= keys.size() ; nextValue *= interval)
                if (keys.contains(nextValue))
                {
                    domain[keyCount] = Double.valueOf(String.valueOf(nextValue)) ;
                    keyCount++ ;
                }
        }
        else if (procedure.equals(INTERVAL))
        {
            for (int nextValue = 0 ; nextValue < keys.size() ; nextValue += interval )
                if (keys.contains(nextValue))
                {
                    domain[keyCount] = Double.valueOf(String.valueOf(nextValue)) ;
                    keyCount++ ;
                }
        }
        
        return Arrays.copyOf(domain, keyCount) ;
    }
    
    /**
     * Presents reportArray as a function of time/cycle.
     * @param scoreName
     * @param reportArrays 
     * @param legend 
     */
    protected void multiPlotCycleValue(String scoreName, ArrayList<ArrayList<String>> reportArrays, String[] legend)
    {
        //LOGGER.info("callPlotChart()") ;
        // Extract data from reportArray
        ArrayList<ArrayList<String>> scoreList = parseReportArrays(scoreName, reportArrays) ;
        // Send data to be processed and presented
        chart_awt.callPlotChart(chartTitle,scoreList,scoreName,legend) ;
    }
    
    /**
     * Presents reportArray as a function of time/cycle.
     * @param scoreNames
     * @param reportArrays 
     * @param legend 
     */
    protected void multiPlotCycleValue(ArrayList<String> scoreNames, ArrayList<ArrayList<String>> reportArrays, String[] legend)
    {
        //LOGGER.info("callPlotChart()") ;
        // Extract data from reportArray
        ArrayList<ArrayList<String>> scoreList = parseReportArrays(scoreNames, reportArrays) ;
        // Generate approriate scoreName from scoreNames with no repetition
        String scoreName = "" ;
        for (String name : scoreNames)
            if (!scoreName.contains(name))
                scoreName += "/" + name ;
        scoreName = scoreName.substring(1) ;
        // Send data to be processed and presented

        chart_awt.callPlotChart(chartTitle,scoreList,scoreName,legend) ;
    }
    
    /**
     * Presents reportArray as a function of time/cycle.
     * @param scoreNames
     * @param reportArrays 
     */
    protected void multiPlotCycleValue(ArrayList<String> scoreNames, ArrayList<String> reportArrays)
    {
        //LOGGER.info("callPlotChart()") ;
        // Extract data from reportArray
        ArrayList<ArrayList<String>> scoreList = parseReportArray(scoreNames, reportArrays) ;
        
        // Generate approriate scoreName from scoreNames with no repetition
        String[] legend = new String[scoreNames.size()] ;
        String scoreName = "" ;
        String name ;
        for (int scoreIndex = 0 ; scoreIndex < scoreNames.size() ; scoreIndex++ )
        {
            name = scoreNames.get(scoreIndex) ;
            scoreName += "/" + name ;
            legend[scoreIndex] = name ;
            //LOGGER.info(name);
        }
        // Send data to be processed and presented
        chart_awt.callPlotChart(chartTitle,scoreList,scoreName,legend) ;
    }
    
    /**
     * Presents reportArray as a function of time/cycle.
     * @param scoreNames
     * @param reportArrays 
     */
    protected void multiPlotChart(ArrayList<String> scoreNames, ArrayList<Object> reportArrays, String property)
    {
        //LOGGER.info("callPlotChart()") ;
        // Extract data from reportArray
        ArrayList<ArrayList<String>> scoreList = parseReportArray(scoreNames, reportArrays, property) ;
        String[] legend = new String[scoreNames.size()] ;
        for (int index = 0 ; index < legend.length ; index++ )
            legend[index] = scoreNames.get(index) ;
        // Send data to be processed and presented
        chart_awt.callPlotChart(chartTitle,scoreList,property, legend) ;
    }
    
    /**
     * Presents reports in reportArray as a multi-bar plot.
     * @param scoreNames
     * @param reportArrays 
     */
    protected void multiBarPlotValue(ArrayList<String> scoreNames, ArrayList<String> reportArrays)
    {
        //LOGGER.info("callPlotChart()") ;
        // Extract data from reportArray
        parseReportArray(scoreNames, reportArrays) ;
        
        // Generate approriate scoreName from scoreNames with no repetition
        String[] legend = new String[scoreNames.size()] ;
        String scoreName = String.join("/", scoreNames) ;
        /*String name ;
        for (int scoreIndex = 0 ; scoreIndex < scoreNames.size() ; scoreIndex++ )
        {
            name = scoreNames.get(scoreIndex) ;
            scoreName += "/" + name ;
            legend[scoreIndex] = name ;
            //LOGGER.info(name);
        }*/
        ArrayList<ArrayList<Number>> scoreNumbers = new ArrayList<ArrayList<Number>>() ;
        ArrayList<Object> categoryEntry = new ArrayList<Object>() ;
        
        //categoryData.add(categoryEntry) ;
        //chart_awt.callStackedPlotChart(chartTitle,categoryEntry, (ArrayList<ArrayList<Number>>) scoreData, scoreNames.toArray(new String[scoreNames.size()]),"Year") ;
    }
    
    protected void plotHashMapScatter(String categoryName, String scoreName, HashMap<Comparable<?>,ArrayList<Comparable<?>>> hashMapReport )
    {
        chart_awt.callPlotScatterPlot(chartTitle, hashMapReport, scoreName, categoryName) ;
    }
    
    /**
     * Converts values of (HashMap) hashMapReportInteger from Integer to Number
     * and passes it to plotHashMap().
     * TODO: Convert all Methods calling this one to call plotHashMap() and then
     * remove this Method.
     * @param categoryName
     * @param scoreName
     * @param hashMapReport 
     */
    /*protected void plotHashMapInteger(String categoryName, String scoreName, HashMap<Object,Integer> hashMapReport )
    {
        HashMap<Object,Number> numberHashMap = new HashMap<Object,Number>() ;
        for (Object key : hashMapReport.keySet())
            numberHashMap.put(key, (Number) hashMapReport.get(key)) ;
        
        plotHashMap(categoryName,scoreName,numberHashMap) ;
    }*/
    
    /**
     * Sends hashMapReport to chart_awt to be plotted in a stacked bar chart.
     * @param categoryName
     * @param scoreName
     * @param hashMapReport 
     */
    protected void plotHashMap(String categoryName, String scoreName, HashMap<Comparable<?>,Number> hashMapReport ) 
    {
        HashMap<Comparable<?>,Number[]> newHashMapReport = new HashMap<Comparable<?>,Number[]>() ;
        
        for (Comparable key : hashMapReport.keySet())
            newHashMapReport.put(key, new Number[] {hashMapReport.get(key)}) ;
        
        LOGGER.info(newHashMapReport.toString()) ;
        
        plotHashMap(categoryName, new String[] {scoreName}, newHashMapReport) ;
    }
    
    
    /**
     * Sends hashMapReport to chart_awt to be plotted in a stacked bar chart.
     * @param categoryName
     * @param scoreNames
     * @param hashMapReport
     */
    protected void plotHashMap(String categoryName, String[] scoreNames, HashMap<Comparable<?>,Number[]> hashMapReport )
    {
        LOGGER.severe("plotHashMap()") ;
        //ArrayList<String> categoryInteger = new ArrayList<String>() ;
        ArrayList<ArrayList<Number>> scoreNumbers = new ArrayList<ArrayList<Number>>() ;
        ArrayList<Object> categoryEntry = new ArrayList<Object>() ;
        Number[] hashMapValue ;
        
        // Put keys in order
        for (Object key : hashMapReport.keySet())
        {
            if (key == null)
                continue ;
            categoryEntry.add(key) ;
        }
        categoryEntry.sort(null);
        
        for (Object key : categoryEntry)
        {
            ArrayList<Number> scoreEntry = new ArrayList<Number>() ;
            hashMapValue = hashMapReport.get(key) ;
            scoreEntry.addAll(Arrays.asList(hashMapValue)) ;
            scoreNumbers.add((ArrayList<Number>) scoreEntry.clone()) ;
        }
        //categoryData.add(categoryEntry) ;
        chart_awt.callStackedPlotChart(chartTitle,categoryEntry,scoreNumbers,scoreNames,categoryName) ;
    }
    
    /**
     * Plots values contained in a HashMap of Object to String. The keys are usually 
     * years while the String contains the properties and their values.
     * @param report
     * @param yLabel
     * @param xLabel
     * @param legend 
     */
    protected void plotHashMapString(HashMap<?,String> report, String yLabel, String xLabel, String[] legend)
    {
        // Extract data from reportArray
        XYSeriesCollection xySeriesCollection = parseReportHashMap(report, legend) ;

        // TODO: may have to convert XYSeriesCollection to XYIntervalSeriesCollection

        String[] newLegend = legend ;
        
        if (PLOT_FILE)
        {
            HashMap<Comparable<?>,Number[]> dataReport = READ_HASHMAP_NUMBER_ARRAY_CSV(FILENAME) ;
            xySeriesCollection = EXPAND_DATASET(xySeriesCollection,dataReport, DATA_SCORE) ;
            
        }

        // Send data to be processed and presented
        chart_awt.plotLineChart(chartTitle,xySeriesCollection, yLabel, xLabel, newLegend) ;
    }

    /**
     * - Calls plotLineChart and setting the appropriate fields to draw a shaded graph
     * @param report: type HashMap<String, HashMap> contains ConfidenceInterval/Range (String) used to draw shaded bounds
     * @param yLabel
     * @param xLabel
     * @param legend
     */
    protected void plotShadedHashMapStringCI(HashMap<String,HashMap<String,String[]>> report, String yLabel, String xLabel, String[] legend) 
    {
        // Extract data from reportArray
        XYIntervalSeriesCollection xyIntervalSeriesCollection = parseReportHashMapError(report, legend) ;

        Boolean drawShadedRegion = ConfigLoader.getMethodVariableBoolean("presenter", "plotShadedHashMapStringCI", "drawShadedRegion") ;
        
        
        setDrawError(false) ;
        setDrawPoints(true);
        
        if (drawShadedRegion)
            setErrorType(SHADED_REGION) ;
        else
        	setErrorType(ERROR_INTERVALS);
            
        // Send data to be processed and presented
        chart_awt.plotLineChart(chartTitle,xyIntervalSeriesCollection, yLabel, xLabel, legend) ;
    }

    
    protected void plotHashMapStringCI(HashMap<String,HashMap<String,String[]>> report, String yLabel, String xLabel, String[] legend)
    {
        // Extract data from reportArray
        XYIntervalSeriesCollection xyIntervalSeriesCollection = parseReportHashMapError(report, legend) ;
            
        // Send data to be processed and presented
        chart_awt.plotLineChart(chartTitle,xyIntervalSeriesCollection, yLabel, xLabel, legend) ;
    }
    
    protected void plotSortedHashMap(HashMap<Object,String> report, String yLabel, String xLabel, String[] legend)
    {
        
    }
    
    protected void plotHashMapArea(String categoryName, String scoreName, HashMap<Object,Number[]> hashMapReport )
    {
        Double xTrack ;    // Tracks where the x-coordinate of the variable-width bars
        double gap = 0.02 ;    // Gap between bars in barChart
        //ArrayList<String> categoryInteger = new ArrayList<String>() ;
        ArrayList<Number[]> scoreNumbers = new ArrayList<Number[]>() ;
        ArrayList<Object> categoryEntry = new ArrayList<Object>() ;
        Number[] hashMapValue ;
        Number[] scoreEntry ;
            
        // Put keys in order
        for (Object key : hashMapReport.keySet())
        {
            if (key.equals(null))
                continue ;
            categoryEntry.add(key) ;
        }
        categoryEntry.sort(null);
        
        hashMapValue = hashMapReport.get(categoryEntry.get(0)) ;
        xTrack = - ((Double) hashMapValue[0])/2.0 ;
        for (Object key : categoryEntry)
        {
            // left-hand corner
            scoreEntry = new Number[2] ;
            hashMapValue = hashMapReport.get(key) ;
            Number score = hashMapValue[1] ;
            scoreEntry[0] = xTrack ;
            scoreEntry[1] = score.doubleValue() ;
            scoreNumbers.add(scoreEntry.clone()) ;
            // centre
            xTrack += ((Double) hashMapValue[0]) ;
            scoreEntry = new Number[2] ;
            scoreEntry[0] = xTrack ;
            scoreEntry[1] = score.doubleValue() ;
            scoreNumbers.add(scoreEntry.clone()) ;
            // right-hand corner
            xTrack += ((Double) hashMapValue[0]) ;
            scoreEntry = new Number[2] ;
            scoreEntry[0] = xTrack ;
            scoreEntry[1] = score.doubleValue() ;
            scoreNumbers.add(scoreEntry.clone()) ;
            // Go to zero and make gap
            scoreEntry = new Number[2] ;
            scoreEntry[0] = xTrack ;
            scoreEntry[1] = 0.0 ;    // Math.pow(10, -3) ;
            scoreNumbers.add(scoreEntry.clone()) ;
            xTrack += gap ;
            scoreEntry = new Number[2] ;
            scoreEntry[0] = xTrack ;
            scoreEntry[1] = 0.0 ;    // Math.pow(10, -3) ;
            scoreNumbers.add(scoreEntry.clone()) ;
            
           // LOGGER.info(String.valueOf(score));
        }
        
        
        chart_awt.callAreaPlotChart(chartTitle, categoryEntry, scoreNumbers, scoreName, categoryName);
    }
    
    /**
     * Presents scoreName as a function of categoryName from reportArray[cycle]
     * or HashMap
     * @param categoryNames
     * @param scoreName
     * @param xLabel
     * @param record 
     */
    protected void callPlotChartDefault(String[] categoryNames, String scoreName, String xLabel, String record)
    {
        // Extract data from report
        ArrayList<ArrayList<Number>> categoryData = parseRecord(categoryNames, record) ;
        //String[] categoryList = new String[categoryNames.size()] ;
        ArrayList<Object> categoryList = new ArrayList<Object>() ;
        categoryList.addAll(Arrays.asList(categoryNames)); // + GROUP + scoreName) ;
        
        // Send data to be processed and presented
        //chart_awt.plotBarChart(chartTitle, categoryData, scoreName, xLabel) ;
        chart_awt.callStackedPlotChart(chartTitle, categoryList,categoryData, new String[] {},xLabel) ;
        //callPlotChart(chartTitle,categoryData.get(0),scoreData.get(0),scoreName,categoryNames) ;
        //chart_awt.callPlotChart(chartTitle,categoryData,scoreName,xLabel,categoryNames) ;
    }
    
    /**
     * 
     * @param scoreName (String) 
     * @param reportArray (ArrayList(ArrayListObject)) 
     * @return (String[]) Each entry is String.valueOf(the number of entries in each entry of reportArray)
     */
    protected ArrayList<String> prepareEventsPerCycle(String scoreName, ArrayList<ArrayList<Comparable<?>>> reportArray)
    {
        ArrayList<String> eventsPerCycle = new ArrayList<String>() ;
        scoreName += ":" ;
        
        for (ArrayList<Comparable<?>> report : reportArray)
        {
            eventsPerCycle.add(scoreName + Integer.toString(report.size()) + " ") ;
        }
        
        return eventsPerCycle ;
        
    }
    
    /**
     * Calls plotCycleValue
     * @param scoreName
     * @param record
     */
    public void plotValues(String scoreName, String record)
    {
        ArrayList<String> recordList = new ArrayList<String>() ;
        recordList.add(record) ;
        callPlotChart(scoreName,recordList) ;
    }            
    
    /**
     * Calls plotCycleValue
     * @param record
     * @param yLabel
     * @param xLabel
     */
    public void multiPlotValues(String record, String yLabel, String xLabel)
    {
        DefaultCategoryDataset dataset = parseSortedRecord(record) ;
      // logger.log(level.info, "{0}",dataset);
        chart_awt.plotBarChart(chartTitle, dataset, yLabel, xLabel) ;
    }            
    
    public void plotEventsPerCycle(String scoreName, ArrayList<ArrayList<Comparable<?>>> reportArray)
    {
        ArrayList<String> eventsPerCycle = prepareEventsPerCycle(scoreName,reportArray) ;
        
        callPlotChart(scoreName,eventsPerCycle) ;
    }

    /**
     * Calls callPlotChart
     * @param scoreName
     * @param reportArray 
     */
    public void plotCycleValue(String scoreName, ArrayList<String> reportArray)
    {
        //LOGGER.info("plotCycleValue") ;
        callPlotChart(scoreName,reportArray) ;
    }            
    
    public void multiPlotCycleValue(String property, ArrayList<String> reportArrays)
    {
        ArrayList<ArrayList<String>> scoreList = parseReportArray(property, reportArrays) ;
    }
            
    /**
     * Calls multiBarPlotValue after converting scoreName to an ArrayList.
     * @param scoreName
     * @param reportArrays 
     */
    public void multiBarPlotValue(String scoreName, ArrayList<String> reportArrays)
    {
        ArrayList<String> scoreNames = new ArrayList<String>() ;
        scoreNames.add(scoreName) ;
        //LOGGER.info("plotCycleValue") ;
        multiBarPlotValue(scoreNames,reportArrays) ;
    }            
            
    /**
     * Uses reflect to call Method prepareReportNameReport()
     * @param reportName
     * @return reportArray returned by prepareReportNameReport()
     */
    private ArrayList<String> getReportArray(String reportName)
    {
        ArrayList<String> reportArray = new ArrayList<String>() ;
        
        // Name of Method which provides report
        String reportMethodName = "prepare" + reportName + "Report" ;
        try
        {
            Class reporterClass = reporter.getClass().asSubclass(reporter.getClass()) ;
            Method prepareReportMethod = reporterClass.getMethod(reportName) ;
            reportArray = (ArrayList<String>) prepareReportMethod.invoke(reporter) ;
        }
        catch ( Exception e )
        {
            LOGGER.severe(e.getLocalizedMessage());
        }
        return reportArray ;
    }
    
    /**
     * Converts sorted HashMaps to the format used for plotting.
     * @param sortedHashMap
     * @return (HashMap) unsortedKey maps to (Number[]) values in order determined by 
     * looping through keySet.
     */
    public HashMap<Comparable<?>,Number[]> prepareSortedHashMap(HashMap<Comparable,HashMap<Comparable,Number>> sortedHashMap)
    {
        HashMap<Comparable<?>,Number[]> plottingHashMap = new HashMap<Comparable<?>,Number[]>() ;
        
        HashMap<Comparable,Number> subHashMap ;
        
        int arraySize = sortedHashMap.keySet().size() ;
        int nbKeys = 0 ;
        for (Comparable sortingKey : sortedHashMap.keySet())
        {
            subHashMap = sortedHashMap.get(sortingKey) ;
            for (Comparable subKey : subHashMap.keySet())
            {
                if (!plottingHashMap.containsKey(subKey))
                {
                    plottingHashMap.put(subKey, new Number[arraySize]) ;
                    // If subKey not present under earlier sortingKey?
                    for (int index = 0 ; index < nbKeys ; index++ )
                        plottingHashMap.get(subKey)[index] = 0 ;
                }
                Number entry = subHashMap.get(subKey) ;
                if (entry == null)    // If subKey missing under this sortingKey 
                    entry = 0 ;
                plottingHashMap.get(subKey)[nbKeys] = entry ;
            }
            nbKeys++ ;
        }
        
        return plottingHashMap ;
    }
    
    /**
     * Extracts category (x) and score (y) data and records in corresponding fields
     * @param categoryName
     * @param scoreName
     * @param report 
     */
    private void parseRecord(String categoryName, String scoreName, String report)
    {
        parseRecord(new String[] {categoryName}, scoreName, report) ;
    }
    
    /**
     * Extracts category (x) and score (y) data and records in corresponding fields
     * @param categoryName
     * @param scoreName
     * @param record 
     */
    private DefaultCategoryDataset parseRecord(String[] categoryNames, String scoreName, String record)
    {
        return parseRecord(categoryNames, new String[] {scoreName}, record) ;
    }
    
    /**
     * Extracts category (x) and score (y) data and records in corresponding fields
     * @param categoryName
     * @param scoreNames
     * @param record 
     */
    private DefaultCategoryDataset parseRecord(String[] categoryNames, String[] scoreNames, String record)
    {        
        DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset() ;
        
        String categoryValue ;
        String categoryString ;
        String scoreValueString ;
        Number scoreValue = 0 ;
        
        ArrayList<String> categoryList = (ArrayList<String>) Arrays.asList(categoryNames) ;
        for (String categoryName : categoryNames)
            for (String scoreName : scoreNames)
            {
                int categoryIndex = Reporter.INDEX_OF_PROPERTY(categoryName,record) ;

                categoryString = Reporter.EXTRACT_LABEL_STRING(categoryName, record, categoryList) ;
                categoryValue = Reporter.EXTRACT_VALUE(categoryName, record, categoryIndex) ;
                scoreValueString = Reporter.EXTRACT_VALUE(scoreName, record, categoryIndex) ;
                if (int.class.isInstance(scoreValueString))
                    scoreValue = (Integer) Integer.valueOf(scoreValueString) ;
                else    // scoreValue is double
                    scoreValue = (Double) Double.valueOf(scoreValueString) ;
                categoryDataset.addValue( scoreValue, categoryName, categoryValue ) ;
            }
        
        return categoryDataset ;
    }
    
    private DefaultCategoryDataset parseSortedRecord(String record)
    {
        DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset() ;
        Number scoreValue ;
        
        ArrayList<String> sortedNames = Reporter.IDENTIFY_PROPERTIES(record) ;
        for (String name : sortedNames)
        {
            //LOGGER.info(name);
            String[] nameParts = name.split(GROUP) ;
            String property = nameParts[0] ;
            
            String scoreString = Reporter.EXTRACT_VALUE(name,record) ;
            if (int.class.isInstance(scoreString) || Integer.class.isInstance(scoreString)) 
                scoreValue = Integer.valueOf(scoreString) ;
            else
                scoreValue = Double.valueOf(scoreString) ;

            categoryDataset.addValue( scoreValue, "", property ) ;

        }
        
        return categoryDataset ;
    }
    
    /**
     * Extracts one value for scoreName from record.
     * @param scoreNames
     * @param record 
     */
    private ArrayList<ArrayList<Number>> parseRecord(String[] scoreNames, String record)
    {
        ArrayList<ArrayList<Number>> scoreList = new ArrayList<ArrayList<Number>>() ;
        ArrayList<Number> plotList  ;
        
        String valueString; 
        Number value ;
        for (String scoreName : scoreNames)
        {
            // LOGGER.info(scoreName);
            plotList = new ArrayList<Number>() ;
            valueString = Reporter.EXTRACT_VALUE(scoreName,record) ;
            if (Integer.class.isInstance(valueString))
                value = Integer.valueOf(valueString) ;
            else    // if Double
                value = Double.valueOf(valueString) ;
            plotList.add(value) ;
            scoreList.add((ArrayList<Number>) plotList.clone()) ;
        }
        
        return scoreList ;
    }

    /**
     * Extracts one value for scoreName from each report cycle.
     * Intended for plots over time.
     * @param scoreNames
     * @param report 
     */
    private ArrayList<ArrayList<Object>> parseReportArray(String[] scoreNames, ArrayList<Object> report)
    {       
        ArrayList<ArrayList<Object>> scoreList = new ArrayList<ArrayList<Object>>() ;
        ArrayList<Object> plotList = new ArrayList<Object>() ;
        
        String value; 
        
        for (Object record : report)
        {
            for (String scoreName : scoreNames)
            {
                value = Reporter.EXTRACT_VALUE(scoreName,String.valueOf(record)) ;
                plotList.add(value) ;
            }
        }
        scoreList.add(plotList) ;
        
        return scoreList ;
    }

    /**
     * Extracts one value for scoreName from record.
     * @param scoreName
     * @param record 
     */
    private ArrayList<ArrayList<String>> parseRecord(String scoreName, String record)
    {
        return parseReportArray(scoreName, (ArrayList<String>) Arrays.asList(new String[] {record})) ;
    }

    /**
     * Extracts one value for scoreName from each report cycle.
     * Intended for plots over time.
     * @param scoreName
     * @param report 
     */
    private ArrayList<ArrayList<String>> parseReportArray(String scoreName, ArrayList<String> report)
    {       
        ArrayList<ArrayList<String>> scoreList = new ArrayList<ArrayList<String>>() ;
        // LOGGER.info(report.toString());
        ArrayList<String> plotList = new ArrayList<String>() ;
        for (Object record : report)
        {
            String value = Reporter.EXTRACT_VALUE(scoreName,String.valueOf(record)) ;
            plotList.add(value) ;
        }
        scoreList.add(plotList) ;
        
        return scoreList ;
    }

    /**
     * Extracts one value for scoreName from each report cycle of each report.
     * Intended for plots over time.
     * @param scoreName
     * @param reports 
     */
    private ArrayList<ArrayList<String>> parseReportArrays(String scoreName, ArrayList<ArrayList<String>> reports)
    {       
        ArrayList<ArrayList<String>> scoreList = new ArrayList<ArrayList<String>>() ;
        ArrayList<String> plotList ;
        
        for (ArrayList<String> report : reports)
        {
            plotList = new ArrayList<String>() ;
            for (Object record : report)
            {
                String value = Reporter.EXTRACT_VALUE(scoreName,String.valueOf(record)) ;
                plotList.add(value) ;
            }
            scoreList.add(plotList) ;
        }
        return scoreList ;
    }

    /**
     * Extracts one value for scoreName from each report cycle of each report.
     * Intended for plots over time.
     * @param scoreName
     * @param reports 
     */
    private ArrayList<ArrayList<String>> parseReportArrays(ArrayList<String> scoreNames, ArrayList<ArrayList<String>> reports)
    {       
        ArrayList<ArrayList<String>> scoreList = new ArrayList<ArrayList<String>>() ;
        ArrayList<String> plotList ;
        String scoreName ;
        ArrayList<String> report ;
        for (int index = 0 ; index < scoreNames.size() ; index++ )
        {
            plotList = new ArrayList<String>() ;
            scoreName = scoreNames.get(index) ;
            report = reports.get(index) ;
            
            // An empty entry in reports indicates that the previous report should be used.
            if (report.isEmpty())
                report = reports.get(index - 1) ;
            
            // Add value to plotArray for scoreData
            for (Object record : report)
            {
                String value = Reporter.EXTRACT_VALUE(scoreName,String.valueOf(record)) ;
                plotList.add(value) ;
            }
            scoreList.add(plotList) ;
        }
        return scoreList ;
    }
    
    /**
     * Extracts plotable values from report and puts in XYSeriesCollection format.
     * @param report
     * @param legend
     * @return 
     */
    private DefaultCategoryDataset parseSortedHashMap(HashMap<Object,String> report, String[] legend)
    {
        DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset() ;
        
        String property ;
        Number scoreValue ;
        Number categoryValue ;
        
        int plotTotal ;
        if (legend.length == 0)
            legend = new String[] {""} ;
        plotTotal = legend.length ;

        for (int plotIndex = 0 ; plotIndex < plotTotal ; plotIndex++ )
        {
            property = legend[plotIndex] ;

            for (Object category : report.keySet())
            {

                String scoreString = Reporter.EXTRACT_VALUE(legend[plotIndex],report.get(category)) ;
                if (int.class.isInstance(scoreString) || Integer.class.isInstance(scoreString)) 
                    scoreValue = Integer.valueOf(scoreString) ;
                else
                    scoreValue = Double.valueOf(scoreString) ;

                categoryDataset.addValue( scoreValue, "", category.toString() + GROUP + property ) ;
            }
            
        }
        return categoryDataset ;
    }
    
    /**
     * Extracts plottable values from report and puts in XYSeriesCollection format.
     * @param report
     * @param legend
     * @return 
     */
    private XYSeriesCollection parseReportHashMap(HashMap<?,String> report, String[] legend)  // 
    {       
        XYSeriesCollection xySeriesCollection = new XYSeriesCollection() ;
        String property ;
        // Sorted ArrayList of HashMap keys
        ArrayList<Object> categoryEntry = new ArrayList<Object>() ;
        for (Object key : report.keySet()) 
            categoryEntry.add(key) ;
        categoryEntry.sort(null) ;
        
        // ArrayList<String> categoryData = data.get(0) ;
        // ArrayList<String> scoreData = data.get(1) ;
        Number scoreValue ;
        Number categoryValue ;
        
        int plotTotal ;
        if (legend.length == 0)
            legend = new String[] {""} ;
        plotTotal = legend.length ;

        for (int plotIndex = 0 ; plotIndex < plotTotal ; plotIndex++ )
        {
            property = legend[plotIndex] ;
            XYSeries xySeries = new XYSeries(property) ;

            for (Object category : categoryEntry)
            {
                String scoreString = Reporter.EXTRACT_VALUE(property,report.get(category)) ;
                if (int.class.isInstance(scoreString) || Integer.class.isInstance(scoreString)) 
                    scoreValue = Integer.valueOf(scoreString) ;
                else
                    scoreValue = Double.valueOf(scoreString) ;

                if (int.class.isInstance(category) || Integer.class.isInstance(category)) 
                {
                    categoryValue = Integer.valueOf(category.toString()) ;
                    xySeries.add((Integer) categoryValue, scoreValue, false);
                }
                else
                {
                    categoryValue = Double.valueOf(category.toString()) ;
                    xySeries.add((Double) categoryValue, scoreValue, false);
                }
            }
            try
            {
                xySeriesCollection.addSeries((XYSeries) xySeries.clone()) ;
            }
            catch ( CloneNotSupportedException cnse )
            {
                LOGGER.log(Level.SEVERE, cnse.toString());
            }

        }
        
        return xySeriesCollection ;
    }

    private XYIntervalSeriesCollection parseReportHashMapError(HashMap<String, HashMap<String,String[]>> report, String[] legend) {
        XYIntervalSeriesCollection xyIntervalSeriesCollection = new XYIntervalSeriesCollection() ;

        // Sorted ArrayList of HashMap keys
        ArrayList<String> categoryEntry = new ArrayList<String>() ;
        for (String key : report.keySet()) 
            categoryEntry.add(key) ;
        categoryEntry.sort(null) ;

        

        if (legend.length == 0)
            legend = new String[] {""} ;

        // if (legend.length == report.size()) {
        //     HashMap<String, HashMap<String, String[]>> legendIncorporatedReport = new HashMap<String, HashMap<String, String[]>>();
        //     for (int i = 0; i < legend.length; ++i) {

        //     }
        // }

        for (String property : categoryEntry) {
            HashMap<String, String[]> propertyToCategories = report.get(property);

            // set property
            XYIntervalSeries xyIntervalSeries = new XYIntervalSeries(property);

            // TODO: at the moment, we assume data is valid, checks can and should be added
            for (String category : propertyToCategories.keySet()) {
                String[] extractedYAndRange = (String[]) report.get(property).get(category);
                double xValue = Double.valueOf(category);
                
                // Needed for gaps in the data, such as in gonoGoneWild.csv
                // TODO: There is probably a better way to do this
                if (extractedYAndRange[0].isEmpty())
                	continue ;

                // TODO: these are hard-coded at the moment - extract based on col
                LOGGER.log(Level.INFO,"{0} {1} {2}", (Object[]) extractedYAndRange);
                LOGGER.log(Level.INFO,"{0} {1}", new Object[] {property,xValue});
                double yMean = Double.valueOf(extractedYAndRange[0]);
                double yLower95 = Double.valueOf(extractedYAndRange[1]);
                double yUpper95 = Double.valueOf(extractedYAndRange[2]);

                xyIntervalSeries.add(xValue, xValue, xValue, yMean, yLower95, yUpper95);
            }

            try
            {
                xyIntervalSeriesCollection.addSeries((XYIntervalSeries) xyIntervalSeries.clone()) ;
            }
            catch ( CloneNotSupportedException cnse )
            {
                LOGGER.log(Level.SEVERE, cnse.toString());
            
            }
        }


        return xyIntervalSeriesCollection;
    }

    /**
     * Extracts one value for scoreName from each report cycle of each report.
     * Intended for plots over time.
     * @param scoreName
     * @param reports 
     */
    private ArrayList<ArrayList<String>> parseReportArray(ArrayList<String> scoreNames, ArrayList<String> report)
    {       
        ArrayList<ArrayList<String>> scoreList = new ArrayList<ArrayList<String>>() ;
        ArrayList<Object> plotList ;
        
        for (String scoreName : scoreNames)
        {
            plotList = new ArrayList<Object>() ;
            
            // Add value to plotArray for scoreData
            for (Object record : report)
            {
                String value = Reporter.EXTRACT_VALUE(scoreName,String.valueOf(record)) ;
                plotList.add(value) ;
            }
            scoreList.add((ArrayList<String>) plotList.clone()) ;
        }
        return scoreList ;
    }

    /**
     * Extracts one value for scoreName from each report cycle of each report.
     * Intended for plots over time.
     * @param scoreName
     * @param reports 
     */
    private ArrayList<ArrayList<String>> parseReportArray(ArrayList<String> scoreNames, ArrayList<Object> report, String property)
    {       
        ArrayList<ArrayList<String>> scoreList = new ArrayList<ArrayList<String>>() ;
        //ArrayList<Object> plotList ;
        String scoreRecord = "" ;
        String value = "" ;
        for (String scoreName : scoreNames)
        {
            ArrayList<String> plotList = new ArrayList<String>() ;
            
            // Add value to plotArray for scoreData
            for (Object record : report)
            {
                scoreRecord = Reporter.EXTRACT_LABEL_STRING(scoreName, String.valueOf(record),scoreNames) ;
                value = Reporter.EXTRACT_VALUE(property,scoreRecord) ;
                plotList.add(value) ;
            }
          // logger.log(level.info, "{0} {1}", new Object[] {scoreName, value});
            scoreList.add((ArrayList<String>) plotList.clone()) ;
        }
        
        return scoreList ;
    }

    /**
     * True by default
     * If set to false -> graph will not draw points
     * @param val
     */
    public void setDrawPoints(boolean val) 
    {
        this.drawPoints = val;
    }

    public void setDrawError(boolean val) 
    {
        this.drawError = val;
    }

    public boolean getDrawCI() {
        return this.drawError;
    }

    public void setErrorType(String val) {
        this.lineGraphErrorType = val;
    }


    /**
     * private class to specifically handle JFreeChart functions such as
     * handling Datasets and plotting charts.
     */
    private class BarChart_AWT {
   
        String chartTitle ;
        
        private BarChart_AWT()
        {
        	
        }
        
        private BarChart_AWT( String applicationTitle , String chartTitle ) 
        {
            this.chartTitle = chartTitle ;
        }
        
        /**
         * Invokes plotLineChart() for networks (eg. Agent-to-Agent) after invoking
         * createHubData() to generate Dataset
         * @param chartTitle
         * @param networkData
         * @param hub
         * @param hubCycle
         * @param yLabel
         * @param xLabel 
         */
        private void callPlotNetwork(String chartTitle, ArrayList<HashMap<Comparable<?>,ArrayList<Comparable<?>>>> networkData, 
                String yLabel, String xLabel)
        {
            XYSeriesCollection dataset = createHubDataset(networkData) ;
            plotLineChart(chartTitle, dataset, yLabel, xLabel, new String[] {""}) ;
        }

        /**
         * Calls Method callPlotChart() for plots over time after generating dataset without legend
         * @param chartTitle
         * @param dataArray
         * @param yLabel 
         */
        private void callPlotChart(String chartTitle, ArrayList<ArrayList<String>> dataArray, String yLabel)
        {
            callPlotChart(chartTitle, dataArray, yLabel, new String[] {""}) ;
        }
        
        /**
         * Calls Method callPlotChart() for plots over time after generating dataset without legend
         * @param chartTitle
         * @param dataArray
         * @param yLabel 
         */
        private void callPlotSpline(String chartTitle, PolynomialSplineFunction[] functions, double[] domain, String yLabel, String xLabel)
        {
            callPlotSpline(chartTitle, functions, domain, yLabel, xLabel, new String[] {""}) ;
        }
        
        /**
         * Calls Method plotLineChart() for plots over time after generating dataset
         * @param chartTitle
         * @param dataArray
         * @param yLabel 
         */
        private void callPlotChart(String chartTitle, ArrayList<ArrayList<String>> dataArray, String yLabel, String[] legend)
        {
            callPlotChart(chartTitle, dataArray, yLabel, "day", legend) ;
        }
        
        /**
         * Calls Method plotLineChart() for plots over time after generating dataset
         * @param chartTitle
         * @param dataArray 
         * @param yLabel 
         */
        private void callPlotChart(String chartTitle, ArrayList<ArrayList<String>> dataArray, String yLabel, String xLabel, String[] legend)
        {
            //LOGGER.info("callPlotChart()") ;
            XYSeriesCollection dataset = createXYDataset(dataArray,legend) ;
            plotLineChart(chartTitle, dataset, yLabel, xLabel, legend) ;
        }
        
        /**
         * Calls Method plotLineChart() for plots over time after generating dataset
         * @param chartTitle
         * @param function (PolynomialSplineFunction) Spline for plotting curves
         * @param domain (double[]) Points on x-axis for plotting.
         * @param yLabel 
         */
        private void callPlotSpline(String chartTitle, PolynomialSplineFunction[] functions, double[] domain, String yLabel, String xLabel, String[] legend)
        {
            //LOGGER.info("callPlotChart()") ;
            XYSeriesCollection dataset = createXYDataset(functions, domain, legend) ;
            plotLineChart(chartTitle, dataset, yLabel, xLabel, legend) ;
        }
        
        /**
         * Creates scatter plot by generating a suitable Dataset from HashMaps in dataArray and feeding it plotScatterPlot().
         * @param chartTitle
         * @param dataArray
         * @param yLabel
         * @param xLabel 
         */
        private void callPlotScatterPlots(String chartTitle, ArrayList<HashMap<Comparable<?>,ArrayList<Comparable<?>>>> dataArray, String yLabel, String xLabel)
        {
            XYSeriesCollection dataset = createScatterPlotDataset(dataArray, chartTitle) ;
            plotScatterPlot(chartTitle, dataset, yLabel, xLabel) ;
        }
       
        /**
         * Creates scatter plot by generating a suitable Dataset from dataHashMap and feeding it plotScatterPlot().
         * @param chartTitle
         * @param dataHashMap
         * @param yLabel
         * @param xLabel 
         */
        private void callPlotScatterPlot(String chartTitle, HashMap<Comparable<?>,ArrayList<Comparable<?>>> dataHashMap, String yLabel, String xLabel)
        {
            XYSeriesCollection dataset = createScatterPlotDataset(dataHashMap, chartTitle) ;
            plotScatterPlot(chartTitle, dataset, yLabel, xLabel) ;
        }
       
        /**
         * Calls method plotChart for within-cycle plots after generating dataset
         * @param chartTitle
         * @param categoryArray
         * @param scoreArray
         * @param yLabel
         * @param xLabel 
         */
        private void callPlotChart(String chartTitle, ArrayList<Object> categoryArray, ArrayList<Object> scoreArray, String yLabel, String xLabel)
        {
            CategoryDataset dataset = createDataset(xLabel, categoryArray, scoreArray) ;
            plotBarChart(chartTitle, dataset, yLabel, xLabel) ;
        }
        
        /**
         * Calls plotStackedBarChart() after generating dataset
         * @param chartTitle
         * @param categoryList
         * @param scoreLists
         * @param scoreNames
         * @param xLabel 
         */
        private void callStackedPlotChart(String chartTitle, ArrayList<Object> categoryList, ArrayList<ArrayList<Number>> scoreLists, String[] scoreNames, String xLabel)
        {
            // LOGGER.info("callPlotChartInteger()") ;
            boolean bin = false ;    // Comparable.class.isInstance(categoryList.get(0)) ;
            boolean readInputFile = false ;
            DefaultCategoryDataset dataset ;
            
            String[] finalNames = new String[scoreNames.length + 1] ;
            if (PLOT_FILE)
            {
                // Data from file
                HashMap<Comparable<?>,Number[]> dataReport = READ_HASHMAP_NUMBER_ARRAY_CSV(FILENAME) ;
              // logger.log(level.info, "{0}", dataReport);
                // Match categories to input file
                if (readInputFile && (categoryList.size() > dataReport.size()))
                {
                    ArrayList<Object> loseCategories = new ArrayList<Object>() ;
                    for (Object category : categoryList)
                        if (!dataReport.containsKey(category))
                            loseCategories.add(category) ;
                    categoryList.removeAll(loseCategories) ;
                }
                
                dataset = createDataset(scoreNames, categoryList, scoreLists,bin) ;

                //LOGGER.log(Level.INFO, "{0}", dataset);
                dataset = EXPAND_DATASET(dataset,dataReport, DATA_SCORE) ;
                //dataset.removeRow(DATA_SCORE[0]) ;
                //dataset.removeRow(DATA_SCORE[1]) ;
                //dataset.removeRow(DATA_SCORE[2]) ;
                //dataset.removeRow(DATA_SCORE[3]) ;
              // logger.log(level.info, "{0}", dataset);
                
                finalNames = new String[scoreNames.length + 1] ; // DATA_SCORE.length] ;
                for (int scoreIndex = 0 ; scoreIndex < scoreNames.length ; scoreIndex++ )
                    finalNames[scoreIndex] = scoreNames[scoreIndex] ;
                
                finalNames[scoreNames.length] = DATA_SCORE[1].concat(GROUP).concat("DATA") ;
                //for (int dataIndex = 0 ; dataIndex < DATA_SCORE.length ; dataIndex++ )
                  //  finalNames[scoreNames.length + dataIndex] = DATA_SCORE[dataIndex].concat(GROUP).concat("DATA") ;
                /*if (bin)
                    for (int scoreIndex = 0 ; scoreIndex < scoreNames.length ; scoreIndex++ )
                        scoreNames[scoreIndex] = "Log() ".concat(scoreNames[scoreIndex]) ;*/
                //LOGGER.log(Level.INFO, "{0}", finalNames);
                }
            else 
            {
                dataset = createDataset(scoreNames, categoryList, scoreLists,bin) ;
                finalNames = scoreNames ;
            }
            plotStackedBarChart(chartTitle, dataset, finalNames, xLabel) ;
        }
        
        /**
         * Invokes plotAreaChart after preparing suitable (XYDataset) dataset.
         * @param chartTitle
         * @param categoryArray
         * @param scoreArray
         * @param scoreName
         * @param xLabel 
         */
        private void callAreaPlotChart(String chartTitle, ArrayList<Object> categoryArray, ArrayList<Number[]> scoreArray, String scoreName, String xLabel)
        {
            //LOGGER.info("callPlotChartInteger()") ;
            String[] legend = new String[] {""} ;
            XYDataset dataset = createAreaPlotDataset(scoreArray, legend) ;
            plotAreaChart(chartTitle, dataset, scoreName, xLabel, categoryArray, legend) ;
        }
        
        
        
        /**
         * Redundant version of callPlotChart allowing for scoreArray to be integer instead of Object.
         * TODO: Refactor and remove, replace with callPlotChart() 
         * @param chartTitle
         * @param categoryArray
         * @param scoreArray
         * @param yLabel
         * @param xLabel 
         */
        /*private void callPlotChartInteger(String chartTitle, ArrayList<Object> categoryArray, ArrayList<Number> scoreArray, String yLabel, String xLabel)
        {
            //LOGGER.info("callPlotChartInteger()") ;
            CategoryDataset dataset = createDatasetInteger(xLabel, categoryArray, scoreArray) ;
            plotBarChart(chartTitle, dataset, yLabel, xLabel) ;
        }*/
        
        /**
         * Generates plot of dataset
         * @param chartTitle
         * @param dataset
         * @param yLabel
         * @param xLabel 
         */
        private void plotBarChart(String chartTitle, CategoryDataset dataset, String yLabel, String xLabel)
        {
            // LOGGER.info("plotBarChart()");
            JFreeChart barChart = ChartFactory.createBarChart(chartTitle,xLabel,
                yLabel,dataset,PlotOrientation.VERTICAL,true, true, false);
            
            barChart.getPlot().setBackgroundPaint(Color.WHITE) ;

            /* !!!
            * * * * * * * * * * * * * * * * * * * * *
            *        BarChart Render Settings       *
            * * * * * * * * * * * * * * * * * * * * *
            */

            BarRenderer r = (BarRenderer) barChart.getCategoryPlot().getRenderer();
            r.setBarPainter(new StandardBarPainter());
            // r.setSeriesPaint(0, new Color(15, 159, 240));
            // r.setSeriesPaint(0, Color.BLACK);

            //barChart.getXYPlot().getDomainAxis().set.setTickUnit(new NumberTickUnit(dataset.getColumnCount()/20)) ;
            //saveChart(barChart) ;
            displayChart(barChart) ;
            
        }
        
        /**
         * Generates stacked bar chart from data in dataset.
         * FIXME: Currently only one bar per category is enabled. Multiple entries
         * are stacked.
         * @param chartTitle
         * @param dataset
         * @param scoreNames
         * @param xLabel 
         */
        private void plotStackedBarChart(String chartTitle, CategoryDataset dataset, String[] scoreNames , String xLabel)
        {
            // LOGGER.info("plotBarChart()");
            //if (grouped)
            String[] nameList ;
            
            String scoreName = "" ; // "infections per 100,000 person-years" ; // getYLabel(scoreNames) ;
            
            JFreeChart barChart ;
            if (stacked)
                barChart = ChartFactory.createStackedBarChart(chartTitle,xLabel,
                  scoreName,dataset,PlotOrientation.VERTICAL,true, true, false);    // Stacked
            else
                barChart = ChartFactory.createBarChart(chartTitle,xLabel,
                    scoreName,dataset,PlotOrientation.VERTICAL,true, true, false);    // Stacked
            
            CategoryPlot plot = (CategoryPlot) barChart.getPlot();
            
            if (stacked)
            {
                String group = "G1" ;
                GroupedStackedBarRenderer renderer = new GroupedStackedBarRenderer();
                KeyToGroupMap map = new KeyToGroupMap(group);
                for (String name : scoreNames)
                {
                    if (name.contains(GROUP))
                    {
                        nameList = name.split(GROUP) ;
                        if (nameList.length == 1)
                        {
                            name = name.substring(0, name.indexOf(GROUP)) ;
                            group = name ;
                        }
                        else
                        {
                            name = nameList[0] ;
                            group = nameList[1] ;
                        }
                        //LOGGER.log(Level.INFO, "{0} {1}", new Object[] {name,group});
                        map.mapKeyToGroup(name, group);
                    }
                    else
                    {
                        map.mapKeyToGroup(name, "G1") ;
                    } // map.mapKeyToGroup(name, group);
                }
                renderer.setSeriesToGroupMap(map);
                plot.setRenderer(renderer);
            }

            //if (stacked)
                
            //LOGGER.info(plot.getDomainAxis().getLabelFont().) ;
            CategoryAxis domainAxis = plot.getDomainAxis();
            ValueAxis rangeAxis = plot.getRangeAxis();

            Font font2 = new Font("Dialog", Font.PLAIN, 20); 
            Font font3 = new Font("Dialog", Font.PLAIN, 25); 
            domainAxis.setLabelFont(font2);
            rangeAxis.setLabelFont(font3);
            //domainAxis.setTickLabelFont(font2);
            rangeAxis.setTickLabelFont(font2);
            rangeAxis.setMinorTickCount(4);
            rangeAxis.setMinorTickMarksVisible(true);

            //domainAxis.getLabelFont().getAttributes().put(TextAttribute.SIZE, TextAttribute.WIDTH_EXTENDED) ;

            plot.setOutlineVisible(false) ;
            plot.setBackgroundPaint(Color.WHITE) ;
            
            ((BarRenderer) plot.getRenderer()).setBarPainter(new StandardBarPainter());
            //((StackedBarRenderer) plot.getRenderer()).setBarPainter(new StandardBarPainter());
            
            //LegendTitle legend = barChart.getLegend() ;
            //legend.setPosition(RectangleEdge.TOP) ;
            //plot.setFixedLegendItems(createLegendItems());
            if (String.valueOf(dataset.getColumnKeys().get(0)).length() > 1)
                domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
            
            saveChart(barChart) ;
            displayChart(barChart) ;
        }
        
        private void plotScatterPlot(String chartTitle, XYDataset dataset, String yLabel, String xLabel)
        {
            JFreeChart scatterPlot = ChartFactory.createScatterPlot(chartTitle, xLabel, yLabel, dataset, PlotOrientation.VERTICAL,true,true,false) ;
            //JFreeChart scatterPlot = ChartFactory.createScatterPlot(xLabel, xLabel, xLabel, dataset, PlotOrientation.HORIZONTAL, rootPaneCheckingEnabled, rootPaneCheckingEnabled, rootPaneCheckingEnabled)
            
            NumberAxis rangeAxis = (NumberAxis) scatterPlot.getXYPlot().getRangeAxis() ;
            rangeAxis.setTickUnit(new NumberTickUnit(1)) ;
            scatterPlot.setBackgroundPaint(Color.WHITE) ;
            //saveChart(scatterPlot) ;
            displayChart(scatterPlot) ;
            
        }
        
        /**
         * Generates plot of dataset
         * @param chartTitle
         * @param dataset
         * @param yLabel
         * @param xLabel 
         */
        private void plotLineChart(String chartTitle, XYDataset dataset, String yLabel, String xLabel, String[] legend) 
        {

            boolean showLegend = (legend[0].isEmpty() || insetLegend) ? false : true ;

            // define lineChart and set error renderer
            JFreeChart lineChart = ChartFactory.createXYLineChart(chartTitle,xLabel,
                yLabel,dataset,PlotOrientation.VERTICAL,showLegend, true, false);

            // lineChart.getLegend();

            // LOGGER.info("LEGEND:" + Arrays.toString(legend));

            // declare renderer used
            XYLineAndShapeRenderer r = null;
            
            switch (lineGraphErrorType) {
                case ERROR_INTERVALS:
                    XYErrorRenderer rErrorRenderer = new XYErrorRenderer();
                    
                    // determine whether to draw confidence intervals
                    if (getDrawCI())    
                    	rErrorRenderer.setDrawYError(true);
                    else                
                    	rErrorRenderer.setDrawYError(false);

                    rErrorRenderer.setDrawXError(false);

                    // confidence interval styles
                    rErrorRenderer.setErrorStroke(new BasicStroke(2.0f));
                    // ((XYErrorRenderer) r).setErrorPaint(Color.BLACK); // sets error paint
                    rErrorRenderer.setCapLength(15);
                    // lineChart.getXYPlot().setRenderer(((XYErrorRenderer) r));

                    r = rErrorRenderer;
                    break;

                case SHADED_REGION:
                    DeviationRenderer rDeviationRenderer = new DeviationRenderer(true, true);
                    rDeviationRenderer.setAlpha(0.4f);
                    // lineChart.getXYPlot().setRenderer(((DeviationRenderer) r));

                    r = rDeviationRenderer;
                    break;
            }
            // DeviationRenderer r = new DeviationRenderer(true, false);
            

            lineChart.getXYPlot().setRenderer(r);
            
            // axes information:
            // setting logarithmic 
            // lineChart.getXYPlot().setDomainAxis(new LogarithmicAxis(xLabel));
            NumberAxis domainAxis = (NumberAxis) lineChart.getXYPlot().getDomainAxis() ;
            ValueAxis rangeAxis = lineChart.getXYPlot().getRangeAxis() ;
            // NumberAxis rangeAxis = (NumberAxis) lineChart.getXYPlot().getRangeAxis();

            double upperBound = dataset.getItemCount(0) ;    // domainAxis.getRange().getUpperBound() ;
            



            // // chart settings based on number of elements plotted
            // if ((upperBound % ConfigLoader.DAYS_PER_YEAR) == 0)    // if upperBound a multiple of 365 (days)
            // {
            //     if (upperBound >= ConfigLoader.DAYS_PER_YEAR * 2)    // more than two years
            //     {
            //         domainAxis.setTickUnit(new NumberTickUnit(ConfigLoader.DAYS_PER_YEAR)) ;
            //         if (upperBound < ConfigLoader.DAYS_PER_YEAR * 10)    // less than ten years
            //         {
            //             domainAxis.setMinorTickCount(4);
            //             domainAxis.setMinorTickMarksVisible(true);
            //         }
            //     }
            // }
            // else {
            //     domainAxis.setMinorTickMarksVisible(true);
            // }
            
            //domainAxis.setRange(2.0,upperBound);
            
            // Set unit tick distance if range is integer.
            // if (int.class.isInstance(dataset.getX(0,0)) || Integer.class.isInstance(dataset.getX(0, 0)))
            // {
            //     LOGGER.info("integer domain") ;
            //     //NumberAxis rangeAxis = (NumberAxis) lineChart.getXYPlot().getRangeAxis() ;
            //     //rangeAxis.setTickUnit(new NumberTickUnit(1)) ;
            //     domainAxis.setTickUnit(new NumberTickUnit(1)) ;
            // }

         // set font:
            String UNIFORM_FONT = "Helvetica";
            
            Font titleFont = new Font(UNIFORM_FONT, Font.PLAIN, 48) ;
            Font labelFont = new Font(UNIFORM_FONT, Font.PLAIN, 30) ;
            Font legendFont = new Font(UNIFORM_FONT, Font.PLAIN, 30) ;
            Font tickDomainFont = new Font(UNIFORM_FONT, Font.PLAIN, 24) ;
            Font tickRangeFont = new Font(UNIFORM_FONT, Font.PLAIN, 36) ;
            
            domainAxis.setNumberFormatOverride(new DecimalFormat("####")) ;
            
            // randomly pick domain values to find out if the domain contains only integer values
            // draw ticks based on certain condititions
            // undefined for non-integer domains
            if (this.isDomainInteger(dataset)) {
            	if (upperBound > ConfigLoader.DAYS_PER_YEAR * 10) 
                {
                    domainAxis.setTickUnit(new NumberTickUnit(upperBound*3/10)) ; //(3*ConfigLoader.DAYS_PER_YEAR)) ;
                }
                else if (upperBound > ConfigLoader.DAYS_PER_YEAR * 2) 
                {
                    domainAxis.setTickUnit(new NumberTickUnit(ConfigLoader.DAYS_PER_YEAR)) ;
                }
                else if (upperBound > ConfigLoader.DAYS_PER_YEAR/2 && upperBound <= ConfigLoader.DAYS_PER_YEAR * 2) 
                {
                    domainAxis.setTickUnit(new NumberTickUnit(ConfigLoader.DAYS_PER_MONTH)) ;
                }
                else if (upperBound <= ConfigLoader.DAYS_PER_MONTH * 2) 
                {
                        domainAxis.setTickUnit(new NumberTickUnit(2));
                        tickDomainFont = new Font(UNIFORM_FONT, Font.PLAIN, 28) ;
                        //domainAxis.setVerticalTickLabels(true);
                        //domainAxis.setNumberFormatOverride(new DecimalFormat("####")) ;
                } 
                else 
                {
                    domainAxis.setTickUnit(new NumberTickUnit(ConfigLoader.DAYS_PER_WEEK));
                }
                domainAxis.setMinorTickMarksVisible(false);
            }

            // temporary
            // domainAxis.setTickUnit(new NumberTickUnit(500));

            // draw legend
            
            if (!legend[0].isEmpty()) {
                LegendTitle plotLegend = lineChart.getLegend() ;
                if (insetLegend)
                {
                    LegendTitle lt = new LegendTitle(lineChart.getXYPlot());
                    lt.setItemFont(legendFont);
                    lt.setBackgroundPaint(new Color(200, 200, 255, 100));
                    lt.setFrame(new BlockBorder(Color.white));
                    lt.setPosition(RectangleEdge.RIGHT);
                    XYTitleAnnotation ta = new XYTitleAnnotation(0.98, 0.02, lt, RectangleAnchor.BOTTOM_RIGHT);
                    lineChart.getXYPlot().addAnnotation(ta);

                    // code snippet from GrahamA's solution at:
                    // https://stackoverflow.com/questions/11320360/embed-the-legend-into-the-plot-area-of-jfreechart
                }
                else
                {
                    plotLegend.setPosition(RectangleEdge.RIGHT);
                }
            }

            /* !!!
            * * * * * * * * * * * * * * * * * * * * *
            *         XYPlot Render Settings        *
            * * * * * * * * * * * * * * * * * * * * *
            */

            // set background to white
            lineChart.getPlot().setBackgroundPaint(Color.WHITE);

            // draw points
            if (upperBound > ConfigLoader.MAX_YEARS) {
                setDrawPoints(false);
                setDrawError(false);
            }
            // else {
            //     setDrawPoints(true);
            //     setDrawError(true);
            // }
            
            

            // set shape of points
            double circleWidth = 8.0;
            double circleOffset = circleWidth / 2;
            Shape shape = new Ellipse2D.Double(-circleOffset, -circleOffset, circleWidth, circleWidth);

            // get preloaded colours
            ArrayList<ArrayList<Integer>> colours = ConfigLoader.getColours();

            for (int numSeries = 0; numSeries < legend.length; ++numSeries) {
                // XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) lineChart.getXYPlot().getRenderer();
                
                r.setSeriesShape(numSeries, shape);
                r.setSeriesLinesVisible(numSeries, true);
                if (drawPoints) 
                	r.setSeriesShapesVisible(numSeries, true);
                else 
                	r.setSeriesShapesVisible(numSeries, false);
                
                // set line colours - remove from start and add to the end just in case we run out of colours
                ArrayList<Integer> rgb = colours.remove(0);
                colours.add(rgb);
                Color paintColour = new Color(rgb.get(0).intValue(),rgb.get(1).intValue(),rgb.get(2).intValue());
                r.setSeriesPaint(numSeries, paintColour);
                r.setSeriesFillPaint(numSeries, paintColour);
                
                // set line thickness
                r.setSeriesStroke(numSeries, new BasicStroke(2.0f));
            }
            
            
            // title:
            lineChart.getTitle().setFont(titleFont);
            
            // x and y labels:
            domainAxis.setLabelFont(labelFont);
            rangeAxis.setLabelFont(labelFont);
            
            domainAxis.setTickLabelFont(tickDomainFont);
            rangeAxis.setTickLabelFont(tickRangeFont);
            
            // legend
            if (showLegend) lineChart.getLegend().setItemFont(legendFont);
            
            
            displayChart(lineChart) ;
            saveChart(lineChart);
        }


        /**
         * Randomly selects 3 points in the domain and verify that they are all integers
         * @param dataset
         * @return a boolean on whether the domain only contains integer values or not
         */
        private boolean isDomainInteger(XYDataset dataset) {
            int RANDOM_SAMPLE_SIZE = 3;
            
            boolean toReturn = true;
            int numSeries = dataset.getSeriesCount();
            
            for (int i = 0; i < RANDOM_SAMPLE_SIZE; ++i) {

                // randomly select a series
                int series = this.randIntInRangeInclusive(0, numSeries - 1);
                int itemCount = dataset.getItemCount(series);

                if (itemCount == 0) continue;
                // randomly select an item
                int item = this.randIntInRangeInclusive(0, itemCount - 1);

                // extract its value
                double xValue = (double) dataset.getX(series, item);

                // if this is non-integer, break early and return false
                if (xValue % 1 != 0) {
                    toReturn = false;
                    break;
                }
            }
            return toReturn;
        }
        
        /**
         * @param from: an integer as an inclusive lower bound
         * @param to: an integer as an inclusive upper bound
         * @return a random number between the ranges(from, to) inclusive of the bounds
         */
        private int randIntInRangeInclusive(int from, int to) {
            Random ran = new Random();
            return ran.nextInt((to - from) + 1) + from;
        }

        /**
         * Generates Area plot of dataset.
         * @param chartTitle
         * @param dataset
         * @param yLabel
         * @param xLabel
         * @param legend 
         */
        private void plotAreaChart(String chartTitle, XYDataset dataset, String yLabel, String xLabel, ArrayList<Object> categoryList, String[] legend)
        {
            boolean showLegend = !(legend[0].isEmpty()) ;
            JFreeChart areaChart = ChartFactory.createXYAreaChart(chartTitle,xLabel,
                yLabel,dataset,PlotOrientation.VERTICAL,showLegend, true, false);
            
            areaChart.getXYPlot().getDomainAxis().setTickLabelsVisible(false);
            areaChart.getXYPlot().getDomainAxis().setTickMarksVisible(false);
            //areaChart.getXYPlot().setRangeAxis(new LogarithmicAxis(yLabel));
        
            
            int xPos = 1 ;
            for (Object label : categoryList)
            {
                XYTextAnnotation xyTextAnnotation = new 
                XYTextAnnotation(String.valueOf(label),dataset.getXValue(0, xPos),dataset.getYValue(0, xPos)/4) ;
                xyTextAnnotation.setFont(new Font("SansSerif", Font.BOLD, 11));
                xyTextAnnotation.setBackgroundPaint(Color.RED);
                areaChart.getXYPlot().addAnnotation(xyTextAnnotation);
            
            /*final Marker categoryMarker = new ValueMarker(dataset.getXValue(0, xPos));
                //currentEnd.setPaint(Color.red);
                categoryMarker.setLabelBackgroundColor(Color.red);
                categoryMarker.setPaint(Color.LIGHT_GRAY);
                categoryMarker.setLabel("" + String.valueOf(label)) ;
                categoryMarker.setLabelAnchor(RectangleAnchor.BOTTOM_RIGHT) ;
                categoryMarker.setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);
                areaChart.getXYPlot().addDomainMarker(categoryMarker);*/
                xPos += 5 ;
            }
            areaChart.getPlot().setBackgroundPaint(Color.WHITE) ;
            if (SAVE_PLOT)
                saveChart(areaChart) ;
            displayChart(areaChart) ;
        }   
        
        /**
         * Opens an interactive window with the chart if HPC hasn't been detected
         * @param chart 
         */
        private void displayChart(JFreeChart chart)
        {
            // fix any concurrency problems within JFreeChart, notably legend elements doubling up
            try { Thread.sleep(1000); }
            catch (InterruptedException e) { e.printStackTrace(); }

            String APPLICATION_TITLE = applicationTitle;
            //String APPLICATION_TITLE = chartTitle;

            if (!detectHPC()) {
                ChartPanel chartPanel = new ChartPanel( chart );
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                int screenWidth = (int) screenSize.getWidth();
                int screenHeight = (int) screenSize.getHeight();
                int windowSize = (int) (Math.min(screenWidth, screenHeight) * 0.75);

                //chartPanel.setPreferredSize(new java.awt.Dimension( 2240 , 734 ) );        
                chartPanel.setPreferredSize(new java.awt.Dimension((int) (windowSize * 1.5), windowSize));        
                //chartPanel.setPreferredSize(new java.awt.Dimension( 560 , 367 ) );
                
                ApplicationFrame appFrame = new ApplicationFrame(APPLICATION_TITLE);

                // detect opearting system:
                appFrame.setContentPane(chartPanel) ; 
                appFrame.pack() ;
                appFrame.setVisible(true) ;
            }
            else {
                LOGGER.warning("HPC detected, display not possible.") ;
            }
        }

        /**
         * method detects whether the current environment's state is headless
         * assume if an environment is headless, project is being executed on HPC
         */
        private boolean detectHPC() {
            if (GraphicsEnvironment.isHeadless()) return true;
            else return false;
        }
        
        /**
         * Saves the plot to a .jpg file
         * @param barChart 
         */
        private void saveChart(JFreeChart chart)
        {
        	if (!SAVE_PLOT)
        		return ;
            try { Thread.sleep(1000); }
            catch (InterruptedException e) { e.printStackTrace(); }

            String directory = folderPath ;
            String address = directory + applicationTitle + chartTitle + ".jpg" ;

            int width = FIGURE_WIDTH ;
            int height = FIGURE_HEIGHT ;
            //int width = 2560 ;
            //int width = 640 ;
            //int height = 960 ;
            File file = new File(address) ;
            //File file = new File(directory) ;
            //String[] files = file.list() ;
            try
            {
            //LOGGER.info(file.getCanonicalPath());
            //for (String fileName : files)
              //  LOGGER.info(fileName);
                ChartUtils.saveChartAsJPEG(file, chart, width, height);
            }
            catch ( IOException ioe)
            {
                LOGGER.severe("Saving failed");
                LOGGER.severe(ioe.getMessage());
                LOGGER.severe(ioe.getLocalizedMessage());
            }
        }
        
        /**
         * Writes a prospective dataset to .RData file instead of generating a 
         * graph with JFreeChart.
         * @param scoreData
         * @param yLabel
         * @param xLabel
         * @param legend 
         */
        private void exportDataset(ArrayList<ArrayList<Object>> scoreData, String yLabel, String xLabel, String[] legend)
        {
            String firstLine = xLabel + "," + yLabel + ",PlotNb" ;
            
            String filePath = folderPath + applicationTitle + chartTitle + ".csv" ;
            
            int dataSize;
            String plotLabel ;
            
            try
            {
                BufferedWriter fileWriter = new BufferedWriter(new FileWriter(filePath,false));
                fileWriter.write(firstLine) ;
                fileWriter.newLine() ;
                
                int plotTotal = legend.length ;
                for (int plotNumber = 0 ; plotNumber < plotTotal ; plotNumber++ )
                {
                    ArrayList<Object> data = scoreData.get(plotNumber) ;
                    dataSize = data.size();
                    plotLabel = String.valueOf(plotNumber);

                    for (int index = 0 ; index < dataSize; index++ )
                    {
                        String fileLine = String.valueOf(index) + "," ;
                        fileLine += String.valueOf(data.get(index)) + "," ;
                        fileLine += plotLabel ;
                        fileWriter.write(fileLine) ; 
                        fileWriter.newLine() ;
                    }
                }
                fileWriter.close() ;
            }
            catch ( Exception e )
            {
                LOGGER.log(Level.SEVERE, e.toString());
            }
        }
        
        /**
         * Generate Dataset of scoreData as function of categoryData.
         * Usually used for within-cycle plots
         * @param category
         * @param categoryData
         * @param scoreData
         * @return 
         */
        private CategoryDataset createDataset(String category, ArrayList<Object> categoryData, ArrayList<Object> scoreData)
        {
            DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset() ;
            // ArrayList<String> categoryData = data.get(0) ;
            // ArrayList<String> scoreData = data.get(1) ;
            
            String categoryValue ;
            int scoreValue ;
            
            for (int index = 0 ; index < scoreData.size() ; index++ )
            {
                categoryValue = (String) categoryData.get(index) ;
                scoreValue = Integer.valueOf((String) scoreData.get(index)) ;
                categoryDataset.addValue( scoreValue, category, categoryValue ) ;
            }
            return categoryDataset ;
        }
        
        /*private CategoryDataset createDatasetInteger(String category, ArrayList<Object> categoryData, ArrayList<Number> scoreData)
        {
            DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset() ;
            // ArrayList<String> categoryData = data.get(0) ;
            // ArrayList<String> scoreData = data.get(1) ;
            
            String categoryValue ;
            Number scoreValue ;
            
            for (int index = 0 ; index < scoreData.size() ; index++ )
            {
                categoryValue = String.valueOf(categoryData.get(index)) ;
                scoreValue = scoreData.get(index) ;
                categoryDataset.addValue( scoreValue, category, categoryValue ) ;
            }
            return categoryDataset ;
        }*/
        
        /**
         * Constructs dataset suitable for feeding a stackedPlotChart.
         * @param scoreNames
         * @param categoryData
         * @param scoreData
         * @return 
         */
        private DefaultCategoryDataset createDataset(String[] scoreNames, ArrayList<Object> categoryData, ArrayList<ArrayList<Number>> scoreData, boolean cluster)
        {
            if (cluster)
                return createDataset(scoreNames, categoryData, scoreData) ;

            DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset() ;
            // ArrayList<String> categoryData = data.get(0) ;
            // ArrayList<String> scoreData = data.get(1) ;

            String categoryValue ;
            ArrayList<Number> scoreValueArray ;
            //Number scoreValue ;
            
            for (int index = 0 ; index < scoreData.size()  ; index++ ) //-2
            {
                categoryValue = String.valueOf(categoryData.get(index)) ;
                scoreValueArray = scoreData.get(index) ;
                if (scoreNames.length == 0)
                {
                    Number scoreValue = scoreValueArray.get(0) ;
                    String scoreName = categoryValue ;
                    categoryDataset.addValue( scoreValue, scoreName, categoryValue ) ;
                }
                else
                    for (int scoreIndex = 0 ; scoreIndex < scoreNames.length ; scoreIndex++ ) // scoreValueArray.size() 
                    {
                        Number scoreValue = scoreValueArray.get(scoreIndex) ;
                        String scoreName = scoreNames[scoreIndex] ;
                        if (scoreName.contains(GROUP))
                            scoreName = scoreName.substring(0, scoreName.indexOf(GROUP)) ;
                        categoryDataset.addValue( scoreValue, scoreName, categoryValue ) ;
                    }
            }
            return categoryDataset ;
        }
        
        /**
         * Constructs dataset suitable for feeding a stackedPlotChart after binning.
         * First record gets its own bin, then bin increase according powers of base=2 .
         * @param scoreNames
         * @param categoryData
         * @param scoreData
         * @return 
         */
        private DefaultCategoryDataset createDataset(String[] scoreNames, ArrayList<Object> categoryData, ArrayList<ArrayList<Number>> scoreData)
        {
            DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset() ;
            // ArrayList<String> categoryData = data.get(0) ;
            // ArrayList<String> scoreData = data.get(1) ;
            
            String scoreName ;
            boolean cumulative = scoreNames[0].contains("umulative") ;
          // logger.log(level.info, "cumulative:{0} {1}", new Object[] {String.valueOf(cumulative),scoreNames}) ;
            String categoryValue = "" ;
            ArrayList<Number> scoreValueArray ;
            ArrayList<Number> scoreValue ;
            int base = 2 ;
            
            int dataSize = scoreData.size() ;
            //for (int index = 0 ; index < dataSize ; index++ )
            {
                //LOGGER.info("index:"+String.valueOf(index)) ;
                int binIndex = 0 ;
                int nextIndex = 1 ;
                int openSegmentNb = 0 ;
                int closeSegmentNb = 1 ;    // (int) Math.pow(base, nextIndex) - 1 ;    // First category stands alone
                //scoreValueArray = scoreData.get(index) ;
                
                while (closeSegmentNb > openSegmentNb)
                {
                    categoryValue = String.valueOf(categoryData.get(openSegmentNb)) 
                            + categoryValue ;
                    
                    // Initiate ArrayList 
                    if (cumulative)    // Take first record
                        scoreValue = scoreData.get(openSegmentNb) ;
                    else    // Sum over records in bin
                    {
                        scoreValue = new ArrayList<Number>() ;
                        for (String scoreName1 : scoreNames) {
                            scoreValue.add(0.0) ;
                        }

                        // loop through bin
                        for (int segmentIndex = openSegmentNb ; segmentIndex < closeSegmentNb ; segmentIndex++ )
                        {
                            scoreValueArray = scoreData.get(segmentIndex) ;
                            // Add scores
                            for (int scoreIndex = 0 ; scoreIndex < scoreValueArray.size() ; scoreIndex++ )
                                scoreValue.set(scoreIndex, scoreValue.get(scoreIndex).doubleValue() 
                                        + scoreValueArray.get(scoreIndex).doubleValue()) ;
                        }
                    //LOGGER.log(Level.INFO,"{0}",scoreValue) ;
                    }
                    
                    // Add bin to dataset
                    for (int scoreIndex = 0 ; scoreIndex < scoreNames.length ; scoreIndex++ )
                    {
                        /*if (cumulative)
                            scoreName = "Log() " ;
                        else
                            scoreName = "" ;*/
                        scoreName = scoreNames[scoreIndex] ;
                        Number score = scoreValue.get(scoreIndex) ;
                        categoryDataset.addValue( score, scoreName, categoryValue ) ;
                    }
                    
                    // prepare for next bin
                    binIndex++ ;
                    nextIndex++ ;
                    openSegmentNb = closeSegmentNb ;    // (int) Math.pow(base, binIndex) - 1 ;    // -1 java counts from 0
                    closeSegmentNb = (closeSegmentNb + 1) * base - 1  ;    // (int) Math.pow(base, nextIndex) - 1 ;    // -1 include closeSegmentNB in for-loop
                    if (closeSegmentNb > dataSize) 
                        closeSegmentNb = dataSize ;
                    if (cumulative)
                        categoryValue = "" ;
                    else
                        categoryValue = "-" + String.valueOf(categoryData.get(closeSegmentNb-1)) ;
                        
                }
            }
            return categoryDataset ;
        }

        /**
         * Generate Dataset for a given double[] of domain values from a function.
         * @param functions
         * @param domain
         * @return 
         */
        private XYSeriesCollection createXYDataset(PolynomialSplineFunction[] functions, double[] domain, String[] legend)
        {
            XYSeriesCollection xySeriesCollection = new XYSeriesCollection() ;
            
            int plotTotal ;
            
            if (legend.length == 0)
                legend = new String[] {""} ;
            plotTotal = legend.length ;
            
            for (int plotNumber = 0 ; plotNumber < plotTotal ; plotNumber++ )
            {
                XYSeries xySeries = new XYSeries(legend[plotNumber]) ;

                for (double x : domain)
                    xySeries.add(x, functions[plotNumber].value(x), false);
            
                try
                {
                    xySeriesCollection.addSeries((XYSeries) xySeries.clone());
                }
                catch ( CloneNotSupportedException cnse )
                {
                    LOGGER.log(Level.SEVERE, cnse.toString());
                }
            }
            return xySeriesCollection ;
        }
        
        /**
         * Generate Dataset from scoreData.
         * Intended for plots over time/cycles
         * @param scoreData
         * @return CategoryDataset of score over cycle
         */
        private XYSeriesCollection createXYDataset(ArrayList<ArrayList<String>> scoreData, String[] legend)
        {
            XYSeriesCollection xySeriesCollection = new XYSeriesCollection() ;
            // ArrayList<String> categoryData = data.get(0) ;
            // ArrayList<String> scoreData = data.get(1) ;
            Number scoreValue ;
            int dataSize ;
            ArrayList<String> data ;
            int plotTotal ;
            if (legend.length == 0)
                legend = new String[] {""} ;
            plotTotal = legend.length ;
            
            for (int plotNumber = 0 ; plotNumber < plotTotal ; plotNumber++ )
            {
                XYSeries xySeries = new XYSeries(legend[plotNumber]) ;

                data = scoreData.get(plotNumber) ;
                //LOGGER.info(scoreData.toString());
                dataSize = data.size();
                
                for (int index = 0 ; index < dataSize; index++ )
                {
                    String scoreString = (String) data.get(index) ;
                    if (int.class.isInstance(scoreString)) 
                        scoreValue = Integer.valueOf(scoreString) ;
                    else
                        scoreValue = Double.valueOf(scoreString) ;
                    xySeries.add(1 + index, scoreValue, false);
                }
                try
                {
                    xySeriesCollection.addSeries((XYSeries) xySeries.clone()) ;
                }
                catch ( CloneNotSupportedException cnse )
                {
                    LOGGER.log(Level.SEVERE, cnse.toString());
                }
            }
            return xySeriesCollection ;
        }
        
        /**
         * 
         * @param scoreData
         * @param legend
         * @return (XYSeriesCollection) dataset for plotting XYAreaGraphs
         */
        private XYSeriesCollection createAreaPlotDataset(ArrayList<Number[]> scoreData, String[] legend)
        {
            // ArrayList<String> categoryData = data.get(0) ;
            // ArrayList<String> scoreData = data.get(1) ;
            Number[] scoreValueArray ;
            Number xValue ;
            Number yValue ;
            
            int dataSize ;
            ArrayList<Number[]> data ;

            XYSeries xySeries = new XYSeries(0) ;

            //data = scoreData.get(0) ;
            dataSize = scoreData.size();

            for (int index = 0 ; index < dataSize; index++ )
            {
                scoreValueArray = scoreData.get(index) ;
                xValue = scoreValueArray[0] ;
                yValue = scoreValueArray[1] ;
                xySeries.add(xValue, yValue, false);
            }
            
            //TODO: Expand to include multiplot plots on same graph
            /*try
            {
                xySeriesCollection.addSeries((XYSeries) xySeries.clone()) ;
            }
            catch ( CloneNotSupportedException cnse )
            {
                LOGGER.log(Level.SEVERE, cnse.toString());
            }*/

            
            return new XYSeriesCollection(xySeries) ;//xySeriesCollection ;
        }
        
        /**
         * Generates Dataset suitable for scatter plots on XYPlot from an ArrayList<HashMap>.
         * Suitable for plots over multiple cycles.
         * @param hashMapArrayList
         * @param plotTitle
         * @return 
         */
        private XYSeriesCollection createScatterPlotDataset(ArrayList<HashMap<Comparable<?>,ArrayList<Comparable<?>>>> hashMapArrayList, 
                String plotTitle)
        {
            XYSeriesCollection scatterPlotDataset = new XYSeriesCollection() ;
            String seriesTitle ;
            HashMap<Comparable<?>,ArrayList<Comparable<?>>> hashMap ;
                
            for (int index = 0 ; index < hashMapArrayList.size() ; index++ )
            {
                seriesTitle = plotTitle + "_" + String.valueOf(index);
                hashMap = hashMapArrayList.get(index);
                scatterPlotDataset.addSeries(createScatterPlotSeries(hashMap,seriesTitle));
            }
            return scatterPlotDataset ;
        }
        
        /**
         * Generates Dataset suitable for scatter plots on XYPlot from single HashMap.
         * Suitable for plots with a single cycle.
         * @param agentToAgentHashMap
         * @param plotTitle
         * @return (XYSeriesCollection) 
         */
        private XYSeriesCollection createScatterPlotDataset(HashMap<Comparable<?>,ArrayList<Comparable<?>>> agentToAgentHashMap, String plotTitle)
        {
            return new XYSeriesCollection(createScatterPlotSeries(agentToAgentHashMap,plotTitle)) ;
        }
        
        /**
         * 
         * @param agentToAgentHashMap
         * @param seriesTitle
         * @return (XYSeries) with entires suitable for XYPlot.
         */
        private XYSeries createScatterPlotSeries(HashMap<Comparable<?>,ArrayList<Comparable<?>>> agentToAgentHashMap, String seriesTitle)
        {
            XYSeries scatterPlotDataset = new XYSeries(seriesTitle) ;
            for (Object positiveAgent : agentToAgentHashMap.keySet())
                for (Object negativeAgent : agentToAgentHashMap.get(positiveAgent))
                    scatterPlotDataset.add((Number) positiveAgent, (Number) negativeAgent) ;
            return scatterPlotDataset ;
        }
        
        /**
         * 
         * @param cycleToAgentArray
         * @param hubTitle
         * @return 
         */
        private XYSeriesCollection createHubDataset(ArrayList<HashMap<Comparable<?>,ArrayList<Comparable<?>>>> cycleToAgentArray)
        {
            XYSeriesCollection hubSeriesCollection = new XYSeriesCollection() ;
            HashMap<Object,Number> lastInfected = new HashMap<Object,Number>() ;
            
            for (int cycle = 0 ; cycle < cycleToAgentArray.size() ; cycle++ )
            {
                HashMap<Comparable<?>,ArrayList<Comparable<?>>> agentToAgentHashMap = cycleToAgentArray.get(cycle) ;
                for (Object transmitterId : agentToAgentHashMap.keySet())
                {
                    String seriesTitle = "cycle" + String.valueOf(cycle) 
                            + "agentId" + String.valueOf(transmitterId) ;
                    XYSeries hubSeries = new XYSeries(seriesTitle,false,true) ;
            
                    if (!lastInfected.containsKey(transmitterId))
                        lastInfected.put(transmitterId, 0) ;
                    ArrayList<Comparable<?>> toAgentArray = agentToAgentHashMap.get(transmitterId) ;
                    //LOGGER.log(Level.INFO, "{0}", toAgentArray ) ;
                    hubSeries = generateHub((Number) transmitterId, lastInfected.get(transmitterId), toAgentArray, cycle, hubSeries) ;
                    for (Object receiverId : toAgentArray)
                        lastInfected.put(receiverId, cycle) ;
                    try
                    {
                        hubSeriesCollection.addSeries((XYSeries) hubSeries.clone());
                    }
                    catch ( CloneNotSupportedException cnse )
                    {
                        LOGGER.severe(cnse.toString());
                        hubSeriesCollection.addSeries(hubSeries);
                    }
                }
            }
            return hubSeriesCollection ;
        }
        
        /**
         * 
         * @param hubId
         * @param hubCycle
         * @param hubHashMap
         * @param nodeCycle
         * @return (XYSeries) with additional nodes showing transmissions in cycle nodeCycle from Agent hubId infected in cycle hubCycle.
         */
        private XYSeries generateHub(Number hubId, Number hubCycle, ArrayList<Comparable<?>> hubArray, Number nodeCycle, XYSeries hubSeries)
        {
            for (Object nodeId : hubArray)
                hubSeries = generateHubNode(hubId, hubCycle, (Number) nodeId, nodeCycle, hubSeries) ;
            //for (double[] entry : hubSeries.toArray()) 
              //  System.out.println(entry[0] + entry[1]);
            return hubSeries ;
        }

       /**
         * 
         * @param hubId
         * @param hubCycle
         * @param hubHashMap
         * @param nodeCycle
         * @param hubSeries (XYSeries) to be added to
         * @return (XYSeries) with additional nodes showing transmissions in cycle nodeCycle from Agent hubId infected in cycle hubCycle.
         */
        private XYSeries generateHubNode(Number hubId, Number hubCycle, Number nodeId, Number nodeCycle, XYSeries hubSeries)
        {
            //XYSeries hubSeries = new XYSeries("Infections by agentId " + String.valueOf(hubId)) ;
            //hubSeries.add(hubId, hubCycle) ;
            hubSeries = hubEntry(hubId, hubCycle, nodeId, nodeCycle, hubSeries) ;
            
            return hubSeries ;
        }
        
        /**
         * Adds to xySeries the lines needed to add a node to a hub and be 
         * ready to add next node
         * @param hub
         * @param node
         * @param xySeries 
         */
        private XYSeries hubEntry(Number hubId, Number hubCycle, Number nodeId, Number nodeCycle, XYSeries xySeries)
        {
            xySeries.add(hubId, hubCycle);
            xySeries.add(nodeId, nodeCycle);
            xySeries.add(hubId, hubCycle);
            return xySeries ;
        }
    }
    
}
