class flight {
  String id;
  int maxdelay=0;
  int delays[]=null;

  public flight (String id) {
    this.id=id;
    delays=new int[1440];
    for (int i=0; i<delays.length; i++)
      delays[i]=0;
  }
}

public class Main {



  public static void main(String[] args) {
    System.out.println("Hello World!");
  }

}
