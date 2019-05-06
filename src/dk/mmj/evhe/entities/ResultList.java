package dk.mmj.evhe.entities;

import java.util.List;

@SuppressWarnings("JavaDocs, unused")
public class ResultList {

    private List<PartialResult> results;

    public ResultList() {
    }

    public ResultList(List<PartialResult> results) {
        this.results = results;
    }

    public List<PartialResult> getResults() {
        return results;
    }

    public void setResults(List<PartialResult> results) {
        this.results = results;
    }
}
