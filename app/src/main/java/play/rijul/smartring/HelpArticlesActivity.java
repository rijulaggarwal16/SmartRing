package play.rijul.smartring;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

public class HelpArticlesActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help_articles);
		
		int opt = 1;
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			opt = extras.getInt("Option");
		}
		WebView myWebView = (WebView) findViewById(R.id.helpArticles);
		
		switch(opt){
		case 1: 
			myWebView.loadUrl("file:///android_asset/time_rules.html");
			break;
		case 2: 
			myWebView.loadUrl("file:///android_asset/white_list.html");
			break;
		case 3: 
			myWebView.loadUrl("file:///android_asset/emergency.html");
			break;
		case 4: 
			myWebView.loadUrl("file:///android_asset/text_reject.html");
			break;
		case 5: 
			myWebView.loadUrl("file:///android_asset/call_through.html");
			break;
		case 6: 
			myWebView.loadUrl("file:///android_asset/color_bars.html");
			break;
		}
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
