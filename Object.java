package uebung;

public class Object {
	// Object Types par -> parameter in fp section
	public static final int sst_class = 0, sst_proc = 1, sst_var = 2;
	
	String name;
	int objClass;
	boolean init = false;
	
	boolean fin; // Is Variable final -> can be changed? or not
	Node finalstartNode; // is Variable final it can have an AST
	
	// bool or int
	long intValue = 0; String val;
	
	// Functions
	int parameterLength;
	String resultType;
	Node func;
		
	// class
	Symboltable finalVar; // final variables
	Symboltable methodDef; // methods
	
	// class and function
	Symboltable varDef; // variables
	
	// only functions
	Symboltable parameter;
	
	Object next;
	
	Symboltable nextSymboltable;
	
	private int line;
	private static String[] occupiedNames = {"public", "private", "int", "class", "final", "void",
										"if", "else", "while", "return", "null"};
	
	public Object() {}
	
	public Object(String name, int type, String val, boolean fin, int index) {
		// create Object of type normal int
		if (type != Object.sst_var) {
			this.Error("No Variable!, Object declaration!");
		}
		this.name = name;
		this.objClass = type;
		
		this.fin = fin;
		if (this.fin) {
			// final vars have to be initialized
			this.init = true;
			
			this.finalstartNode = new Node(Node.ASSIGN, index);
			this.finalstartNode.left = new Node(Node.VAR, index+1);
			this.finalstartNode.left.varName = name;
			this.finalstartNode.right = new Node(Node.CONST, index+2);
			this.finalstartNode.right.varVal = 0;
		}
		else {
			this.finalstartNode = new Node(Node.VAR, index);
			this.finalstartNode.varName = name;
		}
	}
	
	public Object(String name, int type) {
		// create parameter for fp Sections
		this.name = name;
		this.objClass = type;
		if (type == Object.sst_class) {
			this.objClass = type;
			this.finalVar = new Symboltable();
			this.varDef = new Symboltable();
			this.methodDef = new Symboltable();
		}
	}
	
	public Object(String name, int type, String t, Symboltable encl, Node start) {
		// Procedure
		this.name = name;
		this.objClass = Object.sst_proc;
		this.resultType = t;
		this.varDef = new Symboltable(encl);
		this.func = start;
		
		this.parameter = new Symboltable();
	}
	
	public void setParameterLength(int length) {
		this.parameterLength = length;
	}
	
	public int getLine() {
		return this.line;
	}
	
	public void setLine(int line) {
		this.line = line;
	}
	
	public void Error(String str) {
		System.out.println("ERROR!");
		System.out.println(str);
		System.exit(1);
	}
	
	public void printClass() {
		finalVar.printSymboltable();
		varDef.printSymboltable();
		methodDef.printSymboltable();
	}
	
	public void printFinalVar() {
		Object pointer = this;
		System.out.println("************");
		System.out.println("START!!! final var in printFinalVar - Object");
		pointer = pointer.next;
		while (pointer.next != null) {
			pointer.finalstartNode.traverse();
			pointer = pointer.next;
		}
		pointer.finalstartNode.traverse();
		System.out.println("************");
	}
	
	public void printVar() {
		System.out.println("************");
		Object pointer = this;
		System.out.println("START!!! VAR Objects in printVar - Object");
		pointer = pointer.next;
		while (pointer.next != null) {
			System.out.println("NAME: " + pointer.name);
			pointer = pointer.next;
		}
		System.out.println("NAME: " + pointer.name);
		System.out.println("************");
	}
	
	public void printProc() {
		System.out.println("************");
		Object pointer = this;
		System.out.println("START!!! Procedure Objects in printProc - Object");
		pointer = pointer.next;
		while (pointer.next != null) {
			System.out.println("NAME: " + pointer.name);
			System.out.println("Start of variable Definitions!");
			pointer.varDef.printSymboltable();
			System.out.println("End of variable Definitions!");
			pointer = pointer.next;
		}
		System.out.println("NAME: " + pointer.name);
		System.out.println("************");
	}
	
	public void printProcNode() {
		// print ast of Procedure
		System.out.println("AST of " + this.name + " in printProcNode - Object");
		System.out.println("************* PARAMETER: ********");
		System.out.println("PARAMETER LENGTH: " + this.parameterLength);
		this.func.left.traverse();
		System.out.println("************* FUNCTION: ********");
		this.func.right.traverse();
	}
	
	public static void checkName(String local_name, int local_line) {
		/*
		 * check given name if its public, private, int, etc
		 */
		for (int i = 0; i < occupiedNames.length; i++) {
			if (local_name.equals(occupiedNames[i])) {
				System.out.println("Name is ambigious! Please choose an other variablename");
				System.out.println("Line: " + local_line);
				System.out.println("******************* End ****************");
				System.exit(1);
			}
		}
	}
}
