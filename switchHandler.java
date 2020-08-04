import java.io.*;
import java.net.Socket;
import java.util.*;
import java.lang.*;

/*
 * switchHandler.java

 * Revisions:
 *     Final
 */

/**
 * Thread program handling the individual connection
 *
 * @author Gopinath Swaminathan    .
 */

public class switchHandler extends Thread {
    InputStream is;
    OutputStream os;
    Socket s;
    private HashMap<String, String> hmap2 = new HashMap<>();
    private static HashMap<String, HashMap<String, String>> hmap = new HashMap<String, HashMap<String, String>>();
    private static HashMap<String, HashMap<String, String>> hmap1 = new HashMap<String, HashMap<String, String>>();
    private HashMap<String, String> hmap3 = new HashMap<>();

    //Constructor
    public switchHandler(Socket s) throws IOException {
        this.s = s;
        this.is = new DataInputStream(s.getInputStream());
        this.os = new DataOutputStream(s.getOutputStream());
    }


    public void run() {
        String dp_id = "";
        Port test = null;
        try {
            //read till data is available
            while (true) {
                if (is.available() > 0) {
                    byte[] array = new byte[is.available()];

                    is.read(array);
                    StringBuilder sb = new StringBuilder();
                    int i = 0;
                    for (byte b : array) {
                        sb.append(String.format("%02x", b & 0xff));
                        i++;
                    }
                    //process hello
                    //System.out.println(sb.toString());
                    if (sb.subSequence(2, 4).equals("00")) {

                        //respond hello
                        os.write(new byte[]{4, 0, 0, 8, 0, 0, 1, (byte) 34});
                        os.flush();

                        //respond Feature Request
                        os.write(new byte[]{4, 5, 0, 8, (byte) 255, (byte) 255, (byte) 255, (byte) 254});
                        os.flush();
                    }

                    //process Feature Reply
                    else if (sb.subSequence(2, 4).equals("06")) {
                        dp_id = sb.subSequence(16, 32).toString();
                        //System.out.println(dp_id);
                        os.write(new byte[]{4, 18, 0, 16, 0, 0, 0, 30, 0, 13, 0, 0, 0, 0, 0, 0,});
                        os.flush();
                    } else if (sb.subSequence(2, 4).equals("13")) {
                        // System.out.println(sb.toString().length());
                        int count_port = (sb.length() - 32) / 128;
                        int idx = 0;
                        int start = 32;
                        int end = 128;
                        String name = "";
                        //System.out.println(sb.toString());
                        // System.out.println(count_port);
                        while (idx < count_port) {

                            String port = sb.subSequence(start, start + 8).toString();
                            if (port.equals("fffffffe")) {
                                port = "Local";
                            }
                            // System.out.println(port);
                            //System.out.println(sb.subSequence(start, start+8));


                            String hw_addr = sb.subSequence(start + 16, start + 28).toString();
                            //System.out.println(sb.subSequence(start+16, start+28));
                            //System.out.println(hw_addr);

                            //System.out.println(sb.subSequence(start+32, start+64));
                            int temp_start = start;
                            while (!sb.subSequence(temp_start + 32, temp_start + 34).equals("00")) {
                                int val = Integer.parseInt(sb.subSequence(temp_start + 32, temp_start + 34).toString(), 16);
                                char ch = (char) val;
                                name += ch;
                                temp_start += 2;

                            }
                            // System.out.println(name);

                            test = new Port(dp_id, name, hw_addr, port);

                            hmap3.put(test.Hw_addr, test.name);
                            hmap1.put(dp_id, hmap3);
                            hmap2.put(test.Hw_addr, test.number);
                            hmap.put(dp_id, hmap2);

                            //System.out.println(hmap);
                            //System.out.println(hmap1);

                            name = "";

                            start += 128;
                            end += 128;
                            idx++;
                        }
                        String port1 = "";
                        String port2 = "";
                        for (Map.Entry<String, String> entry : hmap2.entrySet()) {
                            if (entry.getValue().endsWith("1")) {
                                port1 = entry.getKey();
                            } else if (entry.getValue().endsWith("2")) {
                                port2 = entry.getKey();
                            }
                        }
                        //System.out.println(port1);
                        //System.out.println(port2);

                        byte[] portOne = new byte[6];
                        byte[] portTwo = new byte[6];
                        byte[] dp = new byte[8];

                        int id = 0;
                        for (int j = 4; j < dp_id.length(); j += 2) {
                            StringBuilder s1 = new StringBuilder();
                            s1.append(dp_id.charAt(j));
                            s1.append(dp_id.charAt(j + 1));
                            int val = Math.abs(Integer.parseInt(s1.toString(), 16));
                            dp[id++] = (byte) val;
                        }
                        // System.out.println(Arrays.toString(dp));


                        int byteval = 0;
                        for (int j = 0; j < port1.length(); j += 2) {
                            StringBuilder s1 = new StringBuilder();
                            s1.append(port1.charAt(j));
                            s1.append(port1.charAt(j + 1));
                            int val = Math.abs(Integer.parseInt(s1.toString(), 16));
                            portOne[byteval++] = (byte) val;
                        }
                        //System.out.println(Arrays.toString(portOne));

                        byteval = 0;
                        for (int j = 0; j < port2.length(); j += 2) {
                            StringBuilder s1 = new StringBuilder();
                            s1.append(port2.charAt(j));
                            s1.append(port2.charAt(j + 1));
                            int val = Math.abs(Integer.parseInt(s1.toString(), 16));
                            portTwo[byteval++] = (byte) val;
                        }
                        //System.out.println(Arrays.toString(portTwo));


                        byte[] out = new byte[]{4, 13, 0, 115, 0, 0, 0, 87, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, 0, 16,
                                0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, (byte) 128, (byte) 194, 0, 0, 14, portOne[0], portOne[1], portOne[2], portOne[3], portOne[4], portOne[5],
                                (byte) 136, (byte) 204, 2, 7, 4, dp[0], dp[1], dp[2], dp[3], dp[4], dp[5], 4, 3, 2, 0, 1, 6, 2, 0, 120, (byte) 254, 12, 0, 38, (byte) 225, 0,
                                0, 0, 0, 0, 0, 0, 0, dp[5], 24, 8, 1, (byte) 186, 16, (byte) 179, 22, (byte) 171, 43, 53, (byte) 230, 1, 1, (byte) 254, 12, 0, 38, (byte) 225, 1, 0, 0, 1, 109,
                                (byte) 240, (byte) 184, 105, 79, 0, 0};

                        os.write(out);
                        os.flush();


                        out = new byte[]{4, 13, 0, 115, 0, 0, 0, 87, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, 0, 16,
                                0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 1, (byte) 128, (byte) 194, 0, 0, 14, portTwo[0], portTwo[1], portTwo[2], portTwo[3], portTwo[4], portTwo[5],
                                (byte) 136, (byte) 204, 2, 7, 4, dp[0], dp[1], dp[2], dp[3], dp[4], dp[5], 4, 3, 2, 0, 2, 6, 2, 0, 120, (byte) 254, 12, 0, 38, (byte) 225, 0,
                                0, 0, 0, 0, 0, 0, 0, dp[5], 24, 8, 1, (byte) 186, 16, (byte) 179, 22, (byte) 171, 43, 53, (byte) 230, 1, 1, (byte) 254, 12, 0, 38, (byte) 225, 1, 0, 0, 1, 109,
                                (byte) 240, (byte) 184, 105, 79, 0, 0};

                        os.write(out);
                        os.flush();


                    }

                    //process echo request
                    else if (sb.subSequence(2, 4).equals("02")) {
                        //respond echo response
                        os.write(new byte[]{4, 3, 0, 8, (byte) 255, (byte) 255, (byte) 255, (byte) 253});
                        os.flush();
                    } else if (sb.subSequence(2, 4).equals("0a")) {
                        //Packet_in processing
                        while (hmap.size() < 5) {

                        }
                        //System.out.println(sb.subSequence(70, 72));
                        //System.out.println(sb.subSequence(118, 130));
                        //System.out.println(sb.subSequence(96, 108));
                        //System.out.println(sb.toString());
                        System.out.println("Switch" + test.name + " port " + sb.subSequence(70, 72) + " is connected to port " +
                                hmap.get("0000" + sb.subSequence(118, 130)).get(sb.subSequence(96, 108)) + "of switch " +
                                hmap1.get("0000" + sb.subSequence(118, 130)).get(sb.subSequence(96, 108)));
                        //System.out.println("Switch"+ test.name + " port "+ sb.subSequence(70, 72)+ " is connected to " +
                        //        hmap.get("0000"+sb.subSequence(118, 130)).get(sb.subSequence(96, 108)));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
