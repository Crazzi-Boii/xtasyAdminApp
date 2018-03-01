
package com.example.xtasy;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import com.example.xtasy.R;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import android.app.ProgressDialog;
import android.os.Environment;
import java.io.IOException;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.Buffer;
import android.Manifest;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.net.Uri;

import com.example.xtasy.data.PetContract.PetEntry;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class ExportActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the pet data loader
     */
    private static final int PET_LOADER = 0;
    int n;

    /**
     * Adapter for the ListView
     */
    PetCursorAdapter mCursorAdapter;
    public static final String LOG_TAG = EditorActivity.class.getSimpleName();

    private int requestCode;
    private int grantResults[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_email);
        setTitle("Export Data");
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ){
            //if you dont have required permissions ask for it (only required for API 23+)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},requestCode);


            onRequestPermissionsResult(requestCode,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},grantResults);}

        create();
        getLoaderManager().initLoader(PET_LOADER, null, this);
        Toast.makeText(getApplicationContext(),"registration.csv file created under xtasy folder.\nExport Successful!!",Toast.LENGTH_LONG);

        findViewById(R.id.buttonSend).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try{

                            File file = new File(Environment.getExternalStorageDirectory().getPath()+"/xtasy"+"/registration.csv");
                            String filelocation=file.toString();
                            String subject=((EditText)findViewById(R.id.editTextSubject)).getText().toString().trim();
                            String body=((EditText)findViewById(R.id.editTextBody)).getText().toString().trim();
                            Intent intent = new Intent(Intent.ACTION_SENDTO);
                            intent.setType("text/plain");
                            intent.putExtra(Intent.EXTRA_TEXT, body);
                            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse( "file://"+filelocation));
                            intent.setData(Uri.parse("mailto:"));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            startActivity(intent);
                            finish();
                        }
                        catch(Exception e)  {
                            Log.i("Error Email:","is exception raises during sending mail"+e.getMessage());
                        }

                    }
                }
        );

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_EMAIL,
                PetEntry.COLUMN_PET_COLLEGE,
                PetEntry.COLUMN_PET_CONTACT,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_EXTRAS
        };
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                PetEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        n=cursor.getCount();
        cursor.moveToFirst();
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        File folder = new File(Environment.getExternalStorageDirectory().getPath()+"/xtasy");
        folder.mkdirs();
        File file = new File(Environment.getExternalStorageDirectory().getPath()+"/xtasy"+"/registration.csv");
        if(!file.exists())
        {
            try {
                file.createNewFile();

            }
            catch (Exception e)
            {
                Log.i("ERROR:", e.getMessage());
            }
        }

        try {
            FileOutputStream fileinput = new FileOutputStream(file,true);
            OutputStreamWriter file_writer = new OutputStreamWriter(fileinput);
            BufferedWriter buffered_writer = new BufferedWriter(file_writer);
            buffered_writer.write("XTASY ID, NAME, EMAIL ID, COLLEGE, CONTACT, GENDER, EXTRAS,\n");
            do {
                // Find the columns of pet attributes that we're interested in
                int idColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_ID);
                int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
                int emailColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_EMAIL);
                int collegeColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_COLLEGE);
                int contactColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_CONTACT);
                int genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
                int extrasColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_EXTRAS);

                // Extract out the value from the Cursor for the given column index
                buffered_writer.write( cursor.getString(idColumnIndex).replaceAll(","," ")+",");
                buffered_writer.write(  cursor.getString(nameColumnIndex).replaceAll(","," ")+",");
                buffered_writer.write(  cursor.getString(emailColumnIndex).replaceAll(","," ")+",");
                buffered_writer.write( cursor.getString(collegeColumnIndex).replaceAll(","," ")+",");
                buffered_writer.write(  cursor.getString(contactColumnIndex).replaceAll(","," ")+",");
                buffered_writer.write( cursor.getString(genderColumnIndex).replaceAll(","," ")+",");
                buffered_writer.write(  cursor.getString(extrasColumnIndex).replaceAll(","," ")+", \n");
            } while (cursor.moveToNext());
            buffered_writer.close();

        }
        catch (Exception e){Log.e("Error",e.getMessage());}
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
    }

    public void create()
    {

        File folder = new File(Environment.getExternalStorageDirectory().getPath()+"/xtasy");
        folder.mkdirs();
        File file = new File(Environment.getExternalStorageDirectory().getPath()+"/xtasy"+"/registration.csv");
        try {
            FileOutputStream fileinput = new FileOutputStream(file);
            PrintStream printstream = new PrintStream(fileinput);
            Log.i("Created","Successfully");
            fileinput.close();
        }
        catch (Exception e)
        {
            Log.i("Error while creating:", e.getMessage());
        }
    }
    @Override // android recommended class to handle permissions
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d("permission", "granted");
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.uujm
                    Log.d("permission", "denied");

                    //app cannot function without this permission for now so close it...
                    onDestroy();
                }
                return;
            }
        }
    }
}