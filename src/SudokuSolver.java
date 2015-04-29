import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class SudokuSolver implements ISudokuSolver {

	int[][] puzzle;
	int size;
	ArrayList<ArrayList<Integer>> D = new ArrayList<ArrayList<Integer>>();
	
	public int[][] getPuzzle() {
		return puzzle;
	}

	public void setValue(int col, int row, int value) {
		puzzle[col][row] = value;
	}

	public void setup(int size1) {
		size = size1;
		puzzle = new int[size*size][size*size];
		D = new ArrayList<ArrayList<Integer>>(size*size*size*size);
		
		// Initialize and fill all the domains with values from 1-9.
		InitializeAndFillDomains();
		
	}


	public boolean solve() {
		ArrayList<Integer> asn = GetAssignment(puzzle);
		
		// Make sure already set variables only has the value in their domain.
		SetInitialDomains(asn);
		
		//INITIAL_FC
		// Check if the already set values are arc consistent.
		if (!INITIAL_FC(asn)) {
			// The sudoku could not be solved, return false.
			System.out.println("Could not complete initial FC. The sudoku violates the constraints");
			return false;
		} else {
			// The constraints hold and we can proceed.
			System.out.println("Completed initial FC");
		}
		
		// Initialize the forward checking method.
		ArrayList<Integer> solution = FC(asn); 

		// Set the puzzle to be displayed to the solution found by using the GetPuzzle() method.
		this.puzzle = GetPuzzle(solution);
		
		// Return true to indicate that a solution has been found.
		return true;
	}

	public void readInPuzzle(int[][] p) {
		puzzle = p;
	}
	
	
		//---------------------------------------------------------------------------------
		//YOUR TASK:  Implement FC(asn)
		//---------------------------------------------------------------------------------
		public ArrayList FC(ArrayList<Integer> asn) {
			
			// Check if the sudoku is already filled out and an answer thus found.
			if (ContainsNoZero(asn)) {
				return asn;
			}
			
			// Finding the index of the first 0.
			int X = FindFirstZero(asn);
			
			// Clone The domain.
			ArrayList<ArrayList<Integer>> oldD = CloneDomain(D);
			
			// Go throught the values in the domain for the variable.
			for (int V : D.get(X).toArray(new Integer[D.get(X).size()])) {
				
				// Check if there is arc consistency with the chosen variable.
				if (AC_FC(X, V)) {
					
					// Set the variable to the value and move forward by calling the FC method.
					asn.set(X, V);
					ArrayList<Integer> R = FC(asn);
					if (R != null) {
						return R;
					}
					// If the returned value is null, set the value to 0 as it didn't work.
					asn.set(X,0);	
				}
				// Role back the domains to their previous state.
				D = CloneDomain(oldD);
			}
			// If none of the values are arc consistent go one stpe back by returning null.
			return null; //failure
		}

	

		
		//---------------------------------------------------------------------------------
		// CODE SUPPORT FOR IMPLEMENTING FC(asn)
		//
		// It is possible to implement FC(asn) by using only AC_FC function from below.
		// 
		// If you have time, I strongly reccomend that you implement AC_FC and REVISE from scratch
		// using only implementation of CONSISTENT algorithm and general utility functions. In my opinion
		// by doing this, you will gain much more from this exercise.
		//
		//---------------------------------------------------------------------------------
		
		
	
		//------------------------------------------------------------------
		//				AC_FC
		//
		// Implementation of acr-consistency for forward-checking AC-FC(cv).
		// This is a key component of FC algorithm, and the only function you need to 
		// use in your FC(asn) implementation
		//------------------------------------------------------------------
		public boolean AC_FC(Integer X, Integer V){
			//Reduce domain Dx
			D.get(X).clear();
			D.get(X).add(V);
			
			//Put in Q all relevant Y where Y>X
			ArrayList<Integer> Q = new ArrayList<Integer>(); //list of all relevant Y
			int col = GetColumn(X);
			int row = GetRow(X);
			int cell_x = row / size;
			int cell_y = col / size;
			
			//all variables in the same column
			for (int i=0; i<size*size; i++){
				if (GetVariable(i,col) > X) {
					Q.add(GetVariable(i,col));
				}
			}
			//all variables in the same row
			for (int j=0; j<size*size; j++){
				if (GetVariable(row,j) > X) {
					Q.add(GetVariable(row,j));
				}
			}
			//all variables in the same size*size box
			for (int i=cell_x*size; i<=cell_x*size + 2; i++) {
				for (int j=cell_y*size; j<=cell_y*size + 2; j++){
					if (GetVariable(i,j) > X) {
						Q.add(GetVariable(i,j));
					}
				}
			}
		
			//REVISE(Y,X)
			boolean consistent = true;
			while (!Q.isEmpty() && consistent){
				Integer Y = (Integer) Q.remove(0);
				if (REVISE(Y,X)) {
					consistent = !D.get(Y).isEmpty();
				}
			}
			return consistent;
		}	
		
		
		//------------------------------------------------------------------
		//				REVISE 
		//------------------------------------------------------------------
		public boolean REVISE(int Xi, int Xj){
			Integer zero = new Integer(0);
			
			assert(Xi >= 0 && Xj >=0);
			assert(Xi < size*size*size*size && Xj <size*size*size*size);
			assert(Xi != Xj);
			
			boolean DELETED = false;

			
			ArrayList<Integer> Di = D.get(Xi);
			ArrayList<Integer> Dj = D.get(Xj);	
			
			for (int i=0; i<Di.size(); i++){
				Integer vi = (Integer) Di.get(i);
				ArrayList<Integer> xiEqVal = new ArrayList<Integer>(size*size*size*size);	
				for (int var=0; var<size*size*size*size; var++){
					xiEqVal.add(var,zero);				
				}

				xiEqVal.set(Xi,vi);
				
				boolean hasSupport = false;	
				for (int j=0; j<Dj.size(); j++){
					Integer vj = (Integer) Dj.get(j);
					if (CONSISTENT(xiEqVal, Xj, vj)) {
						hasSupport = true;
						break;
					}
				}
				
				if (hasSupport == false) {
					Di.remove((Integer) vi);
					DELETED = true;
				}
				
			}
			
			return DELETED;
		}
				
		

		
		//------------------------------------------------------------------
		//CONSISTENT: 
		//
		//Given a partial assignment "asn"  checks whether its extension with 
		//variable = val is consistent with Sudoku rules, i.e. whether it violates
		//any of constraints whose all variables in the scope have been assigned. 
		//This implicitly encodes all constraints describing Sudoku.
		//
		//Before it returns, it undoes the temporary assignment variable=val
		//It can be used as a building block for REVISE and AC-FC
		//
		//NOTE: the procedure assumes that all assigned values are in the range 
		// 		{0,..,9}. 
		//-------------------------------------------------------------------
		public boolean CONSISTENT(ArrayList<Integer> asn, Integer variable, Integer val) {
			Integer v1,v2;
			
			//variable to be assigned must be clear
			assert(asn.get(variable) == 0);
			asn.set(variable,val);

			//alldiff(col[i])
		 	for (int i=0; i<size*size; i++) {
		 		for (int j=0; j<size*size; j++) {
		 			for (int k=0; k<size*size; k++) {
			 			if (k != j) {
			 				v1 = (Integer) asn.get(GetVariable(i,j));
			 				v2 = (Integer) asn.get(GetVariable(i,k));
				 			if (v1 != 0 && v2 != 0 && v1.compareTo(v2) == 0) {
				 				asn.set(variable,0);
				 				return false;
				 			}
				 		}
		 			}
		 		}
		 	}
		

		 	
		 	//alldiff(row[j])
		 	for (int j=0; j<size*size; j++) {
		 		for (int i=0; i<size*size; i++) {
		 			for (int k=0; k<size*size; k++) {
			 			if (k != i) {
			 				v1 = (Integer) asn.get(GetVariable(i,j));
			 				v2 = (Integer) asn.get(GetVariable(k,j));
				 			if (v1 != 0 && v2 != 0 && v1.compareTo(v2) == 0) {
				 				asn.set(variable,0);			 				
				 				return false;
				 			}
			 			}
		 			}
		 		}
		 	}
		 	

		 	//alldiff(block[size*i,size*j])
		 	for (int i=0; i<size; i++) {
		 		for (int j=0; j<size; j++) {
		 			for (int i1 = 0; i1<size; i1++) {
		 				for (int j1=0; j1<size; j1++) {
		 					int var1 = GetVariable(size*i + i1, size*j + j1);
		 		 			for (int i2 = 0; i2<size; i2++) {
		 		 				for (int j2=0; j2<size; j2++) {
		 		 					int var2 = GetVariable(size*i+i2, size*j + j2);
		 		 					if (var1 != var2) {
		 				 				v1 = (Integer) asn.get(var1);
		 				 				v2 = (Integer) asn.get(var2);
		 		 			 			if (v1 != 0 && v2 != 0 && v1.compareTo(v2) == 0) {
		 					 				asn.set(variable,0);	 		 			 				
		 					 				return false;
		 					 			}
		 		 					}
		 		 				}
		 		 			}
	 
		 				}
		 			}
		 		}
		 	}

			asn.set(variable,0);
			return true;
		}	
		
		

	
		//------------------------------------------------------------------
		//						INITIAL_FC
		//------------------------------------------------------------------
		public boolean INITIAL_FC(ArrayList<Integer> anAssignment) {
			//Enforces consistency between unassigned variables and all 
			//initially assigned values; 
			for (int i=0; i<anAssignment.size(); i++){
				Integer V = (Integer) anAssignment.get(i);
				if (V != 0){
					ArrayList<Integer> Q = GetRelevantVariables(i);
					boolean consistent = true;
					while (!Q.isEmpty() && consistent){
						Integer Y = (Integer) Q.remove(0);
						if (REVISE(Y,i)) {
							consistent = !D.get(Y).isEmpty();
						}
					}	
					if (!consistent) return false;
				}
			}
			
			return true;
		}
		
		
	
		
		//------------------------------------------------------------------
		//						GetRelevantVariables
		//------------------------------------------------------------------
		public ArrayList<Integer> GetRelevantVariables(Integer X){
			//Returns all variables that are interdependent of X, i.e. 
			//all variables involved in a binary constraint with X
			ArrayList<Integer> Q = new ArrayList<Integer>(); //list of all relevant Y
			int col = GetColumn(X);
			int row = GetRow(X);
			int cell_x = row / size;
			int cell_y = col / size;
			
			//all variables in the same column
			for (int i=0; i<size*size; i++){
				if (GetVariable(i,col) != X) {
					Q.add(GetVariable(i,col));
				}
			}
			//all variables in the same row
			for (int j=0; j<size*size; j++){
				if (GetVariable(row,j) != X) {
					Q.add(GetVariable(row,j));
				}
			}
			//all variables in the same size*size cell
			for (int i=cell_x*size; i<=cell_x*size + 2; i++) {
				for (int j=cell_y*size; j<=cell_y*size + 2; j++){
					if (GetVariable(i,j) != X) {
						Q.add(GetVariable(i,j));
					}
				}
			}	
			
			return Q;
		}
		
		



		//------------------------------------------------------------------
		// Functions translating between the puzzle and an assignment
		//-------------------------------------------------------------------
		public ArrayList<Integer> GetAssignment(int[][] p) {
			ArrayList<Integer> asn = new ArrayList<Integer>();
			for (int i=0; i<size*size; i++) {
				for (int j=0; j<size*size; j++) {
					asn.add(GetVariable(i,j), new Integer(p[i][j]));
					if (p[i][j] != 0){
							//restrict domain
							D.get(GetVariable(i,j)).clear();
							D.get(GetVariable(i,j)).add(new Integer(p[i][j]));
						}
				}
			}
			return asn;
		}	
		
	
		public int[][] GetPuzzle(ArrayList asn) {
			int[][] p = new int[size*size][size*size];
			for (int i=0; i<size*size; i++) {
				for (int j=0; j<size*size; j++) {
					Integer val = (Integer) asn.get(GetVariable(i,j));
					p[i][j] = val.intValue();
				}
			}
			return p;
		}

	
		//------------------------------------------------------------------
		//Utility functions
		//-------------------------------------------------------------------
		public int GetVariable(int i, int j){
			assert(i<size*size && j<size*size);
			assert(i>=0 && j>=0);		
			return (i*size*size + j);	
		}	
		
		
		public int GetRow(int X){
			return (X / (size*size)); 	
		}	
		
		public int GetColumn(int X){
			return X - ((X / (size*size))*size*size);	
		}	
		
		public void InitializeDomains() {
			for (int i = 0; i < ((size*size)*(size*size)); i++) {
				D.add(new ArrayList<Integer>());
			}
		}
		
		// Initiates and sets all domains in D
		public void InitializeAndFillDomains() {
			
			// Going through all variables in D
			for (int i = 0; i < ((size*size)*(size*size)); i++) {
				
				// Create new Arraylist to be inserted into the spot in D
				ArrayList<Integer> temp = new ArrayList<Integer>();
				
				// Fill all arraylist variables with the domain from 1-9
				for (int j = 1; j < 10; j++) {
					temp.add(j);
				}
				D.add(temp);
			}
		}
		
		// Check it there exist a zero in the solution
		public boolean ContainsNoZero(ArrayList<Integer> asn) {
			return FindFirstZero(asn) == -1;
		}
		
		// Find the first an dreturn the index of the first zero
		public int FindFirstZero(ArrayList<Integer> asn) {			
			return asn.indexOf(0);
		}
		
		// Clone the the Arraylist of domains to be able to role back.
		public ArrayList<ArrayList<Integer>> CloneDomain(ArrayList<ArrayList<Integer>> D) {
			
			// Create new domain Arraylist
			ArrayList<ArrayList<Integer>> clone = new ArrayList<ArrayList<Integer>>();
			
			// Loop through all variables in D and clone the content to the new Domain variable.
			for (int i = 0 ; i < D.size() ; i++) {
				ArrayList<Integer> temp = new ArrayList<Integer>();
				for (int j : D.get(i)) {
					temp.add(j);
				}
				clone.add(temp);
			}
			return clone;
		}
		
		// Go through all the domains and set the ones with already set numbers.
		public void SetInitialDomains(ArrayList<Integer> asn) {
			for (int i = 0 ; i < asn.size() ; i++) {
				int V = asn.get(i);
				
				// If the value is already set (not a 0) delete everything in the domain but the value.
				if (V != 0) {
					ArrayList<Integer> X = D.get(i);
					X.clear();
					X.add(V);
				}
			}
		}
}
