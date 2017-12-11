package server;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Json {

    public static void createJsonFilePlayerWithAI(int[][] shipPlayer, int[][] shipAI, String namePlayer, String nameAI)
    {
       // String resultJson = "{\ngame:\n[" + createJsonFromMatr(shipPlayer,namePlayer) + "," + createJsonFromMatr(shipAI,nameAI) + "]}";
    }

    /*private static String createJsonFromMatr(int[][] ships,String name)
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);
        for (int i = 0; i < ships.length; i++)
        {
            for (int j = 0; j < ships[i].length)
            {
                System.out.println(ships[i][j]);
            }
        }
    } */
}
