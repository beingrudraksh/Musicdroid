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
package org.catrobat.musicdroid.pocketmusic.midi;


import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.NoteOff;
import com.leff.midi.event.NoteOn;
import com.leff.midi.event.ProgramChange;
import com.leff.midi.event.meta.Tempo;
import com.leff.midi.event.meta.Text;

import org.catrobat.musicdroid.pocketmusic.note.symbol.Project;
import org.catrobat.musicdroid.pocketmusic.note.MusicalInstrument;
import org.catrobat.musicdroid.pocketmusic.note.MusicalKey;
import org.catrobat.musicdroid.pocketmusic.note.NoteEvent;
import org.catrobat.musicdroid.pocketmusic.note.NoteName;
import org.catrobat.musicdroid.pocketmusic.note.Track;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class MidiToProjectConverter {

	private static final MusicalInstrument DEFAULT_INSTRUMENT = MusicalInstrument.ACOUSTIC_GRAND_PIANO;

	private int beatsPerMinute;

	public MidiToProjectConverter() {
		beatsPerMinute = Project.DEFAULT_BEATS_PER_MINUTE;
	}

	public Project readFileAndConvertMidi(File file) throws MidiException, FileNotFoundException, IOException {
		MidiFile midi = new MidiFile(file);

		validateMidiFile(midi);

		return convertMidi(midi);
	}

	protected void validateMidiFile(MidiFile midiFile) throws MidiException {
		if (midiFile.getTrackCount() > 0) {
			MidiTrack tempoTrack = midiFile.getTracks().get(0);

			Iterator<MidiEvent> it = tempoTrack.getEvents().iterator();

			if (it.hasNext()) {
				MidiEvent event = it.next();

				if (event instanceof Text) {
					Text text = (Text) event;

					if (text.getText().equals(ProjectToMidiConverter.MIDI_FILE_IDENTIFIER)) {
						return;
					}
				}
			}
		}

		throw new MidiException("Unsupported MIDI!");
	}

	protected Project convertMidi(MidiFile midi) {
		List<Track> tracks = new ArrayList<Track>();

		for (MidiTrack midiTrack : midi.getTracks()) {
			tracks.add(createTrack(midiTrack));
		}

		Project project = new Project(beatsPerMinute);

		for (Track track : tracks) {
			if (track.size() > 0) {
				project.addTrack(track);
			}
		}

		return project;
	}

	private Track createTrack(MidiTrack midiTrack) {
		MusicalInstrument instrument = getInstrumentFromMidiTrack(midiTrack);
		Track track = new Track(MusicalKey.VIOLIN, instrument);
		Iterator<MidiEvent> it = midiTrack.getEvents().iterator();

		while (it.hasNext()) {
			MidiEvent midiEvent = it.next();

			if (midiEvent instanceof NoteOn) {
				NoteOn noteOn = (NoteOn) midiEvent;
				long tick = noteOn.getTick();
				NoteName noteName = NoteName.getNoteNameFromMidiValue(noteOn.getNoteValue());
				NoteEvent noteEvent = new NoteEvent(noteName, true);

				track.addNoteEvent(tick, noteEvent);
			} else if (midiEvent instanceof NoteOff) {
				NoteOff noteOff = (NoteOff) midiEvent;
				long tick = noteOff.getTick();
				NoteName noteName = NoteName.getNoteNameFromMidiValue(noteOff.getNoteValue());
				NoteEvent noteEvent = new NoteEvent(noteName, false);

				track.addNoteEvent(tick, noteEvent);
			} else if (midiEvent instanceof Tempo) {
				Tempo tempo = (Tempo) midiEvent;

				beatsPerMinute = (int) tempo.getBpm();
			}
		}

		return track;
	}

	private MusicalInstrument getInstrumentFromMidiTrack(MidiTrack midiTrack) {
		Iterator<MidiEvent> it = midiTrack.getEvents().iterator();
        MusicalInstrument instrument = DEFAULT_INSTRUMENT;

		while (it.hasNext()) {
			MidiEvent midiEvent = it.next();

			if (midiEvent instanceof ProgramChange) {
				ProgramChange program = (ProgramChange) midiEvent;

				instrument = MusicalInstrument.getInstrumentFromProgram(program.getProgramNumber());
				break;
			}
		}

		return instrument;
	}
}
