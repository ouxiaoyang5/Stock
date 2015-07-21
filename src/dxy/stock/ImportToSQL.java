package dxy.stock;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImportToSQL {
	//private static ArrayList<String> stockList=new ArrayList<String>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		connectSql();
	}
	
	public static void updateDataBase(Connection connection) throws SQLException {
		try(Statement st=connection.createStatement();
				ResultSet rs=st.executeQuery("show tables;")){
				if(rs.next()){
					//System.out.println(rs.getString(1));
//					GetAllStocks.multiGetList(stockList);
//					System.out.println(stockList.size());
					File[] files=new File("E:\\stockcsv").listFiles();
					String filename;
					String tablename;
					for(File f:files){
						filename=f.getName();
						tablename=filename.substring(0, filename.indexOf("."));
						//System.out.println(tablename);
//						Thread t=new Thread(new MultiUpdate(tablename,connection));
//						t.start();
//						break;
						URLConnection uc=connectToURL(tablename);
						try(BufferedReader br=new BufferedReader(new InputStreamReader(uc.getInputStream(),"GB2312"));
								ResultSet rsDate=st.executeQuery("select date from stock"+tablename+" order by date desc limit 1;");
								PreparedStatement ps=connection.prepareStatement("insert into stock"+tablename+" values(?,?,?,?,?,?,?,?,?,?,?,?);")){
							SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
							br.readLine();
							String str=br.readLine();
							//System.out.println(str);
							String[] strs=str.split(",");
							if(rsDate.next()){
								//System.out.println(rsDate.getString(1));
								Date sqlDate = null,urlDate = null;
								try {
									sqlDate=sdf.parse(rsDate.getString(1));
									urlDate=sdf.parse(str);
									while(sqlDate.before(urlDate)){
										for(int i=0;i<strs.length;i++){
											if(i==3){
												ps.setFloat(i+1, Float.parseFloat(strs[i]));
											}else{
												ps.setString(i+1, strs[i]);
											}
										}
										str=br.readLine();
										strs=str.split(",");
										urlDate=sdf.parse(str);
										ps.addBatch();
										//System.out.println(ps);
									}
									ps.executeBatch();
									connection.commit();
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
							}
							
							
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//break;
					}
				}else{
					importDataBase(connection);
				}
		}
	}
	
	public static void importDataBase(Connection connection) throws SQLException {
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
			updateDataBase(connection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		}
	}
	
	public static URLConnection connectToURL(String tablenum) {		
		
		String str = "http://quotes.money.163.com/service/chddata.html?code="
				+ tablenum
				+ "&start=19901219&end=30150716&fields=TCLOSE;HIGH;LOW;TOPEN;LCLOSE;CHG;PCHG;VOTURNOVER;VATURNOVER";
		//System.out.println(str);
		
		URL url = null;
		try {
			url = new URL(str);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			System.out.println("URL not right");
			System.exit(0);
		}
		URLConnection uc = null;
		int reconnecttimes = 0;
		while (uc == null && reconnecttimes < 10) {
			reconnecttimes++;
			try {
				uc = url.openConnection();
//				uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
//				uc.setRequestProperty("Accept", "text/html, application/xhtml+xml, */*");
//				uc.setRequestProperty("Accept-Language", "zh-CN");
//				uc.setRequestProperty("Accept-Encoding", "gzip, deflate");
//				uc.setRequestProperty("Host", "quotes.money.163.com");
//				uc.setRequestProperty("Connection", "Connection");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("network error, reconnect.");
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					System.out.println("Thread sleep interrupted");
				}
			}
		}
		if (uc == null) {
			System.out.println("can not connect network");
			System.exit(0);
		}
		try {
			uc.connect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		return uc;
}
}

class MultiUpdate implements Runnable{
	private String tablenum;
	private Connection connection;
	public MultiUpdate(String tablenum,Connection conn) {
		super();
		this.tablenum = tablenum;
		connection=conn;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			updataTable();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updataTable() throws SQLException{
		URLConnection uc=connectToURL();
		try(BufferedReader br=new BufferedReader(new InputStreamReader(uc.getInputStream(),"GB2312"));
				PreparedStatement ps=connection.prepareStatement("select * from stock"+tablenum+";");){
			br.readLine();
			String str=br.readLine();
			System.out.println(str);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public URLConnection connectToURL() {		
		
			String str = "http://quotes.money.163.com/service/chddata.html?code="
					+ tablenum
					+ "&start=19901219&end=30150716&fields=TCLOSE;HIGH;LOW;TOPEN;LCLOSE;CHG;PCHG;VOTURNOVER;VATURNOVER";
			//System.out.println(str);
			
			URL url = null;
			try {
				url = new URL(str);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				System.out.println("URL not right");
				System.exit(0);
			}
			URLConnection uc = null;
			int reconnecttimes = 0;
			while (uc == null && reconnecttimes < 10) {
				reconnecttimes++;
				try {
					uc = url.openConnection();
//					uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
//					uc.setRequestProperty("Accept", "text/html, application/xhtml+xml, */*");
//					uc.setRequestProperty("Accept-Language", "zh-CN");
//					uc.setRequestProperty("Accept-Encoding", "gzip, deflate");
//					uc.setRequestProperty("Host", "quotes.money.163.com");
//					uc.setRequestProperty("Connection", "Connection");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("network error, reconnect.");
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						System.out.println("Thread sleep interrupted");
					}
				}
			}
			if (uc == null) {
				System.out.println("can not connect network");
				System.exit(0);
			}
			try {
				uc.connect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(0);
			}

//			Map<String, List<String>> m=uc.getHeaderFields();
//				 for (String key : m.keySet()) {   
//			            System.out.println("key= " + key + "  and  value= " + m.get(key));   
//			        }   
			return uc;
	}
		

}
