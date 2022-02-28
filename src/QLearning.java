import java.io.File;
import java.util.*;

public class QLearning {

    private static float learnTime;
    private static String map_file;
    private static double epsilon = 0.1; // Learning rate/e-greedy
    private static double gamma = 0.8; // Eagerness - 0 looks in the near future, 1 looks in the distant future

    private static int grid_width = 1;
    private static int grid_height = 1;
    private int statesCount = 0;

    private int[][] gridworld;  // Maze read from file
    private static ArrayList<Integer> arrayList = new ArrayList<>();
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
        gamma = Double.parseDouble(args[3]) * -1;

        ql.init();
        ql.calculateQ();
        ql.printQ();
        ql.printPolicy();
    }

    public void init() {
        String path = "src/" + map_file;
        System.out.println(path);
        int [] dimensions = file_dimensions(path);
        grid_height = dimensions[0];
        grid_width = dimensions[1];
        statesCount = grid_height * grid_width;
        R = new int[statesCount][statesCount];
        Q = new double[statesCount][statesCount];
        gridworld = new int[grid_height][grid_width];
        gridworld = fileTo2DArray(path, grid_height, grid_width);


        int i = 0;
        int j = 0;

        // We will navigate through the reward matrix R using k index
        for (int k = 0; k < statesCount; k++) {

            // We will navigate with i and j through the maze, so we need
            // to translate k into i and j
            i = k / grid_width;
            j = k - i * grid_width;

            // Fill in the reward matrix with -1
            for (int s = 0; s < statesCount; s++) {
                R[k][s] = -1;
            }

            // If not in final state or a wall try moving in all directions in the maze
            if (gridworld[i][j] == 0)  {
                // Try to move left in the maze
                int goLeft = j - 1;
                if (goLeft >= 0) {
                    int target = i * grid_width + goLeft;
                    if (gridworld[i][goLeft] == 0) {
                        R[k][target] = 0;
                    } else if (gridworld[i][goLeft] != 0) {
                        int temp = gridworld[i][goLeft];
                        R[k][target] = temp;
                    }
                }

                // Try to move right in the maze
                int goRight = j + 1;
                if (goRight < grid_width) {
                    int target = i * grid_width + goRight;
                    if (gridworld[i][goRight] == 0) {
                        R[k][target] = 0;
                    } else if (gridworld[i][goRight] != 0) {
                        int temp = gridworld[i][goRight];
                        R[k][target] = temp;
                    }
                }

                // Try to move up in the maze
                int goUp = i - 1;
                if (goUp >= 0) {
                    int target = goUp * grid_width + j;
                    if (gridworld[goUp][j] == 0) {
                        R[k][target] = 0;
                    } else if (gridworld[goUp][j] != 0) {
                        int temp = gridworld[goUp][j];
                        R[k][target] = temp;
                    }
                }

                // Try to move down in the maze
                int goDown = i + 1;
                if (goDown < grid_height) {
                    int target = goDown * grid_width + j;
                    if (gridworld[goDown][j] == 0) {
                        R[k][target] = 0;
                    } else if (gridworld[goDown][j] != 0) {
                        int temp = gridworld[goDown][j];
                        R[k][target] = temp;
                    }
                }
            }
        }
        initializeQ();
        printR(R);
    }

    //This takes the file path and returns an array with [height, width]
    public static int[] file_dimensions(String path){
        int height = 1; //The height starts at 1 because the while loop checks every line after the first.
        int width = 0;
        try {
            //Take path and make a file object.
            File file = new File(path);
            //Initialize scanner object and associate it with the file.
            Scanner sc = new Scanner(file);
            String temp = sc.nextLine();
            //Loop through all the lines so long as there is a next line.
            while(sc.hasNextLine()) {
                sc.nextLine();
                height++;
            }
            String[] line_split = temp.split("\t");
            System.out.println("Map Height: " + height);
            System.out.println("Map Width: " + line_split.length);
            width = line_split.length;
            sc.close();
        } catch (Exception e) {
            e.getStackTrace();
        }



        return new int[]{height, width};
    }

    //Set Q values to R values
    void initializeQ()
    {
        for (int i = 0; i < statesCount; i++){
            for(int j = 0; j < statesCount; j++){
                Q[i][j] = R[i][j];
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
            if (System.nanoTime() - start > learnTime) {
                learning = false;
            }
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

                double value = q + (epsilon * (r + gamma * maxQ - q));
                Q[crtState][nextState] = value;

                crtState = nextState;
            }
        }
    }

    boolean isFinalState(int state) {
        int i = state / grid_width;
        int j = state - i * grid_width;

        return gridworld[i][j] != 0;
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

    public static int[][] fileTo2DArray(String path, int height, int width){
        int index = 1; //The height starts at 1 because the while loop checks every line after the first.

        int[][] gridArray = new int[height][width];
        try {
            //Take path and make a file object.
            File file = new File(path);
            //Initialize scanner object and associate it with the file.
            Scanner sc = new Scanner(file);
            String temp = sc.nextLine();
            String[] line_split = temp.split("\t");
            for (int i = 0; i < line_split.length; i++){
                gridArray[0][i] = Integer.parseInt(line_split[i]);
                arrayList.add(Integer.parseInt(line_split[i]));
            }
            //Loop through all the lines so long as there is a next line.
            while(index < height + 1) {
                temp = sc.nextLine();
                line_split = temp.split("\t");
                for (int i = 0; i < line_split.length; i++){
                    gridArray[index][i] = Integer.parseInt(line_split[i]);
                    arrayList.add(Integer.parseInt(line_split[i]));
                }
                index++;
            }
            System.out.println("Map Height: " + height);
            System.out.println("Map Height: " + line_split.length);
            sc.close();
        } catch (Exception e) {
            e.getStackTrace();
        }

        return gridArray;
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