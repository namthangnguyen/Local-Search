package search_strategy;

import localsearch.model.ConstraintSystem;
import localsearch.model.IConstraint;
import localsearch.model.IFunction;
import localsearch.model.VarIntLS;

import java.util.ArrayList;
import java.util.Random;

public class TabuSearch {
    class Move {
        int i;
        int v;

        public Move(int i, int v) {
            this.i = i;
            this.v = v;
        }
    }

    int tabu[][];
    int tbl;
    IConstraint c;
    VarIntLS[] x;
    int N;// number of variables;
    int D;// max domain
    int bestViolations;
    Random R = new Random();
    int nic = 0;

    public TabuSearch(ConstraintSystem s) {
        // assumption minValue of domain >= 0


    }

    private void restart() {
        for (int i = 0; i < N; i++) {
            int v = R.nextInt(x[i].getMaxValue() - x[i].getMinValue() + 1) + x[i].getMinValue();
            x[i].setValuePropagate(v);
        }
        if (c.violations() < bestViolations) {
            bestViolations = c.violations();
        }
    }

    public void search(IConstraint c, IFunction f, int tblen, int maxStable) {
        // tim loi giai thoa man rang buoc c, dong thoi minimize function f


    }

    public void search(IConstraint c, int maxIter, int tblen, int maxStable) {
        this.c = c;
        x = c.getVariables();
        N = x.length;
        D = 0;
        for (int i = 0; i < x.length; i++)
            D = D < x[i].getMaxValue() ? x[i].getMaxValue() : D;
        tabu = new int[N][D + 1];
        for (int i = 0; i < N; i++)
            for (int v = 0; v <= D; v++)
                tabu[i][v] = -1;

        this.tbl = tblen;
        bestViolations = c.violations();
        ArrayList<Move> cand = new ArrayList<>();
        nic = 0;
        //for(int it = 0; it <= maxIter; it++){
        int it = 0;
        while (it <= maxIter && bestViolations > 0) {
            int minDelta = Integer.MAX_VALUE;
            cand.clear();
            for (int i = 0; i < N; i++) {
                for (int v = x[i].getMinValue(); v <= x[i].getMaxValue(); v++)
                    if (x[i].getValue() != v) {
                        int delta = c.getAssignDelta(x[i], v);
                        if (tabu[i][v] <= it || delta + c.violations() < bestViolations) {
                            if (delta < minDelta) {
                                cand.clear();
                                cand.add(new Move(i, v));
                                minDelta = delta;
                            } else if (delta == minDelta) {
                                cand.add(new Move(i, v));
                            }
                        }
                    }
            }

            Move m = cand.get(R.nextInt(cand.size()));
            x[m.i].setValuePropagate(m.v);
            tabu[m.i][m.v] = it + tbl;// dua move(i,v) vao DS tabu

            if (c.violations() < bestViolations) {
                bestViolations = c.violations();
                nic = 0;
            } else {
                nic++;
                if (nic >= maxStable) {
                    restart();
                }
            }
            System.out.println("Step " + it + " violations = " + c.violations()
                    + ", bestViolations = " + bestViolations);
            it++;
        }

    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
