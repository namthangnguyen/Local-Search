package school_schedule;

import localsearch.constraints.basic.AND;
import localsearch.constraints.basic.Implicate;
import localsearch.constraints.basic.IsEqual;
import localsearch.constraints.basic.LessOrEqual;
import localsearch.constraints.basic.OR;
import localsearch.functions.basic.FuncPlus;
import localsearch.functions.sum.Sum;
import localsearch.model.ConstraintSystem;
import localsearch.model.IConstraint;
import localsearch.model.LocalSearchManager;
import localsearch.model.VarIntLS;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class project {

	int shifts = 10;

	int num_rooms = 0;
	int num_classes = 0;
	int[] room_capacity = new int[100];
	int[] class_student = new int[100];
	int[] class_units = new int[100];
	int[] class_teacher = new int[100];

	long time_start;
	long time_end;
	long WAIT_TIME = 1000 * 30 * 60; // max time = 30 phut
	
	LocalSearchManager mgr;
	VarIntLS[] Y;
	VarIntLS[][] Z;
	VarIntLS[][] T;
	ConstraintSystem S;
	
	private void stateModel() {
		time_start = System.currentTimeMillis();
		mgr = new LocalSearchManager();
		// tiet bat dau cua mon i
		Y = new VarIntLS[num_classes];	
		// Mon i vao phong k
		Z = new VarIntLS[num_classes][num_rooms];
		//Mon hoc i vao buoi j
		T = new VarIntLS[num_classes][shifts];

		for(int i = 0; i < num_classes; i++) {
			Y[i] = new VarIntLS(mgr, 0, shifts-1);
		}
		for(int i = 0; i < num_classes; i++) {
			for(int k = 0; k < num_rooms; k++) {
				Z[i][k] = new VarIntLS(mgr, 0, 1);
			}
		}
		for(int i = 0; i < num_classes; i++) {
			for(int j = 0; j < shifts; j++) {
				T[i][j] = new VarIntLS(mgr, 0, 1);
			}
		}

		// ====================================
		S = new ConstraintSystem(mgr);
		
		// Moi mon hoc chi hoc duy nhat trong mot phong
		for(int i = 0; i < num_classes; i++) {
			S.post(new IsEqual(new Sum(Z[i]), 1));
		}
		//Moi mon hoc chi dien ra trong mot buoi
		for(int i = 0; i < num_classes; i++) {
			S.post(new IsEqual(new Sum(T[i]), 1));
		}
		
		// So sinh vien phai nho hon suc chua cua lop
		for(int i = 0; i < num_classes; i++) {
			for(int k = 0; k < num_rooms; k++) {
				if (class_student[i] > room_capacity[k]) {
					S.post(new IsEqual(Z[i][k], 0));
				}
			}
		}
		
		// Khong mon hoc nao nam ngoai khung thoi gian
		for(int i = 0; i < num_classes; i++) {
			S.post(new LessOrEqual(Y[i], 6-class_units[i]));
		}
		
		// Hai mon hoc cung mot thoi diem thi phai khac tiet
		for(int i1 = 0; i1 < num_classes-1; i1++) {
			for(int i2 = i1+1; i2 < num_classes; i2++) {
				IConstraint[] c1 = new IConstraint[2];
				c1[0] = new LessOrEqual(new FuncPlus(Y[i1], class_units[i1]), Y[i2]);
				c1[1] = new LessOrEqual(new FuncPlus(Y[i2], class_units[i2]), Y[i1]);
				for(int j = 0; j < shifts; j++) {
					for(int k = 0; k < num_rooms; k++) {
						IConstraint[] c2 = new IConstraint[4];
						c2[0] = new IsEqual(Z[i1][k], 1);
						c2[1] = new IsEqual(Z[i2][k], 1);
						c2[2] = new IsEqual(T[i1][j], 1);
						c2[3] = new IsEqual(T[i2][j], 1);
						S.post(new Implicate(new AND(c2), new OR(c1)));
					}
				}
			}
		}
		
		// Neu hai mon hoc co cung giao vien thi khong duoc hoc cung mot thoi diem
		for(int i1 = 0; i1 < num_classes-1; i1++) {
			for(int i2 = i1+1; i2 < num_classes; i2++) {
				if (class_teacher[i1] == class_teacher[i2]) {
					IConstraint[] c1 = new IConstraint[2];
					c1[0] = new LessOrEqual(new FuncPlus(Y[i1], class_units[i1]), Y[i2]);
					c1[1] = new LessOrEqual(new FuncPlus(Y[i2], class_units[i2]), Y[i1]);
					for(int j = 0; j < shifts; j++) {
						for(int k1 = 0; k1 < num_rooms; k1++) {
							for(int k2 = 0; k2 < num_rooms; k2++) {
								IConstraint[] c2 = new IConstraint[4];
								c2[0] = new IsEqual(Z[i1][k1], 1);
								c2[1] = new IsEqual(Z[i2][k2], 1);
								c2[2] = new IsEqual(T[i1][j], 1);
								c2[3] = new IsEqual(T[i2][j], 1);
								S.post(new Implicate(new AND(c2), new OR(c1)));
							}
						}
					}
				}
			}
		}
		mgr.close();
	}
	
	public void printSol() {
		for (int j = 0; j < shifts; j++) {
			System.out.println("Shift " + j);
			for (int i = 0; i < num_classes; i++) {
				for (int k = 0; k < num_rooms; k++) {
					if (Z[i][k].getValue() == 1) {
						if (T[i][j].getValue() == 1) {
							int start_unit = Y[i].getValue();
							int end_unit = start_unit + class_units[i] - 1;
							System.out.println("Class " + i + " in Room " + k +
									" with teacher " + class_teacher[i] + 
									", start at " + start_unit + " end at " + end_unit +
									", student in class " + class_student[i] +
									", room's cap " + room_capacity[k]);
						}
					}	
				}
			}
		}
		System.out.println();
	}
	
	public boolean search() {
		boolean hasSolution;
		System.out.println(S.getVariables());
		TabuSearch searcher = new TabuSearch(S);
		hasSolution = searcher.search_in_limit_time(1000, 4, 100, WAIT_TIME);
		time_end = System.currentTimeMillis();
		return hasSolution;
	}


	// Read data from text file ===================================================================
	private final static String FILE_URL = "/home/thangnn/Documents/HUST/Constraint-based Local Search/Project/OpenCBLS/data/17_3_5.txt";

	public static int[] parseAllLine(String a) {
		String[] integerStrings = a.split(" ");
		int[] integers = new int[integerStrings.length];
		for (int i = 0; i < integers.length; i++){
			integers[i] = Integer.parseInt(integerStrings[i]);
		}
		return integers;
	}

	public static int parseFirstNum(String a) {
		String[] integerStrings = a.split(" ");
		int integers = Integer.parseInt(integerStrings[0]);
		return integers;
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		BufferedReader br = new BufferedReader(new FileReader(FILE_URL));
		project app = new project();

		String textInALine;
		int line = 0;
		while ((textInALine = br.readLine()) != null) {
			if (line == 2) {
				app.num_rooms = parseFirstNum(textInALine);
			} else if (line == 3) {
				app.num_classes = parseFirstNum(textInALine);
			} else if (line == 5) {
				app.room_capacity = parseAllLine(textInALine);
			} else if (line == 6) {
				app.class_student = parseAllLine((textInALine));
			} else if (line == 7) {
				app.class_units = parseAllLine(textInALine);
			} else if (line == 8) {
				app.class_teacher = parseAllLine(textInALine);
			}
			line += 1;
		}

		app.stateModel();
		boolean hasSolution;
		hasSolution = app.search();
		if (hasSolution) {
//			app.printSol();
			System.out.println("Have solution");
		}
		System.out.println(app.time_end - app.time_start);
	}
}
