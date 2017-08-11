package questionGenerator;

import symjava.symbolic.Expr;
import symjava.symbolic.Symbol;

public class Device {
	
	//Yes, I should be using proper OOP here.  prototype-prototype-prototype.
	//Devices have 2 sets of data - the device data (data like its name and value that don't rely
	//on a solved circuit), and the solution data.
	public String name;
	public String type;
	public double value;
	public int node1;
	public int node2;
	public int centerX;
	public int centerY;
	public boolean isVertical; //whether the part gets displayed vertically.  This will...probably
	//be more sophisticated later
	
	//symbolic math data
	public Symbol sym;
	public Expr componentCurrent;
	
	//the solution data
	public double current;
	public double voltage1;
	public double voltage2; //voltages at node 1 and node 2.  We don't yet know which is
	//more positive
	public double power;
	
	/*
	 * WARNING: potentially bad idea above - passive components have two node voltages, while active 
	 * components have only one.  This feels like a silly billy thing to do...so be aware that you
	 * made that decision
	 * 
	 * TODO: Revisit this discussion
	 */
	
	
	//default constructor is used when we are reading from the input file
	public Device()
	{
		//the device data
		this.name = "";
		this.type = "";
		this.value = 0;
		this.node1 = -1;
		this.node2 = -1; //init to -1 since 0 is a valid node number
		this.centerX = 0;
		this.centerY = 0;
		this.isVertical = false;
		
		//the solution
		this.current = 0;
		this.voltage1 = 0;
		this.voltage2 = 0;
		this.power = 0;
		
		//the current
		this.componentCurrent = 0;
	}
	
	//a constructor useful for the series/parallel connection for symbolic computation
	public Device(String n, String t, int n1, int n2)
	{
		this.name = n;
		this.type = t;
		this.node1 = n1;
		this.node2 = n2;
		this.sym = new Symbol(this.name);
		this.componentCurrent = 0;
	}
	
	//power is given as voltage1-voltage2 (voltage difference) times current.
	void updatePower()
	{
		this.power = (this.voltage1 - this.voltage2) * this.current;
	}
	
	//a helper function that turns a letter into a human-readable component class
	String parseType(String s)
	{
		
		String typeLetter = s.substring(0, 1); //R, C, L, or V for now
	
		if(typeLetter.equals("R"))
		{
			return "Resistor";
		}
		else  if(typeLetter.equals("C"))
		{
			return "Capacitor";
		}
		else if(typeLetter.equals("L"))
		{
			return "Inductor";
		}
		else
		{
			return "Voltage Source";
		}
	}
	
	/*
	 * This function fills in the device data from the  file that comes from the MATLAB classifier
	 */
	public void parseDeviceData(String s)
	{
		//we first need to split it - it's space delimited
		String[] Data = s.split(" ");
		
		//first entry is the name and gives information on the type
		this.name = Data[0];
		this.sym = new Symbol("$"+this.name); //every device has a Symbol that is its own name
		this.type = parseType(Data[0]);
		
		//for the  rest of the data, we can just steal them directly from the data after  parsing
		
		//nodes
		this.node1 = Integer.parseInt(Data[1]);
		this.node2 = Integer.parseInt(Data[2]);
		
		//position
		this.centerX = Integer.parseInt(Data[3]);
		this.centerY = Integer.parseInt(Data[4]);
		
		//verticality
		this.isVertical = Boolean.parseBoolean(Data[5]);
		
	}
	
	//A helper function for the series and parallel generators
	//Returns true if this component has a node with the given number
	public boolean hasNode(int nodeNum)
	{
		return (this.node1 == nodeNum || this.node2 == nodeNum);
	}
	
	//equals: two components are equal if they have the same NAME.
	public boolean equals(Device other)
	{
		return (this.name.equals(other.name));
	}
	
	public String toString()
	{
		String s = "";
		
		s = "Device name: " + this.name + "\n";
		s += "Device type: " + this.type + "\n";
		s += "Nodes: " + this.node1 + " " + this.node2 + "\n";
		s += "Position: (" + this.centerX + "," + this.centerY + ")\n";
		s += "Vertical: " + this.isVertical + "\n";
		s += "Voltage 1: " + this.voltage1 + ", Voltage 2: " + this.voltage2 + "\n";
		s += "Current: " + this.current + "\n";
		s += "Power: " + this.power + "\n";
		return s;
	}
	
	//this function outputs a string for MapleTA to use a randomization algorithm
	public String outputDeviceVariable(int lower, int upper)
	{
		//here is the format: $variableName=maple("randomize():rand(lower..upper)()")
		String outString = this.sym.toString() + "=maple(\"randomize():rand(" + lower + ".." + upper + ")()\");";
		return outString;
	}
}
