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
package org.catrobat.catroid.test.content.actions;

import android.test.InstrumentationTestCase;

import com.badlogic.gdx.scenes.scene2d.Action;

import org.catrobat.catroid.common.Constants;
import org.catrobat.catroid.common.LookInfo;
import org.catrobat.catroid.content.ActionFactory;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.NextLookAction;
import org.catrobat.catroid.content.actions.PreviousLookAction;
import org.catrobat.catroid.content.actions.SetLookAction;

import java.io.File;

public class PreviousLookActionTest extends InstrumentationTestCase {

	private File testImage;
	private Sprite sprite;
	private ActionFactory actionFactory;

	@Override
	protected void setUp() throws Exception {
		final String imagePath = Constants.DEFAULT_ROOT + "/testImage.png";
		testImage = new File(imagePath);
		sprite = new Sprite("cat");
		actionFactory = sprite.getActionFactory();
	}

	public void testPreviousLook() {
		LookInfo lookInfo1 = new LookInfo();
		lookInfo1.setFileName(testImage.getName());
		lookInfo1.setName("testImage1");
		sprite.getLookInfoList().add(lookInfo1);

		LookInfo lookInfo2 = new LookInfo();
		lookInfo2.setFileName(testImage.getName());
		lookInfo2.setName("testImage2");
		sprite.getLookInfoList().add(lookInfo2);

		SetLookAction setLookAction = (SetLookAction) actionFactory.createSetLookAction(sprite, lookInfo1);
		NextLookAction nextLookAction = (NextLookAction) actionFactory.createNextLookAction(sprite);
		PreviousLookAction previousLookAction = (PreviousLookAction) actionFactory.createPreviousLookAction(sprite);

		setLookAction.act(1.0f);
		nextLookAction.act(1.0f);

		assertEquals("Look is not next look", lookInfo2, sprite.look.getLookInfo());

		previousLookAction.act(1.0f);

		assertEquals("Look is not previous look", lookInfo1.getName(), sprite.look.getLookInfo().getName());
	}

	public void testLastLook() {
		LookInfo lookInfo1 = new LookInfo();
		lookInfo1.setFileName(testImage.getName());
		lookInfo1.setName("testImage1");
		sprite.getLookInfoList().add(lookInfo1);

		LookInfo lookInfo2 = new LookInfo();
		lookInfo2.setFileName(testImage.getName());
		lookInfo2.setName("testImage2");
		sprite.getLookInfoList().add(lookInfo2);

		LookInfo lookInfo3 = new LookInfo();
		lookInfo3.setFileName(testImage.getName());
		lookInfo3.setName("testImage3");
		sprite.getLookInfoList().add(lookInfo3);

		Action setLookAction = actionFactory.createSetLookAction(sprite, lookInfo1);
		Action previousLookAction = actionFactory.createPreviousLookAction(sprite);

		setLookAction.act(1.0f);
		previousLookAction.act(1.0f);

		assertEquals("Look is not last look", lookInfo3.getName(), sprite.look.getLookInfo().getName());
	}

	public void testLookGalleryNull() {
		Action previousLookAction = actionFactory.createPreviousLookAction(sprite);
		previousLookAction.act(1.0f);

		assertEquals("Look is not null", null, sprite.look.getLookInfo());
	}

	public void testLookGalleryWithOneLook() {
		LookInfo lookInfo1 = new LookInfo();
		lookInfo1.setFileName(testImage.getName());
		lookInfo1.setName("testImage1");
		sprite.getLookInfoList().add(lookInfo1);

		Action setLookAction = actionFactory.createSetLookAction(sprite, lookInfo1);
		Action previousLookAction = actionFactory.createPreviousLookAction(sprite);

		setLookAction.act(1.0f);
		previousLookAction.act(1.0f);

		assertEquals("Wrong look after executing PreviousLookBrick with just one look", lookInfo1.getName(),
				sprite.look.getLookInfo().getName());
	}

	public void testPreviousLookWithNoLookSet() {
		Action previousLookAction = actionFactory.createPreviousLookAction(sprite);

		LookInfo lookInfo1 = new LookInfo();
		lookInfo1.setFileName(testImage.getName());
		lookInfo1.setName("testImage1");
		sprite.getLookInfoList().add(lookInfo1);

		previousLookAction.act(1.0f);

		assertNull("No look should be set.", sprite.look.getLookInfo());
	}
}
