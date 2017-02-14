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
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Scene;
import org.catrobat.catroid.io.backpack.BackpackSceneController;
import org.catrobat.catroid.ui.adapter.CheckBoxListAdapter;
import org.catrobat.catroid.ui.adapter.SceneListAdapter;
import org.catrobat.catroid.ui.controller.BackPackListManager;
import org.catrobat.catroid.ui.dialogs.CustomAlertDialogBuilder;

import java.util.List;

public class BackPackSceneListFragment extends BackPackActivityFragment implements CheckBoxListAdapter
		.ListItemClickHandler<Scene>, CheckBoxListAdapter.ListItemLongClickHandler {

	public static final String TAG = BackPackSceneListFragment.class.getSimpleName();
	private static final String BUNDLE_ARGUMENTS_ITEM_TO_EDIT = "sceneToEdit";

	private SceneListAdapter sceneAdapter;
	private Scene sceneToEdit;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_backpack, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		registerForContextMenu(getListView());
		itemIdentifier = R.plurals.scene;
		deleteDialogTitle = R.plurals.delete_dialog_scene;

		if (savedInstanceState != null) {
			sceneToEdit = (Scene) savedInstanceState.get(BUNDLE_ARGUMENTS_ITEM_TO_EDIT);
		}

		initializeList();
		checkEmptyBackgroundBackPack();
	}

	private void initializeList() {
		List<Scene> sceneList = BackPackListManager.getInstance().getBackPackedScenes();

		sceneAdapter = new SceneListAdapter(getActivity(), R.layout.list_item, sceneList);

		setListAdapter(sceneAdapter);
		sceneAdapter.setListItemClickHandler(this);
		sceneAdapter.setListItemCheckHandler(this);
		sceneAdapter.setListItemLongClickHandler(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(BUNDLE_ARGUMENTS_ITEM_TO_EDIT, sceneToEdit);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
		super.onPause();
		BackPackListManager.getInstance().saveBackpack();
		saveCurrentProject();
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.findItem(R.id.show_details).setVisible(false);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		menu.setHeaderTitle(sceneToEdit.getName());
		super.onCreateContextMenu(menu, view, menuInfo);
	}

	@Override
	public void handleOnItemClick(int position, View view, Scene listItem) {
		sceneToEdit = listItem;
		getListView().showContextMenuForChild(view);
	}

	@Override
	public void handleOnItemLongClick(int position, View view) {
		sceneToEdit = sceneAdapter.getItem(position);
		getListView().showContextMenuForChild(view);
	}

	@Override
	public void deleteCheckedItems() {
		//TODO: find better way
		if (sceneAdapter.getCheckedItems().isEmpty()) {
			delete(sceneToEdit);
			return;
		}
		for (Scene scene : sceneAdapter.getCheckedItems()) {
			delete(scene);
		}
	}

	public void delete(Scene scene) {
		//TODO: remove scene data (i.e. looks, sounds).
		sceneAdapter.remove(scene);
	}

	@Override
	protected void unpackCheckedItems() {
		if (sceneAdapter.getCheckedItems().isEmpty()) {
			unpack(sceneToEdit);
			return;
		}

		for (Scene scene : sceneAdapter.getCheckedItems()) {
			unpack(scene);
		}
		clearCheckedItems();
	}

	private void  unpack(Scene scene) {
		Scene unpackedScene = BackpackSceneController.unpack(scene);
		ProjectManager.getInstance().getCurrentProject().addScene(unpackedScene);
	}

	//TODO: Check conflicting resolutions.
	private boolean conflictingResolutionsDetected(List<Scene> sceneList) {
		int currentHeight = ProjectManager.getInstance().getCurrentProject().getXmlHeader().virtualScreenHeight;
		int currentWidth = ProjectManager.getInstance().getCurrentProject().getXmlHeader().virtualScreenWidth;

		for (Scene scene : sceneList) {
			if (scene.getOriginalHeight() != currentHeight || scene.getOriginalWidth() != currentWidth) {
				return true;
			}
		}

		return false;
	}

	private void showDifferentResolutionDialog(final Scene scene) {
		AlertDialog.Builder builder = new CustomAlertDialogBuilder(getActivity());
		builder.setTitle(R.string.warning);
		builder.setMessage(R.string.error_unpack_scene_with_different_resolution);
		builder.setPositiveButton(R.string.main_menu_continue, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				unpack(scene);
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
