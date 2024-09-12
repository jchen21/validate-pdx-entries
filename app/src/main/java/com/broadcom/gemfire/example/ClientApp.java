package com.broadcom.gemfire.example;

import org.apache.geode.pdx.PdxReader;
import org.apache.geode.pdx.PdxSerializable;
import org.apache.geode.pdx.PdxWriter;
import org.apache.geode.cache.Region;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;

import java.util.ArrayList;


public class ClientApp {

    public static class IdAndName implements PdxSerializable {
        private String id;
        private String name;

        public IdAndName() {
            // nothing
        }

        IdAndName(String id, String name) {
            this.id = id;
            this.name = name;
        }

        String getId() {
            return id;
        }

        String getName() {
            return name;
        }

        @Override
        public void toData(PdxWriter writer) {
            writer.writeString("myId", id);
            writer.writeString("name", name);
        }

        @Override
        public void fromData(PdxReader reader) {
            id = reader.readString("myId");
            name = reader.readString("name");
        }
    }

    private void populateRegion(Region region) {
        for (int i = 0; i < 10; i++) {
            ArrayList list = new ArrayList<>();
            list.add(new IdAndName("" + i, "name" + i));
            region.put(i, list);
        }
    }

    private void createClient() {
        ClientCache clientCache = new ClientCacheFactory().addPoolLocator("viking.intranet.hyperic.net", 10334).create();
        Region region = clientCache.createClientRegionFactory(ClientRegionShortcut.PROXY).create("testRegion");
        populateRegion(region);
    }

    public static void main(String[] args) {
        ClientApp app = new ClientApp();
        app.createClient();
    }
}
