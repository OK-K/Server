package server;

import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Json {

    public static void createJsonFilePlayerWithAI(int[][] shipPlayer, int[][] shipAI, String namePlayer, String nameAI) throws IOException {
        String resultJson = "{\ngame:\n[" + createJsonFromMatr(shipPlayer,namePlayer) + "," + createJsonFromMatr(shipAI,nameAI) + "]}";
        File fOutput = new File(Server.contentPath + "//json","new_" + namePlayer +".json");
        FileWriter fw = new FileWriter(fOutput);
        fw.write(resultJson);
        fw.close();
    }

    public static String createJsonFromMatr(int[][] ships,String name)
    {
        JSONObject jsonObject = new JSONObject();
        String jsonMatr = "";

        for (int i = 0; i < ships.length; i++)
        {
            for (int j = 0; j < ships[i].length; j++)
            {
                jsonMatr += i + "_" + j + "=!" + ships[i][j] + "!";
            }
        }
        jsonObject.put("ships",jsonMatr);
        jsonObject.put("name", name);
        return jsonObject.toString();
    }
}
