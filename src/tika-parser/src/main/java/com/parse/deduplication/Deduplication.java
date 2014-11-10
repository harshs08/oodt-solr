package com.parse.deduplication;
/*import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class Deduplication {

	  private HashFunction hash = Hashing.murmur3_32();
	  
	    public String hash(String string) {
	        int min = Integer.MAX_VALUE;
	        for (int i=0; i<string.length(); i++) {
	            int c = string.charAt(i);
	            int n = hash.hashInt(c).asInt();
	            if (n < min) {
	                min = n;
	            }
	        }
	        return Integer.toHexString(min);
	    }
	    
	public static void main(String[] args) {
		HashMap<String,String> jsonHash = new HashMap<String,String>();
		File folder = new File("C:/Users/Akanksha/workspace/HW1Deduplication/jsonFiles");
		File[] listOfFiles = folder.listFiles();
		String jsonString = "";
		    for (int i = 0; i < listOfFiles.length; i++) {
		    	
		      if (listOfFiles[i].isFile()) {
		        System.out.println("File " + listOfFiles[i].getName());
		        try(BufferedReader br = new BufferedReader(new FileReader(listOfFiles[i]))) {
		            StringBuilder sb = new StringBuilder();
		            String line = br.readLine();

		            while (line != null) {
		                sb.append(line);
		                line = br.readLine();
		            }
		           jsonString = sb.toString();
		        } catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        byte[] bytesOfMessage;
				try {
					bytesOfMessage = jsonString.getBytes("UTF-8");
					MessageDigest md;
					md = MessageDigest.getInstance("MD5");
					byte[] thedigest = md.digest(bytesOfMessage);
		
					Deduplication minHash = new Deduplication();
					//if(!jsonHash.containsKey(Arrays.toString(thedigest))) {
					if(!jsonHash.containsKey(minHash.hash(jsonString))) {
						jsonHash.put(minHash.hash(jsonString),listOfFiles[i].getName());
						File dest = new File("C:/Users/Akanksha/workspace/HW1Deduplication/jsonFiles/dedup/" + listOfFiles[i].getName());
						Files.copy(listOfFiles[i].toPath(), dest.toPath());
					}
				} 
				catch (Exception e)
				{
					
				}
		        
		      }
		    }
		    
		    

	}

}
*/

/*import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


import com.google.common.collect.Sets;

public class Deduplication<T> {

	
	public static void main(String[] args){
		
		HashMap<Set<String>,String> jsonHash = new HashMap<Set<String>,String>();
		int count=1;
		File folder = new File("C:/Users/Akanksha/workspace/HW1Deduplication/jsonFiles");
		File[] listOfFiles = folder.listFiles();
		String jsonString = "";
		    for (int i = 0; i < listOfFiles.length; i++) {
		    	
		      if (listOfFiles[i].isFile()) {
		        //System.out.println("File " + listOfFiles[i].getName());
		        try(BufferedReader br = new BufferedReader(new FileReader(listOfFiles[i]))) {
		            StringBuilder sb = new StringBuilder();
		            String line = br.readLine();

		            while (line != null) {
		                sb.append(line);
		                line = br.readLine();
		            }
		           jsonString = sb.toString();
		        } catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		       
				try {
					String[] words = jsonString.split(" ");  
					boolean isSimilar=false;
					Set<String> set1 = new HashSet<String>(Arrays.asList(words));
					
					//if(jsonHash.isEmpty()) {
					if(count==1) {
						jsonHash.put(set1,listOfFiles[i].getName());
						File dest = new File("C:/Users/Akanksha/workspace/HW1Deduplication/jsonFiles/dedup/" + listOfFiles[i].getName());
						Files.copy(listOfFiles[i].toPath(), dest.toPath());
						count++;
					}
					else {
					Set<Set<String>> keysSet = jsonHash.keySet();
					for (Set<String> keys : keysSet) {
					 float numerator = Sets.intersection(set1, keys).size();
					 float denominator = Sets.union(set1, keys).size();
					 float similarity  = numerator / denominator;
					 System.out.println( numerator / denominator);
					 if(similarity > 0.9) {
						 isSimilar=true;
						 break;
					 }
					}
					if(!isSimilar)
					{
						jsonHash.put(set1,listOfFiles[i].getName());
						File dest = new File("C:/Users/Akanksha/workspace/HW1Deduplication/jsonFiles/dedup/" + listOfFiles[i].getName());
						Files.copy(listOfFiles[i].toPath(), dest.toPath());
					}
					}
				} 
				catch (Exception e)
				{
					
				}
		        
		      }
		    }
	}
}*/



import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.parse.tika.FieldConstants;


/**
 * a basic SimHash implementation
 * 
 * @author rana
 * 
 */
public class Deduplication {
  public static final int  HASH_SIZE          = 64;
  public static final long HASH_RANGE         = 2 ^ HASH_SIZE;
  public static MurmurHash hasher             = new MurmurHash();
  private int originalDocCount;
  private static int count;
  private HashMap<String,Map<String, String>> jsonHash = new HashMap<String,Map<String, String> >();
    
  class Data{
	  public Data() {
		 // Arrays.fill(val, 0);
	  }
	  public int[][] val = new int[30][30];
	  
	  public void set(float x,float y){
		  if(x == 0 || y== 0){
			  return ;
		  }
		  val[getX(x)][getY(y)]++;
	  }
	  
	  public int getX(float x){
		  int val = (int)x+180;
		  return val%30;
	  }
	  
	  public int getY(float y){
		  int val = (int)y+180;
		  return val%30;
	  }
	  
	  public int get(float x,float y){
		  return val[getX(x)][getY(y)];
	  }
	  
	  public long getScore(float x,float y){
		  int x1 = getX(x);
		  int y1 = getY(y);
		  long score =0;
		  for(int m=0;m<30;m++){
			  for(int n=0;n<30;n++){
				  score = score + ((Math.abs(m-x1)+Math.abs(n-y1))*val[m][n]);
			  }
		  }
		  return score;
	  }
  }
  private static HashMap<String,Data> jobType = new HashMap<String,Data>();
  private Set<String> unique = new HashSet<String>();
  /**
   * use short cuts to obtains a speed optimized simhash calculation
   * 
   * @param s
   *          input string
   * @return 64 bit simhash of input string
   */
  public Deduplication() {
	  originalDocCount =0;
	  count = 0;
  }
  private static final int FIXED_CGRAM_LENGTH = 4;


  

  public static long computeSimHashFromString(Set<String> shingles) {

    int v[] = new int[HASH_SIZE];
    // compute a set of shingles
    for (String shingle : shingles) {
      byte[] bytes = shingle.getBytes();
      long longHash = FPGenerator.std64.fp(bytes, 0, bytes.length);
      // long hash1 = hasher.hash(bytes, bytes.length, 0);
      // long hash2 = hasher.hash(bytes, bytes.length, (int)hash1);
      // long longHash = (hash1 << 32) | hash2;
      for (int i = 0; i < HASH_SIZE; ++i) {
        boolean bitSet = ((longHash >> i) & 1L) == 1L;
        v[i] += (bitSet) ? 1 : -1;
      }
    }
    long simhash = 0;
    for (int i = 0; i < HASH_SIZE; ++i) {
      if (v[i] > 0) {
        simhash |= (1L << i);
      }
    }

    return simhash;
  }

  public static int hammingDistance(long hash1, long hash2) {
    long bits = hash1 ^ hash2;
    int count = 0;
    while (bits != 0) {
      bits &= bits - 1;
      ++count;
    }
    return count;
  }

  public static long rotate(long hashValue) {
    return (hashValue << 1) | (hashValue >>> -1);
  }

  public boolean calculateOriginalDocs( File file,Map<String, String> data1) {
   boolean b=false;
	 
		String jsonString = "";
		   		    	
		      if (file.isFile()) {
		        //System.out.println("File " + listOfFiles[i].getName());
		        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
		            StringBuilder sb = new StringBuilder();
		            String line = br.readLine();

		            while (line != null) {
		                sb.append(line);
		                line = br.readLine();
		            }
		           jsonString = sb.toString();
		        } catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		       
				try {
					
					boolean isSimilar=false;
					
					
					if(true) {
						if(!jsonHash.containsKey(data1.get(FieldConstants.URL))){
							//Solr.indexFileInSolr(file);
							//System.out.println(getScore(data.get(FieldConstants.JOB_TYPE), new Float(data.get(FieldConstants.LONGITUDE)),  new Float(data.get(FieldConstants.LATITUDE))));
							
							b=true;
						}
						jsonHash.put(data1.get(FieldConstants.URL),null);
						//originalDocCount++;
						count++;
					}
					
				} 
				catch (Exception e)
				{
					System.out.println(e);
				}
		        
		      }
		      return b;
		    
  }
  
  public void findNearlyDuplicates() {}
  
  public void preprocessing(Map<String, String> dat){
	  if(!unique.contains(dat.get(FieldConstants.URL))){
		  unique.add(dat.get(FieldConstants.URL));
	  String jobtype = dat.get(FieldConstants.JOB_TYPE);
	  try{
	  if(!jobType.containsKey(jobtype)){
		  Data d = new Data();
		  d.set(new Float(dat.get(FieldConstants.LONGITUDE)),new Float(dat.get(FieldConstants.LONGITUDE)));
		  jobType.put(jobtype, d);
	  }else{
		  Data d = jobType.get(jobtype);
		
		  d.set(new Float(dat.get(FieldConstants.LONGITUDE)),new Float(dat.get(FieldConstants.LONGITUDE)));
		 
	  
	  }
	  }catch(Exception e){
		  
	  }
	  
	  
  }
  }
  
  public static long getScore(String type,float x,float y){
	  if(x == 0 || y== 0){
		  return Integer.MAX_VALUE;
	  }
	  Data d = jobType.get(type);
	  if(d == null){
		  return 0;
	  }
	  return d.getScore(x, y);
  }
  public int getCount(){
	  return jsonHash.size();
  }
  public int getNearlyDedupCount(){
	  return originalDocCount;
  }

}