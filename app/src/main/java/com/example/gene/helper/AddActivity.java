package com.example.gene.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class AddActivity extends Activity implements OnClickListener {

	private Button btn_save;
	private EditText edit_first, edit_last, edit_phone;
	private DatabaseManager dbManager;
	private String id, fname, lname, phone;
	private boolean isUpdate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add);

		btn_save = (Button)findViewById(R.id.save_btn);
		edit_first = (EditText)findViewById(R.id.frst_editTxt);
		edit_last = (EditText)findViewById(R.id.last_editTxt);
		edit_phone = (EditText)findViewById(R.id.phone_editTxt);

		isUpdate = getIntent().getExtras().getBoolean("update");
		if(isUpdate)
		{
			id = getIntent().getExtras().getString("id");
			fname = getIntent().getExtras().getString("fname");
			lname = getIntent().getExtras().getString("lname");
			phone = getIntent().getExtras().getString("phone");
			
			edit_first.setText(fname);
			edit_last.setText(lname);
			edit_phone.setText(phone);
		}
		
		btn_save.setOnClickListener(this);
		dbManager = new DatabaseManager(this);
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		fname = edit_first.getText().toString().trim();
		lname = edit_last.getText().toString().trim();
		phone = edit_phone.getText().toString().trim();
		if(fname.length()>0 && lname.length()>0 && phone.length()>0) {
			saveData();
		}
		else {
			AlertDialog.Builder alertBuilder = new AlertDialog.Builder(AddActivity.this);
			alertBuilder.setMessage(R.string.settings_j2);
			alertBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();

				}
			});
			alertBuilder.create().show();
		}
	}

	private void saveData(){
		SQLiteDatabase db = dbManager.getWritableDatabase();
		ContentValues values = new ContentValues();

		values.put("imie",fname);
		values.put("nazwisko",lname);
		values.put("telefon",phone);
		
		if(isUpdate) {    
			// Update database with new data 
			db.update("telefony", values, "nr="+id, null);
		}
		else {
			// Insert data into database
			db.insertOrThrow("telefony", null, values);
		}
		
		// Close database
		db.close();
		finish();
	}
}
