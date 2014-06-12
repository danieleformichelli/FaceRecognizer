package com.eim.facesmanagement;

import com.eim.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class InsertNameDialog extends DialogFragment {
	private static final int ALERT_DIALOG_OK = 0;
	String oldName, name;
	OnClickListener okOnClickListener, cancelOnClickListener;

	public InsertNameDialog(String oldName, OnClickListener okOnClickListener,
			OnClickListener cancelOnClickListener) {
		this.oldName = oldName;
		this.okOnClickListener = okOnClickListener;
		this.cancelOnClickListener = cancelOnClickListener;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater().inflate(
				R.layout.dialog_insert_person, null);
		EditText editText = (EditText) v
				.findViewById(R.id.dialog_insert_person_name);
		editText.setText(oldName);
		editText.setSelection(editText.getText().length());
		editText.setImeActionLabel(getString(R.string.alert_dialog_ok),
				ALERT_DIALOG_OK);
		editText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == ALERT_DIALOG_OK) {
					dialogOkClickListener.onClick(getDialog(),
							DialogInterface.BUTTON_POSITIVE);
					getDialog().dismiss();
					return true;
				}

				return false;
			}
		});

		// TODO keyboard is not shown automatically

		return new AlertDialog.Builder(getActivity())
				.setIcon(R.drawable.action_insert_person)
				.setTitle(R.string.alert_dialog_insert_person)
				.setView(v)
				.setPositiveButton(R.string.alert_dialog_ok,
						dialogOkClickListener)
				.setNegativeButton(R.string.alert_dialog_cancel,
						cancelOnClickListener).create();
	}

	private DialogInterface.OnClickListener dialogOkClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			name = ((EditText) InsertNameDialog.this.getDialog().findViewById(
					R.id.dialog_insert_person_name)).getText().toString();
			okOnClickListener.onClick(dialog, which);
		}
	};

	public String getOldName() {
		return oldName;
	}

	public String getName() {
		return name;
	}
}
