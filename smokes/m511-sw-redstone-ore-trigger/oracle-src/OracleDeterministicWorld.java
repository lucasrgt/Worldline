/** Official-name counterpart exposing the random-tick cursor. */
final class OracleDeterministicWorld extends dj{
 OracleDeterministicWorld(om save,String name,long seed){super(save,name,seed,null);}
 void freezeRandom(long seed){g=(int)seed;r.setSeed(seed);}
}
