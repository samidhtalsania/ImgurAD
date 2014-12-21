package com.blueleaf.imguralbumdownloader;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

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
import android.os.Handler;
import android.preference.PreferenceManager;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.graphics.Color;
import com.todddavies.components.progressbar.*;

public class MainActivity extends ActionBarActivity  {
	
	private static final String MIME = "image/*";
	private String downloadLocation ;
	private String path ;
	private String result = DownloadManager.ACTION_DOWNLOAD_COMPLETE;
	
	private static List<String> links;
	private static List<String> names;
	private static List<Long> ids;
	
	DownloadManager manager ;
	
	//Holds the ID of the last file that downloads.
	//Used by broadcast receiver to determine when the downloads have finished
	private long enqueue;
	
	private boolean isAsyncTaskGoingOn ;
	
	//	error codes
	public static final String NO_INTERNET = "NO_INTERNET";
	public static final String NO_ALBUM = "NO_ALBUM";
	public static final int ERROR = 404 ;
	public static final int SUCCESS = 200 ;
	
	//Constants
	private static final String DEFAULT_LOCATION = Environment.DIRECTORY_DOWNLOADS;
	private static final String DLOC = "dloc";
	private static final String NETWORK_PREF = "network_pref";
	private static final String RESULT = "result";
	private static final String PATH = "path";
	private static final String NOTIFICATION = "com.example.imguralbundownloader";
	private String album = "album" ;
	
	private static ProgressWheel wheel ;
	RelativeLayout topLevelLayout ;
	private View view;
	
	//Variables for showing download progress
	private int mInterval = 10; // 10 mili seconds
	private Handler mHandler;
	
	
	long totalSizeOfAllImages ;
	
	private Query query;
	
	// An empty immutable long array.
	
	public static final long[] EMPTY_LONG_ARRAY = new long[0];
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		
		
		enqueue = 0 ;
		isAsyncTaskGoingOn = false;
		query = new Query();
		
		links = new ArrayList<String>();
		names = new ArrayList<String>();
		ids = new ArrayList<Long>();
		
		
		
		//Initialize the layout
		setContentView(R.layout.activity_main_working);
		view = findViewById(R.id.top_layout);
		topLevelLayout = (RelativeLayout) view.findViewById(R.id.top_layout);
		topLevelLayout.setVisibility(View.GONE);
		getSupportActionBar().show();
		
		//Initialize the progress wheel
		wheel = (ProgressWheel) findViewById(R.id.pw_spinner);
		wheel.setBarColor(Color.BLUE);
		
		//initialize the download manager object
		manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
		
		registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		
		if(savedInstanceState != null)
		{
			if(savedInstanceState.getBoolean("isAsyncTaskGoingOn"))
			{
				startSpiningWheel();
				disableInputs();
			}
			if(savedInstanceState.getString("link") != null)
	    	{
	    		EditText input = (EditText) findViewById(R.id.httpAddress);
	    		input.setText(savedInstanceState.getString("link"));
	    	}
			
		}
		
		Button viewBtn = (Button) findViewById(R.id.viewButton);
		
		viewBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				if(path == null){
					SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
					scanFile(sharedPref.getString(DLOC,DEFAULT_LOCATION));
				}
				else if(wheel.isSpinning()){
					Toast.makeText(v.getContext(),Errors.NO_ALBUMS_TO_SHOW,Toast.LENGTH_LONG).show();
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
		
		//Initialize the Handler Thread that handles progress bar updation thread
		mHandler = new Handler();
		
	}
	
	//Creates the menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	
	//Menu selector
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()){
			case R.id.action_settings:
				Intent i = new Intent(this,settingsActivity.class);
				startActivity(i);
				break ;	
			 default:
		        break;
		}
		return true ;
	}

	//OnClick method that starts the download
	public void onClick(View view)
	{
		EditText input = (EditText) findViewById(R.id.httpAddress);
		String str = input.getText().toString();
		Button submitBtn = (Button) findViewById(R.id.submitButton);
		
		if(str.contains("imgur.com/a/"))
		{
			Toast.makeText(this, Errors.DOWNLOADING+str,Toast.LENGTH_SHORT).show();
			GradientDrawable gradientDrawable = (GradientDrawable) input.getBackground();
			gradientDrawable.setStroke(3, getResources().getColor(R.color.black));
			startDownload(str);
			input.setEnabled(false);
			submitBtn.setEnabled(false);
		}
		else
		{
			Toast.makeText(this, "Please enter the download link",Toast.LENGTH_LONG).show();
			GradientDrawable gradientDrawable = (GradientDrawable) input.getBackground();
			gradientDrawable.setStroke(3, getResources().getColor(R.color.red));
			
		}
	}
	
	
	//Starts the async task that will further start the download
	private void startDownload(String str) {			
		startSpiningWheel();
		wheel.setText("0%");
		new HttpAsyncTask(this).execute(str);
	}
		
	//Formats the input string so that the API call can  be made
	private String parseURI(String URI) {
		String temp = "https://api.imgur.com/3/album/";
		album = URI.substring(URI.lastIndexOf('/')+1);
		temp += album+".json";
		return temp ;
	}

	
	//Broadcast receiver that receives the intent when all downloads have finished.
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
            		  	stopProbingDownloads();
            		    reEnableInputs();
            		    stopSpinningWheel();
	      		        Toast.makeText(MainActivity.this, "Downloading Finished.",Toast.LENGTH_LONG).show();
            	  }
              }
              if( c != null && !c.isClosed() )
			        c.close();
	      }
		  else {
			  reEnableInputs();
			  stopSpinningWheel();
			  Toast.makeText(MainActivity.this, "Download failed.Please try again",
		              Toast.LENGTH_LONG).show();
		          
		  }
		}
	};
	
	//Activity Life Cycle method
	//When the activity resumes it re registers the broadcast receiver
	//When it resumes it also checks whether there are ongoing downloads. In case there are downloads going on it 
	//keeps the progress wheel going on and keeps the inputs disabled. If the downloads have finished it re enables
	//inputs and stops the spinning of the progress wheel.
	  
	
	@Override
    protected void onResume() {
		super.onResume();
	    registerReceiver(receiver, new IntentFilter(result));
	    if(enqueue != 0 )
	    {
	    	
	    	Query q = new Query();
	    	q.setFilterById(enqueue);
	    	DownloadManager dm = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
			Cursor c = dm.query(q);
	    	int downloadSatus = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
	    	while(c.moveToNext())
	    	{
	    		if(c.getInt(downloadSatus) == DownloadManager.STATUS_SUCCESSFUL)
	    		{
	    			if(wheel.isSpinning())
	    			{
	    				stopSpinningWheel();
	    				
	    			}
	    			reEnableInputs();
	    		}
	    		else
	    		{
	    			startSpiningWheel();
	    			disableInputs();
	    			startProbingDownloads();
	    		}
	    	}
	    	if( c != null && !c.isClosed() )
		        c.close();
	    }
	    
	    if(isAsyncTaskGoingOn)
	    {
			startSpiningWheel();
	    }
	    
	}
  
	
	//Activity Life Cycle Method
	//When the activity pauses(loses focus) it de registers the broadcast receiver
    @Override
    protected void onPause() {
    	super.onPause();
    	unregisterReceiver(receiver);
	    if(isAsyncTaskGoingOn)
	    {
	    	stopSpinningWheel();
	    }
	    
	  }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
    	super.onSaveInstanceState(savedInstanceState);
    	savedInstanceState.putBoolean("isAsyncTaskGoingOn", isAsyncTaskGoingOn);
    	EditText input = (EditText) findViewById(R.id.httpAddress);
    	savedInstanceState.putString("link", input.getText().toString());
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
    	super.onRestoreInstanceState(savedInstanceState);
    	if(savedInstanceState.getBoolean("isAsyncTaskGoingOn"))
		{
			startSpiningWheel();
			disableInputs();
		}
    	if(savedInstanceState.getString("link") != null)
    	{
    		EditText input = (EditText) findViewById(R.id.httpAddress);
    		input.setText(savedInstanceState.getString("link"));
    	}
    }
	
    //Method that allows the OS to Scan Downloaded files.
    //Enables the functionality of View Albums button
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
    
    @SuppressWarnings("unused")
    
    //Core Method
    //First connects to Imgur API to get the links
    //Once it gets the links it gives it to the down load manager for downloading
    
    private class HttpAsyncTask extends AsyncTask<String, Void, Void>{
		
		String error ;
		
		Context mContext ;
		
		public HttpAsyncTask(Context mContext){
			this.mContext = mContext ;
		}
		@Override
		protected void onPreExecute ()
		{	
			isAsyncTaskGoingOn = true;
		}

		@Override
		protected Void doInBackground(String... params) {
			
			String DOWNLOAD_URI = params[0] ;
			//use imgur api to start downloading 
			try{
				//in the header add the client id
				
				DOWNLOAD_URI = parseURI(DOWNLOAD_URI) ;
				
		        HttpClient httpClient = new DefaultHttpClient();
		        HttpContext localContext = new BasicHttpContext();
		        HttpGet httpGet = new HttpGet(DOWNLOAD_URI);
		        
		        httpGet.setHeader("Authorization", "Client-ID d6ba5383ebed3de");
				
		        HttpResponse response = httpClient.execute(httpGet,localContext);

		        String response_string = EntityUtils.toString(response.getEntity());
		        
		        
		        if(response_string != null){
					
					//receive the json file DONE
			        JSONObject json = new JSONObject(response_string);
					
					//parse the json file DONE
			        JSONObject jsonObject = json.getJSONObject("data");
			        int jsonStatus = json.getInt("status");
			        Log.d("status",String.valueOf(jsonStatus));
			        if(jsonStatus == ERROR){
			        	error = NO_ALBUM ;
			        }
			        else if(jsonStatus == SUCCESS){
			        	JSONArray jsonArray = jsonObject.getJSONArray("images");
			        	totalSizeOfAllImages  = 0 ;
				        names.clear();
				        links.clear();
				        ids.clear();
				        
				        JSONObject jsonTemp; 
				        
				        for( int i=0;i<jsonArray.length();i++)
				        {
				        	jsonTemp = jsonArray.getJSONObject(i);
				        	names.add(jsonTemp.getString("id"));
				        	links.add(jsonTemp.getString("link"));
				        	totalSizeOfAllImages  += jsonTemp.getLong("size") ;
				        }
				        
				    	
						Uri myUri ;
						Request request ;
						SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
						downloadLocation = sharedPref.getString(DLOC,DEFAULT_LOCATION);
						String pubDir = downloadLocation+"/"+album ;
						String formatStr = Environment.getExternalStorageDirectory().getAbsolutePath() ;
						pubDir = pubDir.replace(formatStr, "");
						
						Log.d("loc",pubDir);
						boolean OnlyWifi = false;
						if(sharedPref.getBoolean(NETWORK_PREF, true))
						{
							OnlyWifi = true;
						}
						
						for(int i=0;i<links.size();i++)
				        {
				        	myUri = Uri.parse(links.get(i));
				        	request = new Request(myUri);
				        	path = names.get(i) + ".jpg";
				        	request.setTitle(names.get(i));
				        	request.setDestinationInExternalPublicDir(pubDir,names.get(i)+".jpg");
				        	request.setVisibleInDownloadsUi(false);
				        	if(OnlyWifi)
				        	{
				        		request.setAllowedNetworkTypes(Request.NETWORK_WIFI);
				        	}
				        	else
				        	{
				        		request.setAllowedNetworkTypes(Request.NETWORK_WIFI | Request.NETWORK_MOBILE);
				        	}
				        	
				        	enqueue = manager.enqueue(request);
				        	ids.add(enqueue);
				        }
						long[] longIDs = toPrimitive(ids.toArray(new Long[ids.size()]));
			        	query.setFilterById(longIDs);
						
			        }
			    }
		        else{
		        	error = NO_ALBUM ;
		        }
  			}
			
			catch(UnknownHostException ex){
			
				ex.printStackTrace();
				error = NO_INTERNET ;
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void v){
			isAsyncTaskGoingOn = false;
			if(error == NO_INTERNET){
				Toast.makeText(getApplicationContext(), Errors.NO_INTERNET, Toast.LENGTH_LONG).show();
				reEnableInputs();
				stopSpinningWheel();
				isAsyncTaskGoingOn = false;
				
			}
			else if(error == NO_ALBUM){
				Toast.makeText(getApplicationContext(), Errors.NO_ALBUM, Toast.LENGTH_LONG).show();
				reEnableInputs();
				stopSpinningWheel();
				isAsyncTaskGoingOn = false;
			}
			else{
				startProbingDownloads();
			}
		}
	}
	
    //The thread code which handles probing download manager to check  how much of the download has been finished
    //The probe handles at an interval defined by mInterval variables
    //Basically starts a new async thread (CalculateProgressAsyncTask) after every mInterval interval 
    Runnable downloadStatusChecker = new Runnable(){
    	@Override
		public void run() {
    		new CalculateProgressAsyncTask().execute();
			mHandler.postDelayed(downloadStatusChecker, mInterval );
		}
    
    } ;
    
    //Async task that handles probing downloads 
    private class CalculateProgressAsyncTask extends AsyncTask<Void, Void, Long>{

		@Override
		protected Long doInBackground(Void... arg0) {
			long progress = 0;
			Cursor c = null ;
			try {
				c = manager.query(query);
				long bytesDownloaded = 0;
				
				while(c.moveToNext())
				{
					int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
					bytesDownloaded += c.getLong(columnIndex);
					
					progress = bytesDownloaded*100/totalSizeOfAllImages;
				}
				return progress;
			} catch (Exception e) {
				
				e.printStackTrace();
				
			}
			finally{
				if( c != null && !c.isClosed() )
			        c.close();
			}
			return progress;
			
		}
		
		@Override
		protected void onPostExecute(Long progress)
		{
			wheel.setText(String.valueOf(progress)+"%");
		}
    }
    
    
    //This Method starts the thread that calculates the perentage downloads
    void startProbingDownloads(){
    	downloadStatusChecker.run();
    }
    
    //This method stops the thread that  calculates the perentage downloads
    void stopProbingDownloads(){
    	mHandler.removeCallbacks(downloadStatusChecker);
    }
    
    //ReEnables inputs 
	private void reEnableInputs(){
		EditText input = (EditText) findViewById(R.id.httpAddress);
		input.setEnabled(true);
		Button submitBtn = (Button) findViewById(R.id.submitButton);
		submitBtn.setEnabled(true);
	}
	
    //Disables inputs 
	private void disableInputs(){
		EditText input = (EditText) findViewById(R.id.httpAddress);
		input.setEnabled(false);
		Button submitBtn = (Button) findViewById(R.id.submitButton);
		submitBtn.setEnabled(false);
	}
	
	//Helper method to start spinning wheel
	private void startSpiningWheel()
	{
		wheel.spin();
		topLevelLayout = (RelativeLayout) view.findViewById(R.id.top_layout);
		topLevelLayout.setVisibility(View.VISIBLE);
	}
	
	//Helper method to stop spinning wheel
	private void stopSpinningWheel()
	{
		wheel.stopSpinning();
		topLevelLayout = (RelativeLayout) view.findViewById(R.id.top_layout);
		topLevelLayout.setVisibility(View.INVISIBLE);
	}
	
	
	//Converts an array of object Long to primitives handling 
	public static long[] toPrimitive(final Long[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_LONG_ARRAY;
        }
        final long[] result = new long[array.length];
        for (int i = 0; i < array.length; i++) {
        	result[i] = array[i].longValue();
        }
        return result;
	}
}	
