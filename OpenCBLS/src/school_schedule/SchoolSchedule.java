package school_schedule;

import localsearch.constraints.basic.*;
import localsearch.functions.basic.FuncPlus;
import localsearch.functions.sum.Sum;
import localsearch.model.ConstraintSystem;
import localsearch.model.IFunction;
import localsearch.model.LocalSearchManager;
import localsearch.model.VarIntLS;
import localsearch.selectors.MinMaxSelector;

public class SchoolSchedule {
    int somon = 12;
    int[] tinchi = {2, 4, 2, 1, 3, 2, 4, 3, 2, 3, 4, 3};
    int[] sosv = {30, 60, 20, 40, 120, 100, 30, 20, 60, 50, 120, 40};
    int[] giaovienday = {1, 2, 3, 4, 3, 2, 4, 1, 1, 3, 2, 2};
    int sophong = 3;
    int[] chongoi = {30, 60, 120};

    long time_start;
    long time_end;
    long WAIT_TIME = 1000 * 30 * 60; // max time = 30 phut

    LocalSearchManager mgr;
    ConstraintSystem S;
    VarIntLS X[][][]; // môn học i, học ngày j, phòng học k
    VarIntLS Y[]; // tiết học bắt đầu

    public void stateModel() {
        time_start = System.currentTimeMillis();
        mgr = new LocalSearchManager();
        X = new VarIntLS[somon][8][sophong];
        Y = new VarIntLS[somon];

        // set value for bien quyet dinh
        for (int i = 0; i < somon; i++) {
            Y[i] = new VarIntLS(mgr, 0, 5);
            for (int j = 0; j < 8; j++) {
                for (int k = 0; k < sophong; k++) {
                    X[i][j][k] = new VarIntLS(mgr, 0, 1);
                }
            }
        }

        S = new ConstraintSystem(mgr);

        // mỗi lớp học chỉ học trong duy nhất một buổi
        for (int i = 0; i < somon; i++) {
            IFunction[] tongcot = new IFunction[8];
            for (int j = 0; j < 8; j++) {
                VarIntLS[] Z = new VarIntLS[sophong];
                for (int k = 0; k < sophong; k++) {
                    Z[k] = X[i][j][k];
                }
                tongcot[j] = new Sum(Z);
            }
            IFunction tong = new Sum(tongcot);
            S.post(new IsEqual(tong, 1));
        }

        // số sinh viên trong một lớp phải nhỏ hơn sức chứa của phòng học
        for (int i = 0; i < somon; i++) {
            for (int k = 0; k < sophong; k++) {
                if (sosv[i] > chongoi[k]) {
                    for (int j = 0; j < 8; j++) {
                        S.post(new IsEqual(X[i][j][k], 0));
                    }
                }
            }
        }

        // không buổi nào được bắu đầu vào sáng và kết thúc vào chiều ( tiết kết thúc y[i] + tiet[i] -  1 <= 5)
        for (int i = 0; i < somon; i++) {
            S.post(new LessOrEqual(new FuncPlus(Y[i], tinchi[i]), 6));
        }

        // hai môn học học cùng ngày cùng phòng thì phải khác tiết
        for (int j = 0; j < 8; j++) {
            for (int k = 0; k < sophong; k++) {
                for (int i1 = 0; i1 < somon - 1; i1++) {
                    for (int i2 = i1 + 1; i2 < somon; i2++) {
                        S.post(new Implicate(
                                new AND(
                                        new IsEqual(X[i1][j][k], 1),
                                        new IsEqual(X[i2][j][k], 1)
                                ),
                                new OR(
                                        new LessOrEqual(
                                                new FuncPlus(Y[i1], tinchi[i1]),
                                                new FuncPlus(Y[i2], 1)
                                        ),
                                        new LessOrEqual(
                                                new FuncPlus(Y[i2], tinchi[i2]),
                                                new FuncPlus(Y[i1], 1)
                                        )
                                ))
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
                                S.post(new Implicate(
                                        new AND(
                                                new IsEqual(X[i1][j][k1], 1),
                                                new IsEqual(X[i2][j][k2], 1)
                                        ),
                                        new OR(
                                                new LessOrEqual(
                                                        new FuncPlus(Y[i1], tinchi[i1]),
                                                        new FuncPlus(Y[i2], 1)
                                                ),
                                                new LessOrEqual(
                                                        new FuncPlus(Y[i2], tinchi[i2]),
                                                        new FuncPlus(Y[i1], 1)
                                                )
                                        ))
                                );
                            }
                        }
                    }
                }
            }
        }
    }

//    public void search(){
//        HillClimbingSearch searcher = new HillClimbingSearch();
//        searcher.search(S, 10000);
//    }

    private void localSearch() {
        System.out.println("init, S = " + S.violations());
        int it = 1;
        while (it < 10000 && S.violations() > 0) {
            MinMaxSelector mms = new MinMaxSelector(S);
            VarIntLS sel_x = mms.selectMostViolatingVariable();
            int sel_value = mms.selectMostPromissingValue(sel_x);
            sel_x.setValuePropagate(sel_value);    // local move: assign value,
            // propagate update violations,
            // thanks to dependency graph
            System.out.println("Step " + it + ", S = " + S.violations());
            it++;
        }
        System.out.print("Best solution: ");
        printResult();
    }

    public boolean search() {
        boolean hasSolution;
        System.out.println(S.getVariables());
        TabuSearch searcher = new TabuSearch(S);
        hasSolution = searcher.search_in_limit_time(1000, 4, 100, WAIT_TIME);
        time_end = System.currentTimeMillis();
        return hasSolution;
    }

    public void printResult() {
        for (int i = 0; i < somon; i++) {
            System.out.println("mon " + i + " tiet bat dau: " + Y[i].getValue());
            for (int j = 0; j < 8; j++) {
                for (int k = 0; k < sophong; k++) {
                    if (X[i][j][k].getValue() == 1) {
                        System.out.print("ngay " + j + " phong " + k);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        SchoolSchedule app = new SchoolSchedule();
        boolean hasSolution = false;
        app.stateModel();
        app.localSearch();
        hasSolution = app.search();
        if (hasSolution) {
            app.printResult();
        }
//        app.search();
//        app.printResult();
    }
}
