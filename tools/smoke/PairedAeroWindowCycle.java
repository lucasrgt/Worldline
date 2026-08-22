import java.io.*;
import java.math.*;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.*;

/** Runs balanced fresh control/event Aero windows and reports selected-row summaries. */
public final class PairedAeroWindowCycle {
    private static final String ID="m71-paired-aero-window",TRIGGER="[WorldlinePair] trigger ",COMPLETE="[WorldlinePair] complete ";
    private final Path root=Paths.get("").toAbsolutePath().normalize(),smoke=root.resolve("smokes").resolve(ID);
    private final Path build=root.resolve(".worldline/smokes").resolve(ID);private final Properties config=new Properties(),artifact=new Properties();
    public static void main(String[] args){if(!Arrays.equals(args,new String[]{ID})){System.err.println("usage: java tools/smoke/PairedAeroWindowCycle.java "+ID);System.exit(2);}
        try{new PairedAeroWindowCycle().execute();}catch(Exception error){System.err.println("paired Aero cycle failed: "+error.getMessage());System.exit(1);}}
    private void execute()throws Exception{
        load(smoke.resolve("smoke.properties"),config);load(root.resolve("artifacts/minecraft-b1.7.3-server.properties"),artifact);
        require(ID.equals(value(config,"id")),"id drift");require(value(config,"server.jar.sha256").equals(value(artifact,"expected.sha256")),"server drift");
        Path official=root.resolve(value(artifact,"local.path")).normalize(),checkout=root.resolve(value(config,"aero.path")).normalize();
        verifyArtifact(official);verifyCheckout(checkout);recreate(build);Path classes=compile();int pairs=Integer.parseInt(value(config,"pairs"));
        String[] order={"control","event","event","control","control","event","event","control"};require(pairs==4,"pair design drift");
        int start=Integer.getInteger("worldline.m71.armStart",0),limit=Integer.getInteger("worldline.m71.armLimit",order.length-start);
        require(start>=0&&limit>0&&start+limit<=order.length,"arm range drift");boolean diagnostic=start!=0||limit<order.length;
        require(!diagnostic||Boolean.getBoolean("worldline.m71.diagnostic"),"partial run requires diagnostic opt-in");List<Arm> arms=new ArrayList<>();
        for(int index=start;index<start+limit;index++){verifyCheckout(checkout);arms.add(run(order[index],index/2,index,checkout,official,classes));verifyCheckout(checkout);}
        if(diagnostic){System.out.println("M71 diagnostic arm passed; qualification not attempted arms="+limit);for(Arm arm:arms)System.out.println("  "+arm.summary());return;}
        List<Pair> results=new ArrayList<>();for(int pair=0;pair<pairs;pair++){Arm a=arms.get(pair*2),b=arms.get(pair*2+1);
            results.add(new Pair("control".equals(a.mode)?a:b,"event".equals(a.mode)?a:b));}
        String trace="v1|design=4-balanced-pairs-C/E+E/C+C/E+E/C|arms=fresh-official-server+2-wire+real-aero"
                +"|equivalence=same-seed-names-fixture-provenance-config|trigger=exact-packet3-broadcast"
                +"|control=no-packet18-or-packet7-38|event=packet18-before-packet7+observer18-before38"
                +"|warmup=min300frames+5s|window=min480frames+8s|rows=min30-threshold-gc-heartbeat-selected"
                +"|logger=threshold25-heartbeat200-sync-false|metrics=descriptive-paired-dynamic-only|causality=not-claimed|shutdown=clean";
        String signature=sha256(trace);require(signature.equals(value(config,"expected.signature")),"M71 signature drift: "+signature);
        StringBuilder evidence=new StringBuilder("id="+ID+"\npairs="+pairs+"\narms=8\nwire.sessions=16\nserver.jvm=8\nclient.jvm=8\ntrace="+trace+"\n");
        for(int i=0;i<results.size();i++)evidence.append("pair.").append(i+1).append('=').append(results.get(i).summary()).append('\n');
        evidence.append("signature=").append(signature).append('\n');Files.write(build.resolve("evidence.txt"),evidence.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println("M71 paired Aero window passed");for(int i=0;i<results.size();i++)System.out.println("  pair "+(i+1)+": "+results.get(i).summary());
        System.out.println("  signature: "+signature);
    }
    private Arm run(String mode,int pair,int index,Path checkout,Path official,Path classes)throws Exception{
        Path workspace=build.resolve("pair-"+(pair+1)+"-"+(index%2+1)+"-"+mode);Files.createDirectories(workspace);int port=freePort();Captured wire=null,client=null;
        try{wire=Captured.start(root,Arrays.asList("java","-classpath",classpath(classes),"worldline.smoke.aeropair.AeroPairedWireSmoke",
                    official.toString(),workspace.resolve("server").toString(),Integer.toString(port),value(config,"seed"),value(config,"attacker"),
                    value(config,"victim"),value(config,"observer"),mode,value(config,"trigger.raw")));String armed=wire.awaitLine("WORLDLINE_M71_WIRE_ARMED=",120);
            int attacker=number(armed,"attacker"),victim=number(armed,"victim");require(armed.contains("arm="+mode),"wire arm drift");
            Path test=checkout.resolve("stationapi/test"),log=workspace.resolve("aero.log");String wrapper=System.getProperty("os.name").startsWith("Windows")?"gradlew.bat":"gradlew";
            List<String> command=Arrays.asList(test.resolve(wrapper).toString(),"--no-daemon","--init-script",root.resolve(value(config,"runner")).toString(),"runClient",
                    "-PworldlinePort="+port,"-PworldlineObserver="+value(config,"observer"),"-PworldlineArm="+mode,
                    "-PworldlineAttacker="+value(config,"attacker"),"-PworldlineVictim="+value(config,"victim"),
                    "-PworldlineTrigger="+value(config,"trigger.observed"),"-PworldlineWarmupFrames="+value(config,"warmup.frames"),
                    "-PworldlineWarmupSeconds="+value(config,"warmup.seconds"),"-PworldlineWindowFrames="+value(config,"window.frames"),
                    "-PworldlineWindowSeconds="+value(config,"window.seconds"),"-PworldlineLog="+log);client=Captured.start(test,command);
            String ready=client.awaitLine("[WorldlinePair] ready ",120);require(number(ready,"attacker")==attacker&&number(ready,"victim")==victim,"identity drift");
            client.awaitLine("[WorldlinePair] armed ",60);wire.write("GO");wire.awaitLine("WORLDLINE_M71_WIRE_TRIGGER=",20);client.awaitLine(TRIGGER,20);
            if("event".equals(mode))wire.awaitLine("WORLDLINE_M71_WIRE_EVENT=",30);else wire.awaitLine("WORLDLINE_M71_WIRE_CONTROL=",20);
            String complete=client.awaitLine(COMPLETE,Integer.parseInt(value(config,"timeout.seconds")));client.finish(60);
            require(client.exitCode==0&&client.output().contains("BUILD SUCCESSFUL"),"client exit drift\n"+client.output());
            wire.write("RELEASE");wire.awaitLine("WORLDLINE_M71_WIRE_COMPLETE=",30);wire.finish(60);require(wire.exitCode==0,"wire exit drift\n"+wire.output());
            String output=client.output();verifyOrder(output,mode);require(number(complete,"windowFrames")>=Integer.parseInt(value(config,"window.frames")),"short frame window");
            List<String> selected=bracketedRows(output),fileRows=Files.readAllLines(log,StandardCharsets.UTF_8);require(subsequence(selected,fileRows),"stdout/file row drift");
            List<Row> rows=new ArrayList<>();for(String row:selected)rows.add(parse(row));require(rows.size()>=Integer.parseInt(value(config,"minimum.rows")),"insufficient rows: "+rows.size());
            return new Arm(mode,rows);
        }finally{if(client!=null)client.killIfAlive();if(wire!=null)wire.killIfAlive();}}
    private void verifyOrder(String output,String mode){int trigger=output.indexOf(TRIGGER),complete=output.indexOf(COMPLETE),swing=output.indexOf("[WorldlinePair] swing "),hurt=output.indexOf("[WorldlinePair] hurt ");
        require(trigger>=0&&complete>trigger&&output.indexOf(TRIGGER,trigger+1)<0&&output.indexOf(COMPLETE,complete+1)<0,"marker drift");
        if("event".equals(mode))require(swing>trigger&&hurt>swing&&complete>hurt&&!hasSelected(output.substring(trigger,swing)),"event order drift");
        else require(swing<0&&hurt<0,"control contamination");}
    private List<String> bracketedRows(String output){int start=output.indexOf(TRIGGER),end=output.indexOf(COMPLETE);List<String> rows=new ArrayList<>();
        for(String row:output.substring(start,end).lines().collect(Collectors.toList()))if(row.startsWith("[Aero_")){String kind=kind(row);
            if("WorldFlush".equals(kind))continue;require(Arrays.asList("FrameSpike","GC","Pulse").contains(kind),"unknown Aero kind: "+kind);rows.add(row);}return rows;}
    private boolean hasSelected(String text){return text.lines().anyMatch(row->row.startsWith("[Aero_")&&!"WorldFlush".equals(kind(row)));}
    private String kind(String row){int end=row.indexOf(']');require(row.startsWith("[Aero_")&&end>6,"invalid Aero row");return row.substring(6,end);}
    private Row parse(String row){Map<String,String> fields=new HashMap<>();for(String token:row.substring(row.indexOf(']')+1).trim().split(" +")){int at=token.indexOf('=');
            if(at>0&&at<token.length()-1)require(fields.put(token.substring(0,at),token.substring(at+1))==null,"duplicate Aero field");}
        long frame=micros(fields,"frameMs"),compile=micros(fields,"compileChunksMs"),compileMax=micros(fields,"compileChunksMaxMs"),gc=micros(fields,"gcTimeDeltaMs");
        long calls=whole(fields,"compileChunksCalls");for(String name:Arrays.asList("compileChunksSkipped","compileBudgetSkipped","batchQueued","cellQueued","beViewCulled"))whole(fields,name);
        long visible=whole(fields,"visibleChunks");require(visible>0,"no visible chunks");return new Row(kind(row),frame,compile,compileMax,gc,calls,visible);}
    private long micros(Map<String,String> fields,String name){try{BigDecimal value=new BigDecimal(required(fields,name));require(value.signum()>=0,"negative "+name);
            return value.movePointRight(3).setScale(0,RoundingMode.HALF_UP).longValueExact();}catch(NumberFormatException|ArithmeticException e){throw new IllegalStateException("invalid "+name,e);}}
    private long whole(Map<String,String> fields,String name){try{long result=Long.parseLong(required(fields,name));require(result>=0,"negative "+name);return result;}
        catch(NumberFormatException e){throw new IllegalStateException("invalid "+name,e);}}
    private boolean subsequence(List<String> expected,List<String> actual){int at=0;for(String row:actual)if(at<expected.size()&&expected.get(at).equals(row))at++;return at==expected.size();}
    private Path compile()throws Exception{Path output=build.resolve("classes");Files.createDirectories(output);List<String> command=new ArrayList<>(Arrays.asList("javac","-encoding","UTF-8","--release","8","-Xlint:all,-options","-Werror","-classpath",product("api").toString(),"-d",output.toString()));
        command.addAll(javaFiles(root.resolve("adapters/b173-server/src/main/java")));command.addAll(javaFiles(smoke.resolve("src")));Captured.run(root,command,90);return output;}
    private void verifyCheckout(Path checkout)throws Exception{require(Files.isDirectory(checkout.resolve(".git")),"Aero absent");require(Captured.run(root,Arrays.asList("git","-C",checkout.toString(),"remote","get-url","origin"),30).trim().equals(value(config,"aero.repository")),"origin drift");
        require(Captured.run(root,Arrays.asList("git","-C",checkout.toString(),"rev-parse","HEAD"),30).trim().equals(value(config,"aero.revision")),"revision drift");require(Captured.run(root,Arrays.asList("git","-C",checkout.toString(),"status","--porcelain"),30).trim().isEmpty(),"checkout dirty");}
    private void verifyArtifact(Path path)throws Exception{require(Files.isRegularFile(path),"server absent");require(Files.size(path)==Long.parseLong(value(artifact,"expected.bytes")),"size drift");
        require(digest(path,"SHA-1").equals(value(artifact,"expected.sha1")),"sha1 drift");require(digest(path,"SHA-256").equals(value(artifact,"expected.sha256")),"sha256 drift");}
    private List<String> javaFiles(Path source)throws Exception{try(Stream<Path> paths=Files.walk(source)){return paths.filter(p->p.toString().endsWith(".java")).sorted().map(Path::toString).collect(Collectors.toList());}}
    private int number(String marker,String name){for(String token:marker.split("[ ;]+"))if(token.startsWith(name+"="))return Integer.parseInt(token.substring(name.length()+1));throw new IllegalStateException("missing "+name+" in "+marker);}
    private String required(Map<String,String> fields,String name){String result=fields.get(name);require(result!=null,"missing "+name);return result;}
    private int freePort()throws Exception{try(ServerSocket socket=new ServerSocket(0)){return socket.getLocalPort();}}
    private void recreate(Path target)throws Exception{if(Files.exists(target)){require(target.startsWith(root.resolve(".worldline")),"unsafe path");try(Stream<Path> paths=Files.walk(target)){for(Path p:paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList()))Files.delete(p);}}Files.createDirectories(target);}
    private void load(Path path,Properties into)throws Exception{try(Reader reader=Files.newBufferedReader(path,StandardCharsets.UTF_8)){into.load(reader);}}
    private String value(Properties source,String key){String result=source.getProperty(key);require(result!=null&&!result.trim().isEmpty(),"missing "+key);return result.trim();}
    private String digest(Path path,String algorithm)throws Exception{MessageDigest digest=MessageDigest.getInstance(algorithm);try(InputStream in=Files.newInputStream(path)){byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))>=0)digest.update(buffer,0,n);}return java.util.HexFormat.of().formatHex(digest.digest());}
    private String sha256(String text)throws Exception{return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));}
    private String classpath(Path classes){return classes+System.getProperty("path.separator")+product("api");}private Path product(String name){return root.resolve(".worldline/build/classes").resolve(name);}
    private static void require(boolean ok,String message){if(!ok)throw new IllegalStateException(message);}
    private static long percentile(List<Long> values,double q){List<Long> copy=new ArrayList<>(values);Collections.sort(copy);return copy.get(Math.min(copy.size()-1,(int)Math.ceil(q*copy.size())-1));}
    private static long median(List<Long> values){List<Long> copy=new ArrayList<>(values);Collections.sort(copy);int middle=copy.size()/2;return copy.size()%2==1?copy.get(middle):(copy.get(middle-1)+copy.get(middle))/2;}
    private static final class Row{final String kind;final long frame,compile,compileMax,gc,calls,visible;Row(String k,long f,long c,long m,long g,long a,long v){kind=k;frame=f;compile=c;compileMax=m;gc=g;calls=a;visible=v;}}
    private static final class Arm{final String mode;final List<Row> rows;final long frameMedian,frameP95,frameMax,compileMedian,compileP95,compileMax,calls,gcRows,visibleMin,visibleMax;
        Arm(String m,List<Row> r){mode=m;rows=r;List<Long> f=r.stream().map(x->x.frame).collect(Collectors.toList()),c=r.stream().map(x->x.compile).collect(Collectors.toList());
            frameMedian=median(f);frameP95=percentile(f,.95);frameMax=Collections.max(f);compileMedian=median(c);compileP95=percentile(c,.95);compileMax=r.stream().mapToLong(x->x.compileMax).max().orElse(0);calls=r.stream().mapToLong(x->x.calls).sum();gcRows=r.stream().filter(x->x.gc>0).count();visibleMin=r.stream().mapToLong(x->x.visible).min().orElse(0);visibleMax=r.stream().mapToLong(x->x.visible).max().orElse(0);}
        String summary(){return mode+":rows="+rows.size()+",frameUs="+frameMedian+"/"+frameP95+"/"+frameMax+",compileTotalUs="+compileMedian+"/"+compileP95+",compileSingleMaxUs="+compileMax+",calls="+calls+",gcRows="+gcRows+",visible="+visibleMin+".."+visibleMax;}}
    private static final class Pair{final Arm control,event;Pair(Arm c,Arm e){control=c;event=e;}String summary(){return control.summary()+" | "+event.summary()+" | descriptiveDeltaUs(frameMedian/p95/max)="+(event.frameMedian-control.frameMedian)+"/"+(event.frameP95-control.frameP95)+"/"+(event.frameMax-control.frameMax)+",compileTotalMedian/p95="+(event.compileMedian-control.compileMedian)+"/"+(event.compileP95-control.compileP95)+",compileSingleMax="+(event.compileMax-control.compileMax);}}
    private static final class Captured{final Process process;final StringBuilder text=new StringBuilder();final Thread reader;final Writer input;int exitCode=-1;
        Captured(Process p){process=p;input=new OutputStreamWriter(p.getOutputStream(),StandardCharsets.UTF_8);reader=new Thread(()->{try(BufferedReader in=new BufferedReader(new InputStreamReader(p.getInputStream(),StandardCharsets.UTF_8))){String row;while((row=in.readLine())!=null)synchronized(text){text.append(row).append('\n');text.notifyAll();}}catch(IOException e){synchronized(text){text.append(e).append('\n');}}});reader.setDaemon(true);reader.start();}
        static Captured start(Path dir,List<String> cmd)throws Exception{return new Captured(new ProcessBuilder(cmd).directory(dir.toFile()).redirectErrorStream(true).start());}static String run(Path dir,List<String> cmd,int timeout)throws Exception{Captured c=start(dir,cmd);c.finish(timeout);require(c.exitCode==0,cmd.get(0)+" failed\n"+c.output());return c.output();}
        String awaitLine(String prefix,int seconds)throws Exception{long end=System.currentTimeMillis()+seconds*1000L;synchronized(text){while(process.isAlive()&&System.currentTimeMillis()<end&&!text.toString().lines().anyMatch(r->r.startsWith(prefix)))text.wait(100L);}return output().lines().filter(r->r.startsWith(prefix)).findFirst().orElseThrow(()->new IllegalStateException("missing "+prefix+"\n"+output())).substring(prefix.length());}
        void write(String value)throws Exception{input.write(value+"\n");input.flush();}void finish(int seconds)throws Exception{if(!process.waitFor(seconds,TimeUnit.SECONDS)){kill();throw new IllegalStateException("timeout\n"+output());}exitCode=process.exitValue();reader.join(5000L);}String output(){synchronized(text){return text.toString();}}
        void killIfAlive(){if(process.isAlive())kill();}void kill(){process.descendants().forEach(ProcessHandle::destroyForcibly);process.destroyForcibly();}}
}
