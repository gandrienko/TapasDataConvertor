import java.io.*;
import java.util.Hashtable;
import java.util.Vector;

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

class Record {
  String Sector, EntryTime, ExitTime, FromSector, ToSector;
  int EntryTimeN, ExitTimeN;
  protected int calc(String s) {
    int n=Integer.valueOf(s.substring(0,2)).intValue()*60+Integer.valueOf(s.substring(3,5)).intValue();
    return n;
  }
  public void calc() {
    EntryTimeN=calc(EntryTime);
    ExitTimeN=calc(ExitTime);
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
    int N=0;
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
          Flight flight=flights.get(id);
          if (flight==null)
            flight=new Flight(id);
          flight.delays[step]=delay;
          if (flight.maxdelay<delay)
            flight.maxdelay=delay;
          flights.put(id,flight);
          N++;
          if (N % 100000 == 0)
            System.out.println("* snapshots: "+N+" lines processed, "+flights.size()+" flights recorded");
        }
        br.close();
        System.out.println("* snapshots: "+N+" lines processed, "+flights.size()+" flights recorded");
      } catch  (IOException io) {}
    } catch (FileNotFoundException ex) {System.out.println("problem reading file "+fname+" : "+ex);}
    return flights;
  }

  protected static int countCommas (String str) {
    int n=0;
    for (int i=0; i<str.length(); i++)
      if (str.charAt(i)==',')
        n++;
    return n;
  }

  protected static void readFlightPlans (String fname, String fnOutput, Hashtable<String,Flight> flights) {
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fname+".csv")))) ;
      FileOutputStream fos = new FileOutputStream(new File(fnOutput+".csv"));
    	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
      String strLine;
      int N=0, M=0, K=0;
      try {
        String header=br.readLine();
        int columnFlightID=countCommas(header.substring(0,header.indexOf("FlightID"))),
            columnDelays=countCommas(header.substring(0,header.indexOf("Delays"))),
            columnSector=countCommas(header.substring(0,header.indexOf("Sector_0"))),
            columnEntryTime=countCommas(header.substring(0,header.indexOf("EntryTime_0")));
        String[] columns=header.split(",");
        bw.write("FLIGHTID,STEP,DELAY,SECTOR,ENTRYTIME,EXITTIME,ENTRYTIMEN,EXITTIMEN,FROMSECTOR,TOSECTOR\n");
        Flight flight=null;
        String flightID=null;
        while ((strLine = br.readLine()) != null) {
          String str=strLine.replaceAll(" ","");
          String[] tokens=str.split(",");
          String id=tokens[columnFlightID];
          if (!id.equals(flightID)) { // search in hashtable only if a new flight
            flight=flights.get(id);
            flightID=id;
            M++;
          }
          int delay=Integer.valueOf(tokens[columnDelays]);
          if (delay<=flight.maxdelay) {
            //if (delay>0)
              //System.out.println("* delay="+delay);
            // 1. parse record into a sequence of sectors with times
            Vector<Record> vr=new Vector<Record>(columnEntryTime-columnSector);
            for (int i=columnSector; i<columnEntryTime; i++)
              if (tokens[i].equals("NULL") || tokens[i].equals("NONE"))
                ;
              else {
                Record r=new Record();
                r.Sector=tokens[i];
                if (i>columnSector)
                  r.FromSector=tokens[i-1];
                else
                  r.FromSector="NULL";
                if (i<columnEntryTime)
                  r.ToSector=tokens[i+1];
                else
                  r.ToSector="NULL";
                r.EntryTime=tokens[columnEntryTime+i-columnSector];
                r.ExitTime=tokens[1+columnEntryTime+i-columnSector];
                r.calc();
                vr.add(r);
              }
            // 2. output for all steps with the same delay
            for (int step = 0; step < flight.delays.length; step++)
              if (flight.delays[step] == delay)
                for (Record r:vr) {
                  //bw.write("FLIGHTID,STEP,DELAY,SECTOR,ENTRYTIME,EXITTIME,ENTRYTIMEN,EXITTIMEN,FROMSECTOR,TOSECTOR\n");
                  bw.write(flight.id+","+step+","+delay+","+r.Sector+","+r.EntryTime+","+r.ExitTime+","+r.EntryTimeN+","+r.ExitTimeN+","+r.FromSector+","+r.ToSector+"\n");
                  K++;
                }
          }
          N++;
          if (N % 200 == 0)
            System.out.println("* flights: "+M+" flights in "+N+" snapshot lines processed, "+K+" outputs recorded");
        }
        br.close();
        bw.close();
        System.out.println("* flights: "+M+" flights in "+N+" snapshot lines processed, "+K+" outputs recorded");
      } catch  (IOException io) {}
    } catch (FileNotFoundException ex) {System.out.println("problem reading file "+fname+" : "+ex);}
  }

  protected static void readFlightPlans (String fname, String fnOutput, Hashtable<String,Flight> flights, int step) {
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fname+".csv")))) ;
      FileOutputStream fos = new FileOutputStream(new File(fnOutput+".csv"));
    	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
      String strLine;
      int N=0, M=0, K=0;
      try {
        String header=br.readLine();
        int columnFlightID=countCommas(header.substring(0,header.indexOf("FlightID"))),
            columnDelays=countCommas(header.substring(0,header.indexOf("Delays"))),
            columnSector=countCommas(header.substring(0,header.indexOf("Sector_0"))),
            columnEntryTime=countCommas(header.substring(0,header.indexOf("EntryTime_0")));
        String[] columns=header.split(",");
        bw.write("FLIGHTID,STEP,DELAY,SECTOR,ENTRYTIME,EXITTIME,ENTRYTIMEN,EXITTIMEN,FROMSECTOR,TOSECTOR\n");
        Flight flight=null;
        String flightID=null;
        while ((strLine = br.readLine()) != null) {
          String str=strLine.replaceAll(" ","");
          String[] tokens=str.split(",");
          String id=tokens[columnFlightID];
          if (!id.equals(flightID)) { // search in hashtable only if a new flight
            flight=flights.get(id);
            flightID=id;
            M++;
          }
          int delay=Integer.valueOf(tokens[columnDelays]);
          if (delay==flight.delays[step]) {
            //if (delay>0)
              //System.out.println("* delay="+delay);
            // 1. parse record into a sequence of sectors with times
            Vector<Record> vr=new Vector<Record>(columnEntryTime-columnSector);
            for (int i=columnSector; i<columnEntryTime; i++)
              if (tokens[i].equals("NULL") || tokens[i].equals("NONE"))
                ;
              else {
                Record r=new Record();
                r.Sector=tokens[i];
                if (i>columnSector)
                  r.FromSector=tokens[i-1];
                else
                  r.FromSector="NULL";
                if (i<columnEntryTime)
                  r.ToSector=tokens[i+1];
                else
                  r.ToSector="NULL";
                r.EntryTime=tokens[columnEntryTime+i-columnSector];
                r.ExitTime=tokens[1+columnEntryTime+i-columnSector];
                r.calc();
                vr.add(r);
              }
            // 2. output for all steps with the same delay
            for (Record r:vr) {
              //bw.write("FLIGHTID,STEP,DELAY,SECTOR,ENTRYTIME,EXITTIME,ENTRYTIMEN,EXITTIMEN,FROMSECTOR,TOSECTOR\n");
              bw.write(flight.id+","+step+","+delay+","+r.Sector+","+r.EntryTime+","+r.ExitTime+","+r.EntryTimeN+","+r.ExitTimeN+","+r.FromSector+","+r.ToSector+"\n");
              K++;
            }
          }
          N++;
          if (N % 200 == 0)
            System.out.println("* flights: "+M+" flights in "+N+" snapshot lines processed, "+K+" outputs recorded");
        }
        br.close();
        bw.close();
        System.out.println("* flights: "+M+" flights in "+N+" snapshot lines processed, "+K+" outputs recorded");
      } catch  (IOException io) {}
    } catch (FileNotFoundException ex) {System.out.println("problem reading file "+fname+" : "+ex);}
  }

  protected static void readSolution (String fname, String fnOutput, int step) {
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fname+".csv")))) ;
      FileOutputStream fos = new FileOutputStream(new File(fnOutput+".csv"));
    	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
      String strLine;
      int N=0, M=0, K=0;
      try {
        String header=br.readLine();
        int columnFlightID=countCommas(header.substring(0,header.indexOf("FlightID"))),
            columnDelays=countCommas(header.substring(0,header.indexOf("Delays"))),
            columnSector=countCommas(header.substring(0,header.indexOf("Sector_0"))),
            columnEntryTime=countCommas(header.substring(0,header.indexOf("EntryTime_0")));
        //String[] columns=header.split(",");
        bw.write("FLIGHTID,STEP,DELAY,SECTOR,ENTRYTIME,EXITTIME,ENTRYTIMEN,EXITTIMEN,FROMSECTOR,TOSECTOR\n");
        //Flight flight=null;
       // String flightID=null;
        while ((strLine = br.readLine()) != null) {
          String str=strLine.replaceAll(" ","");
          String[] tokens=str.split(",");
          String id=tokens[columnFlightID];
          int delay=Integer.valueOf(tokens[columnDelays]);
          // 1. parse record into a sequence of sectors with times
          Vector<Record> vr=new Vector<Record>(columnEntryTime-columnSector);
          for (int i=columnSector; i<columnEntryTime; i++)
            if (tokens[i].equals("NULL") || tokens[i].equals("NONE"))
              ;
            else {
              Record r=new Record();
              r.Sector=tokens[i];
              if (i>columnSector)
                r.FromSector=tokens[i-1];
              else
                r.FromSector="NULL";
              if (i<columnEntryTime)
                r.ToSector=tokens[i+1];
              else
                r.ToSector="NULL";
              r.EntryTime=tokens[columnEntryTime+i-columnSector];
              r.ExitTime=tokens[1+columnEntryTime+i-columnSector];
              r.calc();
              vr.add(r);
            }
          // 2. output for all steps with the same delay
          for (Record r:vr) {
            //bw.write("FLIGHTID,STEP,DELAY,SECTOR,ENTRYTIME,EXITTIME,ENTRYTIMEN,EXITTIMEN,FROMSECTOR,TOSECTOR\n");
            bw.write(id+","+step+","+delay+","+r.Sector+","+r.EntryTime+","+r.ExitTime+","+r.EntryTimeN+","+r.ExitTimeN+","+r.FromSector+","+r.ToSector+"\n");
            K++;
          }
          N++;
          if (N % 200 == 0)
            System.out.println("* flights: "+M+" flights in "+N+" snapshot lines processed, "+K+" outputs recorded");
        }
        br.close();
        bw.close();
        System.out.println("* flights: "+M+" flights in "+N+" snapshot lines processed, "+K+" outputs recorded");
      } catch  (IOException io) {}
    } catch (FileNotFoundException ex) {System.out.println("problem reading file "+fname+" : "+ex);}
  }

  public static void main(String[] args) {
    //System.out.println("Hello World!");
    String fnCapacities="C:\\CommonGISprojects\\tracks-avia\\TAPAS\\ATFCM-20210331\\0_delays\\scenario_20190801_capacities",
           fnFlights="C:\\CommonGISprojects\\tracks-avia\\TAPAS\\ATFCM-20210331\\0_delays\\scenario_20190801_exp0_snapshots",
           fnFlightPlans="C:\\CommonGISprojects\\tracks-avia\\TAPAS\\ATFCM-20210331\\0_delays\\scenario_20190801_exp0_baseline_flight_plans",
           fnOutput="C:\\CommonGISprojects\\tracks-avia\\TAPAS\\ATFCM-20210331\\0_delays\\output_step0000";
    //Hashtable<String,Integer> capacities=readCapacities(fnCapacities);
    //System.out.println("* capacities: ready");
    Hashtable<String,Flight> flights=readFlights(fnFlights);
    System.out.println("* flights: ready");
    readFlightPlans(fnFlightPlans,fnOutput,flights,0);
    System.out.println("* flight plans: processed");

/*
    fnFlightPlans="C:\\CommonGISprojects\\tracks-avia\\TAPAS\\ATFCM-20210331\\0_delays\\scenario_20190801_exp0_solution";
    fnOutput="C:\\CommonGISprojects\\tracks-avia\\TAPAS\\ATFCM-20210331\\0_delays\\output_solution";
    readSolution(fnFlightPlans,fnOutput,1439);
*/

  }

}
