

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

package net.happybrackets.kadenze_course.assignment_tasks.session3;

import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.happybrackets.controller.gui.WaveformVisualiser;
import net.happybrackets.extras.assignment_autograding.BeadsChecker;

/**
 * In this task you will make a three-oscillator FM synth from scratch. This consists of three oscillators, each
 * modulating the next in turn.
 *
 * The frequency of the carrier oscillator = baseFrequency + mod1Output * mod1ModStrength.
 * The frequency of the mod1 oscillator = baseFrequency * mod1FreqMult + mod2Output * mod2ModStrength.
 * The frequency of the mod2 oscillator = baseFrequency * mod2FreqMult.
 *
 * All oscillators should be sine waves.
 */
public class CodeTask3_6 extends Application implements BeadsChecker.BeadsCheckable {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        //the AudioContext
        AudioContext ac = new AudioContext();
        ac.start();
        //a StringBuffer used to record anything you want to print out
        StringBuffer buf = new StringBuffer();
        //parameters that are used in this task
        float baseFreq = 440f;
        float mod1FreqMult = 2;
        float mod1ModStrength = 300;
        float mod2FreqMult = 4;
        float mod2ModStrength = 400;
        //do your work here, using the function below
        task(ac, buf, new Object[]{baseFreq, mod1FreqMult, mod1ModStrength, mod2FreqMult, mod2ModStrength});
        //say something to the console output
        System.out.println(buf.toString());
        //finally, this creates a window to visualise the waveform
        WaveformVisualiser.open(ac);
    }

    @Override
    public void task(AudioContext ac, StringBuffer stringBuffer, Object... objects) {
        //********** do your work here ONLY **********
        float baseFreq = (Float) objects[0];
        float mod1FreqMult = (Float) objects[1];
        float mod1ModStrength = (Float) objects[2];
        float mod2FreqMult = (Float) objects[3];
        float mod2ModStrength = (Float) objects[4];

        ac.out.setGain(0.1f);
        //********** do your work here ONLY **********
    }

}
