import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.io.IOException;

public class QLearning {

    private static float learnTime;
    private static String map_file;
    private static double epsilon = 0.1; // Learning rate/e-greedy
    private static double gamma = 0.9; // Eagerness - 0 looks in the near future, 1 looks in the distant future

    private final int mazeWidth = 6;
    private final int mazeHeight = 11;
    private final int statesCount = mazeHeight * mazeWidth;

    private char[][] maze;  // Maze read from file
    private int[][] R;       // Reward lookup
    private double[][] Q;    // Q learning


    public static void main(String args[]) {
        //The name of the file to read in representing the map
        map_file = args[0];
        QLearning ql = new QLearning();
        //How long to learn (in seconds)
        learnTime = Float.parseFloat(args[1]) * 1000000000; //convert input into nano seconds
        //The probability of moving in the desired direction upon taking an action (0.8 recreates behavior from value iteration example).
        epsilon = Double.parseDouble(args[2]);
        //The constant reward for each action
        gamma = Double.parseDouble(args[3]);

        ql.init();
        ql.calculateQ();
        ql.printQ();
        ql.printPolicy();
    }

    public void init() {
        File file = new File("src/" + map_file);

        R = new int[statesCount][statesCount];
        Q = new double[statesCount][statesCount];
        maze = new char[mazeHeight][mazeWidth];

        //with open(file, 'r') as f:
            //lines = f.readlines();


        try (FileInputStream fis = new FileInputStream(file)) {

            int i = 0;
            int j = 0;

            int content;


            // Read the maze from the input file
            while ((content = fis.read()) != -1) {
                char c = (char) content;
                if (c != '0' && c != '5' && c != '3') {
                    continue;
                }
                maze[i][j] = c;
                j++;
                if (j == mazeWidth) {
                    j = 0;
                    i++;
                }
            }

            // We will navigate through the reward matrix R using k index
            for (int k = 0; k < statesCount; k++) {

                // We will navigate with i and j through the maze, so we need
                // to translate k into i and j
                i = k / mazeWidth;
                j = k - i * mazeWidth;

                // Fill in the reward matrix with -1
                for (int s = 0; s < statesCount; s++) {
                    R[k][s] = -1;
                }

                // If not in final state or a wall try moving in all directions in the maze
                if (maze[i][j] == '0') {

                    // Try to move left in the maze
                    int goLeft = j - 1;
                    if (goLeft >= 0) {
                        int target = i * mazeWidth + goLeft;
                        if (maze[i][goLeft] == '0') {
                            R[k][target] = 0;
                        } else if (maze[i][goLeft] != '0') {
                            R[k][target] = maze[i][j];
                        }
                    }

                    // Try to move right in the maze
                    int goRight = j + 1;
                    if (goRight < mazeWidth) {
                        int target = i * mazeWidth + goRight;
                        if (maze[i][goRight] == '0') {
                            R[k][target] = 0;
                        } else if (maze[i][goRight] != '0') {
                            R[k][target] = maze[i][j];
                        }
                    }

                    // Try to move up in the maze
                    int goUp = i - 1;
                    if (goUp >= 0) {
                        int target = goUp * mazeWidth + j;
                        if (maze[goUp][j] == '0') {
                            R[k][target] = 0;
                        } else if (maze[goUp][j] != '0') {
                            R[k][target] = maze[i][j];
                        }
                    }

                    // Try to move down in the maze
                    int goDown = i + 1;
                    if (goDown < mazeHeight) {
                        int target = goDown * mazeWidth + j;
                        if (maze[goDown][j] == '0') {
                            R[k][target] = 0;
                        } else if (maze[goDown][j] != '0') {
                            R[k][target] = maze[i][j];
                        }
                    }
                }
            }
            initializeQ();
            printR(R);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Set Q values to R values
    void initializeQ()
    {
        for (int i = 0; i < statesCount; i++){
            for(int j = 0; j < statesCount; j++){
                Q[i][j] = (double)R[i][j];
            }
        }
    }
    // Used for debug
    void printR(int[][] matrix) {
        System.out.printf("%25s", "States: ");
        for (int i = 0; i <= 8; i++) {
            System.out.printf("%4s", i);
        }
        System.out.println();

        for (int i = 0; i < statesCount; i++) {
            System.out.print("Possible states from " + i + " :[");
            for (int j = 0; j < statesCount; j++) {
                System.out.printf("%4s", matrix[i][j]);
            }
            System.out.println("]");
        }
    }

    void calculateQ() {
        Random rand = new Random();

        long start = System.nanoTime();
        boolean learning = true;
        while(learning) {
            if(System.nanoTime() - start > learnTime) {
                learning = false;
            }
            // Select random initial state
            int crtState = rand.nextInt(statesCount);

            while (!isFinalState(crtState)) {
                int[] actionsFromCurrentState = possibleActionsFromState(crtState);

                // Pick a random action from the ones possible
                int index = rand.nextInt(actionsFromCurrentState.length);
                int nextState = actionsFromCurrentState[index];

                // Q(state,action)= Q(state,action) + epsilon * (R(state,action) + gamma * Max(next state, all actions) - Q(state,action))
                double q = Q[crtState][nextState];
                double maxQ = maxQ(nextState);
                int r = R[crtState][nextState];

                double value = q + epsilon * (r + gamma * maxQ - q);
                Q[crtState][nextState] = value;

                crtState = nextState;
            }

        }
    }

    boolean isFinalState(int state) {
        int i = state / mazeWidth;
        int j = state - i * mazeWidth;

        return maze[i][j] > Integer.parseInt("0");
    }

    int[] possibleActionsFromState(int state) {
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < statesCount; i++) {
            if (R[state][i] != -1) {
                result.add(i);
            }
        }

        return result.stream().mapToInt(i -> i).toArray();
    }

    double maxQ(int nextState) {
        int[] actionsFromState = possibleActionsFromState(nextState);
        //the learning rate and eagerness will keep the W value above the lowest reward
        double maxValue = -10;
        for (int nextAction : actionsFromState) {
            double value = Q[nextState][nextAction];

            if (value > maxValue)
                maxValue = value;
        }
        return maxValue;
    }

    void printPolicy() {
        System.out.println("\nPrint policy");
        for (int i = 0; i < statesCount; i++) {
            System.out.println("From state " + i + " goto state " + getPolicyFromState(i));
        }
    }

    int getPolicyFromState(int state) {
        int[] actionsFromState = possibleActionsFromState(state);

        double maxValue = Double.MIN_VALUE;
        int policyGotoState = state;

        // Pick to move to the state that has the maximum Q value
        for (int nextState : actionsFromState) {
            double value = Q[state][nextState];

            if (value > maxValue) {
                maxValue = value;
                policyGotoState = nextState;
            }
        }
        return policyGotoState;
    }

    void printQ() {
        System.out.println("Q matrix");
        for (int i = 0; i < Q.length; i++) {
            System.out.print("From state " + i + ":  ");
            for (int j = 0; j < Q[i].length; j++) {
                System.out.printf("%6.2f ", (Q[i][j]));
            }
            System.out.println();
        }
    }
}