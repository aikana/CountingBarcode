import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class AnalyizeResult {
	
	boolean getPercentForEachCode(String outputFilePath, String inputFilePath, String []originals, long total) {
		try {
			InputStream is = new FileInputStream(inputFilePath);
			Reader rpt = new InputStreamReader(is, StandardCharsets.UTF_8);
			BufferedReader bread = new BufferedReader(rpt);
			
			OutputStream os = new FileOutputStream(outputFilePath);
			Writer wpt= new OutputStreamWriter(os, StandardCharsets.UTF_8);
			BufferedWriter bwrt = new BufferedWriter(wpt);
			
			OutputStream os1 = new FileOutputStream("./summary.txt");
			Writer wpt1 = new OutputStreamWriter(os1, StandardCharsets.UTF_8);
			BufferedWriter bwrt1 = new BufferedWriter(wpt1);
			
			HashMap<String, Long> map = new HashMap<String, Long>();
			
			for (int i =0 ; i < originals.length; i ++) {
				map.put(originals[i], 0l);
			}
			
			String line = "";
			while ((line =bread.readLine()) != null) {
				String[] elements = line.split("/");
				Double last= Double.valueOf(elements[(elements.length-1)]);
				bwrt.write(line +" / "+ String.format("%.4f", ((double)last*100.0/(double)total)) + "\n");
				if (map.containsKey(elements[0].trim())) // origianl
				{
					String origin = elements[0].trim();
					long current = map.get(origin);
					map.remove(origin);
					map.put(origin, current + Long.valueOf(elements[elements.length-1].trim()));
				}
				else if ( elements.length == 3)
				{
					String vari_key = elements[1].trim();
					String origin = vari_key.split("_")[0]; //
					long count = 0;
					// variation 1 and 2
					if (map.containsKey(vari_key))
						count = map.get(vari_key);
					else
						map.put(vari_key, 0l);
					count += Long.valueOf(elements[elements.length-1].trim());
					map.remove(vari_key);
					map.put(vari_key, count);
				}
			}
			for (int i =0 ; i < originals.length; i ++) {
				double count_origin = (double) map.get(originals[i]);
				double count_var1 = 0.0;
				double count_var2 = 0.0;
				if (map.containsKey(originals[i]+"_v1"))
					count_var1 = (double) map.get(originals[i]+"_v1");
				else if (map.containsKey(originals[i]+"_v2"))
					count_var2 = (double) map.get(originals[i]+"_v2");
					
				System.out.println("code number :" + count_origin + " Var1 : " + count_var1 + " Var2 : " + count_var2 + "  total :" + (double)total);
				String temp = String.format("%s \t %.4f \t %.4f \t %.4f \t %.4f \n", originals[i], count_origin*100.0 / (double)total, count_var1*100.0 / (double)total, count_var2*100.0 / (double)total, (count_origin + count_var1 + count_var2)*100.0 / (double)total);
				System.out.print(temp);
				bwrt1.write(temp);
			}
			System.out.println("Calculation for percent is done");
			bread.close();
			bwrt.close();
			bwrt1.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	boolean getSeperateResult(String outputFilePath, String actFileName, String crashFileName, String otherFileName, String useFileName) {
		try {
			
			String output1 = actFileName;
			String output2 = crashFileName;
			String output3 = otherFileName;
			String output4 = useFileName;
			
			InputStream is = new FileInputStream(outputFilePath);
			Reader rpt = new InputStreamReader(is, StandardCharsets.UTF_8);
			BufferedReader bread = new BufferedReader(rpt);
			
			OutputStream os1 = new FileOutputStream(output1);
			Writer wpt1= new OutputStreamWriter(os1, StandardCharsets.UTF_8);
			BufferedWriter bwrt1 = new BufferedWriter(wpt1);
			
			OutputStream os2 = new FileOutputStream(output2);
			Writer wpt2= new OutputStreamWriter(os2, StandardCharsets.UTF_8);
			BufferedWriter bwrt2 = new BufferedWriter(wpt2);
			
			OutputStream os3 = new FileOutputStream(output3);
			Writer wpt3= new OutputStreamWriter(os3, StandardCharsets.UTF_8);
			BufferedWriter bwrt3 = new BufferedWriter(wpt3);
			
			OutputStream os4 = new FileOutputStream(output4);
			Writer wpt4= new OutputStreamWriter(os4, StandardCharsets.UTF_8);
			BufferedWriter bwrt4 = new BufferedWriter(wpt4);
			String line = "";
			while ((line =bread.readLine()) != null) {
				String[] elements = line.split("/");
				String formedStr = elements[0].trim();
				for (int i =1 ; i < elements.length; i++) {
					elements[i] = elements[i].trim();
					formedStr += (","+elements[i]);
				}
				
				if (elements[0].equalsIgnoreCase(getNSizeStringbyOneChar('A', elements[0].length())) &&
						elements[0].equalsIgnoreCase(getNSizeStringbyOneChar('G', elements[0].length()))&&
						elements[0].equalsIgnoreCase(getNSizeStringbyOneChar('N', elements[0].length()))) {
					bwrt1.write(formedStr+"\n");
				}
				else if (elements.length == 2) // when there is no possible variation.
				{
					if (elements[1].equalsIgnoreCase("0"))
						bwrt3.write(formedStr + "\n");
					else
						bwrt4.write(formedStr + "\n");
				}
				else // crash list.
				{
					bwrt2.write(formedStr + "\n");
				}
			}
			System.out.println("Output file is seperated to four different files");
			bread.close();
			bwrt1.close();
			bwrt2.close();
			bwrt3.close();
			bwrt4.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	String getNSizeStringbyOneChar(char a, int n) {
		String str = "";
		for (int i = 0 ; i < n ; i++) {
			str += Character.toString(a);
		}
		return str;
	}
	
	HashMap<String, String> generateVariationMap(String inputFilePath, String []originals) {
		HashMap<String, String> map = new HashMap<String, String>();
		System.out.println("Start to Generate variation Map");
		try {
			InputStream is = new FileInputStream(inputFilePath);
			Reader rpt = new InputStreamReader(is, StandardCharsets.UTF_8);
			BufferedReader bread = new BufferedReader(rpt);
			
			OutputStream os1 = new FileOutputStream("./variations.txt");
			Writer wpt1 = new OutputStreamWriter(os1, StandardCharsets.UTF_8);
			BufferedWriter bwrt1 = new BufferedWriter(wpt1);
		
			// input same barcode
			for (int i =0 ; i < originals.length; i ++) {
				map.put(originals[i], originals[i]);
			}
			
			// input variation with original
			String line = "";
			while ((line =bread.readLine()) != null) {
				String[] elements = line.split("/");
				if ( elements.length == 3)
				{
					// variation 1 and 2
					String vari_key = elements[1].trim();
					String origin = vari_key.split("_")[0]; //
					map.put(elements[0].trim(), origin);
					//System.out.println(elements[0].trim() +"////");
				}
			}
			//System.out.println(map.keySet().size());
			for (String key : map.keySet()) {
				String temp = new String();
				temp = temp.format("%s %s\n", key, map.get(key));
				bwrt1.write(temp);
			}
			System.out.println("Generating variation set is done");
			bread.close();
			bwrt1.close();
			System.out.println("End to Generate variation Map");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	

	boolean getSeparatedGZipBasedOnBarcode(String outputFilePath, String inputFilePath, String resultPath, String []originals)
	{
		// map with variation ; origin matches
		HashMap<String, String> map = generateVariationMap(resultPath, originals);
		try {
			// file input - gzip fastq			
			InputStream is = new GZIPInputStream(new FileInputStream(inputFilePath));
			Reader rpt = new InputStreamReader(is, StandardCharsets.UTF_8);
			BufferedReader bread = new BufferedReader(rpt);
			// output array
			int size = originals.length; 
			OutputStream []os = new GZIPOutputStream[size];
			Writer []wpt = new OutputStreamWriter[size];
			
			BufferedWriter []bwrt = new BufferedWriter[size];
			for (int i = 0; i < size ; i ++) {
				os[i] = new GZIPOutputStream( new FileOutputStream(outputFilePath+originals[i]+".gz"));
				wpt[i] = new OutputStreamWriter(os[i], StandardCharsets.UTF_8);
				bwrt[i] = new BufferedWriter(wpt[i]);
			}
			String line[] = new String[4];
			int count =0;
			while ((line[0] = bread.readLine()) != null) {
				if (line[0].indexOf("@") != -1) {
					// file read for 
					line[1] = bread.readLine();
					line[2] = bread.readLine();
					line[3] = bread.readLine();
					
					// code find
					String code = line[0].substring(line[0].lastIndexOf(":") + 1);
					if (map.containsKey(code)) {
						String origin = map.get(code);
						// file output stream search
						for (int i = 0; i < size; i ++) {
							if (originals[i].compareToIgnoreCase(origin)==0) {
								for (int j=0; j < 4; j++) {
									bwrt[i].write(line[j]+"\n");
									//System.out.print(i+" th : " + line[j]+"\n");
								}
								break;
							}
						}
					}
				}
			}
			
			//close all stream.
			bread.close();
			for (int i =0 ; i < size ; i ++)	wpt[i].close();
			System.out.println("Separation is completed");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return true;
	}	
}
