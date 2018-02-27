package com.example.xtasy;


import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import com.example.xtasy.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import com.example.xtasy.data.PetContract.PetEntry;

public class EditorActivity extends AppCompatActivity  implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the pet data loader */
    private static final int EXISTING_PET_LOADER = 0;

    /** Content URI for the existing pet (null if it's a new pet) */
    private Uri mCurrentPetUri;

    int final_id=0;
    String id_string="";
    private TextView xtasyid,name,email,college,contact,gender,extras;
    public static final String LOG_TAG = EditorActivity.class.getSimpleName();
    private static String request_url = "http://xtasy.cetb.in/api/find?xtasyid=";
    private Button scanBtn;
    private TextView tvScanFormat, tvScanContent;
    private LinearLayout llSearch;

    private boolean mPetHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPetHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        scanBtn = (Button) findViewById(R.id.scan);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new pet or editing an existing one.
        Intent intent = getIntent();
        mCurrentPetUri = intent.getData();

        if (mCurrentPetUri == null) {
            // This is a new pet, so change the app bar to say "Add a Pet"
            setTitle("Add a Participant");

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing pet, so change app bar to say "Edit Pet"
            setTitle("Edit Participant");

            // Initialize a loader to read the pet data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);
        }

        xtasyid=(TextView) findViewById(R.id.xtasyid);
        name = (TextView) findViewById(R.id.name);
        email = (TextView) findViewById(R.id.email);
        college = (TextView) findViewById(R.id.college);
        contact = (TextView) findViewById(R.id.contact);
        gender = (TextView) findViewById(R.id.gender);
        extras = (TextView) findViewById(R.id.extras);

        xtasyid.setOnTouchListener(mTouchListener);
        name.setOnTouchListener(mTouchListener);
        email.setOnTouchListener(mTouchListener);
        college.setOnTouchListener(mTouchListener);
        contact.setOnTouchListener(mTouchListener);
        gender.setOnTouchListener(mTouchListener);
        extras.setOnTouchListener(mTouchListener);


        findViewById(R.id.search).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        id_string=xtasyid.getText().toString();
                        if(id_string.compareTo("")==0)
                        {
                            Toast.makeText(getApplicationContext(), "Xtasy id is Null", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            if(cropId(id_string)==null)
                            {
                                Toast.makeText(getApplicationContext(),"Xtasy id less than 4 characters.",Toast.LENGTH_LONG).show();
                            }
                            else
                            {
                                try{
                                    final_id=Integer.parseInt(cropId(id_string));
                                    request_url+=(((((final_id+5)*30)-7)*7)+2)*300;
                                    TsunamiAsyncTask task = new TsunamiAsyncTask();
                                    task.execute();
                                }
                                catch (Exception e)
                                {
                                    Toast.makeText(getApplicationContext(),"Invalid Xtasy id",Toast.LENGTH_LONG).show();
                                }

                            }
                        }

                    }
                }
        );


        findViewById(R.id.scan).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        scanner(v);
                    }
                }
        );
    }


    private void savePet() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String xtasyidString = xtasyid.getText().toString().trim();
        String nameString = name.getText().toString().trim();
        String emailString = email.getText().toString().trim();
        String collegeString = college.getText().toString().trim();
        String contactString = contact.getText().toString().trim();
        String genderString = gender.getText().toString().trim();
        String extrasString = extras.getText().toString().trim();

        // Check if this is supposed to be a new pet
        // and check if all the fields in the editor are blank
        if (mCurrentPetUri == null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(emailString) ) {
            // Since no fields were modified, we can return early without creating a new pet.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_ID, xtasyidString);
        values.put(PetEntry.COLUMN_PET_NAME, nameString);
        values.put(PetEntry.COLUMN_PET_EMAIL, emailString);
        values.put(PetEntry.COLUMN_PET_COLLEGE, collegeString);
        values.put(PetEntry.COLUMN_PET_CONTACT, contactString);
        values.put(PetEntry.COLUMN_PET_GENDER, genderString);
        values.put(PetEntry.COLUMN_PET_EXTRAS, extrasString);

        // Determine if this is a new or existing pet by checking if mCurrentPetUri is null or not
        if (mCurrentPetUri == null) {
            // This is a NEW pet, so insert a new pet into the provider,
            // returning the content URI for the new pet.
            Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING pet, so update the pet with content URI: mCurrentPetUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPetUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentPetUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentPetUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save pet to database
                savePet();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mPetHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
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
                mCurrentPetUri,   // Provider content URI to query
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

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int idColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_ID);
            int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int emailColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_EMAIL);
            int collegeColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_COLLEGE);
            int contactColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_CONTACT);
            int genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int extrasColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_EXTRAS);

            // Extract out the value from the Cursor for the given column index
            String mXtasyid = cursor.getString(idColumnIndex);
            String mName = cursor.getString(nameColumnIndex);
            String mEmail = cursor.getString(emailColumnIndex);
            String mCollege = cursor.getString(collegeColumnIndex);
            String mContact = cursor.getString(contactColumnIndex);
            String mGender = cursor.getString(genderColumnIndex);
            String mExtras = cursor.getString(extrasColumnIndex);

            // Update the views on the screen with the values from the database
            xtasyid.setText(mXtasyid);
            name.setText(mName);
            email.setText(mEmail);
            college.setText(mCollege);
            contact.setText(mContact);
            gender.setText(mGender);
            extras.setText(mExtras);
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        xtasyid.setText("");
        name.setText("");
        email.setText("");
        college.setText("");
        contact.setText("");
        gender.setText("");
        extras.setText("");
    }


    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void deletePet() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentPetUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentPetUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }



    private void updateUi(Participant p) {
        name.setText(p.getName());
        email.setText(p.getEmail());
        college.setText(p.getCollege());
        contact.setText(p.getContact());
        gender.setText(p.getGender());
    }

    private class TsunamiAsyncTask extends AsyncTask<URL, Void, Participant> {

        @Override
        protected Participant doInBackground(URL... urls) {
            // Create URL object
            URL url = createUrl(request_url);

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
                request_url = "http://xtasy.cetb.in/api/find?xtasyid=";
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem making the HTTP request.", e);
            }
            Participant p = extractFeatureFromJson(jsonResponse);

            // Return the {@link Event} object as the result fo the {@link TsunamiAsyncTask}
            return p;
        }

        /**
         * Update the screen with the given earthquake (which was the result of the
         * {@link TsunamiAsyncTask}).
         */
        @Override
        protected void onPostExecute(Participant p) {
            if (p == null) {
                Toast.makeText(getApplicationContext(), "Invalid Xtasy id", Toast.LENGTH_LONG).show();
                return;
            }
            updateUi(p);
        }
        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error with creating URL", exception);
                return null;
            }
            return url;
        }
        private String makeHttpRequest(URL url) throws IOException {

            String jsonResponse = "";
            if(url==null)
                return jsonResponse;
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();
                if(urlConnection.getResponseCode()==200)
                {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                }
                else {
                    Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
                    Toast.makeText(getApplicationContext(),"Error response code: " + urlConnection.getResponseCode(),Toast.LENGTH_LONG).show();
                }

            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem retrieving the participant JSON results.", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            return jsonResponse;
        }
        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }
        private Participant extractFeatureFromJson(String participantJSON) {
            if(participantJSON.isEmpty())
            {
                return null;
            }
            try {
                JSONObject baseJsonResponse = new JSONObject(participantJSON);
                String xtasyid=baseJsonResponse.getString("xtasyid");
                String name=baseJsonResponse.getString("name");
                String email=baseJsonResponse.getString("emailid");
                String college=baseJsonResponse.getString("college");
                String contact=baseJsonResponse.getString("contact");
                String gender=baseJsonResponse.getString("gender");
                return new Participant(xtasyid,name,email,college,contact,gender);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the Participant JSON results", e);
            }
            return null;
        }
    }
    private String cropId(String word)
    {
        if (word.length() == 4) {
            return word;
        } else if (word.length() > 4) {
            return word.substring(word.length() - 4);
        } else {
            return null;
        }
    }


    public void scanner(View v) {

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Scan a barcode or QRcode");
        integrator.setOrientationLocked(false);
        integrator.initiateScan();

//        Use this for more customization
//        IntentIntegrator integrator = new IntentIntegrator(this);
//        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
//        integrator.setPrompt("Scan a barcode");
//        integrator.setCameraId(0);  // Use a specific camera of the device
//        integrator.setBeepEnabled(false);
//        integrator.setBarcodeImageEnabled(true);
//        integrator.initiateScan();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        xtasyid = (TextView) findViewById(R.id.xtasyid);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                //llSearch.setVisibility(View.VISIBLE);

                final_id=Integer.parseInt(result.getContents());
                final_id=(((final_id-7)*4)+20)/300;
                xtasyid.setText("xtasy"+final_id);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }




}