/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.bosesoundtouch.internal;

import static org.eclipse.smarthome.binding.bosesoundtouch.BoseSoundTouchBindingConstants.BINDING_ID;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.smarthome.binding.bosesoundtouch.internal.exceptions.ContentItemNotPresetableException;
import org.eclipse.smarthome.binding.bosesoundtouch.internal.exceptions.NoPresetFoundException;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link PresetContainer} class manages a PresetContainer which contains all additional Presets
 *
 * @author Thomas Traunbauer
 */
public class PresetContainer {

    private final Logger logger = LoggerFactory.getLogger(PresetContainer.class);

    private HashMap<Integer, ContentItem> mapOfPresets;
    private File presetFile;

    /**
     * Creates a new instance of this class
     */
    public PresetContainer() {
        init();
    }

    private void init() {
        this.mapOfPresets = new HashMap<Integer, ContentItem>();
        File folder = new File(ConfigConstants.getUserDataFolder() + "/" + BINDING_ID);
        if (!folder.exists()) {
            logger.debug("Creating directory {}", folder.getPath());
            folder.mkdirs();
        }
        presetFile = new File(folder, "presets.json");
        try {
            readFromFile();
        } catch (IOException e) {
            logger.debug("Could not load Presets from File: {}", presetFile.getPath());
        }
    }

    /**
     * Returns a Collection of all Presets
     *
     * @param operationModeType
     */
    public Collection<ContentItem> getAllPresets() {
        return mapOfPresets.values();
    }

    /**
     * Adds a ContentItem as Preset, with presetID. Note that a eventually existing id in preset will be overwritten by
     * presetID
     *
     * @param presetID
     * @param preset
     *
     * @throws ContentItemNotPresetableException if ContentItem is not presetable
     * @throws IOException if Presets could not be saved to file
     */
    public void put(int presetID, ContentItem preset) throws ContentItemNotPresetableException, IOException {
        preset.setPresetID(presetID);
        if (preset.isPresetable()) {
            mapOfPresets.put(presetID, preset);
            writeToFile();
        } else {
            throw new ContentItemNotPresetableException();
        }
    }

    /**
     * Returns the Preset with presetID
     *
     * @param presetID
     *
     * @throws NoPresetFoundException if Preset could not be found
     */
    public ContentItem get(int presetID) throws NoPresetFoundException {
        ContentItem psFound = mapOfPresets.get(presetID);
        if (psFound != null) {
            return psFound;
        } else {
            throw new NoPresetFoundException();
        }
    }

    private void writeToFile() throws IOException {
        if (presetFile != null) {
            Collection<ContentItem> colletionOfPresets = getAllPresets();
            List<ContentItem> listOfPresets = new ArrayList<>();
            listOfPresets.addAll(colletionOfPresets);
            // Only openhab Presets get saved
            for (Iterator<ContentItem> cii = listOfPresets.iterator(); cii.hasNext();) {
                if (cii.next().getPresetID() <= 6) {
                    cii.remove();
                }
            }

            if (listOfPresets.size() > 0) {
                if (presetFile.exists()) {
                    presetFile.delete();
                }

                BufferedWriter writer = new BufferedWriter(new FileWriter(presetFile));
                new GsonBuilder().create().toJson(listOfPresets, writer);
                writer.close();
            }
        }
    }

    private void readFromFile() throws IOException {
        if (presetFile != null) {
            if (!presetFile.exists()) {
                throw new IOException("Could not load save PRESETS");
            }
            if (presetFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(presetFile));
                Collection<ContentItem> items = new GsonBuilder().create().fromJson(reader,
                        new TypeToken<Collection<ContentItem>>() {
                        }.getType());
                reader.close();
                if (items != null) {
                    for (ContentItem item : items) {
                        try {
                            put(item.getPresetID(), item);
                        } catch (ContentItemNotPresetableException e) {
                        }
                    }
                }
            }
        }
    }
}
