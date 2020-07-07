package test_syntax_cbls;

import localsearch.functions.conditionalsum.ConditionalSum;

import localsearch.model.IFunction;
import localsearch.model.LocalSearchManager;
import localsearch.model.VarIntLS;

public class ConditionSum {

    public static void main(String[] args) {
        LocalSearchManager mgr = new LocalSearchManager();
        int N = 5;
        int[] w = new int[]{3, 2, 5, 3, 6}; // trọng số của mỗi biến quyết định X[i] (có thể có nhiều loại trọng số)
        VarIntLS[] X = new VarIntLS[N];
        for(int i = 0; i < N; i++) {
            X[i] = new VarIntLS(mgr,1,5);
        }

        X[0].setValue(2); X[1].setValue(1);
        X[2].setValue(5); X[3].setValue(2);
        X[4].setValue(3);

        IFunction s = new ConditionalSum(X, w, 2); // tổng các trọng số của các X[i], khi X[i] = 2
        IFunction s2 = new ConditionalSum(X, 3); // số các X[i] có giá trị = 3

        mgr.close();

        System.out.println("s = " + s.getValue());
        System.out.println("s = " + s2.getValue());

        // khi đóng mô hình (mgr.close()) chương trình mới bắt gán các biến quyết định, tính toán các vi phạm, giá trị cá hàm,...
        // và tạo ra đồ thị phụ thuộc
        // 1. => lúc này mới gọi đc s.getValue()
        // 2. khi muốn set giá trị cho biến quyết định sau khi đóng mô hình thì phải dùng hàm setValuePropagate()
        // để vừa set giá trị vừa cập nhật lại các vi phạm, giá trị các hàm,..

        X[1].setValuePropagate(2);
        System.out.println("\ns = " + s.getValue());

        // xem giá trị s thay đổi bao nhiêu khi thay X[0] = 1, nhưng s chưa thay đổi
        // phải cập nhật X[0] bằng setValuePropagate ở phía dưới thì s mới thật sự thay đổi
        int d = s.getAssignDelta(X[0], 1);
        System.out.println("d = " + d + ", s = " + s.getValue());
        X[0].setValuePropagate(1);
        System.out.println("s = " + s.getValue());
    }
}
