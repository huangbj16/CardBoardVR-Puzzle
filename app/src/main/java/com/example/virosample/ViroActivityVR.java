/*
 * Copyright (c) 2017-present, Viro, Inc.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.virosample;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.util.Log;

import com.viro.core.*;
//import com.viro.core.AmbientLight;
//import com.viro.core.AsyncObject3DListener;
//import com.viro.core.Box;
//import com.viro.core.Camera;
//import com.viro.core.ClickListener;
//import com.viro.core.ClickState;
//import com.viro.core.Geometry;
//import com.viro.core.Material;
//import com.viro.core.Node;
//
//import com.viro.core.Object3D;
//import com.viro.core.OmniLight;
//import com.viro.core.Scene;
//import com.viro.core.Sphere;
//import com.viro.core.Spotlight;
//import com.viro.core.Text;
//import com.viro.core.Texture;
//import com.viro.core.TouchState;
//import com.viro.core.TouchpadTouchListener;
//import com.viro.core.Vector;
//import com.viro.core.ViroView;
//import com.viro.core.ViroViewGVR;
//import com.viro.core.ViroViewOVR;
//import com.viro.renderer.BuildConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A sample Android activity for creating GVR and/or OVR stereoscopic scenes.
 * <p>
 * This activity automatically handles the creation of the VR renderer based on the currently
 * selected build variant in Android Studio.
 * <p>
 * Extend and override onRendererStart() to start building your 3D scenes.
 */
public class ViroActivityVR extends Activity {

    private static final String TAG = ViroActivityVR.class.getSimpleName();
    private ViroView mViroView;
    private AssetManager mAssetManager;
    private Scene scene;
    private Node cameraNode;

    final private float STEP = 0.05f;
    final private int puzzleSize = 5;
    final private int puzzleCubeLength = puzzleSize*puzzleSize*puzzleSize;
    final private float[] puzzleOffsets = new float[]{-2, -2, -10};
    final private Vector roomPositiveRange = new Vector(2.5, 2.5, 5);
    final private Vector roomNegativeRange = new Vector(-2.5, -2.5, -15);
    private List<Node> modelCube;
    private boolean isClick;
    private Vector userPosition, userShift;
    private static final float COLLISON_OFFSET = 0.3f;
    private PuzzleGenerator generator;
    private boolean[] puzzleMap;

    final private String[] cubePaths = new String[]{
            "file:///android_asset/cube2/PONI.obj",
            "file:///android_asset/cube5/rubiks-cube.obj",
            "file:///android_asset/cube9/model.obj",
            "file:///android_asset/cube10/tfx80.obj"
    };

    final private Vector[] cubeScales = new Vector[]{
            new Vector(0.4f,0.4f,0.4f),
            new Vector(6.535f, 6.535f, 6.535f),
            new Vector(1, 1, 1),
            new Vector(0.5f, 0.5f, 0.5f)
    };

    final private String[] pokemonPaths = new String[]{
            "file:///android_asset/Bulbasaur/model.obj",
            "file:///android_asset/oddish/model.obj",
            "file:///android_asset/pikachu/model.obj",
            "file:///android_asset/snorlax/model.obj",
            "file:///android_asset/charmander.obj",
    };

    final private Vector[] pokemonPositions = new Vector[]{
            new Vector(-2, -2, -3),
            new Vector(-1, -2, -3),
            new Vector(0, -2, -3),
            new Vector(1, -2, -3),
            new Vector(2, -2, -3),
    };

    private long beginTime = 0;
    private int count = 0;

//    "file:///android_asset/charmander.obj"
//    "file:///android_asset/Bulbasaur/model.obj",
//    "file:///android_asset/oddish/model.obj",
//    "file:///android_asset/pikachu/model.obj",
//    "file:///android_asset/squirtle/model.obj",
//    "file:///android_asset/cube1/Cube.obj",
//    "file:///android_asset/cube2/PONI.obj",
//    "file:///android_asset/cube3/blcube.obj",
//    "file:///android_asset/cube5/rubiks-cube.obj"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        initialize();
        isClick = false;
        userPosition = new Vector(0,0,0);
        generator = new PuzzleGenerator();
        puzzleMap = generator.generatePuzzle(puzzleSize);

        mViroView = createGVRView();

//        if (BuildConfig.VIRO_PLATFORM.equalsIgnoreCase("GVR")) {
//            mViroView = createGVRView();
//        } else if (BuildConfig.VIRO_PLATFORM.equalsIgnoreCase("OVR")) {
//            mViroView = createOVRView();
//        }
        setContentView(mViroView);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    if (isClick) {
                        if(count == 0){
                            beginTime = System.currentTimeMillis();
                            count++;
                        }
                        else{
                            System.out.println("time: "+((float)(System.currentTimeMillis()-beginTime)/count));
                            count++;
                        }
//                        System.out.println("forward!");
                        Vector forward = mViroView.getLastCameraForwardRealtime();

                        userShift = forward.scale(STEP);
                        userPosition.x += userShift.x;
                        if(isInPuzzle()) {
                            if(isCrash(0)) {
                                userPosition.x -= userShift.x;
                            }
                        }
                        else{
                            if(isOutOfRoom()){
                                userPosition.x -= userShift.x;
                            }
                        }
                        userPosition.y += userShift.y;
                        if(isInPuzzle() && isCrash(0)) {
                            userPosition.y -= userShift.y;
                        }
                        else{
                            if(isOutOfRoom()){
                                userPosition.y -= userShift.y;
                            }
                        }
                        userPosition.z += userShift.z;
                        if(isInPuzzle() && isCrash(0)) {
                            userPosition.z -= userShift.z;
                        }
                        else{
                            if(isOutOfRoom()){
                                userPosition.z -= userShift.z;
                            }
                        }
//                        for (int i = 0; i < 3; i++) {
//                            userShift[i] = forwardVector[i] * STEP;
//                            userPosition[i] += userShift[i];
//                            if (isInPuzzle()) {
//                                if (isCrash(i)) {
//                                    userPosition[i] -= userShift[i];
//                                    userShift[i] = 0;
//                                }
//                            }
//                        }

//                        position = position.add(forward.scale(STEP));
//                        System.out.println(position);
//                        Node cameraNode = new Node();
                        cameraNode.setPosition(userPosition);
//                        cameraNode.setRotation(new Vector(0, 0, 0));
//                        Camera camera = new Camera();
//                        cameraNode.setCamera(camera);
//                        scene.getRootNode().addChildNode(cameraNo、de);
                        mViroView.setPointOfView(cameraNode);
//                        updateScene();
                        try {
                            Thread.sleep(40);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        count = 0;
                        beginTime = 0;
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    private ViroView createGVRView() {
        ViroViewGVR viroView = new ViroViewGVR(this, new ViroViewGVR.StartupListener() {
            @Override
            public void onSuccess() {
                onRendererStart();
            }

            @Override
            public void onFailure(ViroViewGVR.StartupError error, String errorMessage) {
                onRendererFailed(error.toString(), errorMessage);
            }
        }, new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "On GVR userRequested exit");
            }
        });
        return viroView;
    }

    private ViroView createOVRView() {
        ViroViewOVR viroView = new ViroViewOVR(this, new ViroViewOVR.StartupListener() {
            @Override
            public void onSuccess() {
                onRendererStart();
            }

            @Override
            public void onFailure(ViroViewOVR.StartupError error, String errorMessage) {
                onRendererFailed(error.toString(), errorMessage);
            }
        });
        return viroView;
    }

    private void onRendererStart() {
        // Create your scene here. We provide a simple Hello World scene as an example
        createHelloWorldScene();
    }

    public void onRendererFailed(String error, String errorMessage) {
        // Fail as you wish!
    }

    public void initialize(){
//        modelCube = new ArrayList<>();
//        for (int i = 0; i < puzzleSize; i++) {
//            for (int j = 0; j < puzzleSize; j++) {
//                for (int k = 0; k < puzzleSize; k++) {
//                    int index = puzzleSize*puzzleSize*i+puzzleSize*j+k;
//                    modelCube.add(new Box(1,1,1));
//                            //new Box(puzzleOffsets[0]+(float)i, puzzleOffsets[1]+(float)j, puzzleOffsets[2]+(float)k);
//                }
//            }
//        }
    }

    private void createHelloWorldScene() {
        mViroView.setBloomEnabled(false);
        mViroView.setHDREnabled(false);
        mViroView.setPBREnabled(false);
        mViroView.setShadowsEnabled(false);
        // Create a new Scene and get its root Node
        scene = new Scene();
        Node rootNode = scene.getRootNode();

        // Load the background image into a Bitmap file from assets
//        Bitmap backgroundBitmap = bitmapFromAsset("guadalupe_360.jpg");

        // Add a 360 Background Texture if we were able to find the Bitmap
//        if (backgroundBitmap != null) {
//            Texture backgroundTexture = new Texture(backgroundBitmap, Texture.Format.RGBA8, true, true);
//            scene.setBackgroundTexture(backgroundTexture);
//        }

//        Geometry box = new Box(0.8f,0.8f,0.8f);
//        Material material = new Material();
//        material.setDiffuseColor(Color.BLUE);
//        Material material2 = new Material();
//        material2.setDiffuseColor(Color.YELLOW);
//        ArrayList<Material> list = new ArrayList<>();
//        list.add(material);
//        ArrayList<Material> list2 = new ArrayList<>();
//        list2.add(material2);
//        box.setMaterials(list);
//        cubeNode.setGeometry(box);
//        rootNode.addChildNode(cubeNode);
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(Color.WHITE);
        ambient.setIntensity(500);

        Spotlight spotlight = new Spotlight();
        spotlight.setPosition(new Vector(0,0,1));
        spotlight.setDirection(new Vector(0, 0, -1));
        spotlight.setAttenuationStartDistance(10);
        spotlight.setAttenuationEndDistance(20);
        spotlight.setInnerAngle(20);
        spotlight.setOuterAngle(40);
        spotlight.setColor(Color.RED);
        spotlight.setIntensity(800);
        // Shadow casting parameters
        spotlight.setCastsShadow(true);
        spotlight.setShadowMapSize(4096);
        spotlight.setShadowNearZ(1);
        spotlight.setShadowFarZ(10);

        OmniLight omniLight = new OmniLight();
        omniLight.setPosition(new Vector(0,0,-12));
        omniLight.setAttenuationStartDistance(10);
        omniLight.setAttenuationEndDistance(20);
        omniLight.setColor(Color.BLUE);
        omniLight.setIntensity(800);

        Node lightNode = new Node();
        lightNode.addLight(ambient);
        lightNode.addLight(spotlight);
        lightNode.addLight(omniLight);
        rootNode.addChildNode(lightNode);

        modelCube = new ArrayList<>();

        //load room
        Object3D room = new Object3D();
        room.loadModel(mViroView.getViroContext(), Uri.parse("file:///android_asset/Hallway/model.obj"), Object3D.Type.OBJ, new AsyncObject3DListener() {
            @Override
            public void onObject3DLoaded(Object3D object3D, Object3D.Type type) {
                Log.w(TAG, "successfully loaded the model!");
            }

            @Override
            public void onObject3DFailed(String s) {
                Log.w(TAG, "failed to load the model!");
            }
        });
        room.setScale(new Vector(6, 6, 6));
        room.setRotation(new Vector(0, 1.57, 0));
        room.setPosition(new Vector(0,0,-5));
//        room.addLight(new AmbientLight(Color.WHITE, 1000.0f));
        rootNode.addChildNode(room);
        System.out.println("room position: "+room.getPositionRealtime());
        modelCube.add(room);

        //load pokemons
        for (int i = 0; i < 5; i++) {
            Object3D pokemon = new Object3D();
            pokemon.loadModel(mViroView.getViroContext(), Uri.parse(pokemonPaths[i]), Object3D.Type.OBJ, new AsyncObject3DListener() {
                @Override
                public void onObject3DLoaded(Object3D object3D, Object3D.Type type) {
                    Log.w(TAG, "successfully loaded the model!");
                }

                @Override
                public void onObject3DFailed(String s) {
                    Log.w(TAG, "failed to load the model!");
                }
            });
//            room.setScale(new Vector(6, 6, 6));
            pokemon.setRotation(new Vector(0, 3.14, 0));
            pokemon.setPosition(pokemonPositions[i]);
//        room.addLight(new AmbientLight(Color.WHITE, 1000.0f));
            rootNode.addChildNode(pokemon);
            modelCube.add(pokemon);
        }

        Random random = new Random(19980608);
        for (int i = 0; i < puzzleSize; i++) {
            for (int j = 0; j < puzzleSize; j++) {
                for (int k = 0; k < puzzleSize; k++) {
                    int index = puzzleSize*puzzleSize*i+puzzleSize*j+k;
                    if(!puzzleMap[index]) continue;
                    int cubeIndex = random.nextInt(4);
                    Object3D cube = new Object3D();
                    cube.loadModel(mViroView.getViroContext(), Uri.parse(cubePaths[cubeIndex]), Object3D.Type.OBJ, new AsyncObject3DListener() {
                        @Override
                        public void onObject3DLoaded(Object3D object3D, Object3D.Type type) {
                            Log.w(TAG, "successfully loaded the model!");
                        }

                        @Override
                        public void onObject3DFailed(String s) {
                            Log.w(TAG, "failed to load the model!");
                        }
                    });
                    cube.setPosition(new Vector(puzzleOffsets[0]+(float)i, puzzleOffsets[1]+(float)j, puzzleOffsets[2]+(float)k));
                    cube.setScale(cubeScales[cubeIndex]);
//                    cube.setRotation(new Vector(0, 0, 0));
//                    cube.addLight(new AmbientLight(Color.WHITE, 1.0f));
                    rootNode.addChildNode(cube);
                    modelCube.add(cube);
                }
            }
        }

        cameraNode = new Node();
        cameraNode.setPosition(userPosition);
        cameraNode.setRotation(new Vector(0, 0, 0));
        Camera camera = new Camera();
        cameraNode.setCamera(camera);
        rootNode.addChildNode(cameraNode);
        mViroView.setPointOfView(cameraNode);

//        Object3D bird = new Objec3D();
//        bird.loadModel(Uri.parse("bird.vrx"), Type.FBX, new AsyncObject3DListener() {
//            public void onObject3DFailed(String error) {
//                Log.w(TAG, "Failed to load the model");
//            }
//            public void onObject3DLoaded(Object3D object, Object3D.Type type)
//                // Start chirping after the bird is loaded
//                chirp.play();
//            }
//        ));

//        Node mosquitoNode = new Node();
////        node.addChildNode(bird);
//        mosquitoNode.setPosition(new Vector(0, 0, -2));
//        mosquitoNode.addSound(chirp);
//        rootNode.addChildNode(mosquitoNode);

        // Create a Text geometry
        Text helloWorldText = new Text.TextBuilder().viroContext(mViroView.getViroContext()).
                textString("Hello World").
                fontFamilyName("Roboto").fontSize(50).
                color(Color.WHITE).
                width(4).height(2).
                horizontalAlignment(Text.HorizontalAlignment.CENTER).
                verticalAlignment(Text.VerticalAlignment.CENTER).
                lineBreakMode(Text.LineBreakMode.NONE).
                clipMode(Text.ClipMode.CLIP_TO_BOUNDS).
                maxLines(1).build();

//        // Create a Node, position it, and attach the Text geometry to it
        String mosquitoFilePath = "file:///android_asset/chirp.mp3";
        Sound chirp = new Sound(mViroView.getViroContext(), Uri.parse(mosquitoFilePath), null);
        chirp.setLoop(true);
        chirp.setVolume(1.0f);
        chirp.play();

        Node textNode = new Node();
        textNode.setPosition(new Vector(0, 0, -2));
//        textNode.addSound(chirp);
        textNode.setGeometry(helloWorldText);
        rootNode.addChildNode(textNode);

        // Animate the bird to the right; this will animate the chirp sound as well
//        AnimationTransaction.begin();
//        AnimationTransaction.setAnimationDuration(10000);
//        textNode.setPosition(new Vector(10, 0, 0));
//        AnimationTransaction.commit();
////
////        // Attach the textNode to the Scene's rootNode.
//        rootNode.addChildNode(textNode);
        rootNode.setClickListener(new ClickListener() {
            @Override
            public void onClick(int i, Node node, Vector vector) {
                System.out.println("click!");
            }

            @Override
            public void onClickState(int i, Node node, ClickState clickState, Vector vector) {
                System.out.println("clickstate: "+clickState);
                if(clickState == ClickState.CLICK_DOWN){
                    isClick = true;
                }
                else if(clickState == ClickState.CLICK_UP){
                    isClick = false;
                }
            }
        });

        mViroView.setScene(scene);
    }

    private boolean isOutOfRoom(){
        if(userPosition.x < roomNegativeRange.x || userPosition.x > roomPositiveRange.x) return true;
        if(userPosition.y < roomNegativeRange.y || userPosition.y > roomPositiveRange.y) return true;
        if(userPosition.z < roomNegativeRange.z || userPosition.z > roomPositiveRange.z) return true;
        return false;
    }

    private boolean isInPuzzle(){
        float[] positionArray = userPosition.toArray();
        for (int i = 0; i < 3; i++) {
            int cell1 = Math.round(positionArray[i]+COLLISON_OFFSET);
            int cell2 = Math.round(positionArray[i]-COLLISON_OFFSET);
            if( cell1 < puzzleOffsets[i] || cell2 > puzzleOffsets[i]+puzzleSize-1 )
                return false;
        }
        return true;
    }

    private boolean isCrash(int coord){
        float[] shiftArray = userShift.toArray();
        float[] positionArray = userPosition.toArray();
        int[] cellPosition = new int[3];
        for (int i = 0; i < 3; i++) {
            if(i == coord) {
                if (shiftArray[i] > 0)
                    cellPosition[i] = Math.round(positionArray[i] - puzzleOffsets[i]+COLLISON_OFFSET);
                else
                    cellPosition[i] = Math.round(positionArray[i] - puzzleOffsets[i]-COLLISON_OFFSET);
            }
            else{
                cellPosition[i] = Math.round(positionArray[i] - puzzleOffsets[i]);
            }
        }
        for (int i = 0; i < cellPosition.length; i++) {
            if(cellPosition[i] < 0 || cellPosition[i] >= puzzleSize)//out of range
                return false;
        }
        int cubeIndex = cellPosition[0]*puzzleSize*puzzleSize + cellPosition[1]*puzzleSize + cellPosition[2];
        if(puzzleMap[cubeIndex])
            return true;
        else return false;
    }

    private void updateScene(){
//        Node rootNode = scene.getRootNode();
//        List<Node> childNodes = rootNode.getChildNodes();
////        System.out.println(childNodes.size());
//        for (int i = 0; i < puzzleSize; i++) {
//            for (int j = 0; j < puzzleSize; j++) {
//                for (int k = 0; k < puzzleSize; k++) {
//                    int index = puzzleSize*puzzleSize*i+puzzleSize*j+k;
//                    Node cube = childNodes.get(index);
//                    cube.setPosition(new Vector(puzzleOffsets[0]+(float)i-position.x, puzzleOffsets[1]+(float)j-position.y, puzzleOffsets[2]+(float)k- position.z));
//                }
//            }
//        }
//        Node charmander = childNodes.get(125);
//        charmander.setPosition(new Vector(-position.x, -position.y, -2-position.z));
//        mViroView.setScene(scene);
    }

    private Bitmap bitmapFromAsset(String assetName) {
        if (mAssetManager == null) {
            mAssetManager = getResources().getAssets();
        }

        InputStream imageStream;
        try {
            imageStream = mAssetManager.open(assetName);
        } catch (IOException exception) {
            Log.w(TAG, "Unable to find image ["+assetName+"] in assets!");
            return null;
        }
        return BitmapFactory.decodeStream(imageStream);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mViroView.onActivityStarted(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViroView.onActivityResumed(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mViroView.onActivityPaused(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mViroView.onActivityStopped(this);
    }
}