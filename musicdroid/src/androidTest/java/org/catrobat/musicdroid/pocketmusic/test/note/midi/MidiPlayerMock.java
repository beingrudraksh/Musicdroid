/*
 * Musicdroid: An on-device music generator for Android
 * Copyright (C) 2010-2014 The Catrobat Team
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

package org.catrobat.musicdroid.pocketmusic.test.note.midi;

import android.app.Activity;
import android.media.MediaPlayer;

import org.catrobat.musicdroid.pocketmusic.note.NoteName;
import org.catrobat.musicdroid.pocketmusic.note.midi.MidiPlayer;

import java.io.File;
import java.util.Queue;

public class MidiPlayerMock extends MidiPlayer {

    public Queue<NoteName> getPlayQueue() {
        return playQueue;
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public void stop() {
        player.stop();
    }

    @Override
     protected MediaPlayer createPlayer(Activity activity, int midiFileId) {
        return new MediaPlayerMock();
    }

    public void onPlayNoteCompletionCallback(final Activity activity) {
        super.restartPlayerThroughQueue(activity);
    }

    @Override
    protected MediaPlayer createPlayer(Activity activity, File file) {
        return new MediaPlayerMock();
    }

    @Override
    protected int getMidiResourceId(final Activity activity, final NoteName noteName) {
        return 1;
    }

    private class MediaPlayerMock extends MediaPlayer {

        private boolean isPlaying;

        public MediaPlayerMock() {
            isPlaying = false;
        }

        @Override
        public void start() {
            isPlaying = true;
        }

        @Override
        public void stop() {
            isPlaying = false;
        }

        @Override
        public boolean isPlaying() {
            return isPlaying;
        }
    }
}
