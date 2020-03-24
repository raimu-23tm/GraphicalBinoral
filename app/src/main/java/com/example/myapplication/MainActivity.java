package com.example.myapplication;

import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MainActivity extends AppCompatActivity {

    //ソルフェジオ周波数リスト
    private static final int[] frequencyList = {528,174,285,396,417,639,741,852,4096};

    //代表バイノーラル音リスト
    private static final double[] binoralList = {1.7 ,5.5 ,9.4 ,30.0};

    //サンプル音リスト
    private static final int[] sampleFrequeList = {83 ,116 ,83 ,110 ,116 ,110 ,741 ,528};
    private static final double[] sampleBinoralList = {1.7 ,2.8 ,3.5 ,5.5 ,9.4 ,11.3 ,7.1 ,7.83};

    private int mainHz = 528;
    private double mainBinoralHz = 0;
    private boolean playFlg;
    private int waveType = 1;

    private static final int STREAM_TYPE = AudioManager.STREAM_MUSIC;
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_STEREO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
    private static final int MODE = AudioTrack.MODE_STREAM;
    private Oscillator osc = new Oscillator(BUFFER_SIZE, SAMPLE_RATE);
    private AudioTrack track;
    private Thread backgroundThread;
    private boolean running;

    private GLShaderViewRender mRenderer;

    /** 初期処理 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("単一周波数");
        setSupportActionBar(toolbar);

        //メイリオフォントをインポート
        Typeface customFont = Typeface.createFromAsset(getAssets(), "font/meiryo.ttc");
        TextView frequencyButton1 = findViewById(R.id.frequencyButton1);
        frequencyButton1.setTypeface(customFont);
        TextView frequencyButton5= findViewById(R.id.frequencyButton5);
        frequencyButton5.setTypeface(customFont);

        //タブ設定
        TabHost tabhost = findViewById(R.id.tabHost);
        tabhost.setup();

        TabSpec tab1 = tabhost.newTabSpec("tab1");
        tab1.setIndicator("タブ１");
        tab1.setContent(R.id.tab1);
        tabhost.addTab(tab1);
        TabSpec tab2 = tabhost.newTabSpec("tab2");
        tab2.setIndicator("タブ2");
        tab2.setContent(R.id.tab2);
        tabhost.addTab(tab2);
        TabSpec tab3 = tabhost.newTabSpec("tab3");
        tab3.setIndicator("タブ3");
        tab3.setContent(R.id.tab3);
        tabhost.addTab(tab3);
        TabHost.TabSpec tab4 = tabhost.newTabSpec("tab4");
        tab4.setIndicator("タブ4");
        tab4.setContent(R.id.tab4);
        tabhost.addTab(tab4);
        tabhost.setCurrentTab(0);

        //初期値設定
        playFlg = false;
        ChangeFrequency(mainHz);
        findViewById(R.id.plusText).setVisibility(View.INVISIBLE);
        findViewById(R.id.binoralText).setVisibility(View.INVISIBLE);



        final SeekBar sb1_1 = findViewById(R.id.seekBar1_1);
        final SeekBar sb1_2 = findViewById(R.id.seekBar1_2);
        final SeekBar sb2 = findViewById(R.id.seekBar2);

        /** シークバーを動かす */
        sb1_1.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progress, boolean fromUser) {

                        if (fromUser == true) {

                            mainHz = sb1_1.getProgress() + sb1_2.getProgress();

                            TextView textView = findViewById(R.id.HzText1);
                            textView.setText(mainHz + " Hz");

                        }
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // ツマミに触れたときに呼ばれる
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // ツマミを離したときに呼ばれる
                    }
                }
        );
        sb1_2.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progress, boolean fromUser) {

                        if (fromUser == true) {

                            mainHz = sb1_1.getProgress() + sb1_2.getProgress();

                            TextView textView = findViewById(R.id.HzText1);
                            textView.setText(mainHz + " Hz");

                        }
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // ツマミに触れたときに呼ばれる
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // ツマミを離したときに呼ばれる
                    }
                }
        );
        sb2.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progress, boolean fromUser) {

                        if (fromUser == true) {

                            mainBinoralHz = sb2.getProgress() / 10f;

                            TextView textView = findViewById(R.id.binoralText);
                            textView.setText(String.format("%.1f",mainBinoralHz) + " Hz");

                        }
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // ツマミに触れたときに呼ばれる
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // ツマミを離したときに呼ばれる
                    }
                }
        );

        //タブを変更する
        tabhost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {

                final Toolbar header = findViewById(R.id.toolbar);

                switch (tabId) {
                    case "tab1":
                        findViewById(R.id.tabview1).setVisibility(View.VISIBLE);
                        findViewById(R.id.tabview2).setVisibility(View.INVISIBLE);
                        findViewById(R.id.tabview3).setVisibility(View.INVISIBLE);
                        findViewById(R.id.tabview4).setVisibility(View.INVISIBLE);
                        if (mainBinoralHz == 0.0) {
                            findViewById(R.id.plusText).setVisibility(View.INVISIBLE);
                            findViewById(R.id.binoralText).setVisibility(View.INVISIBLE);
                        }
                        else
                        {
                            findViewById(R.id.plusText).setVisibility(View.VISIBLE);
                            findViewById(R.id.binoralText).setVisibility(View.VISIBLE);
                        }
                        header.setTitle("単一周波数");
                        break;
                    case "tab2":
                        findViewById(R.id.tabview1).setVisibility(View.INVISIBLE);
                        findViewById(R.id.tabview2).setVisibility(View.VISIBLE);
                        findViewById(R.id.tabview3).setVisibility(View.INVISIBLE);
                        findViewById(R.id.tabview4).setVisibility(View.INVISIBLE);
                        findViewById(R.id.plusText).setVisibility(View.VISIBLE);
                        findViewById(R.id.binoralText).setVisibility(View.VISIBLE);
                        header.setTitle("バイノーラル音");
                        break;
                    case "tab3":
                        findViewById(R.id.tabview1).setVisibility(View.INVISIBLE);
                        findViewById(R.id.tabview2).setVisibility(View.INVISIBLE);
                        findViewById(R.id.tabview3).setVisibility(View.VISIBLE);
                        findViewById(R.id.tabview4).setVisibility(View.INVISIBLE);
                        findViewById(R.id.plusText).setVisibility(View.VISIBLE);
                        findViewById(R.id.binoralText).setVisibility(View.VISIBLE);
                        header.setTitle("波系");
                        break;
                    case "tab4":
                        findViewById(R.id.tabview1).setVisibility(View.INVISIBLE);
                        findViewById(R.id.tabview2).setVisibility(View.INVISIBLE);
                        findViewById(R.id.tabview3).setVisibility(View.INVISIBLE);
                        findViewById(R.id.tabview4).setVisibility(View.VISIBLE);
                        findViewById(R.id.plusText).setVisibility(View.VISIBLE);
                        findViewById(R.id.binoralText).setVisibility(View.VISIBLE);
                        header.setTitle("サンプル音");
                        break;
                    default:

                        break;

                }

            }


        });

        // ナビゲーションアイコンの設定、クリック処理
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //インテントの作成
                Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                //遷移先の画面を起動
                startActivity(intent);

            }
        });

        // ラジオグループのオブジェクトを取得
        RadioGroup wavetypeRadioGroup = (RadioGroup)findViewById(R.id.wavetypeRadioGroup);

        // ラジオグループのチェック状態変更イベントを登録
        wavetypeRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                switch (checkedId)
                {
                    case R.id.type1:
                        waveType = 1;
                        break;
                    case R.id.type2:
                        waveType = 2;
                        break;
                    case R.id.type3:
                        waveType = 3;
                        break;
                    case R.id.type4:
                        waveType = 4;
                        break;
                    default:
                        break;
                }

            }

        });

        /** シェーダーの表示 */
        final GLSurfaceView glSurfaceView = findViewById(R.id.surfaceView);
        glSurfaceView.setEGLContextClientVersion(2);
        mRenderer = new GLShaderViewRender(this);
        glSurfaceView.setRenderer(mRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        mRenderer.frequency = mainHz;

    }

    /** 再生ボタン押下 */
    public void onClickPlayButton(View view) {

        if ( playFlg == false) {
            ((ImageView)findViewById(R.id.playButton)).setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
            playFlg = true;
        }
        else {
            ((ImageView)findViewById(R.id.playButton)).setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
            playFlg = false;
        }

    }

    /** ＋ボタン押下 */
    public void onClickPlusButton(View view) {
        // Do something in response to button

        mainHz = mainHz + 1;

        final SeekBar sb1_2 = findViewById(R.id.seekBar1_2);
        int sb1_2value = sb1_2.getProgress();
        if (sb1_2value < 200) {
            sb1_2.setProgress(sb1_2value + 1);
        }

        TextView textView = findViewById(R.id.HzText1);
        textView.setText(mainHz + " Hz");

    }

    /** －ボタン押下 */
    public void onClickMinusButton(View view) {

        mainHz = mainHz - 1;

        if (mainHz < 0){
            mainHz = 0;
        }

        final SeekBar sb1_2 = findViewById(R.id.seekBar1_2);
        int sb1_2value = sb1_2.getProgress();
        if (sb1_2value > 0) {
            sb1_2.setProgress(sb1_2value - 1);
        }

        TextView textView = findViewById(R.id.HzText1);
        textView.setText(mainHz + " Hz");

    }


    /** ＋ボタン押下 */
    public void onClickPlus2Button(View view) {

        if (mainBinoralHz <= 35) {
            mainBinoralHz = mainBinoralHz + 1;
        }
        else
        {
            mainBinoralHz = 36;
        }
        ChangeBinoralFrequency(mainBinoralHz);

    }
    public void onClickPlus2miniButton(View view) {

        if (mainBinoralHz < 36) {
            mainBinoralHz = mainBinoralHz + 0.1;
        }
        else
        {
            mainBinoralHz = 36;
        }
        ChangeBinoralFrequency(mainBinoralHz);

    }

    /** －ボタン押下 */
    public void onClickMinus2Button(View view) {

        if (mainBinoralHz >= 1) {
            mainBinoralHz = mainBinoralHz - 1;
        }
        else
        {
            mainBinoralHz = 0;
        }
        ChangeBinoralFrequency(mainBinoralHz);

    }
    public void onClickMinus2miniButton(View view) {

        if (mainBinoralHz > 0) {
            mainBinoralHz = mainBinoralHz - 0.1;
        }
        else
        {
            mainBinoralHz = 0;
        }
        ChangeBinoralFrequency(mainBinoralHz);

    }

    /** ソルフォジオ周波数系ボタン押下 */
    public void onClickSolfeggio1Button (View view) {
        ChangeFrequency(frequencyList[0]);
    }
    public void onClickSolfeggio2Button (View view) {
        ChangeFrequency(frequencyList[1]);
    }
    public void onClickSolfeggio3Button (View view) {
        ChangeFrequency(frequencyList[2]);
    }
    public void onClickSolfeggio4Button (View view) {
        ChangeFrequency(frequencyList[3]);
    }
    public void onClickSolfeggio5Button (View view) {
        ChangeFrequency(frequencyList[4]);
    }
    public void onClickSolfeggio6Button (View view) {
        ChangeFrequency(frequencyList[5]);
    }
    public void onClickSolfeggio7Button (View view) {
        ChangeFrequency(frequencyList[6]);
    }
    public void onClickSolfeggio8Button (View view) {
        ChangeFrequency(frequencyList[7]);
    }
    public void onClickSolfeggio9Button (View view) {
        ChangeFrequency(frequencyList[8]);
    }

    /** 周波数変更 */
    public void ChangeFrequency (int frequency) {

        mainHz = frequency;

        if (frequency < 4000) {

            final SeekBar sb1_2 = findViewById(R.id.seekBar1_2);
            int sb1_2value = sb1_2.getProgress();
            final SeekBar sb1_1 = findViewById(R.id.seekBar1_1);
            sb1_1.setProgress(mainHz - sb1_2value);

        }
        else
        {

            final SeekBar sb1_1 = findViewById(R.id.seekBar1_1);
            sb1_1.setProgress(4000);
            sb1_1.setProgress(frequency - 4000);

        }

        TextView textView = findViewById(R.id.HzText1);
        textView.setText(mainHz + " Hz");

    }

    /** バイノーラル周波数系ボタン押下 */
    public void onClickBinoral1Button (View view) {
        ChangeBinoralFrequency(binoralList[0]);
    }
    public void onClickBinoral2Button (View view) {
        ChangeBinoralFrequency(binoralList[1]);
    }
    public void onClickBinoral3Button (View view) {
        ChangeBinoralFrequency(binoralList[2]);
    }
    public void onClickBinoral4Button (View view) {
        ChangeBinoralFrequency(binoralList[3]);
    }

    /** バイノーラル周波数変更 */
    public void ChangeBinoralFrequency (double binoral) {

        mainBinoralHz = binoral;

        final SeekBar sb2 = findViewById(R.id.seekBar2);
        sb2.setProgress((int)binoral * 10);

        TextView textView = findViewById(R.id.binoralText);
        String formatText = String.format("%.1f",mainBinoralHz);
        textView.setText(formatText + " Hz");

    }

    /** サンプル系ボタン押下 */
    public void onClickSample1Button (View view) {
        ChangeFrequency(sampleFrequeList[0]);
        ChangeBinoralFrequency(sampleBinoralList[0]);
    }
    public void onClickSample2Button (View view) {
        ChangeFrequency(sampleFrequeList[1]);
        ChangeBinoralFrequency(sampleBinoralList[1]);
    }
    public void onClickSample3Button (View view) {
        ChangeFrequency(sampleFrequeList[2]);
        ChangeBinoralFrequency(sampleBinoralList[2]);
    }
    public void onClickSample4Button (View view) {
        ChangeFrequency(sampleFrequeList[3]);
        ChangeBinoralFrequency(sampleBinoralList[3]);
    }
    public void onClickSample5Button (View view) {
        ChangeFrequency(sampleFrequeList[4]);
        ChangeBinoralFrequency(sampleBinoralList[4]);
    }
    public void onClickSample6Button (View view) {
        ChangeFrequency(sampleFrequeList[5]);
        ChangeBinoralFrequency(sampleBinoralList[5]);
    }
    public void onClickSample7Button (View view) {
        ChangeFrequency(sampleFrequeList[6]);
        ChangeBinoralFrequency(sampleBinoralList[6]);
    }
    public void onClickSample8Button (View view) {
        ChangeFrequency(sampleFrequeList[7]);
        ChangeBinoralFrequency(sampleBinoralList[7]);
    }

    /** 画面表示前処理 */
    @Override
    protected void onResume() {
        super.onResume();

        // AudioTrackの作成(
        // STREAM_MUSIC,
        // 44100,
        // ステレオ,
        // 16bit,
        // バッファーサイズ,
        // Stream)
        track = new AudioTrack(STREAM_TYPE, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE, MODE);

        //スレッドスタート
        startBackgroundThread();
    }

    /** ホームボタン押下などした場合 */
    @Override
    protected void onPause() {

        //スレッドを止める。音を止める処理
        stopBackgroundThread();
        track.release();
        track = null;

        super.onPause();

    }

    private void startBackgroundThread() {
        running = true;

        //AudioTrackの再生
        track.play();

        //波形設定のリセット
        osc.reset();

        //スレッド作成
        backgroundThread = new Thread() {
            @Override
            public void run() {
                short[] sBuffer = new short[BUFFER_SIZE];
                while (running) {

                    //周波数設定
                    osc.frequency = mainHz;
                    osc.binoral = mainBinoralHz;
                    osc.waveType = waveType;

                    // 画面を触っている間のみ音を鳴らす
                    if (playFlg) {
                        double[] dBuffer = osc.nextBuffer();

                        // bufferには -1 〜 +1 のデータが入るので、shortの値域に変換する
                        for (int i = 0; i < BUFFER_SIZE; i++) {
                            sBuffer[i] = (short) (dBuffer[i] * Short.MAX_VALUE);
                        }
                    } else {
                        // 無音
                        for (int i = 0; i < BUFFER_SIZE; i++) {
                            sBuffer[i] = 0;
                        }
                    }
                    track.write(sBuffer, 0, BUFFER_SIZE);
                }
            }
        };
        //スレッド起動
        backgroundThread.start();
    }

    private void stopBackgroundThread() {
        running = false;
        track.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
