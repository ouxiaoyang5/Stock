package dxy.stock;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DownloadStock implements Runnable {
	private String stocknum;
	private String operation;
	private ArrayList<String> stockList;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DownloadStock dl = new DownloadStock("000001");
		Thread thread = new Thread(dl);
		thread.start();
	}

	public DownloadStock(String stocknum) {
		this.stocknum = stocknum;
	}

	public DownloadStock(String stocknum,String op) {
		super();
		this.stocknum = stocknum;
		operation=op;
	}
	
	public DownloadStock(String stocknum,String op,ArrayList<String> sList) {
		super();
		this.stocknum = stocknum;
		operation=op;
		stockList=sList;
	}
	
	public String getStocknum() {
		return stocknum;
	}

	public void setStocknum(String stocknum) {
		this.stocknum = stocknum;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		if(operation.equalsIgnoreCase("intoFile")){
			URLConnection uc = connectToURL("0");
			if(uc!=null){
				updateFile(uc,"0");
			}
			uc=connectToURL("1");
			if(uc!=null){
				updateFile(uc,"1");
			}
		}else if(operation.equalsIgnoreCase("intoSql")){
			if(isRight("0")){
				//System.out.println("0"+stocknum);
				stockList.add("0"+stocknum);
			}
			if(isRight("1")){
				//System.out.println("1"+stocknum);
				stockList.add("1"+stocknum);
			}
		}
		
	}

	public void updateFile(URLConnection uc,String addString){
		try (BufferedReader br=new BufferedReader(new InputStreamReader(uc.getInputStream(),"GB2312"));
				InputStream is=uc.getInputStream();
				RandomAccessFile ra=new RandomAccessFile("E:\\stockcsv\\"
						+ addString+stocknum + ".csv", "rw");
				FileOutputStream fo = new FileOutputStream("E:\\stockcsv\\"
						+ addString+stocknum + "_temp.csv")
						) {
			if(ra.length()<100){
				downloadFile(uc, addString);
				return;
			}else {
				ra.seek(85);
				byte[] buffer=new byte[10];
				ra.read(buffer);
				String fileTimeStr=new String(buffer,"GB2312");
				String str=br.readLine()+"\r\n";
				fo.write(str.getBytes("GB2312"));
				str=br.readLine()+"\r\n";				
				String urlTimeStr=str.substring(0, 10);
				SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
				Date fileDate=sdf.parse(fileTimeStr);
				Date urlDate=sdf.parse(urlTimeStr);

				while(fileDate.before(urlDate)){
					fo.write(str.getBytes("GB2312"));
					str=br.readLine()+"\r\n";
					urlTimeStr=str.substring(0, 10);
					urlDate=sdf.parse(urlTimeStr);
				}
				fo.write((str).getBytes("GB2312"));
				while((str=br.readLine())!=null){
					fo.write((str+"\r\n").getBytes("GB2312"));
				}


				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new File("E:\\stockcsv\\"
				+ addString+stocknum + ".csv").delete();
		new File("E:\\stockcsv\\"
				+ addString+stocknum + "_temp.csv").renameTo(new File("E:\\stockcsv\\"
						+ addString+stocknum + ".csv"));
		System.out.println(addString+stocknum);
	}
	public void downloadFile(URLConnection uc,String addString) {
		try (InputStream is = uc.getInputStream();
				FileOutputStream fo = new FileOutputStream("E:\\stockcsv\\"
						+ addString+stocknum + ".csv")) {
			byte[] buffer = new byte[1024];
			//int sum = 0;
			int byteread = 0;
			while ((byteread = is.read(buffer)) != -1) {
				fo.write(buffer, 0, byteread);
				//sum++;
				//System.out.println(sum);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println(addString+stocknum);
	}
	
	public boolean isRight(String addString){
		boolean urlRight=false;
		String tryString="http://quotes.money.163.com/"+addString+stocknum+".html";
		try {
			URLConnection tryConnection=new URL(tryString).openConnection();
			String result=tryConnection.getHeaderField(0);
			//System.out.println(result);
			if(result==null||result.charAt(9)=='4'){
				//addString="1";
				urlRight=false;
			}else {
				urlRight=true;
			}
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		return urlRight;
	}
	
	public URLConnection connectToURL(String addString) {		
		if(isRight(addString)){
			String str = "http://quotes.money.163.com/service/chddata.html?code="
					+ addString
					+ stocknum
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
		}else {
			return null;
		}
		
	}

}
