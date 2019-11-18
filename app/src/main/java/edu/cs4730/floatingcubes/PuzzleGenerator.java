package edu.cs4730.floatingcubes;

public class PuzzleGenerator {
    int[][] pathCoord;
    int pathLength;

    public boolean[] generatePuzzle(int puzzleSize){
        boolean[] puzzleMap = new boolean[puzzleSize*puzzleSize*puzzleSize];

        for (int i = 0; i < puzzleMap.length; i++) {
            puzzleMap[i] = true;
        }

        for (int i = 0; i < pathLength; i++) {
            puzzleMap[pathCoord[i][0]*puzzleSize*puzzleSize + pathCoord[i][1]*puzzleSize + pathCoord[i][2]] = false;
        }

        return puzzleMap;
    }

    PuzzleGenerator(){
        pathLength = 19;
        pathCoord = new int[pathLength][3];
        pathCoord[0] = new int[]{3, 3, 3};
        pathCoord[1] = new int[]{3, 3, 4};
        pathCoord[2] = new int[]{3, 3, 5};
        pathCoord[3] = new int[]{3, 2, 5};
        pathCoord[4] = new int[]{3, 1, 5};
        pathCoord[5] = new int[]{3, 1, 4};
        pathCoord[6] = new int[]{3, 3, 2};
        pathCoord[7] = new int[]{3, 3, 1};
        pathCoord[8] = new int[]{3, 4, 1};
        pathCoord[9] = new int[]{4, 4, 1};
        pathCoord[10] = new int[]{4, 4, 0};
        pathCoord[11] = new int[]{2, 3, 3};
        pathCoord[12] = new int[]{1, 3, 3};
        pathCoord[13] = new int[]{1, 4, 3};
        pathCoord[14] = new int[]{1, 5, 3};
        pathCoord[15] = new int[]{4, 3, 3};
        pathCoord[16] = new int[]{5, 3, 3};
        pathCoord[17] = new int[]{5, 3, 4};
        pathCoord[18] = new int[]{5, 3, 5};
    }
}
