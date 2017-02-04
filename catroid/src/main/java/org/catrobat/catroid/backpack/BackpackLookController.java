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
import org.catrobat.catroid.common.Backpack;
import org.catrobat.catroid.common.FileChecksumContainer;
import org.catrobat.catroid.common.LookData;
import org.catrobat.catroid.io.StorageHandler;
import org.catrobat.catroid.ui.controller.BackPackListManager;
import org.catrobat.catroid.utils.Utils;

import java.io.File;
import java.util.List;

public final class BackpackLookController {

	public static LookData pack(LookData item) {
		List<LookData> backpackedLooks = BackPackListManager.getBackPackedLooks();
		if(backpackedLooks.contains(item)) {
			//contains and indexOf use the passed Class' equals implementation (stated in JavaDoc)
			return backpackedLooks.get(backpackedLooks.indexOf(item));
		}

		File packedLookFile = getPackedLookFile(item);

		if(packedLookFile != null) {
			LookData backpackedLookData = new LookData(item.getLookName(), packedLookFile.getName());
			backpackedLookData.isBackpackLookData = true;
			BackPackListManager.getInstance().addLookToBackPack(backpackedLookData);

			return backpackedLookData;
		}

		return null;
	}

	private static File getPackedLookFile(LookData item) {
		FileChecksumContainer container = BackPackListManager.getFileChecksumContainer();
		return StorageHandler.copyFile(item.getAbsolutePath(), Utils.getBackpackImageDirectoryPath(), container);
	}

	public static LookData unpack(LookData item) {
		List<LookData> scope = ProjectManager.getInstance().getCurrentSprite().getLookDataList();
		String newLookName = Utils.getUniqueLookName(item.getLookName(), scope);

		File unpackedLookFile = getUnpackedLookFile(item);

		if(unpackedLookFile != null) {
			LookData unpackedLookData = new LookData(newLookName, unpackedLookFile.getName());
			unpackedLookData.isBackpackLookData = false;
			return unpackedLookData;
		}

		return null;
	}

	private static File getUnpackedLookFile(LookData item) {
		String currentImageDirectoryPath = ProjectManager.getInstance().getCurrentScene().getSceneImageDirectoryPath();
		FileChecksumContainer container = ProjectManager.getInstance().getFileChecksumContainer();
		return StorageHandler.copyFile(item.getAbsolutePath(), currentImageDirectoryPath, container);
	}
}
