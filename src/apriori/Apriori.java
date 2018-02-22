package apriori;
import java.io.*;
import java.util.*;

/** The Apriori mining algorithm to compute frequent item sets.
 *
 * Precondition: Datasets contains integers (>=0) separated by spaces, one transaction by line; e.g.
 * 1 5 3
 * 4 9
 * 10 100
 *    
 * Usage as library: see {@link ExampleOfClientCodeOfApriori}
 * 
 * @author Rebecca Ramnauth
 * @date 22 February 2018
 */
public class Apriori extends Observable {

    public static void main(String[] args) throws Exception {
        Apriori ap = new Apriori(args);
    }

    private List<int[]> itemsets ;          // list of current item sets
    private String transaFile;              // name of the data/transaction file
    private int numItems;                   // number of unique items in the dataset
    private int numTransactions;            // total number of transaction in dataset
    private double minSup;                  // minimum support for frequent item set in % (e.g., 0.8 for 80%)
    private double confd;                   // total confidence
    private List<int[]> originsets;         // list of original item sets
    
    private boolean usedAsLibrary = false;  // to use as a library in cmd

    /** Interface: using this class as a library */
    public Apriori(String[] args, Observer ob) throws Exception {
    	usedAsLibrary = true;
    	configure(args);
    	this.addObserver(ob);
    	go();
    }

    /** Configuration: generate itemsets from local file
     *  @param args > configuration parameters: 
     *      args[0] = filename
     *      args[1] = minimum support (%)
     */
    public Apriori(String[] args) throws Exception {
        configure(args);
        go();
    }

    /** starts apriori algorithm after configuration */
    private void go() throws Exception {
        long start = System.currentTimeMillis(); // start timer
        
        createItemsetsOfSize1(); // first generate the candidates of size 1    
        int itemsetNumber = 1; //the current itemset being looked at
        int nbFrequentSets = 0;
        
        while (itemsets.size() > 0) {
            calculateFrequentItemsets();

            if(itemsets.size()!= 0) {
                nbFrequentSets+=itemsets.size();
                log("Found " + itemsets.size() + " frequent itemsets of size " + itemsetNumber + " (with support " +(minSup * 100) + "%)");
                createNewItemsetsFromPreviousOnes();
            }
            
            itemsetNumber++;
        } 

        //display the execution time
        long end = System.currentTimeMillis();
        log("Execution time is: "+((double)(end-start)/1000) + " seconds.");
        log("Found " + nbFrequentSets + " frequents sets for support " +(minSup*100) + "% (absolute " + Math.round(numTransactions*minSup) + ")");
        log("Done");
    }

    /** triggers actions if a frequent item set has been found  */
    private void foundFrequentItemSet(int[] itemset, int support) {
    	if (usedAsLibrary) {
            this.setChanged();
            notifyObservers(itemset);
    	}
    	else {System.out.println(Arrays.toString(itemset) +  "  (support/transaction (%): " + ((support / (double) numTransactions)) + "; raw support: " + support + ")");}
    }

    /** outputs a message in Sys.err if not used as library */
    private void log(String message) {
    	if (!usedAsLibrary) {
    		System.err.println(message);
    	}
    }

    /** computes numItems, numTransactions, and sets minSup */
    private void configure(String[] args) throws Exception {        
        // establish the transaction file > dataset
        if (args.length!=0) transaFile = args[0];
        else {  // not a call from library use > ask for dataset file as input
            System.out.print("Enter the transaction's filename: ");
            Scanner file = new Scanner(System.in);
            String filename = file.nextLine();
            if (!((new File(filename)).exists())) {
                filename = "default.txt";
                System.out.println("File not found. Will execute a default dataset \"default.txt\"");
            }
            transaFile = filename;
        }
    	
    	// setting minsupport
    	if (args.length>=2) minSup=(Double.valueOf(args[1]).doubleValue());    	
    	else {
            System.out.print("Enter minimum support (0 < input < 1): ");
            Scanner sup = new Scanner (System.in);
            double support = sup.nextDouble();
            if (minSup>1 || minSup<0) {
                support = 0.8; // default minimum support value
                System.out.println("Illegal value. Minimum support defaulted to 0.8");
            }
            minSup = support;
        }
    	if (minSup>1 || minSup<0) throw new Exception("Minimum Support: Illegal Value");
    	
    	// going thourgh the file to compute numItems and numTransactions
    	numItems = 0;
    	numTransactions=0;
        
    	BufferedReader data_in = new BufferedReader(new FileReader(transaFile));
        originsets = new ArrayList<int[]>();
    	while (data_in.ready()) {    		
    		String line=data_in.readLine();
    		if (line.matches("\\s*")) continue; // consider empty lines
    		numTransactions++;
    		StringTokenizer t = new StringTokenizer(line," ");
                
    		while (t.hasMoreTokens()) {
    			int x = Integer.parseInt(t.nextToken());
                        if (x+1>numItems) numItems=x+1;
    		}
                originsets.add(tokenArray(t));
        }    	
        outputConfig();
        originReader(originsets);
    }
    
    private String tokenReader(StringTokenizer in){
        String sum = "";
        while(in.hasMoreTokens())
            sum += " " + in.nextToken();
        return sum;
    }
    private int[] tokenArray(StringTokenizer in){
        String sum = tokenReader(in);
        String[] strArr = sum.split(" ");
        int[] intArr = new int[strArr.length];
        for (int i = 1; i < strArr.length; i++){
            String num = strArr[i];
            intArr[i] = Integer.parseInt(num);
        }
        return intArr;
    }
    private void originReader(List<int[]> set){
        String sum = "";
        for (int i = 0; i < set.size(); i++){
            for (int j = 0; j < set.get(i).length; j++){
                int[] row = set.get(i);
                //System.out.println(String.valueOf(row[j]));
            }
        }
    }

   /** outputs the current configuration */ 
	private void outputConfig() {
		//output config info to the user
                 log("********************** CONFIGURATION ********************* ");
		 log("Input configuration: "+numItems+" unique elements, "+numTransactions+" transactions");
		 log("Minimum support = "+ (minSup*100) +"%");
                 log("********************************************************** ");
	}

	/** determines all sets of size 1 / list unique items of the datasets */
	private void createItemsetsOfSize1() {
		itemsets = new ArrayList<int[]>();
        for(int i=0; i<numItems; i++) {
        	int[] cand = {i};
        	itemsets.add(cand);
        }
    }
			
    /** create item set of items size n+1; where n is current size */
    private void createNewItemsetsFromPreviousOnes() {
    	// by construction, all existing itemsets have the same size
    	int currentSizeOfItemsets = itemsets.get(0).length;
    	log("Creating itemsets of size "+(currentSizeOfItemsets+1)+" based on "+itemsets.size()+" itemsets of size "+currentSizeOfItemsets + "...");
    		
    	HashMap<String, int[]> tempCandidates = new HashMap<String, int[]>(); //temporary candidates
    	
        // compare each pair of itemsets of size n-1
        for(int i=0; i<itemsets.size(); i++) {
            for(int j=i+1; j<itemsets.size(); j++) {
                int[] X = itemsets.get(i);
                int[] Y = itemsets.get(j);

                assert (X.length==Y.length);
                
                //make a string of the first n-2 tokens of the strings
                int [] newCand = new int[currentSizeOfItemsets+1];
                for(int s=0; s<newCand.length-1; s++) {
                	newCand[s] = X[s];
                }
                    
                int ndifferent = 0;
                // then we find the missing value
                for(int s1=0; s1<Y.length; s1++) {
                	boolean found = false;
                	// is Y[s1] in X?
                    for(int s2=0; s2<X.length; s2++) {
                    	if (X[s2]==Y[s1]) { 
                    		found = true;
                    		break;
                    	}
                    }
                	if (!found){ // Y[s1] is not in X
                		ndifferent++;
                		// we put the missing value at the end of newCand
                		newCand[newCand.length -1] = Y[s1];
                	}
            	}
                
                // we have to find at least 1 different, otherwise it means that we have two times the same set in the existing candidates
                assert(ndifferent>0);
                
                
                if (ndifferent==1) {
                    // HashMap does not have the correct "equals" for int[] :-(
                    // I have to create the hash myself using a String :-(
                	// I use Arrays.toString to reuse equals and hashcode of String
                	Arrays.sort(newCand);
                	tempCandidates.put(Arrays.toString(newCand),newCand);
                }
            }
        }
        
        //set the new itemsets
        itemsets = new ArrayList<int[]>(tempCandidates.values());
    	log("Created "+itemsets.size()+" unique itemsets of size "+(currentSizeOfItemsets+1));

    }



    /** put "true" in trans[i] if the integer i is in line */
    private void line2booleanArray(String line, boolean[] trans) {
	    Arrays.fill(trans, false);
	    StringTokenizer stFile = new StringTokenizer(line, " "); //read a line from the file to the tokenizer
	    //put the contents of that line into the transaction array
	    while (stFile.hasMoreTokens())
	    {
	    	
	        int parsedVal = Integer.parseInt(stFile.nextToken());
			trans[parsedVal]=true; //if it is not a 0, assign the value to true
	    }
    }

    
    /** passes through the data to measure the frequency of sets in {@link itemsets},
     *  then filters those who are under the minimum support (minSup)
     */
    private void calculateFrequentItemsets() throws Exception {
    	
        log("Reading data to compute the frequency of " + itemsets.size()+ " itemsets of size "+itemsets.get(0).length + "...");

        List<int[]> frequentCandidates = new ArrayList<int[]>(); //the frequent candidates for the current itemset

        boolean match; //whether the transaction has all the items in an itemset
        int count[] = new int[itemsets.size()]; //the number of successful matches, initialized by zeros


		// load the transaction file
		BufferedReader data_in = new BufferedReader(new InputStreamReader(new FileInputStream(transaFile)));

		boolean[] trans = new boolean[numItems];
		
		// for each transaction
		for (int i = 0; i < numTransactions; i++) {

			// boolean[] trans = extractEncoding1(data_in.readLine());
			String line = data_in.readLine();
			line2booleanArray(line, trans);

			// check each candidate
			for (int c = 0; c < itemsets.size(); c++) {
				match = true; // reset match to false
				// tokenize the candidate so that we know what items need to be
				// present for a match
				int[] cand = itemsets.get(c);
				//int[] cand = candidatesOptimized[c];
				// check each item in the itemset to see if it is present in the
				// transaction
				for (int xx : cand) {
					if (trans[xx] == false) {
						match = false;
						break;
					}
				}
				if (match) { // if at this point it is a match, increase the count
					count[c]++;
					//log(Arrays.toString(cand)+" is contained in trans "+i+" ("+line+")");
				}
			}

		}
		
		data_in.close();

		for (int i = 0; i < itemsets.size(); i++) {
			// if the count% is larger than the minSup%, add to the candidate to
			// the frequent candidates
			if ((count[i] / (double) (numTransactions)) >= minSup) {
				foundFrequentItemSet(itemsets.get(i),count[i]);
				frequentCandidates.add(itemsets.get(i));
			}
			//else log("-- Remove candidate: "+ Arrays.toString(candidates.get(i)) + "  is: "+ ((count[i] / (double) numTransactions)));
		}

        //new candidates are only the frequent candidates
        itemsets = frequentCandidates;
    }
}