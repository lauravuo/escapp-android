package com.escapp.controller;

import java.util.ArrayList;

import com.escapp.R;
import com.escapp.model.Country;
import com.escapp.view.App;

/**
 * Created by laura on 2.12.2014.
 */
public class CountryManager {
    private ArrayList<Country> countries = null;

    public CountryManager(DatabaseManager databaseManager) {
        double currentVersion = App.getCountriesVersion();
        double newVersion = 0;
        // TODO: check version only when app is upgraded
        try {
            newVersion = XmlParser.getCountriesVersion(
                    App.getContext().getResources().openRawResource(R.raw.countries));
        } catch (Exception e) {
            Logger.e("Unable to get countries version: " + e.toString());
        }

        // Clear and set countries again if greater version is found
        if (newVersion > currentVersion) {
            databaseManager.clearCountries();
            App.setCountriesVersion((float)newVersion);
        }
        countries = databaseManager.getCountries();

        // TODO: check countries version and update countries only if needed
        if (countries.size() == 0) {
            try {
                countries = XmlParser.parseCountries(
                        App.getContext().getResources().openRawResource(R.raw.countries));
                databaseManager.setCountries(countries);
            } catch (Exception e) {
                Logger.e("Failed to parse countries XML. " + e.toString());
            }
        }
    }

    public Country getCountry(String name) {
        for (Country country : countries) {
            if (country.getName().equals(name)) {
                return country;
            }
        }
        Logger.e("Failed to find country for name " + name);
        return null;
    }
}
