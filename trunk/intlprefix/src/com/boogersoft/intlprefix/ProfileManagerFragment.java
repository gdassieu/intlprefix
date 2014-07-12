package com.boogersoft.intlprefix;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileManagerFragment extends ListFragment
	implements LoaderCallbacks<Cursor>
{
	private EditText txtProfileName;
	private ImageButton btnLoad;
	private ImageButton btnSave;
	private ImageButton btnDiscard;

	SimpleCursorAdapter adapter;

//	@Override
//    public void onCreate(Bundle savedInstanceState)
//	{
//		super.onCreate(savedInstanceState);
//	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.profile_manager_fragment,
			container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		txtProfileName = (EditText)getActivity().findViewById(R.id.profileName);
		btnLoad = (ImageButton)getActivity().findViewById(R.id.buttonLoad);
		btnSave = (ImageButton)getActivity().findViewById(R.id.buttonSave);
		btnDiscard = (ImageButton)getActivity().findViewById(
			R.id.buttonDiscard);

		boolean save = getActivity().getIntent().getBooleanExtra("SAVE", false);
		if(save)
		{
			txtProfileName.setText(Preferences.getProfileName(getActivity()));
			btnLoad.setVisibility(View.GONE);
			btnSave.setVisibility(View.VISIBLE);
			btnDiscard.setVisibility(View.GONE);
		}
		else
		{
			btnLoad.setVisibility(View.VISIBLE);
			btnSave.setVisibility(View.GONE);
			btnDiscard.setVisibility(View.VISIBLE);
		}

		txtProfileName.addTextChangedListener(new TextWatcher()
		{	
			@Override
			public void onTextChanged(
				CharSequence s, int start, int before, int count)
			{
			}

			@Override
			public void beforeTextChanged(
				CharSequence s, int start, int count, int after)
			{
			}

			@Override
			public void afterTextChanged(Editable s)
			{
				btnSave.setEnabled(s.length() != 0);
				getLoaderManager().restartLoader(0, null,
					ProfileManagerFragment.this);
			}
		});

		adapter = new SimpleCursorAdapter(getActivity(),
			android.R.layout.simple_list_item_1, null,
			new String[] {MainContentProvider.Profile.COL_NAME},
			new int[] {android.R.id.text1}, 0);
		setListAdapter(adapter);
		getLoaderManager().initLoader(0, null, this);

		btnSave.setOnClickListener(new OnClickListener()
		{	
			@Override
			public void onClick(View v)
			{
				saveProfile(true);
			}
		});

		btnLoad.setOnClickListener(new OnClickListener()
		{	
			@Override
			public void onClick(View v)
			{
				loadProfile();
			}
		});

		btnDiscard.setOnClickListener(new OnClickListener()
		{	
			@Override
			public void onClick(View v)
			{
				discardProfile(true);
			}
		});
	}

//	@Override
//	public void onResume()
//	{
//		super.onResume();
//	}

	public static class YesNoDialogFragment
		extends DialogFragment
	{
		public interface Callback
		{
			public void onYesButtonClicked();
		}

		String text = "(null)";
		Callback callback = null;

		public void initialize(String text, Callback callback)
		{
			this.text = text;
			this.callback = callback;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(
				getActivity());
			builder.setMessage(text);
			builder.setPositiveButton(R.string.button_yes,
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						if(callback != null)
							callback.onYesButtonClicked();
					}
				});
			builder.setNegativeButton(R.string.button_no,
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
					}
				});
			return builder.create();
		}
	}

	private void saveProfile(boolean showConfirmationDialog)
	{
		String profileName = txtProfileName.getText().toString();
		if(profileName.length() == 0)
			return;

		boolean exists = getActivity().getContentResolver().query(
			MainContentProvider.Profile.CONTENT_URI,
			new String[] {MainContentProvider.Profile.COL_NAME},
			MainContentProvider.Profile.COL_NAME + " = ?",
			new String[] {profileName}, null).getCount() > 0;

		if(exists && showConfirmationDialog && !profileName.equals(
			Preferences.getProfileName(getActivity())))
		{
			YesNoDialogFragment dialog = new YesNoDialogFragment();
			dialog.initialize(
				getString(R.string.text_confirmOverwriteProfile, profileName),
				new YesNoDialogFragment.Callback()
				{
					@Override
					public void onYesButtonClicked()
					{
						saveProfile(false);
					}
				});
			dialog.show(getFragmentManager(),
				"ConfirmOverwriteProfileDialogFragment");
		}
		else
		{
			ContentValues values = new ContentValues();
			values.put(MainContentProvider.Profile.COL_NAME, profileName);
			values.put(MainContentProvider.Profile.COL_COUNTRY_CODE,
				Preferences.getCurrentCountryCode(getActivity()));
			values.put(MainContentProvider.Profile.COL_ADD_DOM_PREFIX,
				Preferences.getConvertToLocal(getActivity()));
			values.put(MainContentProvider.Profile.COL_DOM_PREFIX,
				Preferences.getLocalPrefix(getActivity()));
			values.put(MainContentProvider.Profile.COL_ADD_INTL_PREFIX,
				Preferences.getAddIntlPrefix(getActivity()));
			values.put(MainContentProvider.Profile.COL_INTL_PREFIX,
				Preferences.getIntlPrefix(getActivity()));

			if(exists)
			{
				getActivity().getContentResolver().update(
					MainContentProvider.Profile.CONTENT_URI, values,
					MainContentProvider.Profile.COL_NAME + " = ?",
					new String[] {profileName});
			}
			else
			{
				getActivity().getContentResolver().insert(
					MainContentProvider.Profile.CONTENT_URI, values);
			}

			Preferences.setProfileName(getActivity(), profileName);
			Preferences.setProfileDirty(getActivity(), false);

			getActivity().finish();
			Toast.makeText(getActivity(),
				getString(R.string.text_profileSaved, profileName),
				Toast.LENGTH_SHORT).show();
		}
	}

	private void loadProfile()
	{
		String profileName = txtProfileName.getText().toString();
		Cursor c = getActivity().getContentResolver().query(
			MainContentProvider.Profile.CONTENT_URI,
			new String[]
			{
				MainContentProvider.Profile.COL_COUNTRY_CODE,
				MainContentProvider.Profile.COL_ADD_DOM_PREFIX,
				MainContentProvider.Profile.COL_DOM_PREFIX,
				MainContentProvider.Profile.COL_ADD_INTL_PREFIX,
				MainContentProvider.Profile.COL_INTL_PREFIX
			},
			MainContentProvider.Profile.COL_NAME + " = ?",
			new String[] {profileName}, null);

		if(c.getCount() > 0)
		{
			c.moveToFirst();

			String countryCode = c.getString(c.getColumnIndexOrThrow(
				MainContentProvider.Profile.COL_COUNTRY_CODE));
			int addDomPrefix = c.getInt(c.getColumnIndexOrThrow(
				MainContentProvider.Profile.COL_ADD_DOM_PREFIX));
			String domPrefix = c.getString(c.getColumnIndexOrThrow(
				MainContentProvider.Profile.COL_DOM_PREFIX));
			int addIntlPrefix = c.getInt(c.getColumnIndexOrThrow(
				MainContentProvider.Profile.COL_ADD_INTL_PREFIX));
			String intlPrefix = c.getString(c.getColumnIndexOrThrow(
				MainContentProvider.Profile.COL_INTL_PREFIX));

			Preferences.setProfileName(getActivity(), profileName);
			Preferences.setCurrentCountryCode(getActivity(), countryCode);
			Preferences.setConvertToLocal(getActivity(), addDomPrefix != 0);
			Preferences.setLocalPrefix(getActivity(), domPrefix);
			Preferences.setAddIntlPrefix(getActivity(), addIntlPrefix != 0);
			Preferences.setIntlPrefix(getActivity(), intlPrefix);
			Preferences.setProfileDirty(getActivity(), false);

			getActivity().finish();
			Toast.makeText(getActivity(),
				getString(R.string.text_profileLoaded, profileName),
				Toast.LENGTH_SHORT).show();
		}
	}

	private void discardProfile(boolean showConfirmationDialog)
	{
		String profileName = txtProfileName.getText().toString();

		boolean exists = getActivity().getContentResolver().query(
			MainContentProvider.Profile.CONTENT_URI,
			new String[] {MainContentProvider.Profile.COL_NAME},
			MainContentProvider.Profile.COL_NAME + " = ?",
			new String[] {profileName}, null).getCount() > 0;

		if(exists)
		{
			if(showConfirmationDialog)
			{
				YesNoDialogFragment dialog = new YesNoDialogFragment();
				dialog.initialize(
					getString(R.string.text_confirmDiscardProfile, profileName),
					new YesNoDialogFragment.Callback()
					{
						@Override
						public void onYesButtonClicked()
						{
							discardProfile(false);
						}
					});
				dialog.show(getFragmentManager(),
					"ConfirmDiscardProfileDialogFragment");
			}
			else
			{
				getActivity().getContentResolver().delete(
					MainContentProvider.Profile.CONTENT_URI,
					MainContentProvider.Profile.COL_NAME + " = ?",
					new String[] {profileName});

				if(profileName.equals(
					Preferences.getProfileName(getActivity())))
				{
					Preferences.setProfileName(getActivity(), null);
					Preferences.setProfileDirty(getActivity(), true);
				}

				getActivity().finish();
				Toast.makeText(getActivity(),
					getString(R.string.text_profileDiscarded, profileName),
					Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args)
	{
		return new CursorLoader(getActivity(),
			MainContentProvider.Profile.CONTENT_URI,
			new String[] {MainContentProvider.Profile.COL_ID,
				MainContentProvider.Profile.COL_NAME},
			MainContentProvider.Profile.COL_NAME + " LIKE ?",
			new String[] {"%" + txtProfileName.getText().toString() + "%"},
			null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data)
	{
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{
		adapter.swapCursor(null);
	}

	@Override 
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		txtProfileName.setText(((TextView)v).getText());
	}
}
