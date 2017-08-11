package questionGenerator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class questionGenerator {

	public static void main(String[] args) throws IOException {
		// TODO Seriously move all of this stuff out of main...
		
		//this begins by creating the device list (without referencing the solution)
		ArrayList<Device> devices = new ArrayList<Device>(); //everything is array lists.
		
		//TODO: make this work with the NIO library instead of bufferedReader
		//open a file
		//file name is absolute path for now.  Again, yes this will be fixed.
		String netFileName = "C:\\Users\\Mike\\Dropbox\\GENE123\\detector\\analyzedCircuits\\circuit2_bounded3.txt";
		String solutionFileName = "C:\\Users\\Mike\\Dropbox\\GENE123\\detector\\analyzedCircuits\\circuit2.raw";
		 String line = "";
         BufferedReader in;

         in = new BufferedReader(new FileReader(netFileName));
         while(line != null && !line.startsWith(".end"))
         {
                line = in.readLine();
                //this feels like an anti-pattern...
                if(!line.startsWith(".end"))
                {
	                Device d = new Device();
	                d.parseDeviceData(line);
	                devices.add(d);
                }
         }
                 
         in.close();//always close your files!
         
         //next parse the variables - we actually don't need to do any of this...it's all symbolic now.
         in = new BufferedReader(new FileReader(solutionFileName));
         
         line = "";
         
         //create the Variables ArrayList
         ArrayList<OutputVariable> variables = new ArrayList<OutputVariable>();
         //there is a header that we need to skip
         while(line != null && !line.startsWith("Variables"))
         {
        	 line = in.readLine();
         }
         //OK, at this point, line is "Variables".  We may now begin parsing variables
         line = in.readLine();
         while(!line.startsWith("Values"))
         {
        	 OutputVariable v = new OutputVariable();
        	 v.inputDescriptorData(line);
        	 variables.add(v);
        	 line = in.readLine();
         }
         
         //At this point line is "Values".  Now, the irritating thing is that
         //the first entry of the first line of the values is always a time stamp, so we need to get
         //a substring
         for(int i = 0; i < variables.size(); i++)
         {
	         line=in.readLine();
	         line = line.substring(line.indexOf('\t')); //this SHOULD work...I think
	         variables.get(i).inputValue(line);
         }

         //always close your files!
         in.close();
         
         /*
          * Due to how LTSpice operates, some node voltages are specified in terms of voltage source
          * voltages instead.  So, the OutputVariable corresponding to that node will not have its
          * node filled in (thank you, LTSpice, I guess).  Thus we have to resolve nodes.
          */
         
         for(int i = 0; i < variables.size(); i++)
         {
        	 if(variables.get(i).type.equals("Voltage Source"))
        	 {
        		 for(int j = 0; j < devices.size(); j++)
        		 {
        			 if(variables.get(i).deviceName.equals(devices.get(j).name))
        			 {
        				 variables.get(i).node = devices.get(j).node1;
        			 }
        		 }
        	 }
         }
         
         
         //Next step is to set the variables for each device
         for(int i = 0; i < devices.size(); i++)
         {
        	 //search for relevant variables in the variables array
        	 for(int j = 0; j < variables.size(); j++)
        	 {
        		 OutputVariable v = variables.get(j);
        		 //check current
        		 if(v.type.equals("current"))
        		 {
        			 if(v.deviceName.equals(devices.get(i).name))
        			 {
        				 devices.get(i).current = v.value;
        			 }
        		 }
        		 else //it's a voltage
        		 {
        			 if(v.node == devices.get(i).node1)
        			 {
        				 devices.get(i).voltage1 = v.value;
        			 }
        			 else if(v.node == devices.get(i).node2)
        			 {
        				 devices.get(i).voltage2 = v.value;
        			 }
        			 else if(v.deviceName.equals(devices.get(i).name))
        			 {
        				 devices.get(i).voltage1 = v.value;
        			 }
        		 }
        	 }
        	 //update the device's power
        	 devices.get(i).updatePower();
         }
 
         //Now we generate the problems themselves
         //open the file to write to
         String outFileName = "C:\\Users\\Mike\\Dropbox\\GENE123\\detector\\questionsOut.txt";
         
         //open the buffered writer
         //TODO: move to the NIO framework
         BufferedWriter bw;
         bw = new BufferedWriter(new FileWriter(outFileName));
         
         //generate the easy ones first
         
         //voltages and currents through each component
         //TODO: make this output in just about every format imaginable
         for(int i = 0; i < devices.size();i++){
        	 bw.write("Q: ");
        	 bw.write("What is the voltage across ");
        	 bw.write(devices.get(i).name);
        	 bw.write("\nA: ");
        	 bw.write("" + (devices.get(i).voltage1 - devices.get(i).voltage2) + "\n");
         }
         
         //always close your files!
         bw.close();
         System.out.println("Question generation done!");
         
         //I know this should all be different functions and perhaps different classes.
         
         //Generate the circuit object and its simplifications
         Circuit cr = new Circuit();
         cr.initDevices(devices); //input the devices, obs
         cr.initNodes();
         cr.generateDeviceCurrents();
         cr.generateNodalEquations();
         System.out.println(cr.MapleTAOut());
         /*System.out.println("Nodes: ");
         for(int i = 0; i < cr.nodes.size(); i++)
         {
        	 System.out.println("" + cr.nodes.get(i).nodeNum + " " + cr.nodes.get(i).voltage + " " + cr.nodes.get(i).eqn + " " + cr.nodes.get(i).known);
         }*/
         //cr.findSeries();
        // cr.printSeries();
         
         
	}

}
