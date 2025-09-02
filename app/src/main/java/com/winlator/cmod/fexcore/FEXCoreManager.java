package com.winlator.cmod.fexcore;

import com.winlator.cmod.R;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.winlator.cmod.contents.ContentProfile;
import com.winlator.cmod.contents.ContentsManager;
import com.winlator.cmod.core.AppUtils;
import com.winlator.cmod.core.EnvVars;
import com.winlator.cmod.core.KeyValueSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class FEXCoreManager {

    private static KeyValueSet config;

    public static void loadFEXCoreSpinners(Context ctx, ContentsManager contentsManager, Spinner fexcoreTSOSpinner, Spinner fexcoreMultiblockSpinner, Spinner fexcoreX87ModeSpinner, Spinner fexcoreVersion, String fexcoreConfig) {
        ArrayList<String> tsoPresets = new ArrayList<>(Arrays.asList(ctx.getResources().getStringArray(R.array.fexcore_preset_entries)));
        ArrayList<String> x87modePresets = new ArrayList<>(Arrays.asList(ctx.getResources().getStringArray(R.array.x87mode_preset_entries)));
        ArrayList<String> multiblockValues = new ArrayList<>(Arrays.asList(ctx.getResources().getStringArray(R.array.multiblock_values)));
        
        fexcoreTSOSpinner.setAdapter(new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_dropdown_item, tsoPresets));
        fexcoreMultiblockSpinner.setAdapter(new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_dropdown_item, multiblockValues));
        fexcoreX87ModeSpinner.setAdapter(new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_dropdown_item, x87modePresets));

        config = new KeyValueSet(fexcoreConfig);

        AppUtils.setSpinnerSelectionFromValue(fexcoreTSOSpinner, config.get("tsoMode"));
        AppUtils.setSpinnerSelectionFromValue(fexcoreMultiblockSpinner, config.get("multiblock").equals("1") ? "Enabled" : "Disabled");
        AppUtils.setSpinnerSelectionFromValue(fexcoreX87ModeSpinner, config.get("x87Mode").equals("1") ? "Fast" : "Slow");

        loadFEXCoreVersion(ctx, contentsManager, fexcoreVersion, config.get("version"));

    }
    
    public static String saveFEXCoreConfig(Spinner fexcoreTSOSpinner, Spinner fexcoreMultiblockSpinner, Spinner fexcoreX87ModeSpinner, Spinner fexcoreVersion) {
        String preset = fexcoreTSOSpinner.getSelectedItem().toString();
        String multiBlock = fexcoreMultiblockSpinner.getSelectedItem().toString();
        String x87Mode = fexcoreX87ModeSpinner.getSelectedItem().toString();
        String version = fexcoreVersion.getSelectedItem().toString();

        config.put("version", version);
        config.put("tsoMode", preset);
        config.put("x87Mode", x87Mode.equals("Fast") ? "1" : "0");
        config.put("multiblock", multiBlock.equals("Enabled") ? "1" : "0");

        return config.toString();
    }

    public static void loadFEXCoreEnvVars (KeyValueSet config, EnvVars envVars) {
        String x87Mode = config.get("x87Mode");
        String tsoMode = config.get("tsoMode");
        String multiblock = config.get("multiblock");

        envVars.put("FEX_X87REDUCEDPRECISION", x87Mode);
        envVars.put("FEX_MULTIBLOCK", multiblock);

        switch (tsoMode) {
            case "Fastest":
                envVars.put("FEX_TSOENABLED", "0");
                envVars.put("FEX_VECTORTSOENABLED", "0");
                envVars.put("FEX_MEMCPYSETTSOENABLED", "0");
                envVars.put("FEX_HALFBARRIERTSOENABLED", "0");
                break;
            case "Fast":
                envVars.put("FEX_TSOENABLED", "1");
                envVars.put("FEX_VECTORTSOENABLED", "0");
                envVars.put("FEX_MEMCPYSETTSOENABLED", "0");
                envVars.put("FEX_HALFBARRIERTSOENABLED", "1");
                break;
            case "Slow":
                envVars.put("FEX_TSOENABLED", "1");
                envVars.put("FEX_VECTORTSOENABLED", "1");
                envVars.put("FEX_MEMCPYSETTSOENABLED", "0");
                envVars.put("FEX_HALFBARRIERTSOENABLED", "1");
                break;
            case "Slowest":
                envVars.put("FEX_TSOENABLED", "1");
                envVars.put("FEX_VECTORTSOENABLED", "1");
                envVars.put("FEX_MEMCPYSETTSOENABLED", "1");
                envVars.put("FEX_HALFBARRIERTSOENABLED", "1");
                break;
        }
    }

    private static void loadFEXCoreVersion(Context context, ContentsManager contentsManager, Spinner spinner, String fexcoreVersion) {
        String[] originalItems = context.getResources().getStringArray(R.array.fexcore_version_entries);
        List<String> itemList = new ArrayList<>(Arrays.asList(originalItems));
        for (ContentProfile profile : contentsManager.getProfiles(ContentProfile.ContentType.CONTENT_TYPE_FEXCORE)) {
            String entryName = ContentsManager.getEntryName(profile);
            int firstDashIndex = entryName.indexOf('-');
            itemList.add(entryName.substring(firstDashIndex + 1));
        }
        spinner.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, itemList));
        AppUtils.setSpinnerSelectionFromValue(spinner, fexcoreVersion);
    }
}
