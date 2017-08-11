package questionGenerator;

import symjava.symbolic.Expr;
import symjava.symbolic.Symbol;

public class Node {
	
	//nodes have NUMBERS, equations (their nodal, KCL equation), and voltages (what we want to solve for)
	public int nodeNum;
	Expr eqn;
	Symbol voltage;
	boolean known;
	
	public Node(int n)
	{
		this.nodeNum = n;
		this.eqn = 0;
		this.voltage = new Symbol("vn"+n);
		this.known = false; //determines whether this is specified by the components (for example, a node above a
		//voltage source that is otherwise connected to ground is a known voltage)
	}
	
	//two nodes are equal if they have the same number
	public boolean equals(Node n)
	{
		return this.nodeNum == n.nodeNum;
	}
}
