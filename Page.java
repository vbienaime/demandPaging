/**
 * Page Object w/ getters & setters
 * 
 * @author VB
 *
 */
public class Page {

    private int pageNumber;
    private int startTime;
    private int endTime;
    private int sum;

    public Page(int pagenum, int start) {

        this.setPageNumber(pagenum);
        this.setStartTime(start);

    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startWord) {
        this.startTime = startWord;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endWord) {
        this.endTime = endWord;
    }

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

}
