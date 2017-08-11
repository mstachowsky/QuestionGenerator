package questionGenerator;

//This class describes output variables from LTSpice
public class OutputVariable {
	//a variable has a name, a value, and either a node (for voltages) or a device (for some
	//voltages and all currents)
	
	/*
	 * TODO: make this work for transient analyses - this would mean that the 
	 * values below must be an array or something
	 */
	
	public String name;
	public String type;
	public double value;
	public int node;
	public String deviceName;
	public OutputVariable()
	{
		this.name = "";
		this.value = 0;
		this.node = -1; //must begin at -1 since this may not get changed but we still search for
		//equality on nodes
		this.deviceName = "none";
		this.type = "none";
	}
	
	//gets the name from a line in the file
	public void getName(String s)
	{
		String[] Data = s.split("\t");
		this.name = Data[2]; //Data[0] is...something, Data[1] is the variable number
		//so Data[2] is the name
	}
	
	//gets the value from a line in the file.  Assumes that the string contains ONLY the double,
	//not anything else
	public void getValue(String s)
	{
		this.value = Double.parseDouble(s);
	}
	
	//extracts the node number from a voltage
	public void setNode()
	{
		//We need to extract the integer between parentheses
		this.node = Integer.parseInt(this.name.substring(this.name.indexOf('(') + 1,this.name.indexOf(')'))); //for some reason we get the splitting characters
	}

	//extracts the device name from a voltage
	public void setDevice()
	{
		//We need to extract the string between parentheses
		this.deviceName = this.name.substring(this.name.indexOf('(') + 1,this.name.indexOf(')')); //for some reason we get the splitting characters
	}

	
	//gets a line and parses it appropriately into a name and either a node or device
	public void inputDescriptorData(String s)
	{
		//get the name first, since that's always important
		this.getName(s);
		
		//now, if it is a NODE voltage, it will start with capital V
		if(this.name.startsWith("V"))
		{
			this.setNode(); //thus we can get the node
			this.type = "voltage";
		}
		else if(this.name.startsWith("I"))
		{
			this.setDevice();
			this.type = "current";
		}
		else //for now, I believe this means it is a voltage source voltage
		{
			//voltage sources are named Vn, not vn, so we need to convert to upper case
			this.deviceName = this.name.toUpperCase();
			this.type ="Voltage Source";
		}
	}
	
	public void inputValue(String s)
	{
		//trim it and parse
		this.value = Double.parseDouble(s.trim());
	}
	
	public String toString()
	{
		String s = "";
		
		s += "Name: " + this.name + "\n";
		
		s += "Value: " + this.value + "\n";
		
		if(this.node != -1)
			s += "Node: " + this.node + "\n";
		else
			s += "Device: " + this.deviceName + "\n";
		
		return s;
	}
	
	
	
}
