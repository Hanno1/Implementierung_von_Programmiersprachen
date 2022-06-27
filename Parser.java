package uebung;

public class Parser {
	// Scanner to get next chars
	Scanner s;
	// main Object, has finalVars, methods, normalVars
	Object mainClass;
	
	// cur func to save time for access
	Object curFunc;
	
	// Node from which the Tree will start
	Node start;
	// Ensures all Nodes have a unique name
	int counter = 0;
	
	boolean ret = false;
	boolean inWhile = false;
	boolean inIf = false;
			
	public Parser(Scanner s) throws Exception {
		/*
		 * Constructor for Parser for a given Scanner
		 */
		this.s = s;
		this.s.getSym();
	}
	
	public void check_synt() throws Exception {
		/*
		 * Main Function, check Syntax
		 */
		this.sst_class();
		
		this.curFunc = null;
		this.checkProgram(this.start);
		System.out.println("Correct Syntax");
	}
	
	private void checkProgram(Node n) throws Exception {
		/*
		 * if method call -> check if method exists
		 * if var -> check if var exists
		 */
		// if we have an actual procedure assignement
		// we want the object
		boolean check_left_right = true;
		if (n.sst_class == Node.PROC) {
			if (this.curFunc == null) {
				this.curFunc = this.mainClass.methodDef.head.next;
			}
			else {
				this.curFunc = this.curFunc.next;
			}
		}
		else if (n.sst_class == Node.PROC_CALL) {
			this.checkFunctioncall(n);
		}
		// if Assign Node check right path returns int
		// dont check function calls for now
		// check if vars exists
		else if (n.sst_class == Node.ASSIGN) {
			this.checkAssign(n);
			check_left_right = false;
		}
		else if (n.sst_class == Node.WHILE || n.sst_class == Node.IF) {
			// check condition
			check_left_right = false;
			
			this.checkComp(n.left);
			
			if (n.right != null) {
				this.checkProgram(n.right);
			}
		}
		else if (n.sst_class == Node.RETURN) {
			if (n.right != null) {
				this.checkRightAssign(n.right, 0);
			}
		}
		if (check_left_right) {
			if (n.left != null) {
				this.checkProgram(n.left);
			}
			if (n.right != null) {
				this.checkProgram(n.right);
			}
		}
		if (n.link != null) {
			this.checkProgram(n.link);
		}
	}
	
	private void checkComp(Node n) throws Exception {
		if (n.sst_class != Node.COMPOP) {
			this.ErrorNode(n.getLine(), "Expected Comparission!");
		}
		else {
			if (!n.operation.equals("==")) {
				this.checkRightAssign(n.right, 0);
				this.checkRightAssign(n.left, 0);
			}
			else {
				if (n.left.sst_class != Node.COMPOP && n.right.sst_class == Node.COMPOP) {
					this.ErrorNode(n.getLine(), "Cant compare int to bool!");
				}
				if (n.left.sst_class == Node.COMPOP && n.right.sst_class != Node.COMPOP) {
					this.ErrorNode(n.getLine(), "Cant compare int to bool!");
				}
				if (n.left.sst_class == Node.COMPOP && n.right.sst_class == Node.COMPOP) {
					this.checkComp(n.left);
					this.checkComp(n.right);
				}
				else {
					this.checkRightAssign(n.right, 0);
					this.checkRightAssign(n.left, 0);
				}
			}
		}
	}
	
	private void checkAssign(Node n) throws Exception {
		// check left side only if (re)declaration in function
		// if its outside function we dont have to do anything
		String left = n.left.varName;
		boolean initClassvar = false;
		boolean initFuncvar = false;
		if (!(this.curFunc == null)) {
			if (mainClass.finalVar.searchObject(left, Object.sst_var)) {
				this.ErrorNode(n.getLine(), left + " allready exists and is final!");
			}
			if (mainClass.varDef.searchObject(left, Object.sst_var)) {
				// check if variable is initialized (global var
				initClassvar = true;
			}
			else if (this.curFunc.varDef.searchObject(left, Object.sst_var)) {
				initFuncvar = true;
			}
			else if (this.curFunc.parameter.searchObject(left, Object.sst_var)) {}
			else {
				this.ErrorNode(n.getLine(), left + " is not defined!");
			}
			
		}
		// check right side, also todo outside of function
		this.checkRightAssign(n.right, this.mainClass.finalVar.searchObjectIndex(left, Object.sst_var));
		if (initClassvar) {
			mainClass.varDef.setInit(left, Object.sst_var);
		}
		if (initFuncvar) {
			curFunc.varDef.setInit(left, Object.sst_var);
		}
	}
	
	private void checkRightAssign(Node n, int index) throws Exception {
		if (n.sst_class == Node.VAR) {
			// final vars -> check if only vars are used that come after the var we define
			if (this.curFunc == null) {
				int temp = this.mainClass.finalVar.searchObjectIndex(n.varName, Object.sst_var);
				if (temp >= index || temp == 0) {
					this.ErrorNode(n.getLine(), n.varName + " is not Defined!");
				}
			}
			// not final vars -> just check if var occured before
			else {
				if (!this.checkExistence(n.varName)) {
					this.ErrorNode(n.getLine(), n.varName + " is not declared or not initialized!");
				}
			}
		}
		else if (n.sst_class == Node.PROC_CALL) {
			this.checkFunctioncall(n);
			Object func = mainClass.methodDef.getFunction(n.varName, countParameters(n));
			if (func.resultType == "VOID") {
				this.ErrorNode(n.getLine(), n.varName + " returns VOID!");
			}
		}
		else if (n.sst_class == Node.COMPOP) {
			this.ErrorNode(n.getLine(), "Cant convert bool to int!");
		}
		if (n.left != null) {
			checkRightAssign(n.left, index);
		}
		if (n.right != null) {
			checkRightAssign(n.right, index);
		}
	}
	
	private boolean checkExistence(String name) {
		if (mainClass.finalVar.searchObject(name, Object.sst_var) ||
				this.curFunc.parameter.searchObject(name, Object.sst_var)) {
			return true;
		}
		else if (mainClass.varDef.searchObject(name, Object.sst_var)) {
			if (mainClass.varDef.getInit(name, Object.sst_var)) { return true; }
		}
		else if (this.curFunc.varDef.searchObject(name, Object.sst_var)) {
			if (this.curFunc.varDef.getInit(name, Object.sst_var)) { return true; }
		}
		return false;
	}
	
	private void checkFunctioncall(Node n) throws Exception {
		if (!mainClass.methodDef.searchFunction(n.varName, countParameters(n))){
			System.out.println("ERROR in Line: " + n.left.getLine());
			endError("Function with name " + n.varName + " does not exists or the call hasnt the right parameter Length");
		}
		if (countParameters(n) == 0) {
			n.left = null;
		}
	}
	
	private int countParameters(Node n) {
		/*
		 * Count Parameter of a function and returns the counter
		 */
		int counter = 0;
		
		if (n.left == null) {
			return 0;
		}
		
		if (n.left.sst_class != Node.CONST && n.left.varName == null) { counter = 0; }
		else {
			n = n.left;
			while (n.link != null) {
				counter += 1;
				n = n.link;
			}
			counter += 1;
		}
		return counter;
	}
	
	public void sst_class() throws Exception {
		if (this.s.sym == Scanner.sst_class) {
			s.getSym();
			Object.checkName(s.id, s.line_counter);
			
			// create class object
			mainClass = new Object(s.id, Object.sst_class);
			mainClass.setLine(s.line_counter);
			
			// create class node
			start = new Node(Node.CLASS, counter);
			counter += 1;
						
			start.varName = this.ident(start);
			
			this.classbody(start);
			if (this.s.end == false) { this.Error("More Code after class"); }
		}
		else {
			this.Error("Class Declaration ist falsch!");
		}
	}
	
	public void classbody(Node n) throws Exception {
		if (this.s.sym == Scanner.lbrace) {
			s.getSym();
			this.declarations(n);
			if (this.s.sym == Scanner.rbrace) {
				s.getSym();
			}
			else {
				this.Error("Fehlende Klammer in Classbody '}'");
			}
		}
		else {
			this.Error("Fehlende Klammer in Classbody '{'");
		}
	}
	
	public void declarations(Node n) throws Exception {
		// final Variable declaration
		Node finals = new Node(Node.COMMENT, "Class Vars:", counter);
		boolean first = true;
		counter += 1;
				
		n.left = finals;
		Object c;
		while (this.s.sym == Scanner.sst_final) {
			// Create Node just in case
			// Just final Vars
			s.getSym();
			this.type();
			
			// create final int Object
			Node t = new Node(Node.ASSIGN, counter);
			
			counter += 1;
			t.assignLeftRight(Node.VAR, Node.VAR, counter);
			if (first) {
				n.left = t;
				first = false;
				finals = t;
			}
			else {
				finals.link = t;
				finals = finals.link;
			}
			
			counter += 2;
			String id = this.ident(t.left);
			Object.checkName(id, s.line_counter);
						
			t.left.varName = id;
			
			if (this.s.sym == Scanner.equal) {
				s.getSym();
				// declaration Object var
				c = new Object(id, Object.sst_var, this.expression(t.right), true, counter);
				counter += 3;
				
				c.setLine(t.left.getLine());
				c.finalstartNode = t;
				mainClass.finalVar.insert(c);
				
				if (this.s.sym == Scanner.sst_semicolon) {
					s.getSym();
				}
				else {
					this.ErrorNode(c.getLine(), "Kein Semikolon nach Expression!, final");
				}
			}
			else {
				this.ErrorNode(t.left.getLine(), "Kein Gleichheitszeichen nach Deklaration!");
			}
		}
		// integer declarations
		while (this.s.sym == Scanner.sst_int) {
			s.getSym();
			String id = this.ident(n);
			Object.checkName(id, s.line_counter);
			
			// dont need nodes
			// create Object int -> not final
			c = new Object(id, Object.sst_var, "0", false, counter);
			counter += 3;
			
			c.finalstartNode.setLine(n.getLine());
			c.setLine(n.getLine());
			if (first) {
				n.left = c.finalstartNode;
				first = false;
				finals = c.finalstartNode;
			}
			else {
				finals.link = c.finalstartNode;
				c.finalstartNode.setLine(n.getLine());
				finals = finals.link;
			}
			
			this.mainClass.finalVar.searchObjectError(c.name, Object.sst_var, c.getLine());
			this.mainClass.varDef.insert(c);
			
			if (this.s.sym == Scanner.sst_semicolon) {
				s.getSym();
			}
			else {
				this.ErrorNode(n.getLine(), "Kein Semikolon nach Expression!, int");
			}
		}
		// Method Declarations
		while (this.s.sym == Scanner.sst_public) {
			this.ret = false;
			Node n2 = new Node(Node.PROC, counter);
			counter += 1;
						
			n.link = n2;
			n = n2;

			s.getSym();
			this.method_declaration(n);
			if (this.curFunc.resultType.equals("INT") && this.ret == false) {
				this.Error("Missing Return in Function " + this.curFunc.name);
			}
		}
	}
	
	public void method_declaration(Node n) throws Exception {		
		n.setClass(Node.PROC);
		n.setLeft(new Node(Node.PARAMETER, counter));
		counter += 1;
		n.setRight(new Node(Node.PROGRAM, counter));
		counter += 1;
		
		this.method_head(n.left);
		this.method_body(n.right);
	}
	
	public void method_head(Node n) throws Exception {
		// Since we checked already for public we go directly to the rest
		String t = this.method_type(n);
		String id = this.ident(n);
		Object.checkName(id, s.line_counter);
		
		Object c;
		// method object 
		c = new Object(id, Object.sst_proc, t, this.mainClass.methodDef, n.parent);
		c.setLine(n.getLine());
		curFunc = c;

		n.parent.varName = id;
		Symboltable para = this.formal_parameters(n);
		c.setParameterLength(para.length);
		c.parameter = para;
		n.parent.parameterLength = para.length;
		if (curFunc.resultType == "INT") { n.parent.returnValue = 1; }
		
		this.mainClass.methodDef.insert(c);
	}
	
	public String method_type(Node n) throws Exception {
		String ret = "";
		if (this.s.sym == Scanner.sst_void) {
			s.getSym();
			ret = "VOID";
		}
		else if (this.s.sym == Scanner.sst_int) {
			this.type();
			ret = "INT";
		}
		else {
			this.Error("Wrong Declaration of methode Type!");
		}
		return ret;
	}
	
	public Symboltable formal_parameters(Node n) throws Exception {
		Symboltable fp = new Symboltable();

		if (this.s.sym == Scanner.lparen) {
			s.getSym();
			if (this.s.sym == Scanner.sst_int) {
				Node n2 = new Node(Node.VAR, counter);
				counter += 1;
				
				n.setLink(n2);
				n = n.link;
				
				fp.insert(this.fp_section(n));
				while (this.s.sym == Scanner.sst_comma) {
					n2 = new Node(Node.VAR, counter);
					counter += 1;
					
					n.setLink(n2);
					n = n.link;					
					s.getSym();
					fp.insert(this.fp_section(n));
				}
			}
			if (this.s.sym == Scanner.rparen) {
				s.getSym();
			}
			else {
				this.Error("Klammer zu vergessen bei formal Parameters oder fehlender Type!");
			}
		}
		else {
			this.Error("Klammer auf vergessen bei formal Parameters!");
		}
		return fp;
	}
	
	public Object fp_section(Node n) throws Exception {				
		this.type();
		String id = this.ident(n);
		
		Object.checkName(id, s.line_counter);
		
		Object k = new Object(id, Object.sst_var);
		k.setLine(n.getLine());
		n.varName = id;
		
		this.mainClass.finalVar.searchObjectError(id, Object.sst_var, k.getLine());
		this.mainClass.varDef.searchObjectError(id, Object.sst_var, k.getLine());
		
		return k;
	}
	
	public void method_body(Node n) throws Exception {
		if (this.s.sym == Scanner.lbrace) {
			s.getSym();
			
			boolean exists = false;
			while(this.s.sym == Scanner.sst_int) {
				exists = true;				
				this.local_declaration(n);
				n = n.link;
			}
			
			if (exists) { 
				n.link = new Node(counter); 
				n = n.link; 
				counter++; 
			}
			this.statement_sequence(n);
			if (this.s.sym == Scanner.rbrace) {
				s.getSym();
			}
			else {
				this.Error("Klammer '}' vergessen bei method Body!");
			}
		}
		else {
			this.Error("Klammer '{' vergessen bei method Body!");
		}
	}
	
	public void local_declaration(Node n) throws Exception {
		this.type();
		String id = this.ident(n);
		
		Object.checkName(id, s.line_counter);
		
		// create local Object
		Object j = new Object(id, Object.sst_var, "0", false, counter);
		
		n.link = j.finalstartNode;
		n.link.setLine(n.getLine());
		
		counter += 3;
		
		j.setLine(n.getLine());
		// same name as final vars
		this.mainClass.finalVar.searchObjectError(id, Object.sst_var, j.getLine());
		// same name as global vars
		this.mainClass.varDef.searchObjectError(id, Object.sst_var, j.getLine());
		// same name as function parameters
		this.curFunc.parameter.searchObjectError(id, Object.sst_var, j.getLine());
		
		this.curFunc.varDef.insert(j);
		if (this.s.sym == Scanner.sst_semicolon) {
			s.getSym();
		}
		else {
			this.ErrorNode(j.getLine(), "Semikolon vergessen bei Local Declaration!");
		}
	}
	
	public void statement_sequence(Node n) throws Exception {
		this.statement(n);
		while (this.s.sym == Scanner.ident || this.s.sym == Scanner.sst_if || 
				this.s.sym == Scanner.sst_while || this.s.sym == Scanner.sst_ret) {			
			Node n2 = new Node(Node.TEMP, counter);
			counter += 1;
			
			n.setLink(n2);
			n = n.link;
			
			this.statement(n);
		}
	}
	
	public void statement(Node n) throws Exception {
		if (this.s.sym == Scanner.ident) {
			String id = this.s.id;
			n.setLine(s.line_counter);
			
			s.getSym();
			if (this.s.sym == Scanner.equal) {
				// search for variable since this is an Assignement
				n.setClass(Node.ASSIGN);
				n.assignLeftRight(Node.VAR, Node.VAR, counter);
				counter += 2;
				
				n.left.varName = id;
				n.setLine(s.line_counter);
				this.assignment(n.right);
			}
			else if (this.s.sym == Scanner.lparen) {
				n.setClass(Node.PROC_CALL);
				n.setLine(s.line_counter);
				n.varName = id;
				// cant check id yet since not all procedures are declared
				this.procedure_call(n);
			}
			else {
				this.Error("Statement falsch nach ident kein '=' oder '('!");
			}
		}
		else if (this.s.sym == Scanner.sst_if) {
			this.inIf = true;
			this.if_statement(n);
			this.inIf = false;
		}
		else if (this.s.sym == Scanner.sst_while) {
			this.inWhile = true;
			this.while_statement(n);
			this.inWhile = false;
		}
		else if (this.s.sym == Scanner.sst_ret) {
			this.return_statement(n);
			if (this.curFunc != null && !this.inIf && !this.inWhile) {
				this.ret = true;
			}
		}
		else {
			this.Error("Statement falsch! Anfang!");
		}
	}
	
	public void type() throws Exception {
		if (this.s.sym == Scanner.sst_int) {
			s.getSym();
		}
		else {
			this.Error("Wrong Type Declaration!");
		}
	}
	
	public String assignment(Node n) throws Exception {
		String assign = "";
		// ident has already been checked (had to be)
		if (this.s.sym == Scanner.equal) {
			s.getSym();
			assign += this.expression(n);
			if (this.s.sym == Scanner.sst_semicolon) {
				s.getSym();
			}
			else {
				this.Error("Fehlendes Semikolon in Assignement!");
			}
		}
		else {
			this.Error("Fehlendes Gleichheitszeichen in Assignement!");
		}
		return assign;
	}
	
	public void procedure_call(Node n) throws Exception {
		this.intern_procedure_call(n);
		if (this.s.sym == Scanner.sst_semicolon) {
			s.getSym();
		}
		else {
			this.Error("Fehlendes Semikolon in Procedure Call!");
		}
	}
	
	public void intern_procedure_call(Node n) throws Exception {
		// ident was already read
		this.actual_parameters(n);
	}
	
	public void if_statement(Node n) throws Exception {		
		n.setClass(Node.IFELSE);
		Node left = new Node(Node.IF, counter);
		counter += 1;
		Node ifCond = new Node(Node.IF_COND, counter);
		counter += 1;
		Node ifDo = new Node(Node.IF_DO, counter);
		counter += 1;
		
		left.setLeft(ifCond);
		left.setRight(ifDo);
		
		Node right = new Node(Node.ELSE, counter);
		counter += 1;
		
		n.setLeft(left);
		n.setRight(right);
		
		if (this.s.sym == Scanner.sst_if) {
			s.getSym();
			if (this.s.sym == Scanner.lparen) {
				s.getSym();
								
				this.expression(ifCond);
								
				if (this.s.sym == Scanner.rparen) {
					s.getSym();
					if (this.s.sym == Scanner.lbrace) {
						s.getSym();
						this.statement_sequence(ifDo);
						if (this.s.sym == Scanner.rbrace) {
							s.getSym();
							if (this.s.sym == Scanner.sst_else) {
								s.getSym();
								if (this.s.sym == Scanner.lbrace) {
									s.getSym();
									this.statement_sequence(right);
									if (this.s.sym == Scanner.rbrace) {
										s.getSym();
									}
									else {
										this.Error("Fehlende Klammer '}' in else von if Statement!");
									}
								}
								else {
									this.Error("Fehlende Klammer '{' in else von if Statement!");
								}
							}
							else {
								this.Error("Fehlendes else in if_statment!");
							}
						}
						else {
							this.Error("Fehlende Klammer '}' in if Statement!");
						}
					}
					else {
						this.Error("Fehlende Klammer '{' an Anfang von if Statement!");
					}
				}
				else {
					this.Error("Fehlende Klammer ')' an Anfang von if Statement!");
				}
			}
			else {
				this.Error("Fehlende Klammer '(' an Anfang von if Statement!");
			}
		}
		else {
			this.Error("Fehlendes if in if_statement!");
		}
	}
	
	public void while_statement(Node n) throws Exception {		
		n.setClass(Node.WHILE);
		n.setLeft(new Node(Node.WHILE_COND, counter));
		counter += 1;
		n.setRight(new Node(Node.WHILE_DO, counter));
		counter += 1;
		
		if (this.s.sym == Scanner.sst_while) {
			s.getSym();
			if (this.s.sym == Scanner.lparen) {
				s.getSym();
				this.expression(n.left);
				if (this.s.sym == Scanner.rparen) {
					s.getSym();
					if (this.s.sym == Scanner.lbrace) {
						s.getSym();
						this.statement_sequence(n.right);
						if (this.s.sym == Scanner.rbrace) {
							s.getSym();
						}
						else {
							this.Error("Fehlende '}' in while_statement!");
						}
					}
					else {
						this.Error("Fehlende '{' in while_statement!");
					}
				}
				else {
					this.Error("Fehlende ')' in while_statement!");
				}
			}
			else {
				this.Error("Fehlende '(' in while_statement!");
			}
		}
		else {
			this.Error("Fehlendes While in while_statement!");
		}
	}
	
	public void return_statement(Node n) throws Exception {		
		n.setClass(Node.RETURN);
		
		n.setLeft(new Node(Node.VOID, counter));
		counter += 1;
		
		if (this.s.sym == Scanner.sst_ret) {
			s.getSym();
			String ret = "VOID";
			if (this.s.sym != Scanner.sst_semicolon) {	
				ret = "INT";
				n.setRight(new Node(Node.TEMP, counter));
				counter += 1;
				
				this.simple_expression(n.right);
				n.left.setClass(Node.INT);
			}
			// Function doesnt return right value
			if (this.curFunc.resultType != ret) {
				this.ErrorNode(s.line_counter, "Function should return " + this.curFunc.resultType + ", but returns " + ret);
			}
			if (this.s.sym == Scanner.sst_semicolon) {
				s.getSym();
			}
			else {
				this.Error("Fehlendes ';' in return_statement!");
			}
		}
		else {
			this.Error("Fehlendes return in return_statement!");
		}
	}
	
	public String actual_parameters(Node n) throws Exception {
		// cant check if number is correct since procedure might not
		// have been initialized
		n.setLeft(new Node(Node.PROC_CALL, counter));
		counter += 1;
		n = n.left;
		n.setLine(s.line_counter);

		String actPara = "";
		if (this.s.sym == Scanner.lparen) {
			s.getSym();
			if (this.s.sym != Scanner.rparen) {
				actPara += this.expression(n);
				while (this.s.sym == Scanner.sst_comma) {
					n.setLink(new Node(Node.VAR, counter));
					counter += 1;
					
					n = n.link;
					n.setLine(s.line_counter);
					
					s.getSym();
					actPara += "," + this.expression(n);
				}
				if (this.s.sym == Scanner.rparen) {
					actPara += ")";
					
					s.getSym();
				}
				else {
					this.Error("Fehlende Klammer ')' in actual_parameters!");
				}
			}
			else {
				s.getSym();
			}
		}
		else {
			this.Error("Fehlende Klammer '(' in actual_parameters!");
		}
		return actPara;
	}
	
	public String expression(Node n) throws Exception {
		String expr = "";
		
		Node t = new Node(Node.VAR, counter);
		t.setLine(s.line_counter);
		
		counter += 1;
		
		expr += this.simple_expression(t);
		
		n.setClass(Node.COMPOP);
		
		if (this.s.sym == Scanner.equal_equal || this.s.sym == Scanner.smaller || 
			this.s.sym == Scanner.smaller_equal || this.s.sym == Scanner.bigger ||
			this.s.sym == Scanner.bigger_equal) {
			n.setLeft(new Node(t, counter));
			n.setLine(s.line_counter);
			counter += 1;
			n.setRight(new Node(Node.COMPOP, counter));
			counter += 1;
			n.operation = s.compare;
			
			expr += s.compare;
			s.getSym();
			expr += this.simple_expression(t);
			
			n = n.right;
		}
		n.copyNode(t);		
		return expr;
	}
	
	public String simple_expression(Node n) throws Exception {
		String simpleExpr = "";
		
		Node t = new Node(Node.VAR, counter);
		t.setLine(s.line_counter);
		counter += 1;
		
		simpleExpr += this.term(t);
		n.setClass(Node.BINOP);
				
		while (this.s.sym == Scanner.plus || this.s.sym == Scanner.minus) {
			if (this.s.sym == Scanner.plus) { simpleExpr += "+"; n.operation = "+"; }
			else { simpleExpr += "-"; n.operation = "-"; }
			n.setLeft(new Node(t, counter));
			counter += 1;
			n.setRight(new Node(Node.BINOP, counter));
			counter += 1;
			
			s.getSym();
			
			simpleExpr += this.term(t);
			
			n = n.right;
		}	
		n.copyNode(t);
		
		return simpleExpr;
	}
	
	public String term(Node n) throws Exception {
		String term = "";
		
		Node t = new Node(Node.VAR, counter);
		t.setLine(s.line_counter);
		counter += 1;
		
		term += this.factor(t);
		
		while (this.s.sym == Scanner.mult || this.s.sym == Scanner.div) {
			if (this.s.sym == Scanner.mult) { term += "*"; n.operation = "*"; }
			else { term += "/"; n.operation = "/"; }
			n.setClass(Node.BINOP);
			n.setLeft(new Node(t, counter));
			counter += 1;
			n.setRight(new Node(Node.BINOP, counter));
			counter += 1;
			
			s.getSym();
			
			term += this.factor(t);
			n = n.right;
		}
		n.copyNode(t);
		return term;
	}
	
	public String factor(Node n) throws Exception {
		String factor = "";		
		if (this.s.sym == Scanner.ident) {
			factor += s.id;
			n.varName = s.id;
			
			s.getSym();
			n.setClass(Node.VAR);
			
			n.setLine(s.line_counter);
			if (this.s.sym == Scanner.lparen) {
				// intern procedure call
				n.setClass(Node.PROC_CALL);
								
				factor += "(" + this.actual_parameters(n);
			}
		}
		else if (this.s.sym == Scanner.number) {
			factor += s.num;
			
			n.setClass(Node.CONST);
			n.varVal = this.stringToInt(s.num);
						
			s.getSym();
		}
		else if (this.s.sym == Scanner.lparen) {
			// Expression
			s.getSym();
			n.first = true;
			factor += "(" + this.expression(n);
			if (this.s.sym == Scanner.rparen) {
				s.getSym();
				factor += ")";
			}
			else {
				this.Error("Fehlende Klammer ')' in factor!");
			}
		}
		else {
			this.Error("factor is wrong!");
		}
		return factor;
	}
	
	public String ident(Node n) throws Exception {
		String id = "";
		n.setLine(s.line_counter);
		if (this.s.sym == Scanner.ident) { id += s.id; s.getSym(); }
		else { this.Error("No Ident!"); }
		return id;
	}
	
	public int stringToInt(String number) throws Exception {
		int val = 0;
		for (int i = 0; i < number.length(); i++) {
			if (!('0' <= number.charAt(i) && number.charAt(i) <= '9')) {
				this.Error("Convertion failed of " + number);
			}
			val = val * 10 + (number.charAt(i) - '0');
		}
		return val;
	}
	
	private void Error(String string) throws Exception {
		System.out.println(string);
		System.out.println("Line: " + s.line_counter);
		System.out.println("******************* End ****************");
		System.exit(1);
	}
	
	private void ErrorNode(int line, String string) throws Exception {
		System.out.println(string);
		System.out.println("Line: " + line);
		System.out.println("******************* End ****************");
		System.exit(1);
	}
	
	static private void endError(String string) throws Exception {
		System.out.println(string);
		System.out.println("******************* End ****************");
		System.exit(1);
	}
}
