package com.example.alvindrakes.my_fitness_tracker.ContentProvider;

import android.net.Uri;

public class MyProviderContract {

    final static String AUTHORITY = "com.example.alvindrakes.trackerprovider";
    final static Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    public interface URI {
        Uri ID_INSERT = Uri.parse("content://" + AUTHORITY + "/tracker/insert");
        Uri ID_QUERY = Uri.parse("content://" + AUTHORITY + "/tracker/query");
        Uri ID_UPDATE = Uri.parse("content://" + AUTHORITY + "/tracker/update");
        Uri ID_DELETE = Uri.parse("content://" + AUTHORITY + "/tracker/delete");
    }
}
