package search_strategy;

import localsearch.model.IConstraint;
import localsearch.model.VarIntLS;
import java.util.ArrayList;
import java.util.Random;

public class HillClimbingSearch {

    class AssignMove{
        int i;
        int v;
        public AssignMove(int i, int v){
            this.i = i;
            this.v = v;
        }
    }

    Random R = new Random();

    // Thu thập vào danh sách candidate những cặp biến i, v làm cho mức độ suy giảm của vi phạm ràng buộc là lớn nhất
    private void exploreNeighborhood(IConstraint c, VarIntLS[] x, ArrayList<AssignMove> candidate) {
        int minDelta = Integer.MAX_VALUE;
        candidate.clear();
        for (int i = 0; i < x.length; i++) {
            for (int v = x[i].getMinValue(); v <= x[i].getMaxValue(); v++) {
                if (v == x[i].getValue()) continue; // ignore current solution
                // getAssignDelta trả về sự thay đổi mức độ vi phạm của ràng buộc c khi x[i] có giá trị mới là v
                // số vi phạm ràng buộc mới - số vi phạm ràng buộc hiện tại => càng âm càng tốt
                int delta = c.getAssignDelta(x[i], v);
                if(delta < minDelta) {
                    candidate.clear();
                    candidate.add(new AssignMove(i, v));
                    minDelta = delta;
                } else if (delta == minDelta){
                    candidate.add(new AssignMove(i,v));
                }
            }
        }
    }

    private void generateInitialSolution(VarIntLS[] x) {
        for (int i = 0; i < x.length; i++) {
            int v = R.nextInt(x[i].getMaxValue() - x[i].getMinValue() + 1) + x[i].getMinValue();
            x[i].setValuePropagate(v);
        }
    }

    public void search(IConstraint c, int maxIter) {
        // Chỉ lấy decision variables mà bị ràng buộc (c là constraint system)
        VarIntLS [] x = c.getVariables();
        generateInitialSolution(x);
        int it = 0;
        ArrayList<AssignMove> candidate = new ArrayList<AssignMove>();
        while(it < maxIter && c.violations() > 0) {
            exploreNeighborhood(c, x, candidate);
            // rơi vào tối ưu cục bộ
            if (candidate.size() == 0) {
                System.out.println("Reach local optimum");
                break;
            }
            AssignMove m = candidate.get(R.nextInt((candidate.size())));
            // local move (assign value, update violations, data structures)
            // set giá trị mới cho biến quyết định x[i] và lan truyền để cập nhât violation, các giá trị ràng buộc mới,... tới các biến khác
            // ví dụ x[i] = 6, mà có ràng buộc x[j] != x[i] => bây giờ x[j] != 6
            x[m.i].setValuePropagate(m.v);
            it++;
            System.out.println("Step "+ it + ", violations = " + c.violations());
        }
    }
}
