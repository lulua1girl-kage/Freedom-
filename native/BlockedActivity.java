package com.kage.focus;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
public class BlockedActivity extends Activity {
 @Override protected void onCreate(Bundle state){super.onCreate(state);render();}
 @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);render();}
 private void render(){
  LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setGravity(Gravity.CENTER);root.setPadding(48,48,48,48);root.setBackgroundColor(Color.rgb(8,8,8));
  TextView title=new TextView(this);title.setText("FOCUS SESSION ACTIVE");title.setTextColor(Color.rgb(57,255,136));title.setTextSize(24);title.setGravity(Gravity.CENTER);
  TextView message=new TextView(this);message.setText("This app is protected until your KAGE Focus session ends.");message.setTextColor(Color.WHITE);message.setTextSize(16);message.setGravity(Gravity.CENTER);message.setPadding(0,24,0,32);
  Button back=new Button(this);back.setText("RETURN TO KAGE");back.setOnClickListener(v->{Intent i=new Intent(this,MainActivity.class);i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);startActivity(i);finish();});
  root.addView(title);root.addView(message);root.addView(back);setContentView(root);
 }
}
