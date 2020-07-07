package test_syntax_cbls;

import localsearch.functions.sum.Sum;
import localsearch.model.IFunction;
import localsearch.model.LocalSearchManager;
import localsearch.model.VarIntLS;

public class Sum2DVarLS {
    public static void main(String[] args) {
        LocalSearchManager mgr = new LocalSearchManager();
        VarIntLS[][] X = new VarIntLS[9][9];
        for(int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                X[i][j] = new VarIntLS(mgr, 1, 9);
                X[i][j].setValue(1);
            }
        }

        IFunction[] c = new IFunction[9];
        for (int i = 0; i < 9; i++) {
            VarIntLS[] Z = new VarIntLS[9];
            for (int j = 0; j < 9; j++) {
                Z[j] = X[i][j];
            }
            c[i] = new Sum(Z);
        }
        IFunction s = new Sum(c);

        mgr.close();
        System.out.println("tong ma tran " + s.getValue());
    }
}
