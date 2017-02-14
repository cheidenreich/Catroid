/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2017 The Catrobat Team
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

package org.catrobat.catroid.io.backpack;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.common.LookInfo;
import org.catrobat.catroid.common.SoundInfo;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.ui.controller.BackPackListManager;
import org.catrobat.catroid.utils.Utils;

import java.io.IOException;
import java.util.List;

public final class BackpackSpriteController {

	public static Sprite pack(Sprite item) throws IOException {
		/**
		 * TODO: check if item exists already (i.e. implement equals()
		 *
		 * List<Sprite> backpackedSprites = BackPackListManager.getBackPackedSprites();
		 * if (backpackedSprites.contains(item)) {
		 *	 return backpackedSprites.get(backpackedSprites.indexOf(item));
		 * }
		 *
		 */

		Sprite packedSprite = item.cloneForBackPack();

		for (Script script : item.getScriptList()) {
			packedSprite.addScript(BackpackScriptController.pack(script));
		}

		for (LookInfo lookInfo : item.getLookInfoList()) {
			packedSprite.getLookInfoList().add(BackpackLookController.pack(lookInfo));
		}

		for (SoundInfo soundInfo: item.getSoundList()) {
			packedSprite.getSoundList().add(BackpackSoundController.pack(soundInfo));
		}

		return packedSprite;
	}

	public static Sprite packAndAddToVisibleBackpack(Sprite item) throws IOException {
		Sprite packedSprite = pack(item);
		BackPackListManager.getInstance().addSpriteToBackPack(packedSprite);
		return packedSprite;
	}

	public static Sprite unpack(Sprite item) throws IOException {
		Sprite unpackedSprite = item.cloneForBackPack();

		List<Sprite> scope = ProjectManager.getInstance().getCurrentScene().getSpriteList();
		unpackedSprite.setName(Utils.getUniqueSpriteName(unpackedSprite.getName(), scope));

		for (Script script : item.getScriptList()) {
			unpackedSprite.addScript(BackpackScriptController.unpack(script));
		}

		for (LookInfo lookInfo : item.getLookInfoList()) {
			unpackedSprite.getLookInfoList().add(BackpackLookController.unpack(lookInfo));
		}

		for (SoundInfo soundInfo: item.getSoundList()) {
			unpackedSprite.getSoundList().add(BackpackSoundController.unpack(soundInfo));
		}

		return unpackedSprite;
	}
}
