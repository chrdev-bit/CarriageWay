import com.cb.Main;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.*;

public class Tests {
    @Test
    public void testScale() throws Throwable {
        int clones = 10;
        //Simulate a stream of data by duplicating existing area data
        //The zones must be able to fit into RAM. As long as they do this could process areas indefinitely
        //It doesn't save bitmaps to disk because the amount it produces wouldn't be much fun for a file system
        PipedOutputStream pos = new PipedOutputStream();
        PrintStream pr = new PrintStream(pos);
        class Runner extends Thread{
            public void run(){
                    log("Running "+this.getClass().getName());
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonFactory factory = objectMapper.getFactory();
                    pr.print("{\"areas\":[");
                    try (JsonParser parser = factory.createParser(new FileInputStream("areas.json"))) {

                        while (parser.nextToken() != null) {
                            String fieldName = parser.getCurrentName();
                            if ("areas".equals(fieldName)) {
                                parser.nextToken();
                                break;
                            }
                        }
                        int N=0;
                        if (parser.currentToken() == JsonToken.START_ARRAY) {
                            while (parser.nextToken() == JsonToken.START_OBJECT) {
                                TreeNode tn = parser.readValueAsTree();
                                if(tn==null){
                                    break;
                                }
                                //simulate testing using clones
                                for(int i=0; i<clones; i++){
                                    if(N>0){
                                        pr.print(",");
                                    }
                                    pr.print(tn.toString());
                                    N++;
                                }
                            }
                        }
                        pr.print("]}");
                        pr.close();
                }catch(Throwable t){
                    t.printStackTrace();
                }
            }
        }

        PipedInputStream pis = new PipedInputStream(pos);
        Runner r = new Runner();
        r.start();

        Main main = new Main();
        main.parse(new FileInputStream("zones.json"),pis,false, true);
        r.join();

    }

    @Test
    public void drawJagged()throws Throwable{
        Main m = new Main();
        m.parse(new FileInputStream("zones.json"),new FileInputStream("areas.json"),true, false);
    }

    void log(Object o){
       System.out.println(o);
    }

}