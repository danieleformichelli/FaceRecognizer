package com.eim.facesmanagement.peopledb;

import java.util.ArrayList;
import java.util.List;

import com.eim.facesmanagement.peopledb.FacesContract.Faces;
import com.eim.facesmanagement.peopledb.FacesContract.People;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
	
	public void addPhoto(String name, String photoUrl) {
		SQLiteDatabase db = pdboh.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(FacesContract.Faces.PERSON_ID, name);
		values.put(FacesContract.Faces.PHOTO_URL, photoUrl);
		db.insert(FacesContract.Faces.TABLE, null, values);
	}
	
	public void removePhoto(String name, String photoUrl) {
		SQLiteDatabase db = pdboh.getWritableDatabase();
		String whereClause = FacesContract.Faces.PHOTO_URL + " = '" + photoUrl + "' AND " + FacesContract.Faces.PERSON_ID + " = '" + name + "'";
		db.delete(FacesContract.Faces.TABLE, whereClause, null);
	}
	
	public List<Person> getPeople() {
		SQLiteDatabase db = pdboh.getReadableDatabase();
		String query = "SELECT * FROM " 
					+ FacesContract.Faces.TABLE + ", " 
					+ FacesContract.People.TABLE +
				"WHERE "
					+ FacesContract.People.TABLE + "." + FacesContract.People.NAME
					+ " = " 
					+ FacesContract.Faces.TABLE + "." + FacesContract.Faces.PERSON_ID + 
				"GROUP BY "
					+ FacesContract.People.TABLE + "." + FacesContract.People.NAME
				;
		List<Person> retList = new ArrayList();
		Person p;
		String name = null;
		List<Photo> photos = new ArrayList();
		Cursor c = db.rawQuery(query, null);
		while (!c.isAfterLast()) {
			String actualName = c.getString(c.getColumnIndex(FacesContract.People.NAME));
			if (!actualName.equals(name)) {
				//add a new Person if not the first time
				if (!name.equals(null)) {
					p = new Person(name, photos);
					retList.add(p);
				}
				photos.clear();
				name = c.getString(c.getColumnIndex(FacesContract.People.NAME));	
			}
			String url = c.getString(c.getColumnIndex(FacesContract.Faces.PHOTO_URL));
			Photo ph = new Photo (url);
			photos.add(ph);
		}
		return retList;
	}
}
