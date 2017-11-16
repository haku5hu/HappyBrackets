/*
 * Copyright 2016 Ollie Bown
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.happybrackets.kadenze_course.assignment_tasks.session7;

import de.sciss.net.OSCMessage;
import net.beadsproject.beads.core.AudioContext;
import net.happybrackets.extras.assignment_autograding.BeadsChecker;

/**
 * In this task you receive an OSC message which represents a chord. The first argument gives a wave form, either SINE,
 * SAW or SQUARE. The remaining arguments to the message consist of pairs of MIDI-note and gain values.
 *
 * However, due to a miscommunication about communication standards, some manufacturers understood that the waveform
 * argument can actually go at the end of the list instead of at the beginning.
 *
 * Other manufacturers have decided that after the list of numbers and the waveform argument, they will add any number
 * of other parameters of the form: x, f, where x is a String and f is a float.
 *
 * Your code should print out the OSC data in the following format:
 * Waveform=SINE
 * Note1: MIDI=60, gain=0.1
 * Note2: MIDI=63, gain=0.2
 * Note3: MIDI=67, gain=0.1
 * filtfreq=500
 * filtq=0.2
 *
 * Note that wherever numbers are concerned, you should read those in as numbers, not a strings, before printing them,
 * and choose appropriate representations, e.g., MIDI notes should be integers, and for unknown parameters you should
 * check to see if the number is best interpreted as an int or a float.
 *
 * Last but not least, you should only do the above when the message received has the OSC address "/chord". However,
 * let's assume that different forms of capitalisation (Chord, CHORD, etc.) are accepted. Also let's assume that the
 * beginning forward slash is not necessary.
 *
 * For any messages that are not with address "/chord", print out:
 * Message received with address "/theAddress"
 *
 * Remember to print to the StringBuffer, not to System.out.
 */
public class CodeTask7_1 implements BeadsChecker.BeadsCheckable {

    public static void main(String[] args) {
        StringBuffer buf = new StringBuffer();
        OSCMessage message = new OSCMessage("/chord", new Object[]{"SINE", 60, 0.1f, 63, 0.2f, 67, 0.1f, "filtfreq", 500f, "filtq", 0.2f});
        new CodeTask7_1().task(null, buf, new Object[]{message});
        System.out.println(buf);
    }

    @Override
    public void task(AudioContext ac, StringBuffer buf, Object... objects) {
        //********** do your work here ONLY **********
        //your objects...
        OSCMessage message = (OSCMessage) objects[0];
        //do stuff here, remove the following line
        buf.append("Hello World!\n");
        //********** do your work here ONLY **********
    }
}
