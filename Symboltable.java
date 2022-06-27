package uebung;

public class Symboltable {
	Object head;
	Symboltable enclose; // übergeordneter Block
	int length;
		
	public Symboltable() {
		this.head = new Object();
		this.enclose = null;
		this.length = 0;
	}
	
	public Symboltable(Symboltable s) {
		this.head = new Object();
		this.enclose = s;
		this.length = 0;
	}
	
	public Object insert(Object obj) {
		Object pointer = head;
		if (pointer.next != null) {
			pointer = pointer.next;
			while (pointer.next != null) {
				if (pointer.name.contentEquals(obj.name) && pointer.objClass == obj.objClass && pointer.parameterLength == obj.parameterLength) {
					System.out.println("Insert - Object " + pointer.name + " exists in Line " + pointer.getLine());
					this.Error("Insert - Error in Line " + obj.getLine());
				}
				pointer = pointer.next;
			}
			if (pointer.name.contentEquals(obj.name) && pointer.objClass == obj.objClass && pointer.parameterLength == obj.parameterLength) {
				System.out.println("Insert - Object " + pointer.name + " exists in Line " + pointer.getLine());
				this.Error("Insert - Error in Line " + obj.getLine());
			}
		}
		pointer.next = obj;
		length++;
		return obj;
	}
	
	public boolean searchObject(String name, int objClass) {
		Object pointer = head;
		if (pointer.next != null) {
			pointer = pointer.next;
			while (pointer.next != null) {
				if (pointer.name.contentEquals(name) && pointer.objClass == objClass) {
					return true;
				}
				pointer = pointer.next;
			}
			if (pointer.name.contentEquals(name) && pointer.objClass == objClass) {
				return true;
			}
		}
		return false;
	}
	
	public boolean getInit(String name, int objClass) {
		// object has to exists (check before)
		Object pointer = head;
		if (pointer.next != null) {
			pointer = pointer.next;
			while (pointer.next != null) {
				if (pointer.name.contentEquals(name) && pointer.objClass == objClass) {
					return pointer.init;
				}
				pointer = pointer.next;
			}
			if (pointer.name.contentEquals(name) && pointer.objClass == objClass) {
				return pointer.init;
			}
		}
		return false;
	}
	
	public void setInit(String name, int objClass) {
		// object has to exists (check before)
		Object pointer = head;
		if (pointer.next != null) {
			pointer = pointer.next;
			while (pointer.next != null) {
				if (pointer.name.contentEquals(name) && pointer.objClass == objClass) {
					pointer.init = true;
					return;
				}
				pointer = pointer.next;
			}
			if (pointer.name.contentEquals(name) && pointer.objClass == objClass) {
				pointer.init = true;
				return;
			}
		}
	}
	
	public int searchObjectIndex(String name, int objClass) {
		Object pointer = head;
		int index = 1;
		if (pointer.next != null) {
			pointer = pointer.next;
			while (pointer.next != null) {
				if (pointer.name.contentEquals(name) && pointer.objClass == objClass) {
					return index;
				}
				pointer = pointer.next;
				index += 1;
			}
			if (pointer.name.contentEquals(name) && pointer.objClass == objClass) {
				return index;
			}
		}
		return 0;
	}
	
	public boolean searchFunction(String name, int parameterLength) {
		Object pointer = head;
		if (pointer.next != null) {
			pointer = pointer.next;
			while (pointer.next != null) {
				if (pointer.name.contentEquals(name) && pointer.parameterLength == parameterLength) {
					return true;
				}
				pointer = pointer.next;
			}
			if (pointer.name.contentEquals(name) && pointer.parameterLength == parameterLength) {
				return true;
			}
		}
		return false;
	}
	
	public Object getFunction(String name, int parameterLength) {
		Object pointer = head;
		if (pointer.next != null) {
			pointer = pointer.next;
			while (pointer.next != null) {
				if (pointer.name.contentEquals(name) && pointer.parameterLength == parameterLength) {
					return pointer;
				}
				pointer = pointer.next;
			}
			if (pointer.name.contentEquals(name) && pointer.parameterLength == parameterLength) {
				return pointer;
			}
		}
		this.Error("FUNTION DOES NOT EXISTS -> getfunction in Symboltable");
		return pointer;
	}
	
	public void searchObjectError(String name, int objClass, int line) {
		Object pointer = head;
		if (pointer.next != null) {
			pointer = pointer.next;
			while (pointer.next != null) {
				if (pointer.name.contentEquals(name) && pointer.objClass == objClass) {
					System.out.println("Search - Object " + pointer.name + " exists in Line " + pointer.getLine());
					this.Error("Search - Error in Line " + line);
				}
				pointer = pointer.next;
			}
			if (pointer.name.contentEquals(name) && pointer.objClass == objClass) {
				System.out.println("Search - Object " + pointer.name + " exists in Line " + pointer.getLine());
				this.Error("Search - Error in Line " + line);
			}
		}
	}
	
	public void Error(String str) {
		System.out.println(str);
		System.exit(1);
	}
	
	public void printSymboltable() {
		System.out.println("This Symboltable has Length " + this.length);
		Object pointer = this.head;
		if (pointer.next == null) { System.out.println("Head.next == null in printSymboltable!"); }
		else {
			if (pointer.next.objClass == Object.sst_var) {
				if (pointer.next.fin) { pointer.printFinalVar(); }
				else { pointer.printVar(); }
			}
			else if (pointer.next.objClass == Object.sst_proc) {
				pointer.printProc();
			}
		}
	}
	
	public void printElements() {
		System.out.println("This Symboltable has Length " + this.length);
		Object pointer = head;
		while (pointer.next != null) {
			pointer = pointer.next;
			if (pointer.objClass == Object.sst_var) {
				if (pointer.fin) {
					System.out.println("Const: " + pointer.name);
				}
				else {
					System.out.println("Int: " + pointer.name);
				}
			}
			else if (pointer.objClass == Object.sst_proc) {
				System.out.println("Procedure: " + pointer.name + ", with " + pointer.parameterLength + " Parameter.");
				pointer.parameter.printElements();
				if (pointer.nextSymboltable.length != 0) {
					System.out.println("Procedure Start: ");
					pointer.nextSymboltable.printElements();
					System.out.println("Procedure End: ");
				}
			}
		}
	}
	
	public void printProcedure() {
		System.out.println("This Symboltable has Length " + this.length);
		Object pointer = head.next;
		System.out.println("**************************");
		while (pointer.next != null) {
			System.out.println("Name: " + pointer.name);
			pointer.printProcNode();
			pointer = pointer.next;
			System.out.println("**************************");
		}
		System.out.println("Name: " + pointer.name);
		pointer.printProcNode();
		System.out.println("**************************");
	}
	
	public void printMain() {
		Object pointer = head.next;
		
		System.out.println("**************************");
		System.out.println("Class Name: " + pointer.name);
		if (pointer.nextSymboltable != null) {
			pointer.nextSymboltable.printElements();
		}
		System.out.println("**************************");
	}
}
