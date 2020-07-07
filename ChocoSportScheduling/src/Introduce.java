import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

public class Introduce {

    public static void main(String[] args) {
        // The model is the main component of Choco Solver
        Model model = new Model("Choco Solver Hello World");
        // Integer variables
        IntVar a = model.intVar("a", new int[]{4, 6, 8}); // takes value in { 4, 6, 8 }
        IntVar b = model.intVar("b", 0, 2); // takes value in [0, 2]

        // Add an arithmetic constraint between a and b
        // BEWARE : do not forget to call post() to force this constraint to be satisfied
        model.arithm(a, "+", b, "<", 8).post();

        int i = 1;
        // Computes all solutions : Solver.solve() returns true whenever a new feasible solution has been found
        while (model.getSolver().solve()) {
            System.out.println("Solution " + i++ + " found : " + a + ", " + b);
        }
    }
}
