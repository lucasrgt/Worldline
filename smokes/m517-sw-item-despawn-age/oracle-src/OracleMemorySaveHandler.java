import java.io.File;import java.util.List;
/** Official-name memory save and chunk implementation. */
@SuppressWarnings("rawtypes") final class OracleMemorySaveHandler implements om,fl,an{
 private final ct info;OracleMemorySaveHandler(long s,String n){info=new ct(s,n);info.a(8,64,8);}public ct c(){return info;}public void b(){}public an a(os p){return this;}public void a(ct v,List p){}public void a(ct v){}public fl d(){return this;}public void e(){}public File b(String n){return null;}public void a(em p){}public void b(em p){}public hi a(dj w,int cx,int cz){byte[] b=new byte[16*128*16];for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=0;y<=64;y++)b[x<<11|z<<7|y]=(byte)(y==0?na.A.bn:na.u.bn);hi c=new hi(w,b,cx,cz);c.n=true;c.p=true;c.b();return c;}public void a(dj w,hi c){}public void b(dj w,hi c){}public void a(){}
}
