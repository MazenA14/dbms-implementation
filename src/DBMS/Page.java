package DBMS;

import java.io.Serializable;
import java.util.ArrayList;

public class Page implements Serializable
{
    public int pageNumber;
    public ArrayList<String[]> records;
    public int recordCount;
    public int pageSize;
    public String tableName;

    public Page(int pageNumber, String tableName, int pageSize) {
        this.pageNumber = pageNumber;
        this.records = new ArrayList<String[]>();
        this.recordCount = records.size();
        this.pageSize = DBApp.dataPageSize;;
        this.tableName = tableName;
    }
    public String[] getRecord(int recordIndex ) {
        return records.get(recordIndex);
    }

    public int getPageNumber() {
        return pageNumber;
    }
    public void addRecord(String[] record) {
        records.add(record);
    }
    public int getRecordCount() {
        return  recordCount;
    }
    public ArrayList<String[]> getRecords() {
        return records;
    }
}