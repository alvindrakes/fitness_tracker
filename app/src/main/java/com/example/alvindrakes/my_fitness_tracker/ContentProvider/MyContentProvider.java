package com.example.alvindrakes.my_fitness_tracker.ContentProvider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.example.alvindrakes.my_fitness_tracker.MyDBOpenHelper;

/*
    Content provider that manage the storing of data in the database
    Implements the 4 basic function which are insert, query, delete and update
 */
public class MyContentProvider extends ContentProvider {
    public static final int TRACKERLOGS = 1;
    private static final String AUTHORITY = "com.example.alvindrakes.my_fitness_tracker.ContentProvider.MyContentProvider";
    private static final String TABLE_TRACKERLOG = "TrackerLog";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_TRACKERLOG);

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, TABLE_TRACKERLOG, TRACKERLOGS);
    }

    private MyDBOpenHelper myDB;

    public MyContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = myDB.getWritableDatabase();
        long id;
        switch (uriType) {
            case TRACKERLOGS:
                id = sqlDB.insert(MyDBOpenHelper.TABLE_TRACKERLOG, null, values);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(TABLE_TRACKERLOG + "/" + id);
    }

    @Override
    public boolean onCreate() {
        myDB = new MyDBOpenHelper(getContext(), null, null, 1);
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(MyDBOpenHelper.TABLE_TRACKERLOG);

        Cursor cursor = queryBuilder.query(myDB.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}