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

package net.happybrackets.kadenze_course.tutorial.session3;

import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.controller.gui.WaveformVisualiser;

/**
 * In this example we explore loading and playing back samples, using granular sample playback.
 *
 * In the same example, towards the bottom, we are also creating an amplitude-modulated wave player.
 */
public class Example3_1 extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //set up the audio context
        AudioContext ac = new AudioContext();
        ac.start();
        //load sample
        Sample s = SampleManager.sample("data/audio/Nylon_Guitar/Clean_A_harm.wav");
        GranularSamplePlayer sp = new GranularSamplePlayer(ac, s);
        //set rate and pitch parameters
        Envelope e = new Envelope(ac, 2);
        e.addSegment(1, 3000);
        sp.setRate(e);
        sp.getPitchUGen().setValue(4);
        //set loop parameters
        sp.setLoopType(SamplePlayer.LoopType.LOOP_ALTERNATING);
        Envelope loopEndEnv = new Envelope(ac, 500);
        sp.setLoopEnd(loopEndEnv);
        loopEndEnv.addSegment(1, 5000);
        //set grain parameters (note if this was a SamplePlayer then these params wouldn't exist).
        sp.getGrainIntervalUGen().setValue(10);
        sp.getGrainSizeUGen().setValue(20);
        sp.getRandomnessUGen().setValue(0.01f);
        //play the granulated sound
        ac.out.addInput(sp);
        //create an LFO
        WavePlayer lfo = new WavePlayer(ac, 1, Buffer.SINE);
        //create an oscillator
        WavePlayer wp = new WavePlayer(ac, 500, Buffer.SINE);
        //set up the LFO to perform amplitude modulation to the oscillator
        ZMap mappedLFOToGain = new ZMap(ac, 1);
        mappedLFOToGain.setRanges(-1, 1, 0, 0.1f);   //set input and output ranges
        mappedLFOToGain.addInput(lfo);
        Gain g = new Gain(ac, 1, mappedLFOToGain);
        g.addInput(wp);
        //play back this modulated waveform sound
        ac.out.addInput(g);
        //visualiser
        WaveformVisualiser.open(ac);

    }
}
