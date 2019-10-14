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
        pathLength = 8;
        pathCoord = new int[pathLength][3];
        pathCoord[0] = new int[]{1, 1, 4};
        pathCoord[1] = new int[]{1, 1, 3};
        pathCoord[2] = new int[]{2, 1, 3};
        pathCoord[3] = new int[]{3, 1, 3};
        pathCoord[4] = new int[]{3, 1, 2};
        pathCoord[5] = new int[]{3, 2, 2};
        pathCoord[6] = new int[]{3, 2, 1};
        pathCoord[7] = new int[]{3, 2, 0};
    }
}
