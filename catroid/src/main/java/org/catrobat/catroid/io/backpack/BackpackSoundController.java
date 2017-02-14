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
import org.catrobat.catroid.common.Constants;
import org.catrobat.catroid.common.SoundInfo;
import org.catrobat.catroid.io.StorageHandler;
import org.catrobat.catroid.ui.controller.BackPackListManager;
import org.catrobat.catroid.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class BackpackSoundController {

	private static final String backpackSoundDirectory = Utils.buildPath(Constants.DEFAULT_ROOT, Constants
			.BACKPACK_DIRECTORY, Constants.BACKPACK_SOUND_DIRECTORY);

	public static SoundInfo pack(SoundInfo item) throws IOException {
		List<SoundInfo> backpackedSounds = BackPackListManager.getInstance().getBackPackedSounds();

		if(backpackedSounds.contains(item)){
			return backpackedSounds.get(backpackedSounds.indexOf(item));
		}

		File packedSoundFile = StorageHandler.copyFile(item.getAbsolutePath(), backpackSoundDirectory);

		SoundInfo packedSoundInfo = new SoundInfo(item.getName(), packedSoundFile.getName());
		packedSoundInfo.setBackpackSoundInfo(true);
		return packedSoundInfo;
	}

	public static SoundInfo packAndAddToVisibleBackpack(SoundInfo item) throws IOException {
		SoundInfo packedSoundInfo = pack(item);
		BackPackListManager.getInstance().addSoundToBackPack(packedSoundInfo);
		return packedSoundInfo;
	}

	public static SoundInfo unpack(SoundInfo item) throws IOException {
		List<SoundInfo> scope = ProjectManager.getInstance().getCurrentSprite().getSoundList();
		String newSoundName = Utils.getUniqueSoundName(item.getName(), scope);

		String currentSoundDirectory = ProjectManager.getInstance().getCurrentScene().getSoundDirectory();
		File unpackedSoundFile = StorageHandler.copyFile(item.getAbsolutePath(), currentSoundDirectory);

		SoundInfo unpackedSoundInfo = new SoundInfo(newSoundName, unpackedSoundFile.getName());
		unpackedSoundInfo.setBackpackSoundInfo(false);
		return unpackedSoundInfo;
	}
}
