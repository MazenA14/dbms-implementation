package DBMS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import DBMS.Page;
public class Table implements Serializable
{
    public String tableName;
    public String[] columnsNames;
    public int pagesCount;
    public ArrayList<Page> pages;
    public ArrayList<String> traceAll;
    public int recordNumbers;

    public Table(String tableName, String[] columnsNames) {
        this.recordNumbers = 0;
        this.tableName = tableName;
        this.columnsNames = columnsNames;
        this.pagesCount = 0;
        this.pages = new ArrayList<Page>();
        this.traceAll = new ArrayList<String>();
    }
    public String getTableName() {
        return tableName;
    }
    public String[] getColumnsNames() {
        return columnsNames;
    }
    public ArrayList<String> getTrace() {
        return traceAll;
    }
    public int getPagesCount(){
        return  pagesCount;
    }
    public void setTrace(String trace) {
        traceAll.add(trace);
    }
}