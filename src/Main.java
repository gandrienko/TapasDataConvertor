import java.io.*;
import java.util.Hashtable;

class Flight {
  String id;
  int maxdelay=0;
  int delays[]=null;

  public Flight (String id) {
    this.id=id;
    delays=new int[1440];
    for (int i=0; i<delays.length; i++)
      delays[i]=0;
  }
}

public class Main {

  protected static Hashtable<String,Integer> readCapacities(String fname) {
    Hashtable<String,Integer> capacities=new Hashtable(100);
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fname+".csv")))) ;
      String strLine;
      try {
        br.readLine();
        while ((strLine = br.readLine()) != null) {
          String str=strLine.replaceAll(" ","");
          String[] tokens=str.split(",");
          String s=tokens[0];
          Integer capacity=Integer.valueOf(tokens[1]);
          capacities.put(s,capacity);
        }
        br.close();
      } catch  (IOException io) {}
    } catch (FileNotFoundException ex) {System.out.println("problem reading file "+fname+" : "+ex);}
    return capacities;
  }

  protected static Hashtable<String,Flight> readFlights (String fname) {
    Hashtable<String,Flight> flights=new Hashtable(1000);
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fname+".csv")))) ;
      String strLine;
      try {
        br.readLine();
        while ((strLine = br.readLine()) != null) {
          String str=strLine.replaceAll(" ","");
          String[] tokens=str.split(",");
          String id=tokens[1];
          int step=Integer.valueOf(tokens[2]), delay=Integer.valueOf(tokens[3]);
          Flight flight=new Flight(id);
          flight.delays[step]=delay;
          if (flight.maxdelay<delay)
            flight.maxdelay=delay;
          flights.put(id,flight);
        }
        br.close();
      } catch  (IOException io) {}
    } catch (FileNotFoundException ex) {System.out.println("problem reading file "+fname+" : "+ex);}
    return flights;
  }

  protected static void readFlightPlans (String fname, String fnOutput) {
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fname+".csv")))) ;
      FileOutputStream fos = new FileOutputStream(new File(fnOutput+"Tests20210310a.csv"));
    	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
      String strLine;
      try {
        String header=br.readLine();
        String[] columns=header.split(",");
        bw.write("FLIGHTID,STEP,DELAY,SECTOR,ENTRYTIME,EXITTIME,ENTRYTIMEN,EXITTIMEN,FROMSECTOR,TOSECTOR\n");
        while ((strLine = br.readLine()) != null) {
          String str=strLine.replaceAll(" ","");
          String[] tokens=str.split(",");
          String id=tokens[1];
          //int step=Integer.valueOf(tokens[2]), delay=Integer.valueOf(tokens[3]);
          //Flight flight=new Flight(id);
        }
        br.close();
        bw.close();
      } catch  (IOException io) {}
    } catch (FileNotFoundException ex) {System.out.println("problem reading file "+fname+" : "+ex);}
  }

  public static void main(String[] args) {
    System.out.println("Hello World!");
    String fnCapacities="C:\\CommonGISprojects\\tracks-avia\\TAPAS\\ATFCM-20210331\\0_delays\\scenario_20190801_capacities",
           fnFlights="C:\\CommonGISprojects\\tracks-avia\\TAPAS\\ATFCM-20210331\\0_delays\\scenario_20190801_exp0_snapshots",
           fnFlightPlans="C:\\CommonGISprojects\\tracks-avia\\TAPAS\\ATFCM-20210331\\0_delays\\scenario_20190801_exp0_baseline_flight_plans",
           fnOutput="C:\\CommonGISprojects\\tracks-avia\\TAPAS\\ATFCM-20210331\\0_delays\\output";
    Hashtable<String,Integer> capacities=readCapacities(fnCapacities);
    Hashtable<String,Flight> flights=readFlights(fnFlights);
    readFlightPlans(fnFlightPlans,fnOutput);
  }

}
