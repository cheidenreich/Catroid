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
import org.catrobat.catroid.common.LookInfo;
import org.catrobat.catroid.io.StorageHandler;
import org.catrobat.catroid.ui.controller.BackPackListManager;
import org.catrobat.catroid.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class BackpackLookController {

	private static final String backpackImageDirectory = Utils.buildPath(Constants.DEFAULT_ROOT, Constants
			.BACKPACK_DIRECTORY, Constants.BACKPACK_IMAGE_DIRECTORY);

	public static LookInfo pack(LookInfo item) throws IOException {
		List<LookInfo> backpackedLooks = BackPackListManager.getInstance().getBackPackedLooks();
		if(backpackedLooks.contains(item)) {
			return backpackedLooks.get(backpackedLooks.indexOf(item));
		}

		File packedLookFile = StorageHandler.copyFile(item.getAbsolutePath(), backpackImageDirectory);

		LookInfo packedLookInfo = new LookInfo(item.getName(), packedLookFile);
		packedLookInfo.isBackpackLookData = true;
		return packedLookInfo;
	}

	public static LookInfo packAndAddToVisibleBackpack(LookInfo item) throws IOException {
		LookInfo packedLookInfo = pack(item);
		BackPackListManager.getInstance().addLookToBackPack(packedLookInfo);
		return packedLookInfo;
	}

	public static LookInfo unpack(LookInfo item) throws IOException {
		List<LookInfo> scope = ProjectManager.getInstance().getCurrentSprite().getLookInfoList();
		String newLookName = Utils.getUniqueLookName(item.getName(), scope);

		String currentImageDirectory = ProjectManager.getInstance().getCurrentScene().getImageDirectory();
		File unpackedLookFile = StorageHandler.copyFile(item.getAbsolutePath(), currentImageDirectory);

		LookInfo unpackedLookInfo = new LookInfo(newLookName, unpackedLookFile);
		unpackedLookInfo.isBackpackLookData = false;
		return unpackedLookInfo;
	}
}
