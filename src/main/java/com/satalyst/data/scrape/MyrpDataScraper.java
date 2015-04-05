package com.satalyst.data.scrape;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Author: Damien Herbert
 * Created: 23/03/15 9:55 PM
 */
public class MyrpDataScraper {

    private static final String BASE_URL = "http://www.myrp.com.au/suburb/wa/";
    private static final String URL_SEPARATOR = "/";
    private static final String HOUSE_ELEMENT_ID = "#housesSoldDetailsList";
    private static final String UNIT_ELEMENT_ID = "#unitsSoldDetailsList";
    private static final String RESOURCE_DIR = "src\\main\\resources\\";
    private static final int CSV_SUBURB_POSITION = 0;
    private static final int CSV_POSTCODE_POSITION = 1;
    private List<Long> scrapeTimes = new ArrayList<Long>();

    public static void main(String[] args) throws FileNotFoundException {
        MyrpDataScraper scraper = new MyrpDataScraper();

        long start = System.currentTimeMillis();
        System.out.println("Start: " + new Date(start).toString());
        scraper.run();
        long end = System.currentTimeMillis();
        System.out.println("End: " + new Date(end).toString());
        System.out.println("Execution time was " + (end - start) + "ms");
    }

    private void run() throws FileNotFoundException {

        List<SalesProfile> salesList = new ArrayList<SalesProfile>();
        File suburbs = new File(RESOURCE_DIR + "metro_suburbs.csv");
        BufferedReader br = new BufferedReader(new FileReader(suburbs));

        String line = null;
        try {
            int count = 0;
            while ((line = br.readLine()) != null) {
                // skip the header
                if (count > 0) {

                    long start = System.currentTimeMillis();
                    String urlPath = buildURLPath(line);
                    String response = invokeUrl(urlPath);
                    long end = System.currentTimeMillis();
                    scrapeTimes.add((end - start));

                    SalesProfile result = extractDataFromResponse(response, urlPath);
                    if (result == null) {
                        result = createEmptySalesProfile(line);
                    }
                    salesList.add(result);

                }

                count++;
            }

            writeSalesListToFile(salesList);
            System.out.println("Scrape Times");
            for (Long runTime : scrapeTimes) {
                System.out.println(runTime);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private SalesProfile createEmptySalesProfile(String line) {
        SalesProfile result;
        result = new SalesProfile();
        String[] values = splitLineByToken(line, ',');
        result.setSuburb(values[CSV_SUBURB_POSITION]);
        return result;
    }

    private String[] splitLineByToken(String line, char token) {
        return StringUtils.splitPreserveAllTokens(line, token);
    }

    private String buildURLPath(String line) {
        String[] vals = splitLineByToken(line, ',');
        String suburb = vals[CSV_SUBURB_POSITION];
        String postcode = vals[CSV_POSTCODE_POSITION];
        return BASE_URL + getSuburbParam(suburb) + URL_SEPARATOR + postcode;
    }


    private SalesProfile extractDataFromResponse(String response, String url) {

        Document doc = Jsoup.parse(response, url);

        Element houses = doc.select(HOUSE_ELEMENT_ID).first();
        Element units = doc.select(UNIT_ELEMENT_ID).first();
        Element suburb = doc.getElementById("last12MonthsActivity");

        if (houses != null || units != null) {
            SalesProfile profile = new SalesProfile(houses, units, suburb);
            profile.build();

            return profile;
        }

        return null;
    }

    private void writeSalesListToFile(List<SalesProfile> salesList) {
        File salesFile = new File(RESOURCE_DIR + "Suburb_Sales.csv");
        FileWriter fw = null;

        try {
            fw = new FileWriter(salesFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            for (SalesProfile profile : salesList) {
                bw.write(profile.toCsvOutput());
                bw.write("\n");
            }
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String invokeUrl(String urlPath) throws IOException {

        System.out.println("Getting content for " + urlPath);
        HttpURLConnection connection = null;

        try {
            //Create connection
            connection = createConnection(urlPath);

            //Send request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.flush();
            wr.close();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }

    }

    private HttpURLConnection createConnection(String urlPath) throws IOException {
        URL url = new URL(urlPath);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        return connection;
    }

    private String getSuburbParam(String suburb) {
        return suburb.replace(' ', '_').toLowerCase();
    }
}
