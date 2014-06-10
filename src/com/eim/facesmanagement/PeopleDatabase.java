package com.eim.facesmanagement;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.widget.Toast;

public class PeopleDatabase {
	
	PeopleDBOpenHelper pdboh;
	
	public void editperson(String oldName, String newName) {
		pdboh = new PeopleDBOpenHelper(this);
	}
	
	public void removePerson(String name) {
		
	}
	
	public void addPerson(String name) {
		SQLiteDatabase db = pdboh.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(FacesContent.People.NAME, name);
		long r = db.insert(FacesContent.People.TABLE, null, values);
	}
	
	public void addPhoto(String name, String photoUrl) {
		
	}
	
	public void removePhoto(String photoUrl) {
		
	}
	
	public List<Person> getPeople() {
		return new ArrayList<Person>();
	}
}
