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
package org.catrobat.catroid.content;

import com.badlogic.gdx.scenes.scene2d.actions.ParallelAction;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.common.LookInfo;
import org.catrobat.catroid.content.actions.SetLookAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class BackgroundWaitHandler {

	private static HashMap<LookInfo, Integer> numberOfRunningScriptsOfLookData = new HashMap<>();
	private static HashMap<Sprite, HashMap<LookInfo, ParallelAction>> actionsOfLookDataPerSprite = new HashMap<>();
	private static HashMap<LookInfo, ArrayList<SetLookAction>> observingActions = new HashMap<>();

	private BackgroundWaitHandler() {
		throw new AssertionError();
	}

	public static void reset() {
		numberOfRunningScriptsOfLookData.clear();
		actionsOfLookDataPerSprite.clear();
		observingActions.clear();
	}

	public static synchronized void decrementRunningScripts(LookInfo lookInfo) {
		Integer counter = numberOfRunningScriptsOfLookData.get(lookInfo);
		if (counter != null) {
			numberOfRunningScriptsOfLookData.put(lookInfo, --counter);
			if (counter == 0) {
				notifyObservingActions(lookInfo);
			}
		}
	}

	public static void addObserver(LookInfo lookInfo, SetLookAction action) {
		ArrayList<SetLookAction> actions = observingActions.get(lookInfo);
		if (actions == null) {
			actions = new ArrayList<>();
			observingActions.put(lookInfo, actions);
		}
		actions.add(action);
	}

	public static void notifyObservingActions(LookInfo lookInfo) {
		ArrayList<SetLookAction> actions = observingActions.get(lookInfo);
		if (actions == null) {
			return;
		}

		for (SetLookAction action : actions) {
			action.notifyScriptsCompleted();
		}
		actions.clear();
	}

	private static void resetNumberOfReceivers(LookInfo lookInfo) {
		List<Sprite> spriteList = ProjectManager.getInstance().getCurrentProject().getSpriteListWithClones();

		Integer scriptsToRun = 0;
		for (Sprite sprite : spriteList) {
			scriptsToRun += sprite.getNumberOfWhenBackgroundChangesScripts(lookInfo);
		}
		numberOfRunningScriptsOfLookData.put(lookInfo, scriptsToRun);
		if (scriptsToRun == 0) {
			notifyObservingActions(lookInfo);
		}
	}

	public static void fireBackgroundChangedEvent(LookInfo lookInfo) {
		numberOfRunningScriptsOfLookData.put(lookInfo, 0);
		resetNumberOfReceivers(lookInfo);

		List<Sprite> spriteList = ProjectManager.getInstance().getCurrentProject().getSpriteListWithClones();

		for (Sprite sprite : spriteList) {
			HashMap<LookInfo, ParallelAction> mapOfSprite = actionsOfLookDataPerSprite.get(sprite);
			if (mapOfSprite == null) {
				mapOfSprite = new HashMap<>();
				actionsOfLookDataPerSprite.put(sprite, mapOfSprite);
			}

			ParallelAction action = mapOfSprite.get(lookInfo);
			if (action == null) {
				action = sprite.createBackgroundChangedAction(lookInfo);
				mapOfSprite.put(lookInfo, action);
			} else {
				Look.actionsToRestartAdd(action);
			}
		}
	}
}
