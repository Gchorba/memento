package com.example.gene.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class SettingsActivity extends Activity {
	private DatabaseManager dbManager;
	private SQLiteDatabase db;
	private TextView range_textview, address_textview;
	private String range, address;
	private Button btn_range ;
	private ImageButton btn_english;
	private Context context = SettingsActivity.this;
	Locale myLocale;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		range_textview = (TextView) findViewById(R.id.settings_textView2);
		address_textview = (TextView) findViewById(R.id.settings_textView4);
		btn_range = (Button) findViewById(R.id.settings_button1);
		btn_english = (ImageButton) findViewById(R.id.imageButton2);

		
		readDB();

		btn_range.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dbManager = new DatabaseManager(context);
				db = dbManager.getWritableDatabase();

				range = range_textview.getText().toString().trim();
				address = address_textview.getText().toString().trim();
				if(range.length()>0 && address.length()>0 ) {
					ContentValues values = new ContentValues();
					values.put("range",range);
					values.put("address",address);
					db.update("startpoint", values, "nr=1", null);
					Toast.makeText(getApplicationContext(), R.string.settings_j1, Toast.LENGTH_LONG).show();
					finish();
				}
				else {
					AlertDialog.Builder alertBuilder = new AlertDialog.Builder(SettingsActivity.this);
					alertBuilder.setMessage(R.string.settings_j2);
					alertBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
					alertBuilder.create().show();
				}	
				db.close();
			}			
		});
		
		btn_english.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				    myLocale = new Locale("en");
				    Resources res = getResources();
				    DisplayMetrics dm = res.getDisplayMetrics();
				    Configuration conf = res.getConfiguration();
				    conf.locale = myLocale; 
				    res.updateConfiguration(conf, dm); 
				    Intent refresh = new Intent(context, MainActivity.class);
				    startActivity(refresh); 
				    Toast.makeText(getApplicationContext(), R.string.settings_j1, Toast.LENGTH_SHORT).show();
			}
		});
		


	}

	public void readDB(){
		dbManager = new DatabaseManager(this);
		db = dbManager.getWritableDatabase();

		String[] kolumny = {"address","range"};
		Cursor cursor = db.query("startpoint", kolumny, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			address = cursor.getString(0);
			range = cursor.getLong(1)+"";			
		}		
		range_textview.setText(range);
		address_textview.setText(address);

		cursor.close();
		db.close();
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
