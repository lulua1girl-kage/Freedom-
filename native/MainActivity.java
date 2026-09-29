package com.kage.focus;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
public class MainActivity extends Activity {
 private WebView webView; private static final int AUDIO_REQUEST=42;
 @Override protected void onCreate(Bundle savedInstanceState){super.onCreate(savedInstanceState);webView=new WebView(this);setContentView(webView);WebSettings s=webView.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setDatabaseEnabled(true);s.setAllowFileAccess(true);s.setAllowContentAccess(true);s.setMediaPlaybackRequiresUserGesture(false);webView.addJavascriptInterface(new KageBridge(this),"KageNative");webView.setWebViewClient(new WebViewClient());webView.setWebChromeClient(new WebChromeClient(){@Override public void onPermissionRequest(final PermissionRequest request){runOnUiThread(()->{boolean wantsAudio=false;for(String resource:request.getResources())if(PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(resource))wantsAudio=true;if(wantsAudio&&checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},AUDIO_REQUEST);request.deny();}else request.grant(request.getResources());});}});webView.loadUrl("file:///android_asset/index.html");}
 @Override public void onBackPressed(){if(webView.canGoBack())webView.goBack();else super.onBackPressed();}
 @Override protected void onDestroy(){if(webView!=null)webView.destroy();super.onDestroy();}
}
