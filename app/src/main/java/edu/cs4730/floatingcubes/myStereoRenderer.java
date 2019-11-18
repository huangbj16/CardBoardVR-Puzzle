package edu.cs4730.floatingcubes;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import com.google.vr.sdk.audio.*;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * This is implementation of the Render, using Cardboards StereoRenderer.
 * Code was used from the cardboardsample and combined with the opengl30Cube.
 * The cube and mycolor code is unchanged from the opengl30Cube example.
 *
 */
public class myStereoRenderer implements GvrView.StereoRenderer{
    private static String TAG = "StereoRenderer";
    private float mAngle = 0.4f;  //spin of the cube.

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100.0f;

    private static final float CAMERA_Z = 0.01f;
    private static final float COLLISON_OFFSET = 0.3f;

    public Cube mCube;
    public Polygon fakeMosquito;
    public Pyramid mPyramid;
    public Floor mFloor;

    private float objectDistance = 6f;
    private float floorDepth = 20f;
    final private float STEP = 0.05f;
    final private int puzzleSize = 7;
    final private int puzzleCubeLength = puzzleSize*puzzleSize*puzzleSize;
    final private float[] puzzleOffsets = new float[]{-3, -3, -3};

    private float[][] modelCube;
    private float[] camera;
    private float[] view;
    private float[] headView;
    private float[] headRotation;
    private float[] modelview;
    private float[] mMVPMatrix;
    private float[] forwardVector;
    private float[] userPosition;
    private float[] userShift;
    private boolean[] puzzleMap;
    private float[] mosquitoPosition;

    private float[] modelFloor;

    private PuzzleGenerator generator;


    // We keep the light always position just above the user.
    private static final float[] LIGHT_POS_IN_WORLD_SPACE = new float[] { 0.0f, 2.0f, 0.0f, 1.0f };

    private final float[] lightPosInEyeSpace = new float[4];

    private GvrAudioEngine gvrAudioEngine;
    private volatile int sourceId = GvrAudioEngine.INVALID_ID;
    private volatile int successSourceId = GvrAudioEngine.INVALID_ID;
    private Context myContext;
    private static final String OBJECT_SOUND_FILE = "audio/mosquito.mp3";
    private static final String FIND_SOUND_FILE = "audio/HelloVR_Activation.ogg";
    private MosquitoPlayer mosquitoPlayer;
    private float[] mosquitoModel;

    public myStereoRenderer(Context context){
        this.myContext = context;
    }
    ///
    // Create a shader object, load the shader source, and
    // compile the shader.
    //
    public static int LoadShader(int type, String shaderSrc) {
        int shader;
        int[] compiled = new int[1];

        // Create the shader object
        shader = GLES30.glCreateShader(type);

        if (shader == 0) {
            return 0;
        }

        // Load the shader source
        GLES30.glShaderSource(shader, shaderSrc);

        // Compile the shader
        GLES30.glCompileShader(shader);

        // Check the compile status
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0);

        if (compiled[0] == 0) {
            Log.e(TAG, "Erorr!!!!");
            Log.e(TAG, GLES30.glGetShaderInfoLog(shader));
            GLES30.glDeleteShader(shader);
            return 0;
        }

        return shader;
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES30.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES30.glGetError()) != GLES30.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    /**
     * Prepares OpenGL ES before we draw a frame.
     *
     * @param headTransform The head transformation in the new frame.
     */
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        headTransform.getForwardVector(forwardVector, 0);
        for (int i = 0; i < 3; i++) {
            userShift[i] = forwardVector[i] * STEP;
            userPosition[i] += userShift[i];
            if (isInPuzzle()) {
                if (isCrash(i)) {
                    userPosition[i] -= userShift[i];
                    userShift[i] = 0;
                }
            }
        }

        for (int i = 0; i < puzzleCubeLength; i++) {
            Matrix.translateM(modelCube[i], 0, -userShift[0], -userShift[1], -userShift[2]);
        }
        Matrix.translateM(modelFloor, 0, -userShift[0], -userShift[1], -userShift[2]);
        Matrix.translateM(mosquitoModel, 0, -userShift[0], -userShift[1], -userShift[2]);

//        //rotate the cube, mangle is how fast, x,y,z which directions it rotates.
//        Matrix.rotateM(CubeMatrix0, 0, mAngle, 0.7f, 0.7f, 1.0f);
//        //rotate cube2, mangle is how fast, x,y,z which directions it rotates.
//        Matrix.rotateM(CubeMatrix1, 0, -mAngle, 0.7f, 0.7f, 1.0f);
//        Matrix.rotateM(CubeMatrix2, 0, mAngle, 1.0f, 0.7f, 0.7f);
//        Matrix.rotateM(CubeMatrix3, 0, -mAngle, 1.0f, 0.7f, 0.7f);
//        Matrix.rotateM(CubeMatrix4, 0, mAngle, 0.7f, 1.0f, 0.7f);
//        Matrix.rotateM(CubeMatrix5, 0, -mAngle, 0.7f, 1.0f, 0.7f);
//        Matrix.rotateM(CubeMatrix6, 0, mAngle, 0.5f, 0.5f, 1.5f);
//        Matrix.rotateM(CubeMatrix7, 0, -mAngle, 0.5f, 0.5f, 1.5f);

        // Build the camera matrix and apply it to the ModelView.
        Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

        headTransform.getHeadView(headView, 0);
        headTransform.getQuaternion(headRotation, 0);

        mosquitoPlayer.updateAudio(userPosition, mosquitoPosition, forwardVector);

        if(isMetMosquito()){
            System.out.println("Metttttttttttttt!");
            successSourceId = gvrAudioEngine.createStereoSound(FIND_SOUND_FILE);
            gvrAudioEngine.playSound(successSourceId, false /* looping disabled */);
            resetPosition();
        }
//
//        gvrAudioEngine.setHeadPosition(userPosition[0], userPosition[1], userPosition[2]);
//        gvrAudioEngine.setHeadRotation(
//                headRotation[0], headRotation[1], headRotation[2], headRotation[3]);
//        // Regular update call to GVR audio engine.
//        gvrAudioEngine.update();
    }

    private boolean isMetMosquito(){
        float dist = 0;
        for (int i = 0; i < 3; i++) {
            dist += (userPosition[i]-mosquitoPosition[i]) * (userPosition[i]-mosquitoPosition[i]);
        }
        System.out.println("dist: "+dist);
        if(dist < 0.1f) {
            return true;
        }
        return false;
    }

    /**
     * Draws a frame for an eye.
     *
     * @param eye The eye to render. Includes all required transformations.
     */
    @Override
    public void onDrawEye(Eye eye) {
        // Clear the color buffer  set above by glClearColor.
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        //need this otherwise, it will over right stuff and the cube will look wrong!
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);

        // Apply the eye transformation to the camera.

        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);

        // Set the position of the light
        Matrix.multiplyMV(lightPosInEyeSpace, 0, view, 0, LIGHT_POS_IN_WORLD_SPACE, 0);

        // combine the model-view with the projection matrix
        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
        // combine the model with the view matrix to create the modelview matreix
        for (int i = 0; i < puzzleCubeLength; i++) {
            if(!puzzleMap[i]) continue;
            Matrix.multiplyMM(modelview, 0, view, 0, modelCube[i], 0);
            Matrix.multiplyMM(mMVPMatrix, 0, perspective, 0, modelview, 0);
            //finally draw the cube with the full Model-view-projection matrix.
            mCube.draw(mMVPMatrix);
        }

        Matrix.multiplyMM(modelview, 0, view, 0, mosquitoModel, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, perspective, 0, modelview, 0);
        fakeMosquito.draw(mMVPMatrix);

        //now calculate for the floor
        Matrix.multiplyMM(modelview, 0, view, 0, modelFloor, 0);
        // combine the model-view with the projection matrix
        Matrix.multiplyMM(mMVPMatrix, 0, perspective, 0, modelview, 0);
        mFloor.drawFloor(mMVPMatrix, modelFloor, modelview, lightPosInEyeSpace);
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
        //no clue, example code was blank here.
    }

    @Override
    public void onSurfaceChanged(int i, int i1) {
        Log.i(TAG, "onSurfaceChanged");  //should not happen, set landscape in the manifest file.
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        modelCube = new float[puzzleCubeLength][16];
        mosquitoModel = new float[16];
        camera = new float[16];
        view = new float[16];
        mMVPMatrix = new float[16];
        modelview = new float[16];
        headView = new float[16];
        headRotation = new float[4];
        modelFloor = new float[16];
        forwardVector = new float[3];
        userPosition = new float[]{0, 0, 0};
        userShift = new float[]{0, 0, 0};
        mosquitoPosition = new float[]{0, 0, -7};
        generator = new PuzzleGenerator();
        puzzleMap = generator.generatePuzzle(puzzleSize);

//        System.out.println(puzzleMap.length);
//        for (int i = 0; i < puzzleMap.length; i++) {
//            System.out.print(puzzleMap[i]+" ");
//        }
//        System.out.println();

        GLES30.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // Dark background so text shows up well.
        //initialize the cube code for drawing.
        mCube = new Cube();
        fakeMosquito = new Polygon();
        mPyramid = new Pyramid();


        // Object first appears directly in front of user.  In front
        for (int i = 0; i < puzzleSize; i++) {
            for (int j = 0; j < puzzleSize; j++) {
                for (int k = 0; k < puzzleSize; k++) {
                    int index = puzzleSize*puzzleSize*i+puzzleSize*j+k;
                    Matrix.setIdentityM(modelCube[index], 0);
                    Matrix.translateM(modelCube[index], 0, puzzleOffsets[0]+i, puzzleOffsets[1]+j, puzzleOffsets[2]+k);
                }
            }
        }

        Matrix.setIdentityM(mosquitoModel, 0);
        Matrix.translateM(mosquitoModel, 0, mosquitoPosition[0], mosquitoPosition[1], mosquitoPosition[2]);
        //floor object
        mFloor = new Floor();
        Matrix.setIdentityM(modelFloor, 0);
        Matrix.translateM(modelFloor, 0, 0, -floorDepth, 0); // Floor appears below user.

        mosquitoPlayer = new MosquitoPlayer(myContext);

        gvrAudioEngine = new GvrAudioEngine(myContext, GvrAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY);
        gvrAudioEngine.preloadSoundFile(FIND_SOUND_FILE);
//
//        new Thread(
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        // Start spatial audio playback of OBJECT_SOUND_FILE at the model position. The
//                        // returned sourceId handle is stored and allows for repositioning the sound object
//                        // whenever the target position changes.
//                        gvrAudioEngine.preloadSoundFile(OBJECT_SOUND_FILE);
//                        sourceId = gvrAudioEngine.createSoundObject(OBJECT_SOUND_FILE);
//                        gvrAudioEngine.setSoundObjectPosition(
//                                sourceId, mosquitoPosition[0], mosquitoPosition[1], mosquitoPosition[2]);
//                        gvrAudioEngine.playSound(sourceId, true /* looped playback */);
//                        gvrAudioEngine.preloadSoundFile(FIND_SOUND_FILE);
//                    }
//                })
//                .start();
    }

    @Override
    public void onRendererShutdown() {
        Log.i(TAG, "onRendererShutdown");
    }

    private boolean isInPuzzle(){
        for (int i = 0; i < 3; i++) {
            int cell1 = Math.round(userPosition[i]+COLLISON_OFFSET);
            int cell2 = Math.round(userPosition[i]-COLLISON_OFFSET);
            if( cell1 < puzzleOffsets[i] || cell2 > puzzleOffsets[i]+puzzleSize-1 )
                return false;
        }
        return true;
    }

    private boolean isCrash(int coord){
        int[] cellPosition = new int[3];
        for (int i = 0; i < 3; i++) {
            if(i == coord) {
                if (userShift[i] > 0)
                    cellPosition[i] = Math.round(userPosition[i] - puzzleOffsets[i]+COLLISON_OFFSET);
                else
                    cellPosition[i] = Math.round(userPosition[i] - puzzleOffsets[i]-COLLISON_OFFSET);
            }
            else{
                cellPosition[i] = Math.round(userPosition[i] - puzzleOffsets[i]);
            }
        }
        for (int i = 0; i < cellPosition.length; i++) {
            if(cellPosition[i] < 0 || cellPosition[i] >= puzzleSize)//out of range
                return false;
        }
        int cubeIndex = cellPosition[0]*puzzleSize*puzzleSize + cellPosition[1]*puzzleSize + cellPosition[2];
        if(puzzleMap[cubeIndex]) return true;
        else return false;
    }

    public void resetPosition(){
        System.out.println("resetttttttttttt");
        for (int i = 0; i < puzzleCubeLength; i++) {
            Matrix.translateM(modelCube[i], 0, userPosition[0], userPosition[1], userPosition[2]);
        }
        Matrix.translateM(modelFloor, 0, userPosition[0], userPosition[1], userPosition[2]);
        Matrix.translateM(mosquitoModel, 0, userPosition[0], userPosition[1], userPosition[2]);
        for (int i = 0; i < 3; i++) {
            userPosition[i] = 0;
        }
//        gvrAudioEngine.setHeadPosition(userPosition[0], userPosition[1], userPosition[2]);
//        gvrAudioEngine.setHeadRotation(
//                headRotation[0], headRotation[1], headRotation[2], headRotation[3]);
//        gvrAudioEngine.update();
    }

    public void printMatrix(float[] matrix, String name){//column priority
        System.out.println("printMatrix: "+name);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                System.out.printf("%.2f ", matrix[i+4*j]);
            }
            System.out.println();
            System.out.flush();
        }
        System.out.println();
    }

    public void printVector(float[] vector, String name){
        System.out.println("printVector: "+name);
        for (int i = 0; i < vector.length; i++) {
            System.out.printf("%.2f ", vector[i]);
        }
        System.out.println();
    }
}
