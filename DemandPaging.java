import java.io.File;
import java.io.FileNotFoundException;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * Lab 4: Demand Paging w/ LIFO, LRU, & Random
 * 
 * @author VB
 *
 */
public class DemandPaging {

    // Program input variables
    static int M; // the machine size in words.
    static int P; // the page size in words.
    static int S; // the size of a process,
    static int J; // the ‘‘job mix’’, which determines A, B, and C
    static int N; // the number of references for each process.
    static String R; // the replacement algorithm, LIFO (NOT FIFO), RANDOM, or LRU.

    // List of processes to stimulate RR
    static Deque<PageProcess> ListOfProcesses = new LinkedList<PageProcess>();

    // Array of Frames
    static FrameTableEntry[] frames;

   
    static int randomNumber = 0;
    static Scanner randomReader = null;
    
    static int lastWordReferenced;
    static int numOfFrames;
    static int lastFrameUsed;
    
    static int clock;

    /**
     * 
     * @param Args
     */
    public static void main(String[] Args) {

        // End program if no arguments were entered, only one was entered, or
        // more than 6 were entered
        if (Args.length == 0 || Args.length < 2 || Args.length > 7) {
            System.err
            .println("ERROR: You did not input all 6 command line arguments needed.");
            System.exit(1);
        }
       
        String sep = System.getProperty("file.separator");

        // Hard coded random numbers file based on current working directory
        String randomFilePath = System.getProperty("user.dir") + sep + "random-numbers.txt";
        
        //System.out.println(randomFilePath);
        File randomNumFile = new File(randomFilePath);


        // End program if file is empty
        if (randomNumFile.length() == 0) {
            System.err.println("ERROR: File is empty.");
            System.exit(1);
        }

        
        randomReader = createFileScanner(randomNumFile);

        M = Integer.parseInt(Args[0]);// the machine size in words.

        P = Integer.parseInt(Args[1]); // the page size in words.

        S = Integer.parseInt(Args[2]);// the size of a process

        J = Integer.parseInt(Args[3]);// the ‘‘job mix’’

        N = Integer.parseInt(Args[4]); //the number of references for each process.

        R = Args[5]; //  the replacement algorithm, LIFO (NOT FIFO), RANDOM, or LRU.

        numOfFrames = M / P;

        frames = new FrameTableEntry[numOfFrames];

        // Initialize frame table
        for (int i = 0; i < numOfFrames; i++) {
            frames[i] = new FrameTableEntry(-1, -1);

        }
        
        // Create LinkedList of Processes based on job mix
        createProcesses();

        // Commence stimulation
        pagingDriver();

        // print Results
        printOutput();

    }

    /**
     * With four possible sets of processes, chooses a set
     * based on J and creates corresponding processes
     * 
     */
    private static void createProcesses() {

        // PageProcess Constructor arguments: A, B, C, process #
        
        if (J == 1) {// One process with A=1 and B=C=0, the simplest (fully
            // sequential) case.
            PageProcess x = new PageProcess(1, 0, 0, 1);
            ListOfProcesses.add(x);

        }
        if (J == 2) {// Four processes, each with A=1 and B=C=0.

            for (int i = 1; i <= 4; i++) {
                PageProcess x = new PageProcess(1, 0, 0, i);
                ListOfProcesses.add(x);
            }
        }
        if (J == 3) {// Four processes, each with A=B=C=0 (fully random
            // references).

            for (int i = 1; i <= 4; i++) {
                PageProcess x = new PageProcess(0, 0, 0, i);
                ListOfProcesses.add(x);
            }
        }
        if (J == 4) {// One process with A=.75, B=.25 and C=0; one process with
            // A=.75, B=0, and C=.25;
            // one process with A=.75, B=.125 and C=.125;
            // and one process with A=.5, B=.125 and C=.125.

            PageProcess x = new PageProcess(0.75, 0.25, 0, 1);
            ListOfProcesses.add(x);

            PageProcess y = new PageProcess(0.75, 0, 0.25, 2);
            ListOfProcesses.add(y);

            PageProcess z = new PageProcess(0.75, 0.125, 0.125, 3);
            ListOfProcesses.add(z);

            PageProcess zz = new PageProcess(0.5, 0.125, 0.125, 4);
            ListOfProcesses.add(zz);
        }
        else System.err.println("ERROR: No Job Mix entered");
        
    }

    /**
     * Main component of Stimulation: calls on method to create word references,
     * checks if a page hit occurs and if a fault occurs, calls on the particular PRA needed 
     */
    private static void pagingDriver() {
        
        // Quantum for RR Scheduling
        int quantum = 3;
        clock = 0;

        
        // Number of References per process * number of processes
        int numOfCycles = N * ListOfProcesses.size();

        while (clock < numOfCycles) {
            
            PageProcess current = null;
            
            // Runs process for quantum cycles, and switches to next process in list
            for (int numOfRefs = 0; numOfRefs < quantum; numOfRefs++) {

                // Process about to run
                current = ListOfProcesses.peek();

                // if current process has been referenced more than the times allowed, break;
                if (current.getRefCount() == N)  break;

                int word;
                
                // If this is process' beginning run, do not use random number
                if (current.isStarted() == false) {

                    word = (111 * current.getProcessNum() % S);
                    current.setStarted(true);

                    lastWordReferenced = word;

                 
                } else if (numOfRefs == 0 && current.isStarted()) {
                    // if Process is running again, after being preempted
                    // use the word that was computed before it was preempted 
                    
                    word = current.getSavedWord();
                    lastWordReferenced = word;
                } else {
                    // Any other case, calculate new reference
                    word = getNextReferencedWord(current);
                    lastWordReferenced = word;
                }
                
                
                // To get page number, divide current word by page size
                int pageNum = word / P;
                
                boolean hit = isAHit(current, pageNum);
                
                if (!hit) {
                    
                    // Page Fault
                    current.setPageFaults(current.getPageFaults() + 1);
                    pageReplacementAlgorithm(current.getProcessNum(), pageNum, current);

                }    
                clock++; // after each reference
                current.setRefCount(current.getRefCount() + 1); // Increment that process' ref count

            }

            // Before you switch to next process, save the next 
            // Word referenced by the current process
            current.setSavedWord(getNextReferencedWord(current));
            ListOfProcesses.addLast(current);
            ListOfProcesses.removeFirst(); // add to back of list

        }

    }

    /**
     * Calls the corresponding PRA based on command line argument
     * 
     * @param processNumber
     * @param page: page number
     * @param currentProcess
     */
    private static void pageReplacementAlgorithm(int processNumber, int page, PageProcess currentProcess) {
       
        if (R.equalsIgnoreCase("lifo"))
            lifo(processNumber, page, currentProcess);

        if (R.equalsIgnoreCase("random"))
            random(processNumber, page, currentProcess);

        if (R.equalsIgnoreCase("lru"))
            lru(processNumber, page, currentProcess);

    }

    /**
     * Least Recently Used Page Replacement Algorithm
     * 
     * Keep a time stamp in each PTE, updated on each reference
     * and scan all the PTEs when choosing a victim to
     * find the PTE with the oldest time stamp.
     * 
     * @param processNumb - process Number of page about to be replaced
     * @param pageNumb - page being replaced
     * @param current - current process being replaced
     */
    private static void lru(int processNumb, int pageNumb, PageProcess current) {

        boolean evicted = false;
        
        // Look for a empty frame, and if there are more than one,
        // choose the highest numbered free frame
        
        int replacedFrame = -1;
        for (int i = 0; i < numOfFrames; i++) {
            if (!frames[i].isValid()) // a frame is not valid if free
                replacedFrame = i;
            
        }
        
        // If no empty frame has been found, time to evict a page
        // Evict the page that has the oldest time at the moment
        
        if (replacedFrame == -1) {
            
            evicted = true;
            
            int time = Integer.MAX_VALUE;

            for (int i = 0; i < numOfFrames; i++) {
                if (frames[i].getTimeLastUsed() < time) {
                    time = frames[i].getTimeLastUsed();
                    replacedFrame = i;
                }
               
            }

        }
       
        calculateNumOFEvictionsAndResidency (evicted, replacedFrame, processNumb, pageNumb, current);

    }

    /**
     * Last In, First Out Page Replacement 
     * 
     * Evict from the frame last used
     * 
     * @param processNumb - process Number of page about to be replaced
     * @param pageNumb - page being replaced
     * @param current - current process being replaced
     */
    private static void lifo(int processNumb, int pageNumb, PageProcess current) {
        
        boolean evicted = false;
        
        // Look for a empty frame, and if there are more than one,
        // choose the highest numbered free frame
        
        int replacedFrame = -1;
        for (int i = 0; i < numOfFrames; i++) {
            if (!frames[i].isValid())
                replacedFrame = i;
        }
    
        // If no empty frame has been found, time to evict a page
        // Evict the page from the last used Frame 
        
        if (replacedFrame == -1) {
            replacedFrame = lastFrameUsed;
            evicted = true;
        }
        
        calculateNumOFEvictionsAndResidency (evicted, replacedFrame, processNumb, pageNumb, current);
    
    }

    /**
     * Random Page Replacement Algorithm
     * 
     * Remove a page from a random frame
     * 
     * @param processNumb - process Number of page about to be replaced
     * @param pageNumb - page being replaced
     * @param current - current process being replaced
     */
    private static void random(int processNumb, int pageNumb, PageProcess current) {
        
        boolean evicted = false;

        // Look for a empty frame, and if there are more than one,
        // choose the highest numbered free frame
         
        int replacedFrame = -1;
        for (int i = 0; i < numOfFrames; i++) {
            if (!frames[i].isValid())
                replacedFrame = i;
        }

        // If no empty frame has been found, time to evict a page
        // Evict the page from a randomly chosen frame
        if (replacedFrame == -1) {
            // if full, pick random page
            replacedFrame = randomReader.nextInt() % numOfFrames;
            evicted = true;
    
        }
        
        calculateNumOFEvictionsAndResidency (evicted, replacedFrame, processNumb, pageNumb, current);
    
    }

    /**
     * Is A Hit
     * 
     * Looks at current frame table and if it contains a particular
     * page from a particular process
     * 
     * @param current: current Process
     * @param pageNum : current Process Page
     * @return true if page is in a frame; else, if a fault - false
     */
    private static boolean isAHit(PageProcess current, int pageNum) {
        
        boolean isInTable = false;
        
        for (int i = 0; i < numOfFrames; i++) {
            // both process # and page # must match for it to be a hit
            if (frames[i].getPageNumber() == pageNum
                    && frames[i].getProcessNumber() == current.getProcessNum()) {
                
                isInTable = true;
                frames[i].setTimeLastUsed(clock);
            }
        }
        return isInTable;
    }

    /**
     * Generate next word to reference, based on 4 cases w/ certain probabilities 
     * 
     * @param process: current process
     * @return nextReference
     */
    private static int getNextReferencedWord(PageProcess process) {
        
        // read random number from file
        randomNumber = randomReader.nextInt();
        
        double y = randomNumber / (Integer.MAX_VALUE + 1d);
        
        int newWord;
        
        // Case 1
        // If y<A, do case 1 (it occurs with probability A),
        if (y < process.getA())
            newWord = (lastWordReferenced + 1) % S;
    
        // Case 2
        // if y<A+B, do case 2, (it occurs with probability B),
        else if (y < (process.getA() + process.getB()))
            newWord = (lastWordReferenced - 5 + S) % S;
    
        // Case 3
        // if y<A+B+C, do case 3 (it occurs with probability C).
        else if (y < (process.getA() + process.getB() + process.getC()))
            newWord = (lastWordReferenced + 4) % S;
    
        // Case 4
        // if y>=A+B+C , do case 4 (it occurs with probability 1-A-B-C.)
        else {
            newWord = (randomReader.nextInt()) % S;
           
        }
        return newWord;
    }

    /**
     * Calculate the number of evictions per process, and overall residence time
     * every time a page is replaced 
     * 
     * @param evicted - true if a page is about to be evicted, else false 
     * @param replacedFrame - the frame being replaced
     * @param processNumb - new process number for new page
     * @param pageNumb - new  page 
     * @param current - current process
     */
    private static void calculateNumOFEvictionsAndResidency(boolean evicted, int replacedFrame,
            int processNumb, int pageNumb, PageProcess current) {
        
        // same old page #, and old process # to calculate end time for running sum
        int oldPageNum = frames[replacedFrame].getPageNumber();
        int oldProcNum = frames[replacedFrame].getProcessNumber();
        
        if (evicted) {
          
            // Iterate over list of Processes, find the process that correlates w/ the old 
            // page num and calculate it's running sum
            Iterator<PageProcess> iterator = ListOfProcesses.iterator();

            while (iterator.hasNext()) {

                PageProcess x = iterator.next();
                
                // Once you get to the right process, you can easily access it's page table
                if (x.getProcessNum() == oldProcNum) {

                    x.getPageTable()[oldPageNum].setEndTime(clock);
                    
                    int l = x.getPageTable()[oldPageNum].getSum();
                    int m = x.getPageTable()[oldPageNum].getEndTime();
                    int n = x.getPageTable()[oldPageNum].getStartTime();
                    
                    // save running sum
                    x.getPageTable()[oldPageNum].setSum(l + (m - n));

                }
                
            }
            
            
            // Calculate it's number of eviction
            frames[replacedFrame].getPage().setNumOfEvictions(
                    (frames[replacedFrame].getPage().getNumOfEvictions()) + 1);

        }
        
        // set the frame to the new page being inserted
        frames[replacedFrame].setPageNumber(pageNumb);
        frames[replacedFrame].setProcessNumber(processNumb);
        frames[replacedFrame].setTimeLastUsed(clock);
        frames[replacedFrame].setVald(true);

        lastFrameUsed = replacedFrame; 

        // Set the new frame's page being loaded's start time
        frames[replacedFrame].setPage(current);
      
        // Set the new frame's page being loaded's start time
        if (current.getPageTable()[pageNumb] != null) {
            current.getPageTable()[pageNumb].setStartTime(clock);
        } else {
            Page page = new Page(pageNumb, clock);
            current.getPageTable()[pageNumb] = page;

        }

        
    }

    /**
     * Create Scanner object on file
     * 
     * @param data
     * @return Scanner object
     */
    private static Scanner createFileScanner(File data) {
        Scanner reader = null;
        try {
            reader = new Scanner(data);
        } catch (FileNotFoundException e) {
            System.err
                    .println("ERROR: You did not enter a valid or correct file path; a file was not found.");
            System.exit(1);
        }
        return reader;
    }

    private static void printOutput() {
    
        System.out.println("The machine size is " + M +".");
        System.out.println("The page size is " + P+".");
        System.out.println("The process size is " + S+".");
        System.out.println("The job mix number is " + J+".");
        System.out.println("The number of references per process is " + N+".");
        System.out.println("The replacement algorithm is " + R+".");
        System.out.println("The level of debugging output is 0");
    
        System.out.println();
       
    
        Iterator<PageProcess> iterator = ListOfProcesses.iterator();
    
        int totalFaults = 0;
        double totalAverage = 0;
        double numOfResicidence = 0;
        
        while (iterator.hasNext()) {
    
            PageProcess x = iterator.next();
            x.calcSum(); // calculate running sum for each process
    
            double result = (double) (x.getSum() / x.getNumOfEvictions());
    
            if (x.getNumOfEvictions() == 0) {
                System.out.println("Process " + x.getProcessNum() + " had " + x.getPageFaults()
                        + " faults.");
                System.out.println("\t With no evictions, the average residence is undefined.");
    
            } else {
                System.out.println("Process " + x.getProcessNum() + " had " + x.getPageFaults()
                        + " faults and " + result + " average residency.");
                totalAverage = totalAverage + (double) (x.getSum());
                numOfResicidence = numOfResicidence + x.getNumOfEvictions();
    
            }
    
            totalFaults = totalFaults + x.getPageFaults();
           
        }
    
        System.out.println();
        double xx = totalAverage / numOfResicidence; 
        
        if (Double.isNaN(xx)) {
    
            System.out.println("The total number of faults is " + totalFaults + ".");
            System.out.println("\t With no evictions, the overall average residence is undefined.");
        } else {
            System.out.println("The total number of faults is " + totalFaults
                    + " and the overall average residency is " + xx + ".");
        }
    }

}
