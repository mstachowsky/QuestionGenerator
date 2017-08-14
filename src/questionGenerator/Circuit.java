package questionGenerator;

import java.util.ArrayList;

import symjava.symbolic.Expr;
import symjava.symbolic.Symbol;
//TODO: Make this more Object Oriented friendly.  This is written very much by a C programmer, and not a very good one at that
public class Circuit {
	public ArrayList<Device> devices;
	public ArrayList<Series> seriesConnections;
	public ArrayList<Parallel> parallelConnections;
	public ArrayList<Node> nodes;
	public AlgorithmMaker Algorithm;
	
	public Circuit()
	{
		this.devices = new ArrayList<Device>();
		this.seriesConnections = new ArrayList<Series>();
		this.parallelConnections = new ArrayList<Parallel>();
		this.nodes = new ArrayList<Node>();
	}
	
	public void initAlgorithm()
	{
		this.Algorithm = new AlgorithmMaker(this); //just set it all up.  This has to be done after all the initialization
	}
	
	//helper function - determines if a node voltage is known or unknown
	public boolean isNodeKnown(int n)
	{
		for(int i = 0; i < nodes.size(); i++)
		{
			if(nodes.get(i).nodeNum == n && nodes.get(i).known == true)
				return true;
		}
		return false;
	}
	
	//helper function version 2: gets a node with a given number.
	//returns null if we didn't find anything
	public Node getNodeFromNum(int n)
	{
		for(int i = 0; i < nodes.size(); i++)
		{
			if(nodes.get(i).nodeNum == n)
				return nodes.get(i);
		}
		return null;
	}
	
	//generates the nodal equations
	public void generateNodalEquations()
	{
		for(int i = 0; i < nodes.size(); i++)
		{
			//don't generate equations for nodes that are known
			if(!nodes.get(i).known)
			{
				//find all device currents for this node
				for(int j = 0; j < devices.size();j++)
				{
					if(devices.get(j).node1 == nodes.get(i).nodeNum) //this is an outgoing current
					{
						nodes.get(i).eqn = nodes.get(i).eqn - devices.get(j).componentCurrent;
					}
					else if(devices.get(j).node2 == nodes.get(i).nodeNum) //this is an incoming current
					{
						nodes.get(i).eqn = nodes.get(i).eqn + devices.get(j).componentCurrent;
					}
				}
				//simplify
				nodes.get(i).eqn.simplify();
			}
		}
	}
	
	//Generates the device currents for devices for whom both nodal voltages are unknown
	public void generateDeviceCurrents()
	{
		for(int i = 0; i < devices.size(); i++)
		{
			if(!isNodeKnown(devices.get(i).node1) || !isNodeKnown(devices.get(i).node2))
			{
				//System.out.println("Device: " + devices.get(i).name); //just print for now to see if it's working
				//we've found one that needs work, so we need both nodes
				Node n1 = getNodeFromNum(devices.get(i).node1);
				Node n2 = getNodeFromNum(devices.get(i).node2);
				
				//at the moment, these can only ever be resistors. 
				//TODO: make this work for more general components!
				Expr tmp1 = n1.voltage;
				Expr tmp2 = n2.voltage;
				//resolve ground: basically, substituting things in is hard until we get a more comptetent
				//symbolic math toolbox
				if(n1.nodeNum == 0)
					tmp1 = 0;
				if(n2.nodeNum == 0)
					tmp2 = 0;
				devices.get(i).componentCurrent = (tmp1 - tmp2)/devices.get(i).sym;
			}
		}
	}
	
	//initialize nodes - assumes that devices is initialized
	public void initNodes()
	{
		//we basically add nodes that do not already exist
		for(int i = 0; i < devices.size();i++)
		{
			boolean isFound1 = false;
			boolean isFound2 = false;
			for(int j = 0; j < nodes.size(); j++)
			{
				if(nodes.get(j).nodeNum == devices.get(i).node1)
					isFound1 = true;
				if(nodes.get(j).nodeNum == devices.get(i).node2)
					isFound2 = true;
			}
			if(!isFound1)
			{
				nodes.add(new Node(devices.get(i).node1));
			}
			if(!isFound2)
			{
				nodes.add(new Node(devices.get(i).node2));
			}
		}
		
		//Now that the nodes are added, we need to resolve known nodes. Ground is the first one, any voltage sources are the other
		for(int i = 0; i < nodes.size(); i++)
		{
			if(nodes.get(i).nodeNum == 0)
			{
				nodes.get(i).known = true; //it is known and it is ground.  Ground always has value 0
				nodes.get(i).eqn = 0; //it's ground.  0 is always ground.
				nodes.get(i).voltage = new Symbol("$" + nodes.get(i).voltage.toString());
			}else{
			/*
			 * Now loop through the devices, finding voltage sources.
			 * TODO: We know that voltage sources may not always be connected to ground.  This is very prototype version.
			 * We assume that the voltage source's first node is the positive terminal (this conforms to what SPICE says)
			 * 
			 */
				for(int j = 0; j < devices.size(); j++)
				{
					if(devices.get(j).type == "Voltage Source")
					{
						if(devices.get(j).node1 == nodes.get(i).nodeNum)
						{
							nodes.get(i).eqn = devices.get(j).sym;
							nodes.get(i).known = true;
							nodes.get(i).voltage = new Symbol("$" + nodes.get(i).voltage.toString());
						}
					}
				}
			}
		}
	}
	
	//initialize devices
	public void initDevices(ArrayList<Device> dv)
	{
		this.devices.addAll(dv); //a deep copy...I think?
	}
	
	public void printSeries()
	{
		for(int i = 0; i < seriesConnections.size(); i++)
			System.out.println(seriesConnections.get(i).toString());
	}
	
	//find all serial components
	public void findSeries()
	{
		ArrayList<Device> crDevices = new ArrayList<Device>();
		ArrayList<ArrayList<Device>> allSeries = new ArrayList<ArrayList<Device>>(); //holds the series connections
		crDevices.addAll(this.devices);
		
		for(int i = 0; i < crDevices.size(); i++){
		ArrayList<Device> serCon = new ArrayList<Device>(); //this will be added into the seriesConnection
		//list
		
		int node1Count = 0;
		int node2Count = 0;
		//search for device 0's connections for now
		for(int j = 0; j < crDevices.size(); j++)
		{
			
			//count the number of devices that share this device's nodes, ignoring this device itself,
			//and do not add if they share both nodes
			if(crDevices.get(j).hasNode(crDevices.get(i).node1) && !crDevices.get(j).hasNode(crDevices.get(i).node2))
			{
				node1Count++;		
			}
			else if(crDevices.get(j).hasNode(crDevices.get(i).node2) && !crDevices.get(j).hasNode(crDevices.get(i).node1)) 
			{
				node2Count++;
			}
		}
		
		if(node1Count == 1 || node2Count == 1)
			serCon.add(crDevices.get(i)); //this device is part of a series chain
		
		//If either of the nodes has only 1 connection, this is a 
		//series node
		if(node1Count == 1)
		{
			for(int j = 0; j < crDevices.size();j++)
			{
				if(crDevices.get(j).hasNode(crDevices.get(i).node1))
				{
					//don't add a device already in the chain. 
					if(!serCon.contains(crDevices.get(j)))
						serCon.add(crDevices.get(j));
					//remove the device from crDevices to avoid counting it twice
				//	crDevices.remove(j);
				}
			}
		}
		if(node2Count == 1)
		{
			for(int j = 0; j < crDevices.size();j++)
			{
				if(crDevices.get(j).hasNode(crDevices.get(i).node2))
				{
					if(!serCon.contains(crDevices.get(j)))
						serCon.add(crDevices.get(j));
				//	crDevices.remove(j);
				}
			}			
		}
		
		//if serCon's size is zero that means that we have some components that aren't in
		//series
		if(serCon.size() != 0)
			allSeries.add(serCon);
		}
		
		//now, we merge!
		
		for(int i = 0; i < allSeries.size(); i++)
		{
			ArrayList<Device> serCon = (ArrayList<Device>)allSeries.get(i);
			//now just print and see
			//System.out.print("Device: " + serCon.get(0).name + " AND ");
			for(int j = 0; j < allSeries.size();j++)
			{
				//check each series connection against all others
				if(i != j)
				{
					ArrayList<Device> toCheck = (ArrayList<Device>)allSeries.get(j);
					//first, see if there are common elements
					boolean hasCommon = false;
					for(int k = 0; k < toCheck.size(); k++)
					{
						if(serCon.contains(toCheck.get(k)))
							hasCommon = true;
					}
					
					//now merge
					if(hasCommon)
					{
						for(int k = 0; k < toCheck.size(); k++)
						{
							if(!serCon.contains(toCheck.get(k)))
								serCon.add(toCheck.get(k));
						}
						//at this point the two are merged. Remove the second list from considerationm
						allSeries.remove(j);
					}
				}
			}
		}
		
		//Alrighty!  Now we have all of the series connections that are contained in this circuit. We can fill up the arrayList.
		
		for(int i = 0; i < allSeries.size(); i++)
		{
			Series s = new Series(allSeries.get(i),i);
			seriesConnections.add(s);
		}
		
		//print to see
	/*	for(int i = 0; i < allSeries.size(); i++)
		{	ArrayList<Device> serCon = (ArrayList<Device>)allSeries.get(i);
			for(int j = 0; j < serCon.size(); j++)
				System.out.print(serCon.get(j).name + " ");
			System.out.println("");
		}*/
	}
	
	
	
	//There is a common setup for all questions.  this function produces the string that is generated by that common setup
	//TODO: This randomly generates values between 1 and 10 for all devices.  This is a temporary range.  This random range selection must be addressed
	public ArrayList<String> setupString()
	{
		ArrayList<String> outList = new ArrayList<String>();
		String setupOut = ""; //these are the variables that come out
		
		//generate the variables: devices
		for (Device d : devices)
		{
			setupOut += d.outputDeviceVariable(1, 10) + "\n";
		}
		
		//generate the variables: nodes
		for (Node n : nodes)
		{
			if(n.known == true)
			{
				setupOut += n.voltage + "=" + n.eqn + ";\n";
			}
		}
		
		
		//generate the string that will house the equations
		String eqString = "{";
		for (Node n : nodes)
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
		for(Node n : nodes)
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
		outList.add(setupOut);
		outList.add(numUnknowns+"");
		for(int i = 0; i < unknowns.size(); i++)
		{
			outList.add(unknowns.get(i));
		}
		return outList;
	}
	
	/*
	 * Generates an array list asking for voltages across components, with the following entries:
	 * list.get(0) = the variable setup.  At the moment, all parameter values are assumed random
	 * TODO: Remove that assumption later, and let the user specify if desired.
	 * 
	 * list.get(i), i > 0 = one device voltage output for each node, in the following format:
	 * vnM:(Answer string) - this is so that we can later extract which node voltage we are asking for
	 * Remember, it's colon separated
	 */
	public ArrayList<String> MapleTADeviceVoltageOut()
	{
		//every one of these functions is going to use the same setup, but it requires the setup of a lot
		//of arrayLists, so I cannot imagine how to do this easily.  Well, I'm obviously a procedural person
		ArrayList<String> outList = new ArrayList<String>();
		ArrayList<String> setupList = this.Algorithm.mapleTA;
		
		//The string at setupList(0) is the algorithm and variable setup
		outList.add(setupList.get(0));
		
		//Swagger.  Now it's time to just output answers in new strings each time, for each device.  Well, for each resistor...
		for(Device d : devices)
		{
			//Only for resistors for now.
			//TODO: Expand to arbitrary devices
			if(d.type.equals("Resistor"))
			{
				//":$answer=maple(\"fsolve($Soln[" + i + "])\");@");
				String ans = d.name + ":";
				ans += "$answer=$v"+d.name + ";@";
				outList.add(ans);
			}	
		}
		return outList;
	}
	
	//TODO: BIG TO DO: fix all of the other generating functions to use the AlgorithmMaker framework
	
	/*
	 * Generates an array list with the following entries:
	 * list.get(0) = the variable setup.  At the moment, all parameter values are assumed random
	 * TODO: Remove that assumption later, and let the user specify if desired.
	 * 
	 * list.get(i), i > 0 = one node voltage output for each node, in the following format:
	 * vnM:(Answer string) - this is so that we can later extract which node voltage we are asking for
	 * Remember, it's colon separated
	 */
	public ArrayList<String> MapleTANodeVoltageOut()
	{
		//every one of these functions is going to use the same setup, but it requires the setup of a lot
		//of arrayLists, so I cannot imagine how to do this easily.  Well, I'm obviously a procedural person
		ArrayList<String> outList = new ArrayList<String>();
		String setupOut = ""; //these are the variables that come out
		ArrayList<String> setupList = setupString();
		setupOut = setupList.get(0);
		int numUnknowns = Integer.parseInt(setupList.get(1));
		ArrayList<String> unknowns = new ArrayList<String>();
		for(int i = 2; i < setupList.size(); i++)
		{
			unknowns.add(setupList.get(i));
		}
 		
		outList.add(setupOut);
		
		//now make a new string for each variable we want to solve for
		for(int i = 1; i <= numUnknowns; i++)
		{
			outList.add(unknowns.get(i-1) + ":$answer=maple(\"fsolve($Soln[" + i + "])\");@");//@"; //Um, I'm pretty sure this is right
		}
		return outList;
	}
	
	//generate MapleTA output.  NOTE: This needs to be its own class, with lots more options
	//TODO: Make this a better class
	public String MapleTAOut()
	{
		String MapleOut = "";
		
		//generate the variables: devices
		for (Device d : devices)
		{
			MapleOut += d.outputDeviceVariable(1, 10) + "\n";
		}
		
		//generate the variables: nodes
		for (Node n : nodes)
		{
			if(n.known == true)
			{
				MapleOut += n.voltage + "=" + n.eqn + ";\n";
			}
		}
		
		
		//generate the string that will house the equations
		String eqString = "{";
		for (Node n : nodes)
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
		for(Node n : nodes)
		{
			if(n.known == false)
			{
				unknownString += n.voltage + ", ";
			}
		}
		//again, remove those last two characters
		unknownString = unknownString.substring(0,unknownString.length()-2);
		unknownString += "}";
		
		MapleOut += "$Soln=maple(\"solve("+eqString +"," + unknownString + ")\");\n";
		MapleOut += "$answer=maple(\"fsolve($Soln[1])\");@"; //Um, I'm pretty sure this is right
		//TODO: the above line just finds the solution to one of the node voltages. We just want to see if this works.  We'll generalize
		//later
		return MapleOut;
	}
}
