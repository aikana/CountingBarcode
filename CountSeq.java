
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;

class Barcode {
	public String barcode;
	public LinkedList<String> link;
	public int count;

	public Barcode(String bar) {
		this.barcode = bar;
		this.link = new LinkedList<String>();
		this.count = 0;
	}

	public String toString() {
		return String.format("Barcode : %s, link %s", barcode, link.toString() + " count :" + count);
	}
}

// main class part
public class CountSeq {
	public static void main(String[] args) {
		
		long start_time = System.currentTimeMillis();
		long end_time =0;
		// input file
		//String input = "/home/zaizhen/workspace/CountSequence/src/Undetermined_S0_L002_R1_001.fastq.gz";
		//String input = "/home/zaizhen/workspace/CountSequence/src/Undetermined_S0_R2_001.fastq.gz";
		String input = "/home/zaizhen/Downloads/Undetermined_S0_R2_001.fastq.gz";
		
		// output file
		String baseoutput = "./baseOutput.txt";
		String result = "./result.txt";
		
		// these barcodes are orginal examples.
		
		
		//String[] originals = { "TCCTAG", "AAACCT", "ATGACG", "ATCACG", "ATCGTA", "CCCTTA", "CTAGTA", "CCTTAA" };
		//String[] originals = { "GAACAACT+ACAACCAT", "ATATGACG+ATCAAGCG", "GAACAACT+ACAACCAA", "CTATTGTA+ACCTCTAA" };
		String[] originals = { "TGTGTC","AAACCT","ATGACG","TCCTTA","CGTCAG","GAAGGA","ATCACG","CGATGT","TTAGGC","TGACCA","ACAGTG","GCCAAT","CAGATC","ACTTGA","GATCAG","TAGCTT","GGCTAC","CTTGTA"};
	
		int variation = 2;
		int len = originals[0].length();
		long total = 341608118;
		int OneBatchSize = 100000;
		
		// if program run with parameter
		if (args.length > 1) {
			// only parameter has 2 (exception handling more)
			if (args.length != 2)
			{
				System.out.println("Parameter Error");
				System.out.println("usage : CountSeq [inputfile.gz] [outputfile.fastq]");
			} else {
				input = args[0];
				baseoutput = args[1];
			}
		}
		/*
		WrapperAllPossibleMatrix(len, variation, originals, baseoutput);
		CountSeq gZip = new CountSeq();
		
		if (len > 15) {
			total = gZip.countUnitsBasedFile(input, result, originals, OneBatchSize, baseoutput);
			return;
		}
		else {
			total = gZip.countUnitsBasedFile(input, result, originals, OneBatchSize, baseoutput);
		}
		*/
		System.out.println("total : "+ total);
		AnalyizeResult ar = new AnalyizeResult();
		ar.getSeperateResult(result, "baseout1.txt", "baseout2.txt", "baseout3.txt", "baseout4.txt");
		ar.getPercentForEachCode("./ResultWithPercent.txt", "./result.txt", originals, total);
		end_time = System.currentTimeMillis();
		System.out.println("Running Time in Milli seconds " + (end_time - start_time));
	}

	public static int makeBaseFile(String outFile, Map<String, Barcode> map) {
		System.out.println("MAP BASE MAKE START");
		File ft = new File(outFile);
		try {
			if (!ft.exists()) 
				ft.createNewFile();
			OutputStream os = new FileOutputStream(outFile);
			Writer wrt = new OutputStreamWriter(os);
			BufferedWriter bwrite = new BufferedWriter(wrt);
			Set<String> keys = map.keySet();
			Iterator<String> it = keys.iterator();
			while (it.hasNext()) {
				String key = it.next();
				Barcode currentBarcode = map.get(key);
				String links = "/";
				for (int i = 0; i < currentBarcode.link.size(); i++) {
					links += currentBarcode.link.get(i) + "/";
				}
				// barcode print format
				String str = String.format("%s %s %d\n", currentBarcode.barcode, links, currentBarcode.count);
				bwrite.write(str);
			}
			bwrite.close();
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		
		System.out.println("MAP BASE MAKE END");
		return 0;
	}

	public static void WrapperAllPossibleMatrix(int baselen, int vari, String[] originals, String outpath) {
		try {
			File ft = new File(outpath);
			if (!ft.exists())
				ft.createNewFile();
			else {
				ft.delete();
				ft.createNewFile();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		if (baselen <= 8) {
			Map<String, Barcode> fullMap = getAllPossibleBarcode(baselen);
			for (int i = 1 ; i <= vari ; i ++)
				SetNVariation(originals, fullMap, i);
			makeBaseFile(outpath, fullMap);
		} else if (baselen < 14) {
			// this barcode include + sign inside.
			baselen = baselen - 1;
			
			// original barcode should be spearated to
			//String dualBaseFile = "./dualbase.txt";
			Map<String, Barcode> fullMap1 = getAllPossibleBarcode(baselen / 2);
			Map<String, Barcode> fullMap2 = getAllPossibleBarcode(baselen / 2);
			makeDualbaseFile(outpath, fullMap1, fullMap2);

			for (int i = 0; i < originals.length; i++) {
				for (int j = 1 ; j <=vari ; j ++) {
					LinkedList<String> variations = getNVariationBothSides(originals[i], j);
					setDualVariationToMap(outpath, variations, originals[i], j);
				}
			}
		} else {
			//String dualBaseFile = "./dualbase8x8.txt";
			System.out.println("16 bases barcode is implementing now");
			for (int i = 0; i < originals.length ; i++) {
				for (int j = 1 ; j <=vari ; j ++) {
					LinkedList<String> variations = getNVariationBothSides(originals[i], j);
					setDualVariationToMapWithNObase(outpath, variations, originals[i], j);
				}
			}
		}
	}
	
	public static void setDualVariationToMapWithNObase(String basefile, LinkedList<String> variations, String org, int varlimit) {
		System.out.println("Dual Map Update START with no Base function");
		String output = "dualnobaseVar_temp.txt";
		File ft = new File(basefile);
		try {
			if (!ft.exists()) {
				//String path = "./" + basefile;
				// Use relative path for Unix systems
				File f = new File(basefile); 
				f.createNewFile();
			}
			InputStream is = new FileInputStream(basefile);
			Reader rpt = new InputStreamReader(is, StandardCharsets.UTF_8);
			BufferedReader bread = new BufferedReader(rpt);

			OutputStream os = new FileOutputStream(output);
			Writer wrt = new OutputStreamWriter(os);
			BufferedWriter bwrite = new BufferedWriter(wrt);

			String str;
			int num = 0;
			// base file has all possible codes.
			while ((str = bread.readLine()) != null) {
				num++;
				// 0 : barcode 1: variations /a/b/c/ 2: count
				String[] store = str.split(" ");
				if (!variations.contains(store[0])) {
					// if there is no match, write line itself.
					if (!org.equalsIgnoreCase(store[0]))
						bwrite.write(str+"\n");
					else {
						System.out.println("Matched with Original " + store[0]+ ":" + org);
						// if origianl barcode is matched with written one
						String temp = store[0]+" ";
						String[] marks = store[1].split("/");
						if (marks.length == 0)
							temp += ("/"+ org + "_v0/");
						else {
							for (int i = 0; i < marks.length; i++) {
								temp += ("/" + marks[i]);
							}
							temp += ("/" + org + "_v0/");
						}
						temp += (" " + store[2]);
						temp += "\n";
						bwrite.write(temp);						
					}
				}
				else {
					//System.out.println("Contain this " + store[0]);
					// if there is data match
					String temp = store[0] +" ";
					String[] marks = store[1].split("/");
					if (marks.length == 0) {
						temp += ("/" + org + "_v" + varlimit + "/");
					}
					else {
						for (int i = 0; i < marks.length; i++) {
							temp += ("/" + marks[i]);
						}
						temp += ("/" + org + "_v" + varlimit + "/");
					}
					temp += (" " + store[2]);
					temp += "\n";
					// System.out.println(temp);
					bwrite.write(temp);
				}
				variations.remove(store[0]);
			}
			bread.close();
			
			// if there is still variation, write all in last part of output
			for ( int i = 0 ; i < variations.size(); i++) {
				str = String.format("%s / 0\n", variations.get(i));
				bwrite.write(str);
			}
			bwrite.close();

			// file change.
			File file = new File(basefile);
			File file2 = new File(output);

			file.delete();
			file = new File(basefile);
			file2.renameTo(file);

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Dual Map based on No base Update END");
	}

	public static void setDualVariationToMap(String basefile, LinkedList<String> variations, String org, int varlimit) {
		System.out.println("Dual Map Update START");
		String output = "dualvar_temp.txt";
		File ft = new File(basefile);
		try {
			if (!ft.exists()) {
				System.out.println("Dual Variation base file doesn't exist");
				return;
			}
			InputStream is = new FileInputStream(basefile);
			Reader rpt = new InputStreamReader(is, StandardCharsets.UTF_8);
			BufferedReader bread = new BufferedReader(rpt);

			OutputStream os = new FileOutputStream(output);
			Writer wrt = new OutputStreamWriter(os);
			BufferedWriter bwrite = new BufferedWriter(wrt);

			String str;
			int num = 0;
			// base file has all possible codes.
			while ((str = bread.readLine()) != null) {
				num++;
				// 0 : barcode 1: variations /a/b/c/ 2: count
				String[] store = str.split(" ");
				if (!variations.contains(store[0])) {
					// if there is no match, write line itself.
					bwrite.write(str);
				} else {
					// if there is data match
					String temp = store[0];
					String[] marks = store[1].split("/");
					if (marks.length == 0)
						temp += " /";
					else {
						for (int i = 0; i < marks.length; i++) {
							temp += ("/" + marks[i]);
						}
						temp += ("/" + org + "_v" + varlimit + "/");
					}
					temp += (" " + store[2]);
					temp += "\n";
					// System.out.println(temp);
					bwrite.write(temp);
				}
			}
			bread.close();
			bwrite.close();

			// file change.
			File file = new File(basefile);
			File file2 = new File(output);

			file.delete();
			file = new File(basefile);
			file2.renameTo(file);

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Dual Map Update END");
	}

	public static void makeDualbaseFile(String filename, Map<String, Barcode> fullmap1, Map<String, Barcode> fullmap2) {
		System.out.println("Start Dual Base file generating");
		File ft = new File(filename);
		try {
			if (!ft.exists())
				ft.createNewFile();

			OutputStream os = new FileOutputStream(filename);
			Writer wrt = new OutputStreamWriter(os);
			BufferedWriter bwrite = new BufferedWriter(wrt);
			Iterator<String> keys = fullmap1.keySet().iterator();
			while (keys.hasNext()) {
				Iterator<String> keys2 = fullmap2.keySet().iterator();
				String l_bar = fullmap1.get(keys.next()).barcode;
				while (keys2.hasNext()) {
					String r_bar = fullmap2.get(keys2.next()).barcode;
					String str = l_bar + "+" + r_bar + " / 0\n";
					// System.out.println(str);
					bwrite.write(str);
				}
			}
			bwrite.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("End Dual Base file generating");
	}

	public static LinkedList<String> getNVariationBothSides(String original, int varlimit) {
		LinkedList<String> arr = new LinkedList<String>();
		String[] separated_codes = original.split("\\+");
		if (varlimit == 1) {
			LinkedList<Barcode> expandedBarcode_left = get1VariationWithoutMap(separated_codes[0]);
			LinkedList<Barcode> expandedBarcode_right = get1VariationWithoutMap(separated_codes[1]);
			for (int j = 0; j < expandedBarcode_left.size(); j++)
			{
				arr.add(expandedBarcode_left.get(j).barcode + "+" + separated_codes[1]);
			}
			for (int k = 0; k < expandedBarcode_right.size(); k++) {
				arr.add(separated_codes[0]+ "+" + expandedBarcode_right.get(k).barcode);
			}
		}
		else if (varlimit == 2) // when both side has one variation
		{
			LinkedList<Barcode> expandedBarcode_left = get1VariationWithoutMap(separated_codes[0]);
			LinkedList<Barcode> expandedBarcode_right = get1VariationWithoutMap(separated_codes[1]);
			for (int j = 0; j < expandedBarcode_left.size(); j++) {
				for (int k = 0; k < expandedBarcode_right.size(); k++) {
					arr.add(expandedBarcode_left.get(j).barcode + "+" + expandedBarcode_right.get(k).barcode);
				}
			}
		} else {
			System.out.println("Variation support only until 2");
		}
		return arr;
	}
	
	public static LinkedList<String> getNVariationBothSides_Store(String original, int varlimit) {
		LinkedList<String> arr = new LinkedList<String>();
		String[] separated_codes = original.split("\\+");
		if (varlimit == 1) {
			LinkedList<Barcode> expandedBarcode_left = get1VariationWithoutMap(separated_codes[0]);
			LinkedList<Barcode> expandedBarcode_right = get1VariationWithoutMap(separated_codes[1]);
			for (int j = 0; j < expandedBarcode_left.size(); j++) {
				for (int k = 0; k < expandedBarcode_right.size(); k++) {
					arr.add(expandedBarcode_left.get(j).barcode + "+" + expandedBarcode_right.get(k).barcode);

				}
				// System.out.println("Barcode Var1 " + temp.barcode + " : " +
				// temp.link.toString());
			}
		} else if (varlimit == 2) {
			LinkedList<Barcode> expandedBarcode_left = get2VariationWithoutMap(separated_codes[0]);
			LinkedList<Barcode> expandedBarcode_right = get2VariationWithoutMap(separated_codes[1]);
			for (int j = 0; j < expandedBarcode_left.size(); j++) {
				for (int k = 0; k < expandedBarcode_right.size(); k++) {
					arr.add(expandedBarcode_left.get(j).barcode + "+" + expandedBarcode_right.get(k).barcode);
				}
			}
		} else {
			System.out.println("Variation support only until 2");
		}
		return arr;
	}

	public static void SetNVariation(String[] originals, Map<String, Barcode> fullmap, int varlimit) {
		for (int i = 0; i < originals.length; i++) {
			String currentBar = originals[i];
			if (varlimit == 1) {
				LinkedList<Barcode> expandedBarcode = get1Variation(currentBar, fullmap);
				for (int j = 0; j < expandedBarcode.size(); j++) {
					Barcode temp = expandedBarcode.get(j);
					// System.out.println("Barcode Var1 " + temp.barcode + " : "
					// + temp.link.toString());
				}
			} else if (varlimit == 2) {
				LinkedList<Barcode> expandedBarcode2 = get2Variation(currentBar, fullmap);
				for (int j = 0; j < expandedBarcode2.size(); j++) {
					Barcode temp = expandedBarcode2.get(j);
					// System.out.println("Barcode Var2 " + temp.barcode + " : "
					// + temp.link.toString());
				}
			} else {
				System.out.println("Variation support only until 2");
			}
		}
	}

	public static LinkedList<Barcode> get1VariationWithoutMap(String barcode) {
		LinkedList<Barcode> bars = new LinkedList<Barcode>();
		char[] codeTable = { 'A', 'T', 'G', 'C' };

		char[] arr_bar = barcode.toCharArray();
		for (int i = 0; i < barcode.length(); i++) {
			for (int j = 0; j < 4; j++) {
				if (arr_bar[i] != codeTable[j]) {
					arr_bar[i] = codeTable[j]; // 4 codes change
					String change = new String(arr_bar);
					Barcode tempBar = new Barcode(change);
					tempBar.link.add(barcode + "_v1");
					bars.add(tempBar);
				}
				arr_bar[i] = barcode.charAt(i);
			}
		}
		return bars;
	}

	public static LinkedList<Barcode> get2VariationWithoutMap(String barcode) {
		LinkedList<Barcode> bars = new LinkedList<Barcode>();
		char[] codeTable = { 'A', 'T', 'G', 'C' };

		char[] arr_bar = barcode.toCharArray();

		for (int i = 0; i < barcode.length(); i++) {
			for (int j = i + 1; j < barcode.length(); j++) {
				for (int k = 0; k < 4; k++) {
					for (int l = 0; l < 4; l++) {
						if (arr_bar[i] != codeTable[k] && arr_bar[j] != codeTable[l]) {
							arr_bar[i] = codeTable[k]; // 4 codes change
							arr_bar[j] = codeTable[l]; // 4 codes change
							String change = new String(arr_bar);
							Barcode tempBar = new Barcode(change);
							bars.add(tempBar);
						}
						arr_bar[i] = barcode.charAt(i);
						arr_bar[j] = barcode.charAt(j);
					}
				}
			}
		}
		return bars;
	}

	public static LinkedList<Barcode> get1Variation(String barcode, Map<String, Barcode> fullMap) {
		LinkedList<Barcode> bars = new LinkedList<Barcode>();
		char[] codeTable = { 'A', 'T', 'G', 'C' };

		char[] arr_bar = barcode.toCharArray();
		for (int i = 0; i < barcode.length(); i++) {
			for (int j = 0; j < 4; j++) {
				if (arr_bar[i] != codeTable[j]) {
					arr_bar[i] = codeTable[j]; // 4 codes change
					String change = new String(arr_bar);
					Barcode tempBar = fullMap.get(change);
					tempBar.link.add(barcode + "_v1");
					fullMap.put(change, tempBar);
					bars.add(tempBar);
				}
				arr_bar[i] = barcode.charAt(i);
			}
		}
		return bars;
	}

	public static LinkedList<Barcode> get2Variation(String barcode, Map<String, Barcode> fullMap) {
		LinkedList<Barcode> bars = new LinkedList<Barcode>();
		char[] codeTable = { 'A', 'T', 'G', 'C' };

		char[] arr_bar = barcode.toCharArray();

		for (int i = 0; i < barcode.length(); i++) {
			for (int j = i + 1; j < barcode.length(); j++) {
				for (int k = 0; k < 4; k++) {
					for (int l = 0; l < 4; l++) {
						if (arr_bar[i] != codeTable[k] && arr_bar[j] != codeTable[l]) {
							arr_bar[i] = codeTable[k]; // 4 codes change
							arr_bar[j] = codeTable[l]; // 4 codes change

							String change = new String(arr_bar);
							Barcode tempBar = fullMap.get(change);
							tempBar.link.add(barcode + "_v2");
							fullMap.put(change, tempBar);
							bars.add(tempBar);

						}
						arr_bar[i] = barcode.charAt(i);
						arr_bar[j] = barcode.charAt(j);
					}
				}
			}
		}
		return bars;
	}

	public static void getSequentialBarcode(int baselen, String inst, Map<String, Barcode> storage) {
		if (inst.length() == baselen) {
			storage.put(inst, new Barcode(inst));
		} else {
			getSequentialBarcode(baselen, inst + "A", storage);
			getSequentialBarcode(baselen, inst + "G", storage);
			getSequentialBarcode(baselen, inst + "C", storage);
			getSequentialBarcode(baselen, inst + "T", storage);
		}
	}

	public static Map<String, Barcode> getAllPossibleBarcode(int baselen) {
		Map<String, Barcode> storage = new HashMap<String, Barcode>();
		getSequentialBarcode(baselen, "", storage);

		return storage;
	}

	public static boolean inOriginal(String[] barcode, String newcode) {
		for (int i = 0; i < barcode.length; i++) {
			if (newcode.compareTo(barcode[i]) == 0)
				return false;
		}
		return true;
	}

	public static Map<String, Integer> getAllBarcodeVariation(String[] barcode, int numOfVariation) {
		Hashtable<String, Integer> storeBar = new Hashtable<String, Integer>();
		char[] codeTable = { 'A', 'T', 'G', 'C' };

		for (int i = 0; i < barcode.length; i++) {
			String currentBar = barcode[i];
			// each barcode setting
			char[] arr_bar = currentBar.toCharArray();
			int len = currentBar.length();
			// System.out.println("currentBarcode : " +currentBar);
			for (int k = 0; k < len; k++) { // each location change
				for (int j = 0; j < 4; j++) {
					if (arr_bar[k] != codeTable[j]) {
						arr_bar[k] = codeTable[j]; // 4 codes change
						String change = new String(arr_bar);
						// System.out.println("changedBarcode : " + change);
						if (storeBar.get(change) == null && inOriginal(barcode, change))
							storeBar.put(change, i); // if the key is first, add
														// with the label
						else {
							// System.out.println("key duplicated");
							storeBar.remove(change); // if there is confict,
														// remove it.
						}
						// restore the character
						arr_bar[k] = currentBar.charAt(k);
					}
				}
			}
			// System.out.println("barcode : " + currentBar);
			/*
			 * for (Map.Entry<String, Integer> entry : storeBar.entrySet()) {
			 * String key = entry.getKey(); int value = entry.getValue();
			 * //System.out.println ("Key: " + key + " Value: " + value); }
			 */
		}
		return storeBar;
	}

	public int updateBarcode(String inFile, String outFile, Map<String, Integer> map) {
		System.out.println("MAP UPDATE START");
		File ft = new File(inFile);
		try {
			if (!ft.exists())
				ft.createNewFile();
			InputStream is = new FileInputStream(inFile);
			Reader rpt = new InputStreamReader(is, StandardCharsets.UTF_8);
			BufferedReader bread = new BufferedReader(rpt);

			OutputStream os = new FileOutputStream(outFile);
			Writer wrt = new OutputStreamWriter(os);
			BufferedWriter bwrite = new BufferedWriter(wrt);

			String str;
			int num = 0;
			while ((str = bread.readLine()) != null) {
				// System.out.println("File exist :" + str);
				num++;
				// 0 : barcode 1: number
				String[] store = str.split(" ");

				// System.out.printf("split check %s %s \n", store[0],
				// store[1]);
				if (map.get(store[0]) == null) {
					// if there is no match, write line itself.
					bwrite.write(str+"\n");
				} else {
					// if there is data match
					String temp = store[0];
					for (int i = 1; i < store.length - 1; i++) {
						temp += (" " + store[i]);
					}
					temp += (" " + map.get(store[0]));
					temp += "\n";
					//System.out.println(temp);
					bwrite.write(temp);
					map.put((store[0]), 0);
				}
			}

			for (Map.Entry<String, Integer> entry : map.entrySet()) {
				if (map.get(entry.getKey()) != 0) {
					bwrite.write(String.format("%s / %d\n", entry.getKey(), entry.getValue()));
					map.put(entry.getKey(), 0);
				}
			}
			bread.close();
			bwrite.close();

			// file change.
			File file = new File(inFile);
			File file2 = new File(outFile);

			file.delete();
			file = new File(inFile);
			file2.renameTo(file);

		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		System.out.println("MAP UPDATE END");
		return 0;

	}

	public void countUnits(String input_gzip_filepath, String output_filepath, String[] bar) {
		try {
			InputStream gzis = new GZIPInputStream(new FileInputStream(input_gzip_filepath));
			Reader decoder = new InputStreamReader(gzis, StandardCharsets.UTF_8);
			BufferedReader buffered = new BufferedReader(decoder);

			FileOutputStream out = new FileOutputStream(output_filepath);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));

			// each 4 lines has one pattern
			Map<String, Integer> map = new HashMap<String, Integer>();
			Map<String, Integer> storeBar = getAllBarcodeVariation(bar, 1);

			for (Map.Entry<String, Integer> entry : storeBar.entrySet())
				map.put(entry.getKey(), 0);
			for (int i = 0; i < bar.length; i++)
				map.put(bar[i], 0);

			String str;
			int total = 0;
			while ((str = buffered.readLine()) != null) {
				if (str.indexOf("@") != -1) {
					total++;
					str = str.substring(str.lastIndexOf(":") + 1);

					// if new barcode exist in changed barcode set
					if (map.get(str) != null) {
						map.put(str, map.get(str) + 1);
					}
				}
			}
			str = "";
			map = sortByValue(map);

			int[] counts = new int[bar.length];
			for (int j = 0; j < bar.length; j++)
				counts[j] = map.get(bar[j]);

			bw.write("barcode\tTotal\tfrequency \n");
			int sum = 0;

			for (Map.Entry<String, Integer> entry : storeBar.entrySet()) {
				counts[entry.getValue()] += map.get(entry.getKey());
				sum += map.get(entry.getKey());
			}

			for (int i = 0; i < bar.length; i++) {
				String line = String.format("%s\t%d\t%.6f\n", bar[i], counts[i], (float) counts[i] / sum);
				bw.write(line);
			}

			/*
			 * Set<String> keys = map.keySet(); // get all keys
			 * bw.write("barcode\tTotal\tfrequency \n"); for (String i : keys) {
			 * String line = String.format("%s\t%d\t%.6f\n", i, map.get(i),
			 * map.get(i) / (float)total); bw.write(line); }
			 */
			buffered.close();
			bw.close();

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void countAllUnits(String input_gzip_filepath, String output_filepath) {
		try {
			InputStream gzis = new GZIPInputStream(new FileInputStream(input_gzip_filepath));
			Reader decoder = new InputStreamReader(gzis, StandardCharsets.UTF_8);
			BufferedReader buffered = new BufferedReader(decoder);

			FileOutputStream out = new FileOutputStream(output_filepath);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));

			// each 4 lines has one pattern
			Map<String, Integer> map = new HashMap<String, Integer>();

			String str;
			int total = 0;
			while ((str = buffered.readLine()) != null) {
				if (str.indexOf("@") != -1) {
					total++;
					str = str.substring(str.lastIndexOf(":") + 1);

					// if new barcode exist in changed barcode set
					if (map.get(str) != null) {
						map.put(str, map.get(str) + 1);
					} else
						map.put(str, 1);
				}
			}
			str = "";
			map = sortByValue(map);

			Set<String> keys = map.keySet(); // get all keys
			bw.write("barcode\tTotal\tfrequency \n");
			for (String i : keys) {
				String line = String.format("%s\t%d\t%.6f\n", i, map.get(i), map.get(i) / (float) total);
				bw.write(line);
			}

			buffered.close();
			bw.close();

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public long countUnitsBasedFile(String input_gzip_filepath, String output_filepath, String[] bar, int maxSize,
			String basepath) {
		long total =0;
		try {
			System.out.println("Count Units Based File Function Start");
			InputStream gzis = new GZIPInputStream(new FileInputStream(input_gzip_filepath));
			Reader decoder = new InputStreamReader(gzis, StandardCharsets.UTF_8);
			BufferedReader br = new BufferedReader(decoder);

			FileOutputStream out = new FileOutputStream(output_filepath);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));

			// each 4 lines has one pattern
			Map<String, Integer> map = new HashMap<String, Integer>();

			String str;

			while ((str = br.readLine()) != null) {
				if (str.indexOf("@") != -1) {
					total ++;
					str = str.substring(str.lastIndexOf(":") + 1);

					// if new barcode exist in changed barcode set
					if (map.get(str) != null)
						map.put(str, map.get(str) + 1);
					else {
						map.put(str, 1);
					}
				}
				if (map.size() > maxSize) {
					// file i o to refresh hashmap.
					System.out.println(map.size());
					updateBarcode(basepath, "./hashout.txt", map);
					map.clear();
					System.out.println(map.size());
				}
			}
			System.out.println("last call");
			System.out.println(map.size());
			updateBarcode(basepath, "./hashout.txt", map);
			map.clear();
			System.out.println(map.size());
			
			br.close();
			bw.close();
			
			// file change.
			File file = new File(output_filepath);
			File file2 = new File(basepath);

			file.delete();
			file = new File(output_filepath);
			file2.renameTo(file);

		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return total;
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return -(o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

}
