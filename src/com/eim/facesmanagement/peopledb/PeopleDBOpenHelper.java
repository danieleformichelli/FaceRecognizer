package com.eim.facesmanagement.peopledb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class PeopleDBOpenHelper extends SQLiteOpenHelper {
	
	private static final int VERSION = 1;

	private static final String DB_CREATE_PEOPLE = "CREATE TABLE " 
			 + FacesContract.People.TABLE + "(" 
			 + FacesContract.People._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
			 + FacesContract.People.NAME + " TEXT UNIQUE NOT NULL" 
			 +");";
	
	private static final String DB_CREATE_FACES = "CREATE TABLE " 
			 + FacesContract.Faces.TABLE + " (" 
			 + FacesContract.Faces._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
			 + FacesContract.Faces.PERSON_ID + " TEXT UNIQUE NOT NULL, "
			 + FacesContract.Faces.PHOTO_URL + " TEXT UNIQUE NOT NULL, "
			 + "FOREIGN KEY(" + FacesContract.Faces.PERSON_ID + ") REFERENCES " 
			 + FacesContract.People.TABLE + "(" + FacesContract.People._ID + ")"
			 + " ON DELETE CASCADE);";

	// TODO create indexes
	
	public PeopleDBOpenHelper(Context context) {
		super(context, FacesContract.DB_NAME, null, VERSION);
	}
	
	public PeopleDBOpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
	}


	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DB_CREATE_PEOPLE);
		db.execSQL(DB_CREATE_FACES);
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXIST " + FacesContract.Faces.TABLE);
		db.execSQL("DROP TABLE IF EXIST " + FacesContract.People.TABLE);
		onCreate(db);
	}
	
}
