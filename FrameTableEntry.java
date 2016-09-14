/**
 * Frame Table Object w/ getters & setters
 * 
 * @author VB
 *
 */
public class FrameTableEntry {

    private boolean valid = false;
    private int processNumber;
    private int pageNumber;
    private int timeLastUsed = 0;
    private PageProcess page;

    public FrameTableEntry(int x, int y) {

        this.setProcessNumber(x);
        this.setPageNumber(y);

    }

    public int getProcessNumber() {
        return processNumber;
    }

    public void setProcessNumber(int processNumber) {
        this.processNumber = processNumber;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public boolean isValid() {
        return valid;
    }

    public void setVald(boolean setValid) {
        this.valid = setValid;
    }

    public int getTimeLastUsed() {
        return timeLastUsed;
    }

    public void setTimeLastUsed(int timeLastUsed) {
        this.timeLastUsed = timeLastUsed;
    }
    
    public PageProcess getPage() {
        return page;
    }

    public void setPage(PageProcess page) {
        this.page = page;
    }
}
