package com.boogersoft.intlprefix;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class ProfileManagerActivity extends FragmentActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_manager_activity);

		boolean save = this.getIntent().getBooleanExtra("SAVE", false);
		if(save)
			this.setTitle(getString(R.string.cat_ProfileManager_Save_Title));
		else
			this.setTitle(getString(R.string.cat_ProfileManager_Load_Title));
	}

//	@Override
//	protected void onResume()
//	{
//		super.onResume();
//
//	}
}
