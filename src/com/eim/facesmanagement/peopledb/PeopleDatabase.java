package com.eim.facesmanagement.peopledb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.LongSparseArray;

public class PeopleDatabase {
	static PeopleDatabase instance;
	Context context;
	SQLiteDatabase db;

	public static PeopleDatabase getInstance(Context c) {
		if (instance == null)
			instance = new PeopleDatabase(c);

		return instance;
	}

	private PeopleDatabase(Context context) {
		this.context = context;
		db = new PeopleDBOpenHelper(context).getWritableDatabase();
	}

	public void editPersonName(long id, String newName) {
		ContentValues values = new ContentValues();
		values.put(FacesContract.People.NAME, newName);
		String whereClause = FacesContract.People._ID + " = ?";
		String[] whereArgs = { String.valueOf(id) };

		db.update(FacesContract.People.TABLE, values, whereClause, whereArgs);
	}

	public void removePerson(long id) {
		String whereClause = FacesContract.People._ID + " = ?";
		String[] whereArgs = { String.valueOf(id) };

		db.delete(FacesContract.People.TABLE, whereClause, whereArgs);
	}

	public long addPerson(String name) {
		ContentValues values = new ContentValues();
		values.put(FacesContract.People.NAME, name);

		return db.insert(FacesContract.People.TABLE, null, values);
	}

	public long addPhoto(long personId, String photoUrl) {
		ContentValues values = new ContentValues();
		values.put(FacesContract.Faces.PERSON_ID, personId);
		values.put(FacesContract.Faces.PHOTO_URL, photoUrl);

		return db.insert(FacesContract.Faces.TABLE, null, values);
	}

	public void removePhoto(long id) {
		String whereClause = FacesContract.Faces._ID + " = ?";
		String[] whereArgs = { String.valueOf(id) };

		db.delete(FacesContract.Faces.TABLE, whereClause, whereArgs);
	}

	public Person getPerson(long id) {
		Person selectedPerson = null;
		String query = "SELECT " + FacesContract.Faces.TABLE + "."
				+ FacesContract.Faces._ID + ", " + FacesContract.People.NAME
				+ ", " + FacesContract.Faces.PHOTO_URL + " FROM "
				+ FacesContract.People.TABLE + " LEFT JOIN "
				+ FacesContract.Faces.TABLE + " WHERE "
				+ FacesContract.People._ID + " = ?";
		String[] whereArgs = new String[] { String.valueOf(id) };

		Cursor c = db.rawQuery(query, whereArgs);

		if (!c.isAfterLast()) {
			String name = c.getString(c
					.getColumnIndex(FacesContract.People.NAME));
			selectedPerson = new Person(name, null);

			while (!c.isAfterLast()) {
				long photoId = c.getLong(c
						.getColumnIndex(FacesContract.Faces.TABLE + "."
								+ FacesContract.Faces._ID));
				String photoUrl = c.getString(c
						.getColumnIndex(FacesContract.Faces.PHOTO_URL));

				selectedPerson.addPhoto(photoId, new Photo(photoUrl));
			}
		}

		return selectedPerson;
	}

	public LongSparseArray<Person> getPeople() {
		long currentId = -1;
		Person currentPerson = null;
		LongSparseArray<Person> people = new LongSparseArray<Person>();

		String query = "SELECT " + FacesContract.People.TABLE + "."
				+ FacesContract.People._ID + " AS PERSON_ID, "
				+ FacesContract.People.NAME + ", " + FacesContract.Faces.TABLE
				+ "." + FacesContract.Faces._ID + " AS PHOTO_ID, "
				+ FacesContract.Faces.PHOTO_URL + " FROM "
				+ FacesContract.People.TABLE + " LEFT JOIN "
				+ FacesContract.Faces.TABLE + " ORDER BY PERSON_ID";
		Cursor c = db.rawQuery(query, null);

		while (!c.isAfterLast()) {
			long personId = c.getLong(c.getColumnIndex("PERSON_ID"));
			String name = c.getString(c
					.getColumnIndex(FacesContract.People.NAME));
			long photoId = c.getLong(c.getColumnIndex("PHOTO_ID"));
			String photoUrl = c.getString(c
					.getColumnIndex(FacesContract.Faces.PHOTO_URL));

			// add a new Person if it is the first time we found it
			if (personId != currentId) {
				currentId = personId;
				currentPerson = new Person(name, null);
				people.put(currentId, currentPerson);
			}

			currentPerson.addPhoto(photoId, new Photo(photoUrl));
		}

		return people;
	}
}
