package DBMS;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import DBMS.Table;
import org.junit.Test;

public class DBApp
{
	public static int dataPageSize = 2;
	static ArrayList<Table> tables = new ArrayList<Table>();


	public static void createTable(String tableName, String[] columnsNames) {
		for (int i = 0; i < tables.size(); i++) {
			Table currentTable = tables.get(i);
			if (currentTable.getTableName().equals(tableName)) {
				System.out.println("Table '" + tableName + "' already exists.");
				return;
			}
		}

		Table newTable = new Table(tableName, columnsNames);
		newTable.setTrace("Table created name:" + tableName + ", columnsNames:" + Arrays.toString(columnsNames));
		tables.add(newTable);
		FileManager.storeTable(tableName, newTable);
//		System.out.println("Table '" + tableName + "' created successfully.");
	}

	public static void insert(String tableName, String[] record) {
		int StartTime = (int) System.currentTimeMillis();
		Table table = FileManager.loadTable(tableName);
		if (table == null) {
			System.out.println("Table not found!");
			return;
		}
		int pagesCount2 = table.getPagesCount();
		Page lastpage;

		// Enters only once, first time
		if (pagesCount2 == -1) {
			lastpage = new Page(0, tableName, dataPageSize);
			lastpage.addRecord(record);
			lastpage.recordCount++;
			FileManager.storeTablePage(tableName, lastpage.pageNumber, lastpage);
			table.pagesCount = 0;
		} else {
			lastpage = FileManager.loadTablePage(tableName, table.pagesCount);
			if (lastpage.recordCount == dataPageSize) {
				lastpage = new Page(lastpage.pageNumber + 1, tableName, dataPageSize);
				lastpage.addRecord(record);
				lastpage.recordCount++;
				FileManager.storeTablePage(tableName, lastpage.pageNumber, lastpage);
				table.pagesCount++;
			} else {
				lastpage.addRecord(record);
				lastpage.recordCount++;
				FileManager.storeTablePage(tableName, lastpage.pageNumber, lastpage);
			}
		}

		table.recordNumbers++;
		table.setPages(lastpage);
		FileManager.storeTable(tableName, table);

		int EndTime = (int) System.currentTimeMillis();
		int ExecutionTime = EndTime - StartTime;
		table.setTrace("Inserted:" + Arrays.toString(record) + ", at page number:" + table.getPagesCount() + ", execution time: " + ExecutionTime);
		FileManager.storeTable(tableName, table);
	}

	public static ArrayList<String[]> select(String tableName) {
		int StartTime = (int) System.currentTimeMillis();
		ArrayList <String[]> results = new ArrayList<String[]>();
		Table table = FileManager.loadTable(tableName);
		int pagesCount = 0;
		if (table == null) {
			return null;
		}
		for ( int i = 0; i <= table.pagesCount; i++) {
			pagesCount += 1;
			Page page = FileManager.loadTablePage(tableName, i);
			for (String[] record : page.getRecords()) {
				results.add(record);
			}

		}
		int EndTime = (int) System.currentTimeMillis();
		int ExecutionTime = EndTime - StartTime;
		table.setTrace("Select all pages:" + pagesCount + " records:" + table.recordNumbers+ ", execution time: " + ExecutionTime
				+ " (ms)");
		FileManager.storeTable(tableName, table);
		return results;
	}

	public static ArrayList<String[]> select(String tableName, int pageNumber, int recordNumber) {
		int StartTime = (int) System.currentTimeMillis();
		ArrayList <String[]> results = new ArrayList<String[]>();
		Table table = FileManager.loadTable(tableName);
		if (table == null) {
			return null;
		}
		Page page = FileManager.loadTablePage(tableName, pageNumber);
		results.addFirst(page.getRecord(recordNumber));
		int EndTime = (int) System.currentTimeMillis();
		int ExecutionTime = EndTime - StartTime;
		table.setTrace("Select pointer page:" + pageNumber + " record:" + recordNumber +" total output count:1"+ ", execution time: " + ExecutionTime
				+ " (ms)");
		FileManager.storeTable(tableName, table);
		return results;
	}
	
	public static ArrayList<String []> select(String tableName, String[] cols, String[] vals) {
		ArrayList<ArrayList<Integer>> outputTrace = new ArrayList<>();
		int [] colIndex = new int[cols.length];
		int matchCount = 0;
		int totalMatchCount = 0;
		boolean match = true;

		int StartTime = (int) System.currentTimeMillis();

		ArrayList <String[]> result = new ArrayList<String[]>();
		Table table = FileManager.loadTable(tableName);
		if (table == null) {
			return null;
		}
		String[] colNames = table.getColumnsNames();
		for (int i = 0; i < cols.length; i++) {
			for (int j = 0; j < colNames.length; j++) {
				if (cols[i].equals(colNames[j])) {
					colIndex[i] = j;
					break;
				}
			}
		}

		for (int i = 0; i <= table.pagesCount; i++) {
			Page page = FileManager.loadTablePage(tableName, i);
			page.getRecords().getFirst();
			for (String[] record : page.getRecords()) {
				match = true;
				for (int j = 0; j < colIndex.length; j++) {
					if(!record[colIndex[j]].equals(vals[j])) {
						match = false;
						break;
					}
				}
				if (match) {
					result.add(record);
				}
			}

			if (match) {
				matchCount++;
				ArrayList<Integer> tempArray = new ArrayList<>();
				tempArray.add(i);
				tempArray.add(matchCount);
				outputTrace.add(tempArray);
			}

			totalMatchCount += matchCount;
			matchCount = 0;
		}

		String outputString = convert2DArrayListToString(outputTrace);
		int EndTime = (int) System.currentTimeMillis();

		int ExecutionTime = EndTime - StartTime;

		table.setTrace("Select condition:" + Arrays.toString(cols) + "->" + Arrays.toString(vals) + ", Records per page:" + outputString + " records:" + totalMatchCount + " execution time: " + ExecutionTime
				+ " (ms)");

		FileManager.storeTable(tableName, table);
		return result;
	}

	public static String convert2DArrayListToString(ArrayList<ArrayList<Integer>> arrayList) {
		StringBuilder result = new StringBuilder("[");
		for (int i = 0; i < arrayList.size(); i++) {
			result.append("[");
			ArrayList<Integer> innerList = arrayList.get(i);
			for (int j = 0; j < innerList.size(); j++) {
				result.append(innerList.get(j));
				if (j < innerList.size() - 1) {
					result.append(", ");
				}
			}
			result.append("]");
			if (i < arrayList.size() - 1) {
				result.append(", ");
			}
		}
		result.append("]");
		return result.toString();
	}
	
	public static String getFullTrace(String tableName)
	{
//		String result = "Full Trace of the table:";
		String result = "";

		Table table = FileManager.loadTable(tableName);

		if (table == null) {
			return "Table not found";
		}
		else {
			ArrayList<String> output = table.getTrace();
			int counter = 0;
			for (String str : output) {
				if(counter == 0) {
					result += str;
					counter++;
				}
				else {
					result += "\n" + str;
				}
			}

			result += "\n" + "Pages Count: " + (table.pagesCount + 1) + ", Records Count: " + table.recordNumbers;
//			result += "\n" + "--------------------------------";
		}
		return result;
	}
	
	public static String getLastTrace(String tableName)
	{
//		String result = "Last Trace of the table:";
		String result = "";

		Table table = FileManager.loadTable(tableName);

		if (table == null) {
			return "Table not found";
		}
		else {
			ArrayList<String> output = table.getTrace();
			if (output.size() == 0) {
				return "No trace available";
			}
			else {
				result += output.getLast();
//				result += "\n" + "--------------------------------";
			}
		}
		return result;
	}
	
	public static void main(String []args) throws IOException
	{
		String[] cols = {"id","name","major","semester","gpa"};
		createTable("student", cols);
		String[] r1 = {"1", "stud1", "CS", "5", "0.9"};
		insert("student", r1);
		String[] r2 = {"2", "stud2", "BI", "7", "1.2"};
		insert("student", r2);
		String[] r3 = {"3", "stud3", "CS", "2", "2.4"};
		insert("student", r3);
		String[] r4 = {"4", "stud4", "DMET", "9", "1.2"};
		insert("student", r4);
		String[] r5 = {"5", "stud5", "BI", "4", "3.5"};
		insert("student", r5);
		System.out.println("Output of selecting the whole table content:");
		ArrayList<String[]> result1 = select("student");
		for (String[] array : result1) {
			for (String str : array) {
				System.out.print(str + " ");
			}
			System.out.println();
		}
		System.out.println("--------------------------------");
		System.out.println("Output of selecting the output by position:");
		ArrayList<String[]> result2 = select("student", 1, 1);
		for (String[] array : result2) {
			for (String str : array) {
				System.out.print(str + " ");
			}
			System.out.println();
		}
		System.out.println("--------------------------------");
		System.out.println("Output of selecting the output by column condition:");
		ArrayList<String[]> result3 = select("student", new String[]{"gpa"}, new
				String[]{"1.2"});
		for (String[] array : result3) {
			for (String str : array) {
				System.out.print(str + " ");
			}
			System.out.println();
		}
		System.out.println("--------------------------------");
		System.out.println("Full Trace of the table:");
		System.out.println(getFullTrace("student"));
		System.out.println("--------------------------------");
		System.out.println("Last Trace of the table:");
		System.out.println(getLastTrace("student"));
		System.out.println("--------------------------------");
		System.out.println("The trace of the Tables Folder:");
		System.out.println(FileManager.trace());
		FileManager.reset();
		System.out.println("--------------------------------");
		System.out.println("The trace of the Tables Folder after resetting:");
		System.out.println(FileManager.trace());
	}

}
