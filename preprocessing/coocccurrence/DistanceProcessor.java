import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;

/**
 * This class allows processing the (Eculidean) distances between the
 * Twitter users from the test set and those from the training
 * set. 
 * <br/>
 * This Java implementation is much quicker than the R one.
 *  
 * @author Vincent Labatut
 * 18/04/2015
 */
public class DistanceProcessor implements Runnable
{	
	/**
	 * Process the distances between two groups of matrices.
	 * 
	 * @param args
	 * 		Not used here, you have to directly modify the source code.
	 * 
	 * @throws IOException
	 * 		Problem while reading the matrices, or recording the distances.
	 */
	public static void main(String[] args) throws IOException
	{	
//		String mainFolder = "data/";
		String mainFolder = "/home/labatutv/work/data/";
//		File folder1 = new File(mainFolder+"part1");
		File folder1 = new File(mainFolder+"test_features");
//		File folder2 = new File(mainFolder+"part2");
		File folder2 = new File(mainFolder+"training_features");
		
		symmetric = true;
		int threadNbr = 10;
		processCorpus(folder1, folder2, threadNbr);
	}
	
	/**
	 * Constructor for the runnable objects.
	 * 
	 * @param threadNumber
	 * 		Local number of the thread.
	 */
	public DistanceProcessor(int threadNumber)
	{	this.threadNumber = threadNumber;
	}
	
	////////////////////////////////////////////////////////////
	////	FILE ACCESS
	////////////////////////////////////////////////////////////
	/** Name of the file containing the matrices (=input) */
	private final static String MATRIX_FILE = "cooccurrences.txt";
	/** Name of the file containing the distances (=output) */
	private final static String DISTANCE_FILE = "tt-distances.txt";

	/**
	 * Record the specified values in a text file.
	 * On each line, we put first the name of the considered
	 * user (folder) and the distance (to the
	 * user of interest).
	 * 
	 * @param values
	 * 		Array of pre-processed distances.
	 * @param folder
	 * 		Folder of the user of interest.
	 * @param folders2
	 * 		Folders of the other users.
	 * 
	 * @throws IOException
	 * 		Problem while recording the distance file.
	 */
	private void writeArray(float[] values, File folder, File[] folders2) throws IOException
	{	// open the file using nio
		String fileName = folder.getPath() + File.separator + DISTANCE_FILE;
		FileWriter fw = new FileWriter(fileName);
	    BufferedWriter bw = new BufferedWriter(fw);
	    
	    // write the data (folder name + distance)
	    for(int i=0;i<values.length;i++)
	    {	float value = values[i];
	    	String f2 = folders2[i].getName();
	    	bw.write(f2 + "\t" + value);
	    	bw.newLine();
	    }
	    
	    bw.close();
	}
	
	/**
	 * Loads the square integer matrix contained 
	 * in the specified file.
	 * 
	 * @param folder
	 * 		Folder containing the matrix.
	 * @return
	 * 		The loaded square integer matrix.
	 * 
	 * @throws IOException
	 * 		Problem while reading the matrix.
	 */
	private Object[] loadMatrix(File folder) throws IOException
	{	// open the file
		String fileName = folder.getPath() + File.separator + MATRIX_FILE;
		FileReader fr = new FileReader(fileName);
		BufferedReader buffer = new BufferedReader(fr);

        // read the whole file (not supposed to be very large)
		String line;
        LinkedList<String> lines = new LinkedList<String>();
        line = buffer.readLine(); //header
        while((line=buffer.readLine()) != null)
        	lines.add(line);
        buffer.close();
        
        // build the matrix
        int k = lines.size();
        int[][] matrix = new int[k][k];
        String[] terms = new String[k];
        int i = 0;
        for(String str: lines)
        {	String[] vals = str.trim().split("\\s+");
        	terms[i] = vals[0];
    		for(int j=0;j<k;j++)
    			matrix[i][j] = Integer.parseInt(vals[j+1]);
        	i++;
        }
        
        Object[] result = {matrix, terms};
        return result;
	}

	////////////////////////////////////////////////////////////
	////	MAIN LOOP
	////////////////////////////////////////////////////////////
	/** First list of user folders */
	private static File[] folders1;
	/** Second list of user folders */
	private static File[] folders2;
	/** Whether the matrices should be considered as symmetric, or not */
	private static boolean symmetric;

	/**
	 * Process the whole corpus. All the matrices from folder1
	 * will be compared to those of folder2. 
	 * <br/>
	 * The process is
	 * handled so that the produced files are placed in the
	 * user folders from the longest of the two lists.
	 * 
	 * @param folder1
	 * 		Folder containing the first part of the corpus.
	 * @param folder2
	 * 		Folder containing the second part of the corpus.
	 * @param threadNbr
	 * 		Number of threads to use.
	 * 
	 * @throws IOException
	 * 		Problem while reading the matrices, or recording the distances.
	 */
	private static void processCorpus(File folder1, File folder2, int threadNbr) throws IOException
	{	logs("Start processing");
		
		// retrieve the list of folders in each folder
		FileFilter filter = new FileFilter()
		{	@Override
			public boolean accept(File file)
			{	boolean result = file.isDirectory();
				return result;
			}
		};
		folders1 = folder1.listFiles(filter);
		Arrays.sort(folders1);
		logs("Numbers of users in the 1st folder: "+folders1.length);
		folders2 = folder2.listFiles(filter);
		Arrays.sort(folders2);
		logs("Numbers of users in the 2nd folder: "+folders2.length);

		// possibly intervert the lists depending on their sizes
		if(folders1.length<folders2.length)
		{	logs("Interverting the folder lists");
			File[] temp = folders1;
			folders1 = folders2;
			folders2 = temp;
		}
		
		// compare the corresponding matrices
		logs("Starting the comparison");
		counter = 0;
		for(int i=0;i<threadNbr;i++)
		{	DistanceProcessor dp = new DistanceProcessor(i);
			Thread t = new Thread(dp);
			logs("Starting thread #"+i+" "+(i+1)+"/"+threadNbr);
			t.start();
		}
	}
	
	/**
	 * Processes one user folder, by comparing the corresponding matrix
	 * to all the others from the second folder list.
	 * 		
	 * @throws IOException
	 * 		Problem while reading the matrices, or recording the distances.
	 */
	private void processFolder() throws IOException
	{	// init current index
		int idx1 = accessCounter();
		
		while(idx1>=0)
		{	File f1 = folders1[idx1];
			log("Treating user "+f1.getName()+" ("+(idx1+1)+"/"+folders1.length+")"); 
		
			// check if it was already processed before
			String fileName = f1.getPath() + File.separator + DISTANCE_FILE;
			File file = new File(fileName);
			if(file.exists())
				log("  This user was already processed before");
		
			// if the user must be processed
			else
			{	// init the distance array
				float distances[] = new float[folders2.length];
				int idx2 = 0;
				
				// get the first matrix
				Object[] tmp1 = loadMatrix(f1);
				int[][] m1 = (int[][])tmp1[0];
				String[] terms1 = (String[])tmp1[1];
				
				// compare to all the other matrices
				for(File f2: folders2)
				{	
//					log("  Processing comparison "+f1.getName()+" vs. "+f2.getName()+" ("+(idx1+1)+"/"+folders1.length+" vs. "+(idx2+1)+"/"+folders2.length+")");
					
					// get the second matrix
					Object[] tmp2 = loadMatrix(f2);
					int[][] m2 = (int[][])tmp2[0];
					String[] terms2 = (String[])tmp2[1];
					
					// get the union of their terms
					Map<String,Integer> common = listTerms(terms1,terms2);
					
					// linearizes the matrices
					int[] v1 = linearizeMatrix(m1, terms1, common);
					int[] v2 = linearizeMatrix(m2, terms2, common);
					
					// process the distance and update the distance array
					distances[idx2] = processEuclideanDistance(v1, v2);
//					log("    Resulting distance: "+distances[idx2]);
					idx2++;
				}
				
				// record the distance array
				writeArray(distances, f1, folders2);
			}
			
			// update index
			idx1 = accessCounter();
		}
		log("Thread over");
	}
	
	////////////////////////////////////////////////////////////
	////	LOG-RELATED STUFF
	////////////////////////////////////////////////////////////
	/** Date format used for logging */
	private final static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	/**
	 * Logs a message, adding date and time.
	 * 
	 * @param text
	 * 		Text to log.
	 */
	private static void logs(String text)
	{	Calendar cal = Calendar.getInstance();
		System.out.println("["+DATE_FORMAT.format(cal.getTime())+"] " + text);
	}
	
	/**
	 * Logs a message, adding date and time.
	 * 
	 * @param text
	 * 		Text to log.
	 */
	private void log(String text)
	{	Calendar cal = Calendar.getInstance();
		System.out.println("["+DATE_FORMAT.format(cal.getTime())+"] ("+threadNumber+") " + text);
	}
	
	////////////////////////////////////////////////////////////
	////	MULTITHREAD PROCESSING
	////////////////////////////////////////////////////////////
	/** Count the current user folder for multithreaded processing */
	private static int counter = 0;
	/** Number locally assigned to the thread */
	private int threadNumber;

	/**
	 * Accesses the counter in a synchronized way.
	 * The counter value is returned, and the counter
	 * is incremented.
	 * 
	 * @return
	 * 		The value of the counter (before incrementing it).
	 */
	private synchronized int accessCounter()
	{	int result = counter;
		if(counter==folders1.length)
			result = -1;
		else
			counter++;
		return result;
	}
	
	@Override
	public void run()
	{	try
		{	processFolder();
		}
		catch (IOException e)
		{	e.printStackTrace();
		}
	}
	
	////////////////////////////////////////////////////////////
	////	TERMS RELATED PROCESS
	////////////////////////////////////////////////////////////
	/**
	 * Takes two arrays of unique terms, and put their union in 
	 * a map so that the associated integer value corresponds to
	 * the position of the term in the ordered list of terms.
	 *  
	 * @param terms1
	 * 		First list of terms.
	 * @param terms2
	 * 		Second list of terms.
	 * @return
	 * 		Resulting map.
	 */
	private Map<String,Integer> listTerms(String[] terms1, String[] terms2)
	{	// build the order list of terms
		TreeSet<String> terms = new TreeSet<String>();
		terms.addAll(Arrays.asList(terms1));
		terms.addAll(Arrays.asList(terms2));
		
		// build the result map
		Map<String,Integer> result = new HashMap<String, Integer>(terms.size());
		int i = 0;
		for(String term: terms)
		{	result.put(term,i);
			i++;
		}

		return result;
	}
	
	/**
	 * Transform the specified matrix into a vector,
	 * adding zeros to fill the missing words listed
	 * in the map.
	 * 
	 * @param m
	 * 		Considered matrix.
	 * @param terms
	 * 		Terms associated to the matrix rows/columns.
	 * @param common
	 * 		Complete list of terms to consider.
	 * @return
	 * 		A vector corresponding to the linearized and completed matrix.
	 */
	private int[] linearizeMatrix(int[][] m, String[] terms, Map<String,Integer> common)
	{	// setup the size of the produced vector
		int k = common.size();
		int size;
		if(symmetric)
			size = (k*k - k) / 2 + k;
		else
			size = k*k;
		int result[] = new int[size];
		Arrays.fill(result, 0);
		
		// fill the non-zero cells
		for(int i=0;i<terms.length;i++)
		{	int i2 = common.get(terms[i]);
			int start = 0;
			if(symmetric)
				start = i;
			for(int j=start;j<terms.length;j++)
			{	int j2 = common.get(terms[j]);
				int idx;
				if(symmetric)
					idx = (k*i2 - (i2-1)*i2/2) + (j2 - i2);
				else
					idx = k*i2 + j2;
				result[idx] = m[i][j];
			}
		}
		
		return result;
	}
	
	////////////////////////////////////////////////////////////
	////	DISTANCE PROCESSING
	////////////////////////////////////////////////////////////
	/**
	 * Processes the Euclidean distance between
	 * the specified vectors.
	 * 
	 * @param v1
	 * 		First integer vector.
	 * @param v2
	 * 		Second integer vector (same size than the first one).
	 * @return
	 * 		The Euclidean distance between the vectors.
	 */
	private float processEuclideanDistance(int[] v1, int[] v2)
	{	float result = 0;
		
		// for debug
//		System.out.print("["+DATE_FORMAT.format(CALENDAR.getTime())+"] " + "    v1: ");
//		for(int val: v1) System.out.print(" "+val);System.out.println();
//		System.out.print("["+DATE_FORMAT.format(CALENDAR.getTime())+"] " + "    v2: ");
//		for(int val: v2) System.out.print(" "+val);System.out.println();
	
		for(int i=0;i<v1.length;i++)
		{	int diff = v1[i] - v2[i];
			result = result + diff*diff;
		}
		
		result = (float)Math.sqrt(result);
		
		return result;
	}
}
