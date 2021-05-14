import frame.MainFrame;

import java.awt.*;

public class Main {


    public static void main(String[] args){
        EventQueue.invokeLater(() -> {
            try {
                MainFrame path = new MainFrame();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }



}
