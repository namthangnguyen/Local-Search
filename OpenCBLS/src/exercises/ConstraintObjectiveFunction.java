/*
* Use Hill Climbing Search to solve Constraint Objective Function
*
*
* */
package exercises;

import localsearch.constraints.alldifferent.AllDifferent;
import localsearch.functions.basic.ConstraintViolations;
import localsearch.functions.basic.FuncMult;
import localsearch.functions.basic.FuncPlus;
import localsearch.model.*;
import java.util.ArrayList;
import java.util.Random;

public class ConstraintObjectiveFunction {

    class Move {
        int i; int v;
        public Move(int i, int v) {
            this.i = i; this.v = v;
        }
    }

    private IFunction f;
    private IConstraint c;
    private VarIntLS[] X;
    private IFunction F;
    private Random R = new Random();

    public ConstraintObjectiveFunction(IConstraint c, IFunction f, VarIntLS[] X) {
        this.c = c; this.f = f; this.X = X;
        IFunction cv = new ConstraintViolations(c);
        // Hàm mục tiêu
        F = new FuncPlus(new FuncMult(cv, 1000), new FuncMult(f, 1));
    }

    public void exploreNeighborhood(ArrayList<Move> cand) {
        cand.clear();
        int minDelta = Integer.MAX_VALUE;
        for (int i = 0; i < X.length; i++) {
            for (int v = X[i].getMinValue(); v <= X[i].getMaxValue(); v++) {
                int delta = F.getAssignDelta(X[i], v);
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

    public void search(int maxIter) {
        int iter = 0;
        ArrayList<Move> cand = new ArrayList<>();
        while (iter < maxIter) {
            exploreNeighborhood(cand);
            if (cand.size() == 0) {
                System.out.println("Reach local optimum");
                break;
            }
            Move m = cand.get(R.nextInt(cand.size()));
            X[m.i].setValuePropagate(m.v);
            System.out.println(
                    "Step " + iter +
                    ", F = " + F.getValue() +
                    ", c = " + c.violations() +
                    ", f = " + f.getValue()
            );

            iter++;
        }
    }

    public static void main(String[] args) {
        // MODEL
        LocalSearchManager mgr = new LocalSearchManager();
        VarIntLS[] X = new VarIntLS[6];
        for (int i = 0; i <= 5; i++) X[i] = new VarIntLS(mgr, 1, 5);
        ConstraintSystem S = new ConstraintSystem(mgr);
        S.post(new AllDifferent(X));
        IFunction f = new FuncPlus(new FuncMult(X[0], 3), new FuncPlus(X[4], 5));
        mgr.close();

        // SEARCH
        ConstraintObjectiveFunction searcher = new ConstraintObjectiveFunction(S, f, X);
        searcher.search(1000);
    }
}
