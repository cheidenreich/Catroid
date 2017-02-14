/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2016 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.catrobat.org/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.ui.fragment;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.catrobat.catroid.R;
import org.catrobat.catroid.ui.dialogs.DeleteItemDialog;
import org.catrobat.catroid.utils.ToastUtil;

public abstract class BackPackActivityFragment extends CheckBoxListFragment implements DeleteItemDialog.DeleteItemInterface {

	protected int itemIdentifier;
	protected int deleteDialogTitle;

	protected ActionMode.Callback unpackModeCallBack = new ActionMode.Callback() {

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			setSelectMode(ListView.CHOICE_MODE_MULTIPLE);

			mode.setTitle(R.string.unpack);

			actionModeTitle = getString(R.string.unpack);
			addSelectAllActionModeButton(mode, menu);

			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			if (adapter.getCheckedItems().isEmpty()) {
				clearCheckedItems();
			} else {
				unpackCheckedItems();
			}
		}
	};

	protected ActionMode.Callback deleteModeCallBack = new ActionMode.Callback() {

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			setSelectMode(ListView.CHOICE_MODE_MULTIPLE);

			actionModeTitle = getString(R.string.delete);

			mode.setTitle(actionModeTitle);
			addSelectAllActionModeButton(mode, menu);

			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			if (adapter.getCheckedItems().isEmpty()) {
				clearCheckedItems();
			} else {
				showDeleteDialog();
			}
		}
	};

	public void startUnpackActionMode() {
		startActionMode(unpackModeCallBack);
	}

	public void startDeleteActionMode() {
		startActionMode(deleteModeCallBack);
	}

	protected void startActionMode(ActionMode.Callback actionModeCallback) {
		if (isActionModeActive()) {
			return;
		}

		if (adapter.isEmpty()) {
			if (actionModeCallback.equals(unpackModeCallBack)) {
				ToastUtil.showError(getActivity(), R.string.nothing_to_unpack);
			} else if (actionModeCallback.equals(deleteModeCallBack)) {
				ToastUtil.showError(getActivity(), R.string.nothing_to_delete);
			}
		} else {
			actionMode = getActivity().startActionMode(actionModeCallback);
			unregisterForContextMenu(getListView());
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		if (adapter.isEmpty()) {
			menu.findItem(R.id.unpack).setVisible(false);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo);
		getActivity().getMenuInflater().inflate(R.menu.context_menu_backpack, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.unpack:
				unpackCheckedItems();
				break;
			case R.id.delete:
				showDeleteDialog();
				break;
			default:
				return super.onContextItemSelected(item);
		}
		return true;
	}

	@Override
	public void clearCheckedItems() {
		setSelectMode(ListView.CHOICE_MODE_NONE);
		adapter.setAllItemsCheckedTo(false);

		actionMode = null;
		registerForContextMenu(getListView());
	}

	@Override
	public void onItemChecked() {
		if (actionMode == null) {
			return;
		}
		updateActionModeTitle();
		updateSelectAllView();
	}

	protected void updateActionModeTitle() {
		int numberOfSelectedItems = adapter.getCheckedItems().size();

		if (numberOfSelectedItems == 0) {
			actionMode.setTitle(actionModeTitle);
			return;
		}

		String itemCount = Integer.toString(numberOfSelectedItems);
		ForegroundColorSpan colorSpan = new ForegroundColorSpan(getResources().getColor(R.color.actionbar_title_color));

		String completeTitle = actionModeTitle.concat(" ").concat(itemCount).concat(" ").concat(getResources()
				.getQuantityString(itemIdentifier, numberOfSelectedItems));

		Spannable completeSpannedTitle = new SpannableString(completeTitle);
		completeSpannedTitle.setSpan(colorSpan, actionModeTitle.length() + 1,
				actionModeTitle.length() + (1 + itemCount.length()), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		actionMode.setTitle(completeSpannedTitle);
	}

	protected abstract void unpackCheckedItems();

	protected void showDeleteDialog() {
		DeleteItemDialog dialog = new DeleteItemDialog(deleteDialogTitle, this);
		dialog.show(getFragmentManager(), DeleteItemDialog.DIALOG_FRAGMENT_TAG);
	}

	@Override
	public int getCheckedItemCount() {
		return adapter.getCheckedItems().size();
	}

	@Override
	public abstract void deleteCheckedItems();

	protected void checkEmptyBackgroundBackPack() {
		if (adapter.isEmpty()) {
			TextView emptyViewHeading = (TextView) getActivity().findViewById(R.id.backpack_text_heading);
			emptyViewHeading.setTextSize(TypedValue.COMPLEX_UNIT_SP, 60.0f);
			emptyViewHeading.setText(R.string.backpack);
			TextView emptyViewDescription = (TextView) getActivity().findViewById(R.id.backpack_text_description);
			emptyViewDescription.setText(R.string.is_empty);
		}
	}
}
