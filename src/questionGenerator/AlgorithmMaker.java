package questionGenerator;

import java.util.ArrayList;

/* 
 * This class makes the base of the algorithm for MapleTA (and other LMS's).  It allows for control over device
 * randomization, and completely generates all systems of equations/supplementary equations for later use.
 */

public class AlgorithmMaker {
	public Circuit cr; 
	ArrayList<String> mapleTA;
	
	//TODO: allow user-customization of random values
	public AlgorithmMaker(Circuit c)
	{
		//initialize the circuit
		this.cr = c;
		
		//create the algorithms
		//TODO: More algorithms for more LMS's
		this.mapleTA = this.mapleTAAlgorithm();
	}
	
	/*
	 * The MapleTA output algorithm.  It includes:
	 * 	- Setup of all known variables
	 * 	- Setup of system of equations
	 * 	- Solution of SoE
	 * 	- Generation of all device current/voltage variables
	 */
	ArrayList<String> mapleTAAlgorithm()
	{
		ArrayList<String> outList = new ArrayList<String>();
		String setupOut = ""; //these are the variables that come out
		
		//generate the variables: devices
		for (Device d : cr.devices)
		{
			setupOut += d.outputDeviceVariable(1, 10) + "\n";
		}
		
		//generate the variables: nodes
		for (Node n : cr.nodes)
		{
			if(n.known == true)
			{
				setupOut += n.voltage + "=" + n.eqn + ";\n";
			}
		}
		
		
		//generate the string that will house the equations
		String eqString = "{";
		for (Node n : cr.nodes)
		{
			//ps: the enhanced for loop is awesome
			if(n.known == false)
			{
				eqString += n.eqn + "=0, ";
			}
		}
		//Remove the last comma: make life easy with substring
		eqString = eqString.substring(0,eqString.length()-2);
		eqString += "}";
		
		//generate the string that will house the unknowns
		String unknownString = "{";
		int numUnknowns = 0;
		ArrayList<String> unknowns = new ArrayList<String>();
		for(Node n : cr.nodes)
		{
			if(n.known == false)
			{
				unknownString += n.voltage + ", ";
				unknowns.add(n.voltage.toString());
				numUnknowns++;
			}
		}
		//again, remove those last two characters
		unknownString = unknownString.substring(0,unknownString.length()-2);
		unknownString += "}";
		
		setupOut += "$Soln=maple(\"solve("+eqString +"," + unknownString + ")\");\n";
		
		//OK, at this point we have a solution to the system of equations.  We must now extract node voltages for each device.
		//Step 1: a list of node voltages, accessible via Maple.  This is easy enough for the knowns (we already have it), but for the
		//unknowns we need to make it.  This is to go inside of  the variable setup string
		for(int i = 0; i < numUnknowns; i++)
		{
			setupOut += "$" + unknowns.get(i) + "=maple(\"fsolve($Soln[" + (i+1) + "])\");\n"; //Maple indexes its arrays at 1
		}
		
		//Swagger.  Next step is to create device voltages and currents  
		for(Device d : cr.devices)
		{
			//Only for resistors for now.
			//TODO: Expand to arbitrary devices
			if(d.type.equals("Resistor"))
			{
				String nv1 = "$vn" + d.node1;
				String nv2 = "$vn" + d.node2;
				//device voltage
				setupOut += "$i" + d.name +"=maple(\"evalf((" + nv1 + "-" + nv2 + ")/$" + d.name + ")\");\n";
				//device current
				setupOut += "$v" + d.name + "=maple(\"evalf((" + nv1 + "-" + nv2 + "))\");\n";
			}
		}
		/*
		 * The order is important.  The first string is the algorithm setup.  The next set of strings are the node voltages that are
		 * unknown.  We distinguish them to make it slightly easier to write algorithms for finding said node voltages
		 */
		outList.add(setupOut);
		for(int i = 0; i < unknowns.size(); i++)
		{
			outList.add(unknowns.get(i));
		}
		outList.add(numUnknowns+"");

		
		return outList;
	}
}
