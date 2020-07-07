import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SchoolSchedulingChoco {

    int somon = 0;
    int sophong = 0;
    int[] tinchi = new int[100];
    int[] sosv = new int[100];
    int[] giaovienday = new int[100];
    int[] chongoi = new int[100];

    long time_start;
    long time_end;

    IntVar X[][][];
    IntVar Y[];

    Model model;

    private void buildModel() {
        time_start = System.currentTimeMillis();
        model = new Model("School Scheduling");

        X = new IntVar[somon][8][sophong];
        Y = new IntVar[somon];
        for (int i = 0; i < somon; i++) {
            Y[i] = model.intVar(0, 5);
            for (int j = 0; j < 8; j++) {
                for (int k = 0; k < sophong; k++) {
                    X[i][j][k] = model.intVar(0, 1);
                }
            }
        }

        // Contrainst =================================================================================================
        // mỗi lớp học chỉ học trong duy nhất một buổi
        for (int i = 0; i < somon; i++) {
            IntVar[] flatMatrix = new IntVar[8 * sophong];
            for (int index = 0; index < 8 * sophong; index++) {
                int j = index % 8;
                int k = index / 8;
                flatMatrix[index] = X[i][j][k];
            }
            model.sum(flatMatrix, "=", 1).post();
        }

        // số sinh viên trong một lớp phải nhỏ hơn sức chứa của phòng học
        for (int i = 0; i < somon; i++) {
            for (int k = 0; k < sophong; k++) {
                if (sosv[i] > chongoi[k]) {
                    for (int j = 0; j < 8; j++) {
                        model.arithm(X[i][j][k], "=", 0).post();
                    }
                }
            }
        }

        // không buổi nào được bắu đầu vào sáng và kết thúc vào chiều ( tiết kết thúc y[i] + tiet[i] -  1 <= 5)
        for (int i = 0; i < somon; i++) {
            model.arithm(model.intOffsetView(Y[i], tinchi[i]), "<=", 6).post();
        }

        // hai môn học học cùng ngày cùng phòng thì phải khác tiết
        for (int j = 0; j < 8; j++) {
            for (int k = 0; k < sophong; k++) {
                for (int i1 = 0; i1 < somon - 1; i1++) {
                    for (int i2 = i1 + 1; i2 < somon; i2++) {
                        model.ifThen(
                                model.and(
                                        model.arithm(X[i1][j][k], "=", 1),
                                        model.arithm(X[i2][j][k], "=", 1)
                                ),
                                model.or(
                                        model.arithm(model.intOffsetView(Y[i1], tinchi[i1]), "<=", Y[i2]),
                                        model.arithm(model.intOffsetView(Y[i2], tinchi[i2]), "<=", Y[i1])
                                )
                        );
                    }
                }
            }
        }

        // nếu hai môn cùng giáo viên dạy thì phải khác ngày hoặc cùng ngày khác tiết
        for (int i1 = 0; i1 < somon - 1; i1++) {
            for (int i2 = i1 + 1; i2 < somon; i2++) {
                if (giaovienday[i1] == giaovienday[i2]) {
                    for (int j = 0; j < 8; j++) {
                        for (int k1 = 0; k1 < sophong; k1++) {
                            for (int k2 = 0; k2 < sophong; k2++) {
                                model.ifThen(
                                        model.and(
                                                model.arithm(X[i1][j][k1], "=", 1),
                                                model.arithm(X[i2][j][k2], "=", 1)
                                        ),
                                        model.or(
                                                model.arithm(model.intOffsetView(Y[i1], tinchi[i1]), "<=", Y[i2]),
                                                model.arithm(model.intOffsetView(Y[i2], tinchi[i2]), "<=", Y[i1])
                                        )
                                );
                            }
                        }
                    }
                }
            }
        }

    }

    public void search() {
        Solution solution = model.getSolver().findSolution();
        time_end = System.currentTimeMillis();
        if (solution != null) {
            for (int i = 0; i < somon; i++) {
                for (int j = 0; j < 8; j++) {
                    for (int k = 0; k < sophong; k++) {
                        if (X[i][j][k].getValue() == 1) {
                            System.out.print(
                                    "Class " + i + " learn on " + j +
                                    " start at " + Y[i].getValue() +
                                    " in room " + k + "\n");
                        }
                    }
                }
            }
        }
    }

    private final static String FILE_URL = "/home/thangnn/Documents/HUST/Constraint-based Local Search/Project/OpenCBLS/data/17_3_5.txt";

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
        SchoolSchedulingChoco sport = new SchoolSchedulingChoco();

        String textInALine;
        int line = 0;
        while ((textInALine = br.readLine()) != null) {
            if (line == 2) {
                sport.sophong = parseFirstNum(textInALine);
            } else if (line == 3) {
                sport.somon = parseFirstNum(textInALine);
            } else if (line == 5) {
                sport.chongoi = parseAllLine(textInALine);
            } else if (line == 6) {
                sport.sosv = parseAllLine((textInALine));
            } else if (line == 7) {
                sport.tinchi = parseAllLine(textInALine);
            } else if (line == 8) {
                sport.giaovienday = parseAllLine(textInALine);
            }
            line += 1;
        }

        sport.buildModel();
        sport.search();
        System.out.println(sport.time_end - sport.time_start);
    }
}
