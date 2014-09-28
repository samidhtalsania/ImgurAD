package com.blueleaf.imguralbumdownloader;

import java.io.File;
import java.net.UnknownHostException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.blueleaf.imguralbumdownloader.R;

import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity  {
	
//	private MediaScannerConnection conn;
	
	private static final String MIME = "image/*";
	
	public static final String NOTIFICATION = "com.example.imguralbundownloader";
	public static final String RESULT = "result";
	
	public static final String PATH = "path";

	
	
	private String path ;
	
	private static String album = "album" ;
	
	private String result = DownloadManager.ACTION_DOWNLOAD_COMPLETE;
	
	private String links[];
	
	private String names[];
	
	DownloadManager manager ;
	
	private long enqueue;
	
	//	error codes
	public static final String NO_INTERNET = "NO_INTERNET";
	public static final String NO_ALBUM = "NO_ALBUM";
//	public static final String NO_ALBUM = "NO_ALBUM";

	public static final int ERROR = 404 ;

	public static final int SUCCESS = 200 ;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		
		Button viewBtn = (Button) findViewById(R.id.viewButton);
		
		viewBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				if(path == null){
					Toast.makeText(v.getContext(),"No albums to show.",Toast.LENGTH_LONG).show();
				}
				else if(path.equals("path")){
					Toast.makeText(v.getContext(),"Please wait for the download to finish...",Toast.LENGTH_LONG).show();
				}
				else{
					scanFile(path);
				}
				
			}
		});
		try{
			final Intent intent = getIntent();
			final String action = intent.getAction();
			if(Intent.ACTION_VIEW.equals(action)){
				final String intentStr = intent.getDataString();
				EditText input = (EditText) findViewById(R.id.httpAddress);
				input.setText(intentStr);
				
			}
		}
		
		catch(NullPointerException ex){
			ex.printStackTrace();
		}
		


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return false;
	}
	
	public void onClick(View view)
	{
		EditText input = (EditText) findViewById(R.id.httpAddress);
		String str = input.getText().toString();
		Button submitBtn = (Button) findViewById(R.id.submitButton);
		if(str.length() != 0 && str.contains("imgur"))
		{
			path = "path";
			Toast.makeText(this, "Downloading from "+str,Toast.LENGTH_LONG).show();
			GradientDrawable gradientDrawable = (GradientDrawable) input.getBackground();
			gradientDrawable.setStroke(3, getResources().getColor(R.color.black));
			startDownload(str);
//			Intent downloadIntent = new Intent(this,downloadIntentService.class);
//			downloadIntent.putExtra(downloadIntentService.URI, str);
			input.setEnabled(false);
			submitBtn.setEnabled(false);
//			startService(downloadIntent);
		}
		else
		{
			Toast.makeText(this, "Please enter the download link",Toast.LENGTH_LONG).show();
			GradientDrawable gradientDrawable = (GradientDrawable) input.getBackground();
			gradientDrawable.setStroke(3, getResources().getColor(R.color.red));
			
		}
	}
	



	private void startDownload(String str) {			
			
//			registerReceiver(onComplete,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
			new HttpAsyncTask().execute(str);
			
			
	}
		
	
	
	private String parseURI(String URI) {
		String temp = "https://api.imgur.com/3/album/";
		album = URI.substring(URI.lastIndexOf('/')+1);
		temp += album+".json";
		return temp ;
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
	    public void onReceive(Context context, Intent intent) {

	      if (result.equals(intent.getAction())) {
	    	  long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
	    	  Query query = new Query();
              query.setFilterById(enqueue);
              Cursor c = manager.query(query);
              if (c.moveToFirst()) {
            	  
            	  int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
            	  if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex) && enqueue == downloadId) {
            		    reEnableInputs();
	      		        Toast.makeText(MainActivity.this, "Downloading Finished.",Toast.LENGTH_LONG).show();
            	  }
              }
		    	
//		        startScan();
	      }
		  else {
			  reEnableInputs();
			  Toast.makeText(MainActivity.this, "Download failed.Please try again",
		              Toast.LENGTH_LONG).show();
		          
		  }
		}
	};
	  
	@Override
    protected void onResume() {
	    super.onResume();
	    registerReceiver(receiver, new IntentFilter(result));
	}
  
    @Override
    protected void onPause() {
	    super.onPause();
	    unregisterReceiver(receiver);
	  }
	  

    
    private void scanFile(String path) {

        MediaScannerConnection.scanFile(MainActivity.this,
                new String[] { path }, null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {
                        Intent i =  new Intent(Intent.ACTION_VIEW);
                        i.setDataAndType(uri, MIME);
                        startActivity(i);
                    }
                });
    }
    
    	
	
	private class HttpAsyncTask extends AsyncTask<String, Void, Void>{
		
		String error ;
		
		public HttpAsyncTask(){
			
		}

		@Override
		protected Void doInBackground(String... params) {
			
			String DOWNLOAD_URI = params[0] ;
			//DownloadManager downloadManager;
			
			
			//use imgur api to start downloading 
			try{
				//use a test uri as of now
				//DOWNLOAD_URI = "https://api.imgur.com/3/album/PVW92" ;
				//DOWNLOAD_URI = "https://api.imgur.com/3/album/lDRB2.json";
				//in the header add the client id
				
				DOWNLOAD_URI = parseURI(DOWNLOAD_URI) ;
				Log.d("DOWNLOAD_URI",DOWNLOAD_URI);
				
		        HttpClient httpClient = new DefaultHttpClient();
		        HttpContext localContext = new BasicHttpContext();
		        HttpGet httpGet = new HttpGet(DOWNLOAD_URI);
		        
		        httpGet.setHeader("Authorization", "Client-ID d6ba5383ebed3de");
				
		        HttpResponse response = httpClient.execute(httpGet,localContext);

		        String response_string = EntityUtils.toString(response.getEntity());
		        
		        
		        if(response_string != null){
//		        	Log.d("JSON", response_string);
					
					//receive the json file DONE
			        JSONObject json = new JSONObject(response_string);
			     
//			        Log.d("JSON", json.toString());

					
					//parse the json file DONE
			        JSONObject jsonObject = json.getJSONObject("data");
			        int jsonStatus = json.getInt("status");
			        Log.d("status",String.valueOf(jsonStatus));
			        if(jsonStatus == ERROR){
			        	error = NO_ALBUM ;
			        }
			        else if(jsonStatus == SUCCESS){
			        	JSONArray jsonArray = jsonObject.getJSONArray("images");
				        links = new String[jsonArray.length()];
				        names = new String[jsonArray.length()];
				        
				        for( int i=0;i<jsonArray.length();i++)
				        {
				        	JSONObject jsonTemp = jsonArray.getJSONObject(i);
				        	names[i] = jsonTemp.getString("id");
				        	links[i] = jsonTemp.getString("link");
				        	Log.d("link",links[i]);

				        }
				        
				        
//				        String album = DOWNLOAD_URI.substring(DOWNLOAD_URI.lastIndexOf('/')+1);
				        File dirPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+album);
				       
						Log.d("dir", "directory created at "+dirPath);

			        }
			        
		        }
		        else{
		        	error = NO_ALBUM ;
		        }
  			}
			
			catch(UnknownHostException ex){
			
				ex.printStackTrace();
				error = NO_INTERNET ;
//				publishResults(result,path);
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void v){
//			if(error){
//				Toast.makeText(getApplicationContext(), "No album found", Toast.LENGTH_LONG).show();
//			}
			if(error == NO_INTERNET){
				Toast.makeText(getApplicationContext(), "Please connect to Internet and try again", Toast.LENGTH_LONG).show();
				reEnableInputs();
			}
			else if(error == NO_ALBUM){
				Toast.makeText(getApplicationContext(), "No Album was found at that URL", Toast.LENGTH_LONG).show();
				reEnableInputs();
			}
			else{
		
				Uri myUri ;
				Request request ;
				String downloadsDir = Environment.DIRECTORY_DOWNLOADS;
				String pubDir = downloadsDir+"/"+album ; 
				manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
				
				
				for(int i=0;i<links.length;i++)
		        {
		        	myUri = Uri.parse(links[i]);
		        	request = new Request(myUri);
		        	request.setTitle(names[i]);
		        	request.setDestinationInExternalPublicDir(pubDir,names[i]+".jpg");
		        	path = album + "/" +names[i] + ".jpg";
		        	enqueue = manager.enqueue(request);
		        	
		        }
				
			}

		}
	}
	
	private void reEnableInputs(){
		EditText input = (EditText) findViewById(R.id.httpAddress);
		input.setEnabled(true);
		Button submitBtn = (Button) findViewById(R.id.submitButton);
		submitBtn.setEnabled(true);
	}

}	
