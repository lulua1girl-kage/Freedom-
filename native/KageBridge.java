package com.kage.focus;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
public final class KageBridge {
 private final Context context; private final SharedPreferences prefs;
 KageBridge(Context context){this.context=context.getApplicationContext();this.prefs=this.context.getSharedPreferences("kage_native_blocker",Context.MODE_PRIVATE);}
 @JavascriptInterface public boolean startProtection(String packagesCsv,long endAtMillis){
  if(!KageBlockerService.isAccessibilityEnabled(context))return false;
  Set<String> packages=new HashSet<>(); if(!TextUtils.isEmpty(packagesCsv))packages.addAll(Arrays.asList(packagesCsv.split(","))); packages.remove("");
  prefs.edit().putBoolean("active",true).putLong("endAt",endAtMillis).putStringSet("packages",packages).apply(); KageBlockerService.requestRefresh(); return true;
 }
 @JavascriptInterface public void stopProtection(){prefs.edit().putBoolean("active",false).remove("endAt").remove("packages").apply();KageBlockerService.requestRefresh();}
 @JavascriptInterface public void openAccessibilitySettings(){try{Intent i=new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);context.startActivity(i);}catch(Exception ignored){}}
 @JavascriptInterface public boolean isAccessibilityEnabled(){return KageBlockerService.isAccessibilityEnabled(context);}
}
