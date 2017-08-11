package questionGenerator;

import java.util.ArrayList;

import symjava.symbolic.Expr;

public class Series {
	
	//A Series connection has a generic list of Devices that is ALL things in series (to simplify
	//coding really), then it will have resistors, caps, etc. as separate Device arrayLists.
	public ArrayList<Device> devices;
	//for now, only these two types
	public ArrayList<Device> resistors;
	public ArrayList<Device> voltageSources;
	
	//identifying stuff
	public int id;
	public int leftNode;
	public int rightNode;
	
	//equivalent components: all evaluated as symbolic expressions
	public Expr ReqExpr;
	public Device Req;
	public Expr VeqExpr;
	public Device Veq;
	
	public Series(ArrayList<Device> d, int i)
	{
		this.id = i; //need a unique ID for when putting this all back together as a circuit
		this.devices = new ArrayList<Device>();
		this.devices.addAll(d);
		this.resistors = new ArrayList<Device>();
		this.voltageSources = new ArrayList<Device>();
		fillResistors();
		fillVoltageSources();
		updateNodes();
		findVeq();
		findReq();
	}
	
	public void updateNodes()
	{
		this.leftNode = 0; //always.  The hard part is rightNode
		rightNode = 0; //just keep adding 1 depending on how many component types we have
		if(this.voltageSources.size() != 0)
		{
			rightNode++;
		}
		if(this.resistors.size() != 0)
			rightNode++;
	}
	
	public void fillResistors()
	{
		for(int i = 0; i < devices.size(); i++)
		{
			if(devices.get(i).type.equals("Resistor"))
				resistors.add(devices.get(i));
		}
	}
	
	public void fillVoltageSources()
	{
		for(int i = 0; i < devices.size(); i++)
		{
			if(devices.get(i).type.equals("Voltage Source"))
				voltageSources.add(devices.get(i));
		}
	}
		
	//use symjava to add them all up
	public void findReq()
	{
		ReqExpr = 0;
		for(int i = 0; i < resistors.size(); i++)
		{
			ReqExpr = ReqExpr + resistors.get(i).sym;
		}
		//now, actually create Req
		if(resistors.size() != 0)
		{
			//there is an Req to make.  Resolve the nodes!
			int n1 = 0;
			if(voltageSources.size() != 0)
			{
				n1 = 1;
			}
			int n2 = n1 + 1;
			this.Req = new Device("Req" + id, "Resistor",n1,n2);
		}
		
	}
	
	//same as Req
	public void findVeq()
	{
		VeqExpr = 0;
		for(int i = 0; i < voltageSources.size(); i++)
		{
			VeqExpr = VeqExpr + voltageSources.get(i).sym;
		}
		//now actually create Veq
		if(voltageSources.size() != 0)
		{
			this.Veq = new Device("Veq" + id,"Voltage Source",0,1); //voltage sources are easy
		}
	}
	
	public String toString()
	{
		String s = "";
		for(int i = 0; i < devices.size(); i++)
		{	s += devices.get(i).name + " ";
		}
		s += "Req: " + Req.toString() + " Veq: " + Veq.toString();
		return s;
	}
	
}
