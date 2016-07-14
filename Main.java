
import com.mpatric.mp3agic.BaseException;
import com.mpatric.mp3agic.BufferTools;
import com.mpatric.mp3agic.EncodedText;
import com.mpatric.mp3agic.ID3Wrapper;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v1Tag;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v23Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Scanner;

//TODO: Implement genres
//TODO: Try/catch for other errors thrown: 
//UnsupportedTagException, IOException, NotSupportedException, NullPointerException

/*Mk 1.1 Changelist:
 * 
 * Added implementations for audio files that do not conform to standard
 * re-track naming principals, including customized name and artist. 
 * 
 * Added a try-catch that detects when the data of an mp3 is incompatible with
 * mp3agic. 
 * 
 * Added a new message upon termination of the program.
 * 
 * Updated file replacement success/error counters. 
 * 
 */

public class Main {
	public static Boolean terminateProgram = false;
	public static final Scanner scnr = new Scanner(System.in);
	
	private static String appMenu(){
		System.out.println("To begin changing tags, press b.");
		System.out.println("To exit the program, press x.");
		String usrIn = scnr.nextLine();
		return verifyInput(usrIn);
	}
	
	private static String verifyInput(String s){
		if(!(s.equals("b") || s.equals("x"))){
			System.out.println("Invalid input. Please try again.");
			s = scnr.nextLine();
			return verifyInput(s);
		}
		else{
			return s;
		}
	}
	
	private static void initMetadata(String[] args) throws 
	UnsupportedTagException, InvalidDataException, IOException, NotSupportedException{
		File dir = new File(args[0]);
		File[] directoryListing = dir.listFiles();
		int tracknum1 = 1;
		System.out.println("The following tracks were found:");
		for(File file: directoryListing){
			System.out.println(file.getName());
			tracknum1++;
		}
		int tracknum = 0;
		int replacementCounter = 0;
		int errorCounter = 0;
		boolean nameIsAbnormal = false;
		System.out.print("Enter desired album name for MP3s: ");
		String albumName = scnr.nextLine();
		//System.out.print("Enter desired genre for MP3s: ");
		//String genre = scnr.nextLine();
		String customName = null;
		String customArtist = null;
		System.out.print("Enter desired year for MP3s: ");
		String year = scnr.nextLine();
		for(File songFile: directoryListing){
			//skip this file
			if(songFile.getName().equals(".DS_Store")){return;}
			
			//case with irregular naming convention
			else if(!songFile.getName().contains("-")){
				System.out.println();
				System.out.println("Warning: the current audio file detected does not conform");
				System.out.println("to \"Song Name - Song Artist.mp3\" naming convention.");
				System.out.println("File name is: " + songFile.getName());
				System.out.print("To ignore this file, press i. Else press any key: ");
				String userIgnoreChoice = scnr.nextLine();
				if(userIgnoreChoice.equals("i")){
					return;
				}
				else{
					System.out.print("Please enter custom file name now: ");
					customName = scnr.nextLine();
					System.out.print("Please enter custom artist name now: ");
					customArtist = scnr.nextLine();
					nameIsAbnormal = true;
				}
				
			//normal song input path
			}
			//creating new mp3 file
			Mp3File mp3file;
			//trying to convert to mp3file
		try{
			mp3file = new Mp3File(songFile.getName());
			//get filepath of song
			//String filePath = songFile.getPath();
			//creating new ID3 tag object
			ID3v2 id3v2Tag = mp3file.getId3v2Tag();
			String[] splitSongName = null;
			if(nameIsAbnormal == false){
				//begin editing name automatically
				splitSongName = id3v2Tag.getTitle().split("-", 2); 
			}
			//check to make sure the file had both halfs of the split
			//i.e. ensure no stray audio file is in the directory
			//once name is saved in variable, wipe the tag		
			mp3file.removeId3v2Tag();
			//create new IDEv1 tag bc v2's are a piece of shit and outdated
			ID3v1 id3v1Tag;
			// remove tag if it has them
			if (mp3file.hasId3v1Tag()) {
				id3v1Tag =  mp3file.getId3v1Tag();
			} else {
				//make new tag and set it to the mp3
				id3v1Tag = new ID3v1Tag();
				mp3file.setId3v1Tag(id3v1Tag);
			}
			//setting track
			id3v1Tag.setTrack(Integer.toString(tracknum));
			//setting album name from input
			id3v1Tag.setAlbum(albumName);
			//setting year from input
			id3v1Tag.setYear(year);
			//branch if song naming convention was not irregular
			//if name was not irregular, customName will still be null
			if(customName == null){
				//setting spliced title
				id3v1Tag.setTitle(splitSongName[0].trim());
				//setting spliced artist
				id3v1Tag.setArtist(splitSongName[1].trim());
				//saving mp3 with new name
				mp3file.save(splitSongName[0].trim() + ".mp3");
			}
			else if(customName != null){
				//setting title as custom name 
				id3v1Tag.setTitle(customName.trim());
				//setting artist as custom artist
				id3v1Tag.setArtist(customArtist.trim());
				//saving mp3 with new name.
				mp3file.save(customName.trim() + ".mp3");
			}
		}
		catch(InvalidDataException d){
			System.out.println("Current file is an invalid filetype.");
			System.out.println("Please ensure file is of type .mp3 and");
			System.out.println("has been converted by iTunes (not Audacity).");
			System.out.println("Skipping current song. Title: " + songFile.getName());
				
		}
			
			
			//deleting old track and upping counters
			if(songFile.delete()){ replacementCounter++; }
			
			else { errorCounter++; }
			
			tracknum++;	 
			
		}
		System.out.println(replacementCounter + " files replaced successfully");
		System.out.println(errorCounter + " files not replaced");
	}
	
	public static void main(String[] args) throws 
	UnsupportedTagException, InvalidDataException, IOException, NotSupportedException {
		
		System.out.println("Mk 1.1 Metadata Fixer");
		System.out.println("12-25-2015 (c) EFK");
		System.out.println();
		if(appMenu().equals("b")){
			initMetadata(args);	
			System.out.println("Program has completed. Now exiting...");
		}
		else{
			System.out.println("Exiting...");
			System.exit(0);
		}	
	}
}
