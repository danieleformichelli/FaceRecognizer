package com.eim.facesmanagement.peopledb;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

/**
 * This class manages the Database containing people and their photos
 */
public class PeopleDatabase {
	private static PeopleDatabase instance;
	private static PeopleDBOpenHelper pdboh;
	private static SQLiteDatabase db;

	public static PeopleDatabase getInstance(Context mContext) {
		if (instance == null) {
			instance = new PeopleDatabase();
			pdboh = new PeopleDBOpenHelper(mContext);
			db = pdboh.getWritableDatabase();
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

	/**
	 * Edits the name of a person
	 * 
	 * @param id
	 *            - identifier of the person
	 * @param newName
	 *            - new name
	 */
	public void editPersonName(int id, String newName) {
		ContentValues values = new ContentValues();
		values.put(FacesContract.People.NAME, newName);
		String whereClause = FacesContract.People._ID + " = ?";
		String[] whereArgs = { String.valueOf(id) };

		db.update(FacesContract.People.TABLE, values, whereClause, whereArgs);
	}

	/**
	 * Removes a person and the related photos
	 * 
	 * @param id
	 *            identifier of the person
	 */
	public void removePerson(int id) {
		String query = "SELECT " + FacesContract.Faces.PHOTO_URL + " FROM "
				+ FacesContract.Faces.TABLE + " WHERE "
				+ FacesContract.Faces.PERSON_ID + " = ?";
		String[] whereArgs = { String.valueOf(id) };

		Cursor c = db.rawQuery(query, whereArgs);

		if (c.moveToNext()) {
			int photoUrlIndex = c.getColumnIndex(FacesContract.Faces.PHOTO_URL);
			while (c.moveToNext()) {
				String photoUrl = c.getString(photoUrlIndex);
				File photoFile = new File(photoUrl);
				if (photoFile != null)
					photoFile.delete();
			}

			String whereClause = FacesContract.People._ID + " = ?";

			db.delete(FacesContract.People.TABLE, whereClause, whereArgs);
		}
	}

	/**
	 * Adds a person
	 * 
	 * @param name
	 *            name of the person
	 * @return the identifier of the inserted person
	 */
	public int addPerson(String name) {
		ContentValues values = new ContentValues();
		values.put(FacesContract.People.NAME, name);

		return (int) db.insert(FacesContract.People.TABLE, null, values);
	}

	/**
	 * Adds a photo to a person
	 * 
	 * @param personId
	 *            identifier of the person to whom the photo is added
	 * @param photoUrl
	 *            url of the photo to be added
	 * @return the identifier of the photo added
	 */
	public int addPhoto(int personId, String photoUrl) {
		ContentValues values = new ContentValues();
		values.put(FacesContract.Faces.PERSON_ID, personId);
		values.put(FacesContract.Faces.PHOTO_URL, photoUrl);

		return (int) db.insert(FacesContract.Faces.TABLE, null, values);
	}

	/**
	 * Removes a photo from both the database and the disk
	 * 
	 * @param id
	 *            identifier of the photo to remove
	 */
	public void removePhoto(int id) {

		String query = "SELECT " + FacesContract.Faces.PHOTO_URL + " FROM "
				+ FacesContract.Faces.TABLE + " WHERE "
				+ FacesContract.Faces._ID + " = " + id;

		Cursor c = db.rawQuery(query, null);
		int photoUrlIndex = c.getColumnIndex(FacesContract.Faces.PHOTO_URL);

		if (c.moveToNext()) {
			String photoUrl = c.getString(photoUrlIndex);
			File photoFile = new File(photoUrl);
			if (photoFile != null)
				photoFile.delete();

			String whereClause = FacesContract.Faces._ID + " = ?";
			String[] whereArgs = { String.valueOf(id) };

			db.delete(FacesContract.Faces.TABLE, whereClause, whereArgs);
		}
	}

	/**
	 * Returns a Person object from its id
	 * 
	 * @param id
	 * @return the Person object, null if the person does not exist
	 */
	public Person getPerson(int id) {
		Person selectedPerson;

		String query = "SELECT " + FacesContract.Faces.TABLE + "."
				+ FacesContract.Faces._ID + " AS photoId, "
				+ FacesContract.People.NAME + ", "
				+ FacesContract.Faces.PHOTO_URL + " FROM "
				+ FacesContract.People.TABLE + " LEFT JOIN "
				+ FacesContract.Faces.TABLE + " WHERE "
				+ FacesContract.Faces.TABLE + "." + FacesContract.People._ID
				+ " = ?";
		String[] whereArgs = new String[] { String.valueOf(id) };

		Cursor c = db.rawQuery(query, whereArgs);
		int personNameIndex = c.getColumnIndex(FacesContract.People.NAME);
		int photoIdIndex = c.getColumnIndex("photoId");
		int photoUrlIndex = c.getColumnIndex(FacesContract.Faces.PHOTO_URL);

		if (!c.moveToNext()) {
			c.close();
			return null;
		}

		selectedPerson = new Person(c.getString(personNameIndex), null);

		do {
			int photoId = c.getInt(photoIdIndex);
			String photoUrl = c.getString(photoUrlIndex);

			selectedPerson.addPhoto(photoId, new Photo(photoUrl));
		} while (c.moveToNext());

		c.close();
		return selectedPerson;
	}

	/**
	 * Returns all the people in the database and their photos
	 * 
	 * @return a sparse array in which each key is the identifier of the person
	 *         stored in the correspondent value
	 */
	public SparseArray<Person> getPeople() {
		int currentId = -1;
		Person currentPerson = null;
		SparseArray<Person> people = new SparseArray<Person>();

		final String query = "SELECT " + FacesContract.People.TABLE + "."
				+ FacesContract.People._ID + " AS personId, "
				+ FacesContract.People.NAME + ", " + FacesContract.Faces.TABLE
				+ "." + FacesContract.Faces._ID + " AS photoId, "
				+ FacesContract.Faces.PHOTO_URL + " FROM "
				+ FacesContract.People.TABLE + " LEFT JOIN "
				+ FacesContract.Faces.TABLE + " ON "
				+ FacesContract.People.TABLE + "." + FacesContract.People._ID
				+ " = " + FacesContract.Faces.PERSON_ID + " ORDER BY "
				+ FacesContract.People.TABLE + "." + FacesContract.People._ID;

		Cursor c = db.rawQuery(query, null);

		int personIdIndex = c.getColumnIndex("personId");
		int personNameIndex = c.getColumnIndex(FacesContract.People.NAME);
		int photoIdIndex = c.getColumnIndex("photoId");
		int photoUrlIndex = c.getColumnIndex(FacesContract.Faces.PHOTO_URL);

		while (c.moveToNext()) {
			int personId = c.getInt(personIdIndex);
			String name = c.getString(personNameIndex);

			// add a new Person if it is the first time we found it
			if (personId != currentId) {
				currentId = personId;
				currentPerson = new Person(name, null);
				people.put(currentId, currentPerson);
			}

			if (!c.isNull(photoIdIndex)) {
				int photoId = c.getInt(photoIdIndex);
				String photoUrl = c.getString(photoUrlIndex);

				currentPerson.addPhoto(photoId, new Photo(photoUrl));
			}
		}

		c.close();
		return people;
	}

	/**
	 * Deletes all the entries from the database and the photos on the disk
	 */
	public void clear() {
		String query = "SELECT " + FacesContract.Faces.PHOTO_URL + " FROM "
				+ FacesContract.Faces.TABLE;

		Cursor c = db.rawQuery(query, null);
		int photoUrlIndex = c.getColumnIndex(FacesContract.Faces.PHOTO_URL);
		while (c.moveToNext()) {
			String photoUrl = c.getString(photoUrlIndex);
			File photoFile = new File(photoUrl);
			if (photoFile != null)
				photoFile.delete();
		}

		pdboh.clear(db);
	}
}
