package com.example.stage.origin.sample;

import com.streamsets.pipeline.api.GenerateResourceBundle;
import com.streamsets.pipeline.api.Label;

@GenerateResourceBundle
public enum Groups implements Label {
    SAMPLE("Sample"),;

    private final String label;

    private Groups(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return this.label;
    }
}
