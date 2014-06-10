package com.eim.facesmanagement;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class PeopleDBOpenHelper extends SQLiteOpenHelper {
	
	private static final String DB_NAME = "face.db";
	private static final int VERSION = 1;

	private static final String DB_CREATE_PERSON = "create table " 
			 + FacesContent.People.TABLE + "(" 
			 + FacesContent.People._ID + " INTEGER PRIMARY KEY, " 
			 + FacesContent.People.NAME + " TEXT UNIQUE " 
			 +")";
	
	private static final String DB_CREATE_FACES = "create table " 
			 + FacesContent.Faces.TABLE + "(" 
			 + FacesContent.Faces._ID + " INTEGER PRIMARY KEY, " 
			 + FacesContent.Faces.PERSON_ID+ " TEXT UNIQUE "
			 + FacesContent.Faces.THUMBNAIL + "TEXT"
			 + FacesContent.Faces.FEATURES + "TEXT"
			 + "FOREIGN KEY(" + FacesContent.Faces.PERSON_ID + ") REFERENCES " 
			 + FacesContent.Faces.TABLE + "(" + FacesContent.People._ID + ")"
			 + ")";

	public PeopleDBOpenHelper() {
		//TODO: costruttore
	}
	
	public PeopleDBOpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}


	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DB_CREATE_PERSON);
		db.execSQL(DB_CREATE_FACES);
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXIST " + FacesContent.Faces.TABLE);
		db.execSQL("DROP TABLE IF EXIST " + FacesContent.People.TABLE);
		onCreate(db);
	}
	
}
