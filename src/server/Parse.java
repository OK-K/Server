package server;

import java.awt.*;

public class Parse {

    //универсальный статический метод для нахождения значения в запросе
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

    //статический метод, который координату выстрела преобразует в Point
    public static Point getShotPoint(String shot)
    {
        char[] requestChars = shot.toCharArray();
        int i = 0;
        while(requestChars[i] != '_')
            i++;
        String x = "";
        x += requestChars[i-1];
        String y = "";
        y += requestChars[i+1];

        return new Point(Integer.parseInt(x) + 1,Integer.parseInt(y) + 1);
    }
}
