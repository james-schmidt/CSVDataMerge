/*CSVDataMerge
 * CC BY This program can freely be used, edited, and redistributed for
 * non-commercial purposes.
 * @author James R. Schmidt
 * @email james.schmidt@ubfc.fr
 * Université Bourgogne Franche-Comté (UBFC)*/
package csvdatamerge;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.*;

/**Merges together CSV files.*/
public class Main{
 /**Combines CSV files.
  * @param args Not used*/
 public static void main(String[] args){
  File headFile = new File(System.getProperty("user.dir")
                  +System.getProperty("file.separator")+"headers.csv");
  String dir = System.getProperty("user.dir")+
               System.getProperty("file.separator")+"data";
  List fileList;
  Object[] fileNames = new Object[0];
  try{
   fileList = Files.walk(Paths.get(dir))
                   .filter(str -> str.getFileName().toString().endsWith("csv"))
                   .collect(Collectors.toList());
   if(fileList.isEmpty()){
    fileList = Files.walk(Paths.get(dir))
               .filter(str -> str.getFileName().toString().endsWith("txt"))
               .collect(Collectors.toList());
   }
   fileNames = fileList.toArray();
  }
  catch(IOException e1){
   System.out.println("Error in finding data files.");
  }
  String[] headers = null;
  boolean headless = false;
  boolean decapitate = false;
  BufferedWriter writer;
  BufferedReader reader = null;
  String line;
  if(headFile.exists()){
   try{
    reader = new BufferedReader(new FileReader(headFile));
    if((line=reader.readLine())!=null && !line.isEmpty()){
     if(line.equals("no headers")){
      headless = true;
      if((line=reader.readLine())!=null && !line.isEmpty()){
       if(line.equals("leave blank")) decapitate = true;
      }
     }
     if(!decapitate)
      headers = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }
    reader.close();
   }
   catch(IOException e2){
    System.out.println("Problem with headers.csv file.");
   }
  }
  if(!headFile.exists()){
   String firstFile = fileNames[0].toString();
   try{
    reader = new BufferedReader(new FileReader(firstFile));
    if((line=reader.readLine())!=null && !line.isEmpty())
     headers = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    reader.close();
   }
   catch(IOException e3){
    System.out.println("Error in reading first file.");
   }
   String[] newHead;
   String nextFile;
   for(int f=1; f<fileNames.length; f++){
    nextFile = fileNames[f].toString();
    try{
     reader = new BufferedReader(new FileReader(nextFile));
     if((line=reader.readLine())!=null && !line.isEmpty()){
      newHead = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
      for(int h=0; h<newHead.length; h++){
       boolean found = false;
       for(int i=0; i<headers.length && !found; i++){
        if(newHead[h].equals(headers[i])) found = true;
       }
       if(!found){
        headers = Arrays.copyOf(headers,headers.length+1);
        headers[6] = newHead[h];
       }
      }
     }
     reader.close();
    }
    catch(IOException e4){
     System.out.println("Error in reading header names.");
    }
   }
  }
  try{
   writer = new BufferedWriter(new FileWriter("dataset.csv", false));
   if(!decapitate){
    writer.write(headers[0]);
    for(int i=1; i<headers.length; i++) writer.write(","+headers[i]);
    writer.newLine();
   }       
   String nextFile;
   for(Object fileName : fileNames){
    nextFile = fileName.toString();
    reader = new BufferedReader(new FileReader(nextFile));
    if(headless){
     while((line=reader.readLine()) != null && !line.isEmpty()){
      writer.write(line);
      writer.newLine();
     }
    }
    else{
     String[] headLine = null;
     if((line=reader.readLine())!=null && !line.isEmpty()){
      headLine = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
      if(!Arrays.equals(headers, headLine)){
       int[] reorder = new int[headers.length];
       Arrays.fill(reorder, -1);
       for(int i=0; i<headers.length; i++){
        for(int j=0; j<headLine.length; j++){
         if(headLine[j].equals(headers[i])) reorder[i] = j;
        }
       }
       String[] splitLine;
       while((line=reader.readLine())!=null && !line.isEmpty()){
        splitLine = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        if(reorder[0] != -1) writer.write(splitLine[reorder[0]]);
        for(int x=1; x<reorder.length; x++){
         if(reorder[x]<splitLine.length && reorder[x]!=-1)
          writer.write(","+splitLine[reorder[x]]);
         else writer.write(",");
        }
        writer.newLine();
       }
      }
      else{
       while((line=reader.readLine())!=null && !line.isEmpty()){
        writer.write(line);
        writer.newLine();
       }
      }
     }
    }
   }
   reader.close();
   writer.close();
  }
  catch(IOException e5){
   System.out.println("Error reading data.");
  }
 }
}