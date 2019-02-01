package play.rijul.smartring;

import java.util.HashMap;
import java.util.LinkedHashSet;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WhiteListActivity extends MainMenu {

	public static final int CONTACT_PICKER_RESULT = 16;
	private LinkedHashSet<View> whiteListContacts = new LinkedHashSet<View>();
	private DBAdapter whiteListDB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_white_list);
		
		openDB();
		
		initializeUI();
	}

	private void initializeUI() {
		Cursor c = whiteListDB.getWhiteList();
		if(c!=null && c.getCount()>0){
			for(int i=0;i<c.getCount();i++){
				insertNewContact(c.getString(DBAdapter.COL_NAME), c.getString(DBAdapter.COL_NUMBER));
				c.moveToNext();
			}
		}
	}

	public void getNewContact(View view) {
		Intent i = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
		startActivityForResult(i, CONTACT_PICKER_RESULT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case CONTACT_PICKER_RESULT:
				// handle contact results
				Uri result = data.getData();
				String id = result.getLastPathSegment();
				Cursor cursor = getContentResolver().query(Phone.CONTENT_URI,
						new String[] { Phone.DISPLAY_NAME, Phone.NUMBER },
						Phone.CONTACT_ID + "=?", new String[] { id }, null);
				int nameIdx = cursor.getColumnIndex(Phone.DISPLAY_NAME);
				int numIdx = cursor.getColumnIndex(Phone.NUMBER);
				if (cursor.moveToFirst()) {
					insertNewContact(cursor.getString(nameIdx),cursor.getString(numIdx));
				}
			}
		}
//		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void removeInfo() {
		View info = findViewById(R.id.info);
		if (info != null) {
			((RelativeLayout) info.getParent()).removeView(info);
		}
	}

	private void insertNewContact(String name, String number) {
		removeInfo();
		LinearLayout parent = (LinearLayout) findViewById(R.id.contactList);
		// Layout inflater
		LayoutInflater layoutInflater = getLayoutInflater();
		View view;

		view = layoutInflater.inflate(R.layout.white_list_item, parent,
				false);
		whiteListContacts.add(view);
		ImageView discard = (ImageView) view.findViewById(R.id.contactDelete);
		discard.setOnClickListener(deleteCont);
		((TextView)view.findViewById(R.id.contactName)).setText(name);
		((TextView)view.findViewById(R.id.contactNumber)).setText(number);
		
		parent.addView(view);
		Animation anim = AnimationUtils.makeInAnimation(this, true);
		view.startAnimation(anim);
	}
	
	OnClickListener deleteCont = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Animation anim = AnimationUtils.makeOutAnimation(
					WhiteListActivity.this, false);
			View row = (View) v.getParent();
			row.startAnimation(anim);
			((LinearLayout) row.getParent()).removeView(row);
			whiteListContacts.remove(row);
		}
	};
	
	public void cancelList(View view){
		finish();
	}
	
	public void saveList(View view){
		whiteListDB.deleteAllWhiteList();
		HashMap<String,String> contacts = new HashMap<String,String>();
		for(View cont : whiteListContacts){
			String name = ((TextView)cont.findViewById(R.id.contactName)).getText().toString();
			String number = ((TextView)cont.findViewById(R.id.contactNumber)).getText().toString();
			contacts.put(number, name);
		}
		if(!contacts.isEmpty()){
			whiteListDB.insertWhiteList(contacts);
		}
		
		shortToast("Successfully saved the White List");
		finish();
	}
	
	private void shortToast(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		closeDB();
	}

	private void openDB() {
		whiteListDB = new DBAdapter(this);
		whiteListDB.open();
	}

	private void closeDB() {
		whiteListDB.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		super.onCreateOptionsMenu(menu);
		menu.removeItem(R.id.action_settings);
		return true;
	}

}
