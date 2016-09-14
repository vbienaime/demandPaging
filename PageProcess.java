/**
 * PageProcess Class Contains all the process' data fields, getters & setters
 * 
 * @author VB
 *
 */
public class PageProcess {

    private double A;
    private double B;
    private double C;
    private int processNum;
    private boolean started;

    private int pageFaults = 0;
    private int pageResidencyTime = 0;

    private int savedWord;
    private int refCount;

    private double sum;
    private int numOfEvictions;

    private Page[] PageTable = new Page[DemandPaging.S / DemandPaging.P];

    public PageProcess(double a, double b, double c, int x) {

        this.setA(a);
        this.setB(b);
        this.setC(c);
        this.setProcessNum(x);
        this.setStarted(false);

    }

    public double getA() {
        return A;
    }

    public void setA(double a) {
        A = a;
    }

    public double getB() {
        return B;
    }

    public void setB(double b) {
        B = b;
    }

    public double getC() {
        return C;
    }

    public void setC(double c) {
        C = c;
    }

    public int getProcessNum() {
        return processNum;
    }

    public void setProcessNum(int processNum) {
        this.processNum = processNum;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean beenStarted) {
        this.started = beenStarted;
    }

    public int getPageFaults() {
        return pageFaults;
    }

    public void setPageFaults(int pageFaults) {
        this.pageFaults = pageFaults;
    }

    public int getPageResidencyTime() {
        return pageResidencyTime;
    }

    public void setPageResidencyTime(int pageResidencyTime) {
        this.pageResidencyTime = pageResidencyTime;
    }

    public int getSavedWord() {
        return savedWord;
    }

    public void setSavedWord(int savedWord) {
        this.savedWord = savedWord;
    }

    public int getRefCount() {
        return refCount;
    }

    public void setRefCount(int refCount) {
        this.refCount = refCount;
    }

    public double getSum() {
        return this.sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    /**
     * Goes through this process' page table and calculate a running sum
     */
    public void calcSum() {

        int x = 0;

        for (int i = 0; i < this.PageTable.length; i++) {

            if (this.PageTable[i] != null)

                x = x + this.PageTable[i].getSum();
        }
        this.sum = x;

    }

    public int getNumOfEvictions() {
        return numOfEvictions;
    }

    public void setNumOfEvictions(int numOfEvictions) {
        this.numOfEvictions = numOfEvictions;
    }
    
    public Page[] getPageTable() {
        return PageTable;
    }

    public void setPageTable(Page[] pageTable) {
        PageTable = pageTable;
    }

}
