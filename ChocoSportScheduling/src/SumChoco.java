import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

public class SumChoco {
    // Tính tổng của matrix variables
    public static void main(String[] args) {
        int N = 2;
        int M = 3;
        Model model = new Model("Choco Solver Hello World");
        IntVar[][] vars = model.intVarMatrix("vs", N, M, 0, 1);

        IntVar[] flatArray = new IntVar[N * N];
        for (int index = 0; index < N * N; index++) {
            int i = index / N;
            int j = index % N;
            flatArray[index] = vars[i][j];
        }

        model.sum(flatArray, "=", 1).post();

        Solver solver = model.getSolver();
        if (solver.solve()) {

            for (int i = 0; i < N; i++) {
                for (int j = 0; j < M; j++) {
                    System.out.print(vars[i][j].getValue() + " ");
                }
                System.out.println();
            }

        }

    }
}
