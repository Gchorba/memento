package com.example.gene.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class DisplayActivity extends Activity {

	private DatabaseManager dbManager;
	private SQLiteDatabase db;
	private ListView dbList;

	private ArrayList<String> user_id = new ArrayList<String>();
	private ArrayList<String> user_fname = new ArrayList<String>();
	private ArrayList<String> user_lname = new ArrayList<String>();
	private ArrayList<String> user_phone = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display);

		dbList = (ListView) findViewById(R.id.List);
		dbManager = new DatabaseManager(this);

		// Adding new data
		findViewById(R.id.btnAdd).setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				Intent i = new Intent(getApplicationContext(), AddActivity.class);
				i.putExtra("update", false);
				startActivity(i);
			}
		
		});

		// Click to update data
		dbList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				Intent i = new Intent(getApplicationContext(), AddActivity.class);
				i.putExtra("fname", user_fname.get(arg2));
				i.putExtra("lname", user_lname.get(arg2));
				i.putExtra("id", user_id.get(arg2));
				i.putExtra("phone", user_phone.get(arg2));
				i.putExtra("update", true);
				startActivity(i);

			}
		});

		// Long click to delete data
		dbList.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {

				AlertDialog.Builder alertBuilder = new AlertDialog.Builder(DisplayActivity.this);
				alertBuilder.setMessage("Usun�� ten kontakt ?");
				alertBuilder.setPositiveButton("Tak",new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {

						Toast.makeText(getApplicationContext(), "Usuni�to " + user_fname.get(arg2) + " " + user_lname.get(arg2), Toast.LENGTH_LONG).show();
						
						db.delete("telefony","nr="+user_id.get(arg2), null);
						displayData();
						dialog.cancel();
					}
				});

				alertBuilder.setNegativeButton("Nie", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
				alertBuilder.create().show();

				return true;
			}
		});

	}

	@Override
	protected void onResume() {
		displayData();
		super.onResume();
	}

	// Displays queries from SQLite
	private void displayData() {
		db = dbManager.getWritableDatabase();
		String[] kolumny = {"nr","imie","nazwisko","telefon"};
		Cursor cursor = db.query("telefony", kolumny, null, null, null, null, null);

		user_id.clear();
		user_fname.clear();
		user_lname.clear();
		user_phone.clear();
		if (cursor.moveToFirst()) {
			do {
				user_id.add(cursor.getLong(0)+"");
				user_fname.add(cursor.getString(1));
				user_lname.add(cursor.getString(2));
				user_phone.add(cursor.getString(3));

			} while (cursor.moveToNext());
		}

		DisplayAdapter disadpt = new DisplayAdapter(DisplayActivity.this, user_fname, user_lname);
		dbList.setAdapter(disadpt);
		cursor.close();
		db.close();
	}

}
