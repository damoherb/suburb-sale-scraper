package com.satalyst.data.scrape;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Element;

/**
 * Author: Damien Herbert
 * Created: 23/03/15 11:46 PM
 */
public class SalesProfile {

    private String homeSales;
    private String unitSales;
    private String homeMedianPrice;
    private String unitMedianPrice;
    private String suburbName;

    private Element houses;
    private Element units;
    private Element suburb;

    public SalesProfile() {
    }

    public SalesProfile(Element houses, Element units, Element suburb) {
        this.houses = houses;
        this.units = units;
        this.suburb = suburb;
    }

    public String getHomeSales() {
        return homeSales;
    }

    public void setHomeSales(String homeSales) {
        this.homeSales = homeSales;
    }

    public String getUnitSales() {
        return unitSales;
    }

    public void setUnitSales(String unitSales) {
        this.unitSales = unitSales;
    }

    public String getHomeMedianPrice() {
        return homeMedianPrice;
    }

    public void setHomeMedianPrice(String homeMedianPrice) {
        this.homeMedianPrice = homeMedianPrice;
    }

    public String getUnitMedianPrice() {
        return unitMedianPrice;
    }

    public void setUnitMedianPrice(String unitMedianPrice) {
        this.unitMedianPrice = unitMedianPrice;
    }

    public String getSuburb() {
        return suburbName;
    }

    public void setSuburb(String suburb) {
        suburbName = suburb;
    }

    public void build() {
        buildHouses();
        buildUnits();
        buildSuburb();
        System.out.println("Built " + toString());
    }

    private void buildHouses() {
        if (houses != null) {
            String housesSold = houses.getElementsMatchingOwnText("sold").html();
            housesSold = StringUtils.remove(housesSold,"houses");
            housesSold = StringUtils.remove(housesSold, "sold");
            this.homeSales = housesSold.trim();

            String houseMedian = houses.getElementsMatchingOwnText("median").html();
            houseMedian = StringUtils.remove(houseMedian, "$");
            houseMedian = StringUtils.remove(houseMedian, ",");
            houseMedian = StringUtils.remove(houseMedian, "median sale price");
            this.homeMedianPrice = houseMedian.trim();
        } else {
            this.homeSales = "0";
            this.homeMedianPrice = "0";
        }
    }

    private void buildUnits() {
        if (units != null) {
            String unitsSold = units.getElementsMatchingOwnText("sold").html();
            unitsSold = StringUtils.remove(unitsSold, "units");
            unitsSold = StringUtils.remove(unitsSold,"sold");
            this.unitSales = unitsSold.trim();

            String unitMedian = units.getElementsMatchingOwnText("median").html();
            unitMedian = StringUtils.remove(unitMedian, "$");
            unitMedian = StringUtils.remove(unitMedian, ",");
            unitMedian = StringUtils.remove(unitMedian, "median sale price");
            this.unitMedianPrice = unitMedian.trim();
        } else {
            this.unitSales = "0";
            this.unitMedianPrice = "0";
        }
    }

    private void buildSuburb() {
        String nameOfSuburb = suburb.parent().getElementsMatchingOwnText("Last 12 months").html();
        nameOfSuburb = nameOfSuburb.replace("Last 12 months ", "").replace("activity", "").trim();
        this.suburbName = nameOfSuburb;
    }

    public String toCsvOutput() {

        return suburbName + ',' +
                homeSales + ',' +
                homeMedianPrice + ',' +
                unitSales + ',' +
                unitMedianPrice;

    }

    @Override
    public String toString() {
        return "SalesProfile{" +
                "homeSales='" + homeSales + '\'' +
                ", unitSales='" + unitSales + '\'' +
                ", homeMedianPrice='" + homeMedianPrice + '\'' +
                ", unitMedianPrice='" + unitMedianPrice + '\'' +
                ", suburbName='" + suburbName + '\'' +
                '}';
    }
}
