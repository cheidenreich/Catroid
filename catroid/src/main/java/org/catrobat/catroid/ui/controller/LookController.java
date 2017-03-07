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
package org.catrobat.catroid.ui.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.badlogic.gdx.graphics.Pixmap;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.common.Constants;
import org.catrobat.catroid.common.DroneVideoLookInfo;
import org.catrobat.catroid.common.LookInfo;
import org.catrobat.catroid.io.StorageHandler;
import org.catrobat.catroid.io.backpack.BackpackLookController;
import org.catrobat.catroid.ui.LookViewHolder;
import org.catrobat.catroid.ui.ScriptActivity;
import org.catrobat.catroid.ui.adapter.LookBaseAdapter;
import org.catrobat.catroid.ui.dialogs.CustomAlertDialogBuilder;
import org.catrobat.catroid.ui.fragment.LookFragment;
import org.catrobat.catroid.ui.fragment.ScriptFragment;
import org.catrobat.catroid.utils.ImageEditing;
import org.catrobat.catroid.utils.UtilFile;
import org.catrobat.catroid.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class LookController {
	public static final int REQUEST_SELECT_OR_DRAW_IMAGE = 0;
	public static final int REQUEST_POCKET_PAINT_EDIT_IMAGE = 1;
	public static final int REQUEST_TAKE_PICTURE = 2;
	public static final int REQUEST_MEDIA_LIBRARY = 3;
	public static final int REQUEST_DRONE_VIDEO = 4;
	public static final int ID_LOADER_MEDIA_IMAGE = 1;
	public static final String BUNDLE_ARGUMENTS_SELECTED_LOOK = "selected_look";
	public static final String BUNDLE_ARGUMENTS_URI_IS_SET = "uri_is_set";
	public static final String LOADER_ARGUMENTS_IMAGE_URI = "image_uri";
	public static final String SHARED_PREFERENCE_NAME = "showDetailsLooks";

	private static final String TAG = LookController.class.getSimpleName();
	private static final LookController INSTANCE = new LookController();

	private OnBackpackLookCompleteListener onBackpackLookCompleteListener;

	private LookController() {
	}

	public static LookController getInstance() {
		return INSTANCE;
	}

	public void updateLookLogic(final int position, final LookViewHolder holder, final LookBaseAdapter lookAdapter) {
		final LookInfo lookInfo = lookAdapter.getLookInfoItems().get(position);

		if (lookInfo == null) {
			return;
		}
		holder.lookNameTextView.setTag(position);
		holder.lookElement.setTag(position);
		holder.lookImageView.setImageBitmap(lookInfo.getThumbnailBitmap());
		holder.lookNameTextView.setText(lookInfo.getName());

		boolean checkboxIsVisible = handleCheckboxes(position, holder, lookAdapter);
		handleDetails(lookInfo, holder, lookAdapter);

		// Disable ImageView on active ActionMode
		if (checkboxIsVisible) {
			holder.lookImageView.setEnabled(false);
		} else {
			holder.lookImageView.setEnabled(true);
		}
		if (holder.lookElement.isClickable()) {
			holder.lookElement.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (lookAdapter.getSelectMode() != ListView.CHOICE_MODE_NONE) {
						holder.checkbox.setChecked(!holder.checkbox.isChecked());
					} else if (lookAdapter.getOnLookEditListener() != null) {
						lookAdapter.getOnLookEditListener().onLookEdit(view);
					}
				}
			});
			setOnTouchListener(holder, lookAdapter);
		} else {
			holder.lookElement.setOnClickListener(null);
		}
	}

	private void setOnTouchListener(LookViewHolder holder, final LookBaseAdapter lookAdapter) {
		if (lookAdapter.backPackAdapter) {
			return;
		}

		holder.lookElement.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					Intent intent = new Intent(ScriptActivity.ACTION_LOOK_TOUCH_ACTION_UP);
					lookAdapter.getContext().sendBroadcast(intent);
				}
				return false;
			}
		});
	}

	private void handleDetails(LookInfo lookInfo, LookViewHolder holder, LookBaseAdapter lookAdapter) {
		if (lookAdapter.getShowDetails()) {
			if (lookInfo.getAbsolutePath() != null) {
				holder.lookFileSizeTextView.setText(UtilFile.getSizeAsString(new File(lookInfo.getAbsolutePath())));
			}
			int[] measure = lookInfo.getMeasure();
			String measureString = measure[0] + " x " + measure[1];

			holder.lookMeasureTextView.setText(measureString);
			holder.lookDetailsLinearLayout.setVisibility(TextView.VISIBLE);
		} else {
			holder.lookDetailsLinearLayout.setVisibility(TextView.GONE);
		}
	}

	private boolean handleCheckboxes(final int position, LookViewHolder holder, final LookBaseAdapter lookAdapter) {
		holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					if (lookAdapter.getSelectMode() == ListView.CHOICE_MODE_SINGLE) {
						lookAdapter.clearCheckedItems();
					}
					lookAdapter.getCheckedItems().add(position);
				} else {
					lookAdapter.getCheckedItems().remove(position);
				}
				lookAdapter.notifyDataSetChanged();

				if (lookAdapter.getOnLookEditListener() != null) {
					lookAdapter.getOnLookEditListener().onLookChecked();
				}
			}
		});

		boolean checkboxIsVisible = false;

		if (lookAdapter.getSelectMode() != ListView.CHOICE_MODE_NONE) {
			holder.checkbox.setVisibility(View.VISIBLE);
			holder.lookElement.setBackgroundResource(R.drawable.button_background_shadowed);
			checkboxIsVisible = true;
		} else {
			holder.checkbox.setVisibility(View.GONE);
			holder.checkbox.setChecked(false);
			holder.lookElement.setBackgroundResource(R.drawable.button_background_selector);
			lookAdapter.clearCheckedItems();
		}

		if (lookAdapter.getCheckedItems().contains(position)) {
			holder.checkbox.setChecked(true);
		} else {
			holder.checkbox.setChecked(false);
		}
		return checkboxIsVisible;
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle arguments, Activity activity) {
		Uri imageUri = null;

		if (arguments != null) {
			imageUri = (Uri) arguments.get(LOADER_ARGUMENTS_IMAGE_URI);
		}
		String[] projection = { MediaStore.MediaColumns.DATA };
		return new CursorLoader(activity, imageUri, projection, null, null, null);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor data, Activity activity, List<LookInfo> lookInfoList,
			LookFragment fragment) {
		String originalImagePath = "";
		CursorLoader cursorLoader = (CursorLoader) loader;

		boolean catchedException = false;

		if (data == null) {
			originalImagePath = cursorLoader.getUri().getPath();
		} else {
			int columnIndex = data.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
			data.moveToFirst();

			try {
				originalImagePath = data.getString(columnIndex);
			} catch (CursorIndexOutOfBoundsException e) {
				catchedException = true;
			}
		}

		if (catchedException || (data == null && originalImagePath.equals(""))) {
			Log.e(TAG, "Error loading image in onLoadFinished");
			Utils.showErrorDialog(activity, R.string.error_load_image);
			return;
		}
		copyImageToCatroid(originalImagePath, activity, lookInfoList, fragment);
	}

	public void updateLookAdapter(String name, File imageFile, List<LookInfo> lookInfoList, LookFragment fragment) {
		updateLookAdapter(name, imageFile, lookInfoList, fragment, false);
	}

	private void updateLookAdapter(String name, File imageFile, List<LookInfo> lookInfoList, LookFragment fragment,
			boolean isDroneVideo) {
		LookInfo lookInfo;

		if (isDroneVideo) {
			lookInfo = new DroneVideoLookInfo(name, imageFile);
		} else {
			lookInfo = new LookInfo(name, imageFile);
		}

		lookInfoList.add(lookInfo);
		fragment.updateLookAdapter(lookInfo);

		if (ProjectManager.getInstance().getCurrentSprite().hasCollision()) {
			lookInfo.getCollisionInformation().calculate();
		}
	}

	public static LookInfo createLookFromBitmapResource(Resources res, int imageId, String outputImageName)
			throws IOException {

		Bitmap newImage = BitmapFactory.decodeResource(res, imageId);
		File outputFile = new File(ProjectManager.getInstance().getCurrentScene().getImageDirectory(), outputImageName);
		StorageHandler.saveBitmapToImageFile(outputFile, newImage);

		return new LookInfo(outputImageName, outputFile);
	}

	private void copyImageToCatroid(String originalImagePath, Activity activity, List<LookInfo> lookInfoList,
			LookFragment fragment) {
		try {
			int[] imageDimensions = ImageEditing.getImageDimensions(originalImagePath);

			if (imageDimensions[0] < 0 || imageDimensions[1] < 0) {
				Log.e(TAG, "Error loading image in copyImageToCatroid imageDimensions");
				Utils.showErrorDialog(activity, R.string.error_load_image);
				return;
			}

			File oldFile = new File(originalImagePath);

			if (originalImagePath.equals("")) {
				throw new IOException();
			}

			String projectName = ProjectManager.getInstance().getCurrentProject().getName();
			String sceneName = ProjectManager.getInstance().getCurrentScene().getName();
			String destinationDir = Utils.buildPath(Utils.buildScenePath(projectName, sceneName), Constants
					.IMAGE_DIRECTORY);

			File imageFile = null;
			try {
				imageFile = StorageHandler.copyFile(originalImagePath, destinationDir);
			} catch (IOException e) {
				e.printStackTrace();
				return;
				//TODO REFACTOR: handle error, all error handling should be done outside of this method, rethrow exception
			}
			String imageName;
			int extensionDotIndex = oldFile.getName().lastIndexOf('.');
			if (extensionDotIndex > 0) {
				imageName = oldFile.getName().substring(0, extensionDotIndex);
			} else {
				imageName = oldFile.getName();
			}

			// if pixmap cannot be created, image would throw an Exception in stage
			// so has to be loaded again with other Config
			Pixmap pixmap = Utils.getPixmapFromFile(imageFile);

			if (pixmap == null) {
				ImageEditing.overwriteImageFileWithNewBitmap(imageFile);
				pixmap = Utils.getPixmapFromFile(imageFile);

				if (pixmap == null) {
					Log.e(TAG, "Error loading image in copyImageToCatroid pixmap");
					Utils.showErrorDialog(activity, R.string.error_load_image);

					//TODO REFACTOR: handle error if file not deleted:
					StorageHandler.deleteFile(imageFile.getAbsolutePath());
					return;
				}
			}
			updateLookAdapter(imageName, imageFile, lookInfoList, fragment);
		} catch (IOException e) {
			//TODO REFACTOR: handle error, all error handling should be done outside of this method, rethrow exception
			Log.e(TAG, "Error loading image in copyImageToCatroid IOException");
			Utils.showErrorDialog(activity, R.string.error_load_image);
		} catch (NullPointerException e) {
			//TODO REFACTOR: handle error, all error handling should be done outside of this method, rethrow exception
			Log.e(TAG, "probably originalImagePath null; message: " + e.getMessage());
			Utils.showErrorDialog(activity, R.string.error_load_image);
		}
		fragment.destroyLoader();
		activity.sendBroadcast(new Intent(ScriptActivity.ACTION_BRICK_LIST_CHANGED));
	}

	public void loadImageIntoCatroid(Intent intent, Activity activity, List<LookInfo> lookInfoList,
			LookFragment fragment) {
		String originalImagePath = "";
		//get path of image - will work for most applications
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			originalImagePath = bundle.getString(Constants.EXTRA_PICTURE_PATH_POCKET_PAINT);
		}

		Uri imageUri = intent.getData();
		if (imageUri != null) {

			Cursor cursor = activity.getContentResolver().query(imageUri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);

			if (cursor != null) {
				cursor.moveToFirst();
				originalImagePath = cursor.getString(0);
				cursor.close();
			}
		}

		if (originalImagePath == null || originalImagePath.equals("")) {
			Bundle arguments = new Bundle();
			arguments.putParcelable(LOADER_ARGUMENTS_IMAGE_URI, intent.getData());
			fragment.initOrRestartLoader(arguments);
		} else {
			copyImageToCatroid(originalImagePath, activity, lookInfoList, fragment);
		}
	}

	public void loadPocketPaintImageIntoCatroid(Intent intent, Activity activity, LookInfo selectedLookInfo) {
		Bundle bundle = intent.getExtras();
		String pathOfPocketPaintImage = bundle.getString(Constants.EXTRA_PICTURE_PATH_POCKET_PAINT);

		int[] imageDimensions = ImageEditing.getImageDimensions(pathOfPocketPaintImage);
		if (imageDimensions[0] < 0 || imageDimensions[1] < 0) {
			Log.e(TAG, "Error loading image in loadPocketPaintImageIntoCatroid");
			Utils.showErrorDialog(activity, R.string.error_load_image);
			return;
		}

		String editedFileChecksum = Utils.md5Checksum(new File(pathOfPocketPaintImage));

		// If look changed --> saving new image with new checksum and changing lookInfo
		if (!selectedLookInfo.getChecksum().equalsIgnoreCase(editedFileChecksum)) {

			String projectName = ProjectManager.getInstance().getCurrentProject().getName();
			String sceneName = ProjectManager.getInstance().getCurrentScene().getName();
			String destinationDir = Utils.buildPath(Utils.buildScenePath(projectName, sceneName), Constants
					.IMAGE_DIRECTORY);

			File imageFile = null;
			try {
				imageFile = StorageHandler.copyFile(pathOfPocketPaintImage, destinationDir);
			} catch (IOException e) {
				e.printStackTrace();
				return;
				//TODO REFACTOR: handle error
			}

			StorageHandler.deleteFile(selectedLookInfo.getAbsolutePath());

			selectedLookInfo.setFile(imageFile);
			selectedLookInfo.resetThumbnailBitmap();

			if (ProjectManager.getInstance().getCurrentSprite().hasCollision()) {
				selectedLookInfo.getCollisionInformation().calculate();
			}
		}

		//delete temporary image
		StorageHandler.deleteFile(pathOfPocketPaintImage);
	}

	public void loadPictureFromCameraIntoCatroid(Uri lookFromCameraUri, Activity activity,
			List<LookInfo> lookInfo, LookFragment fragment) {
		if (lookFromCameraUri != null) {
			String originalImagePath = lookFromCameraUri.getPath();

			int[] imageDimensions = ImageEditing.getImageDimensions(originalImagePath);
			if (imageDimensions[0] < 0 || imageDimensions[1] < 0) {
				Log.e(TAG, "Error loading image in loadPictureFromCameraIntoCatroid");
				Utils.showErrorDialog(activity, R.string.error_load_image);
				return;
			}
			copyImageToCatroid(originalImagePath, activity, lookInfo, fragment);

			File pictureOnSdCard = new File(lookFromCameraUri.getPath());
			pictureOnSdCard.delete();
		}
	}

	public void loadPictureFromLibraryIntoCatroid(String filePath, Activity activity,
			List<LookInfo> lookInfo, LookFragment fragment) {
		File mediaImage = null;
		mediaImage = new File(filePath);
		copyImageToCatroid(mediaImage.toString(), activity, lookInfo, fragment);
		File pictureOnSdCard = new File(mediaImage.getPath());
		pictureOnSdCard.delete();
	}

	public boolean checkIfPocketPaintIsInstalled(Intent intent, final Activity activity) {
		// Confirm if Pocket Paint is installed else start dialog --------------------------

		List<ResolveInfo> packageList = activity.getPackageManager().queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);

		if (packageList.size() <= 0) {
			AlertDialog.Builder builder = new CustomAlertDialogBuilder(activity);
			builder.setTitle(R.string.pocket_paint_not_installed_title);
			builder.setMessage(R.string.pocket_paint_not_installed).setCancelable(false)
					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							Intent downloadPocketPaintIntent = new Intent(Intent.ACTION_VIEW, Uri
									.parse(Constants.POCKET_PAINT_DOWNLOAD_LINK));
							activity.startActivity(downloadPocketPaintIntent);
						}
					}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			AlertDialog alert = builder.create();
			alert.show();
			return false;
		}
		return true;
	}

	public boolean deleteLook(LookInfo look) {
		look.getCollisionInformation().cancelCalculation();
		return StorageHandler.deleteFile(look.getAbsolutePath());
	}

	public boolean checkLookReplaceInBackpack(List<LookInfo> currentLookInfoList) {
		boolean looksAlreadyInBackpack = false;
		for (LookInfo lookInfo : currentLookInfoList) {
			looksAlreadyInBackpack = checkLookReplaceInBackpack(lookInfo);
			if (looksAlreadyInBackpack) {
				return looksAlreadyInBackpack;
			}
		}
		return looksAlreadyInBackpack;
	}

	public boolean checkLookReplaceInBackpack(LookInfo currentLookInfo) {
		return BackPackListManager.getInstance().backPackedLooksContain(currentLookInfo, true);
	}

	public void showBackPackReplaceDialog(final List<LookInfo> currentLookInfoList, final Context context) {
		Resources resources = context.getResources();
		String replaceLookMessage = resources.getString(R.string.backpack_replace_look_multiple);

		AlertDialog dialog = new CustomAlertDialogBuilder(context)
				.setTitle(R.string.backpack)
				.setMessage(replaceLookMessage)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						for (LookInfo currentLookData : currentLookInfoList) {
							backPackVisibleLook(currentLookData);
						}
						onBackpackLookCompleteListener.onBackpackLookComplete(true);
						dialog.dismiss();
					}
				}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						onBackpackLookCompleteListener.onBackpackLookComplete(false);
						dialog.dismiss();
					}
				}).create();
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
	}

	public void showBackPackReplaceDialog(final LookInfo currentLookInfo, final Context context) {
		Resources resources = context.getResources();
		String replaceLookMessage = resources.getString(R.string.backpack_replace_look, currentLookInfo.getName());

		AlertDialog dialog = new CustomAlertDialogBuilder(context)
				.setTitle(R.string.backpack)
				.setMessage(replaceLookMessage)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						backPackVisibleLook(currentLookInfo);
						onBackpackLookCompleteListener.onBackpackLookComplete(true);
						dialog.dismiss();
					}
				}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create();
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
	}

	public void backPackVisibleLook(LookInfo currentLookInfo) {
		//TODO: handle errors elsewhere.
		try {
			BackpackLookController.packAndAddToVisibleBackpack(currentLookInfo);
		} catch (IOException e) {
			Log.e(TAG, "Something went wrong while packing a look.", e);
		}
	}

	public LookInfo backPackHiddenLook(LookInfo currentLookInfo) {
		//TODO: handle errors elsewhere.
		try {
			return BackpackLookController.pack(currentLookInfo);
		} catch (IOException e) {
			Log.e(TAG, "Something went wrong while packing a look.", e);
			return null;
		}
	}

	public LookInfo unpack(LookInfo selectedLookInfoBackPack, boolean deleteUnpackedItems, boolean fromHiddenBackPack) {
		//TODO: handle errors elsewhere and remove boolean flags. Also do not update adapter here.
		try {
			LookInfo newLookInfo = BackpackLookController.unpack(selectedLookInfoBackPack);
			ProjectManager.getInstance().getCurrentSprite().getLookInfoList().add(newLookInfo);
			LookBaseAdapter lookAdapter = BackPackListManager.getInstance().getCurrentLookAdapter();
			if (lookAdapter != null) {
				lookAdapter.notifyDataSetChanged();
			}
			return newLookInfo;
		} catch (IOException e) {
			Log.e(TAG, "Something went wrong while packing a look.", e);
			return null;
		}
	}

	public void switchToScriptFragment(LookFragment fragment, ScriptActivity scriptActivity) {
		scriptActivity.setCurrentFragment(ScriptActivity.FRAGMENT_SCRIPTS);

		FragmentTransaction fragmentTransaction = scriptActivity.getFragmentManager().beginTransaction();
		fragmentTransaction.hide(fragment);
		fragmentTransaction.show(scriptActivity.getFragmentManager().findFragmentByTag(ScriptFragment.TAG));
		fragmentTransaction.commitAllowingStateLoss();

		scriptActivity.setIsLookFragmentFromSetLookBrickNewFalse();
		scriptActivity.setIsLookFragmentHandleAddButtonHandled(false);
	}

	public void setOnBackpackLookCompleteListener(OnBackpackLookCompleteListener listener) {
		onBackpackLookCompleteListener = listener;
	}

	public interface OnBackpackLookCompleteListener {
		void onBackpackLookComplete(boolean startBackpackActivity);
	}
}
