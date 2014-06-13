package com.eim.facesmanagement.peopledb;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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
		String whereClause = FacesContract.People.NAME + " = ?";
		String[] whereArgs = { oldName };

		db.update(FacesContract.People.TABLE, values, whereClause, whereArgs);
		db.close();
	}

	public void removePerson(String name) {
		SQLiteDatabase db = pdboh.getWritableDatabase();
		String whereClause = FacesContract.People.NAME + " = ?";
		String[] whereArgs = { name };

		db.delete(FacesContract.People.TABLE, whereClause, whereArgs);
		db.close();
	}

	public void addPerson(String name) {
		SQLiteDatabase db = pdboh.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(FacesContract.People.NAME, name);

		db.insert(FacesContract.People.TABLE, null, values);
		db.close();
	}

	public void addPhoto(String name, String photoUrl) {
		SQLiteDatabase db = pdboh.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(FacesContract.Faces.PERSON_ID, name);
		values.put(FacesContract.Faces.PHOTO_URL, photoUrl);

		db.insert(FacesContract.Faces.TABLE, null, values);
		db.close();
	}

	public void removePhoto(String photoUrl) {
		SQLiteDatabase db = pdboh.getWritableDatabase();
		String whereClause = FacesContract.People.NAME + " = ? AND "
				+ FacesContract.Faces.PHOTO_URL + " = ?";
		String[] whereArgs = { photoUrl };

		db.delete(FacesContract.Faces.TABLE, whereClause, whereArgs);
		db.close();
	}

	public List<Person> getPeople() {
		Person currentPerson = null;
		List<Person> people = new ArrayList<Person>();
		SQLiteDatabase db = pdboh.getReadableDatabase();
		String query = "SELECT " + FacesContract.People.NAME + ", "
				+ FacesContract.Faces.PHOTO_URL + " FROM "
				+ FacesContract.Faces.TABLE + ", " + FacesContract.People.TABLE
				+ " WHERE " + FacesContract.People.TABLE + "."
				+ FacesContract.People._ID + " = "
				+ FacesContract.Faces.PERSON_ID + " ORDER BY "
				+ FacesContract.People.NAME;
		Cursor c = db.rawQuery(query, null);
		while (!c.isAfterLast()) {
			String name = c.getString(c
					.getColumnIndex(FacesContract.People.NAME));
			String photoUrl = c.getString(c
					.getColumnIndex(FacesContract.People.NAME));

			// add a new Person if it is the first time we found it
			if (name.compareTo(currentPerson.getName()) != 0) {
				currentPerson = new Person(name, null);
				people.add(currentPerson);
			}

			currentPerson.addPhoto(new Photo(photoUrl));
		}

		db.close();
		return people;
	}
}
