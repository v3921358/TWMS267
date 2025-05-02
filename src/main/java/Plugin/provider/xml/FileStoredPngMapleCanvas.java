package Plugin.provider.xml;

import Plugin.provider.MapleCanvas;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


//延遲加載的方式，只有在需要時才讀取圖像。這樣可以節省內存和加快初始化時間
public class FileStoredPngMapleCanvas implements MapleCanvas {

    private final File file;
    private int width;
    private int height;
    private BufferedImage image;

    public FileStoredPngMapleCanvas(int width, int height, File fileIn) {
        this.width = width;
        this.height = height;
        this.file = fileIn;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public BufferedImage getImage() {
        if (image == null) {
            loadImageIfNecessary();
        }
        return image;
    }

    private void loadImageIfNecessary() {
        if (image == null) {
            try {
                image = ImageIO.read(file);
                // replace the dimensions loaded from the wz by the REAL dimensions from the image - should be equal tho
                width = image.getWidth();
                height = image.getHeight();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
