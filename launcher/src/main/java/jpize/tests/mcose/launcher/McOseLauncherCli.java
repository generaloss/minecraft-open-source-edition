package jpize.tests.mcose.launcher;

import generaloss.resourceflow.stream.FastReader;

public class McOseLauncherCli{

    public McOseLauncherCli(){
        final McOseClient client = new McOseClient();

        final FastReader reader = new FastReader();
        while(!Thread.interrupted()){
            if(reader.hasNext()){
                final String line = reader.nextLine();
                if(line.equals("run")){
                    client.launch();
                }
                if(line.equals("download")){
                    client.downloadJar();
                }
            }
        }
        reader.close();
    }

}
