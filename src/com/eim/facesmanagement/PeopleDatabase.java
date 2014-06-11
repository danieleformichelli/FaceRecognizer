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
	
	public void editPerson(String oldName, String newName) {
		SQLiteDatabase db = pdboh.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(FacesContract.People.NAME, newName);
		db.update(FacesContract.People.TABLE, values, FacesContract.People.NAME + " = '" + oldName + "'", null);
	}
	
	public void removePerson(String name) {
		SQLiteDatabase db = pdboh.getWritableDatabase();
		String whereClause = FacesContract.Faces.PERSON_ID + " = '" + name + "'";
		db.delete(FacesContract.Faces.TABLE, whereClause, null);
		whereClause = FacesContract.People._ID + "= '" + name +"'";
		db.delete(FacesContract.People.TABLE, whereClause, null);
	}
	
	public void addPerson(String name) {
		SQLiteDatabase db = pdboh.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(FacesContract.People.NAME, name);
		db.insert(FacesContract.People.TABLE, null, values);
	}
	
	public void addPhoto(String name, String photoUrl, String features) {
		SQLiteDatabase db = pdboh.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(FacesContract.Faces.PERSON_ID, name);
		values.put(FacesContract.Faces.PHOTO_URL, photoUrl);
		values.put(FacesContract.Faces.FEATURES, features);
		db.insert(FacesContract.Faces.TABLE, null, values);
	}
	
	public void removePhoto(String name, String photoUrl) {
		SQLiteDatabase db = pdboh.getWritableDatabase();
		String whereClause = FacesContract.Faces.PHOTO_URL + " = '" + photoUrl + "' AND " + FacesContract.Faces.PERSON_ID + " = '" + name + "'";
		db.delete(FacesContract.Faces.TABLE, whereClause, null);
	}
	
	public List<Person> getPeople() {
		return new ArrayList<Person>();
	}
}
