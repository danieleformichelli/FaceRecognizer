package com.eim.facesmanagement;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.eim.R;

public class EditPersonDialog extends DialogFragment {
	String oldName, name;
	OnClickListener okOnClickListener, deleteOnClickListener,
			cancelOnClickListener;

	public EditPersonDialog(String oldName, OnClickListener okOnClickListener,
			OnClickListener deleteOnClickListener,
			OnClickListener cancelOnClickListener) {
		this.oldName = oldName;
		this.okOnClickListener = okOnClickListener;
		this.deleteOnClickListener = deleteOnClickListener;
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

		// remove comment to enable IME action
		// editText.setImeActionLabel(getString(R.string.alert_dialog_ok),
		// ALERT_DIALOG_OK);
		// editText.setOnEditorActionListener(new OnEditorActionListener() {
		// @Override
		// public boolean onEditorAction(TextView v, int actionId,
		// KeyEvent event) {
		// if (actionId == ALERT_DIALOG_OK) {
		// dialogOnClickListener.onClick(getDialog(),
		// DialogInterface.BUTTON_POSITIVE);
		// getDialog().dismiss();
		// return true;
		// }
		//
		// return false;
		// }
		// });

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
				.setIcon(R.drawable.action_insert_person)
				.setTitle(R.string.alert_dialog_insert_person).setView(v);

		if (okOnClickListener != null)
			builder.setPositiveButton(R.string.alert_dialog_ok,
					dialogOnClickListener);

		if (deleteOnClickListener != null)
			builder.setNeutralButton(R.string.alert_dialog_delete,
					dialogOnClickListener);

		if (cancelOnClickListener != null)
			builder.setNegativeButton(R.string.alert_dialog_cancel,
					dialogOnClickListener);

		return builder.create();
	}

	private DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// store the new name so the listener can get it
			name = ((EditText) EditPersonDialog.this.getDialog().findViewById(
					R.id.dialog_insert_person_name)).getText().toString();

			DialogInterface.OnClickListener onClickListener;
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				onClickListener = okOnClickListener;
				break;
			case DialogInterface.BUTTON_NEUTRAL:
				onClickListener = deleteOnClickListener;
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				onClickListener = cancelOnClickListener;
				break;
			default:
				return;
			}
			onClickListener.onClick(dialog, which);
		}
	};

	public String getInsertedName() {
		return name;
	}
}
