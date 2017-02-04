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

import android.os.AsyncTask;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.common.Backpack;
import org.catrobat.catroid.common.LookData;
import org.catrobat.catroid.common.SoundInfo;
import org.catrobat.catroid.content.Scene;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.bricks.Brick;
import org.catrobat.catroid.content.bricks.SceneStartBrick;
import org.catrobat.catroid.content.bricks.SceneTransitionBrick;
import org.catrobat.catroid.content.bricks.UserBrick;
import org.catrobat.catroid.io.StorageHandler;
import org.catrobat.catroid.ui.adapter.LookBaseAdapter;
import org.catrobat.catroid.ui.adapter.SoundBaseAdapter;
import org.catrobat.catroid.utils.UtilFile;
import org.catrobat.catroid.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class BackPackListManager {
	private static Backpack backpack = new Backpack();
	private static SoundBaseAdapter currentSoundAdapter;
	private static LookBaseAdapter currentLookAdapter;

	public static void addLookToBackPack(LookData lookData) {
		backpack.backpackedLooks.add(lookData);
	}

	public static List<LookData> getBackPackedLooks() {
		return backpack.backpackedLooks;
	}

	public static void clearBackPackScripts() {
		backpack.backpackedScripts.clear();
		backpack.hiddenBackpackedScripts.clear();
	}

	public static void removeItemFromScriptBackPack(String scriptGroup) {
		backpack.backpackedScripts.remove(scriptGroup);
	}

	public static ArrayList<String> getBackPackedScriptGroups() {
		return new ArrayList<>(backpack.backpackedScripts.keySet());
	}

	public static void addScriptToBackPack(String scriptGroup, List<Script> scripts) {
		backpack.backpackedScripts.put(scriptGroup, scripts);
	}

	public static HashMap<String, List<Script>> getBackPackedScripts() {
		return backpack.backpackedScripts;
	}

	public static HashMap<String, List<Script>> getAllBackPackedScripts() {
		HashMap<String, List<Script>> allScripts = new HashMap<>();
		allScripts.putAll(backpack.backpackedScripts);
		allScripts.putAll(backpack.hiddenBackpackedScripts);
		return allScripts;
	}

	public static void clearBackPackUserBricks() {
		//TODO: remove
	}

	public static void removeItemFromUserBrickBackPack(String userBrickGroup) {
		//TODO: remove
	}

	public static ArrayList<String> getBackPackedUserBrickGroups() {
		//TODO: remove
		return new ArrayList<>();
	}

	public static void addUserBrickToBackPack(String userBrickGroup, List<UserBrick> userBricks) {
		//TODO: remove
	}

	public static HashMap<String, List<UserBrick>> getBackPackedUserBricks() {
		//TODO: remove
		return new HashMap<>();
	}

	public static void clearBackPackLooks() {
		backpack.backpackedLooks.clear();
		backpack.hiddenBackpackedLooks.clear();
	}

	public static void removeItemFromLookBackPack(LookData lookData) {
		backpack.backpackedLooks.remove(lookData);
	}

	public static void removeItemFromLookBackPackByLookName(String name) {
		for (LookData lookData : backpack.backpackedLooks) {
			if (lookData.getLookName().equals(name)) {
				backpack.backpackedLooks.remove(lookData);
			}
		}
	}

	public static List<SoundInfo> getBackPackedSounds() {
		return backpack.backpackedSounds;
	}

	public static void clearBackPackSounds() {
		backpack.backpackedSounds.clear();
		backpack.hiddenBackpackedSounds.clear();
	}

	public static void addSoundToBackPack(SoundInfo soundInfo) {
		backpack.backpackedSounds.add(soundInfo);
	}

	public static void removeItemFromSoundBackPack(SoundInfo currentSoundInfo) {
		backpack.backpackedSounds.remove(currentSoundInfo);
	}

	public static void removeItemFromSoundBackPackBySoundTitle(String title) {
		for (SoundInfo soundInfo : backpack.backpackedSounds) {
			if (soundInfo.getTitle().equals(title)) {
				backpack.backpackedSounds.remove(soundInfo);
			}
		}
	}

	public static List<Scene> getAllBackpackedScenes() {
		List<Scene> result = new ArrayList<>();
		result.addAll(backpack.backpackedScenes);
		result.addAll(backpack.hiddenBackpackedScenes);
		return result;
	}

	public static List<Scene> getBackPackedScenes() {
		return backpack.backpackedScenes;
	}

	private static List<Scene> getHiddenBackPackedScenes() {
		return backpack.hiddenBackpackedScenes;
	}

	public static Scene getHiddenSceneByName(String name) {
		for (Scene scene : backpack.hiddenBackpackedScenes) {
			if (scene.getName().equals(name)) {
				return scene;
			}
		}
		return null;
	}

	public static void clearBackPackScenes() {
		backpack.backpackedScenes.clear();
	}

	public static void addSceneToBackPack(Scene scene) {
		backpack.backpackedScenes.add(scene);
	}

	public static void addSceneToHiddenBackpack(Scene scene) {
		backpack.hiddenBackpackedScenes.add(scene);
	}

	public static void removeItemFromSceneBackPackByName(String title, boolean hidden) {
		List<Scene> toRemove = new ArrayList<>();
		for (Scene scene : backpack.backpackedScenes) {
			if (scene.getName().equals(title)) {
				toRemove.add(scene);
				UtilFile.deleteDirectory(new File(Utils.buildBackpackScenePath(scene.getName())));
			}
		}
		(hidden ? getHiddenBackPackedScenes() : getBackPackedScenes()).removeAll(toRemove);
	}

	public static List<Sprite> getBackPackedSprites() {
		return backpack.backpackedSprites;
	}

	public static void clearBackPackSprites() {
		backpack.backpackedSprites.clear();
		backpack.hiddenBackpackedSprites.clear();
	}

	public static void addSpriteToBackPack(Sprite sprite) {
		backpack.backpackedSprites.add(sprite);
	}

	public static void removeItemFromSpriteBackPack(Sprite sprite) {
		backpack.backpackedSprites.remove(sprite);
	}

	public static void removeItemFromSpriteBackPackByName(String name) {
		List<Sprite> sprites = backpack.backpackedSprites;
		for (int spritePosition = 0; spritePosition < sprites.size(); spritePosition++) {
			Sprite sprite = backpack.backpackedSprites.get(spritePosition);
			if (sprite.getName().equals(name)) {
				backpack.backpackedSprites.remove(sprite);
			}
		}
	}

	public static List<LookData> getHiddenBackpackedLooks() {
		return backpack.hiddenBackpackedLooks;
	}

	public static void removeItemFromScriptHiddenBackpack(String scriptGroup) {
		backpack.hiddenBackpackedScripts.remove(scriptGroup);
	}

	public static void addScriptToHiddenBackpack(String scriptGroup, List<Script> scripts) {
		backpack.hiddenBackpackedScripts.put(scriptGroup, scripts);
	}

	public static HashMap<String, List<Script>> getHiddenBackpackedScripts() {
		return backpack.hiddenBackpackedScripts;
	}

	public static void removeItemFromLookHiddenBackpack(LookData lookData) {
		backpack.hiddenBackpackedLooks.remove(lookData);
	}

	public static List<SoundInfo> getHiddenBackpackedSounds() {
		return backpack.hiddenBackpackedSounds;
	}

	public static void addSoundToHiddenBackpack(SoundInfo soundInfo) {
		backpack.hiddenBackpackedSounds.add(soundInfo);
	}

	public static void removeItemFromSoundHiddenBackpack(SoundInfo currentSoundInfo) {
		backpack.hiddenBackpackedSounds.remove(currentSoundInfo);
	}

	public static List<Sprite> getHiddenBackpackedSprites() {
		return backpack.hiddenBackpackedSprites;
	}

	public static void addSpriteToHiddenBackpack(Sprite sprite) {
		backpack.hiddenBackpackedSprites.add(sprite);
	}

	public static void removeItemFromSpriteHiddenBackpack(Sprite sprite) {
		backpack.hiddenBackpackedSprites.remove(sprite);
	}

	static boolean backPackedSoundsContain(SoundInfo soundInfo, boolean onlyVisible) {
		List<SoundInfo> backPackedSounds = onlyVisible ? getBackPackedSounds() : getAllBackPackedSounds();
		for (SoundInfo backPackedSound : backPackedSounds) {
			if (backPackedSound.equals(soundInfo)) {
				return true;
			}
		}
		return false;
	}

	static boolean backPackedLooksContain(LookData lookData, boolean onlyVisible) {
		List<LookData> backPackedLooks = onlyVisible ? getBackPackedLooks() : getAllBackPackedLooks();
		for (LookData backPackedLook : backPackedLooks) {
			if (backPackedLook.equals(lookData)) {
				return true;
			}
		}
		return false;
	}

	static boolean backPackedSpritesContains(Sprite sprite, boolean onlyVisible) {
		List<Sprite> backPackedSprites = onlyVisible ? getBackPackedSprites() : getAllBackPackedSprites();
		for (Sprite backPackedSprite : backPackedSprites) {
			if (backPackedSprite.equals(sprite)) {
				return true;
			}
		}
		return false;
	}

	public static boolean backPackedScenesContains(Scene scene, boolean onlyVisible) {
		List<Scene> toSearch = onlyVisible ? getBackPackedScenes() : getHiddenBackPackedScenes();
		for (Scene backPackedScene : toSearch) {
			if (backPackedScene.getName().equals(scene.getName())) {
				return true;
			}
		}
		return false;
	}

	public static ArrayList<String> getAllBackPackedScriptGroups() {
		ArrayList<String> allScriptGroups = new ArrayList<>();
		allScriptGroups.addAll(new ArrayList<>(backpack.backpackedScripts.keySet()));
		allScriptGroups.addAll(new ArrayList<>(backpack.backpackedScripts.keySet()));
		return allScriptGroups;
	}

	public static List<LookData> getAllBackPackedLooks() {
		List<LookData> allLooks = new ArrayList<>();
		allLooks.addAll(backpack.backpackedLooks);
		allLooks.addAll(backpack.hiddenBackpackedLooks);
		return allLooks;
	}

	public static List<SoundInfo> getAllBackPackedSounds() {
		List<SoundInfo> allSounds = new ArrayList<>();
		allSounds.addAll(backpack.backpackedSounds);
		allSounds.addAll(backpack.hiddenBackpackedSounds);
		return allSounds;
	}

	public static List<Sprite> getAllBackPackedSprites() {
		List<Sprite> allSprites = new ArrayList<>();
		allSprites.addAll(backpack.backpackedSprites);
		allSprites.addAll(backpack.hiddenBackpackedSprites);
		return allSprites;
	}

	static SoundBaseAdapter getCurrentSoundAdapter() {
		return currentSoundAdapter;
	}

	public static void setCurrentSoundAdapter(SoundBaseAdapter adapter) {
		currentSoundAdapter = adapter;
	}

	static LookBaseAdapter getCurrentLookAdapter() {
		return currentLookAdapter;
	}

	public static void setCurrentLookAdapter(LookBaseAdapter currentLookAdapter) {
		BackPackListManager.currentLookAdapter = currentLookAdapter;
	}

	public static void addLookToHiddenBackPack(LookData newLookData) {
		backpack.hiddenBackpackedLooks.add(newLookData);
	}

	public static boolean isBackpackEmpty() {
		return getAllBackPackedLooks().isEmpty() && getAllBackPackedScriptGroups().isEmpty()
				&& getAllBackPackedSounds().isEmpty() && getAllBackPackedSprites().isEmpty();
	}

	public static void saveBackpack() {
		SaveBackpackAsynchronousTask saveTask = new SaveBackpackAsynchronousTask();
		saveTask.execute();
	}

	public static void loadBackpack() {
		LoadBackpackAsynchronousTask loadTask = new LoadBackpackAsynchronousTask();
		loadTask.execute();
	}

	public static Backpack getBackpack() {
		if (backpack == null) {
			backpack = new Backpack();
		}
		return backpack;
	}

	public static void searchForHiddenScenes(Scene sceneToSearch, ArrayList<Scene> foundScenes, boolean inBackpack) {
		for (Sprite sprite : sceneToSearch.getSpriteList()) {
			for (Brick brick : sprite.getListWithAllBricks()) {
				if (brick instanceof SceneTransitionBrick) {
					Scene transitionScene;
					if (inBackpack) {
						transitionScene = BackPackListManager.getHiddenSceneByName(((SceneTransitionBrick) brick).getSceneForTransition());
						if (transitionScene == null) {
							continue;
						}
					} else {
						transitionScene = ProjectManager.getInstance().getCurrentProject().getSceneByName(((SceneTransitionBrick) brick).getSceneForTransition());
						if (transitionScene == null) {
							continue;
						}
					}
					if (!foundScenes.contains(transitionScene)) {
						foundScenes.add(transitionScene);
						searchForHiddenScenes(transitionScene, foundScenes, inBackpack);
					}
				}
				if (brick instanceof SceneStartBrick) {
					Scene startScene;
					if (inBackpack) {
						startScene = BackPackListManager.getHiddenSceneByName(((SceneStartBrick) brick).getSceneToStart());
						if (startScene == null) {
							continue;
						}
					} else {
						startScene = ProjectManager.getInstance().getCurrentProject().getSceneByName(((SceneStartBrick) brick).getSceneToStart());
						if (startScene == null) {
							continue;
						}
					}
					if (!foundScenes.contains(startScene)) {
						foundScenes.add(startScene);
						searchForHiddenScenes(startScene, foundScenes, inBackpack);
					}
				}
			}
		}
	}

	private static class SaveBackpackAsynchronousTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			StorageHandler.getInstance().saveBackpack(getBackpack());
			return null;
		}
	}

	private static class LoadBackpackAsynchronousTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			backpack = StorageHandler.getInstance().loadBackpack();
			setBackPackFlags();
			ProjectManager.getInstance().checkNestingBrickReferences(false, true);
			return null;
		}

		private void setBackPackFlags() {
			for (LookData lookData : getAllBackPackedLooks()) {
				lookData.isBackpackLookData = true;
			}
			for (SoundInfo soundInfo : getAllBackPackedSounds()) {
				soundInfo.setBackpackSoundInfo(true);
			}
			for (Sprite sprite : getAllBackPackedSprites()) {
				sprite.isBackpackObject = true;
				for (LookData lookData : sprite.getLookDataList()) {
					lookData.isBackpackLookData = true;
				}
				for (SoundInfo soundInfo : sprite.getSoundList()) {
					soundInfo.setBackpackSoundInfo(true);
				}
			}
			for (Scene scene : getBackPackedScenes()) {
				scene.isBackPackScene = true;
			}
		}
	}
}
