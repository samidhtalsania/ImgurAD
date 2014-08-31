package com.example.imguralbumdownloader;

import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.GradientDrawable;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity  implements MediaScannerConnectionClient{
	
	private MediaScannerConnection conn;
	
	private static final String MIME = "image/*";
	
	private String PATH ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);


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
		if(str.length() != 0)
		{
			Toast.makeText(this, "Downloading from "+str,Toast.LENGTH_LONG).show();
			GradientDrawable gradientDrawable = (GradientDrawable) input.getBackground();
			gradientDrawable.setStroke(3, getResources().getColor(R.color.black));
			Intent downloadIntent = new Intent(this,downloadIntentService.class);
			downloadIntent.putExtra(downloadIntentService.URI, str);
			input.setEnabled(false);
			startService(downloadIntent);
		}
		else
		{
			Toast.makeText(this, "Please enter the download link",Toast.LENGTH_LONG).show();
			GradientDrawable gradientDrawable = (GradientDrawable) input.getBackground();
			gradientDrawable.setStroke(3, getResources().getColor(R.color.red));
			
		}
	}
	
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
	    public void onReceive(Context context, Intent intent) {
	      Bundle bundle = intent.getExtras();
	      if (bundle != null) {
	        //String string = bundle.getString(downloadIntentService.FILEPATH);
	    	EditText input = (EditText) findViewById(R.id.httpAddress);
	    	input.setEnabled(true);
	        int resultCode = bundle.getInt(downloadIntentService.RESULT);
	        if (resultCode == RESULT_OK) {
	        	Toast.makeText(MainActivity.this, "Downloading Finished.",
	  	              Toast.LENGTH_LONG).show();
	          
	        } else {
	          Toast.makeText(MainActivity.this, "Download failed.Please try again",
	              Toast.LENGTH_LONG).show();
	          
	        }
	      }
	    }
	  };
	  
	  @Override
	  protected void onResume() {
	    super.onResume();
	    registerReceiver(receiver, new IntentFilter(downloadIntentService.NOTIFICATION));
	  }
	  
	  @Override
	  protected void onPause() {
	    super.onPause();
	    unregisterReceiver(receiver);
	  }
	  
    private void startScan()
    {
        if(conn!=null)
        {
            conn.disconnect();
        }
        conn = new MediaScannerConnection(this,this);
        conn.connect();
    }

	@Override
	public void onMediaScannerConnected() {
		// TODO Auto-generated method stub
		conn.scanFile(PATH, MIME);
	}

	@Override
	public void onScanCompleted(String arg0, Uri arg1) {
		// TODO Auto-generated method stub
		
	}

}	
