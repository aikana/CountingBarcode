# CountingBarcode
I am editing this Program only the counting function.

Feature
JAVA 1.8 based code
Read compressed fastq file by gzip and count barcode with possible 1, 2 variation(using java.util.zip.GZIPInputStream)

Performance
8+8 base reading for 200GB will take less than 10 min in dual core laptop with 100000 key limitation for map.
Other than 8+8 base case, just seconds.

Result 4 files
1. Artifect.txt
2. Original.txt
3. Variation.txt
4. Trash.txt (variation conflict)

Basic logic
1. Find length of base and decide which logic the program will follow

For 6, 8, 6+6(dual) base
2. make full list of all possible list in one base file, and make required variation mark for each given barcodes.
For 8+8 base: full list of 8+8 base are too big(more than hundreds GB for base file)
2. For 8+8 base, make given barcodes and its variation with their connection to given barcodes.

3. While reading a file, manage map for each key they contain until maximun number you set(memory wise)
4. When they reach certain number you set inside of map, the program update the base file.
5. repeat 3-4 until the end of the sequencing file you input.

This program has little more categorization to each given barcodes(sequence) than Illumina basespace system.
But I am still working on making better speed of the program. If you have any idea about this program, Drop an email to me.
I can also provide more functions for statics of fastq file.

aikana.wo@gmail.com / zaizhenl@usc.edu
