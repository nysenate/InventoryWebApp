package gov.nysenate.inventory.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RemovalRequest
{
    private int transactionNum;
    private List<InvItem> items;
    private AdjustCode adjustCode;
    private final String employee;
    private Date date;
    private String status;

    public RemovalRequest(String employee) {
        this.employee = employee;
        this.items = new ArrayList<InvItem>();
    }

    public int getTransactionNum() {
        return transactionNum;
    }

    public void setTransactionNum(int transactionNum) {
        this.transactionNum = transactionNum;
    }

    public List<InvItem> getItems() {
        return items;
    }

    public void addItem(InvItem item) {
        items.add(item);
    }

    public void deleteItem(InvItem item) {
        items.remove(item);
    }

    public AdjustCode getAdjustCode() {
        return adjustCode;
    }

    public void setAdjustCode(AdjustCode adjustCode) {
        this.adjustCode = adjustCode;
    }

    public String getEmployee() {
        return employee;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
