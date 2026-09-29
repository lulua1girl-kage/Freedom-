package com.kage.focus;
import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import java.util.HashSet;
import java.util.Set;
public class KageBlockerService extends AccessibilityService {
 private static KageBlockerService instance; private SharedPreferences prefs;
 public static void requestRefresh(){if(instance!=null)instance.refresh();}
 public static boolean isAccessibilityEnabled(Context context){
  String enabled=Settings.Secure.getString(context.getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES); if(TextUtils.isEmpty(enabled))return false;
  String target=new ComponentName(context,KageBlockerService.class).flattenToString(); for(String v:enabled.split(":"))if(target.equalsIgnoreCase(v))return true; return false;
 }
 @Override public void onServiceConnected(){super.onServiceConnected();instance=this;prefs=getSharedPreferences("kage_native_blocker",MODE_PRIVATE);}
 @Override public void onAccessibilityEvent(AccessibilityEvent event){
  if(event==null||event.getPackageName()==null)return; refresh(); if(!isProtectionActive())return; String pkg=String.valueOf(event.getPackageName());
  if(blockedPackages().contains(pkg)&&!pkg.equals(getPackageName()))redirectToBlockedScreen(pkg);
 }
 private void refresh(){if(prefs==null)prefs=getSharedPreferences("kage_native_blocker",MODE_PRIVATE);if(prefs.getBoolean("active",false)&&System.currentTimeMillis()>=prefs.getLong("endAt",0L))prefs.edit().putBoolean("active",false).remove("endAt").remove("packages").apply();}
 private boolean isProtectionActive(){return prefs!=null&&prefs.getBoolean("active",false)&&System.currentTimeMillis()<prefs.getLong("endAt",0L);}
 private Set<String> blockedPackages(){return new HashSet<>(prefs.getStringSet("packages",new HashSet<>()));}
 private void redirectToBlockedScreen(String packageName){Intent i=new Intent(this,BlockedActivity.class);i.putExtra("blocked_package",packageName);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);try{startActivity(i);}catch(Exception ignored){}}
 @Override public void onInterrupt(){}
 @Override public void onDestroy(){if(instance==this)instance=null;super.onDestroy();}
}
