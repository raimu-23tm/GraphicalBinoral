package com.example.myapplication;

import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;

public class GLShaderViewRender extends GLSurfaceView implements GLSurfaceView.Renderer {

    private int _gl_prog;
    private HashMap<String, Integer> _gl_vars;

    private FloatBuffer _vertex_buffer;

    private String flagmentShader = "";
    private String vertexShader = "";
    private InputStream flagmentShaderIS = null;
    private InputStream vertexShaderIS = null;
    private BufferedReader flagmentShadeBR = null;
    private BufferedReader vertexShaderBR = null;

    private int v_shader;
    private int f_shader;
    private String vs_code;
    private String fs_code;

    private float canvasRate = 0;

    private int _draw_counter;

    public float frequency;

    private float[] mViewAndProjectionMatrix = new float[16];


    public GLShaderViewRender(MainActivity activity) {

        super(activity);

        // OpenGL ES 2
        setEGLContextClientVersion(2);

        _draw_counter = 0;

        setRenderer(this);

        // 自動で連続描画を行う
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        try {
            try {
                // assetsフォルダ内の txt をオープンする
                AssetManager assetManager = getResources().getAssets();
                vertexShaderIS = assetManager.open("vertexShader.txt");
                flagmentShaderIS = assetManager.open("flagmentShader.txt");
                vertexShaderBR = new BufferedReader(new InputStreamReader(vertexShaderIS));
                flagmentShadeBR = new BufferedReader(new InputStreamReader(flagmentShaderIS));

                // １行ずつ読み込む
                String str = "";
                while ((str = vertexShaderBR.readLine()) != null) {
                    vertexShader += str;
                }
                str = "";
                while ((str = flagmentShadeBR.readLine()) != null) {
                    flagmentShader += str;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (vertexShaderIS != null) vertexShaderIS.close();
                if (flagmentShaderIS != null) flagmentShaderIS.close();
                if (vertexShaderBR != null) vertexShaderBR.close();
                if (flagmentShadeBR != null) flagmentShadeBR.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        vs_code = vertexShader;
        fs_code = flagmentShader;

        v_shader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        f_shader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

        // Vertexシェーダーのコードをコンパイル
        GLES20.glShaderSource(v_shader, vs_code);
        GLES20.glCompileShader(v_shader);

        // Fragmentシェーダーのコードをコンパイル
        GLES20.glShaderSource(f_shader, fs_code);
        GLES20.glCompileShader(f_shader);

        // Programを作成
        _gl_prog = GLES20.glCreateProgram();

        // Programのシェーダーを設定
        GLES20.glAttachShader(_gl_prog, v_shader);
        GLES20.glAttachShader(_gl_prog, f_shader);

        GLES20.glLinkProgram(_gl_prog);
        GLES20.glUseProgram(_gl_prog);

        _gl_vars = new  HashMap<String, Integer>();

        // gl変数を保存
        _gl_vars.put("A_position", GLES20.glGetAttribLocation(_gl_prog, "A_position"));
        _gl_vars.put("A_canvasrate", GLES20.glGetAttribLocation(_gl_prog, "A_canvasrate"));
        _gl_vars.put("A_time", GLES20.glGetAttribLocation(_gl_prog, "A_time"));
        _gl_vars.put("A_frequency", GLES20.glGetAttribLocation(_gl_prog, "A_frequency"));

        // 縮小時の補間設定
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        // 拡大時の補間設定
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        // 全体の長方形頂点データ作成
        float[] vertex_array = {-1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f};

        // 頂点データをバッファに格納
        ByteBuffer vt_bb = ByteBuffer.allocateDirect(vertex_array.length * 4);
        vt_bb.order(ByteOrder.nativeOrder());
        _vertex_buffer = vt_bb.asFloatBuffer();
        _vertex_buffer.put(vertex_array);
        _vertex_buffer.position(0);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        canvasRate = (float)width / (float)height;

        GLES20.glViewport(0, 0, width, height);

    }

    @Override
    public void onDrawFrame(GL10 gl) {

        int time = _draw_counter;

        GLES20.glEnableVertexAttribArray(_gl_vars.get("A_position"));
        GLES20.glVertexAttribPointer(_gl_vars.get("A_position"), 2, GLES20.GL_FLOAT, false, 0, _vertex_buffer);

        // 長方形を描画
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(_gl_vars.get("A_position"));

        // 描画ビュー比率設定
        GLES20.glVertexAttrib1f(_gl_vars.get("A_canvasrate"), canvasRate);

        // 時間経過設定
        GLES20.glVertexAttrib1f(_gl_vars.get("A_time"), time);

        // 周波数の設定
        GLES20.glVertexAttrib1f(_gl_vars.get("A_frequency"), frequency);

        _draw_counter++;

    }

}
