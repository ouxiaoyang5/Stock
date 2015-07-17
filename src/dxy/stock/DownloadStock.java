package dxy.stock;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class DownloadStock implements Runnable {
	private String stocknum;
	private String addString;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DownloadStock dl = new DownloadStock();
		Thread thread = new Thread(dl);
		thread.start();
	}

	public DownloadStock() {
		this.stocknum = "000705";
	}

	public DownloadStock(String stocknum) {
		super();
		this.stocknum = stocknum;
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
		URLConnection uc = connectToURL();
		downloadFile(uc);
//			if(uc.getContentLength()<1){
//				System.out.println("file length not right");
//			}else{
//				downloadFile(uc);
//			}
			
		
		
	}

	public void downloadFile(URLConnection uc) {
		// InputStream is=null;
		// int sumtry = 0;
		// try (InputStream tryread = uc.getInputStream()) {
		// byte[] buffer = new byte[1024];
		// while (tryread.read(buffer) != -1) {
		// sumtry++;
		// }
		// } catch (IOException e1) {
		// // TODO Auto-generated catch block
		// System.out.println("IO exception");
		// }
		// if (sumtry > 3) {
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
		// }

	}

	public URLConnection connectToURL() {
		//addString="0";
		addString="0";
		String tryString="http://quotes.money.163.com/"+addString+stocknum+".html";
		try {
			URLConnection tryConnection=new URL(tryString).openConnection();
			String result=tryConnection.getHeaderField(0);
			//System.out.println(result);
			//System.out.println(result.charAt(9));
			if(result==null||result.charAt(9)=='4'){
				addString="1";
			}
			tryString="http://quotes.money.163.com/"+addString+stocknum+".html";
			tryConnection=new URL(tryString).openConnection();
			result=tryConnection.getHeaderField(0);
			//System.out.println(result);
			if(result==null || result.charAt(9)=='4'){
				System.out.println(tryString);
				System.out.println("url not right");
			}
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		String str = "http://quotes.money.163.com/service/chddata.html?code="
				+ addString
				+ stocknum
				+ "&start=19901219&end=20150716&fields=TCLOSE;HIGH;LOW;TOPEN;LCLOSE;CHG;PCHG;VOTURNOVER;VATURNOVER";
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
			System.out.println("connect error");
			System.exit(0);
		}
		//System.out.println(uc.getContentLength());
		//System.out.println(uc.getDate());
		//System.out.println(uc.getHeaderField(0));
//		Map<String, List<String>> m=uc.getHeaderFields();
//	
//			 for (String key : m.keySet()) {   
//		            System.out.println("key= " + key + "  and  value= " + m.get(key));   
//		        }   
		//System.out.println(uc.getContentLength());
		//System.out.println();
		return uc;
	}

}
