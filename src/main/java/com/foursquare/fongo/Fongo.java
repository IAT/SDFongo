package com.foursquare.fongo;

import com.mongodb.*;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.InetSocketAddress;
import java.util.*;

public class Fongo {

    private final Map<String, FongoDB> dbMap = Collections
            .synchronizedMap(new HashMap<String, FongoDB>());
    private final ServerAddress serverAddress;
    private final Mongo mongo;
    private final String name;
    private final boolean isDebug;
    private DB db;

    public Fongo(String name) {
        this(name, false);
    }

    public Fongo(String name, boolean isDebug) {
        this.name = name;
        this.serverAddress = new ServerAddress(new InetSocketAddress(
                ServerAddress.defaultPort()));
        this.mongo = createMongo();
        this.isDebug = isDebug;
    }

    public DB getDB(String dbname) {
        synchronized (dbMap) {
            FongoDB fongoDb = dbMap.get(dbname);
            if (fongoDb == null) {
                fongoDb = new FongoDB(this, dbname);
                dbMap.put(dbname, fongoDb);
            }
            return fongoDb;
        }
    }

    public Collection<DB> getUsedDatabases() {
        return new ArrayList<DB>(dbMap.values());
    }

    public List<String> getDatabaseNames() {
        return new ArrayList<String>(dbMap.keySet());
    }

    public void dropDatabase(String dbName) {
        dbMap.remove(dbName);
    }

    public ServerAddress getServerAddress() {
        return serverAddress;
    }

    public Mongo getMongo() {
        return this.mongo;
    }

    private Mongo createMongo() {
        Mongo mongo = Mockito.mock(Mongo.class);
        Mockito.when(mongo.toString()).thenReturn("Fongo (" + this.name + ")");
        Mockito.when(mongo.getMongoOptions()).thenReturn(new MongoOptions());
        Mockito.when(mongo.getDB(Mockito.anyString())).thenAnswer(
                new Answer<DB>() {
                    @Override
                    public DB answer(InvocationOnMock invocation)
                            throws Throwable {
                        String dbName = (String) invocation.getArguments()[0];
                        return getDB(dbName);
                    }
                });
        Mockito.when(mongo.getUsedDatabases()).thenAnswer(
                new Answer<Collection<DB>>() {
                    @Override
                    public Collection<DB> answer(InvocationOnMock invocation)
                            throws Throwable {
                        return getUsedDatabases();
                    }
                });
        Mockito.when(mongo.getDatabaseNames()).thenAnswer(
                new Answer<List<String>>() {
                    @Override
                    public List<String> answer(InvocationOnMock invocation)
                            throws Throwable {
                        return getDatabaseNames();
                    }
                });
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                String dbName = (String) invocation.getArguments()[0];
                dropDatabase(dbName);
                return null;
            }
        }).when(mongo).dropDatabase(Mockito.anyString());
        return mongo;
    }

    public boolean isDebug() {
        return isDebug;
    }

}
