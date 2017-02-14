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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.io.backpack.BackpackSpriteController;
import org.catrobat.catroid.common.LookInfo;
import org.catrobat.catroid.common.SoundInfo;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.io.StorageHandler;
import org.catrobat.catroid.ui.adapter.CheckBoxListAdapter;
import org.catrobat.catroid.ui.adapter.SpriteListAdapter;
import org.catrobat.catroid.ui.controller.BackPackListManager;
import org.catrobat.catroid.ui.controller.LookController;
import org.catrobat.catroid.ui.dialogs.CustomAlertDialogBuilder;
import org.catrobat.catroid.utils.ToastUtil;

import java.io.IOException;
import java.util.List;

public class BackPackSpriteListFragment extends BackPackActivityFragment implements CheckBoxListAdapter
		.ListItemClickHandler<Sprite>, CheckBoxListAdapter.ListItemLongClickHandler {

	public static final String TAG = BackPackSpriteListFragment.class.getSimpleName();
	public static final String SHARED_PREFERENCE_NAME = "showSpriteDetails";
	private static final String BUNDLE_ARGUMENTS_ITEM_TO_EDIT = "spriteToEdit";

	private SpriteListAdapter spriteAdapter;
	private Sprite spriteToEdit;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_backpack, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		registerForContextMenu(getListView());
		itemIdentifier = R.plurals.sprite;
		deleteDialogTitle = R.plurals.delete_dialog_sprite;

		if (savedInstanceState != null) {
			spriteToEdit = (Sprite) savedInstanceState.get(BUNDLE_ARGUMENTS_ITEM_TO_EDIT);
		}

		initializeList();
		checkEmptyBackgroundBackPack();
	}

	private void initializeList() {
		List<Sprite> spriteList = BackPackListManager.getInstance().getBackPackedSprites();

		spriteAdapter = new SpriteListAdapter(getActivity(), R.layout.list_item, spriteList);

		setListAdapter(spriteAdapter);
		spriteAdapter.setListItemClickHandler(this);
		spriteAdapter.setListItemCheckHandler(this);
		spriteAdapter.setListItemLongClickHandler(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(BUNDLE_ARGUMENTS_ITEM_TO_EDIT, spriteToEdit);
		super.onSaveInstanceState(outState);
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
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		menu.setHeaderTitle(spriteToEdit.getName());
		getActivity().getMenuInflater().inflate(R.menu.context_menu_backpack_sprite, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.context_menu_unpack_background:
				showUnpackAsBackgroundDialog();
				break;
			case R.id.context_menu_unpack_object:
				unpackCheckedItems();
				break;
			case R.id.context_menu_delete:
				showDeleteDialog();
				break;
			default:
				return super.onContextItemSelected(item);
		}
		return true;
	}

	@Override
	public void handleOnItemClick(int position, View view, Sprite listItem) {
		spriteToEdit = listItem;
		getListView().showContextMenuForChild(view);
	}

	@Override
	public void handleOnItemLongClick(int position, View view) {
		spriteToEdit = spriteAdapter.getItem(position);
		getListView().showContextMenuForChild(view);
	}

	@Override
	public void deleteCheckedItems() {
		//TODO: find better solution.
		if (spriteAdapter.getCheckedItems().isEmpty()) {
			delete(spriteToEdit);
			return;
		}

		for (Sprite sprite : spriteAdapter.getCheckedItems()) {
			delete(sprite);
		}

		clearCheckedItems();
	}

	public void delete(Sprite sprite) {
		spriteAdapter.remove(sprite);
		removeLooksAndSounds();
	}

	private void removeLooksAndSounds() {
		//TODO: could still be better!
		for (LookInfo lookInfo : spriteToEdit.getLookInfoList()) {
			if (!StorageHandler.deleteFile(lookInfo.getAbsolutePath())) {
				ToastUtil.showError(getActivity(), R.string.error_delete_look);
			}
		}

		for (SoundInfo sound : spriteToEdit.getSoundList()) {
			if (!StorageHandler.deleteFile(sound.getAbsolutePath())) {
				ToastUtil.showError(getActivity(), R.string.error_delete_sound);
			}
		}
	}

	@Override
	protected void unpackCheckedItems() {
		//TODO: find better solution.
		if (spriteAdapter.getCheckedItems().isEmpty()) {
			unpack(spriteToEdit);
			return;
		}

		for (Sprite sprite : spriteAdapter.getCheckedItems()) {
			unpack(sprite);
		}

		clearCheckedItems();
	}

	private void unpack(Sprite sprite) {
		try {
			Sprite unpackedSprite = BackpackSpriteController.unpack(sprite);
			ProjectManager.getInstance().getCurrentScene().addSprite(unpackedSprite);
		} catch (IOException e) {
			ToastUtil.showError(getActivity(), R.string.error_unpack_sprite);
		}
	}

	private void showUnpackAsBackgroundDialog() {
		AlertDialog.Builder builder = new CustomAlertDialogBuilder(getActivity());
		builder.setTitle(R.string.unpack);
		builder.setMessage(R.string.unpack_background);
		builder.setPositiveButton(R.string.main_menu_continue, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				//TODO: unpack as background (e.g. via flag in BackpackSpriteController.
				unpackCheckedItems();
				clearCheckedItems();
			}
		});
		builder.setNegativeButton(R.string.abort, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
				clearCheckedItems();
			}
		});

		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialogInterface) {
				clearCheckedItems();
			}
		});

		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}
}
