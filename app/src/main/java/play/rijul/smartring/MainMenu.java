package play.rijul.smartring;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainMenu extends FragmentActivity {
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
 
        case R.id.action_settings:
            Intent i = new Intent(this, Preferences.class);
            startActivity(i);
            break;
        case R.id.action_help:
        	Intent in = new Intent(this, HelpActivity.class);
            startActivity(in);
            break;
 
        }
 
        return true;
    }

}
