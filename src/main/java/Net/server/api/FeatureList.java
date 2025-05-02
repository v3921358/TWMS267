package Net.server.api;

public class FeatureList {

    public FeatureAPI[] featureList;
    public long lastUpdated = 0;

    public class FeatureAPI {

        public int id;
        public String title, slug, summary, live_date;

    }

}
