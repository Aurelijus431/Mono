//Application to connect to Mono Simulator
package dk.ucn.aurelijus.connecttoserver;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    TextView textResponse, textTemp;
    EditText editTextAddress, editTextPort;
    Button buttonConnect, buttonClear, buttonReceive;
    String command = "*t\n\r";
    OutputStreamWriter osw;
    Socket socket = null;
    String dstAddress;
    int dstPort;
    String response = "";
    String response2 = "lol";
    byte[] bytesRead;
    String temperature;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextAddress = (EditText)findViewById(R.id.address);
        editTextPort = (EditText)findViewById(R.id.port);
        buttonConnect = (Button)findViewById(R.id.connect);
        buttonClear = (Button)findViewById(R.id.clear);
        buttonReceive = (Button)findViewById(R.id.receive);
        textResponse = (TextView)findViewById(R.id.response);
        textTemp = (TextView)findViewById(R.id.temp);
        buttonConnect.setOnClickListener(buttonConnectOnClickListener);
        buttonReceive.setOnClickListener(buttonReceiveOnClickListener);
        buttonClear.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                textResponse.setText("");
            }});
    }
/////////////////////////////////////////////////////////////////////////////////////
    //Reacts to a button click.
    OnClickListener buttonConnectOnClickListener =
            new OnClickListener(){

                @Override
                public void onClick(View arg0) //Reads down the information written from text inputs.
                {
                    MyClientTask myClientTask = new MyClientTask(editTextAddress.getText().toString(),
                    Integer.parseInt(editTextPort.getText().toString()));
                    myClientTask.execute();
                }};
/////////////////////////////////////////////////////////////////////////////////////
    OnClickListener buttonReceiveOnClickListener =
        new OnClickListener(){
                @Override
                public void onClick(View arg0) //Reads down the information written from text inputs.
                {
                    if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
                        new MyClientTask2().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        new MyClientTask2().execute();
                    }
                }};
/////////////////////////////////////////////////////////////////////////////////////
    public class MyClientTask extends AsyncTask<Void, Void, Void> {



        MyClientTask(String addr, int port) //Applies the received inputs to local variables for manipulation.
        {
            dstAddress = addr;
            dstPort = port;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            try {
                socket = new Socket(dstAddress, dstPort);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];
                int bytesRead;
                InputStream inputStream = socket.getInputStream();
                while ((bytesRead = inputStream.read(buffer)) != -1)
                   {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    response += byteArrayOutputStream.toString("UTF-16");
                   }

            }
            catch (UnknownHostException e)
            {
                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                response = "IOException: " + e.toString();
            }
            finally
            {

                if(socket != null)
                {
                    try
                    {

                        socket.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
/////////////////////////////////////////////////////////////////////////////////////////
        @Override
        protected void onPostExecute(Void result) {
            textResponse.setText(response);
            super.onPostExecute(result);
        }

       // public byte[] stringToBytesASCII(String str) {
       // byte[] b = new byte[str.length()];
       // for (int i = 0; i < b.length; i++) {
       //     b[i] = (byte) str.charAt(i);
       // }
       // return b;
       // }
/////////////////////////////////////////////////////////////////////////////////////////


    }

    public class MyClientTask2 extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                //byte[] b = stringToBytesASCII(command);
                byte[] cmd = command.getBytes("US-ASCII");

                dout.write(cmd);

                dout.close();

                byte[] resultBuff = new byte[0];
                byte[] buff = new byte[1024];
                int k = -1;
                while((k = socket.getInputStream().read(buff, 0, buff.length)) > -1) {
                    byte[] tbuff = new byte[resultBuff.length + k]; // temp buffer size = bytes already read + bytes last read
                    System.arraycopy(resultBuff, 0, tbuff, 0, resultBuff.length); // copy previous bytes
                    System.arraycopy(buff, 0, tbuff, resultBuff.length, k);  // copy current lot
                    resultBuff = tbuff; // call the temp buffer as your result buff
                }
                System.out.println(resultBuff.length + " bytes read.");

                temperature = convert(resultBuff);

             //   BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             //   String line;
             //   StringBuilder builder = new StringBuilder();
             //   while ((line = reader.readLine()) != null) {
             //       builder.append(line);
             //   }
             //   temperature = builder.toString();


            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {

                if(socket != null)
                {
                    try
                    {
                        socket.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            return null;
            }


        @Override
        protected void onPostExecute(Void result) {
            response2 = temperature;
            textTemp.setText(response2);
            super.onPostExecute(result);
        }

        String convert(byte[] data) {
            StringBuilder sb = new StringBuilder(data.length);
            for (int i = 0; i < data.length; ++ i) {
                if (data[i] < 0) throw new IllegalArgumentException();
                sb.append((char) data[i]);
            }
            return sb.toString();
        }

    }

}