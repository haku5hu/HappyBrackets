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

package net.happybrackets.kadenze_course.tutorial.session5;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

/**
 * This example explores the behaviour of an HBAction when you send it multiple times to the Pi.
 *
 * Send the following HBAction to your Pi.
 * Send it a second time. You will hear two sounds.
 * Now uncomment the hb.reset() line and send the action once again.
 * Try each of the four uncommented lines, one at a time and compare their effects.
 * You can also evoke the same actions using the interface in the HB plugin.
 */
public class Example5_1 implements HBAction {

    @Override
    public void action(HB hb) {
        /*
         * experiment with the following options for clearing sounds
         */
//        hb.reset();
//        hb.resetLeaveSounding();
//        hb.fadeOutReset(1000);
//        hb.clearSound();
        /*
         * make a new sound, with a random note from minor scale...
         */

        int pitch = Pitch.forceToScale(hb.rng.nextInt(65) + 48, Pitch.minor);
        float freq = Pitch.mtof(pitch);

        WavePlayer sawWave = new WavePlayer(hb.ac, freq, Buffer.SAW);
        Gain g = new Gain(hb.ac, 1, 0.1f);
        g.addInput(sawWave);

        //play the sound
        hb.sound(g);


    }

}
