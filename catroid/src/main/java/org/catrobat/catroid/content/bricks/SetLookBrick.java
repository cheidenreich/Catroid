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
package org.catrobat.catroid.content.bricks;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.common.LookInfo;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.ui.ScriptActivity;
import org.catrobat.catroid.ui.controller.LookController;
import org.catrobat.catroid.ui.fragment.LookFragment;
import org.catrobat.catroid.ui.fragment.LookFragment.OnLookDataListChangedAfterNewListener;

import java.util.List;

public class SetLookBrick extends BrickBaseType implements OnLookDataListChangedAfterNewListener {
	private static final long serialVersionUID = 1L;
	protected LookInfo look;
	private transient View prototypeView;
	private transient LookInfo oldSelectedLook;

	protected transient boolean wait;

	public SetLookBrick() {
		wait = false;
	}

	public void setLook(LookInfo lookInfo) {
		this.look = lookInfo;
	}

	public LookInfo getLook() {
		return this.look;
	}

	@Override
	public Brick copyBrickForSprite(Sprite sprite) {
		SetLookBrick copyBrick = (SetLookBrick) clone();

		if (look != null && look.isBackpackLookData) {
			copyBrick.look = look;
			return copyBrick;
		}

		for (LookInfo data : sprite.getLookInfoList()) {
			if (look != null && data != null && data.getAbsolutePath().equals(look.getAbsolutePath())) {
				copyBrick.look = data;
				break;
			}
		}
		copyBrick.look.isBackpackLookData = false;
		return copyBrick;
	}

	public String getImagePath() {
		return look.getAbsolutePath();
	}

	@Override
	public View getView(final Context context, int brickId, BaseAdapter baseAdapter) {
		if (animationState) {
			return view;
		}

		view = View.inflate(context, R.layout.brick_set_look, null);
		view = BrickViewProvider.setAlphaOnView(view, alphaValue);

		setCheckboxView(R.id.brick_set_look_checkbox);

		final Spinner lookBrickSpinner = (Spinner) view.findViewById(R.id.brick_set_look_spinner);

		final ArrayAdapter<LookInfo> spinnerAdapter = createLookAdapter(context);

		SpinnerAdapterWrapper spinnerAdapterWrapper = new SpinnerAdapterWrapper(context, spinnerAdapter);

		lookBrickSpinner.setAdapter(spinnerAdapterWrapper);

		lookBrickSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (position == 0) {
					look = null;
				} else {
					look = (LookInfo) parent.getItemAtPosition(position);
					oldSelectedLook = look;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		setSpinnerSelection(lookBrickSpinner);

		if (getSprite().getName().equals(context.getString(R.string.background))) {
			TextView textField = (TextView) view.findViewById(R.id.brick_set_look_prototype_text_view);
			textField.setText(R.string.brick_set_background);
		}

		if (!wait) {
			view.findViewById(R.id.brick_set_look_and_wait).setVisibility(View.GONE);
		}

		return view;
	}

	private ArrayAdapter<LookInfo> createLookAdapter(Context context) {
		ArrayAdapter<LookInfo> arrayAdapter = new ArrayAdapter<LookInfo>(context, android.R.layout.simple_spinner_item);
		arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		LookInfo dummyLookInfo = new LookInfo(context.getString(R.string.new_broadcast_message), null);
		arrayAdapter.add(dummyLookInfo);
		for (LookInfo lookInfo : getSprite().getLookInfoList()) {
			arrayAdapter.add(lookInfo);
		}
		return arrayAdapter;
	}

	@Override
	public View getPrototypeView(Context context) {
		prototypeView = View.inflate(context, R.layout.brick_set_look, null);
		if (getSprite().getName().equals(context.getString(R.string.background))) {
			TextView textField = (TextView) prototypeView.findViewById(R.id.brick_set_look_prototype_text_view);
			textField.setText(R.string.brick_set_background);
		}

		if (!wait) {
			prototypeView.findViewById(R.id.brick_set_look_and_wait).setVisibility(View.GONE);
		}
		Spinner setLookSpinner = (Spinner) prototypeView.findViewById(R.id.brick_set_look_spinner);

		SpinnerAdapter setLookSpinnerAdapter = createLookAdapter(context);
		setLookSpinner.setAdapter(setLookSpinnerAdapter);
		setSpinnerSelection(setLookSpinner);
		return prototypeView;
	}

	@Override
	public Brick clone() {
		SetLookBrick clonedBrick = new SetLookBrick();
		clonedBrick.setLook(look);
		return clonedBrick;
	}

	@Override
	public List<SequenceAction> addActionToSequence(Sprite sprite, SequenceAction sequence) {
		sequence.addAction(sprite.getActionFactory().createSetLookAction(sprite, look, wait));
		return null;
	}

	private void setSpinnerSelection(Spinner spinner) {
		if (getSprite().getLookInfoList().contains(look)) {
			oldSelectedLook = look;
			spinner.setSelection(getSprite().getLookInfoList().indexOf(look) + 1, true);
		} else {
			if (spinner.getAdapter() != null && spinner.getAdapter().getCount() > 1) {
				if (getSprite().getLookInfoList().indexOf(oldSelectedLook) >= 0) {
					spinner.setSelection(getSprite().getLookInfoList()
							.indexOf(oldSelectedLook) + 1, true);
				} else {
					spinner.setSelection(1, true);
				}
			} else {
				spinner.setSelection(0, true);
			}
		}
	}

	private void setOnLookDataListChangedAfterNewListener(Context context) {
		ScriptActivity scriptActivity = (ScriptActivity) context;
		LookFragment lookFragment = (LookFragment) scriptActivity.getFragment(ScriptActivity.FRAGMENT_LOOKS);
		if (lookFragment != null) {
			lookFragment.setOnLookDataListChangedAfterNewListener(this);
		}
	}

	private class SpinnerAdapterWrapper implements SpinnerAdapter {

		protected Context context;
		protected ArrayAdapter<LookInfo> spinnerAdapter;

		private boolean isTouchInDropDownView;

		public SpinnerAdapterWrapper(Context context, ArrayAdapter<LookInfo> spinnerAdapter) {
			this.context = context;
			this.spinnerAdapter = spinnerAdapter;

			this.isTouchInDropDownView = false;
		}

		@Override
		public void registerDataSetObserver(DataSetObserver paramDataSetObserver) {
			spinnerAdapter.registerDataSetObserver(paramDataSetObserver);
		}

		@Override
		public void unregisterDataSetObserver(DataSetObserver paramDataSetObserver) {
			spinnerAdapter.unregisterDataSetObserver(paramDataSetObserver);
		}

		@Override
		public int getCount() {
			return spinnerAdapter.getCount();
		}

		@Override
		public Object getItem(int paramInt) {
			return spinnerAdapter.getItem(paramInt);
		}

		@Override
		public long getItemId(int paramInt) {
			LookInfo currentLook = spinnerAdapter.getItem(paramInt);
			if (!currentLook.getName().equals(context.getString(R.string.new_broadcast_message))) {
				oldSelectedLook = currentLook;
			}
			return spinnerAdapter.getItemId(paramInt);
		}

		@Override
		public boolean hasStableIds() {
			return spinnerAdapter.hasStableIds();
		}

		@Override
		public View getView(int paramInt, View paramView, ViewGroup paramViewGroup) {
			if (isTouchInDropDownView) {
				isTouchInDropDownView = false;
				if (paramInt == 0) {
					switchToLookFragmentFromScriptFragment();
				}
			}
			return spinnerAdapter.getView(paramInt, paramView, paramViewGroup);
		}

		@Override
		public int getItemViewType(int paramInt) {
			return spinnerAdapter.getItemViewType(paramInt);
		}

		@Override
		public int getViewTypeCount() {
			return spinnerAdapter.getViewTypeCount();
		}

		@Override
		public boolean isEmpty() {
			return spinnerAdapter.isEmpty();
		}

		@Override
		public View getDropDownView(int paramInt, View paramView, ViewGroup paramViewGroup) {
			View dropDownView = spinnerAdapter.getDropDownView(paramInt, paramView, paramViewGroup);

			dropDownView.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
					isTouchInDropDownView = true;
					return false;
				}
			});

			return dropDownView;
		}

		private void switchToLookFragmentFromScriptFragment() {
			ProjectManager.getInstance().setCurrentSprite(getSprite());
			ScriptActivity scriptActivity = ((ScriptActivity) context);
			scriptActivity.switchToFragmentFromScriptFragment(ScriptActivity.FRAGMENT_LOOKS);

			setOnLookDataListChangedAfterNewListener(context);
		}
	}

	@Override
	public void onLookDataListChangedAfterNew(LookInfo lookInfo) {
		look = lookInfo;
		oldSelectedLook = lookInfo;
	}

	@Override
	public void storeDataForBackPack(Sprite sprite) {
		if (look == null) {
			return;
		}
		look = LookController.getInstance().backPackHiddenLook(this.getLook());
		if (sprite != null && !sprite.getLookInfoList().contains(look)) {
			sprite.getLookInfoList().add(look);
		}
	}

	protected Sprite getSprite() {
		return ProjectManager.getInstance().getCurrentSprite();
	}
}
