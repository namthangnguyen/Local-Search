import java.io.*;

public class readFile {

    private final static String FILE_URL = "/home/thangnn/Documents/HUST/Constraint-based Local Search/Project/OpenCBLS/data/20_5_5.txt";

    public static int[] parseAllLine(String a) {
        String[] integerStrings = a.split(" ");
        int[] integers = new int[integerStrings.length];
        for (int i = 0; i < integers.length; i++){
            integers[i] = Integer.parseInt(integerStrings[i]);
        }
        return integers;
    }

    public static int parseFirstNum(String a) {
        String[] integerStrings = a.split(" ");
        int integers = Integer.parseInt(integerStrings[0]);
        return integers;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(FILE_URL));

        int somon = 0;
        int sophong = 0;
        int[] tinchi = new int[100];
        int[] sosv = new int[100];
        int[] giaovienday = new int[100];
        int[] chongoi = new int[100];

        String textInALine;
        int line = 0;
        while ((textInALine = br.readLine()) != null) {
            if (line == 2) {
                sophong = parseFirstNum(textInALine);
            } else if (line == 3) {
                somon = parseFirstNum(textInALine);
            } else if (line == 5) {
                chongoi = parseAllLine(textInALine);
            } else if (line == 6) {
                sosv = parseAllLine((textInALine));
            } else if (line == 7) {
                tinchi = parseAllLine(textInALine);
            } else if (line == 8) {
                giaovienday = parseAllLine(textInALine);
            }
            line += 1;
        }
        br.close();
    }
}

