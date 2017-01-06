package hohistar.sinde.baselibrary.utility;

import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Utility_File {
	
	public final static String PATH = Environment.getExternalStorageDirectory().toString();
	
	/**
	 * create file by absolute path,if the parents is not exists that will mkdirs 
	 * **/
	public static File createFile(String path){
		File file = new File(path);
		if(!file.getParentFile().exists()){
			file.getParentFile().mkdirs();
		}
		try {
			if(!file.exists())
				file.createNewFile();
			return file;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * create new folder by path, 
	 * **/
	public static File createFiles(String path){
		File file  = new File(path);
		if(!file.exists()){
			file.mkdirs();
		}
		return file;
	}
	
	/**
	 * delete file,if this is folder that will delete all children
	 * **/
	public static boolean delete(final File file){
		if(file.exists() && file.isDirectory()){
			for(File child : file.listFiles()){
				if(child.isDirectory()){
					delete(child);
				}else{
					child.delete();
				}
			}
		}
		return file.delete();
	}
	
	/**
	 * delete file by absolute path,if this is folder that will delete all children
	 * **/
	public static boolean delete(final String path){
		if (path == null)
			return false;
		File file = new File(path);
		if(file.exists() && file.isDirectory()){
			for(File child : file.listFiles()){
				if(child.isDirectory()){
					delete(child);
				}else{
					child.delete();
				}
			}
		}
		return file.delete();
	}
	
	/**
	 * read data from file
	 * **/
	public static byte[] read(final File file){
		ByteArrayOutputStream baos = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			int length = fis.available();
			int ls = length/5;
			byte[] buffer = new byte[ls];
			baos = new ByteArrayOutputStream();
			int tempLength;
			while ( (tempLength = fis.read(buffer, 0, ls)) != -1) {
				if (tempLength < ls) {
					byte[] temp = new byte[tempLength];
					System.arraycopy(buffer, 0, temp, 0, tempLength);
					baos.write(temp);
				}else
					baos.write(buffer);
			}
			fis.close();
			return baos.toByteArray();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				if (baos != null)
					baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * read data from file path
	 * **/
	public static byte[] read(final String filePath){
		try {
			File file = new File(filePath);
			return read(file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * you should close inputStream which returned
	 * **/
	public static InputStream readI(final String path){
		BufferedInputStream bis = null;
		try {
			File inf = new File(path);
			FileInputStream fios = new FileInputStream(inf);
			bis = new BufferedInputStream(fios);
//			fios.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return bis;
	}
	
	/**
	 * write byte[] to file , append that will write data to the file tail
	 * **/
	public static void write(final byte[] data,final File file,boolean append){
		try {
			FileOutputStream fos = new FileOutputStream(file,append);
			fos.write(data, 0, data.length);
			fos.flush();
			fos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	/**
	 * write byte[] to file , append that will write data to the file tail
	 * **/
	public static void write(final byte[] data,final String filePath,boolean append){
		try {
			File file = createFile(filePath);
			FileOutputStream fos = new FileOutputStream(file,append);
			fos.write(data, 0, data.length);
			fos.flush();
			fos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	/**
	 * this is used to write big data to desFile
	 * **/
	public static void write(final InputStream in,final String filePath,boolean append){
		try {
			byte[] buffer = new byte[1024];
			File outFile = createFile(filePath);
			FileOutputStream fous = new FileOutputStream(outFile,append);
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fous);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(in);
			int tempLength = 0;
			while((tempLength = bufferedInputStream.read(buffer, 0, 1024)) != -1){
				bufferedOutputStream.write(buffer, 0, tempLength);
			}
			bufferedInputStream.close();
			bufferedOutputStream.flush();
			bufferedOutputStream.close();
			fous.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean copy(File dst,File src){
		if (src.exists()){
			InputStream in = null;
			try {
				in = readI(src.getAbsolutePath());
				if (in != null){
					write(in,dst.getAbsolutePath(),false);
				}
			}catch (Exception e){
				e.printStackTrace();
			}finally {
				if (in != null){
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}
		return false;
	}

	public static void writeObjectToFile(Object obj,String path) {
		File file =new File(path);
		createFiles(file.getParent());
		FileOutputStream out;
		try {
			out = new FileOutputStream(file);
			ObjectOutputStream objOut=new ObjectOutputStream(out);
			objOut.writeObject(obj);
			objOut.flush();
			objOut.close();
			System.out.println("write object success!");
		} catch (IOException e) {
			System.out.println("write object failed");
			e.printStackTrace();
		}
	}

	public static <T> T readObjectFromFile(String path,Class<T> clazz) {
		T temp=null;
		File file =new File(path);
		FileInputStream in;
		try {
			if (file.exists()){
				in = new FileInputStream(file);
				ObjectInputStream objIn=new ObjectInputStream(in);
				Object obj = objIn.readObject();
				if (obj != null){
					temp = (T)obj;
				}
				objIn.close();
				System.out.println("read object success!");
			}
		} catch (IOException e) {
			System.out.println("read object failed");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return temp;
	}

}
