package play.rijul.smartring;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class HelpActivity extends Activity {

	Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		intent = new Intent(HelpActivity.this, HelpArticlesActivity.class);
	}

	public void timeSetting(View v) {
		intent.putExtra("Option", 1);
		startActivity(intent);
	}
	
	public void whiteList(View v){
		intent.putExtra("Option", 2);
		startActivity(intent);
	}
	
	public void emergency(View v){
		intent.putExtra("Option", 3);
		startActivity(intent);
	}
	
	public void textReject(View v){
		intent.putExtra("Option", 4);
		startActivity(intent);
	}
	
	public void throughCall(View v){
		intent.putExtra("Option", 5);
		startActivity(intent);
	}
	
	public void colorBars(View v){
		intent.putExtra("Option", 6);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.about, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
 
        case R.id.action_about:
            Intent i = new Intent(this, AboutActivity.class);
            startActivity(i);
            break;
        }
        return true;
    }

}
