package server;

public class Parse {

    public static String getValue(String request, int index)
    {
        String data = "";
        char[] requestChars = request.toCharArray();
        while (requestChars[index] != '!')
        {
            index++;
        }
        index++;
        while (requestChars[index] != '!')
        {
            data += requestChars[index];
            index++;
        }
        return data;
    }
}
