package com.example.stage.origin.sample;

import com.example.stage.lib.Errors;
import com.streamsets.pipeline.api.BatchMaker;
import com.streamsets.pipeline.api.Field;
import com.streamsets.pipeline.api.Record;
import com.streamsets.pipeline.api.StageException;
import com.streamsets.pipeline.api.base.BaseSource;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SampleSource extends BaseSource {
    private static final Logger LOG = LoggerFactory.getLogger(SampleSource.class);
    private static final String PATH = "/path/to/your/testrepo";

    private Repository repository;
    private Git git;

    public abstract String getConfig();

    @Override
    protected List<ConfigIssue> init() {
        List<ConfigIssue> issues = super.init();
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try {
            repository = builder
                    .setGitDir(new File(PATH + "/.git"))
                    .setMustExist(true)
                    .build();
            git = new Git(repository);

            LOG.info("Connected to Git repository at {}",
                    repository.getDirectory().getAbsolutePath());
        } catch (IOException e) {
            LOG.error("Exception building Git repository", e);
            issues.add(
                    getContext().createConfigIssue(
                            Groups.SAMPLE.name(), "config", Errors.SAMPLE_00, e.getLocalizedMessage()
                    )
            );
        }
        if (getConfig().equals("invalidValue")) {
            issues.add(
                    getContext().createConfigIssue(
                            Groups.SAMPLE.name(), "config", Errors.SAMPLE_00, "Here's what's wrong..."
                    )
            );
        }
        return issues;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (git != null) {
            git.close();
        }
        if (repository != null) {
            repository.close();
        }
    }

    @Override
    public String produce(String lastSourceOffset, int maxBatchSize, BatchMaker batchMaker) throws StageException {
        long nextSourceOffset = 0;
        if (lastSourceOffset != null) {
            nextSourceOffset = Long.parseLong(lastSourceOffset);
        }
        int numRecords = 0;
        while (numRecords < maxBatchSize) {
            Record record = getContext().createRecord("some-id::" + nextSourceOffset);
            Map<String, Field> map = new HashMap<>();
            map.put("fieldName", Field.create("Some Value"));
            record.set(Field.create(map));
            batchMaker.addRecord(record);
            ++nextSourceOffset;
            ++numRecords;
        }
        return String.valueOf(nextSourceOffset);
    }
}
