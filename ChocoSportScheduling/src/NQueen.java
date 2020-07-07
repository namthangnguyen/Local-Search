import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;

public class NQueen {

    public static void main(String[] args) {
        int n = 8;
        Model model = new Model(n + "-queens problem");
        IntVar[] vars = model.intVarArray("Q", n, 1, n, false);

        IntVar[] diag1 = new IntVar[n];
        IntVar[] diag2 = new IntVar[n];

        for(int i = 0 ; i < n; i++){
            diag1[i] = vars[i].sub(i).intVar();
            diag2[i] = vars[i].add(i).intVar();
        }

        model.post(
                model.allDifferent(vars),
                model.allDifferent(diag1),
                model.allDifferent(diag2)
        );

        Solver solver = model.getSolver();
        solver.setSearch(Search.domOverWDegSearch(vars));
        Solution solution = solver.findSolution();
        if (solution != null) {
            System.out.println(solution.toString());
        }

    }
}
