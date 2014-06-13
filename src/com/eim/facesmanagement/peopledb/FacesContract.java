package com.eim.facesmanagement.peopledb;

import android.provider.BaseColumns;

public class FacesContract {
	public static abstract class People implements BaseColumns {
		public static final String TABLE = "people";
		public static final String NAME = "name";
	}
	
	public static abstract class Faces implements BaseColumns {
		public static final String TABLE = "faces";
		public static final String PERSON_ID = "person_id";
		public static final String PHOTO_URL = "photo_url";
		public static final String FEATURES = "features";
	}
}
