import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.imageio.ImageIO;

import utils.JSONObject;
import utils.JSONReader;

public class Id {
    private final int NAMES = 0;
    private final int IMAGES = 1;
    private Object[][] itemData; // row 0 = String[] itemNames, row 1 = BufferedImage[] itemImgss

    public Id() {
        try {
            JSONReader reader = new JSONReader(new File("itemInfo.json"));
            JSONObject obj = reader.interpretFile();

            ArrayList<JSONObject> blocks = (ArrayList<JSONObject>) obj.get("itemInfo.json");
            itemData = new Object[2][blocks.size()];

            for (JSONObject block : blocks) {
                long id = (long) block.get("id");
                int index = (int) id;
                String name = (String) block.get("name");
                String path = (String) block.get("path");
                itemData[NAMES][index] = (String) block.get("name");
                itemData[IMAGES][index] = Tools.resize(ImageIO.read(new File(path)), World.BLOCK_SIZE, World.BLOCK_SIZE);     
            }
        } catch (IOException e) {
            System.out.println("File Error in reading itemInfo.json");
            e.printStackTrace();
        }
    }

    public String[] getItemNames() {
        return (String[]) itemData[NAMES];
    }
    public BufferedImage[] getItemImages() {
        return (BufferedImage[]) itemData[IMAGES];
    }

    public String getItemNameByID(int id) {
        return (String) itemData[NAMES][id];
    }
    public BufferedImage getItemImageByID(int id) {
        return (BufferedImage) itemData[IMAGES][id];
    }
}

