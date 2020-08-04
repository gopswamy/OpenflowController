import java.util.HashMap;

public class Port extends HashMap<String, Port> {
    String name;
    String Hw_addr;
    String number;
    String dp_ID;
    public Port(String dp_ID,String name,String Hw_addr,String number){
        this.name = name;
        this.Hw_addr = Hw_addr;
        this.number = number;
        this.dp_ID = dp_ID;


        System.out.println("DP_ID is "+dp_ID + " Port name " + name + " Hw_addr " + Hw_addr + " Port_number " + number );
    }
}
