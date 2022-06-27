package uebung;

public class Node {
	public static final int VAR = 0, CONST = 1, ASSIGN = 2, IFELSE = 3, 
			IF = 4, ELSE = 5, WHILE = 6, RETURN = 7, CLASS = 8, 
			BINOP = 9, COMPOP = 10, PROC = 11, PARAMETER = 12,
			PROGRAM = 13, IF_COND = 14, IF_DO = 15, WHILE_COND = 16,
			WHILE_DO = 17, TEMP = 18, VOID = 19, INT = 20, COMMENT = 21,
			PROC_CALL = 22;
	
	//public final int PROGRAM = 0, VAR = 1, CONST = 2, ASSIGN = 3, BINCOMP = 4, BINOP = 
	Node left, right;
	// Verweis auf zusätzliche Sprachkonstrukte
	Node link, prev;
	Node parent;
		
	// Art des Konstruktes (BINOP, BINCOMP, ASSIGN, VAR, CONST, PROGRAM)
	int sst_class;
	String string_class;
	
	// FINAL
	int count;
	
	// BINOP or BINCOMP
	String operation;
	
	// VAR or CONST
	String varName;
	int varVal;
	
	// if in brackets
	boolean first = false;
	
	private int line;
	int uniqueIndex;
	
	// used for procedure calls
	int parameterLength;
	int returnValue;
	
	public Node(int index) { this.uniqueIndex = index; }
	
	public Node(int cl, int index) {
		this.setClass(cl);
		this.uniqueIndex = index;
	}
	
	public Node(int cl, String comment, int index) {
		// create comment node
		this.setClass(cl);
		this.varName = comment;
		this.uniqueIndex = index;
	}
	
	public Node(Node t, int index) {
		this.varVal = t.varVal;
		this.operation = t.operation;
		this.varName = t.varName;
		this.left = t.left;
		this.right = t.right;
		this.uniqueIndex = index;
		this.line = t.getLine();
		
		this.setClass(t.sst_class);
	}
	
	public void setClass(int type) {
		this.sst_class = type;
		this.setClass();
	}
	
	private void setClass() {
		switch (this.sst_class) {
		case 0: this.string_class = "VAR"; break;
		case 1: this.string_class = "CONST"; break;
		case 2: this.string_class = "ASSIGN"; break;
		case 3: this.string_class = "IFELSE"; break;
		case 4: this.string_class = "IF"; break;
		case 5: this.string_class = "ELSE"; break;
		case 6: this.string_class = "WHILE"; break;
		case 7: this.string_class = "RETURN"; break;
		case 8: this.string_class = "CLASS"; break;
		case 9: this.string_class = "BINOP"; break;
		case 10: this.string_class = "COMPOP"; break;
		case 11: this.string_class = "PROC"; break;
		case 12: this.string_class = "PARAMETER"; break;
		case 13: this.string_class = "PROGRAM"; break;
		case 14: this.string_class = "IF COND"; break;
		case 15: this.string_class = "IF DO"; break;
		case 16: this.string_class = "WHILE COND"; break;
		case 17: this.string_class = "WHILE DO"; break;
		case 18: this.string_class = "TEMP"; break;
		case 19: this.string_class = "VOID"; break;
		case 20: this.string_class = "INT"; break;
		case 21: this.string_class = "COMMENT"; break;
		case 22: this.string_class = "PROC CALL"; break;
		default: System.out.println("UNKNOWN TYPE in Node"); break;
		}
	}
	
	public int getLine() {
		return this.line;
	}
	
	public void setLine(int line) {
		this.line = line;
	}
	
	public void setLeft(Node left) {
		this.left = left;
		this.left.parent = this;
	}
	
	public void setRight(Node right) {
		this.right = right;
		right.parent = this;
	}
	
	public void setLink(Node link) {
		this.link = link;
		this.link.prev = this;
	}
	
	public void traverse() {
		System.out.println("-- CLASS: " + string_class + " --");
		System.out.println("LINE: " + this.getLine());
		if (sst_class == Node.VAR) {
			System.out.println("Var Name!   " + this.varName);
		}
		else if (sst_class == Node.PROC) {
			System.out.println("PROCEDURE Name!   " + this.varName);
		}
		else if (sst_class == Node.CONST) {
			System.out.println("CONSTANT: " + this.varVal);
		}
		else if (sst_class == Node.BINOP) {
			System.out.println("Binäre Operation!   " + this.operation);
		}
		else if (sst_class == Node.COMPOP) {
			System.out.println("Compare Operation!   " + this.operation);
		}
		if (this.left != null) {
			System.out.println("LEFT OF " + string_class);
			this.left.traverse();
		}
		if (this.right != null) {
			System.out.println("RIGHT OF " + string_class);
			this.right.traverse();
		}
		if (this.link != null) {
			System.out.println("LINK OF " + string_class);
			this.link.traverse();
		}
	}
	
	public void copyNode(Node c) {
		this.setClass(c.sst_class);
		
		this.operation = c.operation;
		this.varVal = c.varVal;
		
		this.left = c.left;
		this.right = c.right;
		
		this.varName = c.varName;
		this.varVal = c.varVal;
		
		this.line = c.getLine();
	}
	
	public void assignLeftRight(int left, int right, int index) {
		Node l = new Node(left, index);
		Node r = new Node(right, index+1);
		
		this.setLeft(l);
		this.setRight(r);
	}
	
	public void printNode() {
		System.out.println("********************");
		System.out.println("Start printing Node!");
		System.out.println(this.varName);
		System.out.println(this.sst_class);
		if (sst_class == Node.BINOP) {
			System.out.println(this.operation);
		}
		System.out.println("********************");
	}
	
	public String getNodeName() {
		String name = "\"";
		if (this.sst_class == Node.BINOP || this.sst_class == Node.COMPOP) {
			name += this.operation;
		}
		else if (this.sst_class == Node.VAR || this.sst_class == Node.PROC) {
			name += this.varName;
		}
		else if (this.sst_class == Node.CONST) {
			name += this.varVal;
		}
		else if (this.sst_class == Node.COMMENT) {
			name += this.varName;
		}
		else if (this.sst_class == Node.PROC_CALL) {
			name += this.varName;
		}
		else {
			name += this.string_class;
		}
		return name + "\"";
	}
}
