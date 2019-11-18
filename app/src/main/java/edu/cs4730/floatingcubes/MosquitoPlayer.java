package edu.cs4730.floatingcubes;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.opengl.Matrix;

public class MosquitoPlayer{
    Context myContext;
    MediaPlayer mediaPlayer;
    int elevation = 2;
    int azimut = 12;
    boolean isCompleted;
    int maxVolume;
    AudioManager mAudioManager;

    int[] audios = new int[]{
            R.raw.mosquito_1_1, R.raw.mosquito_1_2, R.raw.mosquito_1_3, R.raw.mosquito_1_4, R.raw.mosquito_1_5, R.raw.mosquito_1_6, R.raw.mosquito_1_7, R.raw.mosquito_1_8, R.raw.mosquito_1_9, R.raw.mosquito_1_10,
            R.raw.mosquito_1_11, R.raw.mosquito_1_12, R.raw.mosquito_1_13, R.raw.mosquito_1_14, R.raw.mosquito_1_15, R.raw.mosquito_1_16, R.raw.mosquito_1_17, R.raw.mosquito_1_18, R.raw.mosquito_1_19, R.raw.mosquito_1_20,
            R.raw.mosquito_1_21, R.raw.mosquito_1_22, R.raw.mosquito_1_23, R.raw.mosquito_1_24, R.raw.mosquito_1_25, R.raw.mosquito_1_26, R.raw.mosquito_1_27, R.raw.mosquito_1_28, R.raw.mosquito_1_29, R.raw.mosquito_1_30,
            R.raw.mosquito_1_31, R.raw.mosquito_1_32, R.raw.mosquito_1_33, R.raw.mosquito_1_34, R.raw.mosquito_1_35, R.raw.mosquito_1_36, R.raw.mosquito_1_37, R.raw.mosquito_1_38, R.raw.mosquito_1_39, R.raw.mosquito_1_40,
            R.raw.mosquito_1_41, R.raw.mosquito_1_42, R.raw.mosquito_1_43, R.raw.mosquito_1_44, R.raw.mosquito_1_45, R.raw.mosquito_1_46, R.raw.mosquito_1_47, R.raw.mosquito_1_48, R.raw.mosquito_1_49, R.raw.mosquito_1_50,
            R.raw.mosquito_2_1, R.raw.mosquito_2_2, R.raw.mosquito_2_3, R.raw.mosquito_2_4, R.raw.mosquito_2_5, R.raw.mosquito_2_6, R.raw.mosquito_2_7, R.raw.mosquito_2_8, R.raw.mosquito_2_9, R.raw.mosquito_2_10,
            R.raw.mosquito_2_11, R.raw.mosquito_2_12, R.raw.mosquito_2_13, R.raw.mosquito_2_14, R.raw.mosquito_2_15, R.raw.mosquito_2_16, R.raw.mosquito_2_17, R.raw.mosquito_2_18, R.raw.mosquito_2_19, R.raw.mosquito_2_20,
            R.raw.mosquito_2_21, R.raw.mosquito_2_22, R.raw.mosquito_2_23, R.raw.mosquito_2_24, R.raw.mosquito_2_25, R.raw.mosquito_2_26, R.raw.mosquito_2_27, R.raw.mosquito_2_28, R.raw.mosquito_2_29, R.raw.mosquito_2_30,
            R.raw.mosquito_2_31, R.raw.mosquito_2_32, R.raw.mosquito_2_33, R.raw.mosquito_2_34, R.raw.mosquito_2_35, R.raw.mosquito_2_36, R.raw.mosquito_2_37, R.raw.mosquito_2_38, R.raw.mosquito_2_39, R.raw.mosquito_2_40,
            R.raw.mosquito_2_41, R.raw.mosquito_2_42, R.raw.mosquito_2_43, R.raw.mosquito_2_44, R.raw.mosquito_2_45, R.raw.mosquito_2_46, R.raw.mosquito_2_47, R.raw.mosquito_2_48, R.raw.mosquito_2_49, R.raw.mosquito_2_50,
            R.raw.mosquito_3_1, R.raw.mosquito_3_2, R.raw.mosquito_3_3, R.raw.mosquito_3_4, R.raw.mosquito_3_5, R.raw.mosquito_3_6, R.raw.mosquito_3_7, R.raw.mosquito_3_8, R.raw.mosquito_3_9, R.raw.mosquito_3_10,
            R.raw.mosquito_3_11, R.raw.mosquito_3_12, R.raw.mosquito_3_13, R.raw.mosquito_3_14, R.raw.mosquito_3_15, R.raw.mosquito_3_16, R.raw.mosquito_3_17, R.raw.mosquito_3_18, R.raw.mosquito_3_19, R.raw.mosquito_3_20,
            R.raw.mosquito_3_21, R.raw.mosquito_3_22, R.raw.mosquito_3_23, R.raw.mosquito_3_24, R.raw.mosquito_3_25, R.raw.mosquito_3_26, R.raw.mosquito_3_27, R.raw.mosquito_3_28, R.raw.mosquito_3_29, R.raw.mosquito_3_30,
            R.raw.mosquito_3_31, R.raw.mosquito_3_32, R.raw.mosquito_3_33, R.raw.mosquito_3_34, R.raw.mosquito_3_35, R.raw.mosquito_3_36, R.raw.mosquito_3_37, R.raw.mosquito_3_38, R.raw.mosquito_3_39, R.raw.mosquito_3_40,
            R.raw.mosquito_3_41, R.raw.mosquito_3_42, R.raw.mosquito_3_43, R.raw.mosquito_3_44, R.raw.mosquito_3_45, R.raw.mosquito_3_46, R.raw.mosquito_3_47, R.raw.mosquito_3_48, R.raw.mosquito_3_49, R.raw.mosquito_3_50
    };

    MosquitoPlayer(Context context){
        myContext = context;
        isCompleted = true;
        mAudioManager = (AudioManager) myContext.getSystemService(Context.AUDIO_SERVICE);
        maxVolume = 10;
    }

    public void updateAudio(float[] userPosition, float[] mosquitoPosition, float[] forwardVector){
        int currVolume = calculateVolume(userPosition, mosquitoPosition);
        float[] mosquitoVector = minusAndNormalize(mosquitoPosition, userPosition);
        //Householder Transformation
//        float[] transformMatrix = getTransformMatrix(forwardVector);
//        printVector(forwardVector, "forwardVector");
//        printVector(mosquitoVector, "mosquitoVector");
//        printMatrix(transformMatrix, "transformMatrix");
//        float[] convertedVector = new float[]{0, 0, 0};
//        for (int i = 0; i < 3; i++) {
//            for (int j = 0; j < 3; j++) {
//                convertedVector[i] += transformMatrix[i*3+j] * mosquitoVector[j];
//            }
//        }
//        printVector(convertedVector, "convertedVector");
//        float[] convertedVector = minusAndNormalize(mosquitoVector, forwardVector);
//        printVector(forwardVector, "forwardVector");
//        printVector(mosquitoVector, "mosquitoVector");
//        printVector(convertedVector, "convertedVector");
//        System.out.println(convertedVector[1]+" "+Math.acos(convertedVector[1]));
        float zcosForward = (float)(Math.PI - Math.acos(forwardVector[1]));
        float zcosMosquito = (float)( Math.PI - Math.acos(mosquitoVector[1]));
        int tempElev = (int)Math.round((zcosMosquito-zcosForward)*1.5/Math.PI+2);
//        System.out.println(zcosForward+" "+zcosMosquito+" "+tempElev);
        float forwardAngle = getAngle(forwardVector[2], forwardVector[0]);
        float mosquitoAngle = getAngle(mosquitoVector[2], mosquitoPosition[0]);
        float gap;
        if(forwardAngle < mosquitoAngle)
            gap = mosquitoAngle - forwardAngle;
        else
            gap = mosquitoAngle - forwardAngle + 360;
        if(gap < 270)
            gap += 90;
        else
            gap -= 270;
        int tempAzimut = (int)(gap*50/360);
//        System.out.println(forwardAngle+" "+mosquitoAngle+" "+gap+" "+tempAzimut);
//        float x = (float)Math.atan2(convertedVector[0], convertedVector[2]);
////        System.out.println("x: "+x);
//        float mul = 24 / (float)Math.PI;
//        if(x>=0 && x<Math.PI/2)
//            tempAzimut = (int) (37 - x * mul);
//        else if(x>=Math.PI/2 && x<=Math.PI)
//            tempAzimut = (int) (37 - x * mul);
//        else if(x>=-Math.PI/2 && x<0)
//            tempAzimut = (int) (37 - x * mul);
//        else if(x>=-Math.PI && x<-Math.PI/2)
//            tempAzimut = (int) (-11 - x * mul);
//        else{
//            System.out.println("atan2 error");
//            return;
//        }
//        System.out.println("newElev: "+tempElev);
//        System.out.println("newAzimut: "+tempAzimut);
//        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//        System.out.println(currVolume);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currVolume, 0);
        if(tempElev != elevation || tempAzimut != azimut){
//            System.out.println("newElev: "+tempElev);
//            System.out.println("newAzimut: "+tempAzimut);
            elevation = tempElev;
            azimut = tempAzimut;
//            if(isCompleted){
//                System.out.println("completed");
//                loadAudio();
//            }
        }
        if(isCompleted){
            System.out.println("completed");
            loadAudio();
        }
    }

    private int calculateVolume(float[] x, float[] y){
        double dist = Math.sqrt((x[0]-y[0])*(x[0]-y[0])+(x[1]-y[1])*(x[1]-y[1])+(x[2]-y[2])*(x[2]-y[2]));
        int volume = maxVolume - (int)(Math.atan(dist) * maxVolume / (Math.PI/2));
        return volume;
    }

    private float getAngle(float z, float x){
        float v = (float)Math.atan2(x, z);
        v = (float)(v*180/Math.PI);
        if(v >= -180 && v < -90)
            v = -90 - v;
        else if(v >= -90 && v < 0)
            v = 270 - v;
        else if(v >= 0 && v < 90)
            v = 270 - v;
        else
            v = 270 - v;
        return v;
    }

    private void loadAudio(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                isCompleted = false;
                if(mediaPlayer != null)
                    mediaPlayer.release();
                mediaPlayer = MediaPlayer.create(myContext, audios[(elevation-1) * 50 + azimut-1]);
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        isCompleted = true;
                    }
                });
            }
        }).start();
    }

    private float[] minusAndNormalize(float[] x, float[] y){
        float[] ans = new float[3];
        double sum = 0;
        for (int i = 0; i < 3; i++) {
            ans[i] = x[i]-y[i];
            sum += ans[i]*ans[i];
        }
        sum = Math.sqrt(sum);
        for (int i = 0; i < 3; i++) {
            ans[i] /= sum;
        }
        return ans;
    }
    
    private float[] getTransformMatrix(float[] forwardVector){
        //givens rotation matrix
        float[] t1 = new float[9];
        float sqrlen = (float)Math.sqrt(forwardVector[0]*forwardVector[0]+forwardVector[1]*forwardVector[1]);
        float cos = forwardVector[0]/sqrlen;
        float sin = forwardVector[1]/sqrlen;
        t1[0] = cos;
        t1[1] = sin;
        t1[3] = -sin;
        t1[4] = cos;
        t1[8] = 1;
        printMatrix(t1, "t1");
        float sqrlen2 = (float)Math.sqrt(forwardVector[0]*forwardVector[0]+forwardVector[1]*forwardVector[1]+forwardVector[2]*forwardVector[2]);
        float cos2 = sqrlen/sqrlen2;
        float sin2 = forwardVector[2]/sqrlen2;
        float[] t2 = new float[9];
        t2[0] = cos2;
        t2[2] = sin2;
        t2[4] = 1;
        t2[6] = -sin2;
        t2[8] = cos2;
        printMatrix(t2, "t2");
        return multiplyMM(t2, t1);
        //householder is reflective, need givens
//        float sigma = forwardVector[0] >= 0 ? 1 : -1;
//        float[] v = forwardVector.clone();
//        v[0] += sigma;
//        float[] transformMatrix = new float[9];
//        float sqrlen = v[0]*v[0] + v[1]*v[1] + v[2]*v[2];
//        for (int i = 0; i < 3; i++) {
//            for (int j = 0; j < 3; j++) {
//                transformMatrix[i*3+j] = v[i] * v[j] * 2 / sqrlen;
//            }
//            transformMatrix[i*3+i] -= 1;
//        }
//        if(sigma == -1){
//            for (int i = 0; i < 9; i++) {
//                transformMatrix[i] = -transformMatrix[i];
//            }
//        }
//        return transformMatrix;
    }

    private float[] multiplyMM(float[] x, float[] y){
        float[] res = new float[9];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                res[i*3+j] = 0;
                for (int k = 0; k < 3; k++) {
                    res[i*3+j] += x[i*3+k] * y[k*3+j];
                }
            }
        }
        return res;
    }

    public void printMatrix(float[] matrix, String name){//column priority
        System.out.println("printMatrix: "+name);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                System.out.printf("%.2f ", matrix[i*3+j]);
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
