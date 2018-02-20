package example.android.xtasyevents;


import android.content.Intent;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import example.android.xtasyevents.QuestionsSpreadsheetWebService;
import example.android.xtasyevents.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {
    int final_id=0;
    String id_string="";
    private TextView xtasyid,name,email,college,contact,gender,extras;
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static String request_url = "http://xtasy.cetb.in/api/find?xtasyid=";
    private Button scanBtn;
    private TextView tvScanFormat, tvScanContent;
    private LinearLayout llSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        scanBtn = (Button) findViewById(R.id.scan);
        //tvScanFormat = (TextView) findViewById(R.id.tvScanFormat);
        //tvScanContent = (TextView) findViewById(R.id.tvScanContent);
        llSearch = (LinearLayout) findViewById(R.id.llSearch);




        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://docs.google.com/forms/d/e/")
                .build();
        final QuestionsSpreadsheetWebService spreadsheetWebService = retrofit.create(QuestionsSpreadsheetWebService.class);



        findViewById(R.id.search).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        xtasyid = (TextView)findViewById(R.id.xtasyid);
                        id_string=xtasyid.getText().toString();
                        final_id=Integer.parseInt(cropId(id_string));
                        request_url+=(((((final_id+5)*30)-7)*7)+2)*300;
                        Log.i(LOG_TAG,request_url);
                        TsunamiAsyncTask task = new TsunamiAsyncTask();
                        task.execute();
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




        findViewById(R.id.submit).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        name = (TextView) findViewById(R.id.name);
                        email = (TextView) findViewById(R.id.email);
                        college = (TextView) findViewById(R.id.college);
                        contact = (TextView) findViewById(R.id.contact);
                        gender = (TextView) findViewById(R.id.gender);
                        extras = (TextView) findViewById(R.id.extras);

                        String xtasyidInput = xtasyid.getText().toString();
                        String nameInput = name.getText().toString();
                        String emailInput = email.getText().toString();
                        String collegeInput = college.getText().toString();
                        String contactInput = contact.getText().toString();
                        String genderInput = gender.getText().toString();
                        String extrasInput = extras.getText().toString();
                        Call<Void> completeQuestionnaireCall = spreadsheetWebService.completeQuestionnaire(xtasyidInput,nameInput,emailInput,collegeInput,contactInput,genderInput,extrasInput);
                        completeQuestionnaireCall.enqueue(callCallback);
                    }
                }
        );
    }
    private void updateUi(Participant p) {
        name = (TextView) findViewById(R.id.name);
        email = (TextView) findViewById(R.id.email);
        college = (TextView) findViewById(R.id.college);
        contact = (TextView) findViewById(R.id.contact);
        gender = (TextView) findViewById(R.id.gender);
        extras = (TextView) findViewById(R.id.extras);
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
            Log.i(LOG_TAG,url.toString());

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
    private final Callback<Void> callCallback = new Callback<Void>() {
        @Override
        public void onResponse(Response<Void> response) {
            Log.d("XXX", "Submitted. " + response);
        }

        @Override
        public void onFailure(Throwable t) {
            Log.e("XXX", "Failed", t);
        }
    };
    private String cropId(String word)
    {
        if (word.length() == 4) {
            return word;
        } else if (word.length() > 4) {
            return word.substring(word.length() - 4);
        } else {
            // whatever is appropriate in this case
            throw new IllegalArgumentException("word has less than 3 characters!");
        }
    }




    public void scanner(View v) {

        llSearch.setVisibility(View.GONE);
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
                llSearch.setVisibility(View.GONE);
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