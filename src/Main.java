import java.io.File;
import java.util.Arrays;
import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        String path = "C:/Users/dlean/IdeaProjects/test zip/test/src/sample.txt";
        int grid_dimensions[] = file_dimensions(path);
        int grid_width = grid_dimensions[1];
        int grid_height = grid_dimensions[0];
        int[][] gridArray = new int[grid_width][grid_height];

    }
    //This takes the file path and returns an array with [height, width]
    public static int[] file_dimensions(String path){
        int height = 1; //The height starts at 1 because the while loop checks every line after the first.
        int width = 0;
        int[][] gridArray = new int[6][11];
        try {
            //Take path and make a file object.
            File file = new File(path);
            //Initialize scanner object and associate it with the file.
            Scanner sc = new Scanner(file);
            String temp = sc.nextLine();
            String[] line_split = temp.split("\t");
            System.out.println("This is temp: " + temp);
            for (int i = 0; i < line_split.length; i++){
                gridArray[0][i] = Integer.parseInt(line_split[i]);
            }
            //Loop through all the lines so long as there is a next line.
            while(sc.hasNextLine()) {
                //sc.nextLine();
                temp = sc.nextLine();
                System.out.println("This is temp: " + temp);
                for (int i = 0; i < line_split.length; i++){
                    gridArray[height][i] = Integer.parseInt(line_split[i]);
                }
                height++;
            }
            System.out.println("Map Height: " + height);
            System.out.println("Map Height: " + line_split.length);
            width = line_split.length;
            sc.close();
        } catch (Exception e) {
            e.getStackTrace();
        }

        System.out.println("=========================================================");
        System.out.println(Arrays.deepToString(gridArray));

        return new int[]{height, width};
    }
    public static int[][] file2array(String path, int height, int width){


        return null;
    }
}
