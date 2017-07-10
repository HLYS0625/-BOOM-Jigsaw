package com.cugb.xiaob.mozaiku;

import android.util.Log;

/**
 * Created by xiaob on 2017/7/9.
 */

public class IDAStarAlgorithm {

    //分别代表左、上、右、下四个移动方向的操作数  
    private int[] up = {-1,0};
    private int[] down = {1,0};
    private int[] left = {0,-1};
    private int[] right = {0,1};

    /**注意，这里UP和DOWN，LEFT和RIGHT必须是两两相对的，因为后面代码中使用 
     * ((dPrev != dCurr) && (dPrev%2 == dCurr%2)) 
     * 来判断前后两个移动方向是否相反 
     */
    private final int UP = 0;
    private final int DOWN = 2;
    private final int LEFT = 1;
    private final int RIGHT = 3;

    private int SIZE;

    //各个目标点坐标  
    private int[][] targetPoints;

    //用于记录移动步骤，存储0,1,2,3,对应上，下，左，右  
    private static int[] moves = new int[100000];

    private static long ans = 0; //当前迭代的"设想代价"

    //目标状态  
    private static int[][] tState;
    private static int[][] sState ;
    private static int blank_row,blank_column;

    public IDAStarAlgorithm(int[][] state,int width) {
        SIZE = state.length;
        targetPoints = new int[SIZE * SIZE][2];
        if(width==4){
            tState = new int[][]{
                {1 ,2 ,3 ,4 } ,
                {5 ,6 ,7 ,8 } ,
                {9 ,10,11,12} ,
                {13,14,15,0 }
            };
        }else if(width==3){
            tState = new int[][]{
                    {1, 2, 3},
                    {4, 5, 6},
                    {7, 8, 0}
            };
        }else if(width==5){
            tState = new int[][]{
                    {1,2,3,4,5},
                    {6,7,8,9,10},
                    {11,12,13,14,15},
                    {16,17,18,19,20},
                    {21,22,23,24,0}
            };
        }

        this.sState = state;
        //得到空格坐标  
        for(int i=0;i<state.length;i++) {
            for(int j=0;j<state[i].length;j++) {
                if(state[i][j] == 0) {
                    blank_row = i;
                    blank_column = j;
                    break;
                }
            }
        }

        //得到目标点坐标数组  
        for(int i=0;i<state.length;i++) {
            for(int j=0;j<state.length;j++) {
                targetPoints[tState[i][j]][0] = i; //行信息  

                targetPoints[tState[i][j]][1] = j; //列信息  
            }
        }
    }


    public static void main() {

        IDAStarAlgorithm idaAlgorithm = new IDAStarAlgorithm(sState,sState[0].length);
        Log.i("A*:", "--问题可解，开始求解--");
        //以曼哈顿距离为初始最小代价数
        int j = idaAlgorithm.getHeuristic(sState);
        Log.i("A*:", "初始manhattan距离:" + j);
        int i = -1;//置空默认移动方向

        long time = System.currentTimeMillis();
        //迭代加深"最小代价数"
        for (ans = j; ; ans++) {
            if (idaAlgorithm.solve(sState
                    , blank_row, blank_column, 0, i, j)) {
                break;
            }
        }
        Log.i("A*:", "求解用时:" + (System.currentTimeMillis() - time));

        idaAlgorithm.printMatrix(sState);
        int[][] matrix = idaAlgorithm.move(sState, moves[0]);
        for (int k = 1; k < ans; k++) {
            matrix = idaAlgorithm.move(matrix, moves[k]);
        }

    }

    public int[][] move(int[][]state,int direction) {
        int row = 0;
        int column = 0;
        for(int i=0;i<state.length;i++) {
            for(int j=0;j<state.length;j++) {
                if(state[i][j] == 0) {
                    row = i;
                    column = j;
                }
            }
        }
        switch(direction) {
            case UP:
                state[row][column] = state[row-1][column];
                state[row-1][column] = 0;
                break;
            case DOWN:
                state[row][column] = state[row+1][column];
                state[row+1][column] = 0;
                break;
            case LEFT:
                state[row][column] = state[row][column-1];
                state[row][column-1] = 0;
                break;
            case RIGHT:
                state[row][column] = state[row][column+1];
                state[row][column+1] = 0;
                break;
        }
        printMatrix(state);
        return state;
    }

    public void printMatrix(int[][] matrix) {
        Log.i("A*:","------------");
        for(int i=0;i<matrix.length;i++) {
            for(int j=0;j<matrix.length;j++) {
                System.out.print(matrix[i][j] + " ");
            }
            Log.i("A*:","----------------");
        }
    }

    /**
     * 求解方法 
     * @param state 当前状态 
     * @param blank_row 空位的行坐标 
     * @param blank_column 空格的列坐标 
     * @param dep 当前深度 
     * @param d 上一次移动的方向 
     * @param h 当前状态估价函数 
     * @return
     */
    public boolean solve(int[][] state,int blank_row,int blank_column,
                         int dep,long d,long h) {
        long h1;
        //和目标矩阵比较，看是否相同，如果相同则表示问题已解  
        boolean isSolved = true;
        for(int i=0;i<SIZE;i++) {
            for(int j=0;j<SIZE;j++) {
                if(state[i][j] != tState[i][j]) {
                    isSolved = false;
                }
            }
        }
        if(isSolved) {
            return true;
        }

        if(dep == ans) {
            return false;
        }

        //用于表示"空格"移动后的坐标位置  
        int blank_row1 = blank_row;
        int blank_column1  = blank_column;
        int[][] state2 = new int[SIZE][SIZE];

        for(int direction=0;direction<4;direction++) {
            for(int i=0;i<state.length;i++) {
                for(int j=0;j<state.length;j++) {
                    state2[i][j] = state[i][j];
                }
            }

            //本地移动方向和上次移动方向刚好相反，跳过这种情况的讨论  
            if(direction != d && (d%2 == direction%2)) {
                continue;
            }

            if(direction == UP) {
                blank_row1 = blank_row + up[0];
                blank_column1 = blank_column + up[1];
            } else if(direction == DOWN) {
                blank_row1 = blank_row + down[0];
                blank_column1 = blank_column + down[1];
            } else if(direction == LEFT) {
                blank_row1 = blank_row + left[0];
                blank_column1 = blank_column + left[1];
            } else {
                blank_row1 = blank_row + right[0];
                blank_column1 = blank_column + right[1];
            }

            //边界检查  
            if(blank_column1 < 0 || blank_column1 == SIZE
                    || blank_row1 < 0 || blank_row1 == SIZE) {
                continue ;
            }

            //交换空格位置和当前移动位置对应的单元格     
            state2[blank_row][blank_column] = state2[blank_row1][blank_column1];
            state2[blank_row1][blank_column1] = 0;

            //查看当前空格是否正在靠近目标点  
            if(direction == DOWN && blank_row1
                    > targetPoints[state[blank_row1][blank_column1]][0]) {
                h1 = h - 1;
            } else if(direction == UP && blank_row1
                    < targetPoints[state[blank_row1][blank_column1]][0]){
                h1 = h - 1;
            } else if(direction == RIGHT && blank_column1
                    > targetPoints[state[blank_row1][blank_column1]][1]) {
                h1 = h - 1;
            } else if(direction == LEFT && blank_column1
                    < targetPoints[state[blank_row1][blank_column1]][1]) {
                h1 = h - 1;
            } else {
                //这种情况发生在任意可能的移动方向都会使得估价函数值变大  
                h1 = h + 1;
            }

            if(h1+dep+1>ans) { //剪枝  
                continue;
            }

            moves[dep] = direction;

            //迭代深度求解  
            if(solve(state2, blank_row1, blank_column1, dep+1, direction, h1)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 得到估价函数值 
     */
    public int getHeuristic(int[][] state) {
        int heuristic = 0;
        for(int i=0;i<state.length;i++) {
            for(int j=0;j<state[i].length;j++) {
                if(state[i][j] != 0) {
                    heuristic = heuristic +
                            Math.abs(targetPoints[state[i][j]][0] - i)
                            + Math.abs(targetPoints[state[i][j]][1] - j);

                }
            }
        }
        return heuristic;
    }
    //——————————————————————————————————————————————————
    //——————————————————————引用的分割线——————————————————————
    //——————————————————————————————————————————————————
    public void getState(int[][] state){
        sState = state;
    }
    public int[] returnMoves(){
        return moves;
    }
}  