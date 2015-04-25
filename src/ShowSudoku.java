import java.util.ArrayList;

import javax.swing.*;

public class ShowSudoku {
	public static void main(String[] arg){
		ISudokuSolver s = new SudokuSolver();
		s.setup(3);
		
		int[][] ts = new int[9][9];
		ts[3][0] = 9;
		ts[4][0] = 1;
		ts[7][0] = 2;
		ts[8][0] = 8;
		
		ts[4][1] = 8;
		ts[7][1] = 3;
		
		ts[1][2] = 3;
		ts[5][2] = 7;
		
		ts[1][3] = 2;
		ts[2][3] = 3;
		ts[5][3] = 6;
		ts[6][3] = 4;
		
		ts[0][4] = 4;
		ts[8][4] = 6;
		
		ts[2][5] = 5;
		ts[3][5] = 3;
		ts[6][5] = 2;
		ts[7][5] = 8;
		
		ts[3][6] = 1;
		ts[7][6] = 9;
		
		ts[1][7] = 7;
		ts[4][7] = 3;
		
		ts[0][8] = 3;
		ts[1][8] = 4;
		ts[4][8] = 6;
		ts[5][8] = 5;
		
		ArrayList<Integer> asn = new ArrayList<Integer>();
		asn = s.GetAssignment(ts);
		
		System.out.println(asn.toString());
		
		s.readInPuzzle(ts);

		SudokuGUI g = new SudokuGUI(s);

		// Setup of the frame containing the puzzle
		JFrame f = new JFrame();
		f.setSize(1000,1000);
		f.setTitle("Sudoku Solver");
		f.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
		f.getContentPane().add(g);    
		f.setVisible(true);
	}
}

