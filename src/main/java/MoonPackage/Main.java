package MoonPackage;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import java.io.*;

import org.apache.commons.io.FileUtils;
import org.apache.sanselan.ImageInfo;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;



public class Main {
	
	private static String manhwaName = "keep-it-a-secret-from-your-mother";
	private static String workDirectory = "G:\\Moon Downloader\\";
	
	private static final String WORKING_DIRECTORY = workDirectory + manhwaName + "\\";
	
	private static String MAIN_URL = "https://toonily.com/webtoon/" + manhwaName + "/";
	private final static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36 OPR/78.0.4093.147";
	
	//getChapterById
	//private static String getChapterByIdUrl;// = "https://toonily.com/webtoon/secret-class/chapter-81/";
	private static final String GET_CHAPTER_BY_ID_POINT_FOR_NUMBER_OF_CHARTS = "wp-manga-chapter-img";
	
	//getNumberOfCharts
	private static final String GET_NUMBER_OF_CHAPTERS_URL = "https://toonily.com/wp-admin/admin-ajax.php";
	private static final String GET_NUMBER_OF_CHAPTERS_CONTENT_TYPE = "application/x-www-form-urlencoded; charset=UTF-8";
	private static String getNumberOfChaptersParam = "action=manga_get_chapters&manga=5523";
	
	//getImageById
	private static final String GET_IMAGE_BY_ID_DOWNLOADED_DIR = WORKING_DIRECTORY;
	private static final String GET_IMAGE_BY_ID_REFER_PARAM = "https://toonily.com/";
	
	private static String getCurrentTimeFormat()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date now = new Date();
	    String strDate = sdf.format(now);
	    return strDate;
	}
	
	private static String getIdOfManhwa() throws IOException
	{
		HttpURLConnection con;
		
	    URL myurl = new URL("https://toonily.com/webtoon/" + manhwaName + "/");
	    con = (HttpURLConnection) myurl.openConnection();

	    con.setRequestMethod("GET");
	    con.setRequestProperty("User-Agent", USER_AGENT);

	    try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
		    String line;
	
		    while ((line = br.readLine()) != null) {
		    	if(line.contains("\"disqusIdentifier\":\""))
		    	{
		    		return line.substring(line.indexOf("disqusIdentifier") + 19, line.indexOf(" https:"));
		    	}
		    }
	    }
		return null;
	}
	
	private static ArrayList<String> getChapterById(String urlToChapter) throws IOException 
	{
		ArrayList<String> pathToImages = new ArrayList<>();
		
		HttpURLConnection con;
		
	    URL myurl = new URL(urlToChapter);
	    con = (HttpURLConnection) myurl.openConnection();

	    con.setRequestMethod("GET");
	    con.setRequestProperty("User-Agent", USER_AGENT);

	    try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
		    String line;
	
		    while ((line = br.readLine()) != null) {
		    	if(line.contains(GET_CHAPTER_BY_ID_POINT_FOR_NUMBER_OF_CHARTS) && line.contains("jpg"))
		    	{
			        pathToImages.add(line.substring(line.indexOf("https:"), line.indexOf(".jpg")+4));
		    	}
		    	else if(line.contains(GET_CHAPTER_BY_ID_POINT_FOR_NUMBER_OF_CHARTS) && line.contains("png"))
		    	{
		    		pathToImages.add(line.substring(line.indexOf("https:"), line.indexOf(".png")+4));
		    	}
		    }
	    }
	    return pathToImages;

	}
	
	private static ArrayList<String> getNumberOfCharts() throws IOException
	{
		HttpURLConnection con;
	    byte[] postData = getNumberOfChaptersParam.getBytes(StandardCharsets.UTF_8);

	    URL myurl = new URL(GET_NUMBER_OF_CHAPTERS_URL);
	    con = (HttpURLConnection) myurl.openConnection();

	    con.setDoOutput(true);
	    con.setRequestMethod("POST");
	    con.setRequestProperty("User-Agent", USER_AGENT);
	    con.setRequestProperty("Content-Type", GET_NUMBER_OF_CHAPTERS_CONTENT_TYPE);

	    try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
	    	wr.write(postData);
	    }

	    ArrayList<String> chaptersList = new ArrayList<>();

	    try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
		    String line;
	
		    while ((line = br.readLine()) != null) {
		    	if(line.contains("https://toonily.com/webtoon/" + manhwaName + "/chapter-"))
		    	{
		    		chaptersList.add(line.substring(line.indexOf("/chapter-")+9, line.indexOf("/\"")));
		    	}
		    }
	    }
	    return chaptersList;
	}
	
	public static File getImageById(String urlToImage, String folderName) {

        try {
        	String downloadDir = GET_IMAGE_BY_ID_DOWNLOADED_DIR;
        	HttpURLConnection con;
            URL url = new URL(urlToImage);
            con = (HttpURLConnection) url.openConnection();
            
            con.setRequestProperty("Referer", GET_IMAGE_BY_ID_REFER_PARAM);
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setDoInput(true);
            con.setDoOutput(true);
            String filePathName = con.getURL().getFile();
            String fileName = filePathName.substring(filePathName.lastIndexOf("/") + 1);

            String path = downloadDir + File.separatorChar + folderName + "\\" + fileName;

            File file = new File(path);
            

            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            InputStream inputStream = con.getInputStream();

            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

            OutputStream outputStream = new FileOutputStream(file);

            int size;
            int len = 0;
            byte[] buf = new byte[1024];
            while ((size = bufferedInputStream.read(buf)) != -1) {
                len += size;
                outputStream.write(buf, 0, size);
                //System.out.println("Download progress:" + len * 100 / fileLength + "%\n");
            }
            outputStream.close();
            bufferedInputStream.close();
            inputStream.close();

            return file;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
	
	private static Point getResolutionOfImages(String path) throws IOException, ImageReadException
	{
		Point point = new Point();
		
//		BufferedImage imageSize;
//        
        File input = new File(WORKING_DIRECTORY + path);
//        imageSize = ImageIO.read(input);
//        point.x = imageSize.getWidth();
//        point.y = imageSize.getHeight();
		
		ImageInfo imageInfo = Sanselan.getImageInfo(input);

		point.x = imageInfo.getWidth();
		point.y = imageInfo.getHeight();
        
		return point;
	}
	
	private static void createPdfFilesFromImages(String chapter) throws DocumentException, MalformedURLException, IOException, ImageReadException
	{
		Point point = new Point();
		File root = new File(WORKING_DIRECTORY);
		File images = new File(WORKING_DIRECTORY + chapter);
		String[] listOfFiles = images.list();
		
        String outputFile = "Chapter - " + chapter + ".pdf";
        ArrayList<String> files = new ArrayList<String>();
        
        for(int i = 0; i < listOfFiles.length; i++)
        	files.add(chapter + "\\" + listOfFiles[i]);
        
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(new File(root, outputFile)));
        document.open();
        for (String f : files) {
        	System.out.println(getCurrentTimeFormat() + " | " + "Entry" + " | " + "Checking the image resoultion for " + f.substring(0, f.indexOf(".")) + " | " + "Chapter " + chapter);
        	point = getResolutionOfImages(f);
        	System.out.println(getCurrentTimeFormat() + " | " + "Exit" + " | " + "Checking the image resoultion for " + f.substring(0, f.indexOf(".")) + " | " + "Chapter " + chapter);
        	if(point.y > 14400)
        	{
        		cropLargeImages(f, point);		
        		addCroppedImages(f, point, document, root);
        		continue;
        	}
        	
        	document.setPageSize(new Rectangle(point.x, point.y));
            document.newPage();
            System.out.println(getCurrentTimeFormat() + " | " + "New page has been added to the PDF file" + " | " + "Chapter " + chapter);
            Image image = Image.getInstance(new File(root, f).getAbsolutePath());
            image.setAbsolutePosition(0, 0);
            image.setBorderWidth(0);
            image.setScaleToFitHeight(true);
            document.add(image);
        }
        document.close();
	}
	
	private static void cropLargeImages(String filename, Point point) throws IOException
	{
		System.out.println(getCurrentTimeFormat() + " | " + "A large image has been found" + " | " + "image " + filename.substring(0, filename.indexOf(".")));
		
		File file = new File(WORKING_DIRECTORY + filename);
		
		BufferedImage src = ImageIO.read(file);
		
		int count = point.y / 14400;
		if(point.y % 14400 > 0)
			count++;
		
		for(int i = 0; i < count; i++)
		{
			BufferedImage img;
			if(i != (point.y / 14400))
			{
				img = new BufferedImage(point.x, 14400, BufferedImage.TYPE_INT_RGB);
				img.getGraphics().drawImage(src, 0, 0, point.x, 14400, 0, i * 14400, point.x, (i+1) * 14400, null);
			}
			else
			{
				img = new BufferedImage(point.x, (point.y - (i * 14400)), BufferedImage.TYPE_INT_RGB);
				img.getGraphics().drawImage(src, 0, 0, point.x, (point.y - (i * 14400)), 0, i * 14400, point.x, (i * 14400) + (point.y - (i * 14400)), null);
			}
			ImageIO.write(img, "jpg", new File(WORKING_DIRECTORY + filename.substring(0, filename.indexOf(".")) + "_" + i + "."));
			System.out.println(getCurrentTimeFormat() + " | " + "The cropped " + filename.substring(0, filename.indexOf(".")) + " image has been created");
		}
	}
	
	private static void addCroppedImages(String filename, Point point, Document document, File root) throws MalformedURLException, IOException, DocumentException
	{        
		int count = point.y / 14400;
		if(point.y % 14400 > 0)
			count++;
		
		for(int i = 0; i < count; i++)
		{
			if(i != (point.y / 14400))
				document.setPageSize(new Rectangle(point.x, 14400));
			else
				document.setPageSize(new Rectangle(point.x, point.y - (i * 14400)));
			
			document.newPage();
	        Image image = Image.getInstance(new File(root, filename.substring(0, filename.indexOf(".")) + "_" + i + ".").getAbsolutePath());
	        image.setAbsolutePosition(0, 0);
	        image.setBorderWidth(0);
	        image.setScaleToFitHeight(true);
	        document.add(image);
			System.out.println(getCurrentTimeFormat() + " | " + "The cropped " + filename.substring(0, filename.indexOf(".")) + " image has added to the PDF file");
		}
	}
	
	public static void main(String[] args) throws IOException, URISyntaxException, DocumentException, ImageReadException {
		
		getNumberOfChaptersParam = "action=manga_get_chapters&manga=" + getIdOfManhwa();
		System.out.println(getCurrentTimeFormat() + " | " + "Manhwa params were created" + " | " + "Params: " + "\"" + getNumberOfChaptersParam + "\"");
		
		ArrayList<String> pathToImages;
		
		//1. Get number of chapters for Manhwa
		ArrayList<String> chaptersNameList = new ArrayList<>();
		chaptersNameList = getNumberOfCharts();
		Collections.reverse(chaptersNameList);
		
		//for(int i = 85; i < chaptersNameList.size(); i ++)
		for(int i = 0; i < chaptersNameList.size(); i++)
		{
			File checkPdfFile = new File(WORKING_DIRECTORY + "Chapter - " + chaptersNameList.get(i) + ".pdf");
			if(!checkPdfFile.exists())
			{			
				System.out.println(getCurrentTimeFormat() + " | " + "The following PDF file does NOT exist Chapter - " + chaptersNameList.get(i));
				//2. Get number of images for certain chapter.
				System.out.println("https://toonily.com/webtoon/" + manhwaName + "/chapter-" + chaptersNameList.get(i) + "/");
				pathToImages = getChapterById("https://toonily.com/webtoon/" + manhwaName + "/chapter-" + chaptersNameList.get(i) + "/");
				for(int j = 0; j < pathToImages.size(); j++)
				{
					//3. Download images
					getImageById(pathToImages.get(j), chaptersNameList.get(i));
					System.out.println(getCurrentTimeFormat() + " | " + "Downloading" + " | " + "Chapter #" + chaptersNameList.get(i) + " | " + "URL - " + pathToImages.get(j)); 
				}
				//4. Create pdf from images
				createPdfFilesFromImages(chaptersNameList.get(i));
				System.out.println(getCurrentTimeFormat() + " | " + "Chapter - " + chaptersNameList.get(i) + ".pdf was created");
				//5. Delete folder with images
				FileUtils.deleteDirectory(new File(WORKING_DIRECTORY + chaptersNameList.get(i)));
				System.out.println(getCurrentTimeFormat() + " | " + "The folder with temp images for " + chaptersNameList.get(i) + " chapter has been removed");
			}
			else
			{
				System.out.println(getCurrentTimeFormat() + " | " + "The following PDF file exists Chapter - " + chaptersNameList.get(i));
			}
		}
		
	}

}
