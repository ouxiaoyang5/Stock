package dxy.stock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class GetAllStocks {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		String stocknum="184728";
//		DownloadStock dl=new DownloadStock(stocknum);
//		Thread thread=new Thread(dl);
//		thread.start();
		long startTime=System.currentTimeMillis(); 
		multiDownload();
		long endTime=System.currentTimeMillis();
		System.out.println(endTime-startTime);
	}
	
	public static ArrayList<String> getStockList(){
		//connect
		URLConnection listConn = null;
		try {
			URL url=new URL("http://bbs.10jqka.com.cn/codelist.html");
			listConn=url.openConnection();
			listConn.connect();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			System.out.println("list url not right");
			System.exit(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("IO Error");
			System.exit(0);
		}
		ArrayList<String> stockList=new ArrayList<String>();
		//read source code
		try(InputStream is=listConn.getInputStream();
			BufferedReader br=new BufferedReader(new InputStreamReader(is))) {
			
			String str;
			String sub="  <li><a href=\"http://bbs.10jqka.com.cn";
			String[] splitStr;
			//String regx=".*<li><a href=.*(sh|sz).*target.*";
			int count=0;
			//long startTime=System.currentTimeMillis();
			while((str=br.readLine())!=null){
				count++;
				if(count>150){
					if(str.contains("http://bbs.10jqka.com.cn/fu")){
						break;
					}
					if(str.contains(sub)){//str.matches(regx)
						//System.out.println(str);
						splitStr=str.split(",");
						//System.out.println(splitStr[1]);
						stockList.add(splitStr[1]);
						//System.out.println(count);
					}
				}								
			}
			//long endTime=System.currentTimeMillis();
			//System.out.println(endTime-startTime);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("IO error");
		}
		return stockList;
	}
	
	public static void multiDownload(){
		ArrayList<String> stockList=getStockList();
		Thread thread;
		for(String stocknum:stockList){
			DownloadStock dl=new DownloadStock(stocknum);
			thread=new Thread(dl);
			thread.start();
			System.out.println(Thread.activeCount());
			while(Thread.activeCount()>4){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
