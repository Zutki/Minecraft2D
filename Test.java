import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import utils.*;
class Test {
    public static void main(String[] args)throws IOException {
        JSONReader reader = new JSONReader(new File("test.json"));
        JSONObject object = reader.interpretFile();
        
        System.out.println(object);
    }
}
