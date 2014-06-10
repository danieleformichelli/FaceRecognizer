package com.eim.facesmanagement;

import android.provider.BaseColumns;

public class FacesContent {
	public static abstract class People implements BaseColumns {
		public static final String TABLE = "people";
		public static final String NAME = "name";
	}
	
	public static abstract class Faces implements BaseColumns {
		public static final String TABLE = "faces";
		public static final String PERSON_ID = "person_id";
		public static final String THUMBNAIL = "thumbnail";
		public static final String FEATURES = "features";
	}
}
