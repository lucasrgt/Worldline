import java.io.*;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Aligns one qualified wire combat event with a real StationAPI/Aero observer window. */
public final class AeroCombatWindowCycle {
    private static final String ID = "m70-aero-combat-window";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID), build = root.resolve(".worldline/smokes").resolve(ID);
    private final Properties config = new Properties(), artifact = new Properties();
    public static void main(String[] arguments) { if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: java tools/smoke/AeroCombatWindowCycle.java " + ID); System.exit(2); }
        try { new AeroCombatWindowCycle().execute(); } catch (Exception error) {
            System.err.println("Aero combat window cycle failed: " + error.getMessage()); System.exit(1); } }
    private void execute() throws Exception {
        load(smoke.resolve("smoke.properties"), config); load(root.resolve(
                "artifacts/minecraft-b1.7.3-server.properties"), artifact); require(ID.equals(value(config,"id")),"id drift");
        require(value(config,"server.jar.sha256").equals(value(artifact,"expected.sha256")),"server descriptor drift");
        Path official=root.resolve(value(artifact,"local.path")).normalize(), checkout=root.resolve(value(config,"aero.path")).normalize();
        verifyArtifact(official); verifyCheckout(checkout); recreate(build); Path classes=compile();
        Outcome first=run(checkout,official,classes,build.resolve("first")); Outcome second=run(checkout,official,classes,build.resolve("second"));
        verifyCheckout(checkout); require(first.trace.equals(second.trace)&&first.signature.equals(second.signature),"fresh traces diverged");
        require(first.signature.equals(value(config,"expected.signature")),"M70 signature drift: "+first.signature);
        String evidence="id="+ID+"\nruns=2\nwire.sessions=4\nserver.jvm=2\nclient.jvm=2\naero.revision="
                +value(config,"aero.revision")+"\nserver.sha256="+value(artifact,"expected.sha256")+"\ntrace="+first.trace
                +"\nfirst="+first.observation+"\nsecond="+second.observation+"\nsignature="+first.signature+"\n";
        Files.write(build.resolve("evidence.txt"),evidence.getBytes(StandardCharsets.UTF_8));
        System.out.println("M70 Aero combat window passed"); System.out.println("  path: M66 wire + M69 swing -> real Aero Packet18/38 observer -> frames/log");
        System.out.println("  observations: "+first.observation+" | "+second.observation); System.out.println("  signature: "+first.signature);
    }
    private Outcome run(Path checkout,Path official,Path classes,Path workspace) throws Exception {
        Files.createDirectories(workspace); int port=freePort(); Captured wire=null,client=null;
        try {
            wire=Captured.start(root,Arrays.asList("java","-classpath",classpath(classes),
                    "worldline.smoke.aerocombat.AeroCombatWireSmoke",official.toString(),workspace.resolve("server").toString(),
                    Integer.toString(port),value(config,"seed"),value(config,"attacker"),value(config,"victim"),
                    value(config,"observer")));
            String armed=wire.awaitLine("WORLDLINE_M70_WIRE_ARMED=",90); int attacker=number(armed,"attacker"),victim=number(armed,"victim");
            Path test=checkout.resolve("stationapi/test"),log=workspace.resolve("aero.log");
            String wrapper=System.getProperty("os.name").startsWith("Windows")?"gradlew.bat":"gradlew";
            List<String> command=Arrays.asList(test.resolve(wrapper).toString(),"--no-daemon","--init-script",
                    root.resolve(value(config,"runner")).toString(),"runClient","-PworldlinePort="+port,
                    "-PworldlineObserver="+value(config,"observer"),"-PworldlineAttacker="+value(config,"attacker"),
                    "-PworldlineVictim="+value(config,"victim"),"-PworldlineWarmup="+value(config,"warmup.frames"),
                    "-PworldlinePost="+value(config,"post.frames"),"-PworldlineLog="+log);
            client=Captured.start(test,command); String ready=client.awaitLine("[WorldlineCombat] ready ",90);
            require(number(ready,"attacker")==attacker&&number(ready,"victim")==victim,"observer identity drift");
            client.awaitLine("[WorldlineCombat] armed ",30); wire.write("GO"); String hit=wire.awaitLine("WORLDLINE_M70_WIRE_HIT=",30);
            require(number(hit,"attacker")==attacker&&number(hit,"victim")==victim,"wire event identity drift");
            String complete=client.awaitLine("[WorldlineCombat] complete ",30); client.finish(45);
            require(client.exitCode==0&&client.output().contains("BUILD SUCCESSFUL"),"Aero client exit drift\n"+client.output());
            wire.write("RELEASE"); wire.awaitLine("WORLDLINE_M70_WIRE_COMPLETE=",30); wire.finish(45);
            require(wire.exitCode==0,"wire fixture exit drift\n"+wire.output());
            String output=client.output(); int swing=output.indexOf("[WorldlineCombat] swing "),event=output.indexOf("[WorldlineCombat] event ");
            require(swing>=0&&event>swing&&output.indexOf("[WorldlineCombat] complete ")>event,"observer event order drift");
            require(number(complete,"postFrames")>=Integer.parseInt(value(config,"post.frames"))
                    &&number(complete,"aeroLinesAfterEvent")>0,"post-event window drift");
            List<String> rows=Files.readAllLines(log,StandardCharsets.UTF_8); int baseline=number(line(output,"[WorldlineCombat] event "),"aeroBaseline");
            require(baseline>=0&&baseline<=rows.size(),"event Aero baseline drift"); long parsed=0;
            for(String row:rows.subList(baseline,rows.size())) if(row.startsWith("[Aero_")){parseAero(row);parsed++;}
            require(parsed>0,"parseable post-event Aero row absent");
            String trace="v1|observer=real-b1.7.3+stationapi+aero3|server=official-b1.7.3|actors=2-wire"
                    +"|fixture=m66-leather-diamond|out=packet18-before-packet7|observer=packet20-identities"
                    +"+packet18-attacker-before-packet38-victim|victim=packet38-before-packet8-18"
                    +"|render=20-post-event-frames|aero=post-event-row|post-window-health-persistence=not-claimed|shutdown=clean";
            return new Outcome(trace,sha256(trace),"ids="+attacker+","+victim+";frames="+number(complete,"postFrames")+";aero="+parsed);
        } finally { if(client!=null)client.killIfAlive(); if(wire!=null)wire.killIfAlive(); }
    }
    private Path compile() throws Exception { Path output=build.resolve("classes"); Files.createDirectories(output);
        List<String> command=new ArrayList<>(Arrays.asList("javac","-encoding","UTF-8","--release","8","-Xlint:all,-options","-Werror",
                "-classpath",product("api").toString(),"-d",output.toString())); command.addAll(javaFiles(root.resolve(
                "adapters/b173-server/src/main/java"))); command.addAll(javaFiles(smoke.resolve("src"))); Captured.run(root,command,60); return output; }
    private void parseAero(String row){ Map<String,String> fields=new HashMap<>(); require(row.indexOf(']')>6,"invalid Aero row");
        for(String token:row.substring(row.indexOf(']')+1).trim().split(" +")){int equals=token.indexOf('='); if(equals>0&&equals<token.length()-1)
            require(fields.put(token.substring(0,equals),token.substring(equals+1))==null,"duplicate Aero field");}
        for(String name:Arrays.asList("frameMs","compileChunksMs","compileChunksMaxMs","gcTimeDeltaMs"))
            require(decimal(fields,name).signum()>=0,"negative Aero timing");
        for(String name:Arrays.asList("compileChunksCalls","compileChunksSkipped","compileBudgetSkipped","batchQueued","cellQueued","beViewCulled"))
            require(whole(fields,name)>=0,"negative Aero counter"); require(whole(fields,"visibleChunks")>0,"no visible chunks"); }
    private BigDecimal decimal(Map<String,String> fields,String name){try{return new BigDecimal(required(fields,name));}
        catch(NumberFormatException error){throw new IllegalStateException("invalid "+name,error);}}
    private long whole(Map<String,String> fields,String name){try{return Long.parseLong(required(fields,name));}
        catch(NumberFormatException error){throw new IllegalStateException("invalid "+name,error);}}
    private String required(Map<String,String> fields,String name){String value=fields.get(name);require(value!=null,"missing "+name);return value;}
    private void verifyCheckout(Path checkout)throws Exception{require(Files.isDirectory(checkout.resolve(".git")),"Aero checkout absent");
        require(Captured.run(root,Arrays.asList("git","-C",checkout.toString(),"remote","get-url","origin"),30).trim().equals(
                value(config,"aero.repository")),"Aero origin drift"); require(Captured.run(root,Arrays.asList("git","-C",checkout.toString(),
                "rev-parse","HEAD"),30).trim().equals(value(config,"aero.revision")),"Aero revision drift"); require(Captured.run(root,
                Arrays.asList("git","-C",checkout.toString(),"status","--porcelain"),30).trim().isEmpty(),"Aero checkout dirty"); }
    private void verifyArtifact(Path path)throws Exception{require(Files.isRegularFile(path),"server absent");require(Files.size(path)==Long.parseLong(
        value(artifact,"expected.bytes")),"server size drift");require(digest(path,"SHA-1").equals(value(artifact,"expected.sha1")),"SHA-1 drift");
        require(digest(path,"SHA-256").equals(value(artifact,"expected.sha256")),"SHA-256 drift");}
    private List<String> javaFiles(Path source)throws Exception{try(Stream<Path> paths=Files.walk(source)){return paths.filter(path->path.toString()
        .endsWith(".java")).sorted().map(Path::toString).collect(Collectors.toList());}}
    private int number(String marker,String name){for(String token:marker.split("[ ;]+"))if(token.startsWith(name+"="))
        return Integer.parseInt(token.substring(name.length()+1));throw new IllegalStateException("missing "+name+" in "+marker);}
    private String line(String text,String prefix){return text.lines().filter(row->row.startsWith(prefix)).findFirst().orElseThrow(
        ()->new IllegalStateException("missing "+prefix)).substring(prefix.length());}
    private int freePort()throws Exception{try(ServerSocket socket=new ServerSocket(0)){return socket.getLocalPort();}}
    private void recreate(Path target)throws Exception{if(Files.exists(target)){require(target.startsWith(root.resolve(".worldline")),"unsafe path");
        try(Stream<Path> paths=Files.walk(target)){for(Path path:paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList()))Files.delete(path);}}
        Files.createDirectories(target);}
    private void load(Path path,Properties target)throws Exception{try(Reader reader=Files.newBufferedReader(path,StandardCharsets.UTF_8)){target.load(reader);}}
    private String value(Properties source,String key){String result=source.getProperty(key);require(result!=null&&!result.trim().isEmpty(),"missing "+key);return result.trim();}
    private String digest(Path path,String algorithm)throws Exception{MessageDigest digest=MessageDigest.getInstance(algorithm);try(InputStream input=Files.newInputStream(path)){
        byte[] buffer=new byte[8192];int count;while((count=input.read(buffer))>=0)digest.update(buffer,0,count);}return java.util.HexFormat.of().formatHex(digest.digest());}
    private String sha256(String value)throws Exception{return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}
    private String classpath(Path classes){return classes+System.getProperty("path.separator")+product("api");}
    private Path product(String name){return root.resolve(".worldline/build/classes").resolve(name);}
    private static void require(boolean condition,String message){if(!condition)throw new IllegalStateException(message);}
    private static final class Outcome{final String trace,signature,observation;Outcome(String t,String s,String o){trace=t;signature=s;observation=o;}}
    private static final class Captured{
        final Process process;final StringBuilder text=new StringBuilder();final Thread reader;final Writer input;int exitCode=-1;
        private Captured(Process process){this.process=process;input=new OutputStreamWriter(process.getOutputStream(),StandardCharsets.UTF_8);
            reader=new Thread(()->{try(BufferedReader in=new BufferedReader(new InputStreamReader(process.getInputStream(),StandardCharsets.UTF_8))){
                String row;while((row=in.readLine())!=null)synchronized(text){text.append(row).append('\n');text.notifyAll();}}
                catch(IOException error){synchronized(text){text.append("[capture-error] ").append(error).append('\n');}}});reader.setDaemon(true);reader.start();}
        static Captured start(Path directory,List<String> command)throws Exception{return new Captured(new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true).start());}
        static String run(Path directory,List<String> command,int timeout)throws Exception{Captured value=start(directory,command);value.finish(timeout);
            require(value.exitCode==0,command.get(0)+" failed\n"+value.output());return value.output();}
        String awaitLine(String prefix,int timeout)throws Exception{long end=System.currentTimeMillis()+timeout*1000L;synchronized(text){while(process.isAlive()
            &&System.currentTimeMillis()<end&&!text.toString().lines().anyMatch(row->row.startsWith(prefix)))text.wait(100L);}return lineValue(prefix);}
        String lineValue(String prefix){return output().lines().filter(row->row.startsWith(prefix)).findFirst().orElseThrow(
            ()->new IllegalStateException("missing "+prefix+"\n"+markers())).substring(prefix.length());}
        String markers(){String value=output().lines().filter(row->row.startsWith("WORLDLINE_")||row.contains("[WorldlineCombat]"))
                .collect(Collectors.joining("\n"));if(!value.isEmpty())return value;List<String> rows=output().lines().collect(Collectors.toList());
            return rows.subList(Math.max(0,rows.size()-40),rows.size()).stream().collect(Collectors.joining("\n"));}
        void write(String value)throws Exception{input.write(value+"\n");input.flush();}
        void finish(int timeout)throws Exception{if(!process.waitFor(timeout,TimeUnit.SECONDS)){kill();throw new IllegalStateException("process timeout\n"+output());}
            exitCode=process.exitValue();reader.join(5000L);}
        String output(){synchronized(text){return text.toString();}}
        void killIfAlive(){if(process.isAlive())kill();}void kill(){process.descendants().forEach(ProcessHandle::destroyForcibly);process.destroyForcibly();}
    }
}
