package com.soapparsingusinghttpsclient;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataInputStream;


public class Main2Activity extends AppCompatActivity {

    ProgressDialog progressDialog;
    TextView txt_responce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        txt_responce = findViewById(R.id.txt_responce);

        new AsynTask().execute();
    }

    class AsynTask extends AsyncTask<String,String,String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try{
                if(progressDialog !=null && !progressDialog.isShowing()){
                    progressDialog.show();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            return callSOAPServer();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try{
                if(progressDialog != null && progressDialog.isShowing()){
                    progressDialog.dismiss();
                }

                Log.e("" + getClass().getName() + " :  ",s);

                String jsonString[] = s.split("<"+"?"+"xml");

                Log.e("jsonString[0]",jsonString[0]);

                JSONObject jsonObject= new JSONObject(jsonString[0]);

                StringBuilder responce_sb = new StringBuilder();
                //String Result = jsonObject.getString("Result");
                //String Code = jsonObject.getString("Code");

                responce_sb.append(jsonObject.getString("Result") + "\n" + jsonObject.getString("Code") + "\n\n");
                Log.e("Responce","Result" + jsonObject.getString("Result") + " \n" + "Code" + jsonObject.getString("Code"));

                JSONArray jsonArray = jsonObject.getJSONArray("VisitorType");

                for(int i=0; i < jsonArray.length();i++){
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    responce_sb.append("VisitorTypeID : " + jsonObject1.getString("VisitorTypeID")
                            + " \n" + "VisitorTypeName : " + jsonObject1.getString("VisitorTypeName") + "\n");
                    Log.e("VisitorType_Responce","VisitorTypeID : " + jsonObject1.getString("VisitorTypeID")
                            + " \n" + "VisitorTypeName : " + jsonObject1.getString("VisitorTypeName"));
                }

                txt_responce.setText(responce_sb.toString());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private String callSOAPServer() {
        byte[] result = null;

        HttpParams httpParameters = new BasicHttpParams();
        // Set the timeout in milliseconds until a connection is established.
        int timeoutConnection = 15000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        // Set the default socket timeout (SO_TIMEOUT)
        // in milliseconds which is the timeout for waiting for data.
        int timeoutSocket = 35000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

        DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);
        /*
         * httpclient.getCredentialsProvider().setCredentials( new
         * AuthScope("os.icloud.com", 80, null, "Digest"), new
         * UsernamePasswordCredentials(username, password));
         */
        HttpPost httppost = new HttpPost("http://smartsecure.co.in/service.asmx");
        httppost.setHeader("soapaction", "http://smartsecure.co.in/VisitorType_List");
        httppost.setHeader("Content-Type", "text/xml; charset=utf-8");

        System.out.println("executing request" + httppost.getRequestLine());
        //now create a soap request message as follows:
        final StringBuffer soap = new StringBuffer();
        // this is a sample data..you have create your own required data  BEGIN
        soap.append("" + getbody());
        /* soap.append(body); */
        // END of MEssage Body
        Log.i("SOAP Request", ""+soap.toString());
        // END of full SOAP request  message
        try {
            HttpEntity entity = new StringEntity(soap.toString(),HTTP.UTF_8);
            httppost.setEntity(entity);
            HttpResponse response = httpclient.execute(httppost);// calling server
            HttpEntity r_entity = response.getEntity();  //get response
            Log.i("Reponse Header", "Begin...");          // response headers
            Log.i("Reponse Header", "StatusLine:"+response.getStatusLine());
            Header[] headers = response.getAllHeaders();
            for(Header h:headers){
                Log.i("Reponse Header",h.getName() + ": " + h.getValue());
            }
            Log.i("Reponse Header", "END...");
            if (r_entity != null) {
                result = new byte[(int) r_entity.getContentLength()];
                if (r_entity.isStreaming()) {
                    DataInputStream is = new DataInputStream(
                            r_entity.getContent());
                    is.readFully(result);
                }
            }
        } catch (Exception E) {
            Log.i("Exception While", ""+E.getMessage());
            E.printStackTrace();
        }
        httpclient.getConnectionManager().shutdown(); //shut down the connection
        return new String(result); // for UTF-8 encoding;
    }

    String getbody(){
        return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:smar=\"http://smartsecure.co.in/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <smar:VisitorType_List/>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";
    }
}