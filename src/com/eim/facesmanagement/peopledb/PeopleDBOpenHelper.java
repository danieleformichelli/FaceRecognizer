package com.eim.facesmanagement.peopledb;

import com.eim.facesmanagement.peopledb.FacesContract.Faces;
import com.eim.facesmanagement.peopledb.FacesContract.People;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class PeopleDBOpenHelper extends SQLiteOpenHelper {
	
	private static final String DB_NAME = "faces.db";
	private static final int VERSION = 1;

	private static final String DB_CREATE_PEOPLE = "create table " 
			 + FacesContract.People.TABLE + "(" 
			 + FacesContract.People._ID + " INTEGER PRIMARY KEY, " 
			 + FacesContract.People.NAME + " TEXT UNIQUE " 
			 +")";
	
	private static final String DB_CREATE_FACES = "create table " 
			 + FacesContract.Faces.TABLE + "(" 
			 + FacesContract.Faces._ID + " INTEGER PRIMARY KEY, " 
			 + FacesContract.Faces.PERSON_ID + " TEXT UNIQUE "
			 + FacesContract.Faces.PHOTO_URL + " TEXT "
			 + "FOREIGN KEY(" + FacesContract.Faces.PERSON_ID + ") REFERENCES " 
			 + FacesContract.Faces.TABLE + "(" + FacesContract.People._ID + ")"
			 + ")";

	public PeopleDBOpenHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}
	
	public PeopleDBOpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
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
