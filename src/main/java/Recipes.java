import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

/**
 * Created by magnus010 on 7/2/2017.
 */
public class Recipes {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        System.out.println("Hello World!");

        TransportClient client = new PreBuiltTransportClient(Settings.EMPTY)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));

        String url = "https://api.pinterest.com/v1/boards/chambersbueter/recipes-weve-tried/pins/";
        String charset = "UTF-8";  // Or in Java 7 and later, use the constant: java.nio.charset.StandardCharsets.UTF_8.name()
        String param1 = "token";
        String param2 = "100";
        String param3 = "id,link,counts,note,url,image";

        Properties prop = new Properties();
        InputStream input = null;
        try {
            //load a properties file from class path, inside static method
            input = new FileInputStream("src/main/resources/config.properties");

            // load a properties file
            prop.load(input);

            //get the property value and print it out
            param1 = prop.getProperty("token");
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        String query = String.format("access_token=%s&limit=%s&fields=%s",
                URLEncoder.encode(param1, charset),
                URLEncoder.encode(param2, charset),
                URLEncoder.encode(param3, charset));

        URLConnection connection = new URL(url + "?" + query).openConnection();
        connection.setRequestProperty("Accept-Charset", charset);

        status(url, query);

        headers(connection);

        String getRequest = httpGET(connection);

        System.out.println("GET request string: " + getRequest);

        IndexResponse response = client.prepareIndex("index", "type", "1")
                .setSource(getRequest)
                .get();

        System.out.println("IndexResponse: " + response);

        GetResponse VerifyResponse = client.prepareGet("index", "type", "1").get();

        System.out.println("GetResponse: " + VerifyResponse);

        client.close();
        System.exit(0);
    }

    private static String httpGET(URLConnection connection) throws IOException {
        InputStream response = connection.getInputStream();

        try (Scanner scanner = new Scanner(response)) {
            String responseBody = scanner.useDelimiter("\\A").next();
            System.out.println(responseBody);
            return responseBody;
        }
    }

    private static void status(String url, String query) throws IOException {
        URL proper_url = new URL(url + "?" + query);
        HttpURLConnection httpConnection = (HttpURLConnection) proper_url.openConnection();
        int status = httpConnection.getResponseCode();
        System.out.println("Status: " + status);
    }

    private static void headers(URLConnection connection) {
        for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
            System.out.println(header.getKey() + "=" + header.getValue());
        }
    }
}
