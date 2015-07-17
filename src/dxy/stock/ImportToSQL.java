package dxy.stock;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ImportToSQL {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		connectSql();
	}
	
	
	public static void connectSql() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			System.out.println("class not found");
			System.exit(0);
		}

		try (Connection connection=DriverManager.getConnection("jdbc:mysql://localhost:3306/stock", "root", "0523")){
			connection.setAutoCommit(false);
			File[] files=new File("E:\\stockcsv").listFiles();
			String filename;
			String tablename;
			for(File f:files){
				filename=f.getName();
				tablename=filename.substring(0, filename.indexOf("."));
				System.out.println(tablename);
				String cresql="create table if not exists stock"+tablename+" ("
						+"date varchar(10),code varchar(10),name varchar(10),price float(10),max varchar(10),min varchar(10),begin varchar(10),last varchar(10),pricechange varchar(10),per varchar(10),volume varchar(20),fund varchar(20)"
						+ ");";
				String addsql="LOAD DATA INFILE '"+f.getPath().replace('\\', '/')+"' "
						+ "REPLACE INTO TABLE stock"+tablename+" "
						+ "CHARACTER SET gb2312 "
						+ "FIELDS TERMINATED BY ',' ENCLOSED BY '' "
						+ "LINES TERMINATED BY '\r\n' "
						+ "ignore 1 lines;";
				try(Statement ps=connection.createStatement()){
					//System.out.println(addsql);
					ps.addBatch(cresql);
					ps.addBatch(addsql);
					
					//System.out.println(ps);
					ps.executeBatch();
				}
				connection.commit();
				//break;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		}
	}
}
