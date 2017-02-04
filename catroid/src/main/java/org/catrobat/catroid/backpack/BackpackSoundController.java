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

package org.catrobat.catroid.backpack;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.common.FileChecksumContainer;
import org.catrobat.catroid.common.SoundInfo;
import org.catrobat.catroid.io.StorageHandler;
import org.catrobat.catroid.ui.controller.BackPackListManager;
import org.catrobat.catroid.utils.Utils;

import java.io.File;
import java.util.List;

public final class BackpackSoundController {

	public static SoundInfo pack(SoundInfo item) {
		List<SoundInfo> backpackedSounds = BackPackListManager.getBackPackedSounds();
		if(backpackedSounds.contains(item)){
			//contains and indexOf use the passed Class' equals implementation (stated in JavaDoc)
			return backpackedSounds.get(backpackedSounds.indexOf(item));
		}

		File backpackedSoundFile = getPackedSoundFile(item);

		if(backpackedSoundFile != null) {
			SoundInfo backpackedSoundInfo = new SoundInfo(item.getTitle(), backpackedSoundFile.getName());
			backpackedSoundInfo.setBackpackSoundInfo(true);
			BackPackListManager.addSoundToBackPack(backpackedSoundInfo);
			return backpackedSoundInfo;
		}

		return null;
	}

	private static File getPackedSoundFile(SoundInfo item) {
    FileChecksumContainer container = BackPackListManager.getInstance().getFileChecksumContainer();
		return StorageHandler.copyFile(item.getAbsolutePath(), Utils.getBackpackSoundDirectoryPath(), null);
	}

	public static SoundInfo unpack(SoundInfo item) {
		String newSoundName = Utils.getUniqueSoundName(item, false);
		File unpackedSoundFile = getUnpackedSoundFile(item);

		if(unpackedSoundFile != null) {
			SoundInfo unpackedSoundData = new SoundInfo(newSoundName, unpackedSoundFile.getName());
			unpackedSoundData.setBackpackSoundInfo(false);
			return unpackedSoundData;
		}
		return null;
	}
  
	private static File getUnpackedSoundFile(SoundInfo item) {
		String currentSoundDirectoryPath = ProjectManager.getInstance().getCurrentScene().getSceneSoundDirectoryPath();
		FileChecksumContainer container = ProjectManager.getInstance().getFileChecksumContainer();
		return StorageHandler.copyFile(item.getAbsolutePath(), currentSoundDirectoryPath, container);
	}
}
