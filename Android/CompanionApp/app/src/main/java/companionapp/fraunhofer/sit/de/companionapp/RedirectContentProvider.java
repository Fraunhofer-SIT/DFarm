package companionapp.fraunhofer.sit.de.companionapp;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

/**
 * A content provider which uses the url query parameter to use another content provider.
 * This can be used to get access to a content provider of which the app has access to.
 * 
 * Note that this content provider can be used as a confused deputy.
 */
public class RedirectContentProvider extends ContentProvider {
    public RedirectContentProvider() {
    }

    private Uri translate(Uri uri) {
        String q = uri.getQueryParameter("url");
        return Uri.parse(q);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return getContext().getContentResolver().delete(translate(uri), selection, selectionArgs);
    }

    @Override
    public String getType(Uri uri) {
        return getContext().getContentResolver().getType(translate(uri));
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return getContext().getContentResolver().insert(translate(uri), values);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        return getContext().getContentResolver().query(translate(uri), projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return getContext().getContentResolver().update(uri, values, selection, selectionArgs);
    }
}
