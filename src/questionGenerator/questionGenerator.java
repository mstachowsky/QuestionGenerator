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
         //Now we generate the problems themselves
         
         //I know this should all be different functions and perhaps different classes.
         
         //Generate the circuit object and its simplifications
         Circuit cr = new Circuit();
         cr.initDevices(devices); //input the devices, obs
         cr.initNodes();
         cr.generateDeviceCurrents();
         cr.generateNodalEquations();
         cr.initAlgorithm();
         
         ArrayList<String> als = new ArrayList<String>();
         als = cr.MapleTADeviceVoltageOut(); 
         
         
         for(String s : cr.Algorithm.mapleTA)
         {
        	 System.out.println(s);
         }
         
         //TODO: Make this into an intelligent question
         int topicNum = 1;
         int qNum = 1;
         String pre = "qu.";
         //the algorthm variables
         String vars = als.get(0);
         
         //the common things
         String mode = ".mode=Numeric@\n";
         String editing = ".editing=useHTML@\n";
         String ans = ".answer.num=$answer@\n";
         String units = ".answer.units=@\n";
         String show = ".showUnits=false@\n";
         
         //now we need to start at 1, since als(0) is the common variable string

         //open the buffered writer
         //TODO: move to the NIO framework
         //open the file to write to
         String outFileName = "C:\\Users\\Mike\\Dropbox\\GENE123\\mapleTA\\questionsOut.qu";
         BufferedWriter bw;
         bw = new BufferedWriter(new FileWriter(outFileName));
         
         ///the first line in the file should be the topic number
         bw.write(pre + topicNum + ".topic=Voltage acrosss devices@\n\n");
         
         for(int i = 1; i < als.size(); i++)
         {
        	 String tmp = pre + topicNum + "." + qNum;
        	 String qString = tmp + ".question=What is the voltage across ";
        	 //extract the device name from the answer string
        	 String[] ansString = als.get(i).split(":");
        	 qString += ansString[0] + " $answer @\n";
        	 qString += tmp + ".name=Device Voltage " + i + "@\n"; //TODO: this naming needs to be smarter, probably include the circuit name
        	 qString += tmp + mode;
        	 qString += tmp + editing;
        	 
        	 //tolerance.
        	 //TODO: make this a user-settable parameter
        	 qString += tmp + ".grading=toler_perc@\n";
        	 qString += tmp + ".perc=2@\n"; //2% default for now
        	 
        	 //now fill in the algorithm
        	 qString += tmp + ".algorithm=" + vars + ansString[1] + "\n";
        	 
        	 //now finish up
        	 qString += tmp + ans;
        	 qString += tmp + units;
        	 qString += tmp + show;
        	 
        	 bw.write(qString);
        	 bw.write("\n"); //just so it's easier to see the difference between questions
        	 //next question
        	 qNum++;
         }
         bw.close();
         System.out.println("Done generating file");
	}
	
	
}
