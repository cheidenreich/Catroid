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

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.common.LookInfo;
import org.catrobat.catroid.io.StorageHandler;
import org.catrobat.catroid.io.backpack.BackpackLookController;
import org.catrobat.catroid.ui.adapter.CheckBoxListAdapter;
import org.catrobat.catroid.ui.adapter.LookListAdapter;
import org.catrobat.catroid.ui.controller.BackPackListManager;
import org.catrobat.catroid.utils.ToastUtil;

import java.io.IOException;
import java.util.List;

public class BackPackLookListFragment extends BackPackActivityFragment implements CheckBoxListAdapter
		.ListItemClickHandler<LookInfo>, CheckBoxListAdapter.ListItemLongClickHandler {

	public static final String TAG = BackPackLookListFragment.class.getSimpleName();
	public static final String SHARED_PREFERENCE_NAME = "showLookDetails";

	private LookListAdapter lookAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_backpack, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		registerForContextMenu(getListView());
		itemIdentifier = R.plurals.look;
		deleteDialogTitle = R.plurals.delete_dialog_look;

		initializeList();
		checkEmptyBackgroundBackPack();
	}

	private void initializeList() {
		List<LookInfo> lookList = BackPackListManager.getInstance().getBackPackedLooks();

		lookAdapter = new LookListAdapter(getActivity(), R.layout.list_item, lookList);
		setListAdapter(lookAdapter);
		lookAdapter.setListItemClickHandler(this);
		lookAdapter.setListItemCheckHandler(this);
		lookAdapter.setListItemLongClickHandler(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		loadShowDetailsPreferences(SHARED_PREFERENCE_NAME);
	}

	@Override
	public void onPause() {
		super.onPause();
		BackPackListManager.getInstance().saveBackpack();
		saveCurrentProject();
		putShowDetailsPreferences(SHARED_PREFERENCE_NAME);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		menu.setHeaderTitle(lookAdapter.getCheckedItems().get(0).getName());
		super.onCreateContextMenu(menu, view, menuInfo);
	}

	@Override
	public void handleOnItemClick(int position, View view, LookInfo listItem) {
		lookAdapter.setAllItemsCheckedTo(false);
		lookAdapter.addToCheckedItems(listItem);
		getListView().showContextMenuForChild(view);
	}

	@Override
	public void handleOnItemLongClick(int position, View view) {
		LookInfo listItem = lookAdapter.getItem(position);
		lookAdapter.setAllItemsCheckedTo(false);
		lookAdapter.addToCheckedItems(listItem);
		getListView().showContextMenuForChild(view);
	}

	@Override
	public void deleteCheckedItems() {
		for (LookInfo lookInfo : lookAdapter.getCheckedItems()) {
			if (StorageHandler.deleteFile(lookInfo.getAbsolutePath())) {
				lookAdapter.remove(lookInfo);
			} else {
				ToastUtil.showError(getActivity(), R.string.error_delete_look);
			}
		}
		clearCheckedItems();
	}

	@Override
	protected void unpackCheckedItems() {
		for (LookInfo LookInfo : lookAdapter.getCheckedItems()) {
			try {
				LookInfo unpackedLookInfo = BackpackLookController.unpack(LookInfo);
				ProjectManager.getInstance().getCurrentSprite().getLookInfoList().add(unpackedLookInfo);
			} catch (IOException e) {
				ToastUtil.showError(getActivity(), R.string.error_unpack_look);
			}
		}
		clearCheckedItems();
	}
}
