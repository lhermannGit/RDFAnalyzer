package util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class ArraySerializer {

	public static void main(String[] args) {
	    //deserialize the quarks.ser file
	    try(
	      InputStream file = new FileInputStream("/home/martin/output-test.ser"); // TODO path
	      InputStream buffer = new BufferedInputStream(file);
	      ObjectInput input = new ObjectInputStream (buffer);
	    ){
	      //deserialize the List
	      int[] testDeser = (int[])input.readObject();
	      //display its data
	      for(int i: testDeser){
	    	  System.out.println(i);
	      }
	    }
	    catch(ClassNotFoundException ex){
	    	ex.printStackTrace();
	    }
	    catch(IOException ex){
	    	ex.printStackTrace();
	    }
	}

	/**
	 * based on code from: http://www.javapractices.com/topic/TopicAction.do?Id=57
	 * @param array
	 */
	public void serializeArray(String outputFilePath, int[] array){
	    try (
	      OutputStream file = new FileOutputStream(outputFilePath);
	      OutputStream buffer = new BufferedOutputStream(file);
	      ObjectOutput output = new ObjectOutputStream(buffer);
	    ){
	      output.writeObject(array);
	    }  
	    catch(IOException ex){
	    	ex.printStackTrace();
	    }
	}
	/**
	 * based on code form: http://www.javapractices.com/topic/TopicAction.do?Id=57
	 * @param inputFilePath
	 * @return
	 */
	public int[] deserializeArray(String inputFilePath){
		int[] array=null;
	    try(
	      InputStream file = new FileInputStream(inputFilePath);
	      InputStream buffer = new BufferedInputStream(file);
	      ObjectInput input = new ObjectInputStream (buffer);
	    ){
	      //deserialize the array
	      array = (int[])input.readObject();
	    }
	    catch(ClassNotFoundException ex){
	    	ex.printStackTrace();
	    }
	    catch(IOException ex){
	    	ex.printStackTrace();
	    }
		
	    return array;
	}
}
