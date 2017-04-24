package com.xu.jialu.sandboxgo;

/**
 * Created by karlenz on 4/24/17.
 */

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by karlenz on 4/24/17.
 */

public class WebLookUp extends AppCompatActivity {
    private TextView urlLink;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webpage);
        urlLink = (TextView) findViewById(R.id.urllink);
        urlLink.setText("http://cis.bentley.edu/sandbox/");
        webView = (WebView) findViewById(R.id.webpage);
        webView.getSettings().setJavaScriptEnabled(true);

        //intercept URL loading and load in widget
        webView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

        });
        webView.loadUrl(urlLink.getText().toString());

    }

    //the back key navigates back to the previous web page
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        else {
            //set up the toast when the web lookup finishes
            Toast.makeText(this, "WebLookup Finishes", Toast.LENGTH_LONG).show();
        }


        return super.onKeyDown(keyCode, event);
    }
}




