package com.example.filedownloader;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    TextView mRozmiarTextView;
    TextView mTypTextView;
    Button mGetInfoBtn;
    Button mDownloadBtn;
    EditText mInputLink;

    final static int PERM_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRozmiarTextView = findViewById(R.id.rozmiar);
        mTypTextView = findViewById(R.id.typ);
        mGetInfoBtn = findViewById(R.id.getInfo);
        mDownloadBtn = findViewById(R.id.download);
        mInputLink = findViewById(R.id.inputTextField);

        setListeners();
    }

    private void setListeners() {

        mGetInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String mLink = mInputLink.getText().toString();

                ///VALIDACJA

                GetUrlDataTask task = new GetUrlDataTask(mLink);
                task.execute();
            }
        });

        mDownloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED){

                    String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    requestPermissions( permissions, PERM_CODE);
                    ///perm denied
                } else {

                    Toast.makeText(getBaseContext(), "Zaczynam pobieranie", Toast.LENGTH_SHORT).show();
                    download();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode){

            case PERM_CODE:{

                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    download();
                    Toast.makeText(this, "Zaczynam pobieranie", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Brak uprawnien", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void download(){

        DownloadManager downloadmanager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse("https://cdn.kernel.org/pub/linux/kernel/v5.x/linux-5.6.15.tar.xz");

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle("My File");
        request.setDescription("Downloading");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setVisibleInDownloadsUi(false);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, ""+System.currentTimeMillis() );

        downloadmanager.enqueue(request);
    }

    protected class GetUrlDataTask extends AsyncTask<Void, Void, URL>
    {
        private String url;

        public GetUrlDataTask(String url) {

            this.url = url;
        }

        @SuppressLint("WrongThread")
        @Override
        protected URL doInBackground(Void... params)
        {
            String str = url;

            HttpURLConnection polaczenie = null;
            try {
                URL url = new URL(str);
                polaczenie = (HttpURLConnection) url.openConnection();
                polaczenie.setRequestMethod("GET");
                polaczenie.setDoOutput(true);
                String mRozmiar = polaczenie.getResponseMessage();
                String mTyp = polaczenie.getContentType();

                mRozmiarTextView.setText( mRozmiar );
                mTypTextView.setText(mTyp);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (polaczenie != null) polaczenie.disconnect();
            }


            return null;
        }

    }
}
