import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;

public class ifThenChoco {
    public static void main(String[] args) {
        int n = 3;
        Model model = new Model(n + "-queens problem");
        IntVar[] vars = model.intVarArray("Q", n, 1, 9, false);

        model.sum(vars, "=", 5).post();

        model.ifThen(
                model.or(
                        model.arithm(vars[0], "=", 1),
                        model.arithm(vars[0], "=", 2)
                ),
                model.arithm(vars[1], "=", 2)
        );

//        model.or(
//                model.arithm(vars[0], "=", 1),
//                model.arithm(vars[0], "=", 2)
//        ).post();

        int i = 1;
        // Computes all solutions : Solver.solve() returns true whenever a new feasible solution has been found
        while (model.getSolver().solve()) {
            for (int j = 0; j < n; j++) {
                System.out.println(vars[j]);
            }
            System.out.println("\n");
        }

    }
}
