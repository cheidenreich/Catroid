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
package org.catrobat.catroid.common;

import android.util.Log;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.content.Scene;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.io.StorageHandler;
import org.catrobat.catroid.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class SoundInfo implements Serializable, Comparable<SoundInfo>, Cloneable {

	private static final long serialVersionUID = 1L;
	private static final String TAG = SoundInfo.class.getSimpleName();
	public transient boolean isPlaying;
	private transient boolean isBackpackSoundInfo;
	private String name;
	private String fileName;

	private transient File soundFile;

	public SoundInfo() {
	}

	public SoundInfo(String name, File soundFile) {
		this.name = name;
		this.soundFile = soundFile;

		if(soundFile!=null){
			this.fileName = soundFile.getName();
		}
	}

	public File createSoundFileForCurrentProject(String title, String fileName) {
		String directory = getPathToSoundDirectory();
		File file = new File(Utils.buildPath(directory, fileName));
		setFile(file);
		setName(title);
		return file;
	}

	public void initializeFile(String directory) {
		soundFile = new File(Utils.buildPath(directory, fileName));
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SoundInfo)) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		SoundInfo soundInfo = (SoundInfo) obj;

		if (this.soundFile == null || soundInfo.soundFile == null) {
			return false;
		}

		return soundInfo.getChecksum().equals(this.getChecksum()) && soundInfo.name.equals(this.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode() + fileName.hashCode() + super.hashCode();
	}

	@Override
	public SoundInfo clone() {
		try {
			File copiedFile = StorageHandler.copyFile(this.getAbsolutePath());
			List<SoundInfo> scope = ProjectManager.getInstance().getCurrentSprite().getSoundList();
			return new SoundInfo(Utils.getUniqueSoundName(this.name, scope), copiedFile);
		} catch (IOException e) {
			//TODO REFACTOR: handle error
			e.printStackTrace();
			return null;
		}
	}

	public String getAbsolutePath() {
		if (soundFile != null) {
			return soundFile.getAbsolutePath();
		} else {
			return null;
		}
	}

	public String getAbsoluteProjectPath() {
		if (fileName != null) {
			return Utils.buildPath(getPathToSoundDirectory(), fileName);
		} else {
			return null;
		}
	}

	public String getAbsoluteBackPackPath() {
		if (fileName != null) {
			return Utils.buildPath(getPathToBackPackSoundDirectory(), fileName);
		} else {
			return null;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String title) {
		this.name = title;
	}

	public String getFileName() {
		return fileName;
	}

	//public void setFileName(String fileName) {
	//	this.fileName = fileName;
	//}

	public void setFile(File file){
		this.soundFile = file;
		if (file != null) {
			this.fileName = file.getName();
		}
	}

	public String getChecksum() {
		File file = new File(getAbsolutePath());
		if (!file.exists()) {
			throw new NullPointerException("LookFile does not exist!");
		}
		return Utils.md5Checksum(file);
	}

	private String getPathToSoundDirectory() {
		return Utils.buildPath(Utils.buildProjectPath(ProjectManager.getInstance().getCurrentProject().getName()),
				getSceneNameBySoundInfo(), Constants.SOUND_DIRECTORY);
	}

	protected String getSceneNameBySoundInfo() {
		for (Scene scene : ProjectManager.getInstance().getCurrentProject().getSceneList()) {
			for (Sprite sprite : scene.getSpriteList()) {
				if (sprite.getSoundList().contains(this)) {
					return scene.getName();
				}
			}
		}
		return ProjectManager.getInstance().getCurrentScene().getName();
	}

	private String getPathToBackPackSoundDirectory() {
		return Utils.buildPath(Constants.DEFAULT_ROOT, Constants.BACKPACK_DIRECTORY,
				Constants.BACKPACK_SOUND_DIRECTORY);
	}

	@Override
	public int compareTo(SoundInfo soundInfo) {
		return name.compareTo(soundInfo.name);
	}

	@Override
	public String toString() {
		return name;
	}

	public boolean isBackpackSoundInfo() {
		return isBackpackSoundInfo;
	}

	public void setBackpackSoundInfo(boolean backpackSoundInfo) {
		for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
			Log.e(TAG, stackTraceElement.getMethodName() + " setting Backpack to " + backpackSoundInfo);
		}
		isBackpackSoundInfo = backpackSoundInfo;
	}
}
