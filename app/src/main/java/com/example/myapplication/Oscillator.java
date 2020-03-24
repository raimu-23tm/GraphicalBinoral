package com.example.myapplication;

import android.util.Log;

public class Oscillator {
    public double frequency;
    public double binoral;
    public int waveType;
    private double[] buffer;
    private double t = 0;
    private double sampleRate;

    public Oscillator(int bufferSize, int sampleRate) {
        buffer = new double[bufferSize];
        this.sampleRate = sampleRate;
        waveType = 1;
    }

    /**
     * 呼び出す度に波形の一部を生成する。
     */
    public double[] nextBuffer() {

        for (int i = 0; i < buffer.length; i += 2) {

            switch (waveType) {

                case 1: //サイン波

                    buffer[i] = Math.sin(2 * Math.PI * t * (frequency));
                    buffer[i + 1] = Math.sin(2 * Math.PI * t * (frequency + binoral));
                    break;

                case 2: //三角波

                    buffer[i] = ((Math.abs(((2 * Math.PI * t * frequency + Math.PI) % (2 * Math.PI) / Math.PI ) - 1.0 ) * 2 ) - 1)  / 2;
                    buffer[i + 1] =  ((Math.abs(((2 * Math.PI * t * (frequency+ binoral) + Math.PI) % (2 * Math.PI) / Math.PI ) - 1.0 ) * 2 ) - 1) / 2;
                    break;

                case 3: //短形波

                    buffer[i] = (Math.sin(2 * Math.PI * t * (frequency)) > 0 ? 1 : -1) / 8.0;
                    buffer[i + 1] = (Math.sin(2 * Math.PI * t * (frequency + binoral)) > 0 ? 1 : -1) / 8.0;
                    break;

                case 4: //ノコギリ波

                    buffer[i] = (((2 * Math.PI * t * (frequency) + Math.PI) % (Math.PI * 2) / Math.PI) - 1.0) / 4.0;
                    buffer[i + 1] = (((2 * Math.PI * t * (frequency + binoral) + Math.PI) % (Math.PI * 2) / Math.PI) - 1.0) / 4.0;
                    break;

                default:
                    break;

            }

            t += 1 / sampleRate;
        }
        return buffer;

    }

    /**
     * 生成波形の位相を初期状態に戻す。
     */
    public void reset() {
        t = 0;
    }

}