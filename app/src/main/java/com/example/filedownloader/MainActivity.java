package com.example.filedownloader;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final Pattern HTTPS_PATTERN = Pattern.compile("https://.+");

    TextView mRozmiarTextView;
    TextView mTypTextView;
    Button mGetInfoBtn;
    Button mDownloadBtn;
    EditText mInputLink;

    DownloadManager downloadmanager;

    Boolean dataRecieved;


    final static String TEST_LINK = "https://cdn.kernel.org/pub/linux/kernel/v5.x/linux-5.6.15.tar.xz";

    final static int PERM_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRozmiarTextView = findViewById(R.id.size);
        mTypTextView = findViewById(R.id.type);
        mGetInfoBtn = findViewById(R.id.getInfo);
        mDownloadBtn = findViewById(R.id.download);
        mInputLink = findViewById(R.id.inputTextField);

        mInputLink.setText(TEST_LINK);

        dataRecieved = false;


        setListeners();
    }

    private void setListeners() {

        mGetInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String mLink = mInputLink.getText().toString();

                if(validate(mLink)){

                    GetUrlDataTask task = new GetUrlDataTask(mLink);
                    task.execute();
                }
            }
        });

        mDownloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String mLink = mInputLink.getText().toString();

                if( validate(mLink) ){

                    if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED){

                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions( permissions, PERM_CODE);
                        ///perm denied
                    } else {

                        download(mLink);
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode){

            case PERM_CODE:{

                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "Posiadam uprawienia", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Brak uprawnien", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void download(String link){

            Toast.makeText(getBaseContext(), "Zaczynam pobieranie", Toast.LENGTH_SHORT).show();

            downloadmanager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(link);

            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle("My File");
            request.setDescription("Downloading");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setVisibleInDownloadsUi(false);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, ""+System.currentTimeMillis() );

            downloadmanager.enqueue(request);

            progress();

    }

    Boolean validate(String link){

        if( !HTTPS_PATTERN.matcher(link).matches() ){

            Toast.makeText(MainActivity.this, "LINK MUST BE FORMATED 'https://...'", Toast.LENGTH_LONG).show();
            return false;
        } else if( link.isEmpty() ){

            Toast.makeText(MainActivity.this, "LINK CANNOT BE EMPTY", Toast.LENGTH_LONG).show();
            return false;
        } else {

            return true;
        }
    }

    protected class GetUrlDataTask extends AsyncTask<Void, Void, Void>
    {
        private String url;

        public GetUrlDataTask(String url) {

            this.url = url;
        }

        @SuppressLint("WrongThread")
        @Override
        protected Void doInBackground(Void... params)
        {
            String str = url;

            HttpURLConnection polaczenie = null;
            try {
                URL url = new URL(str);
                polaczenie = (HttpURLConnection) url.openConnection();
                //polaczenie.setRequestMethod("GET");
                //polaczenie.setDoOutput(true);
                int mRozmiar = polaczenie.getContentLength();
                String mTyp = polaczenie.getContentType();

                mRozmiarTextView.setText( String.valueOf(mRozmiar) );
                mTypTextView.setText(mTyp);

                dataRecieved = true;

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (polaczenie != null) polaczenie.disconnect();
            }
            return null;
        }

    }

    void progress(){

        new Thread(new Runnable() {

            @Override
            public void run() {

                boolean downloading = true;

                final ProgressBar pb = findViewById(R.id.progressBar2);

                while (downloading) {

                    DownloadManager.Query q = new DownloadManager.Query();
                    Cursor cursor = downloadmanager.query(q);
                    cursor.moveToFirst();

                    int bytes_downloaded = cursor.getInt(cursor
                            .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)); //Na emulatorze zwraca 0 z niewiadomego dla mnie powodu na moim urządzeniu działa prawidłowo

                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false;
                    }

                    final int dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            pb.setProgress((int) dl_progress);

                        }
                    });

                    cursor.close();
                }

            }
        }).start();
    }

}
