package com.eim.facesmanagement;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.widget.Toast;

public class PeopleDatabase {
	
	Context context;
	PeopleDBOpenHelper pdboh;
	
	public PeopleDatabase(Context context) {
		this.context = context;
		pdboh = new PeopleDBOpenHelper(context);
	}
	
	public void editperson(String oldName, String newName) {
	}
	
	public void removePerson(String name) {
		
	}
	
	public void addPerson(String name) {
		SQLiteDatabase db = pdboh.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(FacesContract.People.NAME, name);
		long r = db.insert(FacesContract.People.TABLE, null, values);
	}
	
	public void addPhoto(String name, String photoUrl) {
		
	}
	
	public void removePhoto(String photoUrl) {
		
	}
	
	public List<Person> getPeople() {
		return new ArrayList<Person>();
	}
}
