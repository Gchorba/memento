package com.example.gene.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseManager extends SQLiteOpenHelper {

	public DatabaseManager(Context context){
		super(context,"kontakty.db",null,1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table telefony(" +
				"nr integer primary key autoincrement," +
				"imie text," +
				"nazwisko text," +
				"telefon text);" +
				"");
		db.execSQL("create table startpoint(" +
				"nr integer primary key," +
				"lan real," +
				"lon real," +
				"address text," + 
				"range integer);" +
				"");
		db.execSQL("INSERT INTO startpoint values (1, 0, 0,'',0)");
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS telefony");
		db.execSQL("DROP TABLE IF EXISTS startpoint");
		onCreate(db);	
	}
	
}