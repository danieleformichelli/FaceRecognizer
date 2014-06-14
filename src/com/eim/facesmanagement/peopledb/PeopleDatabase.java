package com.eim.facesmanagement.peopledb;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.LongSparseArray;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class PeopleDatabase {
	private static PeopleDatabase instance;
	private static SQLiteDatabase db;

	public static PeopleDatabase getInstance(Context mContext) {
		if (instance == null) {
			instance = new PeopleDatabase();
			db = new PeopleDBOpenHelper(mContext).getWritableDatabase();
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					db.close();
				}
			});
		}

		return instance;
	}

	private PeopleDatabase() {
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
		Person selectedPerson;

		String query = "SELECT " + FacesContract.Faces.TABLE + "."
				+ FacesContract.Faces._ID + " AS PHOTO_ID, "
				+ FacesContract.People.NAME + ", "
				+ FacesContract.Faces.PHOTO_URL + " FROM "
				+ FacesContract.People.TABLE + " LEFT JOIN "
				+ FacesContract.Faces.TABLE + " WHERE "
				+ FacesContract.Faces.TABLE + "." + FacesContract.People._ID
				+ " = ?";
		String[] whereArgs = new String[] { String.valueOf(id) };

		Cursor c = db.rawQuery(query, whereArgs);
		int personNameIndex = c.getColumnIndex(FacesContract.People.NAME);
		int photoIdIndex = c.getColumnIndex("PHOTO_ID");
		int photoUrlIndex = c.getColumnIndex(FacesContract.Faces.PHOTO_URL);

		if (!c.moveToNext()) {
			c.close();
			return null;
		}

		selectedPerson = new Person(c.getString(personNameIndex), null);

		do {
			long photoId = c.getLong(photoIdIndex);
			String photoUrl = c.getString(photoUrlIndex);

			selectedPerson.addPhoto(photoId, new Photo(photoUrl));
		} while (c.moveToNext());

		c.close();
		return selectedPerson;
	}

	public LongSparseArray<Person> getPeople() {
		long currentId = -1;
		Person currentPerson = null;
		LongSparseArray<Person> people = new LongSparseArray<Person>();

		final String query = "SELECT " + FacesContract.People.TABLE + "."
				+ FacesContract.People._ID + " AS PERSON_ID, "
				+ FacesContract.People.NAME + ", " + FacesContract.Faces.TABLE
				+ "." + FacesContract.Faces._ID + " AS PHOTO_ID, "
				+ FacesContract.Faces.PHOTO_URL + " FROM "
				+ FacesContract.People.TABLE + " LEFT JOIN "
				+ FacesContract.Faces.TABLE + " ORDER BY PERSON_ID";
		Cursor c = db.rawQuery(query, null);
		int personIdIndex = c.getColumnIndex("PERSON_ID");
		int personNameIndex = c.getColumnIndex(FacesContract.People.NAME);
		int photoIdIndex = c.getColumnIndex("PHOTO_ID");
		int photoUrlIndex = c.getColumnIndex(FacesContract.Faces.PHOTO_URL);

		while (c.moveToNext()) {
			long personId = c.getLong(personIdIndex);
			String name = c.getString(personNameIndex);

			// add a new Person if it is the first time we found it
			if (personId != currentId) {
				currentId = personId;
				currentPerson = new Person(name, null);
				people.put(currentId, currentPerson);
			}

			if (!c.isNull(photoIdIndex)) {
				long photoId = c.getLong(photoIdIndex);
				String photoUrl = c.getString(photoUrlIndex);

				currentPerson.addPhoto(photoId, new Photo(photoUrl));
			}
		}

		c.close();
		return people;
	}

	public void clear() {
		db.delete(FacesContract.People.TABLE, null, null);
		db.delete(FacesContract.Faces.TABLE, null, null);
	}
}
