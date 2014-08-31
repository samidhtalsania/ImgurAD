package com.example.imguralbumdownloader;


import java.io.File;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class downloadIntentService extends IntentService {
	
	//public static final String DOWNLOAD_URI ;
	
	private int result = Activity.RESULT_CANCELED;
	public static String URI;
	public static final String RESULT = "result";
	private static String album = "album" ;
	
	public static final String NOTIFICATION = "com.example.imguralbundownloader";
	public downloadIntentService() {
		super("downloadIntentService");
		// TODO Auto-generated constructor stub
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		
		String DOWNLOAD_URI = intent.getStringExtra(URI);
		//DownloadManager downloadManager;
		Request request ;
		
		//use imgur api to start downloading 
		try{
			//use a test uri as of now
			//DOWNLOAD_URI = "https://api.imgur.com/3/album/PVW92" ;
			//DOWNLOAD_URI = "https://api.imgur.com/3/album/lDRB2.json";
			//in the header add the client id
			DOWNLOAD_URI = parseURI(DOWNLOAD_URI) ;

	        HttpClient httpClient = new DefaultHttpClient();
	        HttpContext localContext = new BasicHttpContext();
	        HttpGet httpGet = new HttpGet(DOWNLOAD_URI);
	        
	        httpGet.setHeader("Authorization", "Client-ID d6ba5383ebed3de");
			
	        HttpResponse response = httpClient.execute(httpGet,localContext);

	        String response_string = EntityUtils.toString(response.getEntity());

	        Log.d("JSON", response_string);
			
			//receive the json file DONE
	        JSONObject json = new JSONObject(response_string);

	        Log.d("JSON", json.toString());

			
			//parse the json file DONE
	        JSONObject jsonObject = json.getJSONObject("data");
	        JSONArray jsonArray = jsonObject.getJSONArray("images");
	        String[] links = new String[jsonArray.length()];
	        String[] names = new String[jsonArray.length()];
	        
	        for( int i=0;i<jsonArray.length();i++)
	        {
	        	JSONObject jsonTemp = jsonArray.getJSONObject(i);
	        	names[i] = jsonTemp.getString("id");
	        	links[i] = jsonTemp.getString("link");
	        	Log.d("link",links[i]);
	        	
	        	//download all images from this file DONE
	        	
	        	
	        	//request.setVisibleInDownloadsUi(true);
	        	
	        	
	        }
	        
	      //TODO : destination directory for storing images DONE
	        
	        Uri myUri ;
//	        String album = DOWNLOAD_URI.substring(DOWNLOAD_URI.lastIndexOf('/')+1);
	        File dirPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+album);
	        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
			if(!dirPath.isDirectory())
			{
				dirPath.mkdir();
				Log.d("dir", "directory created");
				String pubDir = Environment.DIRECTORY_DOWNLOADS+"/"+album ;
				for(int i=0;i<links.length;i++)
		        {
		        	myUri = Uri.parse(links[i]);
		        	request = new Request(myUri);
		        	request.setTitle(names[i]);
		        	request.setDestinationInExternalPublicDir(pubDir,names[i]+".jpg");
		        	manager.enqueue(request);
		        	
		        }
				
				result = Activity.RESULT_OK;
			}
			else
			{
				Log.d("dir","failed to create directory") ;
				result = Activity.RESULT_CANCELED ;
				
			}        
	        
	    }
		
		catch(Exception ex){
		
			Log.d("error", ex.getMessage()) ;
			result = Activity.RESULT_CANCELED ;
		}
	    // successfully finished
	    
		
	    //after succesfull downloading notify MainActivity
		finally
		{
			publishResults(result);
		}
		
	}

	private String parseURI(String URI) {
		// TODO Auto-generated method stub
		String temp = "https://api.imgur.com/3/album/";
		album = URI.substring(URI.lastIndexOf('/')+1);
		temp += album+".json";
		return temp ;
	}

	private void publishResults(int result) {
		// TODO Auto-generated method stub
	    Intent intent = new Intent(NOTIFICATION);
	    
	    intent.putExtra(RESULT, result);
	    sendBroadcast(intent);
		
	}

}
